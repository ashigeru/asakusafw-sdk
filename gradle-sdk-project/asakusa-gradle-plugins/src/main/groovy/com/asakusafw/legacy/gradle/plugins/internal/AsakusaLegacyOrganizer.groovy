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
package com.asakusafw.legacy.gradle.plugins.internal

import org.gradle.api.Project
import org.gradle.api.Task

import com.asakusafw.gradle.plugins.AsakusafwBaseExtension
import com.asakusafw.gradle.plugins.AsakusafwBasePlugin
import com.asakusafw.gradle.plugins.AsakusafwOrganizerProfile
import com.asakusafw.gradle.plugins.internal.AbstractOrganizer
import com.asakusafw.gradle.plugins.internal.PluginUtils

/**
 * Processes an {@link AsakusafwOrganizerProfile} for legacy facilities.
 * @since 0.8.0
 */
class AsakusaLegacyOrganizer extends AbstractOrganizer {

    /**
     * Creates a new instance.
     * @param project the current project
     * @param profile the target profile
     */
    AsakusaLegacyOrganizer(Project project, AsakusafwOrganizerProfile profile) {
        super(project, profile)
    }

    /**
     * Configures the target profile.
     */
    @Override
    void configureProfile() {
        createConfigurations()
        configureTasks()
    }

    private void createConfigurations() {
        createConfigurations('asakusafw', [
            DevelopmentDist : "Contents of Asakusa Framework development tools (${profile.name}).",
        ])
        PluginUtils.afterEvaluate(project) {
            String frameworkVersion = profile.asakusafwVersion
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            createDependencies('asakusafw', [
                DevelopmentDist : "com.asakusafw:asakusa-development-tools:${frameworkVersion}:dist@jar",
            ])
        }
    }

    private void configureTasks() {
        createAttachComponentTasks 'attachComponent', [
            Development : {
                into('.') {
                    extract configuration('asakusafwDevelopmentDist')
                }
            },
        ]
    }
}
