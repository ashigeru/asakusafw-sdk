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

import java.lang.reflect.Field
import java.lang.reflect.Modifier

import org.gradle.api.JavaVersion

/**
 * Convention class for {@link AsakusafwPlugin}.
 * @since 0.5.2
 * @version 0.7.3
 */
class AsakusafwPluginConvention {

    /**
     * Schema version of this convention.
     */
    static final CONVENTION_SCHEMA_VERSION = '1.3.0'

    /**
     * The schema version of this convention.
     * @since 0.6.1
     */
    final String conventionSchemaVersion = CONVENTION_SCHEMA_VERSION

    /**
     * Asakusa Framework Version.
     * This property must be specified in project configuration.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> pom.xml - {@code properties/asakusafw.version} </dd>
     *   <dt> Default value: </dt>
     *     <dd> N/A </dd>
     * </dl>
     */
    String asakusafwVersion

    /**
     * Maximum heap size for Model Generator process.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> N/A </dd>
     *   <dt> Default value: </dt>
     *     <dd> {@code '1024m'} </dd>
     * </dl>
     */
    String maxHeapSize

    /**
     * Logback configuration file path.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> N/A </dd>
     *   <dt> Default value: </dt>
     *     <dd> <code>"src/${project.sourceSets.test.name}/resources/logback-test.xml"</code> </dd>
     * </dl>
     */
    String logbackConf

    /**
     * The base Java package name used in Asakusa Framework code generation.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> (may be specified in {@code mvn archetype:generate}) </dd>
     *   <dt> Default value: </dt>
     *     <dd> {@code project.group} </dd>
     * </dl>
     */
    String basePackage

    /**
     * DMDL settings.
     */
    DmdlConfiguration dmdl

    /**
     * Model generator settings.
     */
    ModelgenConfiguration modelgen

    /**
     * Java compiler settings.
     */
    JavacConfiguration javac

    /**
     * DSL compiler settings.
     */
    CompilerConfiguration compiler

    /**
     * Test tools settings.
     */
    TestToolsConfiguration testtools

    /**
     * ThunderGate settings.
     * @since 0.6.1
     */
    ThunderGateConfiguration thundergate

    /**
     * DMDL settings for building Asakusa batch applications.
     */
    static class DmdlConfiguration {

        /**
         * The character encoding using in DMDL sources.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.dmdl.encoding} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'UTF-8'} </dd>
         * </dl>
         */
        String dmdlEncoding

        /**
         * The directory stored dmdl sources.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.dmdl.dir} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"src/${project.sourceSets.main.name}/dmdl"</code> </dd>
         * </dl>
         */
        String dmdlSourceDirectory
    }

    /**
     * Model Generator settings for building Asakusa batch applications.
     */
    static class ModelgenConfiguration {

        /**
         * Package name that is used Model classes generated by Model Generator.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.package} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.asakusafw.basePackage}.modelgen"</code> </dd>
         * </dl>
         */
        String modelgenSourcePackage

        /**
         * The directory where model sources are generated.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.output} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/generated-sources/modelgen"</code> </dd>
         * </dl>
         */
        String modelgenSourceDirectory
    }

    /**
     * Java compiler settings for building Asakusa batch applications.
     */
    static class JavacConfiguration {

        /**
         * The directory where compiled operator impl/factory sources are generated.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/generated-sources/annotations"</code> </dd>
         * </dl>
         */
        String annotationSourceDirectory

        /**
         * Java source encoding of project.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> pom.xml - {@code properties/project.build.sourceEncoding} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'UTF-8'} </dd>
         * </dl>
         */
        String sourceEncoding

        /**
         * Java version compatibility to use when compiling Java source.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code '1.7'} </dd>
         * </dl>
         */
        JavaVersion sourceCompatibility

        /**
         * Java version to generate classes for.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code '1.7'} </dd>
         * </dl>
         */
        JavaVersion targetCompatibility

        void sourceCompatibility(Object value) {
            this.sourceCompatibility = JavaVersion.toVersion(value)
        }

        void targetCompatibility(Object value) {
            this.targetCompatibility = JavaVersion.toVersion(value)
        }
    }

    /**
     * DSL compiler settings for building Asakusa batch applications.
     */
    static class CompilerConfiguration {

        /**
         * Package name that is used batch compiled classes for Hadoop MapReduce, JobClient and so on.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.package.default} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.asakusafw.basePackage}.batchapp"</code> </dd>
         * </dl>
         */
        String compiledSourcePackage

        /**
         * The directory where batch compiled sources are stored.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.batchc.dir} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/batchc"</code> </dd>
         * </dl>
         */
        String compiledSourceDirectory

        /**
         * DSL Compiler options.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.compiler.options} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"XjavaVersion={@link AsakusafwPluginConvention.JavacConfiguration#targetCompatibility}" </dd>
         * </dl>
         */
        List<String> compilerOptions

        /**
         * The directory where work files for batch compile are stored (optional).
         * If this property is {@code null}, the compiler use a temporary directory.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.compilerwork.dir} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String compilerWorkDirectory

        /**
         * The working root directory when used hadoop job execution.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.hadoopwork.dir} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>'target/hadoopwork/${execution_id}'</code> </dd>
         * </dl>
         */
        String hadoopWorkDirectory

        /**
         * Sets the DSL Compiler options.
         *
         * @param compilerOptions The value for the DSL Compiler options as comma separated String
         */
        void compilerOptions(String compilerOptions) {
            setCompilerOptions(compilerOptions.tokenize(',').collect {it.trim()})
        }

        /**
         * Sets the DSL Compiler options.
         *
         * @param compilerOptions The value for the DSL Compiler options as List of String
         */
        void compilerOptions(List<String> compilerOptions) {
            this.compilerOptions = compilerOptions
        }

    }

    /**
     * Test tools settings for building Asakusa batch applications.
     */
    static class TestToolsConfiguration {

        /**
         * The format of test data sheet.
         * This must be {@code (DATA|RULE|INOUT|INSPECT|ALL|DATAX|RULEX|INOUTX|INSPECTX|ALLX)}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.testdatasheet.format} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'ALL'} </dd>
         * </dl>
         */
        String testDataSheetFormat

        /**
         * The directory where test data sheet files are generated.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties - {@code asakusa.testdatasheet.output} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/excel"</code> </dd>
         * </dl>
         */
        String testDataSheetDirectory
    }

    /**
     * ThunderGate settings for building Asakusa batch applications.
     * @since 0.6.1
     */
    static class ThunderGateConfiguration {

        /**
         * The ThunderGate default name using in the development environment (optional).
         * This will be used for detecting JDBC connection configuration file in the installed Asakusa Framework
         * and generating DMDL files from the target database metadata.
         * ThunderGate facilities will be enabled when this value is non-null,
         * and the facilities may require that the Asakusa Framework with ThunderGate is
         * correctly installed to execute relative tasks.
         * If {@link #jdbcFile} is also set, this property will be ignored.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.target} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String target

        /**
         * The external ThunderGate JDBC connection configuration file path (optional).
         * ThunderGate facilities will be enabled when this value is non-null.
         * The target file must be a Java properties file, and it must include following properties:
         * <table>
         *   <tr>
         *     <th> Key </th>
         *     <th> Value </th>
         *   </tr>
         *   <tr>
         *     <th> {@code jdbc.driver} </th>
         *     <th> JDBC driver class name </th>
         *   </tr>
         *   <tr>
         *     <th> {@code jdbc.url} </th>
         *     <th> target database URL </th>
         *   </tr>
         *   <tr>
         *     <th> {@code jdbc.user} </th>
         *     <th> connection user name </th>
         *   </tr>
         *   <tr>
         *     <th> {@code jdbc.password} </th>
         *     <th> connection password </th>
         *   </tr>
         *   <tr>
         *     <th> {@code database.name} </th>
         *     <th> target database name </th>
         *   </tr>
         * </table>
         * If this property is set, the related tasks will not require any Asakusa Framework installations.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         * @since 0.7.0
         */
        String jdbcFile

        /**
         * DDL sources charset encoding name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (use default system encoding) </dd>
         * </dl>
         */
        String ddlEncoding

        /**
         * The DDL source path.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"src/${project.sourceSets.main.name}/sql/modelgen"</code> </dd>
         * </dl>
         */
        String ddlSourceDirectory

        /**
         * The inclusion target table/view name pattern in regular expression (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.includes} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (includes all targets) </dd>
         * </dl>
         */
        String includes

        /**
         * The exclusion target table/view name pattern in regular expression (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.excludes} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} (no exclusion targets) </dd>
         * </dl>
         */
        String excludes

        /**
         * The generated DMDL files output path from table/view definitions using DDLs.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.dmdl.fromddl.output} </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/thundergate/dmdl"</code> </dd>
         * </dl>
         */
        String dmdlOutputDirectory

        /**
         * The generated SQL files output path from table/view definitions using DDLs.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Default value: </dt>
         *     <dd> <code>"${project.buildDir}/thundergate/sql"</code> </dd>
         * </dl>
         */
        String ddlOutputDirectory

        /**
         * The system ID column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.sid.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'SID'} </dd>
         * </dl>
         */
        String sidColumn

        /**
         * The last modified timestamp column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.timestamp.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'UPDT_DATETIME'} </dd>
         * </dl>
         */
        String timestampColumn

        /**
         * The logical delete flag column name (optional).
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.delete.column} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code 'DELETE_FLAG'} </dd>
         * </dl>
         */
        String deleteColumn

        /**
         * The logical delete flag value representation in DMDL (optional).
         * Note that the text values must be enclosed with double-quotations like as {@code "<text-value>"}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.modelgen.delete.value} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code '"1"'} </dd>
         * </dl>
         */
        String deleteValue
    }

    @Override
    String toString() {
        // explicitly invoke meta-method
        def delegate = this.metaClass.getMetaMethod('toStringDelegate')
        if (delegate) {
            return delegate.invoke(this)
        }
        return toStringDelegate()
    }

    String toStringDelegate() {
        return super.toString()
    }

    def getConventionProperties() {
        return asMap(AsakusafwPluginConvention, this, 'com.asaksuafw.asakusafw.')
    }

    private static def asMap(Class<?> declared, Object obj, String keyPrefix) {
        def results = [:]
        for (Field field : declared.declaredFields.findAll{ !it.synthetic && !Modifier.isStatic(it.getModifiers()) && obj.hasProperty(it.name) }) {
            String propertyKey = keyPrefix + field.name
            Class<?> propertyType = field.type
            Object propertyValue
            try {
                propertyValue = obj.getAt(field.name)
            } catch (Exception e) {
                // ignores unset value
                propertyValue = null
            }
            if (propertyValue != null && isConventionMember(propertyType)) {
                results += asMap(propertyType, propertyValue, propertyKey + '.')
            } else {
                results[propertyKey] = propertyValue?.toString() ?: ''
            }
        }
        return results
    }

    private static boolean isConventionMember(Class<?> target) {
        if (target == null || target.isPrimitive() || target == String) {
            return false
        } else if (AsakusafwPluginConvention.class.isAssignableFrom(target)) {
            return true
        }
        return isConventionMember(target.getEnclosingClass())
    }
}
