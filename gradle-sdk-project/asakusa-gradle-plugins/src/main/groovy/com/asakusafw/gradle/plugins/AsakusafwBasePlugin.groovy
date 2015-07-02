/*
 * Copyright 2011-2015 Asakusa Framework Team.
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
 */
class AsakusafwBasePlugin implements Plugin<Project> {

    private Project project

    void apply(Project project) {
        this.project = project
        if (GradleVersion.current() < GradleVersion.version('2.0')) {
            project.logger.warn "Asakusa Framework Gradle plug-ins recommend using Gradle 2.0 or later"
            project.logger.warn "The current Gradle version (${GradleVersion.current()}) will not be supported in future releases"
        }
        configureProject()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureRepositories()
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
}

