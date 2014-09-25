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
package com.asakusafw.gradle.plugins

import org.gradle.api.NamedDomainObjectContainer

import com.asakusafw.gradle.assembly.AsakusafwAssembly

/**
 * Convention class for {@link AsakusafwOrganizerPlugin}.
 * @since 0.5.2
 * @version 0.7.0
 */
class AsakusafwOrganizerPluginConvention {

    /**
     * Asakusa Framework version.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> pom.xml - {@code properties/asakusafw.version} </dd>
     *   <dt> Default value: </dt>
     *     <dd> {@code project.asakusafw.asakusafwVersion} - only if {@code asakusafw} plug-in is enabled </dd>
     *     <dd> N/A - otherwise</dd>
     * </dl>
     */
    String asakusafwVersion

    /**
     * Working directory path prefix of framework organizer.
     * <dl>
     *   <dt> Migration from Maven-Archetype: </dt>
     *     <dd> N/A </dd>
     *   <dt> Default value: </dt>
     *     <dd> <code>"${project.buildDir}/asakusafw-assembly"</code> </dd>
     * </dl>
     */
    String assembleDir

    /**
     * Direct I/O settings.
     * @since 0.7.0
     */
    DirectIoConfiguration directio

    /**
     * ThunderGate settings.
     * @since 0.6.1
     */
    ThunderGateConfiguration thundergate

    /**
     * WindGate settings.
     * @since 0.7.0
     */
    WindGateConfiguration windgate

    /**
     * Hive settings.
     * @since 0.7.0
     */
    HiveConfiguration hive

    /**
     * YAESS settings.
     * @since 0.7.0
     */
    YaessConfiguration yaess

    /**
     * Batch applications settings.
     * @since 0.7.0
     */
    BatchappsConfiguration batchapps

    /**
     * Test driver settings.
     * @since 0.7.0
     */
    TestingConfiguration testing

    /**
     * Profiles for organizing framework package.
     * @since 0.7.0
     */
    NamedDomainObjectContainer<AsakusafwOrganizerProfile> profiles

    /**
     * Custom framework files.
     */
    final AsakusafwAssembly assembly = new AsakusafwAssembly("assembly")

    /**
     * Direct I/O settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class DirectIoConfiguration {

        /**
         * Configuration whether Direct I/O features are enabled or not.
         * Direct I/O facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentDirectIo} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled
    }

    /**
     * WindGate settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class WindGateConfiguration {

        /**
         * Configuration whether WindGate features are enabled or not.
         * WindGate facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentWindGate} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Configuration whether WindGate SSH feature is enabled or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentWindGateSsh} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean sshEnabled

        /**
         * Configuration whether WindGate retryable feature is enabled or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachExtensionWindGateRetryable} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean retryableEnabled
    }

    /**
     * ThunderGate settings for the Asakusa Framework organizer.
     * @since 0.6.1
     */
    static class ThunderGateConfiguration {

        /**
         * Configuration whether ThunderGate features are enabled or not.
         * ThunderGate facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.enabled} </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentThunderGate} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * The ThunderGate default target file name prefix (optional).
         * If this property is set, Asakusa Framework assembly file will include
         * {@code bulkloader/conf/[target]-jdbc.properties} file with following system default settings:
         * <table border="1">
         *   <tr>
         *     <th> Key </th>
         *     <th> Value </th>
         *   </tr>
         *   <tr>
         *     <td> {@code jdbc.driver} </td>
         *     <td> {@code com.mysql.jdbc.Driver} </td>
         *   </tr>
         *   <tr>
         *     <td> {@code jdbc.url} </td>
         *     <td> {@code jdbc:mysql://localhost/asakusa} </td>
         *   </tr>
         *   <tr>
         *     <td> {@code jdbc.user} </td>
         *     <td> {@code asakusa} </td>
         *   </tr>
         *   <tr>
         *     <td> {@code jdbc.password} </td>
         *     <td> {@code asakusa} </td>
         *   </tr>
         *   <tr>
         *     <td> {@code database.name} </td>
         *     <td> {@code asakusa} </td>
         *   </tr>
         *   <tr>
         *     <td> {@code db.parameter} </td>
         *     <td> (empty) </td>
         *   </tr>
         * </table>
         *
         * This configuration may be convenient for development environments with using the above connection settings,
         * but clients should prepare configured settings files and
         * deploy them using {@link AsakusafwOrganizerProfile#assembly} instead in many cases.
         *
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> build.properties: {@code asakusa.database.target} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code null} </dd>
         * </dl>
         */
        String target

        /**
         * Sets the ThunderGate default target file name prefix.
         * @param value the target name
         */
        void setTarget(String value) {
            this.target = value
            if (value != null && !isEnabled()) {
                setEnabled(true)
            }
        }
    }

    /**
     * Direct I/O Hive settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class HiveConfiguration {

        /**
         * Configuration whether Direct I/O Hive features are enabled or not.
         * Direct I/O Hive facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachExtensionDirectIoHive} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Default libraries for Direct I/O Hive runtime.
         * Clients should not modify this property, and use {@link #libraries} instead.
         */
        List<Object> defaultLibraries = []

        /**
         * Libraries for Direct I/O Hive runtime.
         * <dl>
         *   <dt> Default value: </dt>
         *     <dd> Framework default hive version </dd>
         * </dl>
         */
        List<Object> libraries

        /**
         * Returns libraries for Direct I/O Hive runtime.
         * @return the libraries
         */
        List<Object> getLibraries() {
            if (this.@libraries == null) {
                return Collections.unmodifiableList(getDefaultLibraries())
            }
            return Collections.unmodifiableList(this.@libraries)
        }

        /**
         * Sets libraries for Direct I/O Hive runtime.
         * @param libraries the libraries
         */
        void setLibraries(Object... libraries) {
            // copy on write
            List<Object> list = new ArrayList<Object>()
            list.addAll(libraries.flatten())
            this.@libraries = list
        }
    }

    /**
     * YAESS settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class YaessConfiguration {

        /**
         * Configuration whether YAESS features are enabled or not.
         * YAESS facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentYaess} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled

        /**
         * Configuration whether YAESS Hadoop bridge is enabled or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentYaessHadoop} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean hadoopEnabled

        /**
         * Configuration whether YAESS extra tools is enabled or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachExtensionYaessTools} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean toolsEnabled

        /**
         * Configuration whether YAESS JobQueue client is enabled or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachExtensionYaessJobQueue} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean jobqueueEnabled
    }

    /**
     * Batch application settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class BatchappsConfiguration {

        /**
         * Configuration whether batch applications are included or not.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachBatchapps} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code true} </dd>
         * </dl>
         */
        boolean enabled
    }

    /**
     * Test driver settings for the Asakusa Framework organizer.
     * @since 0.7.0
     */
    static class TestingConfiguration {

        /**
         * Configuration whether test driver features are enabled or not.
         * Testing facilities will be enabled only if this value is {@code true}.
         * <dl>
         *   <dt> Migration from Maven-Archetype: </dt>
         *     <dd> N/A </dd>
         *   <dt> Attachment task: </dt>
         *     <dd> {@code attachComponentTesting} </dd>
         *   <dt> Default value: </dt>
         *     <dd> {@code false} </dd>
         * </dl>
         */
        boolean enabled
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
}
