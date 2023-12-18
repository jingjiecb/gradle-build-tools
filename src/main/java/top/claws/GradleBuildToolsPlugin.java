/* (C)2023 */
package top.claws;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import io.freefair.gradle.plugins.lombok.LombokPlugin;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import org.gradle.testing.jacoco.tasks.JacocoReport;

public class GradleBuildToolsPlugin implements Plugin<Project> {
    private GradleBuildToolsExtension extension;

    @Override
    public void apply(Project project) {
        createExtension(project);
        applyCheckstyle(project);
        applySpotless(project);
        applyJacoco(project);
        applyLombok(project);
    }

    private void createExtension(Project project) {
        extension =
                project.getExtensions().create("gradleBuildTools", GradleBuildToolsExtension.class);
    }

    private void applyCheckstyle(Project project) {
        project.getPlugins()
                .withType(
                        JavaPlugin.class,
                        javaPlugin -> {
                            project.getPlugins().apply(CheckstylePlugin.class);
                            configureCheckstyle(project);
                        });
    }

    private void configureCheckstyle(Project project) {
        CheckstyleExtension checkstyle =
                project.getExtensions().findByType(CheckstyleExtension.class);
        if (checkstyle != null) {
            checkstyle.setIgnoreFailures(false);
            URL resourceUrl = getClass().getClassLoader().getResource("checkstyle.xml");
            if (resourceUrl != null) {
                checkstyle.setConfig(project.getResources().getText().fromUri(resourceUrl));
            }
            URL suppressionRules = getClass().getClassLoader().getResource("suppressions.xml");
            if (suppressionRules != null) {
                checkstyle.setConfigProperties(
                        Map.of("checkstyle.suppression.filter", project.uri(suppressionRules)));
            }
        }
    }

    private void applySpotless(Project project) {
        project.getPlugins()
                .withType(
                        JavaPlugin.class,
                        javaPlugin -> {
                            project.getPlugins().apply(SpotlessPlugin.class);
                            configureSpotless(project);
                            createFormatTask(project);
                        });
    }

    private void createFormatTask(Project project) {
        Task formatTask = project.getTasks().create("format");
        formatTask.dependsOn("spotlessApply");
    }

    private void configureSpotless(Project project) {
        SpotlessExtension spotless = project.getExtensions().findByType(SpotlessExtension.class);
        if (spotless != null) {
            spotless.format(
                    "misc",
                    formatExtension -> {
                        formatExtension.target("*.gradle", ".gitattributes", ".gitignore");
                        formatExtension.trimTrailingWhitespace();
                        formatExtension.indentWithTabs();
                        formatExtension.endWithNewline();
                    });
            spotless.java(
                    javaExtension -> {
                        javaExtension
                                .googleJavaFormat("1.17.0")
                                .aosp()
                                .reflowLongStrings()
                                .skipJavadocFormatting();
                        javaExtension.formatAnnotations();
                        javaExtension.licenseHeader("/* (C)$YEAR */");
                    });
        }
    }

    private void applyJacoco(Project project) {
        project.getPlugins()
                .withType(
                        JavaPlugin.class,
                        javaPlugin -> {
                            project.getPlugins().apply(JacocoPlugin.class);
                            configureJacoco(project);
                        });
    }

    private void configureJacoco(Project project) {
        project.getTasks()
                .withType(JacocoReport.class)
                .configureEach(
                        jacocoReport -> {
                            List<String> excludeFiles = extension.getExcludeFiles().get();
                            jacocoReport
                                    .getClassDirectories()
                                    .setFrom(
                                            project.files(
                                                    jacocoReport
                                                            .getClassDirectories()
                                                            .getFiles()
                                                            .stream()
                                                            .map(
                                                                    file ->
                                                                            project.fileTree(
                                                                                    file,
                                                                                    fileTree -> {
                                                                                        fileTree
                                                                                                .exclude(
                                                                                                        excludeFiles);
                                                                                    }))
                                                            .toArray()));
                        });

        project.getTasks()
                .withType(JacocoCoverageVerification.class)
                .configureEach(
                        jacocoCoverageVerification -> {
                            List<String> excludeFiles = extension.getExcludeFiles().get();
                            jacocoCoverageVerification
                                    .getClassDirectories()
                                    .setFrom(
                                            project.files(
                                                    jacocoCoverageVerification
                                                            .getClassDirectories()
                                                            .getFiles()
                                                            .stream()
                                                            .map(
                                                                    file ->
                                                                            project.fileTree(
                                                                                    file,
                                                                                    fileTree -> {
                                                                                        fileTree
                                                                                                .exclude(
                                                                                                        excludeFiles);
                                                                                    }))
                                                            .toArray()));
                            jacocoCoverageVerification
                                    .getViolationRules()
                                    .rule(
                                            jacocoViolationRule -> {
                                                jacocoViolationRule.limit(
                                                        jacocoLimit -> {
                                                            jacocoLimit.setCounter("LINE");
                                                            jacocoLimit.setMinimum(
                                                                    BigDecimal.valueOf(0.95));
                                                        });
                                                jacocoViolationRule.limit(
                                                        jacocoLimit -> {
                                                            jacocoLimit.setCounter("BRANCH");
                                                            jacocoLimit.setMinimum(
                                                                    BigDecimal.valueOf(0.95));
                                                        });
                                            });
                        });

        project.getTasks()
                .named("check")
                .configure(
                        task -> {
                            task.dependsOn("jacocoTestCoverageVerification");
                        });
    }

    private void applyLombok(Project project) {
        project.getPlugins().apply(LombokPlugin.class);
        configureLombok(project);
    }

    private void configureLombok(Project project) {
        project.getTasks().register("copyDefaultLombokConfig", CopyLombokConfigTask.class);
    }
}
