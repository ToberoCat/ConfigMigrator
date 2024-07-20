package io.github.toberocat.configmigrator

import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * This class is responsible for managing the migration of plugin configuration files to ensure they are up-to-date.
 * It checks the current configuration version against the latest version and performs the necessary updates.
 *
 * @property plugin The JavaPlugin instance for which the configuration is being migrated.
 * @property configVersionPath The path within the configuration file where the version number is stored.
 * @property backupPath The directory path where backups of the configuration are stored before migration.
 * @property makeBackup Determines whether a backup of the configuration should be made before migration.
 */
data class ConfigMigrator(
    private val plugin: JavaPlugin,
    private val configVersionPath: String = "config-version",
    private val backupPath: String = "backups/configs",
    private val makeBackup: Boolean = true
) {

    /**
     * Initiates the migration process if necessary. It checks if the current configuration version is outdated,
     * and if so, performs a migration by updating the configuration to the latest version.
     *
     * @return True, if a migration occurred, false otherwise.
     */
    fun migrate(): Boolean {
        val configFileVersion = getInstalledVersion()
        val latestConfigVersion = getLatestVersion()

        if (configFileVersion == latestConfigVersion) {
            plugin.logger.info("Config is up to date")
            return false
        }

        plugin.logger.info("Config is outdated $configFileVersion -> $latestConfigVersion. Updating...")
        makeConfigBackup(configFileVersion)

        val previousValueTree = generatePreviousValueTree()
        overrideOldConfig()
        plugin.reloadConfig()

        updateConfig(previousValueTree)
        plugin.saveConfig()

        plugin.logger.info("Config updated successfully")

        return true
    }

    /**
     * Retrieves the latest configuration version number from the latest configuration file.
     *
     * @return The latest configuration version number as an Int.
     */
    fun getLatestVersion() = getLatestConfig().getInt(configVersionPath, 0)

    /**
     * Retrieves the currently installed configuration version number from the plugin's configuration.
     *
     * @return The installed configuration version number as an Int.
     */
    fun getInstalledVersion() = plugin.config.getInt(configVersionPath, 0)

    /**
     * Creates a backup of the current configuration file, naming it according to the current configuration version.
     * The backup is stored in the specified backup directory.
     *
     * @param configFileVersion The version number of the configuration file being backed up.
     */
    private fun makeConfigBackup(configFileVersion: Int) {
        if (!makeBackup) return

        val configBackups = File(plugin.dataFolder, backupPath)
        if (!configBackups.exists()) {
            configBackups.mkdirs()
        }

        val source = File(plugin.dataFolder, "config.yml").toPath()
        val target = File(configBackups, "config-${configFileVersion}.yml").toPath()
        if (!Files.exists(source) || Files.isDirectory(source) || Files.isDirectory(target)) {
            plugin.logger.warning("Failed to create a backup of the old config")
            return
        }

        Files.copy(
            source,
            target,
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    /**
     * Generates a map of the previous configuration values, excluding the version and any MemorySection objects.
     * This is used to restore configuration values after updating the configuration file.
     *
     * @return A Map of the previous configuration values.
     */
    private fun generatePreviousValueTree() = plugin.config.getValues(true)
        .filter { it.value !is MemorySection }
        .filter { it.key != configVersionPath }

    /**
     * Updates the current configuration with the values from the previous configuration.
     * This method is called after the configuration file has been updated to the latest version.
     *
     * @param previousValueTree A Map containing the previous configuration values.
     */
    private fun updateConfig(previousValueTree: Map<String, Any>) {
        previousValueTree.forEach { plugin.config.set(it.key, it.value) }
        plugin.logger.info("${previousValueTree.size} values have been restored from the old config.")
    }

    /**
     * Overrides the old configuration file with the latest one from the resources.
     * This is done by copying the latest configuration file to the plugin's data folder.
     */
    private fun overrideOldConfig() {
        val destination = File(plugin.dataFolder, "config.yml").toPath()
        getLatestConfigStream().use {
            Files.copy(it, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    /**
     * Loads the latest configuration from the resources as a YamlConfiguration.
     * This is used to get the latest configuration version and to override the old configuration.
     *
     * @return The latest YamlConfiguration.
     */
    private fun getLatestConfig() = getLatestConfigStream()
        .use { YamlConfiguration.loadConfiguration(it.bufferedReader()) }

    /**
     * Retrieves an InputStream for the latest configuration file from the resources.
     * This stream is used to load the latest configuration.
     *
     * @return An InputStream for the latest configuration file.
     * @throws IllegalStateException If the configuration file is not found in the resources.
     */
    private fun getLatestConfigStream() = this::class.java.getResourceAsStream("/config.yml")
        ?: throw IllegalStateException("Config not found")
}