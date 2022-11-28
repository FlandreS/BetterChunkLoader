package br.com.finalcraft.betterchunkloader;

import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerData;
import br.com.finalcraft.evernifecore.integration.placeholders.PAPIIntegration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlaceHolderIntegration {

    public static void initialize(JavaPlugin plugin){
        PAPIIntegration.createPlaceholderIntegration(plugin, "bcl", PlayerData.class)
                .addParser("chunks_total", playerData -> totalChunks())
                .addParser("chunks_active", playerData -> activeChunks(null))
                .addParser("chunks_commmon_active", playerData -> activeChunks(true))
                .addParser("chunks_premium_active", playerData -> activeChunks(false))

                .addParser("common_chunks", playerData -> getCommonChunks(playerData))
                .addParser("premium_chunks", playerData -> getPremiumChunks(playerData));
    }

    private static String totalChunks(){
        return "" + DataStoreManager.getDataStore().getChunkLoaders().size();
    }

    private static String getCommonChunks(PlayerData playerData){
        int commonChunks = 0;
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(playerData.getUniqueId());
        if (clList != null) {
            for (CChunkLoader aChunk : clList) {
                if (!aChunk.isAlwaysOn()) {
                    commonChunks++;
                }
            }
        }
        return "" + commonChunks;
    }

    private static String getPremiumChunks(PlayerData playerData){
        int premiumChunks = 0;
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(playerData.getUniqueId());
        if (clList != null){
            for (CChunkLoader aChunk : clList){
                if (aChunk.isAlwaysOn()){
                    premiumChunks++;
                }
            }
        }

        return "" + premiumChunks;
    }

    private static String activeChunks(Boolean premium){
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders();

        int activeChunks = 0;
        if (clList != null){
            for (CChunkLoader aChunk : clList){
                if (!aChunk.isExpired()){

                    if (premium == null){
                        activeChunks++;
                    }else if (premium){
                        if (aChunk.isAlwaysOn()){
                            activeChunks++;
                        }
                    }else {
                        if (!aChunk.isAlwaysOn()){
                            activeChunks++;
                        }
                    }
                }
            }
        }
        return "" + activeChunks;
    }
}
