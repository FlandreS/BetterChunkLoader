package net.kaikk.mc.bcl.listeners;

import br.com.finalcraft.betterrankup.api.FCRankUpAPI;
import net.kaikk.mc.bcl.BetterChunkLoader;
import net.kaikk.mc.bcl.config.data.ChunksByRank;
import net.kaikk.mc.bcl.datastore.DataStoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event){
		final Player player = event.getPlayer();
		new BukkitRunnable(){
			@Override
			public void run() {
				if (player.isOnline()){
					int correctAmoutToHave = ChunksByRank.getAmout(FCRankUpAPI.getRUPlayerData(player).getRankName());
					int oldChunksAmout = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId()).getOnlineOnlyChunksAmount();
					if ( oldChunksAmout != correctAmoutToHave){
						DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), correctAmoutToHave);
						BetterChunkLoader.instance().getLogger().info("Corrigindo chunks do jogador " + player.getName() + " de " + oldChunksAmout + " para " + correctAmoutToHave);
					}
				}
			}
		}.runTaskLaterAsynchronously(BetterChunkLoader.instance(),1200);
	}
}
