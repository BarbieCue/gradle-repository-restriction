import org.example.RepositoryRestrictionPlugin

initscript {
    repositories {
        maven {
            val artifactoryAccessToken = System.getProperty("gradle.wrapperPassword")
            url = uri("https://repo.maven.apache.org/maven2/") // url of my custom repo
            name = "MyCustomRepo"
            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "Bearer $artifactoryAccessToken"
            }
            authentication { create<HttpHeaderAuthentication>("header") }
        }
        mavenLocal()
    }
    dependencies {
        classpath("org.example.distribution:plugins:0.0.1")
    }
}

apply<RepositoryRestrictionPlugin>()
