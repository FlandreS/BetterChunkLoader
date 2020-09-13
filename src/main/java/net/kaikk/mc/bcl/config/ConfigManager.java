package net.kaikk.mc.bcl.config;

import br.com.finalcraft.evernifecore.config.Config;
import net.kaikk.mc.bcl.config.data.BCLSettings;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private static Config config;
    private static Config chunksByRank;

    public static Config getMainConfig() {
        return config;
    }

    public static Config getChunksByRank(){
        return chunksByRank;
    }

    public static void initialize(JavaPlugin instance){
        config          = new Config(instance,"config.yml",true);
        chunksByRank    = new Config(instance,"ChunksByRank.yml", true);

        BCLSettings.initialize();
    }
}
