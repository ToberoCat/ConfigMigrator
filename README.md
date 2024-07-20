# ConfigMigrator

ConfigMigrator is a tool designed to help in the process of updating plugin configuration files for Bukkit plugins. It
ensures your configuration files are always up-to-date by handling version checks, backups, and migration processes.

![GitHub release (latest by date)](https://img.shields.io/github/v/release/toberocat/ConfigMigrator)
![GitHub issues](https://img.shields.io/github/issues/toberocat/ConfigMigrator)
![GitHub license](https://img.shields.io/github/license/toberocat/ConfigMigrator)
[![](https://jitpack.io/v/ToberoCat/ConfigMigrator.svg)](https://jitpack.io/#ToberoCat/ConfigMigrator)

## Table of Contents

- [Features](#features)
- [Installation](#installation)
    - [Gradle](#gradle)
    - [Maven](#maven)
- [Usage](#usage)
- [How it works](#how-it-works)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

## Features

- Automatically checks and updates configuration files.
- Creates backups of existing configurations before migration.
- Preserves existing configuration values during updates.
- Comments in config will be copied to the new file.
- Supports all YAML features, including lists, maps, and nested structures.

## Installation

ConfigMigrator is distributed via JitPack. To include it in your project, add the following to your build configuration:

### Gradle

1. Add the JitPack repository to your `build.gradle` file:

    ```groovy
    repositories {
        maven { url 'https://jitpack.io' }
    }
    ```

2. Add the dependency:

    ```groovy
    dependencies {
        implementation 'com.github.ToberoCat:ConfigMigrator:Tag'
    }
    ```

   Replace **`Tag`** with the version you want to use.

### Maven

1. Add the JitPack repository to your `pom.xml` file:

    ```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    ```

2. Add the dependency:

    ```xml
    <dependency>
        <groupId>com.github.ToberoCat</groupId>
        <artifactId>ConfigMigrator</artifactId>
        <version>Tag</version>
    </dependency>
    ```

   Replace **`Tag`** with the version you want to use.

## Usage

To use ConfigMigrator in your Bukkit plugin, follow these steps:

1. Ensure you have a `config.yml` file in your plugin's resources.

2. Create an instance of `ConfigMigrator` and initiate the migration process in your plugin's main class:

    ```java
    import io.github.toberocat.configmigrator.ConfigMigrator;
    import org.bukkit.plugin.java.JavaPlugin;

    public class MyPlugin extends JavaPlugin {
        @Override
        public void onEnable() {
            ConfigMigrator migrator = new ConfigMigrator(this);
            migrator.migrate();
        }
    }
    ```

## How it works

ConfigMigrator will take the config.yml in your plugin resources and will expect it to be the latest version. For
ConfigMigrator to work properly, the config.yml must have the property `config-version` (By default). This will be used
to check if a config file is outdated.

Always make sure to increase the config-version in the config.yml located in the resources folder when the config
changed.

Example `config.yml`:

```yaml
config-version: 1
setting1: value1
setting2: value2
```

## Documentation

### Class: `ConfigMigrator`

#### Properties

- `plugin: JavaPlugin`: The JavaPlugin instance for which the configuration is being migrated.
- `configVersionPath: String`: The path within the configuration file where the version number is stored. Default
  is `"config-version"`.
- `backupPath: String`: The directory path where backups of the configuration are stored before migration. Default
  is `"backups/configs"`.
- `makeBackup: Boolean`: Determines whether a backup of the configuration should be made before migration. Default
  is `true`.

#### Methods

- `boolean migrate()`: Initiates the migration process if necessary. Returns `true` if a migration occurred, `false`
  otherwise.
- `int getLatestVersion()`: Retrieves the latest configuration version number from the latest configuration file.
- `int getInstalledVersion()`: Retrieves the currently installed configuration version number from the plugin's
  configuration.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an Issue on GitHub.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

Thanks to all the developers who contribute to open-source projects.