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
package com.gmail.filoghost.survivalgames.player;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Team {

	private Set<String> members;
	
	public Team() {
		members = new TreeSet<>();
	}
	
	public void add(String member) {
		members.add(member);
	}
	
	public String remove(String member) {
		member = member.toLowerCase();
		Iterator<String> iter = members.iterator();
		while (iter.hasNext()) {
			String next = iter.next();
			if (next.toLowerCase().equals(member)) {
				iter.remove();
				return next;
			}
		}
		
		return null;
	}
	
	public boolean contains(String member) {
		return members.contains(member);
	}
	
	public boolean containsIgnoreCase(String member) {
		member = member.toLowerCase();
		for(String item : members) {
		    if(item.toLowerCase().equals(member)) {
		    	return true;
		    }
		}
		return false;
	}
	
	public int size() {
		return members.size();
	}
	
	public Set<String> getMembersUnsafe() {
		return members;
	}
	
	public void clear() {
		members.clear();
	}
	
	@Override
	public String toString() {
		return members.toString();
	}
}
