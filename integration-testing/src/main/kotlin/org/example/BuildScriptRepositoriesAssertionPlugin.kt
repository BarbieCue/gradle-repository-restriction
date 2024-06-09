package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class BuildScriptRepositoriesAssertionPlugin : Plugin<Project> {

    override fun apply(project: Project) =
        project.afterEvaluate {
            assertRepositories(it.buildScriptMavenRepositories(), "build script")
            println("Test Code: Build script repository assertion plugin has been applied")
        }

    private fun Project.buildScriptMavenRepositories() =
        buildscript.repositories.filterIsInstance<MavenArtifactRepository>()
}