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
package com.gmail.filoghost.survivalgames.generation;

import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ChestFiller {
	
	private static Set<Coords> filledChests = Sets.newHashSet();
	
	private static final int MIN_SLOTS_FILLED = 4;
	
	public static boolean fillChestIfNeeded(Chest chest, boolean tier2) {
		
		Coords coords = Coords.fromBlock(chest.getBlock());
		
		if (filledChests.contains(coords)) {
			return false;
		}
		
		Inventory inv = chest.getBlockInventory();
		inv.clear();
		
		List<Integer> slotsToFill = Lists.newArrayList();
		for (int i = 0; i < inv.getSize(); i++) {
			slotsToFill.add(i);
		}
		
		if (tier2) {
			for (RandomItem randomItem : SurvivalGames.getItemsTier2()) {
				randomItem.distribute(inv, slotsToFill);
			}
			
			int tries = 50;
			while (tries > 0 && getSlotsFilled(inv) < MIN_SLOTS_FILLED) {
				RandomItem additionalItem = SurvivalGames.randomInList(SurvivalGames.getItemsTier2());
				additionalItem.distribute(inv, slotsToFill);
				tries--;
			}
			
		} else {
			for (RandomItem randomItem : SurvivalGames.getItemsTier1()) {
				randomItem.distribute(inv, slotsToFill);
			}
			
			int tries = 50;
			while (tries > 0 && getSlotsFilled(inv) < MIN_SLOTS_FILLED) {
				RandomItem additionalItem = SurvivalGames.randomInList(SurvivalGames.getItemsTier1());
				additionalItem.distribute(inv, slotsToFill);
				tries--;
			}
		}
		
		filledChests.add(coords);
		return true;
	}
	
	private static int getSlotsFilled(Inventory inv) {
		int count = 0;
		
		for (ItemStack item : inv.getContents()) {
			if (item != null && item.getType() != Material.AIR) {
				count++;
			}
		}
		
		return count;
	}
	
	public static void refill(Chest chest) {
		filledChests.remove(Coords.fromBlock(chest.getBlock()));
	}
}
