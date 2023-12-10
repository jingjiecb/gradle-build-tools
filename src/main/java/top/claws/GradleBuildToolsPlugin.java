package top.claws;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;

import java.net.URL;
import java.util.Map;

public class GradleBuildToolsPlugin implements Plugin<Settings> {
    @Override
    public void apply(Settings settings) {
        settings.getGradle().allprojects(this::applyCheckstyleToProject);
        settings.getGradle().allprojects(this::applySpotlessToProject);
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
                javaExtension.googleJavaFormat("1.12.0").aosp().reflowLongStrings().skipJavadocFormatting();
                javaExtension.formatAnnotations();
                javaExtension.licenseHeader("/* (C)$YEAR */");
            });
        }
    }
}
