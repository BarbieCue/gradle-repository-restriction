# Custom distribution integration tests


## What happens in this subproject?

Integration tests for the custom distribution are implemented here.


## Depends on the zip artifact from the _distribution_ subproject

After the custom distribution has been built in the subproject _distribution_,
it is available as a zip file in this location: `distribution/build/distributions/custom-distribution-X.X-Y.Y.Y.zip`.
Changing this location would break tests.

Because _distribution_ is a sibling project of _integration-testing_,
tests need to navigate to the zip file via the parent directory.
For example: `../distribution/build/distributions/custom-distribution-X.X-Y.Y.Y.zip`


## Testing is a bit special here

When you want to test a Gradle plugin (the heart of our custom distribution),
you have to execute a Gradle build and Gradle provides the _Gradle TestKit_ for this purpose.
The Gradle TestKit creates a Gradle instance executing the build.

With the Gradle TestKit, your assertions can only access the build output and task results.
Gradle's internal state at build runtime cannot be accessed.

Consequence: for plugins configuring the Gradle build itself (e.g. changing state in the runtime model),
you have no chance to make assertions using the build result of the TestKit's Gradle runner.

Take a look at this example provided by the TestKit documentation.

```kotlin
val buildResult = GradleRunner
    .create()
    .withProjectDir(testProjectDir)
    .withArguments("...")
    .build()
assertTrue(result.getOutput().contains("Hello world!"));
assertEquals(SUCCESS, result.task(":helloWorld").getOutcome());
```
As you can see you can assert that the output contains some string.
But this means that you only test that the string was written to the output.
It does not test that the plugin actually does something useful.

### So how can we test the internals?

Since Gradle plugins have access to the runtime model, we can place our test code in plugins.
Assertion plugins are just normal Gradle plugins that make assertions and cause the build
to fail if these assertions are not met.

Since the Gradle TestKit is usually used to test plugins,
it generates plugin metadata under `build/pluginUnderTestMetadata/plugin-under-test-metadata.properties`.
This means that if you create a plugin in `src/main/kotlin` and register it in the `build.gradle.kts`
the metadata properties are generated and the _Gradle runner_ can find these plugins.
```kotlin
gradlePlugin {
   plugins {
      create("AssertionsPlugin") {
         id = "assertionsPlugin"
         implementationClass = "org.example.AssertionsPlugin"
      }
   }
}
```

This is the way we make our assertion plugins available to the _Gradle runner_.

As a result the assertion plugins must be placed under `src/main` and not `src/test`as you might expect.
And as a consequence of this we need *JUnit* at compile time (`implementation`)
and not only on at test runtime (`testImplementation`).


## Relevant tasks


```shell
# Run the integration tests
# Depends on the fact that the distribution was built.
# Which in turn depends on the plugins having been published (remote or locally).
../gradlew -p ../ ":integration-test:test"
```

**But wait, can I run the tests now?** 

If you are not sure whether all the preparatory steps for the tests have been completed,
run them again first and then start testing.

_1. Kill running Gradle daemons to avoid caching problems_

```shell
pkill -f '.*GradleDaemon.*'
```

_2. Remove old versions of the custom distribution_

```shell
rm ~/.gradle/wrapper/dists/*custom-distribution* -rf
```

_3. Remove old build artifacts, if there are any_

```shell
../gradlew -p ../ ":distribution:clean" ":plugins:clean" ":integration-test:clean"
```

_4. Publish the plugins to your local maven repository_

```shell
../gradlew -p ../ ":plugins:publishToMavenLocal"
```

_5. Build the distribution zip file_

```shell
../gradlew -p ../ ":distribution:distZip"
```

## How can I test the distribution locally and manually in a real consumer project?

1. Build the distribution zip file and publish the distribution plugins to your local maven 
   repository by running the preparatory steps from the section above
2. Copy and paste the distribution zip file into the consumer projects Gradle wrapper directory
   For example `my-java-project/gradle/wrapper/custom-distribution-8.6-0.0.1.zip`
3. Set the custom distribution for the Gradle wrapper
    ```properties
    # my-java-project
    # └── gradle
    #     └── wrapper
    #         └── gradle-wrapper.properties
    distributionUrl=custom-distribution-8.6-0.0.1.zip
   ```