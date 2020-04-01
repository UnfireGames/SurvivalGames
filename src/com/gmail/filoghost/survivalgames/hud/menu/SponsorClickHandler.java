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
package com.gmail.filoghost.survivalgames.hud.menu;

import java.sql.SQLException;

import lombok.AllArgsConstructor;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import wild.api.menu.ClickHandler;
import wild.api.sound.EasySound;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.commands.SponsorCommand;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLPlayerData;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.utils.SponsorData;

@AllArgsConstructor
public class SponsorClickHandler implements ClickHandler {

	private SponsorData data;

	@Override
	public void onClick(final Player player) {
		final HGamer hGamer = SurvivalGames.getHGamer(player);
		
		String name = SponsorCommand.getSelectedTribute().get(player.getName());
		if (name == null) {
			player.sendMessage(ChatColor.RED + "Errore interno, contatta lo staff.");
			return;
		}
		
		if (name.isEmpty()) {
			player.sendMessage(ChatColor.RED + "Scegli un giocatore con /sponsor <tributo>");
			return;
		}
		
		if (hGamer.getStatus() == Status.TRIBUTE) {
			player.sendMessage(ChatColor.RED + "Non puoi usare gli sponsor mentre sei un tributo.");
			return;
		}
		
		if (SurvivalGames.getState() != GameState.GAME) {
			player.sendMessage(ChatColor.RED + "Puoi usarlo solo durante la partita.");
			return;
		}
		
		final HGamer sponsored = SurvivalGames.getHGamer(name);
		if (sponsored == null || sponsored.getStatus() != Status.TRIBUTE) {
			player.sendMessage(ChatColor.RED + "Quel giocatore non è più in partita.");
			return;
		}
		
		new SQLTask() {
			@Override
			public void execute() throws SQLException {

				final SQLPlayerData playerData = SQLManager.getPlayerData(player.getName());
				
				if (playerData.getKills() < 5) {
					player.sendMessage(ChatColor.RED + "Devi avere almeno 5 uccisioni totali per usare gli sponsor.");
					player.sendMessage(ChatColor.RED + "Ti mancano ancora " + (5 - playerData.getKills()) + " uccisioni.");
					return;
				}
				
				if (playerData.getPoints() < data.getPrice()) {
					player.sendMessage(ChatColor.RED + "Non hai abbastanza punti!");
					return;
				}
				
				SQLManager.decreaseStat(player.getName(), SQLColumns.POINTS, data.getPrice());
				hGamer.setPoints(playerData.getPoints() - data.getPrice());
				
				new BukkitRunnable() {
					@Override
					public void run() {
						if (sponsored.getStatus() == Status.TRIBUTE && SurvivalGames.getState() == GameState.GAME) {
							sponsored.getPlayer().getInventory().addItem(data.getItem());
							EasySound.quickPlay(sponsored.getPlayer(), Sound.LEVEL_UP, 1.5F);
							EasySound.quickPlay(hGamer.getPlayer(), Sound.LEVEL_UP, 1.5F);
							sponsored.sendMessage(ChatColor.DARK_AQUA + "Hai ricevuto " + ChatColor.GRAY + data.getName() + ChatColor.DARK_AQUA + " da " + player.getName() + "!");
						}
						
						player.sendMessage(ChatColor.DARK_AQUA + "Hai dato " + data.getName() + ChatColor.DARK_AQUA + " a " + sponsored.getName() + " per " + ChatColor.GOLD + data.getPrice() + " punti.");
						player.sendMessage(ChatColor.DARK_AQUA + "Ora hai " + (playerData.getPoints() - data.getPrice()) + " punti.");
					}
				}.runTask(SurvivalGames.getInstance());
				
			}
		}.submitAsync(player);
	}
	
	
	
}
