import io.github.toberocat.configmigrator.ConfigMigrator
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File
import java.nio.file.Files
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigMigratorTest {

    private lateinit var plugin: JavaPlugin
    private lateinit var config: FileConfiguration
    private lateinit var migrator: ConfigMigrator
    private lateinit var dataFolder: File

    @BeforeEach
    fun setUp() {
        plugin = Mockito.mock(JavaPlugin::class.java)
        config = Mockito.mock(FileConfiguration::class.java)
        dataFolder = Files.createTempDirectory("pluginDataFolder").toFile()

        val logger = Mockito.mock(Logger::class.java)

        File(dataFolder, "config.yml").writeText(
            """
            some-key: some-value
            config-version: 1
        """.trimIndent()
        )
        config = Mockito.spy(YamlConfiguration.loadConfiguration(File(dataFolder, "config.yml")))

        migrator = Mockito.spy(ConfigMigrator(plugin))
        Mockito.`when`(plugin.logger).thenReturn(logger)
        Mockito.`when`(plugin.dataFolder).thenReturn(dataFolder)
        Mockito.`when`(plugin.config).thenReturn(config)
        Mockito.`when`(plugin.reloadConfig()).thenAnswer { config.load(File(dataFolder, "config.yml")) }
        Mockito.`when`(plugin.saveConfig()).thenAnswer { config.save(File(dataFolder, "config.yml")) }
    }

    @Test
    fun migrationOccursWhenConfigIsOutdated() {
        Mockito.`when`(config.getInt("config-version", 0)).thenReturn(1)
        Mockito.doReturn(2).`when`(migrator).getLatestVersion()

        assertTrue(migrator.migrate(), "Expected migration to occur")
    }

    @Test
    fun noMigrationOccursWhenConfigIsUpToDate() {
        Mockito.`when`(config.getInt("config-version", 0)).thenReturn(2)

        Mockito.doReturn(2).`when`(migrator).getLatestVersion()

        assertFalse(migrator.migrate(), "Expected no migration to occur")
    }

    @Test
    fun backupIsCreatedDuringMigration() {
        Mockito.`when`(config.getInt("config-version", 0)).thenReturn(1)

        Mockito.doReturn(2).`when`(migrator).getLatestVersion()

        migrator.migrate()

        val backupFile = File(dataFolder, "backups/configs/config-1.yml")
        assertTrue(backupFile.exists(), "Expected backup file to exist")
    }

    @Test
    fun previousValuesAreRestoredAfterMigration() {
        assertTrue(migrator.migrate())

        assertTrue(config.getInt("config-version", 0) == 2)
        assertTrue(config.getString("some-key") == "some-value")
        assertEquals(config.getStringList("list-entry"), listOf("first", "second", "third"))

        assertFalse(migrator.migrate())
    }
}