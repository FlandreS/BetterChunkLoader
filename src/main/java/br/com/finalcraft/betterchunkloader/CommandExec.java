package br.com.finalcraft.betterchunkloader;

import br.com.finalcraft.evernifecore.fancytext.FancyText;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.betterchunkloader.config.data.BCLSettings;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.betterchunkloader.datastore.IDataStore;
import br.com.finalcraft.betterchunkloader.datastore.PlayerData;
import br.com.finalcraft.betterchunkloader.evernife.EverNifeFunctions;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class CommandExec implements CommandExecutor {
	BetterChunkLoader instance;
	
	CommandExec(BetterChunkLoader instance) {
		this.instance=instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("betterchunkloader")) {

			if (args.length!=0) {
                switch(args[0].toLowerCase()) {
                    case "info":
                        return info(sender);
                    case "list":
                        return list(sender, label, args);
					case "chunks":
						return chunks(sender, label, args);
					case "removertudo":
						return removertudo(sender);
                    case "delete":
                        return delete(sender, label, args);
                    case "purge":
                        return purge(sender);
                    case "reload":
                        return reload(sender);
                    case "enable":
                        return enable(sender);
                    case "disable":
                        return disable(sender);
                    case "listpremium":
                        return listPremiumChunks(sender);
                    case "disableplayer":
                        return disableFromPlayer(sender,label,args);
					case "near":
						return near(sender);
                }
			}

            sender.sendMessage(tlc("&6---------- BCL ----------"));
            sender.sendMessage(tlc(" &a&l- &a&n/" + label + " list &a- Lista todas os ChunkLoaders."));
			sender.sendMessage(tlc(" &a&l- &a&n/" + label + " chunks &a- Alterar ou ver Chunks de um jogador."));
			sender.sendMessage(tlc(" &a&l- &a&n/" + label + " removertudo &a- Remove todos os seus chunkloaders."));

            if (sender.hasPermission("betterchunkloader.delete")){
				sender.sendMessage(tlc(" &a&l- &a&n/" + label + " info &a- Mostra informações gerais."));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " delete &a- Deleta as chunks de um jogador."));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " purge &a- Limpa os chunkloaders incorretos"));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " enable &a- Ativa o Plugin"));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " disable &a- Desativa o Plugin"));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " listpremium &a- Lista as PremiumChunks"));
				sender.sendMessage(tlc(" &b&l- &b&n/" + label + " disableplayer &a- Desativa as Chunks Premium de um jogador"));

			}
         sender.sendMessage("");
		}
		
		return false;
	}

	private boolean removertudo(CommandSender sender) {

		if ( !(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "Apenas jogadores físicos podem usar esse comando!");
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(),"bcl delete " + sender.getName());
		sender.sendMessage(ChatColor.GREEN + "Todos os seus chunkloaders foram removidos!");
		return true;
	}
	
	private boolean info(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.info")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}
		
		List<CChunkLoader> chunkLoaders = DataStoreManager.getDataStore().getChunkLoaders();
		if (chunkLoaders.isEmpty()) {
			sender.sendMessage(Messages.get("NoStatistics"));
			return true;
		}
		
		int alwaysOnLoaders=0, onlineOnlyLoaders=0, alwaysOnChunks=0, onlineOnlyChunks=0, maxChunksCount=0, players=0;
		UUID maxChunksPlayer=null;
		HashMap<UUID, Integer> loadedChunksForPlayer = new HashMap<>();
		
		for (CChunkLoader chunkLoader : chunkLoaders) {
			if (chunkLoader.isAlwaysOn()) {
				alwaysOnLoaders++;
				alwaysOnChunks+=chunkLoader.size();
			} else {
				onlineOnlyLoaders++;
				onlineOnlyChunks+=chunkLoader.size();
			}
			
			Integer count = loadedChunksForPlayer.get(chunkLoader.getOwner());
			if (count==null) {
				count=0;
			}
			count+=chunkLoader.size();
			loadedChunksForPlayer.put(chunkLoader.getOwner(), count);
		}

		loadedChunksForPlayer.remove(CChunkLoader.adminUUID);
		players=loadedChunksForPlayer.size();
		
		for (Entry<UUID, Integer> entry : loadedChunksForPlayer.entrySet()) {
			if (maxChunksCount<entry.getValue()) {
				maxChunksCount=entry.getValue();
				maxChunksPlayer=entry.getKey();
			}
		}
		
		sender.sendMessage(Messages.get("Info").replace("[onlineOnlyLoaders]", onlineOnlyLoaders+"").replace("[onlineOnlyChunks]", onlineOnlyChunks+"").replace("[alwaysOnLoaders]", alwaysOnLoaders+"").replace("[alwaysOnChunks]", alwaysOnChunks+"").replace("[players]", players+"").replace("[maxChunksPlayer]", instance.getServer().getOfflinePlayer(maxChunksPlayer).getName()).replace("[maxChunksCount]", maxChunksCount+""));		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean list(CommandSender sender, String label, String[] args) {
		if (args.length<2) {
			sender.sendMessage(ChatColor.GOLD + "Usage: /bcl list (own|PlayerName|all) [page]");
			return false;
		}
		
		int page=1;
		if (args.length==3) {
			try {
				page=Integer.valueOf(args[2]);
				if (page<1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(Messages.get("InvalidPage"));
				return false;
			}
		}
		
		if (args[1].equalsIgnoreCase("all")) {
			if (!sender.hasPermission("betterchunkloader.list.others")) {
				sender.sendMessage(Messages.get("PermissionDenied"));
				return false;
			}
			
			List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders();
			
			printChunkLoadersList(clList, sender, page);
		} else if (args[1].equalsIgnoreCase("alwayson")) {
			if (!sender.hasPermission("betterchunkloader.list.others")) {
				sender.sendMessage(Messages.get("PermissionDenied"));
				return false;
			}
			
			List<CChunkLoader> clList = new ArrayList<CChunkLoader>();
			for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders()) {
				if (cl.isAlwaysOn()) {
					clList.add(cl);
				}
			}
			
			printChunkLoadersList(clList, sender, page);
		} else {
			String playerName = args[1];
			if (playerName.equalsIgnoreCase("own")) {
				playerName=sender.getName();
			}
			
			if (sender.getName().equalsIgnoreCase(playerName)) {
				if (!sender.hasPermission("betterchunkloader.list.own")) {
					sender.sendMessage(Messages.get("PermissionDenied"));
					return false;
				}
			} else {
				if (!sender.hasPermission("betterchunkloader.list.others")) {
					sender.sendMessage(Messages.get("PermissionDenied"));
					return false;
				}
			}

			OfflinePlayer player = instance.getServer().getOfflinePlayer(playerName);
			if (player==null || !player.hasPlayedBefore()) {
				sender.sendMessage(Messages.get("PlayerNotFound"));
				return false;
			}
			List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
			if (clList==null || clList.size()==0) {
				sender.sendMessage(Messages.get("PlayerHasNoChunkLoaders"));
				return false;
			}
			
			int clSize=clList.size();
			int pages=(int) Math.ceil(clSize/5.00);

			if (page>pages) {
				sender.sendMessage(Messages.get("InvalidPage"));
				return false;
			}
			
			sender.sendMessage(Messages.get("PlayerChunkLoadersList").replace("[player]", player.getName()).replace("[page]", page+"").replace("[pages]", pages+""));
			
			for(int i=(page-1)*5; i<page*5 && i<clSize; i++) {
				CChunkLoader chunkLoader=clList.get(i);
				sender.sendMessage(chunkLoader.toString());
			}
			
		}	
		return true;
	}
	
	static private boolean printChunkLoadersList(List<CChunkLoader> clList, CommandSender sender, int page) {

		int clSize=clList.size();
		if (clSize==0) {
			sender.sendMessage(Messages.get("NoChunkLoaders"));
			return false;
		}
		
		int pages=(int) Math.ceil(clSize/5.00);

		if (page>pages) {
			sender.sendMessage(Messages.get("InvalidPage"));
			return false;
		}
		
		sender.sendMessage(Messages.get("GlobalChunkLoadersList").replace("[page]", page+"").replace("[pages]", pages+""));
		
		for(int i=(page-1)*5; i<page*5 && i<clSize; i++) {
			CChunkLoader chunkLoader=clList.get(i);
			sender.sendMessage(chunkLoader.getOwnerName()+" - "+chunkLoader.toString());
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private boolean chunks(CommandSender sender, String label, String[] args) {
		final String usage = "Usage: /"+label+" chunks [get (PlayerName)]\n"
							+ "       /"+label+" chunks (add|set) (PlayerName) (alwayson|onlineonly) (amount) [max|force]";
		
		if (sender instanceof Player && args.length==1) {
			sender.sendMessage(chunksInfo((Player) sender));
			return true;
		}
		
		if (args.length<3) {
			sender.sendMessage(usage);
			return false;
		}
		
		if (!sender.hasPermission("betterchunkloader.chunks")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}

		OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
		if (player==null) {
			sender.sendMessage(Messages.get("PlayerNotFound")+"\n"+usage);
			return false;
		}
		
		if (args[1].equalsIgnoreCase("get")) {
			sender.sendMessage(chunksInfo(player));
		} else {
			if (args.length<5) {
				sender.sendMessage(usage);
				return false;
			}
			
			Integer amount;
			try {
				amount = Integer.valueOf(args[4]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid argument "+args[4]+"\n"+usage);
				return false;
			}
			
			if (args[1].equalsIgnoreCase("add")) {
				PlayerData playerData = DataStoreManager.getDataStore().getPlayerData(player.getUniqueId());
				if (args[3].equalsIgnoreCase("alwayson")) {
					if (playerData.getAlwaysOnChunksAmount()+amount> BCLSettings.maxChunksAmountAlwaysOn) {
						sender.sendMessage("Couldn't add " + amount + " always-on chunks to "+player.getName()+" because it would exceed the always-on chunks limit of "+BCLSettings.maxChunksAmountAlwaysOn);
						amount = BCLSettings.maxChunksAmountAlwaysOn = playerData.getAlwaysOnChunksAmount();
					}

					DataStoreManager.getDataStore().addAlwaysOnChunksLimit(player.getUniqueId(), amount);
					sender.sendMessage("Added "+amount+" always-on chunks to "+player.getName());
				} else if (args[3].equalsIgnoreCase("onlineonly")) {
					if (playerData.getOnlineOnlyChunksAmount()+amount>BCLSettings.maxChunksAmountOnlineOnly) {
						sender.sendMessage("Couldn't add " + amount + " online-only chunks to "+player.getName()+" because it would exceed the online-only chunks limit of "+BCLSettings.maxChunksAmountOnlineOnly);
						amount = BCLSettings.maxChunksAmountAlwaysOn = playerData.getOnlineOnlyChunksAmount();
					}
					DataStoreManager.getDataStore().addOnlineOnlyChunksLimit(player.getUniqueId(), amount);
					sender.sendMessage("Added "+amount+" online-only chunks to "+player.getName());
				} else {
					sender.sendMessage("Invalid argument "+args[3]+"\n"+usage);
					return false;
				}
			} else if (args[1].equalsIgnoreCase("set")) {
				if (amount < 0) {
					sender.sendMessage("Invalid argument "+args[4]+"\n"+usage);
					return false;
				}
				
				if (args[3].equalsIgnoreCase("alwayson")) {
					DataStoreManager.getDataStore().setAlwaysOnChunksLimit(player.getUniqueId(), amount);
					sender.sendMessage("Set "+amount+" always-on chunks to "+player.getName());
				} else if (args[3].equalsIgnoreCase("onlineonly")) {
					DataStoreManager.getDataStore().setOnlineOnlyChunksLimit(player.getUniqueId(), amount);
					sender.sendMessage("Set "+amount+" online-only chunks to "+player.getName());
				} else {
					sender.sendMessage("Invalid argument "+args[3]+"\n"+usage);
					return false;
				}
			} else {
				sender.sendMessage("Invalid argument "+args[2]+"\n"+usage);
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private boolean delete(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("betterchunkloader.delete")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}
		
		if (args.length<2) {
			sender.sendMessage(ChatColor.GOLD + "Usage: /bcl delete (PlayerName)");
			return false;
		}
		
		OfflinePlayer player = instance.getServer().getOfflinePlayer(args[1]);
		if (player==null || !player.hasPlayedBefore()) {
			sender.sendMessage(ChatColor.RED + "Player not found.");
			return false;
		}
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(player.getUniqueId());
		if (clList==null) {
			sender.sendMessage(ChatColor.RED + "This player doesn't have any chunk loader.");
			return false;
		}
		
		DataStoreManager.getDataStore().removeChunkLoaders(player.getUniqueId());
		sender.sendMessage(ChatColor.RED + "All chunk loaders placed by this player have been removed.");
		instance.getLogger().info(sender.getName()+" deleted all chunk loaders placed by "+player.getName());
		return true;
	}
	
	private boolean purge(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.purge")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}
		
		IDataStore ds = DataStoreManager.getDataStore();
		List<CChunkLoader> chunkLoaders = new ArrayList<CChunkLoader>(DataStoreManager.getDataStore().getChunkLoaders());
		for (CChunkLoader cl : chunkLoaders) {
			if (!cl.blockCheck()) {
				ds.removeChunkLoader(cl);
			}
		}
		
		sender.sendMessage(ChatColor.GOLD+"All invalid chunk loaders have been removed.");

		return true;
	}

	private boolean reload(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.reload")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}

		instance.getLogger().info(sender.getName()+" reloaded this plugin");
		Bukkit.getPluginManager().disablePlugin(instance);
		Bukkit.getPluginManager().enablePlugin(instance);
		sender.sendMessage(ChatColor.RED + "BetterChunkLoader reloaded.");
		return true;
	}
	
	private boolean enable(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.enable")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}
		
		if (instance.enabled) {
			sender.sendMessage(ChatColor.RED + "BetterChunkLoader is already enabled!");
			return false;
		}
		
		instance.enable();
		sender.sendMessage(ChatColor.GREEN + "BetterChunkLoader has been enabled!");
		return true;
	}
	
	private boolean disable(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.disable")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}
		
		if (!instance.enabled) {
			sender.sendMessage(ChatColor.RED + "BetterChunkLoader is already disabled!");
			return false;
		}
		
		instance.disable();
		sender.sendMessage(ChatColor.GREEN + "BetterChunkLoader has been disabled!");
		return true;
	}

	private boolean listPremiumChunks(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.listPremiumChunks")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}

		if ( !(sender instanceof Player)){
			sender.sendMessage("Apenas jogadores físicos podem usar esse comando!");
		}
		Player player = (Player) sender;

		sender.sendMessage("--------- Premium Chunks ---------");
		EverNifeFunctions.getActivePremiumChunks().forEach(cChunkLoader ->
				FancyText.sendTo(player, new FancyText("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
						.setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
						.setRunCommandActionText("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
				));
		return true;
	}

	private boolean disableFromPlayer(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("betterchunkloader.disableFromPlayer")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}

		if (args.length < 2){
			sender.sendMessage("Erro de parametros, use \"" + label + "disablefromplayer <Player>\" ");
			return false;
		}

		List<CChunkLoader> playersChunks = EverNifeFunctions.getActivePremiumChunksFromPlayer( args[1] );

		if (playersChunks.isEmpty()){
			sender.sendMessage("O jogador " + args[1] + " não possui ChunkLoaders Premium");
			return false;
		}

		boolean senderIsAPlayer = false;

		if (sender instanceof Player) {
			senderIsAPlayer = true;
		}

		sender.sendMessage("--------- Removed Chunks ---------");
		if (senderIsAPlayer){
			final Player player = (Player) sender;
			playersChunks.forEach(cChunkLoader ->
					FancyText.sendTo(player, new FancyText("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
							.setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
							.setRunCommandActionText("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
					));
		}
		sender.sendMessage("");

		for (CChunkLoader cChunkLoader : playersChunks){
			cChunkLoader.markDisabled = true;
			BCLForgeLib.instance().removeChunkLoader(cChunkLoader);
		}

		return true;
	}

	private boolean near(CommandSender sender) {
		if (!sender.hasPermission("betterchunkloader.near")) {
			sender.sendMessage(Messages.get("PermissionDenied"));
			return false;
		}

		if (FCBukkitUtil.isNotPlayer(sender)){
			return true;
		}

		Player player = (Player) sender;

		List<CChunkLoader> allChunks = EverNifeFunctions.getActivePremiumChunks();

		if (allChunks.isEmpty()){
			sender.sendMessage("Não existem ChunkLoaders Premium no servidor;");
			return false;
		}

		List<CChunkLoader> nearChunks = new ArrayList<CChunkLoader>();
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

		sender.sendMessage("--------- Near Premium Chunks ---------");
		nearChunks.forEach(cChunkLoader ->
				FancyText.sendTo(player, new FancyText("§a§lPremium Chunk [§6§l " + (cChunkLoader.markDisabled ? "§c§l" : "") + cChunkLoader.getOwnerName() + " §a§l]")
						.setHoverText("Coords " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
						.setRunCommandActionText("/tppos " + cChunkLoader.getLoc().getX() + " " + cChunkLoader.getLoc().getY() + " " + cChunkLoader.getLoc().getZ())
				));
		return true;
	}
	
	static String chunksInfo(OfflinePlayer player) {
		IDataStore dataStore = DataStoreManager.getDataStore();
		int freeAlwaysOn = dataStore.getAlwaysOnFreeChunksAmount(player.getUniqueId());
		int freeOnlineOnly = dataStore.getOnlineOnlyFreeChunksAmount(player.getUniqueId());
		PlayerData pd=dataStore.getPlayerData(player.getUniqueId());
		int amountAlwaysOn = pd.getAlwaysOnChunksAmount();
		int amountOnlineOnly = pd.getOnlineOnlyChunksAmount();
		
		return Messages.get("PlayerChunksInfo").replace("[player]", player.getName())+"\n"
				+ Messages.get("AlwaysOn") + " - " + ((BetterChunkLoader.hasPermission(player, "betterchunkloader.alwayson")) ? Messages.get("Free")+": "+freeAlwaysOn+" "+Messages.get("Used")+": "+(amountAlwaysOn-freeAlwaysOn)+" "+Messages.get("Total")+": "+amountAlwaysOn : Messages.get("MissingPermission"))+"\n"
				+ Messages.get("OnlineOnly") + " - " + ((BetterChunkLoader.hasPermission(player, "betterchunkloader.onlineonly")) ? Messages.get("Free")+": "+freeOnlineOnly+" "+Messages.get("Used")+": "+(amountOnlineOnly-freeOnlineOnly)+" "+Messages.get("Total")+": "+amountOnlineOnly+"" : Messages.get("MissingPermission"));
	}


	//Translate ColorCodes
	private static String tlc(String aString){
	    return ChatColor.translateAlternateColorCodes('&',aString);
    }
}
