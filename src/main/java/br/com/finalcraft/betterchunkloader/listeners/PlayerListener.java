package br.com.finalcraft.betterchunkloader.listeners;

import br.com.finalcraft.betterchunkloader.BetterChunkLoader;
import br.com.finalcraft.betterchunkloader.config.data.ChunksByRank;
import br.com.finalcraft.betterchunkloader.config.data.RankLimiter;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.betterchunkloader.datastore.BCLPlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerJoinEvent event){

		if (ChunksByRank.enableRankLimit == false){
			return;
		}

		final Player player = event.getPlayer();
		new BukkitRunnable(){
			@Override
			public void run() {
				if (player.isOnline()){

					RankLimiter rankLimiter = ChunksByRank.getPlayerLimit(player);

					if (rankLimiter != null){
						BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());

						int currentOnlineOnly = playerData.getOnlineOnlyChunksAmount();
						int currentAlwaysOn = playerData.getAlwaysOnChunksAmount();

						if (currentOnlineOnly != rankLimiter.getOnlineOnly()){
							DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), rankLimiter.getOnlineOnly());
							BetterChunkLoader.instance().getLogger().info("Fixing [OnlineOnly Chunks]' amount of " + player.getName() + " from " + currentOnlineOnly + " to " + rankLimiter.getOnlineOnly());
						}

						if (currentAlwaysOn != rankLimiter.getAlwaysOn()){
							DataStoreManager.getDataStore().setAlwaysOnChunksLimit(player.getUniqueId(), rankLimiter.getAlwaysOn());
							BetterChunkLoader.instance().getLogger().info("Fixing [AlwaysOn Chunks]' amount of " + player.getName() + " from " + currentAlwaysOn + " to " + rankLimiter.getAlwaysOn());
						}
					}

				}
			}
		}.runTaskLaterAsynchronously(BetterChunkLoader.instance(),20);
	}
}
