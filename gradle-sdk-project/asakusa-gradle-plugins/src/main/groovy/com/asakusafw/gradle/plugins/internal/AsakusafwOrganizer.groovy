/*
 * Copyright 2011-2014 Asakusa Framework Team.
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
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.util.ConfigureUtil

import com.asakusafw.gradle.plugins.AsakusafwOrganizerProfile
import com.asakusafw.gradle.plugins.internal.AsakusafwInternalPluginConvention.DependencyConfiguration
import com.asakusafw.gradle.tasks.GatherAssemblyTask

/**
 * Processes an {@link AsakusafwOrganizerProfile}.
 * @since 0.7.0
 */
class AsakusafwOrganizer {

    /**
     * The current project.
     */
    final Project project

    /**
     * The target profile.
     */
    final AsakusafwOrganizerProfile profile

    /**
     * Creates a new instance.
     * @param project the current project
     * @param profile the target profile
     */
    AsakusafwOrganizer(Project project, AsakusafwOrganizerProfile profile) {
        this.project = project
        this.profile = profile
    }

    /**
     * Returns the profile name.
     * @return the profile name
     */
    String getName() {
        return profile.name
    }

    /**
     * Configures the target profile.
     */
    void configureProfile() {
        configureConfigurations()
        configureDependencies()
        configureTasks()
        enableTasks()
    }

    private void configureConfigurations() {
        createConfigurations('asakusafw', [
                         CoreDist : "Contents of Asakusa Framework core modules (${profile.name}).",
                          CoreLib : "Libraries of Asakusa Framework core modules (${profile.name}).",
                     DirectIoDist : "Contents of Asakusa Framework Direct I/O modules (${profile.name}).",
                      DirectIoLib : "Libraries of Asakusa Framework Direct I/O modules (${profile.name}).",
                 DirectIoHiveDist : "Contents of Direct I/O Hive modules (${profile.name}).",
                  DirectIoHiveLib : "Libraries of Direct I/O Hive modules (${profile.name}).",
                        YaessDist : "Contents of Asakusa Framework YAESS modules (${profile.name}).",
                         YaessLib : "Libraries of Asakusa Framework YAESS modules (${profile.name}).",
                      YaessPlugin : "Default plugin library sets of YAESS (${profile.name}).",
                  YaessHadoopDist : "Contents of Asakusa Framework YAESS Hadoop bridge (${profile.name}).",
                   YaessHadoopLib : "Libraries of Asakusa Framework YAESS Hadoop bridge (${profile.name}).",
                   YaessToolsDist : "Contents of Asakusa Framework YAESS tools (${profile.name}).",
                    YaessToolsLib : "Libraries of Asakusa Framework YAESS tools (${profile.name}).",
                YaessJobQueueDist : "Contents of YAESS JobQueue client modules (${profile.name}).",
                 YaessJobQueueLib : "Libraries of YAESS JobQueue client modules (${profile.name}).",
                     WindGateDist : "Contents of Asakusa Framework WindGate tools (${profile.name}).",
                      WindGateLib : "Libraries of Asakusa Framework WindGate modules (${profile.name}).",
                   WindGatePlugin : "Default plugin library sets of WindGate (${profile.name}).",
                  WindGateSshDist : "Contents of Asakusa Framework WindGate-SSH modules (${profile.name}).",
                   WindGateSshLib : "Libraries of Asakusa Framework WindGate-SSH modules (${profile.name}).",
            WindGateRetryableDist : "Contents of WindGate retryable modules (${profile.name}).",
             WindGateRetryableLib : "Libraries of WindGate retryable modules (${profile.name}).",
                      TestingDist : "Contents of Asakusa Framework testing tools (${profile.name}).",
                  DevelopmentDist : "Contents of Asakusa Framework development tools (${profile.name}).",
                  ThunderGateDist : "Contents of Asakusa Framework ThunderGate modules (${profile.name}).",
               ThunderGateCoreLib : "Libraries of Asakusa Framework ThunderGate common modules (${profile.name}).",
                   ThunderGateLib : "Libraries of Asakusa Framework ThunderGate modules (${profile.name}).",
                    OperationDist : "Contents of Asakusa Framework operation tools (${profile.name}).",
                     OperationLib : "Libraries of Asakusa Framework operation tools (${profile.name}).",
        ])
        configuration('asakusafwThunderGateCoreLib').transitive = false
    }

    private void createConfigurations(String prefix, Map<String, String> confMap) {
        confMap.each { String name, String desc ->
            createConfiguration(prefix + name) {
                description desc
                visible false
            }
        }
    }

    private void configureDependencies() {
        project.afterEvaluate {
            String frameworkVersion = profile.asakusafwVersion
            DependencyConfiguration deps = project.asakusafwInternal.dep
            createDependencies('asakusafw', [
                CoreDist : "com.asakusafw:asakusa-runtime-configuration:${frameworkVersion}:dist@jar",
                CoreLib : [
                    "com.asakusafw:asakusa-runtime-all:${frameworkVersion}:lib@jar"
                ],
                DirectIoDist : "com.asakusafw:asakusa-directio-tools:${frameworkVersion}:dist@jar",
                DirectIoLib : [
                    "com.asakusafw:asakusa-directio-tools:${frameworkVersion}@jar"
                ],
                YaessDist : "com.asakusafw:asakusa-yaess-bootstrap:${frameworkVersion}:dist@jar",
                YaessLib : [
                    "com.asakusafw:asakusa-yaess-bootstrap:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-core:${frameworkVersion}@jar",
                    "commons-cli:commons-cli:${deps.commonsCliVersion}@jar",
                    "ch.qos.logback:logback-classic:${deps.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${deps.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${deps.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${deps.slf4jVersion}@jar",
                ],
                YaessPlugin : [
                    "com.asakusafw:asakusa-yaess-flowlog:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-jsch:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-multidispatch:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-yaess-paralleljob:${frameworkVersion}@jar",
                    "com.jcraft:jsch:${deps.jschVersion}@jar",
                ],
                YaessHadoopDist : "com.asakusafw:asakusa-yaess-core:${frameworkVersion}:dist@jar",
                YaessHadoopLib : [],
                YaessToolsDist : "com.asakusafw:asakusa-yaess-tools:${frameworkVersion}:dist@jar",
                YaessToolsLib : [
                    "com.asakusafw:asakusa-yaess-tools:${frameworkVersion}@jar",
                    "com.google.code.gson:gson:${deps.gsonVersion}@jar",
                ],
                YaessJobQueueLib : [
                    "com.asakusafw:asakusa-yaess-jobqueue:${frameworkVersion}@jar",
                    "org.apache.httpcomponents:httpcore:${deps.httpClientVersion}@jar",
                    "org.apache.httpcomponents:httpclient:${deps.httpClientVersion}@jar",
                    "com.google.code.gson:gson:${deps.gsonVersion}@jar",
                    "commons-codec:commons-codec:${deps.commonsCodecVersion}@jar",
                    "commons-logging:commons-logging:${deps.commonsLoggingVersion}@jar",
                ],
                WindGateDist : "com.asakusafw:asakusa-windgate-plugin:${frameworkVersion}:dist@jar",
                WindGateLib : [
                    "com.asakusafw:asakusa-windgate-bootstrap:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-core:${frameworkVersion}@jar",
                    "ch.qos.logback:logback-classic:${deps.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${deps.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${deps.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${deps.slf4jVersion}@jar",
                    "com.jcraft:jsch:${deps.jschVersion}@jar",
                ],
                WindGatePlugin : [
                    "com.asakusafw:asakusa-windgate-hadoopfs:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-jdbc:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-stream:${frameworkVersion}@jar",
                ],
                WindGateSshDist : "com.asakusafw:asakusa-windgate-hadoopfs:${frameworkVersion}:dist@jar",
                WindGateSshLib : [
                    "com.asakusafw:asakusa-windgate-core:${frameworkVersion}@jar",
                    "com.asakusafw:asakusa-windgate-hadoopfs:${frameworkVersion}@jar",
                    "ch.qos.logback:logback-classic:${deps.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${deps.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${deps.slf4jVersion}@jar",
                    "org.slf4j:jul-to-slf4j:${deps.slf4jVersion}@jar",
                ],
                WindGateRetryableDist : [],
                WindGateRetryableLib : [
                    "com.asakusafw:asakusa-windgate-retryable:${frameworkVersion}@jar",
                ],
                ThunderGateDist : "com.asakusafw:asakusa-thundergate:${frameworkVersion}:dist@jar",
                ThunderGateLib : [
                    "com.asakusafw:asakusa-thundergate:${frameworkVersion}@jar",
                    "commons-configuration:commons-configuration:${deps.commonsConfigurationVersion}@jar",
                    "commons-io:commons-io:${deps.commonsIoVersion}@jar",
                    "commons-lang:commons-lang:${deps.commonsLangVersion}@jar",
                    "commons-logging:commons-logging:${deps.commonsLoggingVersion}@jar",
                    "log4j:log4j:${deps.log4jVersion}@jar",
                    "mysql:mysql-connector-java:${deps.mysqlConnectorJavaVersion}@jar",
                ],
                ThunderGateCoreLib : [
                    "com.asakusafw:asakusa-thundergate-runtime:${frameworkVersion}@jar"
                ],
                TestingDist : "com.asakusafw:asakusa-test-driver:${frameworkVersion}:dist@jar",
                DevelopmentDist : "com.asakusafw:asakusa-development-tools:${frameworkVersion}:dist@jar",
                OperationDist : "com.asakusafw:asakusa-operation-tools:${frameworkVersion}:dist@jar",
                OperationLib : [
                    "com.asakusafw:asakusa-operation-tools:${frameworkVersion}@jar",
                    "commons-cli:commons-cli:${deps.commonsCliVersion}@jar",
                    "ch.qos.logback:logback-classic:${deps.logbackVersion}@jar",
                    "ch.qos.logback:logback-core:${deps.logbackVersion}@jar",
                    "org.slf4j:slf4j-api:${deps.slf4jVersion}@jar",
                ],
                DirectIoHiveDist : [],
                DirectIoHiveLib : ["com.asakusafw:asakusa-hive-core:${frameworkVersion}@jar"] + profile.hive.libraries
            ])
        }
    }

    private void createDependencies(String prefix, Map<String, Object> depsMap) {
        depsMap.each { String name, Object value ->
            if (value instanceof Collection<?> || value instanceof Object[]) {
                value.each { Object notation ->
                    createDependency prefix + name, notation
                }
            } else if (value != null) {
                createDependency prefix + name, value
            }
        }
    }

    private void configureTasks() {
        createAttachComponentTasks 'attach', [
            Batchapps : {
                into('batchapps') {
                    put project.asakusafw.compiler.compiledSourceDirectory
                }
            },
        ]
        createAttachComponentTasks 'attachComponent', [
            Core : {
                into('.') {
                    extract configuration('asakusafwCoreDist')
                }
                into('core/lib') {
                    put configuration('asakusafwCoreLib')
                    process {
                        rename(/asakusa-runtime-all(.*).jar/, 'asakusa-runtime-all.jar')
                    }
                }
            },
            DirectIo : {
                into('.') {
                    extract configuration('asakusafwDirectIoDist')
                }
                into('directio/lib') {
                    put configuration('asakusafwDirectIoLib')
                }
            },
            Yaess : {
                into('.') {
                    extract configuration('asakusafwYaessDist')
                }
                into('yaess/lib') {
                    put configuration('asakusafwYaessLib')
                }
                into('yaess/plugin') {
                    put configuration('asakusafwYaessPlugin')
                }
            },
            YaessHadoop : {
                into('.') {
                    extract configuration('asakusafwYaessHadoopDist')
                }
                into('yaess-hadoop/lib') {
                    put configuration('asakusafwYaessHadoopLib')
                }
            },
            WindGate : {
                into('.') {
                    extract configuration('asakusafwWindGateDist')
                }
                into('windgate/lib') {
                    put configuration('asakusafwWindGateLib')
                }
                into('windgate/plugin') {
                    put configuration('asakusafwWindGatePlugin')
                }
            },
            WindGateSsh : {
                into('.') {
                    extract configuration('asakusafwWindGateSshDist')
                }
                into('windgate-ssh/lib') {
                    put configuration('asakusafwWindGateSshLib')
                }
            },
            ThunderGate : {
                into('.') {
                    extract configuration('asakusafwThunderGateDist')
                    String targetName = profile.thundergate.target
                    if (targetName) {
                        process {
                            rename(/\[\w+\]-jdbc\.properties/, "${targetName}-jdbc.properties")
                        }
                    }
                }
                into('bulkloader/lib') {
                    put configuration('asakusafwThunderGateLib')
                }
                into('core/lib') {
                    put configuration('asakusafwThunderGateCoreLib')
                }
            },
            Development : {
                into('.') {
                    extract configuration('asakusafwDevelopmentDist')
                }
            },
            Testing : {
                into('.') {
                    extract configuration('asakusafwTestingDist')
                }
            },
            Operation : {
                into('.') {
                    extract configuration('asakusafwOperationDist')
                }
                into('tools/lib') {
                    put configuration('asakusafwOperationLib')
                }
            },
        ]
        createAttachComponentTasks 'attachExtension', [
            DirectIoHive : {
                into('.') {
                    put configuration('asakusafwDirectIoHiveDist')
                }
                into('ext/lib') {
                    put configuration('asakusafwDirectIoHiveLib')
                }
            },
            YaessTools : {
                into('.') {
                    extract configuration('asakusafwYaessToolsDist')
                }
                into('yaess/lib') {
                    put configuration('asakusafwYaessToolsLib')
                }
            },
            YaessJobQueue : {
                into('.') {
                    extract configuration('asakusafwYaessJobQueueDist')
                }
                into('yaess/plugin') {
                    put configuration('asakusafwYaessJobQueueLib')
                }
            },
            WindGateRetryable : {
                into('.') {
                    put configuration('asakusafwWindGateRetryableDist')
                }
                into('windgate/plugin') {
                    put configuration('asakusafwWindGateRetryableLib')
                }
            },
        ]
        createTask('cleanAssembleAsakusafw') {
            doLast {
                project.delete task('gatherAsakusafw').destination
                project.delete task('assembleAsakusafw').archivePath
            }
        }
        createTask('attachAssemble')
        createTask('gatherAsakusafw', GatherAssemblyTask) { Task t ->
            dependsOn task('attachAssemble')
            shouldRunAfter task('cleanAssembleAsakusafw')
            assemblies << profile.components
            assemblies << project.asakusafwOrganizer.assembly
            assemblies << profile.assembly
            conventionMapping.with {
                destination = { project.file(profile.assembleDir) }
            }
            // Gather task must be run after 'attach*' were finished
            project.tasks.matching { it.name.startsWith('attach') && isProfileTask(it) }.all { Task target ->
                t.mustRunAfter target
            }
        }

        project.afterEvaluate {
            createTask('assembleAsakusafw', Tar) {
                dependsOn task('gatherAsakusafw')
                from task('gatherAsakusafw').destination
                destinationDir project.buildDir
                compression Compression.GZIP
                archiveName profile.archiveName
            }
        }
    }

    private void createAttachComponentTasks(String prefix, Map<String, Closure<?>> actionMap) {
        actionMap.each { String name, Closure<?> action ->
            createTask(prefix + name) {
                doLast {
                    ConfigureUtil.configure action, profile.components
                }
            }
        }
    }

    private boolean isProfileTask(Task task) {
        if (task.hasProperty('profileName')) {
            return task.profileName == profile.name
        }
        return false
    }

    private void enableTasks() {
        project.afterEvaluate {
            // default enabled
            task('attachAssemble').dependsOn task('attachComponentCore')
            task('attachAssemble').dependsOn task('attachComponentOperation')

            if (profile.directio.isEnabled()) {
                project.logger.info 'Enabling Direct I/O'
                task('attachAssemble').dependsOn task('attachComponentDirectIo')
            }
            if (profile.thundergate.isEnabled()) {
                project.logger.info 'Enabling ThunderGate'
                task('attachAssemble').dependsOn task('attachComponentThunderGate')
            }
            if (profile.windgate.isEnabled()) {
                project.logger.info 'Enabling WindGate'
                task('attachAssemble').dependsOn task('attachComponentWindGate')
            }
            if (profile.windgate.isSshEnabled()) {
                project.logger.info 'Enabling WindGate SSH'
                task('attachAssemble').dependsOn task('attachComponentWindGateSsh')
            }
            if (profile.windgate.isRetryableEnabled()) {
                project.logger.info 'Enabling WindGate Retryable'
                task('attachComponentWindGate').dependsOn task('attachExtensionWindGateRetryable')
            }
            if (profile.hive.isEnabled()) {
                project.logger.info 'Enabling Direct I/O Hive'
                task('attachAssemble').dependsOn task('attachExtensionDirectIoHive')
            }
            if (profile.yaess.isEnabled()) {
                project.logger.info 'Enabling YAESS'
                task('attachAssemble').dependsOn task('attachComponentYaess')
            }
            if (profile.yaess.isHadoopEnabled()) {
                project.logger.info 'Enabling YAESS Hadoop'
                task('attachAssemble').dependsOn task('attachComponentYaessHadoop')
            }
            if (profile.yaess.isToolsEnabled()) {
                project.logger.info 'Enabling YAESS tools'
                task('attachComponentYaess').dependsOn task('attachExtensionYaessTools')
            }
            if (profile.yaess.isJobqueueEnabled()) {
                project.logger.info 'Enabling YAESS JobQueue'
                task('attachComponentYaess').dependsOn task('attachExtensionYaessJobQueue')
            }
            if (profile.batchapps.isEnabled() && project.plugins.hasPlugin('asakusafw')) {
                project.logger.info 'Enabling Batchapps'
                task('attachBatchapps').shouldRunAfter project.tasks.compileBatchapp
                task('attachAssemble').dependsOn task('attachBatchapps')
            }
            if (profile.testing.isEnabled()) {
                project.logger.info 'Enabling Testing'
                task('attachAssemble').dependsOn task('attachComponentTesting')
            }
        }
    }

    private String qualify(String name) {
        return "${name}_${profile.name}"
    }

    private Configuration createConfiguration(String name, Closure<?> configurator) {
        return project.configurations.create(qualify(name), configurator)
    }

    private Dependency createDependency(String configurationName, Object notation) {
        return project.dependencies.add(qualify(configurationName), notation)
    }

    private Task createTask(String name, Class<? extends Task> parent, Closure<?> configurator) {
        project.tasks.create(qualify(name), parent, configurator)
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    private Task createTask(String name) {
        project.tasks.create(qualify(name))
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    private Task createTask(String name, Closure<?> configurator) {
        project.tasks.create(qualify(name), configurator)
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    /**
     * Returns the task name for the profile.
     * @param name the bare task name
     * @return the corresponded profile task name
     */
    String taskName(String name) {
        return qualify(name)
    }

    /**
     * Returns the task for the profile.
     * @param name the bare task name
     * @return the corresponded profile task
     */
    Task task(String name) {
        return project.tasks[qualify(name)]
    }

    /**
     * Returns the configuration for the profile.
     * @param name the bare task name
     * @return the corresponded profile configuration
     */
    Configuration configuration(String name) {
        return project.configurations[qualify(name)]
    }
}
