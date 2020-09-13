package net.kaikk.mc.bcl.config.data;

import net.kaikk.mc.bcl.BetterChunkLoader;
import net.kaikk.mc.bcl.config.ConfigManager;
import org.bukkit.Material;

public class BCLSettings {
    public static int maxHoursOffline;
    public static int defaultChunksAmountAlwaysOn;
    public static int defaultChunksAmountOnlineOnly;
    public static int maxChunksAmountAlwaysOn;
    public static int maxChunksAmountOnlineOnly;
    public static int onlineOnlyMeta;
    public static int alwaysOnMeta;

    public static String dataStore;
    public static String mySqlUsername;
    public static String mySqlPassword;
    public static String mySqlDatabase;
    public static String mySqlHostname;

    public static Material onlineOnlyMaterial;
    public static Material alwaysOnMaterial;

    public static void initialize(){
        onlineOnlyMeta = ConfigManager.getMainConfig().getInt("OnlineOnlyBlockMetadata", 1);
        alwaysOnMeta = ConfigManager.getMainConfig().getInt("AlwaysOnBlockMetadata", 2);

        maxHoursOffline= ConfigManager.getMainConfig().getInt("MaxHoursOffline", 168);

        defaultChunksAmountAlwaysOn= ConfigManager.getMainConfig().getInt("DefaultChunksAmount.AlwaysOn", 5);
        defaultChunksAmountOnlineOnly= ConfigManager.getMainConfig().getInt("DefaultChunksAmount.OnlineOnly", 50);

        maxChunksAmountAlwaysOn= ConfigManager.getMainConfig().getInt("MaxChunksAmount.AlwaysOn", 57);
        maxChunksAmountOnlineOnly= ConfigManager.getMainConfig().getInt("MaxChunksAmount.OnlineOnly", 75);

        dataStore= ConfigManager.getMainConfig().getString("DataStore");

        mySqlHostname= ConfigManager.getMainConfig().getString("MySQL.Hostname");
        mySqlUsername= ConfigManager.getMainConfig().getString("MySQL.Username");
        mySqlPassword= ConfigManager.getMainConfig().getString("MySQL.Password");
        mySqlDatabase= ConfigManager.getMainConfig().getString("MySQL.Database");

        String ms = ConfigManager.getMainConfig().getString("OnlineOnlyBlockMaterial", "IRON_BLOCK");
        Material m = Material.getMaterial(ms);
        if (m == null) {
            m = Material.IRON_BLOCK;
            BetterChunkLoader.instance().getLogger().warning("Invalid material: "+ms);
        }
        onlineOnlyMaterial = m;

        ms = ConfigManager.getMainConfig().getString("AlwaysOnBlockMaterial", "DIAMOND_BLOCK");
        m = Material.getMaterial(ms);
        if (m == null) {
            m = Material.DIAMOND_BLOCK;
            BetterChunkLoader.instance().getLogger().warning("Invalid material: "+ms);
        }
        alwaysOnMaterial = m;
    }
}
