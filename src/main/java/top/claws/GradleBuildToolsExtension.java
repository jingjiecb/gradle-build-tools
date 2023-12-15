package top.claws;

import org.gradle.api.provider.ListProperty;

abstract public class GradleBuildToolsExtension {
    abstract public ListProperty<String> getExcludeFiles();
}
