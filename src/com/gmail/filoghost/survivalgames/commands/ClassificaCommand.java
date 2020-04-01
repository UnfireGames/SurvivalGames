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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import wild.api.command.CommandFramework;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.tasks.SendRankingTask;

public class ClassificaCommand extends CommandFramework {

	public ClassificaCommand() {
		super(SurvivalGames.getInstance(), "classifica");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GOLD + "----- Comandi classifica -----");
			sender.sendMessage(ChatColor.YELLOW + "/classifica punti");
			sender.sendMessage(ChatColor.YELLOW + "/classifica uccisioni");
			sender.sendMessage(ChatColor.YELLOW + "/classifica vittorie");
			sender.sendMessage(ChatColor.YELLOW + "/classifica morti");
			sender.sendMessage(ChatColor.YELLOW + "/stats " + ChatColor.GRAY + "- Le tue statistiche");
			sender.sendMessage("");
			return;
		}
		
		if (args[0].equalsIgnoreCase("punti")) {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(SurvivalGames.getInstance(), new SendRankingTask(sender, SQLColumns.POINTS, "punti"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("uccisioni")) {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(SurvivalGames.getInstance(), new SendRankingTask(sender, SQLColumns.KILLS, "uccisioni"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("vittorie")) {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(SurvivalGames.getInstance(), new SendRankingTask(sender, SQLColumns.WINS, "vittorie"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("morti")) {
			Bukkit.getScheduler().scheduleAsyncDelayedTask(SurvivalGames.getInstance(), new SendRankingTask(sender, SQLColumns.DEATHS, "morti"));
			return;
		}
		
		if (args[0].equalsIgnoreCase("reset") && sender instanceof ConsoleCommandSender) {
			sender.sendMessage(ChatColor.GREEN + "Pulizia uccisioni, morti, vittorie e punti (100)...");
			new SQLTask() {
				public void execute() throws SQLException {
					SQLManager.getMysql().update("UPDATE " + SQLManager.prefix + "players SET " +
							SQLColumns.KILLS + " = 0, " +
							SQLColumns.DEATHS + " = 0, " +
							SQLColumns.WINS + " = 0, " +
							SQLColumns.POINTS + " = 100" +
							";"
					);
					sender.sendMessage(ChatColor.GREEN + "Finita pulizia!");
				}
			}.submitAsync(sender);
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Comando sconosciuto. Scrivi \"/classifica\" per i comandi.");
	}

}
