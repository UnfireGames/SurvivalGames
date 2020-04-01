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
import java.util.Map;
import java.util.WeakHashMap;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import wild.api.sound.EasySound;
import wild.api.world.Particle;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.Perms;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLPlayerData;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.DamageEventData;
import com.gmail.filoghost.survivalgames.utils.PlayerUtils;

public class DeathListener implements Listener {
	
	@Getter private static Map<Player, DamageEventData> lastPlayerDamageEvent = new WeakHashMap<>();
	
	public static void setLastDamager(Player victim, Player damager) {
		lastPlayerDamageEvent.put(victim, new DamageEventData(damager.getName(), System.currentTimeMillis()));
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent event) {
		// Nasconde il messaggio della morte
		String deathMessage = event.getDeathMessage();
		event.setDeathMessage(null);
		
		for (Entity entity : event.getEntity().getWorld().getEntities()) {
			if (entity.getType() == EntityType.WOLF) {
				Wolf wolf = (Wolf) entity;
				if (wolf.getOwner() != null && wolf.getOwner() == event.getEntity()) {
					Particle.CLOUD.display(wolf.getLocation(), 0.2F, 0.2F, 0.2F, 0, 20);
					wolf.remove();
				}
			}
		}
		
		Player killer = PlayerUtils.getRealDamager(event.getEntity().getLastDamageCause());
		
		if (killer == null && lastPlayerDamageEvent.containsKey(event.getEntity())) {
			
			DamageEventData lastDamage = lastPlayerDamageEvent.get(event.getEntity());
			HGamer hKiller = SurvivalGames.getHGamer(lastDamage.getDamager());
			
			if (hKiller != null && System.currentTimeMillis() - lastDamage.getTimestamp() < 5000) {
				killer = hKiller.getPlayer();
			}
		}
		
		parseDeath(SurvivalGames.getHGamer(event.getEntity()), killer != null ? SurvivalGames.getHGamer(killer) : null, deathMessage, true, true);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		SurvivalGames.getHGamer(event.getPlayer()).onRespawn();
		event.setRespawnLocation(SurvivalGames.getHighestSpawn());
	}
	
	/**
	 *  Messaggio sulla chat, kick, coins... (static perché utilizzato da altre classi, come per gli headshot)
	 */
	public static void parseDeath(final HGamer hVictim, final HGamer hKiller, String message, boolean kick, boolean trySpectate) {
		if (message == null) message = "";
		
		final int victimBounty = hVictim.getBounty();
		hVictim.setBounty(0);
		
		new SQLTask() {
			@Override
			public void execute() throws SQLException {
				
				SQLPlayerData victimData = SQLManager.getPlayerData(hVictim.getName());
				int lostPoints = (int) Math.ceil(0.05 * victimData.getPoints());
				
				victimData.setPoints(victimData.getPoints() - lostPoints);
				victimData.setDeaths(victimData.getDeaths() + 1);
				
				SQLManager.savePlayerData(hVictim.getName(), victimData);
				hVictim.setPoints(victimData.getPoints());
				hVictim.sendMessage(ChatColor.GOLD + "- " + lostPoints + " punti");
				
				if (hKiller != null && hKiller != hVictim) {
					int earntPoints = lostPoints + victimBounty;
					
					SQLPlayerData killerData = SQLManager.getPlayerData(hKiller.getName());
					
					killerData.setPoints(killerData.getPoints() + earntPoints);
					killerData.setKills(killerData.getKills() + 1);
					
					SQLManager.savePlayerData(hKiller.getName(), killerData);
					hKiller.setPoints(killerData.getPoints());
					hKiller.sendMessage(ChatColor.GOLD + "+ " + earntPoints + " punti");
				}
				
			}
		}.submitAsync(null);
		
		if (hKiller != null && hKiller.getStatus() == Status.TRIBUTE) {
			
			EasySound.quickPlay(hKiller.getPlayer(), Sound.ORB_PICKUP);
			SidebarManager.addKill(hKiller.getPlayer());
		}
		
		if (trySpectate) {
			
			if (hVictim.getPlayer().hasPermission(Perms.GAMEMAKER)) {
				hVictim.setStatus(Status.GAMEMAKER, false, true, false, true);
				
			} else if (hVictim.getPlayer().hasPermission(Perms.SPECTATOR)) {
				hVictim.setStatus(Status.SPECTATOR, false, true, false, true);
				
			} else {
				if (kick) {
					JoinQuitListener.kickedOnDeath.add(hVictim);
					hVictim.getPlayer().kickPlayer(message + "§0§0§0");
				}
			}
			
		} else if (kick) {
			JoinQuitListener.kickedOnDeath.add(hVictim);
			hVictim.getPlayer().kickPlayer(message + "§0§0§0");
		}
		
		Location thunderLoc = hVictim.getPlayer().getLocation();
		thunderLoc.setY(255);
		hVictim.getPlayer().getWorld().strikeLightningEffect(thunderLoc);

		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(message);
		int tributes = SurvivalGames.countTributes();
		Bukkit.broadcastMessage(ChatColor.RED.toString() + (tributes == 1 ? " giocatore rimanente." : " giocatori rimanenti."));
		
		if (tributes <= 3 && SurvivalGames.getState() == GameState.GAME) {
			if (SurvivalGames.getGameTimer().getCountdown() > 120) {
				SurvivalGames.getGameTimer().setCountdown(120);
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Rimangono pochi tributi, conto alla rovescia ridotto!");
			}
		}
	}
}
