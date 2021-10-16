package br.com.finalcraft.betterchunkloader;

import br.com.finalcraft.betterchunkloader.commands.CommandRegisterer;
import br.com.finalcraft.betterchunkloader.config.ConfigManager;
import br.com.finalcraft.betterchunkloader.config.data.BCLSettings;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.betterchunkloader.datastore.MySqlDataStore;
import br.com.finalcraft.betterchunkloader.datastore.XmlDataStore;
import br.com.finalcraft.betterchunkloader.listeners.PlayerListener;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerController;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerData;
import br.com.finalcraft.evernifecore.config.uuids.UUIDsController;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class BetterChunkLoader extends JavaPlugin {
	private static BetterChunkLoader instance;
	private static Permission permissions;
	public boolean enabled;
	
	public void onLoad() {
		// Register XML DataStore
		DataStoreManager.registerDataStore("XML", XmlDataStore.class);
		
		// Register MySQL DataStore
		DataStoreManager.registerDataStore("MySQL", MySqlDataStore.class);
	}
	
	public void onEnable() {
		// check if forge is running
		try {
			Class.forName("net.minecraftforge.common.ForgeVersion");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cauldron/KCauldron and BCLForgeLib are needed to run this plugin!");
		}
		
		// check if BCLForgeLib is present
		try {
			Class.forName("net.kaikk.mc.bcl.forgelib.BCLForgeLib");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("BCLForgeLib is needed to run this plugin!");
		}
		
		instance=this;
		
		this.enable();
		PlaceHolderIntegration.initialize();
	}
	
	public void enable() {
		// load vault permissions
		if (!this.enabled) {
			permissions = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
			
			try {
				// load config
				this.getLogger().info("Loading config...");
				ConfigManager.initialize(this);

				// load messages localization
				Messages.load(this, "messages.yml");
				
				// instantiate data store, if needed
				if (DataStoreManager.getDataStore()==null || !DataStoreManager.getDataStore().getName().equals(BCLSettings.dataStore)) {
					DataStoreManager.setDataStoreInstance(BCLSettings.dataStore);
				}
				
				// load datastore
				this.getLogger().info("Loading "+DataStoreManager.getDataStore().getName()+" Data Store...");
				DataStoreManager.getDataStore().load();
				
				this.getLogger().info("Loaded "+DataStoreManager.getDataStore().getChunkLoaders().size()+" chunk loaders data.");
				this.getLogger().info("Loaded "+DataStoreManager.getDataStore().getPlayersData().size()+" players data.");
				
				// load always on chunk loaders
				int count=0;
				for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
					if (cl.isLoadable()) {
						BCLForgeLib.instance().addChunkLoader(cl);
						count++;
					}
				}
				
				this.getLogger().info("Loaded "+count+" always-on chunk loaders.");
				
				this.getLogger().info("Loading Listeners...");
				this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
				if (Bukkit.getPluginManager().isPluginEnabled("BetterRankUp")){
					this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
				}

				CommandRegisterer.registerCommands(this);

				this.getLogger().info("Load complete.");
			} catch (Exception e) {
				e.printStackTrace();
				this.getLogger().warning("Load failed!");
				Bukkit.getPluginManager().disablePlugin(this);
			}
			this.enabled = true;
		}
	}
	
	public void onDisable() {
		this.disable();
	}
	
	public void disable() {
		if (this.enabled) {
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (BCLForgeLib.instance().getChunkLoaders().containsValue(cl)){
					BCLForgeLib.instance().removeChunkLoader(cl);
				}
			}
			this.enabled = false;
		}
	}

	public static BetterChunkLoader instance() {
		return instance;
	}
	
	public static long getPlayerLastPlayed(UUID playerId) {
		PlayerData playerData = PlayerController.getPlayerData(UUIDsController.getNameFromUUID(playerId));
		return playerData != null ? playerData.getLastSeen() : 0;
	}

	public static boolean hasPermission(OfflinePlayer player, String permission) {
		try {
			return permissions.playerHas(null, player, permission);
		} catch (Exception e) {
			return false;
		}
	}
}
