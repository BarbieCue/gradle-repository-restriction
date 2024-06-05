package org.example

import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.junit.jupiter.api.Assertions.*

fun assertRepositories(mavenArtifactRepositories: List<MavenArtifactRepository>, type: String) {

    fun assertRepositoryExists(repoName: String, repositoryAssertion: (MavenArtifactRepository) -> Unit) {
        val repository = mavenArtifactRepositories.firstOrNull { repo -> repo.name == repoName }
        assertTrue(repository != null, "$type repository named '$repoName' exists.")
        repositoryAssertion(repository!!)
    }

    fun assertMavenLocalExists() = assertRepositoryExists("MavenLocal") { repo: MavenArtifactRepository ->
        val repositoryUrl = repo.url
        assertEquals("file", repositoryUrl.scheme, "$type repository[MavenLocal].url.scheme")
        assertNull(repositoryUrl.host, "$type repository[MavenLocal].url.host")
        println("Test Code: MavenLocal found")
    }

    fun assertMyCustomRepoExists() = assertRepositoryExists("MyCustomRepo") { repo: MavenArtifactRepository ->
        val repositoryUrl = repo.url
        assertEquals("https", repositoryUrl.scheme, "$type repository[MyCustomRepo].url.scheme")
        assertEquals("repo.maven.apache.org", repositoryUrl.host, "$type repository[MyCustomRepo].url.host")
        println("Test Code: MyCustomRepo found")
    }

    assertEquals(2, mavenArtifactRepositories.size, "$type repositories")
    assertMavenLocalExists()
    assertMyCustomRepoExists()
}