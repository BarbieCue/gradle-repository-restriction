# Plugins that are applied by the distribution


## What happens in this subproject?

The repository restricting Gradle plugin is implemented here.
The _init.d_ script implemented in the _distribution_ subproject
depends on this plugin by expecting that the plugin is published 
to MavenLocal or MyCustomRepo and can be retrieved from there.


## Relevant tasks


```shell
# Remove old artifacts, if there are any
../gradlew -p ../ ":plugins:clean"
```

```shell
# Test the plugins
../gradlew -p ../ ":plugins:test"
```

```shell
# Publish the plugins
../gradlew -p ../ ":plugins:publish"
```

```shell
# Or publish the plugins to your local maven repository for local testing
../gradlew -p ../ ":plugins:publishToMavenLocal"
```
