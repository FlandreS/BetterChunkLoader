package br.com.finalcraft.betterchunkloader;

import br.com.finalcraft.betterchunkloader.config.data.BCLSettings;
import br.com.finalcraft.betterchunkloader.datastore.DataStoreManager;
import br.com.finalcraft.evernifecore.listeners.base.ECListener;
import br.com.finalcraft.evernifecore.locale.FCLocale;
import br.com.finalcraft.evernifecore.locale.LocaleMessage;
import br.com.finalcraft.evernifecore.locale.LocaleType;
import br.com.finalcraft.evernifecore.minecraft.vector.BlockPos;
import br.com.finalcraft.evernifecore.minecraft.vector.ChunkPos;
import br.com.finalcraft.evernifecore.util.FCBukkitUtil;
import br.com.finalcraft.evernifecore.util.FCMessageUtil;
import br.com.finalcraft.evernifecore.util.commons.TriState;
import net.kaikk.mc.bcl.forgelib.BCLForgeLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventListener implements ECListener {

	@FCLocale(lang = LocaleType.PT_BR, text = "§4§l ▶ §cVocê não pode editar o ChunkLoader de outros jogadores!")
	@FCLocale(lang = LocaleType.EN_US, text = "§4§l ▶ §cYou cannot edit other's ChunkLoaders!")
	private static LocaleMessage CANNOT_EDIT_OTHERS_CHUNKLOADERS;

	@EventHandler(ignoreCancelled=true, priority = EventPriority.MONITOR)
	void onPlayerInteract(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

	    Player player = event.getPlayer();
		Block clickedBlock = event.getClickedBlock();
		
		if (clickedBlock == null || player == null) {
			return;
		}

		Material blockType = clickedBlock.getType();

		if (blockType != BCLSettings.alwaysOnMaterial  && blockType != BCLSettings.onlineOnlyMaterial) {
			return;
		}

		CChunkLoader chunkLoader = DataStoreManager.getDataStore().getChunkLoaderAt(new BlockLocation(clickedBlock.getLocation()));

		if (player.getItemInHand().getType() == Material.BLAZE_ROD) {

			if (chunkLoader != null) {
				if (player.getUniqueId().equals(chunkLoader.getOwner())
						|| player.hasPermission(PermissionNodes.ADMIN_EDIT_ALL)
						|| (chunkLoader.isAdminChunkLoader() && player.hasPermission(PermissionNodes.ADMIN_LOADER))) {
					chunkLoader.showUI(player);
				} else {
					CANNOT_EDIT_OTHERS_CHUNKLOADERS.send(player);
				}
				return;
			}

			if (!canBreak(clickedBlock, player)){
				FCMessageUtil.needsThePermission(player);
				return;
			}

			UUID uuid = player.getUniqueId();

			TriState isAlwaysOn = blockType == BCLSettings.alwaysOnMaterial ? TriState.TRUE
					: blockType == BCLSettings.onlineOnlyMaterial ? TriState.FALSE
					: TriState.UNKNOWN;

			if (isAlwaysOn.isUnknown()){
				//Is not a ChunkLoader Material!
				return;
			}

			if (isAlwaysOn.toBoolean()) {
				if (!player.hasPermission(PermissionNodes.CHUNK_ALWAYSON)) {
					FCMessageUtil.needsThePermission(player, PermissionNodes.CHUNK_ALWAYSON);
					return;
				}
				if (player.isSneaking() && player.hasPermission(PermissionNodes.ADMIN_LOADER)) {
					uuid = CChunkLoader.adminUUID;
				}
			} else {
				if (!player.hasPermission(PermissionNodes.CHUNK_ONLINEONLY)) {
					FCMessageUtil.needsThePermission(player, PermissionNodes.CHUNK_ONLINEONLY);
					return;
				}
			}

			ChunkPos chunkPos = BlockPos.from(clickedBlock.getLocation()).getChunkPos();

			chunkLoader = new CChunkLoader(chunkPos.getX(), chunkPos.getZ(), clickedBlock.getWorld().getName(), (byte) -1, uuid,player.getName(), new BlockLocation(clickedBlock), null, (clickedBlock.getType() == BCLSettings.alwaysOnMaterial));
			chunkLoader.showUI(player);

		} else {
			if (chunkLoader!=null) {
				player.sendMessage(chunkLoader.info());
				if (player.isSneaking()) {
					chunkLoader.showCorners(player);
				}
			} else {
				if (player.getItemInHand().getType()!=BCLSettings.alwaysOnMaterial && player.getItemInHand().getType()!=BCLSettings.onlineOnlyMaterial) {
					player.sendMessage(Messages.get("CanCreateChunkLoaders"));
				}
			}
		}

		//player.sendMessage("Checker Nothing Happened");
	}

	@EventHandler(ignoreCancelled=true, priority = EventPriority.HIGH)
	void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block==null || (block.getType()!=BCLSettings.alwaysOnMaterial && block.getType()!=BCLSettings.onlineOnlyMaterial)) {
			return;
		}

		CChunkLoader chunkLoader = DataStoreManager.getDataStore().getChunkLoaderAt(new BlockLocation(block.getLocation()));
		if (chunkLoader==null) {
			return;
		}
		
		DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);
		
		Player player = event.getPlayer();
		player.sendMessage(Messages.get("Removed"));
		
		Player owner = chunkLoader.getPlayer();
		if (owner!=null && player!=owner) {
			owner.sendMessage(Messages.get("RemovedBy").replace("[location]", chunkLoader.getLoc().toString()).replace("[player]", player.getDisplayName()));
		}
		
		BetterChunkLoader.instance().getLogger().info(player.getName()+" broke "+chunkLoader.getOwnerName()+"'s chunk loader at "+chunkLoader.getLocationString());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult()!=Result.ALLOWED) {
			return;
		}

		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(event.getPlayer().getUniqueId());

		for (CChunkLoader chunkLoader : clList) {
			if (!chunkLoader.isAlwaysOn() && chunkLoader.blockCheck()) {
				BCLForgeLib.instance().addChunkLoader(chunkLoader);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event) {
		List<CChunkLoader> clList = DataStoreManager.getDataStore().getChunkLoaders(event.getPlayer().getUniqueId());

		for (CChunkLoader chunkLoader : clList) {
			if (!chunkLoader.isAlwaysOn()) {
				BCLForgeLib.instance().removeChunkLoader(chunkLoader);
			}
		}
	}
	
    @EventHandler(ignoreCancelled=true)
    void onInventoryClick(InventoryClickEvent event) {
    	if (event.getInventory().getHolder() instanceof CChunkLoader && event.getWhoClicked() instanceof Player) {
    		Player player = (Player) event.getWhoClicked();

    		event.setCancelled(true);
    		CChunkLoader chunkLoader = (CChunkLoader) event.getInventory().getHolder();
    		if (chunkLoader==null) {
    			return;
    		}
    		
    		if (chunkLoader.isAdminChunkLoader()) {
				if (!FCBukkitUtil.hasThePermission(player, PermissionNodes.ADMIN_LOADER)) {
					return;
				}
    		} else {
	    		if (!player.getUniqueId().equals(chunkLoader.getOwner()) && !player.hasPermission("betterchunkloader.edit")) {
	    			player.sendMessage(Messages.get("CantEditOthersChunkLoaders"));
	    			return;
	    		}
    		}
    		
    		byte pos = (byte) event.getRawSlot();
    		if(chunkLoader.getRange()!=-1) {
    			if (pos==0) {
        			// remove the chunk loader
        			DataStoreManager.getDataStore().removeChunkLoader(chunkLoader);
        			closeInventory(player);
        		} else if (pos>1 && pos<7) {
        			// change range
        			pos-=2;
        			
        			// if higher range, check if the player has enough free chunks
        			if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission("betterchunkloader.unlimitedchunks")) {
	        			if (pos>chunkLoader.getRange()) {
	        				int needed = ((1+(pos*2))*(1+(pos*2)))-chunkLoader.size();
	        				int available;
	        				if (chunkLoader.isAlwaysOn()) {
	        					available=DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(chunkLoader.getOwner());
	        				} else {
	        					available=DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(chunkLoader.getOwner());
	        				}
	        				
	        				if (needed>available) {
	        					player.sendMessage(Messages.get("NotEnoughChunks").replace("[needed]", needed+"").replace("[available]", available+""));
	        					closeInventory(player);
	        					return;
	        				}
	        			}
        			}
        			
    				BetterChunkLoader.instance().getLogger().info(player.getName()+" edited "+chunkLoader.getOwnerName()+"'s chunk loader at "+chunkLoader.getLocationString()+" range from "+chunkLoader.getRange()+" to "+pos);
    				DataStoreManager.getDataStore().changeChunkLoaderRange(chunkLoader, pos);
    				player.sendMessage(Messages.get("ChunkLoaderUpdated"));
    				closeInventory(player);
        		}
    		} else if (pos>1 && pos<7) {
    			pos-=2;
    			
    			if (!chunkLoader.isAdminChunkLoader() && !player.hasPermission("betterchunkloader.unlimitedchunks")) {
	    			int needed = (1+(pos*2))*(1+(pos*2));
					int available;
					if (chunkLoader.isAlwaysOn()) {
						available=DataStoreManager.getDataStore().getAlwaysOnFreeChunksAmount(chunkLoader.getOwner());
					} else {
						available=DataStoreManager.getDataStore().getOnlineOnlyFreeChunksAmount(chunkLoader.getOwner());
					}
					
					if (needed>available) {
						player.sendMessage(Messages.get("NotEnoughChunks").replace("[needed]", needed+"").replace("[available]", available+""));
						closeInventory(player);
						return;
					}
    			}
    			
    			chunkLoader.setRange(pos);
    			chunkLoader.setCreationDate(new Date());
    			BetterChunkLoader.instance().getLogger().info(player.getName()+" made a new "+(chunkLoader.isAdminChunkLoader()?"admin ":"")+"chunk loader at "+chunkLoader.getLocationString()+" with range "+pos);
    			DataStoreManager.getDataStore().addChunkLoader(chunkLoader);
    			closeInventory(player);
    			player.sendMessage(Messages.get("ChunkLoaderCreated"));
        	}
    	}
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    void onWorldLoad(WorldLoadEvent event) {
		for (CChunkLoader cl : DataStoreManager.getDataStore().getChunkLoaders(event.getWorld().getName())) {
			if (cl.isLoadable()) {
				BCLForgeLib.instance().addChunkLoader(cl);
			}
		}
    }
    
    private static void closeInventory(final Player p) {
		new BukkitRunnable() {
			@Override
			public void run() {
				p.closeInventory();
			}
		}.runTaskLater(BetterChunkLoader.instance(), 1L);
    }
	
	static boolean canBreak(Block block, Player player) {
		BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
		Bukkit.getServer().getPluginManager().callEvent(breakEvent);
		return !breakEvent.isCancelled();
	}
}
