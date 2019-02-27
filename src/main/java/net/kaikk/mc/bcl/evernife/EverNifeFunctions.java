package net.kaikk.mc.bcl.evernife;

import net.kaikk.mc.bcl.CChunkLoader;
import net.kaikk.mc.bcl.datastore.DataStoreManager;

import java.util.ArrayList;
import java.util.List;

public class EverNifeFunctions {


    public static List<CChunkLoader> getActivePremiumChunks(){
        List<CChunkLoader> clList = new ArrayList<CChunkLoader>();
        for (CChunkLoader aChunk : DataStoreManager.getDataStore().getChunkLoaders()){
                if (aChunk.isAlwaysOn() && !aChunk.isExpired()){
                    clList.add(aChunk);
                }
        }
        return clList;
    }

    public static List<CChunkLoader> getActivePremiumChunksFromPlayer(String playerName){
        List<CChunkLoader> clList = new ArrayList<CChunkLoader>();
        for (CChunkLoader aChunk : DataStoreManager.getDataStore().getChunkLoaders()){
            if (aChunk.getOwnerName().equalsIgnoreCase(playerName)){
                if (aChunk.isAlwaysOn()) {
                    clList.add(aChunk);
                }
            }
        }
        return clList;
    }


}
