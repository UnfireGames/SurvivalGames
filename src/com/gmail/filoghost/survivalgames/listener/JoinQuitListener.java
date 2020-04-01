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
package com.gmail.filoghost.survivalgames.listener;

import java.sql.SQLException;
import java.util.Set;

import lombok.AllArgsConstructor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.Perms;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.generation.Platform;
import com.gmail.filoghost.survivalgames.generation.SpawnObjects;
import com.gmail.filoghost.survivalgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.google.common.collect.Sets;

@AllArgsConstructor
public class JoinQuitListener implements Listener {
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		
		if (event.getResult() == Result.KICK_FULL && player.hasPermission(Perms.JOIN_FULL)) {
			event.allow();
		}
		
		if (SurvivalGames.getState() == GameState.PRE_GAME) {
			
			if (!SpawnObjects.isThereFreePlatform() && !canBeGamemakerOrSpectactor(player)) {
				// Pieno, e non può essere spettatore o gamemaker
				event.disallow(Result.KICK_OTHER, ChatColor.RED + "Non ci sono più posti disponibili.");
				return;
			}
			
		} else {
			
			if (!canBeGamemakerOrSpectactor(player)) {
				// Già iniziata e non può essere spettatore o gamemaker
				event.disallow(Result.KICK_OTHER, ChatColor.RED + "La partita è già iniziata.");
				return;
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Status status = Status.TRIBUTE;
		
		if (SurvivalGames.getState() != GameState.PRE_GAME || !SpawnObjects.isThereFreePlatform()) {
			if (player.hasPermission(Perms.GAMEMAKER)) {
				status = Status.GAMEMAKER;
			} else if (player.hasPermission(Perms.SPECTATOR)) {
				status = Status.SPECTATOR;
			} else {
				SurvivalGames.logPurple(player.getName() + " è un tributo ed è entrato fuori dalla partita o quando non c'erano posti!?");
			}
		}
		
		final HGamer hGamer = SurvivalGames.registerHGamer(player, status); // IMPORTANTE!
		
		if (hGamer.getStatus() == Status.TRIBUTE) {
			SpawnObjects.toPlatformOrSpawn(player);
		} else {
			hGamer.teleportDismount(SurvivalGames.getHighestSpawn());
		}
		
		final String name = player.getName();
		new SQLTask() {
			public void execute() throws SQLException {
				if (!SQLManager.playerExists(name)) {
					SQLManager.createPlayerData(name);
				}
				
				hGamer.setPoints(SQLManager.getStat(name, SQLColumns.POINTS));
			}
		}.submitAsync(player);
		
		// Dopo aver registrato il giocatore
		int tributes = SurvivalGames.countTributes();
		SidebarManager.setPlayers(tributes);
		TeleporterMenu.update();
		
		if (SurvivalGames.getState() == GameState.PRE_GAME && !SpawnObjects.isThereFreePlatform()) {
			// Significa che è pieno
			if (SurvivalGames.getPregameTimer().getCountdown() > 15) {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Server pieno: il conto alla rovescia è stato ridotto.");
				SurvivalGames.getPregameTimer().setCountdown(15);
			}
		}
	}
	
	public static Set<HGamer> kickedOnDeath = Sets.newHashSet();
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		HGamer hQuitted = SurvivalGames.unregisterHGamer(event.getPlayer()); // IMPORTANTE!
		
		if (SurvivalGames.getState() == GameState.PRE_GAME) {
			for (Platform platform : SpawnObjects.getPlatforms()) {
				if (platform.hasPlayer() && platform.getPlayer() == event.getPlayer()) {
					platform.setPlayer(null); // Liberala
				}
			}
		}
		
		// Conta la morta solo nell'invincibilità, nel game, e nella battaglia finale
		if (!kickedOnDeath.remove(hQuitted) && hQuitted.getStatus() == Status.TRIBUTE && (SurvivalGames.getState() == GameState.INVINCIBILITY || SurvivalGames.getState() == GameState.GAME || SurvivalGames.getState() == GameState.FINAL_BATTLE)) {
			DeathListener.parseDeath(hQuitted, null, ChatColor.RED + hQuitted.getName() + " è uscito dalla partita.", false, false);
		}

		SidebarManager.setPlayers(SurvivalGames.countTributes());
		TeleporterMenu.update();
	}
	
	private boolean canBeGamemakerOrSpectactor(Player player) {
		return player.hasPermission(Perms.GAMEMAKER) || player.hasPermission(Perms.SPECTATOR);
	}
	
}
