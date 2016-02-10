/**
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
package com.asakusafw.gradle.plugins.internal

import javax.inject.Inject

import org.gradle.api.InvalidUserDataException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.RelativePath
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc

import com.asakusafw.gradle.plugins.AsakusafwBaseExtension
import com.asakusafw.gradle.plugins.AsakusafwBasePlugin
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention
import com.asakusafw.gradle.plugins.EclipsePluginEnhancement
import com.asakusafw.gradle.plugins.IdeaPluginEnhancement
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.tasks.AnalyzeYaessLogTask
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateHiveDdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask
import com.asakusafw.gradle.tasks.GenerateThunderGateDataModelTask
import com.asakusafw.gradle.tasks.RunBatchappTask
import com.asakusafw.gradle.tasks.TestToolTask
import com.asakusafw.gradle.tasks.internal.AbstractTestToolTask

/**
 * A Gradle plug-in for application development project using Asakusa SDK.
 * @since 0.8.0
 */
class AsakusaSdkPlugin implements Plugin<Project> {

    /**
     * The plug-in ID.
     */
    public static final String ID = 'asakusa-sdk'

    /**
     * The build group name.
     */
    public static final String ASAKUSAFW_BUILD_GROUP = 'Asakusa Framework Build'

    private final FileResolver fileResolver

    private Project project

    private AsakusafwPluginConvention extension

    private File frameworkHome

    /**
     * Applies this plug-in and returns the extension object for the project.
     * @param project the target project
     * @return the corresponded extension
     */
    static AsakusafwPluginConvention get(Project project) {
        project.apply plugin: AsakusaSdkPlugin
        return project.plugins.getPlugin(AsakusaSdkPlugin).extension
    }

    @Inject
    AsakusaSdkPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('asakusafw', AsakusafwPluginConvention)
        this.frameworkHome = System.env['ASAKUSA_HOME'] == null ? null : new File(System.env['ASAKUSA_HOME'])

        project.apply plugin: JavaPlugin
        project.apply plugin: AsakusafwBasePlugin

        configureProject()
        configureJavaPlugin()
        defineAsakusaTasks()
        applySubPlugins()
    }

    void setFrameworkInstallationPath(File path) {
        this.frameworkHome = path
    }

    private void configureProject() {
        configureExtentionProperties()
        configureConfigurations()
        configureDependencies()
        configureSourceSets()
    }

    private void configureExtentionProperties() {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        extension.dmdl = extension.extensions.create('dmdl', DmdlConfiguration)
        extension.modelgen = extension.extensions.create('modelgen', ModelgenConfiguration)
        extension.javac = extension.extensions.create('javac', JavacConfiguration)
        extension.compiler = extension.extensions.create('compiler', CompilerConfiguration)
        extension.testtools = extension.extensions.create('testtools', TestToolsConfiguration)
        extension.thundergate = extension.extensions.create('thundergate', ThunderGateConfiguration)
        extension.conventionMapping.with {
            asakusafwVersion = {
                if (base.frameworkVersion == null) {
                    throw new InvalidUserDataException('"asakusafw.asakusafwVersion" must be set')
                }
                return base.frameworkVersion
            }
            maxHeapSize = { '1024m' }
            logbackConf = { (String) "src/${project.sourceSets.test.name}/resources/logback-test.xml" }
            basePackage = {
                if (project.group == null || project.group == '') {
                    throw new InvalidUserDataException('"asakusafw.basePackage" must be specified')
                }
                return project.group
            }
        }
        extension.dmdl.conventionMapping.with {
            dmdlEncoding = { 'UTF-8' }
            dmdlSourceDirectory = { (String) "src/${project.sourceSets.main.name}/dmdl" }
        }
        extension.modelgen.conventionMapping.with {
            modelgenSourcePackage = { (String) "${extension.basePackage}.modelgen" }
            modelgenSourceDirectory = { (String) "${project.buildDir}/generated-sources/modelgen" }
        }
        extension.javac.conventionMapping.with {
            annotationSourceDirectory = { (String) "${project.buildDir}/generated-sources/annotations" }
            sourceEncoding = { 'UTF-8' }
            sourceCompatibility = { JavaVersion.toVersion(base.javaVersion) }
            targetCompatibility = { JavaVersion.toVersion(base.javaVersion) }
        }
        extension.compiler.conventionMapping.with {
            enabled = { true }
            compiledSourcePackage = { (String) "${extension.basePackage}.batchapp" }
            compiledSourceDirectory = { (String) "${project.buildDir}/batchc" }
            compilerOptions = {[
                String.format("XjavaVersion=%s", JavaVersion.toVersion(extension.javac.targetCompatibility))
            ]}
            compilerWorkDirectory = { null }
            hadoopWorkDirectory = { 'target/hadoopwork/${execution_id}' }
        }
        extension.testtools.conventionMapping.with {
            testDataSheetFormat = { 'ALL' }
            testDataSheetDirectory = { (String) "${project.buildDir}/excel" }
        }
        extension.thundergate.conventionMapping.with {
            target = { null }
            jdbcFile = { null }
            ddlEncoding = { null }
            ddlSourceDirectory = { (String) "src/${project.sourceSets.main.name}/sql/modelgen" }
            includes = { null }
            excludes = { null }
            dmdlOutputDirectory = { (String) "${project.buildDir}/thundergate/dmdl" }
            ddlOutputDirectory = { (String) "${project.buildDir}/thundergate/sql" }
            sidColumn = { 'SID' }
            timestampColumn = { 'UPDT_DATETIME' }
            deleteColumn = { 'DELETE_FLAG' }
            deleteValue = { '"1"' }
        }
        extension.metaClass.toStringDelegate = { -> "asakusafw { ... }" }
    }

    private void configureConfigurations() {
        def provided = project.configurations.create('provided')
        provided.description = '''Emulating Maven's provided scope.'''

        def embedded = project.configurations.create('embedded')
        embedded.description = 'Project embedded libraries.'

        def asakusaThunderGateFiles = project.configurations.create('asakusaThunderGateFiles')
        asakusaThunderGateFiles.description = 'Asakusa ThunderGate system files.'
        asakusaThunderGateFiles.transitive = false

        def asakusaYaessLogAnalyzer = project.configurations.create('asakusaYaessLogAnalyzer')
        asakusaYaessLogAnalyzer.description = 'Asakusa YAESS log analyzer libraries.'

        def asakusaHiveCli = project.configurations.create('asakusaHiveCli')
        asakusaHiveCli.description = 'Asakusa Hive CLI libraries.'
        asakusaHiveCli.extendsFrom project.configurations.compile
    }

    private void configureDependencies() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            project.dependencies {
                embedded project.sourceSets.main.libs
                compile group: 'org.slf4j', name: 'jcl-over-slf4j', version: base.slf4jVersion
                compile group: 'ch.qos.logback', name: 'logback-classic', version: base.logbackVersion
                asakusaThunderGateFiles group: 'com.asakusafw', name: 'asakusa-thundergate', version: extension.asakusafwVersion, classifier: 'dist'
                asakusaYaessLogAnalyzer group: 'com.asakusafw', name: 'asakusa-yaess-log-analyzer', version: extension.asakusafwVersion
                asakusaYaessLogAnalyzer group: 'ch.qos.logback', name: 'logback-classic', version: base.logbackVersion
                asakusaHiveCli group: 'com.asakusafw', name: 'asakusa-hive-cli', version: extension.asakusafwVersion
            }
        }
    }

    private void configureSourceSets() {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        SourceSet container = project.sourceSets.main

        // Application Libraries
        SourceDirectorySet libs = createSourceDirectorySet(container, 'libs', 'Application libraries')
        libs.filter.include '*.jar'
        libs.srcDirs { base.embeddedLibsDirectory }

        // DMDL source set
        SourceDirectorySet dmdl = createSourceDirectorySet(container, 'dmdl', 'DMDL scripts')
        dmdl.filter.include '**/*.dmdl'
        dmdl.srcDirs { extension.dmdl.dmdlSourceDirectory }
        container.java.srcDirs { extension.modelgen.modelgenSourceDirectory }

        // Annotation processors
        SourceDirectorySet annotations = createSourceDirectorySet(container, 'annotations', 'Java annotation processor results')
        annotations.srcDirs { extension.javac.annotationSourceDirectory }
        container.allJava.source annotations
        container.allSource.source annotations
        // Note: Don't add generated directory into container.output.dirs for eclipse plug-in

        // ThunderGate DDL source set
        SourceDirectorySet sql = createSourceDirectorySet(container, 'thundergateDdl', 'ThunderGate DDL scripts')
        sql.filter.include '**/*.sql'
        sql.srcDirs { extension.thundergate.ddlSourceDirectory }
        // Note: the generated DMDL source files will be added later only if there are actually required
    }

    private SourceDirectorySet createSourceDirectorySet(SourceSet parent, String name, String displayName) {
        assert parent instanceof ExtensionAware
        ExtensionContainer extensions = parent.extensions
        // currently, project.sourceSets.main.* is not ExtensionAware
        SourceDirectorySet extension = new DefaultSourceDirectorySet(name, displayName, fileResolver)
        extensions.add(name, extension)
        return extension
    }

    private void configureJavaPlugin() {
        // NOTE: Must configure project.sourceSets first for organizing *.compileClasspath/runtimeClasspath
        configureJavaSourceSets()
        configureJavaProjectProperties()
        configureJavaTasks()
    }

    private void configureJavaSourceSets() {
        // FIXME This REDEFINES compileClasspath/runtimeClasspath, please change it to modify FileCollections
        project.sourceSets {
            main.compileClasspath += project.configurations.provided
            test.compileClasspath += project.configurations.provided
            test.runtimeClasspath += project.configurations.provided
            main.compileClasspath += project.configurations.embedded
            test.compileClasspath += project.configurations.embedded
            test.runtimeClasspath += project.configurations.embedded
        }
    }

    private void configureJavaProjectProperties() {
        PluginUtils.afterEvaluate(project) {
            project.sourceCompatibility = extension.javac.sourceCompatibility
            project.targetCompatibility = extension.javac.targetCompatibility
        }
    }

    private void configureJavaTasks() {
        configureJavaCompileTask()
        configureJavadocTask()
    }

    private void configureJavaCompileTask() {
        PluginUtils.afterEvaluate(project) {
            [project.tasks.compileJava, project.tasks.compileTestJava].each { JavaCompile task ->
                task.options.encoding = extension.javac.sourceEncoding
            }
            Set<File> annotations = project.sourceSets.main.annotations.getSrcDirs()
            if (annotations.size() >= 1) {
                if (annotations.size() >= 2) {
                    throw new InvalidUserDataException("sourceSets.main.annotations has only upto 1 directory: ${annotations}")
                }
                File directory = annotations.iterator().next()
                project.tasks.compileJava.options.compilerArgs += ['-s', directory.absolutePath, '-Xmaxerrs', '10000']
                project.tasks.compileJava.inputs.property 'annotationSourceDirectory', directory.absolutePath
            }
        }
    }

    private void configureJavadocTask() {
        project.tasks.javadoc { Javadoc task ->
            // XXX Must reassign compileClasspath because sourceSets.main.compileClasspath is REDEFINED.
            task.classpath = project.sourceSets.main.compileClasspath
            if (task.options.hasProperty('docEncoding')) {
                task.options.docEncoding = 'UTF-8'
            }
            if (task.options.hasProperty('charSet')) {
                task.options.charSet = 'UTF-8'
            }
            task.dependsOn project.tasks.compileJava
        }
        PluginUtils.afterEvaluate(project) {
            project.tasks.javadoc { Javadoc task ->
                task.options.encoding = extension.javac.sourceEncoding
                task.options.source = String.valueOf(extension.javac.sourceCompatibility)
            }
        }
    }

    private void defineAsakusaTasks() {
        extendVersionsTask()
        defineCompileDMDLTask()
        defineCompileBatchappTask()
        defineJarBatchappTask()
        extendCompileJavaTask()
        defineGenerateTestbookTask()
        defineGenerateHiveDdlTask()
        defineSummarizeYaessJobTask()
        defineGenerateThunderGateDataModelTask()
        configureTestToolTasks()
    }

    private void extendVersionsTask() {
        project.tasks.getByName(AsakusafwBasePlugin.TASK_VERSIONS) << {
            def frameworkVersion
            try {
                frameworkVersion = extension.asakusafwVersion
            } catch (Exception e) {
                frameworkVersion = 'INVALID'
            }
            logger.lifecycle "Asakusa SDK: ${frameworkVersion}"
            logger.lifecycle "JVM: ${extension.javac.targetCompatibility}"
        }
    }

    private void defineCompileDMDLTask() {
        project.task('compileDMDL', type: CompileDmdlTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles Asakusa DMDL scripts.'
            launcherClasspath << project.configurations.asakusaToolLauncher
            sourcepath << project.sourceSets.main.dmdl
            toolClasspath << project.sourceSets.main.compileClasspath
            if (isFrameworkInstalled()) {
                pluginClasspath << project.fileTree(dir: getFrameworkFile('dmdl/plugin'), include: '**/*.jar')
            }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                packageName = { extension.modelgen.modelgenSourcePackage }
                sourceEncoding = { extension.dmdl.dmdlEncoding }
                targetEncoding = { extension.javac.sourceEncoding }
                outputDirectory = { project.file(extension.modelgen.modelgenSourceDirectory) }
            }
        }
    }

    private void defineCompileBatchappTask() {
        project.tasks.create('compileBatchapp') { Task task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Compiles Asakusa DSL source files.'

            // sub plug-ins must add '*CompileBatchapps' as a dependency of this task
        }
    }

    private void defineJarBatchappTask() {
        project.tasks.create('jarBatchapp', Jar) { Jar task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Assembles Asakusa batch applications into JAR file.'
            task.dependsOn 'compileBatchapp'

            task.destinationDir project.buildDir
            task.appendix 'batchapps'

            // sub plug-ins must add outputs of '*CompileBatchapps' as input of this task
        }
    }

    private extendCompileJavaTask() {
        project.tasks.compileJava.doFirst {
            project.delete(extension.javac.annotationSourceDirectory)
            project.mkdir(extension.javac.annotationSourceDirectory)
        }
        project.tasks.compileJava.dependsOn(project.tasks.compileDMDL)
    }

    private defineGenerateTestbookTask() {
        project.task('generateTestbook', type: GenerateTestbookTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Generates Asakusa test template Excel books.'
            launcherClasspath << project.configurations.asakusaToolLauncher
            sourcepath << project.sourceSets.main.dmdl
            toolClasspath << project.sourceSets.main.compileClasspath
            if (isFrameworkInstalled()) {
                pluginClasspath << project.fileTree(dir: getFrameworkFile('dmdl/plugin'), include: '**/*.jar')
            }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                sourceEncoding = { extension.dmdl.dmdlEncoding }
                outputSheetFormat = { extension.testtools.testDataSheetFormat }
                outputDirectory = { project.file(extension.testtools.testDataSheetDirectory) }
            }
        }
    }

    private void defineGenerateHiveDdlTask() {
        project.tasks.create('generateHiveDDL', GenerateHiveDdlTask) { GenerateHiveDdlTask task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Generates a Hive DDL file [Experimental].'
            task.toolClasspath += project.configurations.asakusaHiveCli
            task.toolClasspath += project.sourceSets.main.compileClasspath
            task.sourcepath = project.files({ project.sourceSets.main.output.classesDir })
            task.conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                outputFile = { project.file("${project.buildDir}/hive-ddl/${project.name}.sql") }
            }
            task.dependsOn project.tasks.compileJava
        }
    }

    private void defineSummarizeYaessJobTask() {
        project.task('summarizeYaessJob', type: AnalyzeYaessLogTask) { AnalyzeYaessLogTask task ->
            group ASAKUSAFW_BUILD_GROUP
            description 'Summarizes a YAESS log file [Experimental].'
            task.toolClasspath += project.configurations.asakusaYaessLogAnalyzer
            task.inputDriver = 'com.asakusafw.yaess.tools.log.basic.BasicYaessLogInput'
            task.outputDriver = 'com.asakusafw.yaess.tools.log.summarize.SummarizeYaessLogOutput'
            task.inputArguments.put 'encoding', 'UTF-8'
            task.outputArguments.put 'encoding', 'UTF-8'
            task.outputArguments.put 'code', /YS-CORE-\w04\d{3}/
            task.conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                // Note: no default value for 'inputFile' property
                outputFile = { new File(project.buildDir, 'reports/yaess-jobs.csv') }
            }
            task.doFirst {
                if (task.getInputFile() == null) {
                    throw new InvalidUserDataException("${task.name} --input </path/to/yaess-log> must be specified")
                }
            }
        }
    }

    private void defineGenerateThunderGateDataModelTask() {
        def thundergate = extension.thundergate
        def task = project.task('generateThunderGateDataModel', type: GenerateThunderGateDataModelTask) { Task task ->
            group ASAKUSAFW_BUILD_GROUP
            description 'Executes ThunderGate DDL files and generates their data models.'
            sourcepath << project.sourceSets.main.thundergateDdl
            toolClasspath << project.sourceSets.main.compileClasspath
            systemDdlFiles << { getThunderGateFile(task, 'bulkloader/sql/create_table.sql') }
            systemDdlFiles << { getThunderGateFile(task, 'bulkloader/sql/insert_import_table_lock.sql') }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                jdbcConfiguration = {
                    if (thundergate.jdbcFile != null) {
                        return project.file(thundergate.jdbcFile)
                    } else {
                        return getFrameworkFile("bulkloader/conf/${thundergate.target}-jdbc.properties")
                    }
                }
                ddlEncoding = { thundergate.ddlEncoding }
                includePattern = { thundergate.includes }
                excludePattern = { thundergate.excludes }
                dmdlOutputDirectory = { project.file(thundergate.dmdlOutputDirectory) }
                dmdlOutputEncoding = { extension.dmdl.dmdlEncoding }
                recordLockDdlOutput = { new File(project.file(thundergate.ddlOutputDirectory), 'record-lock-ddl.sql') }
                sidColumnName = { thundergate.sidColumn }
                timestampColumnName = { thundergate.timestampColumn }
                deleteFlagColumnName = { thundergate.deleteColumn }
                deleteFlagColumnValue = { thundergate.deleteValue }
            }
            onlyIf { thundergate.target != null || thundergate.jdbcFile != null }
            doFirst {
                if (thundergate.jdbcFile == null) {
                    checkFrameworkInstalled()
                }
            }
        }
        PluginUtils.afterEvaluate(project) {
            if (thundergate.target == null && thundergate.jdbcFile == null) {
                project.logger.info('Disables task: {}', task.name)
            } else {
                project.logger.info('Enables task: {} (using {})',
                    task.name,
                    thundergate.jdbcFile ?: thundergate.target)
                project.tasks.compileDMDL.dependsOn task
                project.sourceSets.main.dmdl.srcDirs { thundergate.dmdlOutputDirectory }
            }
        }
    }

    private void configureTestToolTasks() {
        project.tasks.withType(RunBatchappTask) { AbstractTestToolTask task ->
            task.toolClasspath = project.files({ project.sourceSets.test.runtimeClasspath })
        }
        project.tasks.withType(TestToolTask) { AbstractTestToolTask task ->
            task.toolClasspath = project.files({ project.sourceSets.test.runtimeClasspath })
        }
    }

    protected File findLogbackConf() {
        if (extension.logbackConf) {
            return project.file(extension.logbackConf)
        } else {
            return null
        }
    }

    /**
     * Sets the Asakusa Framework home path (internal use only).
     * @param path the home path
     */
    protected void setFrameworkHome(File path) {
        this.frameworkHome = path
    }

    protected boolean isFrameworkInstalled() {
        return frameworkHome != null && frameworkHome.exists()
    }

    protected def checkFrameworkInstalled() {
        if (isFrameworkInstalled()) {
            return true
        }
        if (frameworkHome == null) {
            throw new IllegalStateException('Environment variable "ASAKUSA_HOME" is not defined')
        }
        throw new IllegalStateException("Environment variable 'ASAKUSA_HOME' is not valid: ${this.frameworkHome}")
    }

    protected File getThunderGateFile(Task task, String relativePath) {
        // if jdbcFile is not set, then we use the deployed system files on $ASAKUSA_HOME
        def thundergate = extension.thundergate
        if (thundergate.jdbcFile == null) {
            return getFrameworkFile(relativePath)
        }

        // checking ThunderGate system file cache
        if (task.hasProperty('cacheThunderGateFile') == false) {
            task.ext.cacheThunderGateFile = [:]
        } else {
            def cached = task.cacheThunderGateFile.get(relativePath)
            if (cached != null && cached.isFile()) {
                return cached
            }
        }

        // extracting system files into task temporary directory
        File target = new File(task.temporaryDir, relativePath)
        project.delete target
        File distribution = project.configurations.asakusaThunderGateFiles.files.find()
        if (distribution == null) {
            throw new FileNotFoundException("ThunderGate system files are not configured")
        }
        RelativePath source = RelativePath.parse(true, relativePath)
        project.zipTree(distribution).visit { FileVisitDetails f ->
            if (f.relativePath == source) {
                project.logger.info "Preparing file: ${source} -> ${target}"
                f.copyTo target
                f.stopVisiting()
            }
        }

        // is distribution file broken or unsupported system file path?
        if (target.isFile() == false) {
            throw new FileNotFoundException("Missing '${relativePath}' in ThunderGate system files: ${distribution}")
        }
        task.cacheThunderGateFile.put(relativePath, target)
        return target
    }

    private File getFrameworkFile(String relativePath) {
        if (frameworkHome != null) {
            return new File(frameworkHome, relativePath)
        }
        return null
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
        new IdeaPluginEnhancement().apply(project)
    }
}
