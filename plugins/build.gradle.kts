plugins {
    kotlin("jvm") version "1.9.22"
    `maven-publish`
    `java-gradle-plugin`
}

group = "org.example.distribution"
version = "0.0.1"

repositories {
    mavenLocal()
    myCustomRepo()
}

publishing {
    repositories {
        myCustomRepo()
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
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
