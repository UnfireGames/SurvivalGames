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
package com.gmail.filoghost.survivalgames.commands;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import wild.api.command.CommandFramework;
import com.gmail.filoghost.survivalgames.Perms;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLPlayerData;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;

public class StatsCommand extends CommandFramework {
	
	public StatsCommand() {
		super(SurvivalGames.getInstance(), "stats");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {

		if (args.length > 0) {
			CommandValidate.isTrue(sender.hasPermission(Perms.VIEW_OTHERS_STATS), "Non puoi vedere le statistiche degli altri.");
			
			String playerName = args[0];
			
			try {
				CommandValidate.isTrue(SQLManager.playerExists(playerName), "Quel giocatore non ha mai giocato qui!");
				SQLPlayerData data = SQLManager.getPlayerData(playerName);
				
				sender.sendMessage(ChatColor.GOLD + "----- Statistiche di " + playerName + " -----");
				sender.sendMessage(ChatColor.GRAY + "Punti: " + ChatColor.YELLOW + data.getPoints());
				sender.sendMessage(ChatColor.GRAY + "Vittorie: " + ChatColor.YELLOW + data.getWins());
				sender.sendMessage(ChatColor.GRAY + "Uccisioni: " + ChatColor.YELLOW + data.getKills());
				sender.sendMessage(ChatColor.GRAY + "Morti: " + ChatColor.YELLOW + data.getDeaths());
				
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Errore nel database, leggi la console.");
			}
			
			return;
		}
		
		final HGamer hGamer = SurvivalGames.getHGamer(CommandValidate.getPlayerSender(sender));
		
		hGamer.sendMessage("");
		hGamer.sendMessage(ChatColor.GOLD + "----- Le tue statistiche -----");
		
		new SQLTask() {
			@Override
			public void execute() throws SQLException {
				SQLPlayerData stats = SQLManager.getPlayerData(hGamer.getName());
				hGamer.setPoints(stats.getPoints());
				hGamer.sendMessage(ChatColor.GRAY + "Punti: " + ChatColor.YELLOW + stats.getPoints());
				hGamer.sendMessage(ChatColor.GRAY + "Vittorie: " + ChatColor.YELLOW + stats.getWins());
				hGamer.sendMessage(ChatColor.GRAY + "Uccisioni: " + ChatColor.YELLOW + stats.getKills());
				hGamer.sendMessage(ChatColor.GRAY + "Morti: " + ChatColor.YELLOW + stats.getDeaths());
			}
		}.submitAsync(hGamer.getPlayer());
	}

}
