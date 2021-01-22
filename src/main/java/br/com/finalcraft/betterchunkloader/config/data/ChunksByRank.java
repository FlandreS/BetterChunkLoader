package br.com.finalcraft.betterchunkloader.config.data;

import br.com.finalcraft.betterchunkloader.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class ChunksByRank {

    public static Map<String, Integer> mapOfChunksByRank = new HashMap<>();

    public static void initialize(){
        mapOfChunksByRank.clear();

        for (String rankName : ConfigManager.getChunksByRank().getKeys("ChunksByRank")) {
            mapOfChunksByRank.put(rankName.toLowerCase(), ConfigManager.getChunksByRank().getInt("ChunksByRank." + rankName));
        }
    }

    public static int getAmout(String rank){
        return mapOfChunksByRank.getOrDefault(rank.toLowerCase(), 9); // Default
    }


}
