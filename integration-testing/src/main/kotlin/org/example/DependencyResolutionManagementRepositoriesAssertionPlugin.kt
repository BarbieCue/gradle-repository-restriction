package org.example

import org.gradle.api.Plugin
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.Settings

class DependencyResolutionManagementRepositoriesAssertionPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) =
        settings.gradle.settingsEvaluated {
            assertRepositories(it.dependencyResolutionManagementMavenRepositories(), "dependency resolution management")
            println("Test Code: Dependency resolution management assertion plugin has been applied")
        }

    private fun Settings.dependencyResolutionManagementMavenRepositories() =
        dependencyResolutionManagement.repositories.filterIsInstance<MavenArtifactRepository>()
}