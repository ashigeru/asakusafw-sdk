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
package com.asakusafw.gradle.tasks

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Nullable
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import com.asakusafw.gradle.tasks.internal.AbstractToolLauncherTask
import com.asakusafw.gradle.tasks.internal.ResolutionUtils
import com.asakusafw.gradle.tasks.internal.ToolLauncherUtils

/**
 * Gradle Task for generating Hive DDL files.
 * @since 0.7.0
 * @version 0.8.1
 */
class GenerateHiveDdlTask extends AbstractToolLauncherTask {

    /**
     * The logback configuration for the tool.
     */
    @Nullable
    File logbackConf

    /**
     * The max heap size for the tool.
     */
    @Nullable
    String maxHeapSize

    /**
     * The Java system properties.
     */
    Map<Object, Object> systemProperties = [:]

    /**
     * The Java VM arguments.
     */
    List<Object> jvmArgs = []

    /**
     * The tool class path.
     */
    @InputFiles
    FileCollection toolClasspath = project.files()

    /**
     * The source path (MUST BE application classes directories).
     */
    FileCollection sourcepath = project.files()

    /**
     * The plug-in libraries.
     */
    @InputFiles
    FileCollection pluginClasspath = project.files()

    /**
     * The output file path.
     */
    @Optional
    @OutputFile
    File outputFile

    /**
     * The target table name pattern in regex, or {@code null} to accept any tables.
     */
    @Optional
    @Input
    String include

    /**
     * The target table location on Hadoop file system, or {@code null} to use default location.
     */
    @Optional
    @Input
    String location

    /**
     * The target database name, or {@code null} to use default database.
     */
    @Optional
    @Input
    String databaseName

    /**
     * Sets the output file path.
     * @param path the output file path
     */
    @Option(option = 'output', description = 'output file')
    void setOptOutput(String path) {
        setOutputFile(project.file(path))
    }

    /**
     * Sets the generation target table name pattern.
     * @param pattern target table name pattern (in regex)
     */
    @Option(option = 'include', description = 'table name pattern in regex')
    void setOptInclude(String pattern) {
        setInclude(pattern)
    }

    /**
     * Sets the base table space path.
     * @param path the base table space path
     */
    @Option(option = 'location', description = 'base table-space path')
    void setOptLocation(String path) {
        setLocation(path)
    }

    /**
     * Sets the database name for each table.
     * @param name database name
     */
    @Option(option = 'database', description = 'database name')
    void setOptDatabaseName(String name) {
        setDatabaseName(name)
    }

    /**
     * Returns the source files.
     * @return the source files
     */
    @SkipWhenEmpty
    @Optional
    @InputFiles
    FileCollection getSourceFiles() {
        def results = project.files()
        getSourcepath().each { File file ->
            if (file.isFile()) {
                results += project.files(file)
            } else if (file.isDirectory()) {
                results += project.fileTree(file)
            }
        }
        return results
    }

    /**
     * Performs this task.
     */
    @TaskAction
    void perform() {
        if (getOutputFile() == null) {
            throw new InvalidUserDataException("${this.name} requires --output <output-file>")
        }
        String javaMain = 'com.asakusafw.directio.hive.tools.cli.GenerateCreateTable'
        FileCollection javaClasspath = project.files(getToolClasspath())
        List<String> javaArguments = createArguments()
        FileCollection launcher = project.files(getLauncherClasspath())
        if (!launcher.empty) {
            logger.info "Starting Hive DDL generator using launcher"
            File script = ToolLauncherUtils.createLaunchFile(this, javaClasspath, javaMain, javaArguments)
            javaMain = ToolLauncherUtils.MAIN_CLASS
            javaClasspath = launcher
            javaArguments = [script.absolutePath]
        }
        project.javaexec { JavaExecSpec spec ->
            spec.main = javaMain
            spec.classpath = javaClasspath
            spec.jvmArgs = ResolutionUtils.resolveToStringList(this.getJvmArgs())
            if (this.getMaxHeapSize() != null) {
                spec.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                spec.systemProperties 'logback.configurationFile' : this.getLogbackConf().absolutePath
            }
            spec.systemProperties ResolutionUtils.resolveToStringMap(this.getSystemProperties())
            spec.args = javaArguments
        }
    }

    private List<String> createArguments() {
        List<String> results = []
        results += ['--classpath', getSourcepath().asPath]
        results += ['--output', getOutputFile().absolutePath]
        if (this.getInclude() != null) {
            results += ['--include', getInclude()]
        }
        if (this.getLocation() != null) {
            results += ['--location', getLocation()]
        }
        if (this.getDatabaseName() != null) {
            results += ['--database', getDatabaseName()]
        }
        if (getPluginClasspath().isEmpty() == false) {
            results += ['--pluginpath', getPluginClasspath().asPath]
        }
        return results
    }
}
