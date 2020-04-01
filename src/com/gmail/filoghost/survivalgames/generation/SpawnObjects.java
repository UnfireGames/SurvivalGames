/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.survivalgames.generation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.utils.LocUtils;
import com.gmail.filoghost.survivalgames.utils.PlayerUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SpawnObjects {
	
	@Getter private static Set<Chest> spawnChests = Sets.newHashSet();
	@Getter private static List<Platform> platforms = Lists.newArrayList();

	public static void load(Location spawn, int chestRadius) {
		int chestRadiusSquared = chestRadius * chestRadius;
		
		World world = spawn.getWorld();
		Chunk spawnChunk = spawn.getChunk();
		
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				Chunk around = world.getChunkAt(spawnChunk.getX() + x, spawnChunk.getZ() + z);
				around.load();
				BlockState[] tileEntities = around.getTileEntities();
				
				for (BlockState tileEntity : tileEntities) {
					if (tileEntity instanceof Chest && LocUtils.distanceSquaredNoY(tileEntity.getLocation(), spawn) <= chestRadiusSquared) {
						spawnChests.add((Chest) tileEntity);
					}
					
					if (tileEntity instanceof Beacon && tileEntity.getBlock().getRelative(BlockFace.UP).getType() == Material.SPONGE) {
						tileEntity.getBlock().setType(Material.AIR);
						tileEntity.getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
						Location loc = tileEntity.getBlock().getLocation().add(0.5, 0.0, 0.5);
						loc.setYaw((float) getYaw(loc, spawn));
						platforms.add(new Platform(loc));
					}
				}
			}
		}
		
		Collections.sort(platforms, new Comparator<Platform>() {
			@Override
			public int compare(Platform o1, Platform o2) {
				if (o1.getLoc().getYaw() - o2.getLoc().getYaw() > 0.0) {
					return 1;
				} else {
					return -1;
				}
			}
		});
	}
	
	public static void clearPlatforms() {
		for (Platform platform : platforms) {
			platform.setPlayer(null);
		}
	}
	
	public static void toPlatformOrSpawn(Player player) {
		for (Platform platform : platforms) {
			if (platform.hasPlayer() && platform.getPlayer() == player) {
				Location loc = platform.getLoc();
				if (!LocUtils.equalsIgnoreLook(loc, player.getLocation())) {
					PlayerUtils.teleportDismount(player, platform.getLoc());
				}
				return;
			}
		}
		
		PlayerUtils.teleportDismount(player, SurvivalGames.getHighestSpawn());
	}
	
	
	public static boolean hasPlatform(Player player) {
		for (Platform platform : platforms) {
			if (platform.hasPlayer() && platform.getPlayer() == player) {
				return true;
			}
		}
		return false;
	}
	
	public static void removePlatform(Player player) {
		for (Platform platform : platforms) {
			if (platform.hasPlayer() && platform.getPlayer() == player) {
				platform.setPlayer(null);
				return;
			}
		}
	}
	
	public static boolean assignPlatform(Player player) {
		if (hasPlatform(player)) {
			return false;
		}
		
		Platform platform = getFreePlatform();
		if (platform != null) {
			platform.setPlayer(player);
			return true;
		} else {
			return false;
		}
	}
	
	public static Platform getFreePlatform() {
		for (Platform platform : platforms) {
			if (!platform.hasPlayer()) {
				return platform;
			}
		}
		
		return null;
	}
	
	public static boolean isThereFreePlatform() {
		for (Platform platform : platforms) {
			if (!platform.hasPlayer()) {
				return true;
			}
		}
		
		return false;
	}

	private static double getYaw(Location from, Location to) {
		// E' fatto in modo strano...
		double deltaX = to.getZ() - from.getZ();
		double deltaY = to.getX() - from.getX();
		
		if (deltaX == 0 && deltaY == 0) {
			return 0.0;
		} else if (deltaX == 0) {
			return deltaY > 0 ? -90.0 : 90.0;
		} else if (deltaY == 0) {
			return deltaX > 0 ? 0.0 : 180.0;
		}
		
		double ratio = deltaY / deltaX;
		
		if (deltaY > 0) {
			if (deltaX > 0) {
				// X+, Y+
				return - Math.toDegrees(Math.atan(ratio));
			} else {
				// X-, Y+
				return - 180.0 - Math.toDegrees(Math.atan(ratio));
			}
		} else {
			if (deltaX > 0) {
				// X+, Y-
				return - Math.toDegrees(Math.atan(ratio));
			} else {
				// X-, Y-
				return 180.0 - Math.toDegrees(Math.atan(ratio));
			}
		}
	}
}
