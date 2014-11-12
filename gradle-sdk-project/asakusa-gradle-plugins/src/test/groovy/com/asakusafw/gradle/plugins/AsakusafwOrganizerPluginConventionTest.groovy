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

import static org.junit.Assert.*

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration
import com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration

/**
 * Test for {@link AsakusafwOrganizerPluginConvention}.
 */
class AsakusafwOrganizerPluginConventionTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.group = 'com.example.testing'
            project.version = '0.1.0'
            project.apply plugin: 'asakusafw-organizer'
            convention = project.asakusafwOrganizer
            return stmt
        }
    }

    Project project

    AsakusafwOrganizerPluginConvention convention

    /**
     * Test for {@code project.asakusafwOrganizer} convention default values.
     */
    @Test
    public void defaults() {
        assert convention != null

        try {
            convention.getAsakusafwVersion()
            fail()
        } catch (InvalidUserDataException e) {
            // ok
        }
        assert convention.assembleDir == "${project.buildDir}/asakusafw-assembly"
        assert convention.directio instanceof DirectIoConfiguration
        assert convention.thundergate instanceof ThunderGateConfiguration
        assert convention.windgate instanceof WindGateConfiguration
        assert convention.hive instanceof HiveConfiguration
        assert convention.yaess instanceof YaessConfiguration
        assert convention.batchapps instanceof BatchappsConfiguration
        assert convention.testing instanceof TestingConfiguration
        assert convention.extension instanceof ExtensionConfiguration
        assert convention.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.directio} convention default values.
     */
    @Test
    public void directio_defaults() {
        assert convention.directio.enabled == true
    }

    /**
     * Test for {@code project.asakusafwOrganizer.thundergate} convention default values.
     */
    @Test
    public void thundergate_defaults() {
        assert convention.thundergate.enabled == false
        assert convention.thundergate.target == null
    }

    /**
     * Test for {@code project.asakusafwOrganizer.thundergate.enable}.
     */
    @Test
    public void thundergate_enable_by_target() {
        convention.thundergate.target 'testing'
        assert convention.thundergate.enabled
        assert convention.thundergate.target == 'testing'
    }

    /**
     * Test for {@code project.asakusafwOrganizer.windgate} convention default values.
     */
    @Test
    public void windgate_defaults() {
        assert convention.windgate.enabled == true
        assert convention.windgate.sshEnabled == true
        assert convention.windgate.retryableEnabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.hive} convention default values.
     */
    @Test
    public void hive_defaults() {
        assert convention.hive.enabled == false
        assert convention.hive.libraries.size() == 1
        assert convention.hive.libraries[0].startsWith('org.apache.hive:hive-exec:')
    }

    /**
     * Test for {@code project.asakusafwOrganizer.extension} convention default values.
     */
    @Test
    public void extension_defaults() {
        assert convention.extension.libraries.size() == 0
    }

    /**
     * Test for {@code project.asakusafwOrganizer.yaess} convention default values.
     */
    @Test
    public void yaess_defaults() {
        assert convention.yaess.enabled == true
        assert convention.yaess.toolsEnabled == true
        assert convention.yaess.hadoopEnabled == true
        assert convention.yaess.jobqueueEnabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.batchapps} convention default values.
     */
    @Test
    public void batchapps_defaults() {
        assert convention.batchapps.enabled == true
    }

    /**
     * Test for {@code project.asakusafwOrganizer.testing} convention default values.
     */
    @Test
    public void testing_defaults() {
        assert convention.testing.enabled == false
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.dev} convention default values.
     */
    @Test
    public void dev_defaults() {
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        convention.asakusafwVersion = 'AFW-TEST'
        assert profile.name == "dev"
        assert profile.asakusafwVersion == convention.asakusafwVersion
        assert profile.assembleDir == "${convention.assembleDir}-dev"
        assert profile.archiveName == "asakusafw-${convention.asakusafwVersion}-dev.tar.gz"
        assert profile.directio.enabled == convention.directio.enabled
        assert profile.thundergate.enabled == convention.thundergate.enabled
        assert profile.windgate.enabled == convention.windgate.enabled
        assert profile.yaess.enabled == convention.yaess.enabled
        assert profile.batchapps.enabled == convention.batchapps.enabled
        assert profile.testing.enabled == true
        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.prod} convention default values.
     */
    @Test
    public void prod_defaults() {
        AsakusafwOrganizerProfile profile = convention.profiles.prod
        convention.asakusafwVersion = 'AFW-TEST'
        assert profile.name == "prod"
        assert profile.asakusafwVersion == convention.asakusafwVersion
        assert profile.assembleDir == "${convention.assembleDir}-prod"
        assert profile.archiveName == "asakusafw-${convention.asakusafwVersion}.tar.gz"
        assert profile.directio.enabled == convention.directio.enabled
        assert profile.thundergate.enabled == convention.thundergate.enabled
        assert profile.windgate.enabled == convention.windgate.enabled
        assert profile.yaess.enabled == convention.yaess.enabled
        assert profile.batchapps.enabled == convention.batchapps.enabled
        assert profile.testing.enabled == convention.testing.enabled
        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} convention default values.
     */
    @Test
    public void profiles_defaults() {
        assert convention.profiles.collect { it.name }.toSet() == ['dev', 'prod'].toSet()

        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert convention.profiles.collect { it.name }.toSet() == ['dev', 'prod', 'testProfile'].toSet()

        assert profile != null
        assert profile.name == "testProfile"

        convention.asakusafwVersion = 'AFW-TEST'
        assert profile.asakusafwVersion == convention.asakusafwVersion

        convention.assembleDir = 'AFW-TEST'
        assert profile.assembleDir == "${convention.assembleDir}-testProfile"
        assert profile.archiveName == "asakusafw-${convention.asakusafwVersion}-testProfile.tar.gz"

        assert profile.directio.enabled == convention.directio.enabled
        convention.directio.enabled = !convention.directio.enabled
        assert profile.directio.enabled == convention.directio.enabled

        assert profile.thundergate.enabled == convention.thundergate.enabled
        convention.thundergate.enabled = !convention.thundergate.enabled
        assert profile.thundergate.enabled == convention.thundergate.enabled

        assert profile.windgate.enabled == convention.windgate.enabled
        convention.windgate.enabled = !convention.windgate.enabled
        assert profile.windgate.enabled == convention.windgate.enabled

        assert profile.yaess.enabled == convention.yaess.enabled
        convention.yaess.enabled = !convention.yaess.enabled
        assert profile.yaess.enabled == convention.yaess.enabled

        assert profile.batchapps.enabled == convention.batchapps.enabled
        convention.batchapps.enabled = !convention.batchapps.enabled
        assert profile.batchapps.enabled == convention.batchapps.enabled

        assert profile.testing.enabled == convention.testing.enabled
        convention.testing.enabled = !convention.testing.enabled
        assert profile.testing.enabled == convention.testing.enabled

        assert profile.components.handlers.isEmpty()
        assert profile.assembly.handlers.isEmpty()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*} must be splitted from inherited convention values.
     */
    @Test
    public void profiles_split() {
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile

        convention.asakusafwVersion = 'AFW-TEST'
        profile.asakusafwVersion = 'PRF-TEST'
        assert profile.asakusafwVersion != convention.asakusafwVersion

        profile.assembleDir = 'AFW-TEST'
        assert profile.assembleDir != "${convention.assembleDir}-testProfile"

        profile.directio.enabled = !convention.directio.enabled
        assert profile.directio.enabled != convention.directio.enabled

        profile.thundergate.enabled = !convention.thundergate.enabled
        assert profile.thundergate.enabled != convention.thundergate.enabled

        profile.windgate.enabled = !convention.windgate.enabled
        assert profile.windgate.enabled != convention.windgate.enabled

        profile.yaess.enabled = !convention.yaess.enabled
        assert profile.yaess.enabled != convention.yaess.enabled

        profile.batchapps.enabled = !convention.batchapps.enabled
        assert profile.batchapps.enabled != convention.batchapps.enabled

        profile.testing.enabled = !convention.testing.enabled
        assert profile.testing.enabled != convention.testing.enabled
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} with direct property access.
     */
    @Test
    public void profiles_configure_property() {
        convention.asakusafwVersion = 'AFW-TEST'
        convention.profiles.testProfile.asakusafwVersion 'TEST-PROFILE'
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert profile != null
        assert profile.name == "testProfile"
        assert profile.asakusafwVersion == 'TEST-PROFILE'
        assert convention.asakusafwVersion == 'AFW-TEST'
        assert convention.profiles.other.asakusafwVersion == convention.asakusafwVersion
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles} with configuration closure.
     */
    @Test
    public void profiles_configure_closure() {
        convention.asakusafwVersion = 'AFW-TEST'
        convention.profiles.testProfile {
            asakusafwVersion 'TEST-PROFILE'
        }
        AsakusafwOrganizerProfile profile = convention.profiles.testProfile
        assert profile != null
        assert profile.name == "testProfile"
        assert profile.asakusafwVersion == 'TEST-PROFILE'
        assert convention.asakusafwVersion == 'AFW-TEST'
        assert convention.profiles.other.asakusafwVersion == convention.asakusafwVersion
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*.hive.libraries} with parent configurations.
     */
    @Test
    public void profiles_hive_libraries() {
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        convention.hive.libraries = ['a']
        convention.hive.libraries = ['b0', 'b1']
        assert profile.hive.libraries.toSet() == ['b0', 'b1'].toSet()

        profile.hive.libraries += ['c0']
        assert convention.hive.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.hive.libraries.toSet() == ['b0', 'b1', 'c0'].toSet()

        profile.hive.libraries = ['d0', 'd1']
        assert convention.hive.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.hive.libraries.toSet() == ['d0', 'd1'].toSet()

        convention.hive.libraries = ['e0']
        assert convention.hive.libraries.toSet() == ['e0'].toSet()
        assert profile.hive.libraries.toSet() == ['d0', 'd1'].toSet()
    }

    /**
     * Test for {@code project.asakusafwOrganizer.profiles.*.extension.libraries} with parent configurations.
     */
    @Test
    public void profiles_extension_libraries() {
        AsakusafwOrganizerProfile profile = convention.profiles.dev
        convention.extension.libraries = ['a']
        convention.extension.libraries = ['b0', 'b1']
        assert profile.extension.libraries.toSet() == ['b0', 'b1'].toSet()

        profile.extension.libraries += ['c0']
        assert convention.extension.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.extension.libraries.toSet() == ['b0', 'b1', 'c0'].toSet()

        profile.extension.libraries = ['d0', 'd1']
        assert convention.extension.libraries.toSet() == ['b0', 'b1'].toSet()
        assert profile.extension.libraries.toSet() == ['d0', 'd1'].toSet()

        convention.extension.libraries = ['e0']
        assert convention.extension.libraries.toSet() == ['e0'].toSet()
        assert profile.extension.libraries.toSet() == ['d0', 'd1'].toSet()
    }
}
