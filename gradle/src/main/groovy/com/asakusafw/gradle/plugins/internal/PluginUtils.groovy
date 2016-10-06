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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.Project
import org.gradle.api.ProjectState
import org.gradle.api.Task
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.util.GradleVersion

import com.asakusafw.gradle.plugins.PluginParticipant
import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Basic utilities for Gradle plug-ins.
 * @since 0.7.4
 * @version 0.9.0
 */
final class PluginUtils {

    /**
     * The property name of module versions.
     * @since 0.9.0
     */
    public static final String PROPERTY_VERSION = 'version'

    /**
     * Executes a closure after the project was evaluated only if evaluation was not failed.
     * @param project the target project
     * @param closure the closure
     */
    static void afterEvaluate(Project project, Closure<?> closure) {
        project.afterEvaluate { Project p, ProjectState state ->
            if (state.failure != null) {
                return
            }
            closure.call(project)
        }
    }

    /**
     * Compares the target Gradle version with the current Gradle version.
     * @param version the target Gradle version
     * @return {@code =0} - same, {@code >0} - the current version is newer, {@code <0} - the current version is older
     */
    static int compareGradleVersion(String version) {
        GradleVersion current = GradleVersion.current()
        GradleVersion target = GradleVersion.version(version)
        return current.compareTo(target)
    }

    /**
     * Calls a closure after the target plug-in is enabled.
     * @param project the current project
     * @param pluginType the target plug-in class
     * @param closure the closure
     */
    static void afterPluginEnabled(Project project, Class<?> pluginType, Closure<?> closure) {
        project.plugins.withType(pluginType) {
            closure.call()
        }
    }

    /**
     * Calls a closure after the target plug-in is enabled.
     * @param project the current project
     * @param pluginId the target plug-in ID
     * @param closure the closure
     */
    static void afterPluginEnabled(Project project, String pluginId, Closure<?> closure) {
        if (compareGradleVersion('2.0') >= 0) {
            project.plugins.withId(pluginId) {
                closure.call()
            }
        } else {
            project.plugins.matching({ it == project.plugins.findPlugin(pluginId) }).all {
                closure.call()
            }
        }
    }

    /**
     * Calls a closure after the target task is enabled.
     * @param project the current project
     * @param taskName the target task name
     * @param closure the closure
     * @since 0.8.0
     */
    static void afterTaskEnabled(Project project, String taskName, Closure<?> closure) {
        project.tasks.matching { Task t ->
            t.project == project && t.name == taskName
        }.all { Task t ->
            closure.call(t)
        }
    }

    /**
     * Make modifying <code>asakusafwVersion</code> deprecated.
     * @param project the current project
     * @param prefix the instance prefix name
     * @param instance the target instance
     * @return the original instance
     * @since 0.8.1
     */
    static <T> T deprecateAsakusafwVersion(Project project, String prefix, T instance) {
        // must declare getter explicitly for older Gradle versions (e.g. 1.12)
        def getter = instance.&getAsakusafwVersion
        instance.metaClass.getAsakusafwVersion = { ->
            return getter()
        }
        instance.metaClass.setAsakusafwVersion = { String arg ->
            project.logger.warn "DEPRECATED: changing ${prefix}.asakusafwVersion is ignored."
        }
        // asakusafwVersion(String) does not via Groovy MOP when calls setAsakusafwVersion()
        instance.metaClass.asakusafwVersion = { String arg ->
            project.logger.warn "DEPRECATED: changing ${prefix}.asakusafwVersion is ignored."
        }
        return instance
    }

    /**
     * Finds for services.
     * @param <T> the service interface type
     * @param project the current project
     * @param serviceInterface the service interface
     * @param loader the service class loader
     * @return the loaded services
     * @since 0.9.0
     */
    static void applyParticipants(Project project, Class<? extends PluginParticipant> type) {
        // We always load implementations from the interface class loader.
        // A class loader relying on the project might not load the target interface.
        ClassLoader loader = type.classLoader
        ServiceLoader<? extends PluginParticipant> services = ServiceLoader.load(type)
        for (Iterator<? extends PluginParticipant> iter = services.iterator(); iter.hasNext();) {
            try {
                PluginParticipant participant = iter.next()
                project.logger.info "applying participant: ${participant.name} (${participant.descriptor})"
                project.apply plugin: participant.descriptor
            } catch (ServiceConfigurationError e) {
                project.logger.warn "error occurred while loading service: ${type.name}", e
            }
        }
    }

    /**
     * Injects the {@code version} property into the given container.
     * @param container the target container
     * @param version the version value
     * @since 0.9.0
     */
    static void injectVersionProperty(ExtensionAware container, Object version) {
        if (!(container instanceof ExtensionAware)) {
            throw new IllegalStateException()
        }
        ExtensionContainer extensions = container.extensions
        FeatureVersionExtension value = new FeatureVersionExtension(version)
        if (extensions instanceof Convention) {
            extensions.plugins['asakusafw-version'] = value
        } else {
            extensions.add(PROPERTY_VERSION, value.version)
        }
    }

    private PluginUtils() {
    }
}
