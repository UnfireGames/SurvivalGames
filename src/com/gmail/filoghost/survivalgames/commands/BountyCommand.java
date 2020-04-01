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
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.command.CommandFramework;
import wild.api.sound.EasySound;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLPlayerData;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;

public class BountyCommand extends CommandFramework {

	public BountyCommand() {
		super(SurvivalGames.getInstance(), "bounty", "taglia");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		CommandValidate.minLength(args, 2, "Utilizzo comando: /bounty <tributo> <punti>");
		
		final HGamer hGamer = SurvivalGames.getHGamer(player);
		final HGamer bountyGamer = SurvivalGames.getHGamer(args[0]);
		
		CommandValidate.isTrue(bountyGamer != null && bountyGamer.getStatus() == Status.TRIBUTE, "Quel giocatore non è in partita o non è online.");
		CommandValidate.isTrue(hGamer != bountyGamer, "Non puoi mettere una taglia su te stesso.");
		
		CommandValidate.isTrue(hGamer.getStatus() != Status.TRIBUTE, "Non puoi farlo mentre sei un tributo.");
		final int bounty = CommandValidate.getPositiveIntegerNotZero(args[1]);
		
		CommandValidate.isTrue(bounty >= 10, "Il minimo di taglia che puoi mettere è 10.");
		
		new SQLTask() {
			@Override
			public void execute() throws SQLException {
				
				SQLPlayerData playerData = SQLManager.getPlayerData(hGamer.getName());
				
				if (playerData.getKills() < 5) {
					hGamer.sendMessage(ChatColor.RED + "Devi avere almeno 5 uccisioni totali per mettere una taglia.");
					hGamer.sendMessage(ChatColor.RED + "Ti mancano ancora " + (5 - playerData.getKills()) + " uccisioni.");
					return;
				}
				
				if (playerData.getPoints() < bounty) {
					hGamer.sendMessage(ChatColor.RED + "Non possiedi abbastanza punti.");
					return;
				}
				
				SQLManager.decreaseStat(hGamer.getName(), SQLColumns.POINTS, bounty);
				hGamer.setPoints(playerData.getPoints() - bounty);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						if (bountyGamer.getStatus() == Status.TRIBUTE) {
							bountyGamer.setBounty(bountyGamer.getBounty() + bounty);
						}
						EasySound.quickPlay(bountyGamer.getPlayer(), Sound.NOTE_STICKS, 0.8F);
						EasySound.quickPlay(hGamer.getPlayer(), Sound.NOTE_STICKS, 0.8F);
						Bukkit.broadcastMessage(ChatColor.DARK_AQUA + hGamer.getName() + " ha messo una taglia di " + ChatColor.GOLD + bounty + " punti" + ChatColor.DARK_AQUA + " su " + bountyGamer.getName() + ".");
					}
				}.runTask(SurvivalGames.getInstance());
				
			}
		}.submitAsync(player);
	}

}
