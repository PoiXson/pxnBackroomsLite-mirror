package com.poixson.backroomslite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

import com.poixson.commonmc.tools.plugin.xJavaPlugin;


public class BackroomsLitePlugin extends xJavaPlugin {
	@Override public int getSpigotPluginID() { return 108409; }
	@Override public int getBStatsID() {       return 17876;  }
	public static final String LOG_PREFIX  = "[pxnBackroomsLite] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;

	protected static final String GENERATOR_NAME = "BackroomsLite";
	protected static final String DEFAULT_RESOURCE_PACK = "https://dl.poixson.com/mcplugins/pxnBackrooms/pxnBackrooms-resourcepack-{VERSION}.zip";

	protected final Level0Generator generator;



	public BackroomsLitePlugin() {
		super(BackroomsLitePlugin.class);
		this.generator = new Level0Generator();
	}



	@Override
	public void onEnable() {
		super.onEnable();
		this.generator.loadConfig(this.config.get().getConfigurationSection("Level0.Blocks"));
		// resource pack
		{
			final String pack = Bukkit.getResourcePack();
			if (pack == null || pack.isEmpty()) {
				LOG.warning(String.format(
					"%sResource pack not set; You can use this one: %s",
					LOG_PREFIX,
					DEFAULT_RESOURCE_PACK.replace("{VERSION}", this.getPluginVersion())
				));
			} else {
				LOG.info(String.format(
					"%sUsing resource pack: %s",
					LOG_PREFIX,
					Bukkit.getResourcePack()
				));
			}
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		LOG.info(String.format("%s%s world: %s", LOG_PREFIX, GENERATOR_NAME, worldName));
		return this.generator;
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfigs() {
		this.mkPluginDir();
		final FileConfiguration cfg = this.getConfig();
		this.config.set(cfg);
		this.configDefaults(cfg);
		cfg.options().copyDefaults(true);
		super.saveConfig();
	}
	@Override
	protected void saveConfigs() {
		super.saveConfig();
	}
	@Override
	protected void configDefaults(final FileConfiguration cfg) {
		Level0Generator.ConfigDefaults(cfg); // lobby
	}



}
