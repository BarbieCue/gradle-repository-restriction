plugins {
    kotlin("jvm") version "1.9.22"
    `java-gradle-plugin`
}

repositories {
    mavenLocal()
    myCustomRepo()
}

gradlePlugin {
    plugins {
        create("BuildScriptRepositoriesAssertion") {
            id = "buildScriptRepositoriesAssertion"
            implementationClass = "org.example.BuildScriptRepositoriesAssertionPlugin"
        }
        create("DependencyResolutionManagementRepositoriesAssertionPlugin") {
            id = "dependencyResolutionManagementRepositoriesAssertionPlugin"
            implementationClass = "org.example.DependencyResolutionManagementRepositoriesAssertionPlugin"
        }
        create("PluginRepositoriesAssertionPlugin") {
            id = "pluginRepositoriesAssertionPlugin"
            implementationClass = "org.example.PluginRepositoriesAssertionPlugin"
        }
        create("ProjectRepositoriesAssertionPlugin") {
            id = "projectRepositoriesAssertionPlugin"
            implementationClass = "org.example.ProjectRepositoriesAssertionPlugin"
        }
        create("PublishingRepositoriesAssertionPlugin") {
            id = "publishingRepositoriesAssertionPlugin"
            implementationClass = "org.example.PublishingRepositoriesAssertionPlugin"
        }
    }
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

fun RepositoryHandler.myCustomRepo(): MavenArtifactRepository =
    maven {
        val artifactoryAccessToken = System.getProperty("gradle.wrapperPassword")
        url = uri("https://repo.maven.apache.org/maven2/") // url of my custom repo
        name = "MyCustomRepo"
        credentials(HttpHeaderCredentials::class) {
            name = "Authorization"
            value = "Bearer $artifactoryAccessToken"
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
