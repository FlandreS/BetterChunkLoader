package br.com.finalcraft.betterchunkloader.commands;

import br.com.finalcraft.betterchunkloader.BetterChunkLoader;
import br.com.finalcraft.betterchunkloader.CChunkLoader;
import br.com.finalcraft.betterchunkloader.PermissionNodes;
import br.com.finalcraft.betterchunkloader.config.data.ChunksByRank;
import br.com.finalcraft.betterchunkloader.config.data.RankLimiter;
import br.com.finalcraft.betterchunkloader.datastore.BCLPlayerData;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.betterchunkloader.datastore.IDataStore;
import br.com.finalcraft.betterchunkloader.evernife.EverNifeFunctions;
import br.com.finalcraft.evernifecore.argumento.MultiArgumentos;
import br.com.finalcraft.evernifecore.commands.finalcmd.annotations.Arg;
import br.com.finalcraft.evernifecore.commands.finalcmd.annotations.FinalCMD;
import br.com.finalcraft.evernifecore.commands.finalcmd.help.HelpLine;
import br.com.finalcraft.evernifecore.config.playerdata.PlayerData;
import br.com.finalcraft.evernifecore.fancytext.FancyText;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.evernifecore.util.FCMessageUtil;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@FinalCMD(
        aliases = {"chunkloader","betterchunkloader","bcl"}
)
public class CMDChunkLoader {

    @FCLocale(lang = LocaleType.EN_US, text = "§2    ▶ §aChunkLoaders Available §e(Online Only): §6%free_chunks%§7§l/§a%total_free_chunks%")
    @FCLocale(lang = LocaleType.PT_BR, text = "§2    ▶ §aChunkLoaders Disponíveis §e(Normal): §6%free_chunks%§7§l/§a%total_free_chunks%")
    public static LocaleMessage AVAILABLE_CHUNKS_ONLINE_ONLY;

    @FCLocale(lang = LocaleType.EN_US, text = "§2    ▶ §aChunkLoaders Available §b(Always On): §6%premium_chunks%§7§l/§a%total_premium_chunks%")
    @FCLocale(lang = LocaleType.PT_BR, text = "§2    ▶ §aChunkLoaders Disponíveis §b(Permanente): §6%premium_chunks%§7§l/§a%total_premium_chunks%")
    public static LocaleMessage AVAILABLE_CHUNKS_ALWAYS_ON;


    @FinalCMD.SubCMD(
            subcmd = {"debuggroup"},
            locales = {
                    @FCLocale(lang = LocaleType.EN_US, text = "Show your group info"),
                    @FCLocale(lang = LocaleType.PT_BR, text = "Mostra suas informações de grupo")
            },
            permission = PermissionNodes.COMMAND_DEBUGGROUP
    )
    public void debuggroup(Player player) {
        player.sendMessage("§a§m-----------------------------------------------------");
        player.sendMessage("§2§l ▶ §eenableRankLimit: §a" + ChunksByRank.enableRankLimit);
        player.sendMessage("§2§l ▶ §eRankLimitGroups: §a" + ChunksByRank.RANK_LIMITERS_REVERSED_ORDER.size());
        player.sendMessage("");

        for (RankLimiter rankLimiter : ChunksByRank.RANK_LIMITERS_REVERSED_ORDER) {
            player.sendMessage(String.format("§2§l    - §e[%s] §awith permission: §b%s", rankLimiter.getRankName(), rankLimiter.getPermission()));
        }

        player.sendMessage("");

        RankLimiter rankLimiter = ChunksByRank.getPlayerLimit(player);

        if (rankLimiter == null){
            player.sendMessage("§2§l ▶ §eYour RankLimiter: §cnull");
        }else {
            player.sendMessage("§2§l ▶ §aYour Limit [§d" + rankLimiter.getRankName() + "§a]: OnlineOnly=§b" + rankLimiter.getOnlineOnly());
            player.sendMessage("§2§l ▶ §aYour Limit [§d" + rankLimiter.getRankName() + "§a]: AlwaysOn=§b" + rankLimiter.getAlwaysOn());
        }

        player.sendMessage("§a§m-----------------------------------------------------");
    }

    @FinalCMD.SubCMD(
            subcmd = {"info"},
            locales = {
                    @FCLocale(lang = LocaleType.EN_US, text = "Show your info"),
                    @FCLocale(lang = LocaleType.PT_BR, text = "Mostra suas informações")
            }
    )
    public void info(CommandSender sender, MultiArgumentos argumentos) {

        if (!(sender instanceof Player) || (!argumentos.get(1).isEmpty() && sender.hasPermission(PermissionNodes.COMMAND_ADMIN)) ) {
            PlayerData fcPlayerData = argumentos.get(1).getPlayerData();
            if (fcPlayerData == null){
                FCMessageUtil.playerDataNotFound(sender, argumentos.getStringArg(1));
                return;
            }
            BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(fcPlayerData.getUniqueId());

            int free_chunks = DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(fcPlayerData.getUniqueId());
            int total_free_chunks = playerData.getOnlineOnlyChunksAmount();

            int premium_chunks = DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(fcPlayerData.getUniqueId());
            int total_premium_chunks = playerData.getAlwaysOnChunksAmount();

            sender.sendMessage("§a§m-----------------------------------------------------");
            sender.sendMessage("§a  ▶ [" + fcPlayerData.getPlayerName() + "] BCL Info:");
            AVAILABLE_CHUNKS_ONLINE_ONLY
                    .addPlaceholder("%free_chunks%", free_chunks)
                    .addPlaceholder("%total_free_chunks%", total_free_chunks)
                    .send(sender);
            AVAILABLE_CHUNKS_ALWAYS_ON
                    .addPlaceholder("%premium_chunks%", premium_chunks)
                    .addPlaceholder("%total_premium_chunks%", total_premium_chunks)
                    .send(sender);
            sender.sendMessage("§a§m-----------------------------------------------------");
        }

        Player player = (Player) sender;
        BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());

        int free_chunks = DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(player.getUniqueId());
        int total_free_chunks = playerData.getOnlineOnlyChunksAmount();

        int premium_chunks = DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(player.getUniqueId());
        int total_premium_chunks = playerData.getAlwaysOnChunksAmount();

        player.sendMessage("§a§m-----------------------------------------------------");
        AVAILABLE_CHUNKS_ONLINE_ONLY
                .addPlaceholder("%free_chunks%", free_chunks)
                .addPlaceholder("%total_free_chunks%", total_free_chunks)
                .send(sender);
        AVAILABLE_CHUNKS_ALWAYS_ON
                .addPlaceholder("%premium_chunks%", premium_chunks)
                .addPlaceholder("%total_premium_chunks%", total_premium_chunks)
                .send(sender);
        player.sendMessage("§a§m-----------------------------------------------------");
        return;
    }


    @FCLocale(lang = LocaleType.EN_US, text = "§2 ▶ §2ChunkLoaders being used §e(Normal): §6%free_chunks%§7§l/§a%total_free_chunks%")
    @FCLocale(lang = LocaleType.PT_BR, text = "§2 ▶ §2ChunkLoaders em USO §e(Normal): §6%free_chunks%§7§l/§a%total_free_chunks%")
    public static LocaleMessage CHUNKS_IN_USE_ONLINE_ONLY;

    @FCLocale(lang = LocaleType.EN_US, text = "§2 ▶ §2ChunkLoaders being used §b(Permanente): §6§6%free_chunks%§7§l/§a%total_premium_chunks%")
    @FCLocale(lang = LocaleType.PT_BR, text = "§2 ▶ §2ChunkLoaders em USO §b(Permanente): §6%premium_chunks%§7§l/§a%total_premium_chunks%")
    public static LocaleMessage CHUNKS_IN_USE_ALWAYS_ON;

    @FinalCMD.SubCMD(
            subcmd = {"list"},
            usage = "<Normal|Perma>",
            locales = {
                    @FCLocale(lang = LocaleType.EN_US, text = "List all your chunkloaders"),
                    @FCLocale(lang = LocaleType.PT_BR, text = "Lista todas os seus ChunkLoaders")
            }
    )
    public void list(Player player, MultiArgumentos argumentos, HelpLine helpLine) {

        if (argumentos.emptyArgs(1)){
            helpLine.sendTo(player);
            return;
        }

        BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());
        List<CChunkLoader> chunkLoaderList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());

        switch (argumentos.get(1).toLowerCase()) {
            case "free":
            case "comum":
            case "normal":
            case "onlineonly":
                int free_chunks = 0;
                int total_free_chunks = playerData.getOnlineOnlyChunksAmount();
                player.sendMessage("§a§m-----------------------------------------------------");
                for (CChunkLoader chunkLoader : chunkLoaderList) {
                    if (!chunkLoader.isAlwaysOn()){
                        free_chunks += chunkLoader.size();
                        FancyText.of("§7  - §e[" + chunkLoader.sizeX() + "] " + chunkLoader.getLoc().toString())
                                .setRunCommandAction(!player.hasPermission(PermissionNodes.COMMAND_ADMIN) ? null : "/tppos " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ() + " " + chunkLoader.getLoc().getWorldName())
                                .send(player);
                    }
                }
                player.sendMessage("");
                CHUNKS_IN_USE_ONLINE_ONLY
                        .addPlaceholder("%free_chunks%", free_chunks)
                        .addPlaceholder("%total_free_chunks%", total_free_chunks)
                        .send(player);
                player.sendMessage("§a§m-----------------------------------------------------");
                break;
            case "vip":
            case "perma":
            case "premium":
            case "alwayson":
            case "permanente":
                int premium_chunks = 0;
                int total_premium_chunks = playerData.getAlwaysOnChunksAmount();
                player.sendMessage("§a§m-----------------------------------------------------");
                for (CChunkLoader chunkLoader : chunkLoaderList) {
                    if (chunkLoader.isAlwaysOn()){
                        premium_chunks += chunkLoader.size();
                        FancyText.of("§7  - §b[" + chunkLoader.sizeX() + "] " + chunkLoader.getLoc().toString())
                                .setRunCommandAction(!player.hasPermission(PermissionNodes.COMMAND_ADMIN) ? null : "/tppos " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ() + " " + chunkLoader.getLoc().getWorldName())
                                .send(player);
                    }
                }
                player.sendMessage("");
                CHUNKS_IN_USE_ALWAYS_ON
                        .addPlaceholder("%premium_chunks%", premium_chunks)
                        .addPlaceholder("%total_premium_chunks%", total_premium_chunks)
                        .send(player);
                player.sendMessage("§a§m-----------------------------------------------------");
                break;
        }

        return;
    }

    @FinalCMD.SubCMD(
            subcmd = {"listother"},
            permission = PermissionNodes.COMMAND_LIST_OTHER,
            locales = {
                    @FCLocale(lang = LocaleType.EN_US, text = "List all ChunkLoaders from a specific player."),
                    @FCLocale(lang = LocaleType.PT_BR, text = "Lista todos os ChunkLoaders de um jogador especifico.")
            }
    )
    public void listother(CommandSender sender, MultiArgumentos argumentos, @Arg(name = "<Player>") PlayerData target, @Arg(name = "<Normal|Premium>") String type) {

        BCLPlayerData playerData = DataStoreManager.getDataStore().getPlayerData(target.getUniqueId());
        List<CChunkLoader> chunkLoaderList = DataStoreManager.getDataStore().getChunkLoaders(target.getUniqueId());

        switch (type.toLowerCase()) {
            case "normal":
                int free_chunks = 0;
                int total_free_chunks = playerData.getOnlineOnlyChunksAmount();
                sender.sendMessage("§a§m-----------------------------------------------------");
                for (CChunkLoader chunkLoader : chunkLoaderList) {
                    if (!chunkLoader.isAlwaysOn()){
                        free_chunks += chunkLoader.size();
                        FancyText.of("§7  - §e[" + chunkLoader.sizeX() + "] " + chunkLoader.getLoc().toString())
                                .setRunCommandAction("/tppos " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ() + " " + chunkLoader.getLoc().getWorldName())
                                .send(sender);
                    }
                }
                sender.sendMessage("");
                CHUNKS_IN_USE_ONLINE_ONLY
                        .addPlaceholder("%free_chunks%", free_chunks)
                        .addPlaceholder("%total_free_chunks%", total_free_chunks)
                        .send(sender);
                sender.sendMessage("§a§m-----------------------------------------------------");
                break;
            case "premium":
                int premium_chunks = 0;
                int total_premium_chunks = playerData.getAlwaysOnChunksAmount();
                sender.sendMessage("§a§m-----------------------------------------------------");
                for (CChunkLoader chunkLoader : chunkLoaderList) {
                    if (chunkLoader.isAlwaysOn()){
                        premium_chunks += chunkLoader.size();
                        FancyText.of("§7  - §b[" + chunkLoader.sizeX() + "] " + chunkLoader.getLoc().toString())
                                .setRunCommandAction("/tppos " + chunkLoader.getLoc().getX() + " " + chunkLoader.getLoc().getY() + " " + chunkLoader.getLoc().getZ() + " " + chunkLoader.getLoc().getWorldName())
                                .send(sender);
                    }
                }
                sender.sendMessage("");
                CHUNKS_IN_USE_ALWAYS_ON
                        .addPlaceholder("%premium_chunks%", premium_chunks)
                        .addPlaceholder("%total_premium_chunks%", total_premium_chunks)
                        .send(sender);
                sender.sendMessage("§a§m-----------------------------------------------------");
                break;
        }

    }

    @FinalCMD.SubCMD(
            subcmd = {"chunks"},
            usage = "%name% <PlayerName> <free|premium> <add|set> <amount> [-force]",
            locales = {
                    @FCLocale(lang = LocaleType.EN_US, text = "Edit specific player chunkloaders's values."),
                    @FCLocale(lang = LocaleType.PT_BR, text = "Edita os chunkloaders de um jogador específico.")
            },
            permission = PermissionNodes.COMMAND_ADMIN
    )
    public void chunks(CommandSender sender, MultiArgumentos argumentos, HelpLine helpLine) {

        if (argumentos.emptyArgs(1,2,3,4)){
            helpLine.sendTo(sender);
            return;
        }

        PlayerData fc_playerData = argumentos.get(1).getPlayerData();

        if (fc_playerData == null){
            FCMessageUtil.playerDataNotFound(sender, argumentos.getStringArg(1));
            return;
        }

        Boolean premium;
        switch (argumentos.get(2).toLowerCase()) {
            case "free":
            case "comum":
            case "normal":
            case "onlineonly":
                premium = false;
                break;
            case "vip":
            case "perma":
            case "premium":
            case "alwayson":
            case "permanente":
                premium = true;
                break;
            default: //Wrong Argument
                helpLine.sendTo(sender);
                return;
        }

        Integer amount = argumentos.get(4).getInteger();
        if (amount == null){
            FCMessageUtil.needsToBeInteger(sender, argumentos.getStringArg(4));
            return;
        }

        switch (argumentos.get(3).toLowerCase()) {
            case "add":
                if (premium){
                    DataStoreManager.getDataStore().addAlwaysOnChunksLimit(fc_playerData.getUniqueId(), amount);
                }else {
                    DataStoreManager.getDataStore().addOnlineOnlyChunksLimit(fc_playerData.getUniqueId(), amount);
                }
                sender.sendMessage("Added " + amount + " " + (premium ? "always-on" : "online-only") + " chunks to " + fc_playerData.getPlayerName());
                break;
            case "set":
                if (premium){
                    DataStoreManager.getDataStore().setAlwaysOnChunksLimit(fc_playerData.getUniqueId(), amount);
                }else {
                    DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(fc_playerData.getUniqueId(), amount);
                }
                sender.sendMessage("Set " + fc_playerData.getPlayerName() + (premium ? "always-on" : "online-only") + " chunks to" + amount);
                break;
            default: //Wrong Argument
                helpLine.sendTo(sender);
                return;
        }

        return;
    }

    @FinalCMD.SubCMD(
            subcmd = {"near"},
            permission = PermissionNodes.COMMAND_ADMIN
    )
    public void near(Player player) {
        List<CChunkLoader> allChunks = EverNifeFunctions.getActivePremiumChunks();

        if (allChunks.isEmpty()){
            player.sendMessage("Não existem ChunkLoaders Premium no servidor;");
            return;
        }

        List<CChunkLoader> nearChunks = new ArrayList<>();
        Location playerLocation = player.getLocation();

        allChunks.forEach(cChunkLoader -> {
            try{
                if (!cChunkLoader.isExpired() && !cChunkLoader.markDisabled){
                    if (playerLocation.distance(cChunkLoader.getLoc().getLocation()) <= 600 ){
                        nearChunks.add(cChunkLoader);
                    }
                }
            }catch (Exception ignored){}
        });

        player.sendMessage("--------- Near Premium Chunks ---------");
        nearChunks.forEach(cChunkLoader ->
                FancyText.of("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
                        .setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
                        .setRunCommandAction("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
                        .send(player)
                );
    }

    @FinalCMD.SubCMD(
            subcmd = {"removertudo"}
    )
    public void removertudo(Player player, MultiArgumentos argumentos) {
        FCBukkitUtil.makeConsoleExecuteCommand("bcl delete " + player.getName());
        player.sendMessage(ChatColor.GREEN + "Todos os seus chunkloaders foram removidos!");
    }

    @FinalCMD.SubCMD(
            subcmd = {"delete"},
            usage = "%name% <PlayerName>",
            permission = PermissionNodes.COMMAND_DELETE
    )
    public void delete(CommandSender sender, @Arg(name = "<Player>") PlayerData target) {

        DataStoreManager.getDataStore().removeChunkLoaders(target.getUniqueId());
        sender.sendMessage(ChatColor.RED + "All chunk loaders placed by " + target.getPlayerName() + " have been removed.");

    }

    @FinalCMD.SubCMD(
            subcmd = {"purge"},
            permission = PermissionNodes.COMMAND_PURGE
    )
    public void purge(CommandSender sender) {
        IDataStore ds = DataStoreManager.getDataStore();
        List<CChunkLoader> chunkLoaders = new ArrayList(DataStoreManager.getDataStore().getChunkLoaders());
        for (CChunkLoader cl : chunkLoaders) {
            if (!cl.blockCheck()) {
                ds.removeChunkLoader(cl);
            }
        }
        sender.sendMessage(ChatColor.GOLD+"All invalid chunk loaders have been removed.");
    }

    @FinalCMD.SubCMD(
            subcmd = {"listpremium"},
            permission = PermissionNodes.COMMAND_ADMIN
    )
    public void listpremium(Player player, String label, MultiArgumentos argumentos, HelpLine helpLine) {
        player.sendMessage("--------- Premium Chunks ---------");
        EverNifeFunctions.getActivePremiumChunks().forEach(cChunkLoader ->
                FancyText.of("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
                        .setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
                        .setRunCommandAction("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ() + " " + cChunkLoader.getLoc().getWorldName())
                        .send(player)
                );
    }

    @FinalCMD.SubCMD(
            subcmd = {"disablefromplayer"},
            usage = "%name% <Player>",
            permission = PermissionNodes.COMMAND_ADMIN
    )
    public void disableFromPlayer(CommandSender sender, MultiArgumentos argumentos, HelpLine helpLine) {
        if (argumentos.emptyArgs(1)){
            helpLine.sendTo(sender);
            return;
        }

        PlayerData fc_playerData = argumentos.get(1).getPlayerData();

        if (fc_playerData == null){
            FCMessageUtil.playerDataNotFound(sender, argumentos.getStringArg(1));
            return;
        }

        List<CChunkLoader> playersChunks = EverNifeFunctions.getActivePremiumChunksFromPlayer(fc_playerData.getPlayerName());

        if (playersChunks.isEmpty()){
            sender.sendMessage("O jogador " + argumentos.get(1) + " não possui ChunkLoaders Premium");
            return;
        }

        sender.sendMessage("--------- Removed Chunks ---------");
        playersChunks.forEach(cChunkLoader ->
                FancyText.of("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
                        .setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
                        .setRunCommandAction("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
                        .send(sender)
                );
        sender.sendMessage("");

        for (CChunkLoader chunkLoader : playersChunks){
            chunkLoader.markDisabled = true;
            BCLForgeLib.instance().removeChunkLoader(chunkLoader);
        }
    }

    @FinalCMD.SubCMD(
            subcmd = {"reload"},
            permission = PermissionNodes.COMMAND_ADMIN
    )
    private void reload(CommandSender sender) {
        // TODO Do something else to reload this plugin, wtf is this?
        // Shame :/
        Bukkit.getPluginManager().disablePlugin(BetterChunkLoader.instance());
        Bukkit.getPluginManager().enablePlugin(BetterChunkLoader.instance());
        FCMessageUtil.pluginHasBeenReloaded(sender, "BetterChunkLoader");
    }
}
