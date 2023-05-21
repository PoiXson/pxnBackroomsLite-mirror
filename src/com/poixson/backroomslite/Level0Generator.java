package com.poixson.backroomslite;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


public class Level0Generator extends ChunkGenerator {

	public static final double THRESH_WALL_L = 0.38;
	public static final double THRESH_WALL_H = 0.5;

	public static final Material LOBBY_WALL     = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR = Material.OAK_PLANKS;
	public static final Material LOBBY_CARPET   = Material.LIGHT_GRAY_WOOL;

	protected final int level_y = 80;
	protected final int level_h = 5;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;



	public Level0Generator() {
		// lobby walls
		this.noiseLobbyWalls = new FastNoiseLiteD();
		this.noiseLobbyWalls.setFrequency(0.022);
		this.noiseLobbyWalls.setFractalOctaves(2);
		this.noiseLobbyWalls.setFractalGain(0.1);
		this.noiseLobbyWalls.setFractalLacunarity(0.4);
		this.noiseLobbyWalls.setNoiseType(NoiseType.Cellular);
		this.noiseLobbyWalls.setFractalType(FractalType.PingPong);
		this.noiseLobbyWalls.setFractalPingPongStrength(2.28);
		this.noiseLobbyWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseLobbyWalls.setCellularReturnType(CellularReturnType.Distance);
		this.noiseLobbyWalls.setRotationType3D(RotationType3D.ImproveXYPlanes);
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		double valueWall;
		boolean isWall;
		int xx, zz;
		final int wh = this.level_h + 4;
		final int cy = this.level_y + this.level_h + 2;
		for (int iz=0; iz<16; iz++) {
			zz = (chunkZ * 16) + iz;
			for (int ix=0; ix<16; ix++) {
				xx = (chunkX * 16) + ix;
				valueWall = this.noiseLobbyWalls.getNoiseRot(xx, zz, 0.25);
				isWall = (valueWall > THRESH_WALL_L && valueWall < THRESH_WALL_H);
				chunk.setBlock(ix, this.level_y, iz, Material.BEDROCK);
				chunk.setBlock(ix, cy+2,         iz, Material.BEDROCK);
				if (isWall) {
					for (int iy=1; iy<wh; iy++) {
						chunk.setBlock(ix, this.level_y+iy, iz, LOBBY_WALL);
					}
				} else {
					chunk.setBlock(ix, this.level_y+1, iz, LOBBY_CARPET);
					final int  modX7 = (xx < 0 ? 1-xx : xx) % 7;
					final int  modZ7 = (zz < 0 ? 0-zz : zz) % 7;
					if (modZ7 == 0 && modX7 < 2) {
						// ceiling lights
						chunk.setBlock(ix, cy, iz, Material.REDSTONE_LAMP);
						final BlockData block = chunk.getBlockData(ix, cy, iz);
						((Lightable)block).setLit(true);
						chunk.setBlock(ix, cy,   iz, block);
						chunk.setBlock(ix, cy+1, iz, Material.REDSTONE_BLOCK);
					} else {
						// ceiling
						chunk.setBlock(ix, cy, iz, Material.SMOOTH_STONE_SLAB);
						final Slab slab = (Slab) chunk.getBlockData(ix, cy, iz);
						slab.setType(Slab.Type.TOP);
						chunk.setBlock(ix, cy,   iz, slab);
						chunk.setBlock(ix, cy+1, iz, Material.STONE);
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



}
