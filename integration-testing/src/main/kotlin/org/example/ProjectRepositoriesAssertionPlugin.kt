package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

class ProjectRepositoriesAssertionPlugin : Plugin<Project> {

    override fun apply(project: Project) =
        project.afterEvaluate {
            assertRepositories(it.projectMavenRepositories(), "project")
            println("Test Code: Project repository assertion plugin has been applied")
        }

    private fun Project.projectMavenRepositories() =
        repositories.filterIsInstance<MavenArtifactRepository>()
}