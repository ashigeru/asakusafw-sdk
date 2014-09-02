/**
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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

import com.asakusafw.gradle.plugins.internal.AsakusafwOrganizer

/**
 * Test for {@link AsakusafwOrganizerPlugin}.
 */
class AsakusafwOrganizerPluginTest {

    /**
     * The test initializer.
     */
    @Rule
    public final TestRule initializer = new TestRule() {
        Statement apply(Statement stmt, Description desc) {
            project = ProjectBuilder.builder().withName(desc.methodName).build()
            project.plugins.apply 'asakusafw-organizer'
            return stmt
        }
    }

    Project project

    /**
     * Test for {@code project.tasks} for organizer facade tasks.
     */
    @Test
    public void tasks_common() {
        assert project.tasks.cleanAssembleAsakusafw
        assert project.tasks.attachBatchapps
        assert project.tasks.attachComponentCore
        assert project.tasks.attachComponentDirectIo
        assert project.tasks.attachComponentWindGate
        assert project.tasks.attachComponentWindGateSsh
        assert project.tasks.attachComponentThunderGate
        assert project.tasks.attachComponentYaess
        assert project.tasks.attachComponentYaessHadoop
        assert project.tasks.attachComponentDevelopment
        assert project.tasks.attachComponentTesting
        assert project.tasks.attachComponentOperation
        assert project.tasks.attachExtensionDirectIoHive
        assert project.tasks.attachExtensionWindGateRetryable
        assert project.tasks.attachExtensionYaessJobQueue
        assert project.tasks.attachAssemble
        assert project.tasks.assembleAsakusafw
        assert project.tasks.installAsakusafw
    }

    /**
     * Test for {@code project.tasks} for profile tasks.
     */
    @Test
    public void tasks_profile() {
        AsakusafwOrganizerProfile profile = project.asakusafwOrganizer.profiles.testp
        assert ptask(profile, 'cleanAssembleAsakusafw')
        assert ptask(profile, 'attachBatchapps')
        assert ptask(profile, 'attachComponentCore')
        assert ptask(profile, 'attachComponentDirectIo')
        assert ptask(profile, 'attachComponentWindGate')
        assert ptask(profile, 'attachComponentWindGateSsh')
        assert ptask(profile, 'attachComponentThunderGate')
        assert ptask(profile, 'attachComponentYaess')
        assert ptask(profile, 'attachComponentYaessHadoop')
        assert ptask(profile, 'attachComponentDevelopment')
        assert ptask(profile, 'attachComponentTesting')
        assert ptask(profile, 'attachComponentOperation')
        assert ptask(profile, 'attachExtensionDirectIoHive')
        assert ptask(profile, 'attachExtensionWindGateRetryable')
        assert ptask(profile, 'attachExtensionYaessTools')
        assert ptask(profile, 'attachExtensionYaessJobQueue')
        assert ptask(profile, 'attachAssemble')

        // assembleAsakusafw will be created in 'project.afterEvaluate' block
    }

    /**
     * Test for dependencies of {@code project.tasks} in organizer facade tasks.
     */
    @Test
    public void tasks_dependencies() {
        checkDependencies('cleanAssembleAsakusafw')
        checkDependencies('attachBatchapps')
        checkDependencies('attachComponentCore')
        checkDependencies('attachComponentDirectIo')
        checkDependencies('attachComponentWindGate')
        checkDependencies('attachComponentWindGateSsh')
        checkDependencies('attachComponentThunderGate')
        checkDependencies('attachComponentYaess')
        checkDependencies('attachComponentYaessHadoop')
        checkDependencies('attachComponentDevelopment')
        checkDependencies('attachComponentTesting')
        checkDependencies('attachComponentOperation')
        checkDependencies('attachExtensionDirectIoHive')
        checkDependencies('attachExtensionWindGateRetryable')
        checkDependencies('attachExtensionYaessTools')
        checkDependencies('attachExtensionYaessJobQueue')
        checkDependencies('attachAssemble')
        checkDependencies('assembleAsakusafw')
    }

    /**
     * Test for {@code project.tasks} for development profile tasks.
     */
    @Test
    public void dev_tasks() {
        AsakusafwOrganizerProfile profile = project.asakusafwOrganizer.profiles.dev
        assert dependencies(project.tasks.installAsakusafw).contains(pname(profile, 'gatherAsakusafw'))
    }

    /**
     * Test for {@code project.tasks} for production profile tasks.
     */
    @Test
    public void prod_tasks() {
        AsakusafwOrganizerProfile profile = project.asakusafwOrganizer.profiles.prod
        assert dependencies(project.tasks.installAsakusafw).contains(pname(profile, 'gatherAsakusafw')) == false
    }

    /**
     * Test for {@code project.tasks.addConf<Name>}.
     */
    @Test
    public void tasks_addConf() {
        Task task = project.tasks.findByName('attachConfTesting')
        assert task
        assert task.ext.distTarget == 'testing'
        project.asakusafwOrganizer.profiles.all { profile ->
            assert dependencies(ptask(profile, 'gatherAsakusafw')).contains(task.name)
        }
    }

    private String pname(AsakusafwOrganizerProfile profile, String name) {
        AsakusafwOrganizer organizer = new AsakusafwOrganizer(project, profile)
        assert organizer.taskName(name) != name
        return organizer.taskName(name)
    }

    private Task ptask(AsakusafwOrganizerProfile profile, String name) {
        AsakusafwOrganizer organizer = new AsakusafwOrganizer(project, profile)
        assert organizer.taskName(name) != name
        return organizer.task(name)
    }

    private void checkDependencies(String name) {
        assert dependencies(project.tasks.getByName(name)).containsAll(profileTasks(name))
    }

    private Set<String> profileTasks(String name) {
        return project.asakusafwOrganizer.profiles.collect { AsakusafwOrganizerProfile profile ->
            return new AsakusafwOrganizer(project, profile).taskName(name)
        }.toSet()
    }

    private Set<String> dependencies(Task task) {
        return task.getDependsOn().collect {
            if (it instanceof Task) {
                return it.name
            } else {
                return String.valueOf(it)
            }
        }.toSet()
    }
}
