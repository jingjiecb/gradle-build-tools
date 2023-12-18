/* (C)2023 */
package top.claws;

import org.gradle.api.provider.ListProperty;

public abstract class GradleBuildToolsExtension {
    public abstract ListProperty<String> getExcludeFiles();
}
