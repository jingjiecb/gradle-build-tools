package top.claws;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GradleBuildToolsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        createExtension(project);
        applyCheckstyleToProject(project);
        applySpotlessToProject(project);
        applyJacocoToProject(project);
    }

    private void createExtension(Project project) {
        GradleBuildToolsExtension extension = project.getExtensions().create("gradleBuildTools", GradleBuildToolsExtension.class);
        extension.getExcludeFiles().convention(new ArrayList<>());
    }

    private void applyCheckstyleToProject(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            project.getPlugins().apply(CheckstylePlugin.class);
            configureCheckstylePlugin(project);
        });
    }

    private void configureCheckstylePlugin(Project project) {
        CheckstyleExtension checkstyle = project.getExtensions().findByType(CheckstyleExtension.class);
        if (checkstyle != null) {
            checkstyle.setIgnoreFailures(false);
            URL resourceUrl = getClass().getClassLoader().getResource("checkstyle.xml");
            if (resourceUrl != null) {
                checkstyle.setConfig(project.getResources().getText().fromUri(resourceUrl));
            }
            URL suppressionRules = getClass().getClassLoader().getResource("suppressions.xml");
            if (suppressionRules != null) {
                checkstyle.setConfigProperties(Map.of("checkstyle.suppression.filter", project.uri(suppressionRules)));
            }
        }
    }

    private void applySpotlessToProject(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            project.getPlugins().apply(SpotlessPlugin.class);
            configureSpotlessToProject(project);
        });
    }

    private void configureSpotlessToProject(Project project) {
        SpotlessExtension spotless = project.getExtensions().findByType(SpotlessExtension.class);
        if(spotless != null) {
            spotless.format("misc", formatExtension -> {
                formatExtension.target("*.gradle", ".gitattributes", ".gitignore");
                formatExtension.trimTrailingWhitespace();
                formatExtension.indentWithTabs();
                formatExtension.endWithNewline();
            });
            spotless.java(javaExtension -> {
                javaExtension.googleJavaFormat("1.17.0").aosp().reflowLongStrings().skipJavadocFormatting();
                javaExtension.formatAnnotations();
                javaExtension.licenseHeader("/* (C)$YEAR */");
            });
        }
    }

    private void applyJacocoToProject(Project project) {
        project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
            project.getPlugins().apply(JacocoPlugin.class);
            configureJacocoToProject(project);
        });
    }
    private void configureJacocoToProject(Project project) {
        project.getTasks().withType(JacocoReport.class).configureEach(jacocoReport -> {
            List<String> excludeFiles = project.getExtensions().findByType(GradleBuildToolsExtension.class).getExcludeFiles().get();
            jacocoReport.getClassDirectories().setFrom(project.files(jacocoReport.getClassDirectories().getFiles().stream().map(file -> project.fileTree(file, fileTree -> {
                fileTree.exclude(excludeFiles);
            })).toArray()));
        });

        project.getTasks().withType(JacocoCoverageVerification.class).configureEach(jacocoCoverageVerification -> {
            List<String> excludeFiles = project.getExtensions().findByType(GradleBuildToolsExtension.class).getExcludeFiles().get();
            jacocoCoverageVerification.getClassDirectories().setFrom(project.files(jacocoCoverageVerification.getClassDirectories().getFiles().stream().map(file -> project.fileTree(file, fileTree -> {
                fileTree.exclude(excludeFiles);
            })).toArray()));
            jacocoCoverageVerification.getViolationRules().rule(jacocoViolationRule -> {
                jacocoViolationRule.limit(jacocoLimit -> {
                    jacocoLimit.setCounter("LINE");
                    jacocoLimit.setMinimum(BigDecimal.valueOf(0.95));
                });
                jacocoViolationRule.limit(jacocoLimit -> {
                    jacocoLimit.setCounter("BRANCH");
                    jacocoLimit.setMinimum(BigDecimal.valueOf(0.95));
                });
            });
        });

        project.getTasks().getByName("check").dependsOn(project.getTasks().getByName("jacocoTestCoverageVerification"));
    }
}
