# Gradle Build Tools 

## What is Gradle Build Tools?

This is a gradle plugin which contains most useful tools for developers who are working on Java project with Gradle build system.

This plugin contains:
1. [Checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) and a set of default rules.
2. [Spotless](https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless)
3. [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html), and default coverage limit.
4. [Lombok](https://plugins.gradle.org/plugin/io.freefair.lombok)

## How to use Gradle Build Tools?

Apply and configure this plugin is very complicated. Just kidding. There are only one line you need to apply this plugin to your project's gradle build script: 

For those who use kotlin(your build script is build.gradle.kts)

```kotlin

```

or Groovy(your build script is build.gradle as usual): 

```groovy

```

### Automatically generate a lombok config file for your project.

Now Lombok can be configured to add an annotation on the generated code to tell Jacoco auto-exclude them from coverage report. I add a config file which enable Lombok to do this in this plugin. If you want to copy one to your project, just run `copyDefaultLombokConfig` task.


## How to configure? 

This plugin essentially automatically apply the needed plugin to your project. Therefore, you can still configure each plugin the way you directly apply them. But I also provide some more convenient method to configure some commonly-used thing. 

### Configure the Jacoco exclude files:

You only need to configure the gradleBuildTools plugin as the following: 

```kotlin
gradleBuildTools {
    excludeFiles.set(listOf("*/any/file.*", "you/want/to.exclude"))
}
```

```groovy
gradleBuildTools {
    excludeFiles = ['*/any/file.*', 'you/want/to.exclude']
}
```

---

Hope this plugin help you insist on the highest code quality bar and build something awesome. 