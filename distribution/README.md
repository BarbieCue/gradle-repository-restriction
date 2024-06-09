# The custom distribution


## What happens in this subproject?

We build the custom distribution here by applying the following steps.

1. Download an official Gradle distribution (.zip file)
2. Unzip it
3. Add an _init.d_ script
4. Zip it again (the resulting zip file is the custom distribution)
5. Publish the zip file in the custom repository


## Depends on artifacts from the _distribution-plugins_ subproject
 
The init script(s) depend on the plugins that are implemented in the `plugins` subproject.
That dependency is an _artifact_ based one,
which means that the plugins must have been published in the custom repository so 
that init scripts can load them from the repository.


## Relevant tasks


```shell
# Remove old artifacts, if there are any
../gradlew -p ../ ":distribution:clean"
```

```shell
# Build the distribution. Results in a new zip file stored at distributions/build/distributions/
../gradlew -p ../ ":distribution:distZip"
```

```shell
# Publish the custom distribution
../gradlew -p ../ ":distribution:publish"
```

```shell
# Or if you are currently developing this repository:
# publish the custom distribution to your local maven repository
../gradlew -p ../ ":distribution:publishToMavenLocal"
```
