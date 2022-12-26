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
import br.com.finalcraft.evernifecore.ecplugin.annotations.ECPlugin;
import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.evernifecore.logger.ECLogger;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@ECPlugin(
		bstatsID = "17189"
)
public class BetterChunkLoader extends JavaPlugin {
	private static BetterChunkLoader instance;
	private static ECLogger ecLogger;

	public static ECLogger getLog() {
		return ecLogger;
	}

	@Override
	public void onEnable() {
		instance = this;
		ecLogger = new ECLogger(this);

		if (!FCBukkitUtil.isModLoaded("BCLForgeLib")){
			throw new RuntimeException("Cauldron/KCauldron and BCLForgeLib are needed to run this plugin!");
		}

		// Register XML DataStore
		DataStoreManager.registerDataStore("XML", XmlDataStore.class);

		// Register MySQL DataStore
		DataStoreManager.registerDataStore("MySQL", MySqlDataStore.class);

		this.onReload();

		getLog().info("Registering Listeners...");
		ECListener.register(this, EventListener.class);
		ECListener.register(this, PlayerListener.class);

		getLog().info("Registering Commands...");
		CommandRegisterer.registerCommands(this);

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
			PlaceHolderIntegration.initialize(this);
		}
	}

	@ECPlugin.Reload
	public void onReload(){
		onDisable();//First thing, remove all existing enabled chunk loaders

		getLog().info("Loading Configuration...");
		ConfigManager.initialize(this);

		// load part of localization
		Messages.load(this, "messages.yml");

		getLog().info("Instantiating Database...");
		// instantiate data store, if needed
		if (DataStoreManager.getDataStore()==null || !DataStoreManager.getDataStore().getName().equals(BCLSettings.dataStore)) {
			DataStoreManager.setDataStoreInstance(BCLSettings.dataStore);
		}

		getLog().info("Loading [%s] DataStore...", DataStoreManager.getDataStore().getName());
		DataStoreManager.getDataStore().load();

		getLog().info("Loaded %s chunk loaders data!", DataStoreManager.getDataStore().getChunkLoaders().size());
		getLog().info("Loaded %s players data!", DataStoreManager.getDataStore().getPlayersData().size());

		long loadedChunks = DataStoreManager.getDataStore().getChunkLoaders().stream()
				.filter(CChunkLoader::isLoadable)
				.peek(chunkLoader -> BCLForgeLib.instance().addChunkLoader(chunkLoader))
				.count();

		getLog().info("Loaded %s always-on chunk loaders!", loadedChunks);
	}

	@Override
	public void onDisable() {
		if (DataStoreManager.getDataStore() != null){
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (BCLForgeLib.instance().getChunkLoaders().containsValue(cl)){
					BCLForgeLib.instance().removeChunkLoader(cl);
				}
			}
		}
	}

	public static BetterChunkLoader instance() {
		return instance;
	}

	public static long getPlayerLastPlayed(UUID playerId) {
		PlayerData playerData = PlayerController.getPlayerData(UUIDsController.getNameFromUUID(playerId));
		return playerData != null ? playerData.getLastSeen() : 0;
	}

}
