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
package com.gmail.filoghost.survivalgames.files;

import java.util.Arrays;
import java.util.List;

import com.gmail.filoghost.survivalgames.SurvivalGames;

import net.cubespace.yamler.YamlerConfig;

public class Settings extends YamlerConfig {

	public Settings() {
		super(SurvivalGames.getInstance(), "config.yml", "Configurazione del plugin SurvivalGames", "Scritto da filoghost");
	}
	
	public int startCountdown = 300;
	public int gameMinutes = 10;
	public int moveCountdown = 10;
	public int invincibilityCountdown = 10;
	public int minPlayers = 5;
	
	public int winPointsPerTribute = 2;
	
	// Protezione
//	public List<String> allowedBlocks = Arrays.asList("Leaves", "Leaves_2");
	
	public String mapsFolder = "../sg_maps";
	
	// Mondi
	public String difficulty = "hard";
	
	public List<String> spectatorCommandBlacklist = Arrays.asList("/msg", "/m", "/tell", "/t", "/whisper", "/w", "/reply", "/r");
	
	public String mysql_host = "localhost";
	public String mysql_database = "database";
	public String mysql_user = "root";
	public String mysql_pass = "toor";
	public int mysql_port = 3306;
	public String mysql_prefix = "sg_";
	
}
