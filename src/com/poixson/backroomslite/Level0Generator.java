package com.poixson.backroomslite;

import static com.poixson.utils.BlockUtils.StringToBlockDataDef;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import com.poixson.tools.FastNoiseLiteD;
import com.poixson.tools.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.tools.FastNoiseLiteD.CellularReturnType;
import com.poixson.tools.FastNoiseLiteD.FractalType;
import com.poixson.tools.FastNoiseLiteD.NoiseType;
import com.poixson.tools.xRand;
import com.poixson.tools.abstractions.AtomicDouble;


public class Level0Generator extends ChunkGenerator {

	// default params
	protected static final double DEFAULT_NOISE_WALL_FREQ     = 0.022;
	protected static final int    DEFAULT_NOISE_WALL_OCTAVE   = 2;
	protected static final double DEFAULT_NOISE_WALL_GAIN     = 0.1;
	protected static final double DEFAULT_NOISE_WALL_LACUN    = 0.4;
	protected static final double DEFAULT_NOISE_WALL_STRENGTH = 2.28;
	protected static final double THRESH_WALL_L               = 0.38;
	protected static final double THRESH_WALL_H               = 0.5;

	// default blocks
	protected static final String DEFAULT_BLOCK_WALL       = "minecraft:yellow_terracotta";
	protected static final String DEFAULT_BLOCK_CEILING    = "minecraft:smooth_stone_slab";
	protected static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:stone";
	protected static final String DEFAULT_BLOCK_CARPET     = "minecraft:light_gray_wool";

	protected final int level_y = 80;
	protected final int level_h = 5;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;

	// params
	protected final AtomicDouble  noise_wall_freq     = new AtomicDouble( DEFAULT_NOISE_WALL_FREQ    );
	protected final AtomicInteger noise_wall_octave   = new AtomicInteger(DEFAULT_NOISE_WALL_OCTAVE  );
	protected final AtomicDouble  noise_wall_gain     = new AtomicDouble( DEFAULT_NOISE_WALL_GAIN    );
	protected final AtomicDouble  noise_wall_lacun    = new AtomicDouble( DEFAULT_NOISE_WALL_LACUN   );
	protected final AtomicDouble  noise_wall_strength = new AtomicDouble( DEFAULT_NOISE_WALL_STRENGTH);

	// blocks
	protected final String block_wall;
	protected final String block_carpet;
	protected final String block_ceiling;
	protected final String block_subceiling;



	public Level0Generator(final BackroomsLitePlugin plugin) {
		final ConfigurationSection cfg = plugin.getConfig();
		final int seed = cfg.getInt("Seed", (int) (new xRand()).seed_time().getSeed() );
		final ConfigurationSection cfgParams = cfg.getConfigurationSection("Level0.Params");
		final ConfigurationSection cfgBlocks = cfg.getConfigurationSection("Level0.Blocks");
		this.configDefaults(cfgParams, cfgBlocks);
		// params
		{
			this.noiseLobbyWalls = new FastNoiseLiteD(seed, 0.25);
			this.initNoise(cfgParams);
		}
		// block types
		{
			this.block_wall       = cfgBlocks.getString("Wall");
			this.block_carpet     = cfgBlocks.getString("Carpet");
			this.block_ceiling    = cfgBlocks.getString("Ceiling");
			this.block_subceiling = cfgBlocks.getString("SubCeiling");
		}
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		final BlockData block_wall       = StringToBlockDataDef(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_ceiling    = StringToBlockDataDef(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_subceiling = StringToBlockDataDef(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_carpet     = StringToBlockDataDef(this.block_carpet,     DEFAULT_BLOCK_CARPET    );
		if (block_wall       == null) throw new RuntimeException("Invalid block type for level 0 Wall"      );
		if (block_ceiling    == null) throw new RuntimeException("Invalid block type for level 0 Ceiling"   );
		if (block_subceiling == null) throw new RuntimeException("Invalid block type for level 0 SubCeiling");
		if (block_carpet     == null) throw new RuntimeException("Invalid block type for level 0 Carpet"    );
		final BlockData lamp = Bukkit.createBlockData("minecraft:redstone_lamp[lit=true]");
		double valueWall;
		boolean isWall;
		int xx, zz;
		int modX, modZ;
		final int wh = this.level_h + 4;
		final int cy = this.level_y + this.level_h + 2;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			modZ = (zz < 0 ? 0-zz : zz) % 7;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				modX = (xx < 0 ? 1-xx : xx) % 7;
				valueWall = this.noiseLobbyWalls.getNoise(xx, zz);
				isWall = (valueWall > THRESH_WALL_L && valueWall < THRESH_WALL_H);
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				chunk.setBlock(ix, cy+2,         iz, Material.BEDROCK);
				if (isWall) {
					for (int iy=1; iy<wh; iy++) {
						chunk.setBlock(ix, this.level_y+iy, iz, block_wall);
					}
				} else {
					chunk.setBlock(ix, this.level_y+1, iz, block_carpet);
					if (modZ == 0 && modX < 2) {
						// ceiling lights
						chunk.setBlock(ix, cy, iz, lamp);
						chunk.setBlock(ix, cy+1, iz, Material.REDSTONE_BLOCK);
					} else {
						// ceiling
						chunk.setBlock(ix, cy, iz, block_ceiling);
						final Slab slab = (Slab) chunk.getBlockData(ix, cy, iz);
						slab.setType(Slab.Type.TOP);
						chunk.setBlock(ix, cy,   iz, slab);
						chunk.setBlock(ix, cy+1, iz, block_subceiling);
					}
				}
			}
		}
	}



	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int y = this.level_y + 2;
		return world.getBlockAt(0, y, 0).getLocation();
	}



	// -------------------------------------------------------------------------------
	// configs



	protected void initNoise(final ConfigurationSection cfg) {
		// lobby walls
		this.noiseLobbyWalls.setAngle(0.25);
		this.noiseLobbyWalls.setFrequency(               cfg.getDouble("Noise-Wall-Freq"    ));
		this.noiseLobbyWalls.setFractalOctaves(          cfg.getInt(   "Noise-Wall-Octave"  ));
		this.noiseLobbyWalls.setFractalGain(             cfg.getDouble("Noise-Wall-Gain"    ));
		this.noiseLobbyWalls.setFractalLacunarity(       cfg.getDouble("Noise-Wall-Lacun"   ));
		this.noiseLobbyWalls.setFractalPingPongStrength( cfg.getDouble("Noise-Wall-Strength"));
		this.noiseLobbyWalls.setNoiseType(               NoiseType.Cellular                        );
		this.noiseLobbyWalls.setFractalType(             FractalType.PingPong                      );
		this.noiseLobbyWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan        );
		this.noiseLobbyWalls.setCellularReturnType(      CellularReturnType.Distance               );
	}



	protected void configDefaults(final ConfigurationSection cfgParams, final ConfigurationSection cfgBlocks) {
		// params
		cfgParams.addDefault("Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ    );
		cfgParams.addDefault("Noise-Wall-Octave",   DEFAULT_NOISE_WALL_OCTAVE  );
		cfgParams.addDefault("Noise-Wall-Gain",     DEFAULT_NOISE_WALL_GAIN    );
		cfgParams.addDefault("Noise-Wall-Lacun",    DEFAULT_NOISE_WALL_LACUN   );
		cfgParams.addDefault("Noise-Wall-Strength", DEFAULT_NOISE_WALL_STRENGTH);
		// block types
		cfgBlocks.addDefault("Wall",       DEFAULT_BLOCK_WALL      );
		cfgBlocks.addDefault("Carpet",     DEFAULT_BLOCK_CARPET    );
		cfgBlocks.addDefault("Ceiling",    DEFAULT_BLOCK_CEILING   );
		cfgBlocks.addDefault("SubCeiling", DEFAULT_BLOCK_SUBCEILING);
	}



}
