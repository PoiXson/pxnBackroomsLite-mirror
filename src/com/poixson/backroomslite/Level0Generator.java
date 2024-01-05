package com.poixson.backroomslite;

import static com.poixson.utils.BlockUtils.StringToBlockData;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import com.poixson.tools.abstractions.AtomicDouble;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


public class Level0Generator extends ChunkGenerator {

	// default params
	public static final double DEFAULT_NOISE_WALL_FREQ     = 0.022;
	public static final int    DEFAULT_NOISE_WALL_OCTAVE   = 2;
	public static final double DEFAULT_NOISE_WALL_GAIN     = 0.1;
	public static final double DEFAULT_NOISE_WALL_LACUN    = 0.4;
	public static final double DEFAULT_NOISE_WALL_STRENGTH = 2.28;
	public static final double THRESH_WALL_L               = 0.38;
	public static final double THRESH_WALL_H               = 0.5;

	// default blocks
	public static final String DEFAULT_BLOCK_WALL       = "minecraft:yellow_terracotta";
	public static final String DEFAULT_BLOCK_CEILING    = "minecraft:smooth_stone_slab";
	public static final String DEFAULT_BLOCK_SUBCEILING = "minecraft:stone";
	public static final String DEFAULT_BLOCK_CARPET     = "minecraft:light_gray_wool";

	protected final int level_y = 80;
	protected final int level_h = 5;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;

	// params
	public final AtomicDouble  noise_wall_freq     = new AtomicDouble( DEFAULT_NOISE_WALL_FREQ    );
	public final AtomicInteger noise_wall_octave   = new AtomicInteger(DEFAULT_NOISE_WALL_OCTAVE  );
	public final AtomicDouble  noise_wall_gain     = new AtomicDouble( DEFAULT_NOISE_WALL_GAIN    );
	public final AtomicDouble  noise_wall_lacun    = new AtomicDouble( DEFAULT_NOISE_WALL_LACUN   );
	public final AtomicDouble  noise_wall_strength = new AtomicDouble( DEFAULT_NOISE_WALL_STRENGTH);

	// blocks
	public final AtomicReference<String> block_wall       = new AtomicReference<String>(null);
	public final AtomicReference<String> block_subceiling = new AtomicReference<String>(null);
	public final AtomicReference<String> block_carpet     = new AtomicReference<String>(null);
	public final AtomicReference<String> block_ceiling    = new AtomicReference<String>(null);



	public Level0Generator() {
		// lobby walls
		this.noiseLobbyWalls = new FastNoiseLiteD();
		this.noiseLobbyWalls.setFrequency(              this.noise_wall_freq    .get());
		this.noiseLobbyWalls.setFractalOctaves(         this.noise_wall_octave  .get());
		this.noiseLobbyWalls.setFractalGain(            this.noise_wall_gain    .get());
		this.noiseLobbyWalls.setFractalLacunarity(      this.noise_wall_lacun   .get());
		this.noiseLobbyWalls.setFractalPingPongStrength(this.noise_wall_strength.get());
		this.noiseLobbyWalls.setNoiseType(NoiseType.Cellular);
		this.noiseLobbyWalls.setFractalType(FractalType.PingPong);
		this.noiseLobbyWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseLobbyWalls.setCellularReturnType(CellularReturnType.Distance);
		this.noiseLobbyWalls.setRotationType3D(RotationType3D.ImproveXYPlanes);
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		final BlockData block_wall       = StringToBlockData(this.block_wall,       DEFAULT_BLOCK_WALL      );
		final BlockData block_ceiling    = StringToBlockData(this.block_ceiling,    DEFAULT_BLOCK_CEILING   );
		final BlockData block_subceiling = StringToBlockData(this.block_subceiling, DEFAULT_BLOCK_SUBCEILING);
		final BlockData block_carpet     = StringToBlockData(this.block_carpet,     DEFAULT_BLOCK_CARPET    );
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
				valueWall = this.noiseLobbyWalls.getNoiseRot(xx, zz, 0.25);
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



	protected void loadConfig(final ConfigurationSection cfg) {
		// params
		{
			this.noise_wall_freq    .set(cfg.getDouble("Level0.Params.Noise-Wall-Freq"    ));
			this.noise_wall_octave  .set(cfg.getInt(   "Level0.Params.Noise-Wall-Octave"  ));
			this.noise_wall_gain    .set(cfg.getDouble("Level0.Params.Noise-Wall-Gain"    ));
			this.noise_wall_lacun   .set(cfg.getDouble("Level0.Params.Noise-Wall-Lacun"   ));
			this.noise_wall_strength.set(cfg.getDouble("Level0.Params.Noise-Wall-Strength"));
		}
		// block types
		{
			this.block_wall      .set(cfg.getString("Wall"      ));
			this.block_ceiling   .set(cfg.getString("Ceiling"   ));
			this.block_subceiling.set(cfg.getString("SubCeiling"));
			this.block_carpet    .set(cfg.getString("Carpet"    ));
		}
	}
	public static void ConfigDefaults(final FileConfiguration cfg) {
		// params
		cfg.addDefault("Level0.Params.Noise-Wall-Freq",     DEFAULT_NOISE_WALL_FREQ    );
		cfg.addDefault("Level0.Params.Noise-Wall-Octave",   DEFAULT_NOISE_WALL_OCTAVE  );
		cfg.addDefault("Level0.Params.Noise-Wall-Gain",     DEFAULT_NOISE_WALL_GAIN    );
		cfg.addDefault("Level0.Params.Noise-Wall-Lacun",    DEFAULT_NOISE_WALL_LACUN   );
		cfg.addDefault("Level0.Params.Noise-Wall-Strength", DEFAULT_NOISE_WALL_STRENGTH);
		// block types
		cfg.addDefault("Level0.Blocks.Wall",       DEFAULT_BLOCK_WALL      );
		cfg.addDefault("Level0.Blocks.Ceiling",    DEFAULT_BLOCK_CEILING   );
		cfg.addDefault("Level0.Blocks.SubCeiling", DEFAULT_BLOCK_SUBCEILING);
		cfg.addDefault("Level0.Blocks.Carpet",     DEFAULT_BLOCK_CARPET    );
	}



}
