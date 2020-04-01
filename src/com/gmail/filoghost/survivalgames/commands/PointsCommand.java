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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.command.CommandFramework;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.utils.Format;


public class PointsCommand extends CommandFramework {

	public PointsCommand() {
		super(SurvivalGames.getInstance(), "points", "point");
	}

	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			final HGamer hGamer = SurvivalGames.getHGamer(CommandValidate.getPlayerSender(sender));
			new SQLTask() {
				@Override
				public void execute() throws SQLException {
					int points = SQLManager.getStat(hGamer.getName(), SQLColumns.POINTS);
					
					hGamer.setPoints(points);
					hGamer.sendMessage("");
					hGamer.sendMessage(Format.CHAT_ECONOMY + "Possiedi " + points + " punti.");
					hGamer.sendMessage(Format.CHAT_INFO + "Guadagni il 5% dei punti di chi elimini, (numero di giocatori * 2) per ogni vittoria.");
				}
			}.submitAsync(hGamer.getPlayer());
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("give")) {
			CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Questo comando può essere usato solo da console.");
			CommandValidate.minLength(args, 3, "Utilizzo: /points give <giocatore> <coins>");
			
			final String playerName = args[1];
			final int points = CommandValidate.getPositiveIntegerNotZero(args[2]);

			new SQLTask() {
				@Override
				public void execute() throws SQLException {

					if (!SQLManager.playerExists(playerName)) {
						sender.sendMessage(ChatColor.RED + "Quel giocatore non ha mai giocato qui!");
						return;
					}
					
					SQLManager.increaseStat(playerName, SQLColumns.POINTS, points);
					final int newPoints = SQLManager.getStat(playerName, SQLColumns.POINTS);
					sender.sendMessage(ChatColor.YELLOW + "Sono stati mandati " + points + " punti a " + playerName);
					
					new BukkitRunnable() {
						@Override
						public void run() {
							HGamer hGamer = SurvivalGames.getHGamer(playerName);
							if (hGamer != null) {
								hGamer.setPoints(newPoints);
							}
						}
					}.runTask(SurvivalGames.getInstance());
				}
			}.submitAsync(sender);
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("set")) {
			CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Questo comando può essere usato solo da console.");
			CommandValidate.minLength(args, 3, "Utilizzo: /points set <giocatore> <punti>");
			
			final String playerName = args[1];
			final int points = CommandValidate.getPositiveInteger(args[2]);
			
			new SQLTask() {
				@Override
				public void execute() throws SQLException {

					if (!SQLManager.playerExists(playerName)) {
						sender.sendMessage(ChatColor.RED + "Quel giocatore non ha mai giocato qui!");
						return;
					}
					
					SQLManager.setStat(playerName, SQLColumns.POINTS, points);
					sender.sendMessage(ChatColor.YELLOW + "Sono stati settati " + points + " punti a " + playerName);
					
					new BukkitRunnable() {
						@Override
						public void run() {
							HGamer hGamer = SurvivalGames.getHGamer(playerName);
							if (hGamer != null) {
								hGamer.setPoints(points);
							}
						}
					}.runTask(SurvivalGames.getInstance());
				}
			}.submitAsync(sender);
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Sub-comando sconosciuto. Scrivi /points per una lista dei comandi.");
	}

}
