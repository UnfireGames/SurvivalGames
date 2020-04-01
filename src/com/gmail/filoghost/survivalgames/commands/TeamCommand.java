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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.google.common.base.Joiner;

@Permission("survivalgames.team")
public class TeamCommand extends CommandFramework {

	public TeamCommand() {
		super(SurvivalGames.getInstance(), "team");
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		HGamer hGamer = SurvivalGames.getHGamer(CommandValidate.getPlayerSender(sender));
		
		if (args.length == 0) {
			CommandValidate.isTrue(hGamer.hasTeam() && hGamer.getTeam().size() > 0, "Non hai un team. Usa /team add <giocatore>");
			
			hGamer.sendMessage("");
			hGamer.sendMessage(ChatColor.AQUA + "Il tuo team:");
			hGamer.sendMessage(ChatColor.DARK_AQUA + Joiner.on(ChatColor.WHITE + ", " + ChatColor.DARK_AQUA).join(hGamer.getTeam().getMembersUnsafe()));
			return;
		}
		
		if (args[0].equalsIgnoreCase("add")) {
			
			if (!hGamer.hasTeam()) {
				hGamer.createTeam();
			}
			
			CommandValidate.minLength(args, 2, "Utilizzo comando: /team add <giocatore>");
			HGamer target = SurvivalGames.getHGamer(args[1]);
			CommandValidate.isTrue(target != null && target.getStatus() == Status.TRIBUTE, "Quel giocatore non è online.");
			CommandValidate.isTrue(target != hGamer, "Non puoi aggiungere te stesso.");
			CommandValidate.isTrue(!hGamer.getTeam().containsIgnoreCase(target.getName()), "Quel giocatore è già nel tuo team!");
			
			hGamer.getTeam().add(target.getName());
			hGamer.sendMessage(ChatColor.AQUA + "Hai aggiunto " + target.getName() + " al tuo team.");
			return;
		}
		
		if (args[0].equalsIgnoreCase("remove")) {
			
			CommandValidate.minLength(args, 2, "Utilizzo comando: /team remove <giocatore>");
			CommandValidate.isTrue(hGamer.hasTeam() && hGamer.getTeam().containsIgnoreCase(args[1]), "Quel giocatore non è nel tuo team!");
			
			String caseCorrectedName = hGamer.getTeam().remove(args[1]);
			hGamer.sendMessage(ChatColor.AQUA + "Hai rimosso " + caseCorrectedName + " dal tuo team.");
			return;
		}
		
		
		sender.sendMessage(ChatColor.RED + "Sub-comando sconosciuto. Sub comandi validi: /team add, /team remove.");
	}

}
