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

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.survivalgames.SurvivalGames;
import com.google.common.collect.Lists;

@Setter @Getter
@ToString
public class RandomItem {

	private ItemStack item;
	private int chanceToSpawn;
	private int amountMin, amountMax;
	private boolean sameSlot;
	
	public RandomItem(ItemStack item) {
		this.item = item;
		sameSlot = false;
	}
	
	public void distribute(Inventory inv, List<Integer> slotsToFill) {
		
		int amount = SurvivalGames.getRandomGenerator().nextInt(amountMax - amountMin + 1) + amountMin;
		
		if (amount == 0) {
			return;
		}
		
		if (SurvivalGames.getRandomGenerator().nextInt(100) + 1 > chanceToSpawn) {
			return;
		}
		
		if (sameSlot) {
			
			Collections.shuffle(slotsToFill);
			for (Integer slotNumber : slotsToFill) {
				
				ItemStack previous = inv.getItem(slotNumber);
				if (previous == null || previous.getType() == Material.AIR) {
					ItemStack copy = item.clone();
					copy.setAmount(amount);
					inv.setItem(slotNumber, copy);
					return;
				}
			}
			
		} else {
			
			List<Integer> validSlotsToFill = Lists.newArrayList();
			for (Integer slotNumber : slotsToFill) {
				if (canStackOn(inv.getItem(slotNumber))) {
					validSlotsToFill.add(slotNumber);
				}
			}

			int tries = 0;
			
			while (amount > 0 && tries < 1000) {
			
				Collections.shuffle(validSlotsToFill);
				
				for (Integer slotNumber : validSlotsToFill) {
					
					if (amount <= 0) break;
					
					ItemStack previous = inv.getItem(slotNumber);
					
					if (previous == null || previous.getType() == Material.AIR) {
						ItemStack copy = item.clone();
						copy.setAmount(1);
						inv.setItem(slotNumber, copy);
						amount--;
						continue;
					}
					
					if (previous.getAmount() == previous.getMaxStackSize()) {
						continue;
					}
					
					if (previous.isSimilar(item)) {
						previous.setAmount(previous.getAmount() + 1);
						amount--;
					}
				}
				
				tries++;
			}
		}

	}
	
	private boolean canStackOn(ItemStack other) {
		if (other == null || other.getType() == Material.AIR) {
			return true;
		}
		
		if (other.getAmount() >= other.getMaxStackSize()) {
			return false;
		}
		
		return other.isSimilar(item);
	}
	
}
