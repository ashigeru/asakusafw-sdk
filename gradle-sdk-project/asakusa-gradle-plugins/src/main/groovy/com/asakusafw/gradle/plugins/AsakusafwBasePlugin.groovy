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
import org.gradle.util.GradleVersion

import com.asakusafw.gradle.plugins.internal.AsakusafwInternalPluginConvention

/**
 * Base class of Asakusa Framework Gradle Plugin.
 * @since 0.5.3
 * @version 0.8.0
 */
class AsakusafwBasePlugin implements Plugin<Project> {

    private static final String ARTIFACT_INFO_PATH = 'META-INF/asakusa-gradle/artifact.properties'

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
    }

    private void configureBaseExtension() {
        configureArtifactVersion()
    }

    private void configureArtifactVersion() {
        InputStream input = getClass().classLoader.getResourceAsStream(ARTIFACT_INFO_PATH)
        if (input != null) {
            try {
                Properties properties = new Properties()
                properties.load(input)
                extension.frameworkVersion = properties.getProperty('framework-version', INVALID_VERSION)
            } catch (IOException e) {
                project.logger.warn "error occurred while extracting artifact version: ${ARTIFACT_INFO_PATH}"
            } finally {
                input.close()
            }
        }
        if (extension.frameworkVersion == null) {
            project.logger.warn "failed to detect version of Asakusa Framework: ${ARTIFACT_INFO_PATH}"
            extension.frameworkVersion = null
        }
        project.logger.info "Asakusa Framework: ${extension.frameworkVersion}"
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

    /**
     * Returns the extension.
     * @return the extension
     */
    AsakusafwBaseExtension getExtension() {
        return extension
    }
}

