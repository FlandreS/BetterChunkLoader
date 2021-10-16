package br.com.finalcraft.betterchunkloader.config.data;

import br.com.finalcraft.betterchunkloader.config.ConfigManager;
import br.com.finalcraft.evernifecore.config.Config;
import br.com.finalcraft.evernifecore.util.numberwrapper.NumberWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class ChunksByRank {

    public static boolean enableRankLimit;
    public static LinkedHashMap<String, RankLimiter> mapOfChunksByRank = new LinkedHashMap<>();

    public static void initialize(){
        mapOfChunksByRank.clear();

        Config config = ConfigManager.getChunksByRank();

        enableRankLimit = config.getOrSetDefaultValue("Settings.enableRankLimit", false);

        if (enableRankLimit){

            List<RankLimiter> rankLimiterList = new ArrayList<>();
            for (String rankName : config.getKeys("ChunkLimiter")) {

                NumberWrapper<Integer> onlineOnly = NumberWrapper.of(config.getOrSetDefaultValue("ChunkLimiter." + rankName + ".onlineOnly", BCLSettings.maxChunksAmountOnlineOnly));
                NumberWrapper<Integer> alwaysOn = NumberWrapper.of(config.getOrSetDefaultValue("ChunkLimiter." + rankName + ".alwaysOn", BCLSettings.maxChunksAmountAlwaysOn));
                String permission = config.getOrSetDefaultValue("ChunkLimiter." + rankName + ".permission", "group." + rankName.toLowerCase());



                RankLimiter rankLimiter = new RankLimiter(
                        rankName,
                        onlineOnly.bound(0,BCLSettings.maxChunksAmountOnlineOnly).get(),
                        alwaysOn.bound(0,BCLSettings.maxChunksAmountAlwaysOn).get(),
                        permission
                );

                rankLimiterList.add(rankLimiter);
            }

            Collections.reverse(rankLimiterList);

            for (RankLimiter rankLimiter : rankLimiterList) {
                mapOfChunksByRank.put(rankLimiter.getRankName().toLowerCase(), rankLimiter);
            }
        }

        config.saveIfNewDefaults();

    }

    public static RankLimiter getPlayerLimit(Player player){
        if (enableRankLimit == false || mapOfChunksByRank.size() == 0){
            return null;
        }
        for (RankLimiter rankLimiter : mapOfChunksByRank.values()) {
            if (player.hasPermission(rankLimiter.getPermission())){
                return rankLimiter;
            }
        }
        return null;
    }


}
