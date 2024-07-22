# Gradle repository restriction

Do you want to make sure that your Gradle based projects can
download dependencies only via your secured custom in-house Maven repository?

This github repository shows an example implementation of a 
Gradle distribution that enforces this restriction.
You just need to make sure that your Gradle based projects 
are using this distribution.

<img src="https://img.shields.io/badge/Gradle-8.9-blue"  alt="Gradle-8.9"/>


## This applies to projects that use the custom Gradle distribution

Each project is restricted to these repositories

- _My custom repository (your secured in-house maven repository)_
- _Maven local_

Repositories that refer to other locations are automatically removed.


### _Example_

_In my Java project, in which I use this custom Gradle distribution,
I don't have to add any repositories myself.
In fact, I am not allowed to use any other repositories.
If I nevertheless specify my own repositories, these are automatically removed.
With this behavior I cannot accidentally by-pass the in-house repository._

```shell
# my-java-project
# └── build.gradle.kts

dependencies {
  implementation("org.apache.commons:commons-math:2.0") # Works out of the box, resolved by our in-house repository or maven local.
}

# No need for a repositories { .. } block.
```

_When I activate the 'maven-publish' plugin in order to publish something from my 
java project into our in-house repository,
I don't need to specify it as publishing repository.
Because it is already specified by this custom Gradle distribution._

```shell
# my-java-project
# └── build.gradle.kts

plugins {
  `maven-publish` # Activating the plugin. Now, I can publish in our in-house maven repository or maven local.
}

# No need for a publishing.repositories { .. } block.
```


## How to use this distribution in your projects

Two simple steps to use this custom Gradle distribution in your Gradle based projects.


### 1. Edit your personal `gradle.properties` file

When retrieving dependencies from a custom repository,
a token is usually used for authentication.
This token is in most cases available for Gradle as a _user_ property
called like `myRepoAccessToken` in `~/.gradle/gradle.properties`.

However, since we want to get the Gradle distribution itself from our secured custom repository,
we also have to add [two _system_ properties](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:authenticated_download).

Therefore, make sure that you have defined the following properties.

```properties
# ~/.gradle/gradle.properties
myRepoAccessToken=<my custom repo token>
systemProp.gradle.wrapperUser=<my custom repo token>
systemProp.gradle.wrapperPassword=<my custom repo token>
```

### 2. Use the custom Gradle distribution in your projects

Reference this custom distribution in your Gradle based projects
(java, kotlin, whatever).

```properties
# my-java-project
# └── gradle
#     └── wrapper
#         └── gradle-wrapper.properties
distributionUrl=https://<url of my custom repo>/org/example/distribution/custom-gradle-distribution-8.9/0.0.1/custom-gradle-distribution-8.9-0.0.1.zip
```


## Your in-house Maven repository

Search all places in the code of this repository, that are commented with
`url of my custom repo` and replace that url with _your_ in-house repository url.
Currently, Maven Central is used here as an example.


## How to build and publish this distribution

Do the following to build, test and publish this distribution 
and the associated repository restriction plugin in your in-house repository.
These are the steps that you usually integrate into your CI/CD pipeline.
Do not forget to define the `gradle.properties` file for your pipeline runner
as described in one of the previous sections.


### Build and Test

1. Test the repository restriction plugin.
```shell
./gradlew ":plugins:test"
```

2. Publish the repository restriction plugin _locally_
   to make it available for the distribution testing in step 4.
```shell
./gradlew ":plugins:publishToMavenLocal"
```

3. Build the distribution artifact (the zip file).
```shell
./gradlew ":distribution:distZip"
```

4. Test the distribution. This step runs Gradle builds in unit tests.
```shell
./gradlew ":integration-test:test"
```


### Publish

Publish the artifacts if all tests are successful.

5. Publish the distribution.
```shell
./gradlew ":distribution:publish"
```

6. Publish the repository restriction plugin.
```shell
./gradlew ":plugins:publish"
```


## How to update the underlying Gradle base version

Only the `gradleBaseVersion` variable in 
[distribution/build.gradle.kts](distribution/build.gradle.kts)
has to be increased.
Then simply build again using the steps from the last section or
from [integration-testing/README.md](integration-testing/README.md).


## Versioning

As indicated in the filename, there are two interesting versions.
The official Gradle version of the underlying Gradle distribution and our custom version.
The combination of both versions is our custom Gradle distribution's version.

```
custom-gradle-distribution-8.9-0.0.1.zip
        |    |
 official    our internal
   gradle    versioning
  version
```
