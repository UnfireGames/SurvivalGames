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
package com.gmail.filoghost.survivalgames.timers;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import wild.api.sound.EasySound;
import wild.api.world.Particle;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.MathUtils;
import com.gmail.filoghost.survivalgames.utils.PlayerUtils;

public class WorldBorderTimer extends TimerMaster {
	
	private int maxDistanceSquared, warningDistanceSquared;
	private int spawnX, spawnZ;
	
	private EasySound bassSound = new EasySound(Sound.NOTE_BASS);
	private EasySound plingSound = new EasySound(Sound.NOTE_PLING);
	public Map<Player, Location> lastSafeLocation = new WeakHashMap<Player, Location>();
	
	public WorldBorderTimer(World world, int maxDistance) {
		super(7, 10L);
		
		if (maxDistance < 50) {
			SurvivalGames.logPurple("La distanza minima del bordo è 50!");
			maxDistance = 50;
		}
		
		this.maxDistanceSquared = MathUtils.square(maxDistance);
		this.warningDistanceSquared = MathUtils.square(maxDistance - 10);
		spawnX = world.getSpawnLocation().getBlockX();
		spawnZ = world.getSpawnLocation().getBlockZ();
	}

	@Override
	public void run() {

		
		for (HGamer hGamer : SurvivalGames.getAllGamersUnsafe()) {
			
			if (hGamer.getStatus() == Status.GAMEMAKER) {
				// Possono allontanarsi quanto vogliono
				continue;
			}
				
			Player player = hGamer.getPlayer();
				
			Location loc = player.getLocation();
			int distanceSquared = distanceFromSpawnSquared(loc);
				
			if (distanceSquared > maxDistanceSquared) {
					
				bassSound.playTo(player);
				player.sendMessage(ChatColor.RED + "Sei ai limiti del mondo!");
				Particle.RED_DUST.displayPlayer(player, player.getWorld(), (float) loc.getX(), (float) loc.getY() + 0.75f, (float) loc.getZ(), 0.5f, 0.75f, 0.5f, 0f, 100);
				PlayerUtils.teleportDismount(player, lastSafeLocation.containsKey(player) ? lastSafeLocation.get(player) : player.getWorld().getSpawnLocation());
					
			} else if (distanceSquared > warningDistanceSquared) {
			
				Location previous = lastSafeLocation.get(player);
				if (previous == null || distanceFromSpawnSquared(previous) <= warningDistanceSquared) {
					// Non mandare il messaggio più di una volta
					plingSound.playTo(player);
					player.sendMessage(ChatColor.RED + "Ti stai avvicinando ai limiti del mondo!");
				}
				
				lastSafeLocation.put(player, loc);
					
			} else {
					
				lastSafeLocation.put(player, loc);
				
			}
		}
	}
	
	
	private int distanceFromSpawnSquared(Location loc) {
		return MathUtils.square(loc.getBlockX() - spawnX) + MathUtils.square(loc.getBlockZ() - spawnZ);
	}

}
