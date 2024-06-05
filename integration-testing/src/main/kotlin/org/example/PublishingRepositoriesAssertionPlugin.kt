package org.example

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension

class PublishingRepositoriesAssertionPlugin : Plugin<Project> {

    override fun apply(project: Project) =
        project.afterEvaluate {
            assertRepositories(it.publishingRepositories(), "maven publish")
            println("Test Code: Publishing repositories assertion plugin has been applied")
        }

    private fun Project.publishingRepositories() =
        extensions.getByType(PublishingExtension::class.java)
            .repositories.filterIsInstance<MavenArtifactRepository>()
}