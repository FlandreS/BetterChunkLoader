package net.kaikk.mc.bcl;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import net.kaikk.mc.bcl.datastore.DataStoreManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PlaceHolderIntegration extends EZPlaceholderHook {

    public static void initialize(){
        new PlaceHolderIntegration(BetterChunkLoader.instance(),"bcl").hook();
    }

    public PlaceHolderIntegration(Plugin plugin, String identifier) {
        super(plugin, identifier);
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {

        switch (placeholder){
            case "chunks_total":
                return totalChunks();
            case "chunks_active":
                return activeChunks(null);
            case "chunks_commmon_active":
                return activeChunks(true);
            case "chunks_premium_active":
                return activeChunks(false);
        }

        if (player == null){
            return "";
        }

        switch (placeholder){
            case "common_chunks":
                return getCommonChunks(player);
            case "premium_chunks":
                return getPremiumChunks(player);
        }

        return null;
    }


    private static String totalChunks(){
        return "" + DataStoreManager.getDataStore().getChunkLoaders().size();
    }

    private static String getCommonChunks(Player player){

        int commonChunks = 0;
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
        if (clList != null) {
            for (CChunkLoader aChunk : clList) {
                if (!aChunk.isAlwaysOn()) {
                    commonChunks++;
                }
            }
        }
        return "" + commonChunks;
    }

    private static String getPremiumChunks(Player player){

        int premiumChunks = 0;
        List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
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
