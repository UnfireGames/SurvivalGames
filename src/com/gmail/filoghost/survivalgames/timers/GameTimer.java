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
import org.bukkit.Sound;
import org.bukkit.block.Chest;

import wild.api.sound.EasySound;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.generation.ChestFiller;
import com.gmail.filoghost.survivalgames.generation.SpawnObjects;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.UnitUtils;

public class GameTimer extends TimerMaster {

	@Setter @Getter private int countdown;
	
	public GameTimer() {
		super(0, 20L);
		this.countdown = SurvivalGames.getSettings().gameMinutes * 60;
	}

	@Override
	public void run() {
		
		if (countdown <= 0) {
			
			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "E' iniziata la battaglia finale! Se entro 2 minuti non ci sarà un vincitore, il server si riavvierà.");
			
			SpawnObjects.clearPlatforms();
			for (HGamer hGamer : SurvivalGames.getAllGamersUnsafe()) {
				if (hGamer.getStatus() == Status.TRIBUTE) {
					SpawnObjects.assignPlatform(hGamer.getPlayer());
					SpawnObjects.toPlatformOrSpawn(hGamer.getPlayer());
				}
			}
			
			SurvivalGames.setState(GameState.FINAL_BATTLE);
			SurvivalGames.getFinalBattleTimer().startNewTask();
			stopTask();
			return;
		}
		
		if (countdown == 360) {
			Bukkit.broadcastMessage(ChatColor.GREEN + "Fra 1 minuto verranno riempite le casse allo spawn.");
			Bukkit.getScheduler().scheduleSyncDelayedTask(SurvivalGames.getInstance(), new Runnable() {
				public void run() {
					
					if (SurvivalGames.getState() == GameState.GAME) {
						new EasySound(Sound.NOTE_PIANO).playToAll();
						Bukkit.broadcastMessage(ChatColor.GREEN + "Sono state riempite le casse allo spawn!");
						for (Chest chest : SpawnObjects.getSpawnChests()) {
							ChestFiller.refill(chest);
						}
					}
				}
			}, 20 * 60);
		}
		
		// Se manca 1 minuto
		if (countdown == 60) {
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Fra 1 minuto inizia la battaglia finale, i tributi verranno teletrasportati allo spawn.");
		}
		
		if (countdown == 10) {
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Fra 10 secondi inizia la battaglia finale, i tributi verranno teletrasportati allo spawn.");
		}
		
		SidebarManager.setTime(UnitUtils.formatSecondsAuto(countdown));
		countdown--;
	}
}
