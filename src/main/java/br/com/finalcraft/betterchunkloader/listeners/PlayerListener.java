package br.com.finalcraft.betterchunkloader.listeners;

import br.com.finalcraft.betterchunkloader.BetterChunkLoader;
import br.com.finalcraft.betterchunkloader.config.data.ChunksByRank;
import br.com.finalcraft.betterchunkloader.config.data.RankLimiter;
import br.com.finalcraft.betterchunkloader.datastore.BCLPlayerData;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.evernifecore.api.events.ECFullyLoggedInEvent;
import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.evernifecore.scheduler.FCScheduller;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PlayerListener implements ECListener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(ECFullyLoggedInEvent event){

		if (ChunksByRank.isRankLimiterDisabled()){
			return;
		}

		final Player player = event.getPlayer();

		FCScheduller.scheduleAssyncInTicks(() -> {
			if (player.isOnline()){
				RankLimiter rankLimiter = ChunksByRank.getPlayerLimit(player);

				if (rankLimiter == null){
					return;
				}

				BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());

				int currentOnlineOnly = playerData.getOnlineOnlyChunksAmount();
				int currentAlwaysOn = playerData.getAlwaysOnChunksAmount();

				if (currentOnlineOnly != rankLimiter.getOnlineOnly()){
					DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), rankLimiter.getOnlineOnly());
					BetterChunkLoader.getLog().info("Fixing [OnlineOnly Chunks]' amount of " + player.getName() + " from " + currentOnlineOnly + " to " + rankLimiter.getOnlineOnly());
				}

				if (currentAlwaysOn != rankLimiter.getAlwaysOn()){
					DataStoreManager.getDataStore().setAlwaysOnChunksLimit(player.getUniqueId(), rankLimiter.getAlwaysOn());
					BetterChunkLoader.getLog().info("Fixing [AlwaysOn Chunks]' amount of " + player.getName() + " from " + currentAlwaysOn + " to " + rankLimiter.getAlwaysOn());
				}
			}
		}, 20);
	}
}
