package com.poixson.backroomslite;

import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;

import com.poixson.tools.xJavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class BackroomsLitePlugin extends xJavaPlugin {
	@Override public int getBStatsID() { return 17876; }
	public static final Component CHAT_PREFIX = Component.text("[Backrooms] ").color(NamedTextColor.AQUA);

	protected static final String GENERATOR_NAME = "BackroomsLite";
	protected static final String DEFAULT_RESOURCE_PACK = "https://dl.poixson.com/mcplugins/pxnBackrooms/pxnBackrooms-resourcepack-{VERSION}.zip";

	protected final AtomicReference<Level0Generator> generator = new AtomicReference<Level0Generator>(null);



	public BackroomsLitePlugin() {
		super();
	}



	@Override
	public void onEnable() {
		super.onEnable();
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



	public Level0Generator getGenerator() {
		// existing generator
		{
			final Level0Generator gen = this.generator.get();
			if (gen != null)
				return gen;
		}
		// new instance
		{
			final Level0Generator gen = new Level0Generator(this);
			if (this.generator.compareAndSet(null, gen))
				return gen;
		}
		return this.generator.get();
	}



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		this.log().info(String.format("%s world: %s", GENERATOR_NAME, worldName));
		return this.getGenerator();
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
	}



}
