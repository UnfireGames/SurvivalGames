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
package com.gmail.filoghost.survivalgames.listener.protection;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.gmail.filoghost.survivalgames.GameState;
import com.gmail.filoghost.survivalgames.Perms;
import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.gmail.filoghost.survivalgames.player.Status;

public class BlockListener implements Listener {

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		if (!canDestroyBlock(event.getPlayer(), event.getBlock().getType())) {
			event.setCancelled(true);
			return;
		}
		
		if (event.getBlock().getType() == Material.LONG_GRASS) {
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		if (event.getBlock().getType() == Material.TNT) {
			if (SurvivalGames.getHGamer(event.getPlayer()).getStatus() == Status.TRIBUTE) {
				event.setCancelled(true);
				if (event.getPlayer().getItemInHand().getAmount() > 1) {
					event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
				} else {
					event.getPlayer().setItemInHand(null);
				}
				event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0.5D, 0.5D, 0.5D), EntityType.PRIMED_TNT);
				return;
			}
		}
		
		if (!canPlaceBlock(event.getPlayer(), event.getBlock().getType())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (!canDestroyBlock(event.getPlayer(), event.getBucket())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		if (!canPlaceBlock(event.getPlayer(), event.getBucket())) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onIgnite(BlockIgniteEvent event) {
		if (SurvivalGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
			return;
		}
		
		if (event.getCause() != IgniteCause.FLINT_AND_STEEL) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBurn(BlockBurnEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onFade(BlockFadeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent event) {
		event.blockList().clear();
	}
	
	public static boolean canDestroyBlock(Player player, Material block) {
		Status status = SurvivalGames.getHGamer(player).getStatus();
		
		switch (status) {
			case GAMEMAKER:
				return player.hasPermission(Perms.INTERACT_GAMEMAKER);

			case TRIBUTE:
				return 	block == Material.RED_MUSHROOM ||
						block == Material.BROWN_MUSHROOM ||
						block == Material.FIRE ||
						block == Material.LEAVES ||
						block == Material.LEAVES_2 ||
						block == Material.VINE ||
						block == Material.CAKE_BLOCK ||
						block == Material.WEB ||
						block == Material.LONG_GRASS;
				
			default:
				return false;
		}
	}
	
	public static boolean canPlaceBlock(Player player, Material block) {
		Status status = SurvivalGames.getHGamer(player).getStatus();
		
		switch (status) {
			case GAMEMAKER:
				return player.hasPermission(Perms.INTERACT_GAMEMAKER);

			case TRIBUTE:
				return 	block == Material.FIRE ||
						block == Material.CAKE_BLOCK ||
						block == Material.WEB;
				
			default:
				return false;
		}
	}
}
