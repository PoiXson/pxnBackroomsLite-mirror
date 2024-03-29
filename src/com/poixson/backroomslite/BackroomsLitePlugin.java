package com.poixson.backroomslite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

import com.poixson.tools.xJavaPlugin;


public class BackroomsLitePlugin extends xJavaPlugin {
	@Override public int getSpigotPluginID() { return 108409; }
	@Override public int getBStatsID() {       return 17876;  }
	public static final String CHAT_PREFIX = ChatColor.AQUA+"[Backrooms] "+ChatColor.WHITE;

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
				this.log().warning("Resource pack not set; You can use this one: " +
					DEFAULT_RESOURCE_PACK.replace("{VERSION}", this.getPluginVersion()));
			} else {
				this.log().info("Using resource pack: "+Bukkit.getResourcePack());
			}
		}
		this.saveConfigs();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		this.log().info(String.format("%s world: %s", GENERATOR_NAME, worldName));
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
