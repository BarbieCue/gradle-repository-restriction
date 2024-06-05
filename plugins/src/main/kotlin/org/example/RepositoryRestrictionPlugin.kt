package org.example

import org.gradle.api.Plugin
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.UrlArtifactRepository
import org.gradle.api.credentials.HttpHeaderCredentials
import org.gradle.api.invocation.Gradle
import org.gradle.api.publish.PublishingExtension
import org.gradle.authentication.http.HttpHeaderAuthentication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.repositories
import java.net.URI

class RepositoryRestrictionPlugin : Plugin<Gradle> {

    // Allow only these repositories:
    // - MyCustomRepo
    // - MavenLocal

    override fun apply(gradle: Gradle) {

        fun UrlArtifactRepository.hasSameUrl(repo: UrlArtifactRepository) =
            url.host == repo.url.host &&
            url.scheme == repo.url.scheme &&
            url.path == repo.url.path
        fun MutableCollection<UrlArtifactRepository>.isUserDefinedRepo(repo: UrlArtifactRepository): Boolean = !contains(repo)
        fun MutableCollection<UrlArtifactRepository>.isDuplicate(repo: UrlArtifactRepository) = any { it.hasSameUrl(repo)}

        gradle.beforeSettings { settings ->

            // Specify plugin repositories

            settings.pluginManagement { pluginSpec ->

                val allowedPluginRepos = mutableListOf<UrlArtifactRepository>()

                pluginSpec.repositories { repoHandler ->

                    allowedPluginRepos.add(repoHandler.mavenLocal())
                    allowedPluginRepos.add(repoHandler.myCustomRepo())
                }

                pluginSpec.repositories { repoHandler ->
                    repoHandler.all { repo ->
                        if (repo is UrlArtifactRepository && allowedPluginRepos.isUserDefinedRepo(repo)) {
                            repoHandler.remove(repo)
                            if (allowedPluginRepos.isDuplicate(repo)) {
                                System.err.println("Removed plugin repository: ${repo.showInfo()} (duplicate)")
                            } else {
                                System.err.println("Removed plugin repository: ${repo.showInfo()} (not allowed)")
                            }
                        }
                    }
                }
            }

            // Specify project repositories

            settings.dependencyResolutionManagement {

                val allowedDrmRepos = mutableListOf<UrlArtifactRepository>()

                it.repositories { repoHandler ->

                    allowedDrmRepos.add(repoHandler.mavenLocal())
                    allowedDrmRepos.add(repoHandler.myCustomRepo())

                    repoHandler.all { repo ->
                        if (repo is UrlArtifactRepository && allowedDrmRepos.isUserDefinedRepo(repo)) {
                            repoHandler.remove(repo)
                            if (allowedDrmRepos.isDuplicate(repo)) {
                                System.err.println("Removed dependency resolution management repository: ${repo.showInfo()} (duplicate)")
                            } else {
                                System.err.println("Removed dependency resolution management repository: ${repo.showInfo()} (not allowed)")
                            }
                        }
                    }
                }
            }
        }

        var showInfo = true
        gradle.allprojects { project ->

            // Configure project repositories

            val allowedProjectRepos = mutableListOf<UrlArtifactRepository>()

            project.repositories {

                allowedProjectRepos.add(mavenLocal())
                allowedProjectRepos.add(myCustomRepo())

                if (showInfo && this.isNotEmpty()) {
                    val info = buildString {
                        appendLine("Using these repositories to resolve plugins, build script and project dependencies (and to publish maven artifacts, if you apply the maven-publish repository):")
                        this@repositories.forEach { repo -> appendLine("- ${repo.showInfo()}") }
                    }
                    println(info)
                    showInfo = false
                }

                all { repo ->
                    if (repo is UrlArtifactRepository && allowedProjectRepos.isUserDefinedRepo(repo)) {
                        remove(repo)
                        if (allowedProjectRepos.isDuplicate(repo)) {
                            System.err.println("Removed project repository: ${repo.showInfo()} (duplicate)")
                        } else {
                            System.err.println("Removed project repository: ${repo.showInfo()} (not allowed)")
                        }
                    }
                }
            }

            // Configure build script repositories

            val allowedBuildScriptRepos = mutableListOf<UrlArtifactRepository>()

            project.buildscript.repositories {

                allowedBuildScriptRepos.add(mavenLocal())
                allowedBuildScriptRepos.add(myCustomRepo())

                all { repo ->
                    if (repo is UrlArtifactRepository &&
                        (allowedBuildScriptRepos.isUserDefinedRepo(repo) ||
                                repo.name == "__plugin_repository__MyCustomRepo" ||
                                repo.name == "__plugin_repository__MavenLocal")
                    ) {
                        remove(repo)
                        if (allowedBuildScriptRepos.isDuplicate(repo)) {
                            System.err.println("Removed build script repository: ${repo.showInfo()} (duplicate)")
                        } else {
                            System.err.println("Removed build script repository: ${repo.showInfo()} (not allowed)")
                        }
                    }
                }
            }

            // When a project uses the maven-publish plugin, restrict the publishing repositories to my custom repository

            project.pluginManager.withPlugin("org.gradle.maven-publish") {
                project.configure<PublishingExtension> {

                        val allowedPublishingRepos = mutableListOf<UrlArtifactRepository>()

                        repositories {

                            allowedPublishingRepos.add(it.mavenLocal())
                            allowedPublishingRepos.add(it.myCustomRepo())

                            it.all { repo ->
                                if (repo is UrlArtifactRepository && allowedPublishingRepos.isUserDefinedRepo(repo)) {
                                    it.remove(repo)
                                    if (allowedPublishingRepos.isDuplicate(repo)) {
                                        System.err.println("Removed publishing repository: ${repo.showInfo()} (duplicate)")
                                    } else {
                                        System.err.println("Removed publishing repository: ${repo.showInfo()} (not allowed)")
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun RepositoryHandler.myCustomRepo(): MavenArtifactRepository =
        maven {
            val artifactoryAccessToken = System.getProperty("gradle.wrapperPassword")
            it.url = URI("https://repo.maven.apache.org/maven2/") // url of my custom repo
            it.name = "MyCustomRepo"
            it.credentials(HttpHeaderCredentials::class) { cred ->
                cred.name = "Authorization"
                cred.value = "Bearer $artifactoryAccessToken"
            }
            it.authentication { auth ->
                auth.create<HttpHeaderAuthentication>("header")
            }
        }

    private fun ArtifactRepository.showInfo(): String {
        return when (this) {
            is MavenArtifactRepository -> {
                "Name: $name; url: $url"
            }
            else -> "Name: $name"
        }
    }
}
