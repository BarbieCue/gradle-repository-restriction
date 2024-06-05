package org.example

import org.gradle.api.Plugin
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.Settings

class PluginRepositoriesAssertionPlugin : Plugin<Settings> {

    override fun apply(settings: Settings) =
        settings.gradle.settingsEvaluated {
            assertRepositories(it.pluginMavenRepositories(), "plugin")
            println("Test Code: Plugin repository assertion plugin has been applied")
        }

    private fun Settings.pluginMavenRepositories() =
        pluginManagement.repositories.filterIsInstance<MavenArtifactRepository>()
}