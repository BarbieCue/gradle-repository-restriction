import java.net.URI

group = "org.example.distribution"
version = "0.0.1"

val gradleBaseVersion = "8.7"
val gradleBase = "gradle-$gradleBaseVersion"

plugins {
    kotlin("jvm") version "1.9.22"
    distribution
    `maven-publish`
}

repositories {
    mavenLocal()
    myCustomRepo()
}

publishing {
    repositories.myCustomRepo()
    publications {
        create<MavenPublication>("myCustomDistribution") {
            artifactId = gradleBase
            artifact(tasks.distZip)
        }
    }
}


// -- Build the custom gradle distribution --
val buildDirectory: File = layout.buildDirectory.asFile.get()

// 1. Download official gradle distribution and unzip it into the build directory
tasks.register("downloadAndUnzipGradleBaseDistribution") {
    buildDirectory.mkdir()
    val file = File("$buildDirectory/gradle-dist.zip")
    file.writeBytes(URI("https://services.gradle.org/distributions/$gradleBase-bin.zip").toURL().readBytes())
    copy {
        from(zipTree("$buildDirectory/gradle-dist.zip"))
        into(buildDirectory)
    }
    delete("$buildDirectory/gradle-dist.zip")
}

// 2. Take the unzipped contents of the downloaded gradle base distribution and add our custom init.d script
distributions {
    main {
        distributionBaseName.set("custom-distribution-$gradleBaseVersion")
        contents {
            into("/${gradleBase}") {
                from("$buildDirectory/$gradleBase")
            }
            into("/${gradleBase}/init.d") {
                from("$projectDir/src/main/resources/init.d")
            }
        }
    }
}

// 3. Run the distZip task to zip our custom distribution
tasks.distZip {
    dependsOn("downloadAndUnzipGradleBaseDistribution")
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
