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
package com.asakusafw.mapreduce.gradle.plugins.internal

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.asakusafw.gradle.plugins.AsakusafwCompilerExtension
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention
import com.asakusafw.gradle.plugins.AsakusafwSdkExtension
import com.asakusafw.gradle.plugins.AsakusafwSdkPluginParticipant
import com.asakusafw.gradle.plugins.internal.AsakusaSdkPlugin
import com.asakusafw.gradle.plugins.internal.PluginUtils

/**
 * A base plug-in of {@link AsakusaMapReduceSdkPlugin}.
 * This only organizes conventions and dependencies.
 * @since 0.9.0
 */
class AsakusaMapReduceSdkBasePlugin implements Plugin<Project> {

    private static final Pattern OPTION_PATTERN = ~/([\+\-])\s*([0-9A-Za-z_\\-]+)|(X[0-9A-Za-z_\\-]+)=([^,]*)/

    private Project project

    private AsakusafwCompilerExtension extension

    @Override
    void apply(Project project) {
        this.project = project

        project.apply plugin: AsakusaSdkPlugin
        project.apply plugin: AsakusaMapReduceBasePlugin
        this.extension = AsakusaSdkPlugin.get(project).extensions.create('mapreduce', AsakusafwCompilerExtension)

        configureConvention()
        configureConfigurations()
    }

    private void configureConvention() {
        AsakusaMapReduceBaseExtension base = AsakusaMapReduceBasePlugin.get(project)
        AsakusafwPluginConvention sdk = AsakusaSdkPlugin.get(project)
        extension.conventionMapping.with {
            outputDirectory = { project.file(sdk.compiler.compiledSourceDirectory) }
            runtimeWorkingDirectory = { sdk.compiler.hadoopWorkDirectory }
            compilerProperties = { parseOptions(sdk.compiler.compilerOptions) }
            failOnError = { true }
        }
        PluginUtils.injectVersionProperty(extension, { base.featureVersion })
        sdk.sdk.availableTestkits << new AsakusaMapReduceTestkit()
        sdk.sdk.availableTestkits << new AsakusaSimpleMapReduceTestkit()
    }

    private void configureConfigurations() {
        project.configurations {
            asakusaMapreduceCommon {
                description 'Common libraries of Asakusa DSL Compiler for MapReduce'
                exclude group: 'asm', module: 'asm'
            }
            asakusaMapreduceCompiler {
                description 'Full classpath of Asakusa DSL Compiler for MapReduce'
                extendsFrom project.configurations.compile
                extendsFrom project.configurations.asakusaMapreduceCommon
            }
            asakusaMapreduceTestkit {
                description 'Asakusa DSL testkit classpath for MapReduce'
                extendsFrom project.configurations.asakusaMapreduceCommon
            }
        }
        PluginUtils.afterEvaluate(project) {
            AsakusaMapReduceBaseExtension base = AsakusaMapReduceBasePlugin.get(project)
            AsakusafwPluginConvention sdk = AsakusaSdkPlugin.get(project)
            AsakusafwSdkExtension features = sdk.sdk
            project.dependencies {
                if (features.core) {
                    asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-core:${sdk.asakusafwVersion}"
                    asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-extension-inspection:${sdk.asakusafwVersion}"
                    asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-extension-yaess:${sdk.asakusafwVersion}"
                    asakusaMapreduceCompiler "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-cli:${sdk.asakusafwVersion}"
                    if (features.directio) {
                        asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-extension-directio:${sdk.asakusafwVersion}"
                    }
                    if (features.windgate) {
                        asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-extension-windgate:${sdk.asakusafwVersion}"
                    }
                    if (features.hive) {
                        asakusaMapreduceCommon "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-extension-hive:${sdk.asakusafwVersion}"
                    }
                }
                if (features.testing) {
                    asakusaMapreduceTestkit "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-test-adapter:${sdk.asakusafwVersion}"
                }
            }
        }
    }

    private Map<String, Object> parseOptions(List<String> options) {
        Map<String, Object> results = [:]
        for (String s : options) {
            if (s == null || s.trim().isEmpty()) {
                continue
            }
            String option = s.trim()
            Matcher m = OPTION_PATTERN.matcher(option)
            if (m.matches()) {
                if (m.group(1) != null) {
                    String key = m.group(2)
                    boolean value = m.group(1) == '+'
                    results[key] = value
                } else if (m.group(3) != null) {
                    String key = m.group(3)
                    String value = m.group(4)
                    results[key] = value
                } else {
                    throw new AssertionError(option)
                }
            } else {
                project.logger.warn "unrecognizable compiler option: ${option}"
            }
        }
        return results
    }

    /**
     * Returns the extension object of this plug-in.
     * The plug-in will be applied automatically.
     * @param project the target project
     * @return the related extension
     */
    static AsakusafwCompilerExtension get(Project project) {
        project.apply plugin: AsakusaMapReduceSdkBasePlugin
        AsakusaMapReduceSdkBasePlugin plugin = project.plugins.getPlugin(AsakusaMapReduceSdkBasePlugin)
        if (plugin == null) {
            throw new IllegalStateException('AsakusaMapReduceSdkBasePlugin has not been applied')
        }
        return plugin.extension
    }

    /**
     * A participant descriptor for {@link AsakusaMapReduceSdkBasePlugin}.
     * @since 0.9.0
     */
    static class Participant implements AsakusafwSdkPluginParticipant {

        @Override
        String getName() {
            return descriptor.simpleName
        }

        @Override
        Class<?> getDescriptor() {
            return AsakusaMapReduceSdkBasePlugin
        }
    }
}
