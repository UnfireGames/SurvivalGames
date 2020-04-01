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
package com.gmail.filoghost.survivalgames.hud.sidebar;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import wild.api.WildCommons;
import wild.api.WildConstants;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.SurvivalGames;

public class SidebarManager {

	private static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
	private static Objective side;
	
	private static Team timeTeam;
	private static Team stateTeam;
	private static Team killsTeam;
	private static Team pointsTeam;
	private static Team playersTeam;
	
	private static final String	SMALL_TITLE_PREFIX = ChatColor.AQUA + "" + ChatColor.BOLD;
	
	private static Map<String, Integer> playerKills = new HashMap<String, Integer>();
	
	
	public static void initialize(GameState initialState) {
		
		// Rimuove gli obiettivi precedenti
		safeRemoveObjective(scoreboard.getObjective(DisplaySlot.SIDEBAR));
		safeRemoveObjective(scoreboard.getObjective("info"));
		
		side = scoreboard.registerNewObjective("info", "dummy");
		side.setDisplayName("    " + ChatColor.DARK_AQUA + ChatColor.BOLD + ChatColor.UNDERLINE + "Survival Games" + ChatColor.RESET + "    ");
		showSidebar();
		
		setScore(emptyLine(14), 14);
		setScore(SMALL_TITLE_PREFIX + "Giocatori:", 13);
		String playersEntry = setScore(emptyLine(12), 12);
		setScore(emptyLine(11), 11);
		setScore(SMALL_TITLE_PREFIX + "Tempo:", 10);
		String timeEntry = setScore(emptyLine(9), 9);
		setScore(emptyLine(8), 8);
		setScore(SMALL_TITLE_PREFIX + "Fase:", 7);
		String stateEntry = setScore(emptyLine(6), 6);
		setScore(emptyLine(4), 4);
		String pointsEntry = setScore(SMALL_TITLE_PREFIX + "Punti:", 3);
		String killsEntry = setScore(SMALL_TITLE_PREFIX + "Uccisioni:", 2);
		setScore(emptyLine(1), 1);
		WildConstants.Messages.displayIP(scoreboard, side, 0);
		
		// Crea i team per i prefissi
		timeTeam = createSafeTeam("time");
		timeTeam.addEntry(timeEntry);
		stateTeam = createSafeTeam("state");
		stateTeam.addEntry(stateEntry);
		killsTeam = createSafeTeam("kills");
		killsTeam.addEntry(killsEntry);
		pointsTeam = createSafeTeam("points");
		pointsTeam.addEntry(pointsEntry);
		playersTeam = createSafeTeam("players");
		playersTeam.addEntry(playersEntry);

		setTime("-");
		updateState(initialState);
		killsTeam.setSuffix(ChatColor.WHITE + " 0");
		pointsTeam.setSuffix(ChatColor.WHITE + " 0");
	}
	
	private static String emptyLine(int sideNumber) {
		if (sideNumber > 15 || sideNumber < 0) return "";
		return ChatColor.values()[sideNumber].toString();
	}
	
	private static Team createSafeTeam(String name) {
		if (scoreboard.getTeam(name) != null) {
			scoreboard.getTeam(name).unregister();
		}
		
		return scoreboard.registerNewTeam(name);
	}
	
	private static void safeRemoveObjective(Objective o) {
		if (o != null) o.unregister();
	}
	
	private static String setScore(String entry, int score) {
		side.getScore(entry).setScore(score);
		return entry;
	}
	
	public static void showSidebar() {
		side.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public static void hideSidebar() {
		side.setDisplaySlot(null);
	}
	
	public static void updateState(GameState state) {
		switch (state) {
			case END:
				stateTeam.setPrefix("Fine");
				break;
				
			case FINAL_BATTLE:
				stateTeam.setPrefix("Battaglia finale");
				break;
				
			case GAME:
				stateTeam.setPrefix("Combattimento");
				break;
				
			case IMMOBILITY:
				stateTeam.setPrefix("Inizio");
				break;
				
			case INVINCIBILITY:
				stateTeam.setPrefix("Invincibilità");
				break;
				
			case PRE_GAME:
				stateTeam.setPrefix("Attesa");
				break;
				
			default:
				stateTeam.setPrefix("ERROR");
				break;
		}
	}
	
	public static void setTime(String time) {
		if (!timeTeam.getPrefix().equals(time)) {
			timeTeam.setPrefix(time);
		}
	}
	
	public static void setPlayers(int i) {
		String number = String.valueOf(i);
		
		if (!playersTeam.getPrefix().equals(number)) {
			playersTeam.setPrefix(number);
		}
	}
	
	public static void updatePoints(Player player, int points) {
		if (player.getScoreboard().equals(scoreboard)) {
			
			try {
				WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, pointsTeam, "", ChatColor.WHITE + " " + points);
			} catch (Exception ex) {
				ex.printStackTrace();
				SurvivalGames.logPurple("Impossibile aggiornare il punteggio di " + player.getName() + " sulla scoreboard!");
			}
		}
		
	}
	
	public static int getKills(Player player) {
		if (playerKills.containsKey(player.getName())) {
			return playerKills.get(player.getName()).intValue();
		}
		
		return 0;
	}
	
	public static void addKill(Player player) {
		int kills = 0;
		
		String playerName = player.getName();
		
		if (playerKills.containsKey(playerName)) {
			kills = playerKills.get(playerName).intValue();
		}
		
		kills++;
		playerKills.put(playerName, Integer.valueOf(kills));
		
		if (player.getScoreboard().equals(scoreboard)) {
			
			try {
				WildCommons.Unsafe.sendTeamPrefixSuffixChangePacket(player, killsTeam, "", ChatColor.WHITE + " " + kills);
			} catch (Exception ex) {
				ex.printStackTrace();
				SurvivalGames.logPurple("Impossibile aggiornare il punteggio di " + playerName + " sulla scoreboard!");
			}
		}
	}

}
