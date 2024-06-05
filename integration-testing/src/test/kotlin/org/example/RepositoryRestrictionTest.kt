package org.example

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.extension


class RepositoryRestrictionTest {

    companion object {

        private const val URL_OF_MY_CUSTOM_REPO = "https://repo.maven.apache.org/maven2/" // url of my custom repo
        private var pathToCustomDistributionZip: URI? = null

        @BeforeAll
        @JvmStatic
        fun getCustomDistribution() {
            Files.walk(Paths.get("../distribution/build/distributions/")).use { paths ->
                pathToCustomDistributionZip = paths
                    .filter(Files::isRegularFile)
                    .filter { it.fileName.extension == "zip" }
                    .toList().first().toUri()
            }
        }
    }

    @TempDir
    var testProjectDir: File? = null
    private var buildFile: File? = null
    private var settingsFile: File? = null

    private fun buildWithGradle(): BuildResult =
        GradleRunner
            .create()
            .withGradleDistribution(pathToCustomDistributionZip)
            .withProjectDir(testProjectDir)
            .withPluginClasspath() // makes the assertion plugins available for the gradle runner
            .forwardOutput()
            .build()

    @BeforeEach
    fun setup() {
        File(testProjectDir, "gradle.properties")
        buildFile = File(testProjectDir, "build.gradle.kts")
        settingsFile = File(testProjectDir, "settings.gradle")
    }

    @Test
    fun `plugin repositories - set MavenLocal and MyCustomRepo by default`() {
        settingsFile.appendInNewLine("""
            plugins {
                id("pluginRepositoriesAssertionPlugin")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Plugin repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
    }

    @Test
    fun `plugin repositories - remove user defined repositories`() {
        settingsFile.write("""
            pluginManagement {
                repositories {
                    maven {
                        url = uri("https://some.where.else") // forbidden, should be removed
                    }
                }
            }
            """.trimIndent())

        settingsFile.appendInNewLine("""
            plugins {
                id("pluginRepositoriesAssertionPlugin")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Plugin repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed plugin repository: Name: maven; url: https://some.where.else (not allowed)"))
    }

    @Test
    fun `plugin repositories - remove duplicated repositories`() {
        settingsFile.write("""
            pluginManagement {
                repositories {
                    maven {
                        url = uri("$URL_OF_MY_CUSTOM_REPO")
                    }
                }
            }
            """.trimIndent())

        settingsFile.appendInNewLine("""
            plugins {
                id("pluginRepositoriesAssertionPlugin")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Plugin repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed plugin repository: Name: maven; url: $URL_OF_MY_CUSTOM_REPO (duplicate)"))
    }

    @Test
    fun `project repositories - set MavenLocal and MyCustomRepo by default`() {
        buildFile.appendInNewLine("""
            plugins {
                id("projectRepositoriesAssertionPlugin")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Project repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
    }

    @Test
    fun `project repositories - remove user defined repositories`() {
        buildFile.write("""
            plugins {
                id("projectRepositoriesAssertionPlugin")
            }
            repositories {
                maven {
                    url = uri("https://some.where.else") // forbidden, should be removed
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Project repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed project repository: Name: maven; url: https://some.where.else (not allowed)"))
    }

    @Test
    fun `project repositories - remove duplicated repositories`() {
        buildFile.write("""
            plugins {
                id("projectRepositoriesAssertionPlugin")
            }
            repositories {
                repositories {
                    maven {
                        url = uri("https://repo.maven.apache.org/maven2/")
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Project repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed project repository: Name: maven; url: https://repo.maven.apache.org/maven2/ (duplicate)"))
    }

    @Test
    fun `build script repositories - set MavenLocal and MyCustomRepo by default`() {
        buildFile.appendInNewLine("""
            plugins {
                id("buildScriptRepositoriesAssertion")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Build script repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
    }

    @Test
    fun `build script repositories - remove user defined repositories`() {
        buildFile.write("""
            plugins {
                id("buildScriptRepositoriesAssertion")
            }
            buildscript {
                repositories {
                    maven {
                        url = uri("https://some.where.else") // forbidden, should be removed
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Build script repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed build script repository: Name: maven; url: https://some.where.else (not allowed)"))
    }

    @Test
    fun `build script repositories - remove duplicated repositories`() {
        buildFile.write("""
            plugins {
                id("buildScriptRepositoriesAssertion")
            }
            buildscript {
                repositories {
                    maven {
                        url = uri("$URL_OF_MY_CUSTOM_REPO")
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Build script repository assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed build script repository: Name: maven; url: $URL_OF_MY_CUSTOM_REPO (duplicate)"))
    }

    @Test
    fun `dependency resolution management repositories - set MavenLocal and MyCustomRepo by default`() {
        settingsFile.appendInNewLine("""
            plugins {
                id("dependencyResolutionManagementRepositoriesAssertionPlugin")
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Dependency resolution management assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
    }

    @Test
    fun `dependency resolution management repositories - remove user defined repositories`() {
        settingsFile.write("""
            plugins {
                id("dependencyResolutionManagementRepositoriesAssertionPlugin")
            }
            dependencyResolutionManagement {
                repositories {
                    maven {
                        url = uri("https://some.where.else") // forbidden, should be removed
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Dependency resolution management assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed dependency resolution management repository: Name: maven; url: https://some.where.else (not allowed)"))
    }

    @Test
    fun `dependency resolution management repositories - remove duplicated repositories`() {
        settingsFile.write("""
            plugins {
                id("dependencyResolutionManagementRepositoriesAssertionPlugin")
            }
            dependencyResolutionManagement {
                repositories {
                    maven {
                        url = uri("$URL_OF_MY_CUSTOM_REPO")
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Dependency resolution management assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed dependency resolution management repository: Name: maven; url: $URL_OF_MY_CUSTOM_REPO (duplicate)"))
    }

    @Test
    fun `publishing repositories - set MavenLocal and MyCustomRepo by default`() {
        buildFile.appendInNewLine("""
            plugins {
                id("publishingRepositoriesAssertionPlugin")
                `maven-publish` // the user only needs to apply this plugin
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Publishing repositories assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
    }

    @Test
    fun `publishing repositories - remove user defined repositories`() {
        buildFile.write("""
            plugins {
                id("publishingRepositoriesAssertionPlugin")
                `maven-publish`
            }
            publishing {
                repositories {
                    maven {
                        url = uri("https://some.where.else") // forbidden, should be removed
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Publishing repositories assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed publishing repository: Name: maven; url: https://some.where.else (not allowed)"))
    }

    @Test
    fun `publishing repositories - remove duplicated repositories`() {
        buildFile.write("""
            plugins {
                id("publishingRepositoriesAssertionPlugin")
                `maven-publish`
            }
            publishing {
                repositories {
                    maven {
                        url = uri("$URL_OF_MY_CUSTOM_REPO")
                    }
                }
            }
            """.trimIndent()
        )
        val buildResult = buildWithGradle()
        assertTrue(buildResult.output.contains("Test Code: Publishing repositories assertion plugin has been applied"))
        assertTrue(buildResult.output.contains("Test Code: MavenLocal found"))
        assertTrue(buildResult.output.contains("Test Code: MyCustomRepo found"))
        assertTrue(buildResult.output.contains("Removed publishing repository: Name: maven; url: $URL_OF_MY_CUSTOM_REPO (duplicate)"))
    }

    private fun File?.write(content: String) = this!!.writeText(content)
    private fun File?.appendInNewLine(content: String) {
        this!!.appendText("\n")
        appendText(content)
    }
}
