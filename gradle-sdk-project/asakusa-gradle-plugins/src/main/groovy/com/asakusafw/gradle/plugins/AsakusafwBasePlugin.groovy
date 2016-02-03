/*
 * Copyright 2011-2016 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.util.GradleVersion

import com.asakusafw.gradle.plugins.internal.AsakusafwInternalPluginConvention

/**
 * Base class of Asakusa Framework Gradle Plugin.
 * @since 0.5.3
 * @version 0.8.0
 */
class AsakusafwBasePlugin implements Plugin<Project> {

    /**
     * The task name of printing versions information.
     * @since 0.8.0
     */
    static final String TASK_VERSIONS = 'asakusaVersions'

    /**
     * The task name of printing versions information.
     * @since 0.8.0
     */
    static final String TASK_UPGRADE = 'asakusaUpgrade'

    private static final String PREFIX_INFO_PATH = 'META-INF/asakusa-gradle/'

    private static final String ARTIFACT_INFO_PATH = PREFIX_INFO_PATH + 'artifact.properties'

    private static final String DEFAULTS_INFO_PATH = PREFIX_INFO_PATH + 'defaults.properties'

    private static final String INVALID_VERSION = 'INVALID'

    private Project project

    private AsakusafwBaseExtension extension

    /**
     * Applies this plug-in and returns the extension object for the project.
     * @param project the target project
     * @return the corresponded extension
     */
    static AsakusafwBaseExtension get(Project project) {
        project.apply plugin: AsakusafwBasePlugin
        return project.plugins.getPlugin(AsakusafwBasePlugin).extension
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('asakusafwBase', AsakusafwBaseExtension)
        if (GradleVersion.current() < GradleVersion.version('2.0')) {
            project.logger.warn "Asakusa Framework Gradle plug-ins recommend using Gradle 2.0 or later"
            project.logger.warn "The current Gradle version (${GradleVersion.current()}) will not be supported in future releases"
        }
        configureProject()
    }

    private void configureProject() {
        configureBaseExtension()
        configureExtentionProperties()
        configureRepositories()
        configureTasks()
    }

    private void configureBaseExtension() {
        configureArtifactVersion()
        configureDefaults()
    }

    private void configureArtifactVersion() {
        Properties properties = loadProperties(ARTIFACT_INFO_PATH)
        extension.pluginVersion = extract(properties, 'plugin-version', 'Asakusa Gradle plug-ins')
        extension.frameworkVersion = extract(properties, 'framework-version', 'Asakusa SDK')
        project.logger.info "Asakusa Gradle plug-ins: ${extension.pluginVersion}"
    }

    private void configureDefaults() {
        Properties properties = loadProperties(DEFAULTS_INFO_PATH)
        extension.gradleVersion = extract(properties, 'gradle-version', 'recommended Gradle')
    }

    private String extract(Properties properties, String key, String name) {
        String value = properties.getProperty(key, INVALID_VERSION)
        if (value == INVALID_VERSION) {
            project.logger.warn "failed to detect version of ${name}"
        } else {
            project.logger.info "${name} version: ${value}"
        }
        return value
    }

    private Properties loadProperties(String path) {
        Properties results = new Properties()
        InputStream input = getClass().classLoader.getResourceAsStream(path)
        if (input == null) {
            project.logger.warn "missing properties file: ${path}"
        } else {
            try {
                results.load(input)
            } catch (IOException e) {
                project.logger.warn "error occurred while extracting properties: ${path}"
            } finally {
                input.close()
            }
        }
        return results
    }

    private void configureExtentionProperties() {
        project.extensions.create('asakusafwInternal', AsakusafwInternalPluginConvention)
    }

    private void configureRepositories() {
        project.repositories {
            maven { url "http://repo1.maven.org/maven2/" }
            maven { url "http://asakusafw.s3.amazonaws.com/maven/releases" }
            maven { url "http://asakusafw.s3.amazonaws.com/maven/snapshots" }
        }
    }

    private void configureTasks() {
        defineVersionsTask()
        defineUpgradeTask()
    }

    private void defineVersionsTask() {
        project.tasks.create(TASK_VERSIONS) { Task t ->
            t.group 'help'
            t.description 'Displays the versions about Asakusa Framework'
            t.doLast {
                t.logger.lifecycle "Asakusa Gradle Plug-ins: ${extension.pluginVersion}"
            }
        }
    }

    private void defineUpgradeTask() {
        project.tasks.create(TASK_UPGRADE) { Task t ->
            t.description 'Upgrades application development project'
        }
        project.tasks.create('upgradeGradleWrapper', Wrapper) { Wrapper t ->
            t.description 'Upgrades Gradle wrapper'
            project.tasks.getByName(TASK_UPGRADE).dependsOn t
            t.distributionUrl "http://services.gradle.org/distributions/gradle-${extension.gradleVersion}-bin.zip"
            t.jarFile project.file('.buildtools/gradlew.jar')
            t.doFirst {
                t.logger.lifecycle "installing Gradle wrapper: version=${extension.gradleVersion}"
            }
            t.onlyIf { extension.gradleVersion != INVALID_VERSION }
        }
    }

    /**
     * Returns the extension.
     * @return the extension
     */
    AsakusafwBaseExtension getExtension() {
        return extension
    }
}

