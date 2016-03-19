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
package com.asakusafw.gradle.plugins

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.UnexpectedBuildResultException
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName

/**
 * Tests for cross Gradle versions compatibility.
 */
class AsakusaUpgradeTest {

    /**
     * temporary project directory.
     */
    @Rule
    public final TemporaryFolder projectDir = new TemporaryFolder()

    /**
     * handles running test name.
     */
    @Rule
    public final TestName testName = new TestName()

    /**
     * Test for the system Gradle version.
     */
    @Test
    void system() {
        doUpgrade(GradleVersion.current().version)
    }

    /**
     * Test for {@code 2.12} (Asakusa {@code 0.8.0}).
     */
    @Test
    void 'v2.12'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.8} (Asakusa {@code 0.7.5}).
     */
    @Test
    void 'v2.8'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.4} (Fixed on Asakusa {@code 0.7.4}).
     */
    @Test
    void 'v2.4'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 2.1} (Asakusa {@code 0.7.0}).
     */
    @Test
    void 'v2.1'() {
        doUpgradeFromTestName()
    }

    /**
     * Test for {@code 1.12} (Asakusa {@code 0.6.0}).
     */
    @Test
    void 'v1.12'() {
        doUpgradeFromTestName()
    }

    private void doUpgradeFromTestName() {
        doUpgrade(testName.methodName.replaceFirst('v', ''))
    }

    private void doUpgrade(String version) {
        Set<File> classpath = toClasspath(AsakusafwBasePlugin, 'META-INF/gradle-plugins/asakusafw-sdk.properties')
        String script = """
            buildscript {
                repositories {
                    mavenCentral()
                }
                dependencies {
                    classpath files(${classpath.collect { "'''${it.absolutePath}'''" }.join(', ')})
                    classpath 'org.codehaus.groovy:groovy-backports-compat23:${GroovySystem.version}'
                }
            }
            apply plugin: 'asakusafw-sdk'
            apply plugin: 'asakusafw-organizer'
            """.stripIndent()
        File buildScript = projectDir.newFile('build.gradle')
        buildScript.setText(script, 'UTF-8')
        BuildResult result
        try {
            result = GradleRunner.create()
                    .withGradleVersion(version)
                    .withProjectDir(projectDir.root)
                    .withArguments(AsakusafwBasePlugin.TASK_UPGRADE, '-i', '-s')
                    .build()
        } catch (UnexpectedBuildResultException t) {
            throw new AssertionError(script, t)
        }
        if (GradleVersion.version(version).compareTo(GradleVersion.version('2.5')) >= 0) {
            assert result.task(":${AsakusafwBasePlugin.TASK_UPGRADE}").outcome == TaskOutcome.SUCCESS
        }
    }

    private static Set<File> toClasspath(Object... resources) {
        Set<File> results = new LinkedHashSet<>()
        for (Object resource : resources) {
            if (resource instanceof Class<?>) {
                results.add(toClasspathEntry((Class<?>) resource))
            } else if (resource instanceof String) {
                results.add(toClasspathEntry((String) resource))
            } else {
                throw new AssertionError(resource)
            }
        }
        return results
    }

    private static File toClasspathEntry(String resourcePath) {
        URL resource = AsakusaUpgradeTest.class.getClassLoader().getResource(resourcePath)
        if (resource == null) {
            throw new AssertionError(resourcePath)
        }
        return findLibraryFromUrl(resource, resourcePath)
    }

    private static File toClasspathEntry(Class<?> aClass) {
        URL resource = toUrl(aClass)
                if (resource == null) {
                    throw new AssertionError(aClass)
                }
        String resourcePath = toResourcePath(aClass)
                return findLibraryFromUrl(resource, resourcePath)
    }

    private static String toResourcePath(Class<?> aClass) {
        return aClass.getName().replace('.', '/') + '.class'
    }

    private static URL toUrl(Class<?> aClass) {
        String className = aClass.getName()
        int start = className.lastIndexOf('.') + 1
        String name = className.substring(start)
        URL resource = aClass.getResource(name + '.class')
        return resource
    }

    private static Set<File> findLibrariesByResource(ClassLoader classLoader, String path) {
        Set<File> results = new LinkedHashSet<>()
        for (URL url : Collections.list(classLoader.getResources(path))) {
            File library = findLibraryFromUrl(url, path)
            if (library != null) {
                results.add(library)
            }
        }
        return results
    }

    private static File findLibraryFromUrl(URL resource, String resourcePath) {
        String protocol = resource.getProtocol()
        if (protocol.equals('file')) {
            File file = new File(resource.toURI())
            return toClassPathRoot(file, resourcePath)
        } else if (protocol.equals('jar')) {
            String path = resource.getPath()
            return toClassPathRoot(path, resourcePath)
        } else {
            throw new AssertionError(resource)
        }
    }

    private static File toClassPathRoot(File resourceFile, String resourcePath) {
        File current = resourceFile.getParentFile()
        for (int start = resourcePath.indexOf('/'); start >= 0; start = resourcePath.indexOf('/', start + 1)) {
            current = current.getParentFile()
            if (current == null || current.isDirectory() == false) {
                throw new AssertionError(resourceFile)
            }
        }
        return current
    }

    private static File toClassPathRoot(String uriQualifiedPath, String resourceName) {
        int entry = uriQualifiedPath.lastIndexOf('!')
        String qualifier
        if (entry >= 0) {
            qualifier = uriQualifiedPath.substring(0, entry)
        } else {
            qualifier = uriQualifiedPath
        }
        URI archive = new URI(qualifier)
        if (archive.getScheme().equals('file') == false) {
            throw new AssertionError(archive)
        }
        return new File(archive)
    }
}
