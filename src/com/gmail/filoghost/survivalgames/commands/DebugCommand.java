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

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Team;
import com.google.common.collect.Sets;

@Permission("survivalgames.debug")
public class DebugCommand extends CommandFramework {

	public DebugCommand() {
		super(SurvivalGames.getInstance(), "debug");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		if (args.length > 0 && args[0].equalsIgnoreCase("gc")) {
			System.gc();
			sender.sendMessage("Garbage collected!");
			return;
		}
		
		Set<Team> teams = Sets.newHashSet();
		for (HGamer hGamer : SurvivalGames.getAllGamersUnsafe()) {
			if (hGamer.hasTeam()) {
				teams.add(hGamer.getTeam());
			}
		}
		
		sender.sendMessage(ChatColor.DARK_PURPLE + "===== Timer =====");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Check winner timer: " + formatBoolean(SurvivalGames.getCheckWinnerTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "World border timer: " + formatBoolean(SurvivalGames.getWorldBorderTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Pregame timer: " + formatBoolean(SurvivalGames.getPregameTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Immobility timer: " + formatBoolean(SurvivalGames.getImmobilityTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Invincibility timer: " + formatBoolean(SurvivalGames.getInvincibilityTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Game timer: " + formatBoolean(SurvivalGames.getGameTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Final battle timer: " + formatBoolean(SurvivalGames.getFinalBattleTimer().isStarted()));
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "End timer: " + formatBoolean(SurvivalGames.getEndTimer().isStarted()));
		sender.sendMessage("");
		sender.sendMessage(ChatColor.DARK_PURPLE + "===== Collections =====");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "SurvivalGames.players size: " + SurvivalGames.players.size());
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "WorldBorderTimer.lastSafeLocation size: " + SurvivalGames.getWorldBorderTimer().lastSafeLocation.size());
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Teams: " + teams.toString());
	}
	
	private String formatBoolean(boolean b) {
		return b ? ChatColor.GREEN + "attivo" : ChatColor.RED + "non attivo";
	}

}
