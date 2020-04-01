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
package com.gmail.filoghost.survivalgames;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import net.cubespace.yamler.YamlerConfigurationException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.WildCommons;
import wild.api.item.BookTutorial;
import wild.api.menu.Icon;
import wild.api.menu.IconMenu;
import wild.api.translation.Translation;
import wild.api.util.UnitFormatter;

import com.gmail.filoghost.survivalgames.commands.BountyCommand;
import com.gmail.filoghost.survivalgames.commands.ClassificaCommand;
import com.gmail.filoghost.survivalgames.commands.DebugCommand;
import com.gmail.filoghost.survivalgames.commands.FinalbattleCommand;
import com.gmail.filoghost.survivalgames.commands.FixCommand;
import com.gmail.filoghost.survivalgames.commands.GamemakerCommand;
import com.gmail.filoghost.survivalgames.commands.GlobalChatCommand;
import com.gmail.filoghost.survivalgames.commands.MapCommand;
import com.gmail.filoghost.survivalgames.commands.PointsCommand;
import com.gmail.filoghost.survivalgames.commands.SpawnCommand;
import com.gmail.filoghost.survivalgames.commands.SpectatorCommand;
import com.gmail.filoghost.survivalgames.commands.SponsorCommand;
import com.gmail.filoghost.survivalgames.commands.StartCommand;
import com.gmail.filoghost.survivalgames.commands.StatsCommand;
import com.gmail.filoghost.survivalgames.commands.TeamCommand;
import com.gmail.filoghost.survivalgames.files.Chests;
import com.gmail.filoghost.survivalgames.files.HelpFile;
import com.gmail.filoghost.survivalgames.files.PluginConfig;
import com.gmail.filoghost.survivalgames.files.Settings;
import com.gmail.filoghost.survivalgames.files.SponsorFile;
import com.gmail.filoghost.survivalgames.generation.ChestFiller;
import com.gmail.filoghost.survivalgames.generation.RandomItem;
import com.gmail.filoghost.survivalgames.generation.SpawnObjects;
import com.gmail.filoghost.survivalgames.hud.menu.SponsorClickHandler;
import com.gmail.filoghost.survivalgames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.survivalgames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.survivalgames.hud.tags.TagsManager;
import com.gmail.filoghost.survivalgames.listener.BoatFixListener;
import com.gmail.filoghost.survivalgames.listener.ChatListener;
import com.gmail.filoghost.survivalgames.listener.ChestListener;
import com.gmail.filoghost.survivalgames.listener.DeathListener;
import com.gmail.filoghost.survivalgames.listener.InventoryToolsListener;
import com.gmail.filoghost.survivalgames.listener.InvisibleFireFixListener;
import com.gmail.filoghost.survivalgames.listener.JoinQuitListener;
import com.gmail.filoghost.survivalgames.listener.LastDamageCauseListener;
import com.gmail.filoghost.survivalgames.listener.PingListener;
import com.gmail.filoghost.survivalgames.listener.ShortFlintAndSteelListener;
import com.gmail.filoghost.survivalgames.listener.StrengthFixListener;
import com.gmail.filoghost.survivalgames.listener.protection.BlockListener;
import com.gmail.filoghost.survivalgames.listener.protection.CommandListener;
import com.gmail.filoghost.survivalgames.listener.protection.EntityListener;
import com.gmail.filoghost.survivalgames.listener.protection.WeatherListener;
import com.gmail.filoghost.survivalgames.mysql.SQLColumns;
import com.gmail.filoghost.survivalgames.mysql.SQLManager;
import com.gmail.filoghost.survivalgames.mysql.SQLPlayerData;
import com.gmail.filoghost.survivalgames.mysql.SQLTask;
import com.gmail.filoghost.survivalgames.player.HGamer;
import com.gmail.filoghost.survivalgames.player.Status;
import com.gmail.filoghost.survivalgames.timers.CheckWinnerTimer;
import com.gmail.filoghost.survivalgames.timers.CompassUpdateTimer;
import com.gmail.filoghost.survivalgames.timers.EndTimer;
import com.gmail.filoghost.survivalgames.timers.FinalBattleTimer;
import com.gmail.filoghost.survivalgames.timers.GameTimer;
import com.gmail.filoghost.survivalgames.timers.ImmobilityTimer;
import com.gmail.filoghost.survivalgames.timers.InvincibilityTimer;
import com.gmail.filoghost.survivalgames.timers.MySQLKeepAliveTimer;
import com.gmail.filoghost.survivalgames.timers.PregameTimer;
import com.gmail.filoghost.survivalgames.timers.TimerMaster;
import com.gmail.filoghost.survivalgames.timers.WorldBorderTimer;
import com.gmail.filoghost.survivalgames.utils.DayPhase;
import com.gmail.filoghost.survivalgames.utils.EnchantmentData;
import com.gmail.filoghost.survivalgames.utils.Parser;
import com.gmail.filoghost.survivalgames.utils.ParserException;
import com.gmail.filoghost.survivalgames.utils.SponsorData;
import com.gmail.filoghost.survivalgames.utils.UnitUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SurvivalGames extends JavaPlugin {

	@Getter private static 				SurvivalGames instance;
	@Getter private static 				Settings settings;
	private static 						Chests chestsConfig;
	@Getter	private static 				GameState state;
	private static 						Set<String> spectatorCommandBlacklist;
	private static						boolean onLoadSuccessful;
	
	// Tutti i timer
	@Getter private static 				PregameTimer pregameTimer;
	@Getter private static 				TimerMaster immobilityTimer;
	@Getter private static 				TimerMaster invincibilityTimer;
	@Getter private static 				GameTimer gameTimer;
	@Getter private static 				TimerMaster finalBattleTimer;
	@Getter private static 				WorldBorderTimer worldBorderTimer;
	@Getter private static 				TimerMaster checkWinnerTimer;
	@Getter private static 				EndTimer endTimer;
	
	private static 						World world;
	@Getter private static 				int worldBorderLimit;
	
	@Getter private static 				Random randomGenerator;
	
	public static 						Map<Player, HGamer> players;
	@Getter private static				List<RandomItem> itemsTier1;
	@Getter private static				List<RandomItem> itemsTier2;
	@Getter private static				Location highestSpawn;
	
	@Getter private static				BookTutorial bookTutorial;
	
	@Getter private static 				boolean wildChat;
	
	@Getter @Setter private static int	pointsForWin;
	@Getter private static IconMenu		sponsorMenu;
	
	
	@Getter private String mapName;
	private DayPhase dayPhase;
	private boolean rain, thunder;

	
	@Override
	public void onLoad() {
		// Prima di tutto
		instance = this;
		randomGenerator = new Random();
		
		// Configurazione
		try {
			settings = new Settings();
			settings.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("config.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		try {
			chestsConfig = new Chests();
			chestsConfig.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("chests.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		// Mappe
		File mapsFolder = new File(settings.mapsFolder);
		
		if (!mapsFolder.isDirectory()) {
			logPurple("Cartella mappe (" + mapsFolder.getAbsolutePath() + ") non trovata, utilizzando quella di default!");
			mapsFolder = new File(getDataFolder(), "maps");
		} else {
			try {
				getLogger().info("Trovata cartella mappe: " + mapsFolder.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!mapsFolder.isDirectory()) mapsFolder.mkdirs();
		
		File mapsConfigFile = new File(mapsFolder, "maps.yml");
		PluginConfig mapsConfig = new PluginConfig(this, mapsConfigFile);
		try {
			mapsConfig.load();
		} catch (Exception ex) {
			ex.printStackTrace();
			logPurple("maps.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		
		logAqua("Cancellazione mondo vecchio.");
		Bukkit.getServer().unloadWorld("world", false);
		try {
			FileUtils.deleteDirectory(new File("world"));
		} catch (IOException e) {
			e.printStackTrace();
			logPurple("Impossibile cancellare il vecchio mondo!");
		}
		
		List<MapInfo> mapInfoList = Lists.newArrayList();
		Set<String> mapFoldersNames = mapsConfig.getKeys(false);
		
		if (mapFoldersNames == null || mapFoldersNames.isEmpty()) {
			logPurple("Nessuna mappa trovata in maps.yml! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			return;
		}
		
		
		for (String mapFolderName : mapFoldersNames) {
			ConfigurationSection mapSection = mapsConfig.getConfigurationSection(mapFolderName);
			String name = mapSection.getString("name");
			if (name == null) {
				name = mapFolderName;
			}
			int borderRadius = mapSection.getInt("radius");
			String time = mapSection.getString("time");
			boolean rain = mapSection.getBoolean("rain", false);
			boolean thunder = mapSection.getBoolean("thunder", false);
				
			if (borderRadius <= 0) {
				logPurple("Dimensione bordo non valide: " + borderRadius);
				continue;
			}
				
			File mapFolder = new File(mapsFolder, mapFolderName);
			if (!mapFolder.isDirectory()) {
				logPurple("Cartella mappa non trovata: " + mapFolderName);
				continue;
			}
			
			DayPhase dayPhase = null;
			if (time != null) {
				try {
					dayPhase = DayPhase.valueOf(time.trim().toUpperCase());
				} catch (IllegalArgumentException e) {
					logPurple("Fase del giorno non valida (verrà messo giorno): " + time);
				}
			}
			
			if (dayPhase == null) {
				dayPhase = DayPhase.DAY;
			}
			
			MapInfo mapInfo = new MapInfo(mapFolder, name, borderRadius);
			mapInfo.setDayPhase(dayPhase);
			mapInfo.setRain(rain);
			mapInfo.setThunder(thunder);
			
			mapInfoList.add(mapInfo);
		}
			
		if (mapInfoList.isEmpty()) {
			logPurple("Nessuna mappa valida trovata!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		logAqua("Trovate " + mapInfoList.size() + " mappe valide.");
				
		MapInfo randomMap = mapInfoList.get(randomGenerator.nextInt(mapInfoList.size()));
		mapName = randomMap.getName();
		worldBorderLimit = randomMap.getBorder();
		dayPhase = randomMap.getDayPhase();
		rain = randomMap.isRain();
		thunder = randomMap.isThunder();
		logAqua("Caricamento mappa casuale '" + mapName + "', bordo: " + worldBorderLimit + ".");
			
		try {
			FileUtils.copyDirectory(randomMap.getFolder(), new File("world"), false);
		} catch (IOException e) {
			e.printStackTrace();
			logPurple("Impossibile copiare la mappa salvata!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		onLoadSuccessful = true;
	}
	
	@Override
	public void onEnable() {
		if (!onLoadSuccessful) {
			return;
		}
		
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("WildChat")) {
			wildChat = true;
		}
		
		// File di aiuto
		try {
			new HelpFile().init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("help.yml non caricato!");
		}
		
		// File di aiuto
		try {
			SponsorFile sponsorFile = new SponsorFile();
			sponsorFile.load();
			
			List<SponsorData> sponsorItemsList = Lists.newArrayList();
			
			for (Object object : sponsorFile.getList("items")) {
				if (object instanceof Map<?, ?>) {
					
					Map<?, ?> sponsorItem = (Map<?, ?>) object;
					
					if (!sponsorItem.containsKey("id") || sponsorItem.get("id") instanceof String == false) {
						continue;
					}
					
					ItemStack item;
					try {
						item = Parser.ItemStacks.parse((String) sponsorItem.get("id"));
					} catch (ParserException e) {
						logPurple("Oggetto non valido! id: " + sponsorItem.get("id"));
						continue;
					}
					
					int price = 999999;
					if (sponsorItem.containsKey("price") && sponsorItem.get("price") instanceof Integer) {
						price = (Integer) sponsorItem.get("price");
					}
					
					String name = "???";
					if (sponsorItem.containsKey("name") && sponsorItem.get("name") instanceof String) {
						name = (String) sponsorItem.get("name");
					}
					
					if (sponsorItem.containsKey("enchants") && sponsorItem.get("enchants") instanceof Map) {
						Map<?, ?> enchantsMap = (Map<?, ?>) sponsorItem.get("enchants");
						for (Entry<?, ?> entry : enchantsMap.entrySet()) {
							if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
								try {
									EnchantmentData enchantmentData = Parser.Enchantments.parse(entry.getKey() + ":" + entry.getValue());
									item.addUnsafeEnchantment(enchantmentData.getEnchant(), enchantmentData.getLevel());
									
								} catch (ParserException ex) {
									throw new ParserException("Incantesimo non valido (" + ex.getMessage() + "): " + entry.getKey() + ": " + entry.getValue());
								}
							}
						}
					}
					
					sponsorItemsList.add(new SponsorData(name, price, item));
				}
			}
			
			sponsorMenu = new IconMenu("Sponsor", UnitUtils.roundToIconMenuRows(sponsorItemsList.size()) / 9);
			
			int index = 0;
			for (SponsorData sponsorData : sponsorItemsList) {
				
				Icon icon = new Icon(sponsorData.getItem().getType());
				icon.setDataValue(sponsorData.getItem().getDurability());
				icon.setAmount(sponsorData.getItem().getAmount());
				icon.setName(ChatColor.GRAY + WildCommons.color(sponsorData.getName()));
				
				List<String> lore = Lists.newArrayList();
				if (!sponsorData.getItem().getEnchantments().isEmpty()) {
					icon.addEnchantment(Enchantment.DURABILITY); // Glow
					for (Entry<Enchantment, Integer> entry : sponsorData.getItem().getEnchantments().entrySet()) {
						lore.add(ChatColor.BLUE + Translation.of(entry.getKey()) + " " + UnitFormatter.getRoman(entry.getValue()));
					}
				}
				lore.add("");
				lore.add(ChatColor.GOLD + "Prezzo: " + sponsorData.getPrice() + " punti");
				
				icon.setLore(lore);
				icon.setClickHandler(new SponsorClickHandler(sponsorData));
				icon.setCloseOnClick(true);
				
				sponsorMenu.setIconRaw(index, icon);
				index++;
			}
			
			sponsorMenu.refresh();
				
		} catch (Exception e) {
			e.printStackTrace();
			logPurple("sponsor.yml non caricato!");
			sponsorMenu = new IconMenu("Errore nel caricamento", 1);
		}
		
		// Database MySQL
		try {
			SQLManager.connect(settings.mysql_host, settings.mysql_port, settings.mysql_database, settings.mysql_user, settings.mysql_pass, settings.mysql_prefix);
			SQLManager.checkConnection();
					
			SQLManager.getMysql().update("CREATE TABLE IF NOT EXISTS " + SQLManager.prefix + "players ("
					+ SQLColumns.NAME + " varchar(20) NOT NULL ,"
					+ SQLColumns.KILLS + " MEDIUMINT unsigned NOT NULL, "
					+ SQLColumns.DEATHS + " MEDIUMINT unsigned NOT NULL, "
					+ SQLColumns.WINS + " MEDIUMINT unsigned NOT NULL, "
					+ SQLColumns.POINTS + " MEDIUMINT unsigned NOT NULL"
					+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
					
		} catch (Exception ex) {
			ex.printStackTrace();
			logPurple("Impossibile connettersi al database! Il server verrà spento in 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
				
		// Variabili
		world = Bukkit.getWorld("world");
		if (world == null) {
			logPurple("Impossibile trovare il mondo principale! Il server verrà spento in 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		highestSpawn = world.getSpawnLocation().add(0.5, 0, 0.5);
		
		state = GameState.PRE_GAME;
		spectatorCommandBlacklist = Sets.newHashSet();
		
		players = Maps.newConcurrentMap();
		
		itemsTier1 = Lists.newArrayList();
		itemsTier2 = Lists.newArrayList();
		for (String randomItemString : chestsConfig.tier1) {
			try {
				itemsTier1.add(Parser.RandomItems.parse(randomItemString));
			} catch (ParserException e) {
				logPurple("Oggetto random non valido (" + e.getMessage() + "): " + randomItemString);
			}
		}
		for (String randomItemString : chestsConfig.tier2) {
			try {
				itemsTier2.add(Parser.RandomItems.parse(randomItemString));
			} catch (ParserException e) {
				logPurple("Oggetto random non valido (" + e.getMessage() + "): " + randomItemString);
			}
		}
		
		// Lettura items
		bookTutorial = new BookTutorial(this, ChatColor.GREEN + "Tutorial", "Wild Adventure");
		
		// Comandi bloccati
		for (String blacklistedCommand : settings.spectatorCommandBlacklist) {
			spectatorCommandBlacklist.add(blacklistedCommand.toLowerCase());
		}
		
		// Piattaforme e casse
		SpawnObjects.load(highestSpawn, 8);
		try {
			setMaxPlayers(SpawnObjects.getPlatforms().size());
		} catch (Exception e) {
			e.printStackTrace();
			logPurple("Impossibile alterare il numero max di giocatori");
		}
		
		if (SpawnObjects.getPlatforms().size() == 0) {
			logPurple("Nessuna piattaforma trovata!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}

		// Teleporter
		TeleporterMenu.load();
		
		// Sidebar & teams
		SidebarManager.initialize(state);
		TagsManager.initialize();

		
		
		// Impostazioni del mondo
		try {
			world.setDifficulty(Difficulty.valueOf(settings.difficulty.toUpperCase()));
		} catch (IllegalArgumentException e) {
			world.setDifficulty(Difficulty.HARD);
			logPurple("Difficoltà non valida. Default: hard");
		}
		world.setPVP(true);
		world.setSpawnFlags(true, true);
		world.setStorm(rain);
		world.setThundering(thunder);
		world.setKeepSpawnInMemory(true);
		world.setGameRuleValue("doFireTick", "true");
		world.setGameRuleValue("doMobLoot", "true");
		world.setGameRuleValue("doMobSpawning", "true");
		world.setGameRuleValue("doTileDrops", "true");
		world.setGameRuleValue("keepInventory", "false");
		world.setGameRuleValue("mobGriefing", "true");
		world.setGameRuleValue("naturalRegeneration", "true");
		world.setGameRuleValue("doDaylightCycle", "false");
		
		// world.setAutoSave(false);

		world.setTime(dayPhase.getTimeOfDay());
		
		for (Chunk chunk : world.getLoadedChunks()) {
			for (Entity entity : chunk.getEntities()) {
				if (entity.getType() != EntityType.PLAYER && entity instanceof LivingEntity) {
					entity.remove();
				}
			}
		}
		
		
		// Comandi
		new StartCommand();
		new DebugCommand();
		new FixCommand();
		new GamemakerCommand();
		new SpectatorCommand();
		new StatsCommand();
		new SpawnCommand();
		new FinalbattleCommand();
		new TeamCommand();
		new ClassificaCommand();
		new SponsorCommand();
		new BountyCommand();
		new PointsCommand();
		new MapCommand();
		new GlobalChatCommand();
		
		// Riempe le casse
		for (Chest chest : SpawnObjects.getSpawnChests()) {
			ChestFiller.fillChestIfNeeded(chest, true);
		}
		
		// Timer iniziali
		immobilityTimer = new ImmobilityTimer();
		invincibilityTimer = new InvincibilityTimer();
		gameTimer = new GameTimer();
		finalBattleTimer = new FinalBattleTimer();
		endTimer = new EndTimer();
		(worldBorderTimer = new WorldBorderTimer(world, worldBorderLimit)).startNewTask();
		(pregameTimer = new PregameTimer()).startNewTask();
		(checkWinnerTimer = new CheckWinnerTimer()).startNewTask();
		new CompassUpdateTimer().startNewTask();
		new MySQLKeepAliveTimer().startNewTask();
		
		// Listeners
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChestListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryToolsListener(), this);
		Bukkit.getPluginManager().registerEvents(new PingListener(), this);
		Bukkit.getPluginManager().registerEvents(new WeatherListener(), this); // Blocca il meteo
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
		Bukkit.getPluginManager().registerEvents(new LastDamageCauseListener(), this);
		Bukkit.getPluginManager().registerEvents(new ShortFlintAndSteelListener(), this);
		
		// Tutti i fix
		Bukkit.getPluginManager().registerEvents(new BoatFixListener(), this);
		Bukkit.getPluginManager().registerEvents(new StrengthFixListener(), this);
		Bukkit.getPluginManager().registerEvents(new InvisibleFireFixListener(), this);
	}
	
	public static HGamer registerHGamer(Player bukkitPlayer, Status status) {
		HGamer hGamer = new HGamer(bukkitPlayer, status);
		players.put(bukkitPlayer, hGamer);
		return hGamer;
	}
	
	public static HGamer unregisterHGamer(Player bukkitPlayer) {
		return players.remove(bukkitPlayer);
	}
	
	public static HGamer getHGamer(String name) {
		name = name.toLowerCase();
		for (HGamer hGamer : players.values()) {
			if (hGamer.getName().toLowerCase().equals(name)) {
				return hGamer;
			}
		}
		return null;
	}
	
	public static HGamer getHGamer(Player bukkitPlayer) {
		if (bukkitPlayer == null) {
			return null;
		}
		return players.get(bukkitPlayer);
	}
	
	public static void setState(GameState state) {
		SurvivalGames.state = state;
		SidebarManager.updateState(state);
	}
	
	public static void checkWinner() {
		if (state == GameState.GAME || state == GameState.FINAL_BATTLE) {
			
			HGamer winner = null;
			for (HGamer hGamer : players.values()) {
				
				if (hGamer.getStatus() == Status.TRIBUTE) {
					
					if (winner == null) {
						winner = hGamer;
					} else {
						return; // Già settato quindi sono almeno 2
					}
				}
			}
			
			setState(GameState.END);
			SidebarManager.setTime("-");
			
			gameTimer.stopTask();
			finalBattleTimer.stopTask();
			
			if (winner == null) {
				
				logAqua("Nessun vincitore!");
				stopServer(ChatColor.RED + "Non c'è stato nessun vincitore, riavvio del server.");
				
			} else {
				
				doWin(winner);
			}
		}
	}
	
	public static void doWin(HGamer winner) {
		for (HGamer other : SurvivalGames.getAllGamersUnsafe()) {
			if (other != winner) {
				other.sendMessage(ChatColor.GOLD + winner.getName() + " ha vinto la partita!");
			}
		}
		
		final int bounty = winner.getBounty();
		winner.setBounty(0);
		
		winner.sendMessage("");
		winner.sendMessage(ChatColor.GOLD + "+ " + (pointsForWin + bounty) + " punti");
		winner.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "HAI VINTO LA PARTITA!");
		winner.getPlayer().setGameMode(GameMode.CREATIVE);
		
		final HGamer winnerFinal = winner;
		
		new SQLTask() {
			@Override
			public void execute() throws SQLException {
				
				SurvivalGames.logAqua("Salvando vittorie (+ 1) e punti (+ " + (pointsForWin + bounty) + ") di " + winnerFinal.getName());

				SQLPlayerData previous = SQLManager.getPlayerData(winnerFinal.getName());
				SurvivalGames.logAqua("Punti attuali di " + winnerFinal.getName() + ": " + previous.getPoints());
				SurvivalGames.logAqua("Vittorie attuali di " + winnerFinal.getName() + ": " + previous.getWins());
				
				SQLManager.increaseStat(winnerFinal.getName(), SQLColumns.WINS, 1);
				SQLManager.increaseStat(winnerFinal.getName(), SQLColumns.POINTS, (pointsForWin + bounty));
				
				SurvivalGames.logAqua("Salvate statistiche di " + winnerFinal.getName());
				
				SQLPlayerData next = SQLManager.getPlayerData(winnerFinal.getName());
				//int newPoints = SQLManager.getStat(winnerFinal.getName(), SQLColumns.POINTS);
				//winnerFinal.setPoints(newPoints);
				winnerFinal.setPoints(next.getPoints());
				SurvivalGames.logAqua("Nuovo punteggio: " + next.getPoints());
				SurvivalGames.logAqua("Nuove vittorie: " + next.getWins());
			}
		}.submitAsync(winner.getPlayer());
		
		logAqua("Vincitore: " + winner.getName());
		endTimer.setWinnerAndStart(winner.getPlayer());
	}
	
	public static void stopServer(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.kickPlayer(message + "§0§0§0");
		}
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
			
			@Override
			public void run() {
				Bukkit.shutdown();
			}
		}, 20L);
		
	}
	
	public static Collection<HGamer> getAllGamersUnsafe() {
		return players.values();
	}
	
	public static Collection<Player> getByStatus(Status status) {
		Set<Player> match = Sets.newHashSet();
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getStatus() == status) {
				match.add(hGamer.getPlayer());
			}
		}
		
		return match;
	}
	
	public static int countTributes() {
		int count = 0;
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getStatus() == Status.TRIBUTE) {
				count++;
			}
		}
		
		return count;
	}
	
	public static Collection<HGamer> getNearTributes(Player nearWho, double distance) {
		return getNearTributes(nearWho.getLocation(), distance, nearWho);
	}
	
	public static Collection<HGamer> getNearTributes(Location loc, double distance, Player excluded) {
		
		double distanceSquared = distance * distance;
		Set<HGamer> near = Sets.newHashSet();
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getPlayer() != excluded && hGamer.getStatus() == Status.TRIBUTE && hGamer.getPlayer().getLocation().distanceSquared(loc) <= distanceSquared) {
				near.add(hGamer);
			}
		}
		
		return near;
	}

	// Scritte di errore
	public static void logPurple(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + log);
	}
	
	// Scritte normali
	public static void logAqua(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + log);
	}

	public static boolean isSpectatorBlacklistCommand(String command) {
		return spectatorCommandBlacklist.contains(command.toLowerCase());
	}

	private static void setMaxPlayers(int max) throws Exception {
		Field playerListField = Bukkit.getServer().getClass().getDeclaredField("playerList");
		playerListField.setAccessible(true);
		Object playerList = playerListField.get(Bukkit.getServer());
		Field maxPlayersField = playerList.getClass().getSuperclass().getDeclaredField("maxPlayers");
		maxPlayersField.setAccessible(true);
		maxPlayersField.set(playerList, max);
	}
	
	public static <T> T randomInList(List<T> list) {
		return list.get(randomGenerator.nextInt(list.size()));
	}
	
}
