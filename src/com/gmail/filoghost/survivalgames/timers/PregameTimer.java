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

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import wild.api.bridges.CosmeticsBridge;
import wild.api.sound.EasySound;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.generation.SpawnObjects;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.UnitUtils;

public class PregameTimer extends TimerMaster {

	@Getter @Setter private int countdown;
	
	@Getter private boolean started;
	
	private EasySound clickSound = new EasySound(Sound.CLICK);
	
	private ItemStack compass = new ItemStack(Material.COMPASS);
	
	@Getter private String lastCountdownMessage;
	
	public PregameTimer() {
		super(0, 20L);
		resetCountdown();
		lastCountdownMessage = "N/A";
	}
	
	private void resetCountdown() {
		started = false;
		this.countdown = SurvivalGames.getSettings().startCountdown;
		SidebarManager.setTime("-");
	}

	@Override
	public void run() {
		
		if (!started) {
			
			if (SurvivalGames.countTributes() >= SurvivalGames.getSettings().minPlayers) {
				started = true;
				SidebarManager.setTime(UnitUtils.formatMinutes(countdown / 60));
			} else {
				return;
			}
		}
		
		if (countdown <= 0) {
			
			if (SurvivalGames.countTributes() < SurvivalGames.getSettings().minPlayers) {
				Bukkit.broadcastMessage(ChatColor.GREEN + "Ci sono pochi giocatori, il conto alla rovescia riparte.");
				resetCountdown();
				return;
			}

			int count = 0;
			
			for (HGamer tribute : SurvivalGames.getAllGamersUnsafe()) {
				if (tribute.getStatus() == Status.TRIBUTE) {
					CosmeticsBridge.updateCosmetics(tribute.getPlayer(), CosmeticsBridge.Status.GAME);
					tribute.cleanCompletely(GameMode.SURVIVAL);
					SpawnObjects.toPlatformOrSpawn(tribute.getPlayer());
					tribute.getPlayer().getInventory().addItem(compass);
					count++;
				}
			};
			
			SurvivalGames.setPointsForWin(count * SurvivalGames.getSettings().winPointsPerTribute);
			
			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(ChatColor.GREEN + "La partita è iniziata! Mappa: " + ChatColor.DARK_GREEN + SurvivalGames.getInstance().getMapName());
			Bukkit.broadcastMessage(ChatColor.GOLD + "Per la vittoria ci sono in palio " + SurvivalGames.getPointsForWin() + " punti.");
			
			SurvivalGames.setState(GameState.IMMOBILITY);
			SurvivalGames.getImmobilityTimer().startNewTask();
			stopTask();
			return;
		}
		
		
		if (countdown >= 60) {
			
			// Ogni 15 secondi
			if (countdown % 15 == 0) {
				lastCountdownMessage = UnitUtils.formatMinutes(countdown / 60);
				Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
				SidebarManager.setTime(lastCountdownMessage);
			}
			
		} else if (countdown > 10) {
			
			// Ogni 10 secondi
			if (countdown % 10 == 0) {
				lastCountdownMessage = UnitUtils.formatSeconds(countdown);
				Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
			}
			SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
			
		} else {
			
			// Countdown finale
			clickSound.playToAll();
			lastCountdownMessage = UnitUtils.formatSeconds(countdown);
			Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
			SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
			
		}

		countdown--;
	}
}
