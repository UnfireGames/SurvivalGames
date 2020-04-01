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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.sound.EasySound;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.generation.SpawnObjects;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.listener.MovementBlocker;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.UnitUtils;

public class FinalBattleTimer extends TimerMaster {

	private int countdown = 130;
	
	private EasySound plingSound = new EasySound(Sound.ORB_PICKUP, 1.5f);
	private EasySound clickSound = new EasySound(Sound.CLICK);
	private EasySound anvilSound = new EasySound(Sound.ANVIL_LAND);
	
	private MovementBlocker movementListener;
	
	public FinalBattleTimer() {
		super(0, 20L);
		movementListener = new MovementBlocker();
	}

	@Override
	public void startNewTask() {
		Bukkit.getPluginManager().registerEvents(movementListener, SurvivalGames.getInstance());
		super.startNewTask();
	}
	
	@Override
	public void run() {
				
		if (countdown <= 0) {
			SurvivalGames.stopServer(ChatColor.RED + "Il tempo è scaduto, nessuno ha vinto la battaglia finale.");
			return;
		}
		
		if (countdown > 120) {
			clickSound.playToAll();
			Bukkit.broadcastMessage(ChatColor.GREEN + "I tributi possono muoversi in " + UnitUtils.formatSeconds(countdown - 120) + ".");
			
		} else if (countdown == 120) {
			anvilSound.playToAll();
			movementListener.unregister();
			Bukkit.broadcastMessage(ChatColor.GREEN + "I tributi ora possono combattere!");
			
			new StrictBorderRunnable().runTaskTimer(SurvivalGames.getInstance(), 20L, 20L);
			
		} else if (countdown > 10) {
			
			if (countdown >= 60) {
				if (countdown % 30 == 0) {
					Bukkit.broadcastMessage(ChatColor.GREEN + "La battaglia finale termina in " + UnitUtils.formatMinutes(countdown / 60) + ".");
				}
			
			} else {
				if (countdown % 10 == 0) {
					Bukkit.broadcastMessage(ChatColor.GREEN + "La battaglia finale termina in " + UnitUtils.formatSeconds(countdown) + ".");
				}
			}
				
		} else {
			
			plingSound.playToAll();
			Bukkit.broadcastMessage(ChatColor.GREEN + "La battaglia finale termina in " + UnitUtils.formatSeconds(countdown) + ".");
			
		}

		SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
		countdown--;
	}
	
	public static class StrictBorderRunnable extends BukkitRunnable {

		Location spawnAtPlatformsHeight;
		int maxDistanceSquared;
		
		public StrictBorderRunnable() {
			spawnAtPlatformsHeight = SurvivalGames.getHighestSpawn().clone();
			spawnAtPlatformsHeight.setY(SpawnObjects.getPlatforms().get(0).getLoc().getY());
			
			maxDistanceSquared = (int) SpawnObjects.getPlatforms().get(0).getLoc().distanceSquared(spawnAtPlatformsHeight) + 25 * 25;
		}

		@Override
		public void run() {
			if (SurvivalGames.getState() == GameState.FINAL_BATTLE) {
				for (HGamer hGamer : SurvivalGames.getAllGamersUnsafe()) {
					if (hGamer.getStatus() == Status.TRIBUTE) {
						Location loc = hGamer.getPlayer().getLocation();
						if (loc.distanceSquared(spawnAtPlatformsHeight) > maxDistanceSquared) {
							hGamer.sendMessage(ChatColor.RED + "Sei troppo lontano, torna verso lo spawn!");
							loc.getWorld().playSound(loc, Sound.HURT_FLESH, 1F, 1F);
							hGamer.getPlayer().playEffect(EntityEffect.HURT);
							hGamer.getPlayer().damage(1.0);
						}
					}
				}
			}
		}
		
	}
}
