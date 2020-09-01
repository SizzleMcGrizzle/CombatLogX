package com.SirBlobman.combatlogx.expansion.compatibility.citizens;

import com.SirBlobman.combatlogx.api.ICombatLogX;
import com.SirBlobman.combatlogx.api.expansion.Expansion;
import com.SirBlobman.combatlogx.api.expansion.ExpansionManager;
import com.SirBlobman.combatlogx.api.shaded.nms.VersionUtil;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.listener.ListenerCombat;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.listener.ListenerDamageDeath;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.listener.ListenerLogin;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.listener.ListenerPunish;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.listener.ListenerResurrect;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.manager.NPCManager;
import com.SirBlobman.combatlogx.expansion.compatibility.citizens.manager.SentinelManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.craftlancer.clstuff.CLStuff;
import de.craftlancer.clstuff.WGNoDropFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class CompatibilityCitizens extends Expansion {
    private NPCManager npcManager = null;
    private SentinelManager sentinelManager = null;
    private CLStuff clStuff;
    private WorldGuard wg;
    private WorldGuardPlugin wgPlugin;
    
    public CompatibilityCitizens(ICombatLogX plugin) {
        super(plugin);
    }
    
    @Override
    public void onLoad() {
        saveDefaultConfig("citizens-compatibility.yml");
    }
    
    @Override
    public void reloadConfig() {
        reloadConfig("citizens-compatibility.yml");
    }
    
    @Override
    public void onEnable() {
        Logger logger = getLogger();
        ICombatLogX plugin = getPlugin();
        ExpansionManager expansionManager = plugin.getExpansionManager();
        
        if (checkForCitizens()) {
            logger.info("Could not find the Citizens plugin.");
            logger.info("This expansion will be automatically disabled.");
            expansionManager.disableExpansion(this);
            return;
        }
        
        this.npcManager = new NPCManager(this);
        this.npcManager.registerTrait();
        
        if (checkForSentinel()) {
            this.sentinelManager = new SentinelManager(this);
            this.sentinelManager.onEnable();
        }
        
        expansionManager.registerListener(this, new ListenerCombat(this));
        expansionManager.registerListener(this, new ListenerDamageDeath(this));
        expansionManager.registerListener(this, new ListenerLogin(this));
        expansionManager.registerListener(this, new ListenerPunish(this));
        
        // 1.11+ Totem of Undying
        int minorVersion = VersionUtil.getMinorVersion();
        if (minorVersion >= 11) expansionManager.registerListener(this, new ListenerResurrect(this));
    }
    
    @Override
    public void onDisable() {
        if (this.npcManager == null) return;
        this.npcManager.onDisable();
    }
    
    public NPCManager getNPCManager() {
        return this.npcManager;
    }
    
    public SentinelManager getSentinelManager() {
        return this.sentinelManager;
    }
    
    public boolean isKeepInventoryRegion(OfflinePlayer player, Location location) {
        if (clStuff == null)
            clStuff = (CLStuff) Bukkit.getPluginManager().getPlugin("CLStuff");
        if (wg == null)
            wg = WorldGuard.getInstance();
        if (wgPlugin == null)
            wgPlugin = WorldGuardPlugin.inst();
        
        LocalPlayer wgPlayer = wgPlugin.wrapOfflinePlayer(player);
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(location);
        RegionQuery query = wg.getPlatform().getRegionContainer().createQuery();
        
        return query.queryState(wgLoc, wgPlayer, WGNoDropFlag.getNoDropFlag()) == StateFlag.State.ALLOW;
    }
    
    public boolean isExcludedItem(ItemStack item) {
        return clStuff.getNoDropFlag().getExcluded().stream().anyMatch(i -> i.isSimilar(item));
    }
    
    private boolean checkForCitizens() {
        PluginManager manager = Bukkit.getPluginManager();
        if (!manager.isPluginEnabled("Citizens")) return true;
        
        Plugin plugin = manager.getPlugin("Citizens");
        if (plugin == null) return true;
        
        PluginDescriptionFile description = plugin.getDescription();
        String fullName = description.getFullName();
        
        Logger logger = getLogger();
        logger.info("Successfully hooked into " + fullName);
        return false;
    }
    
    private boolean checkForSentinel() {
        PluginManager manager = Bukkit.getPluginManager();
        if (!manager.isPluginEnabled("Sentinel")) return false;
        
        Plugin plugin = manager.getPlugin("Sentinel");
        if (plugin == null) return false;
        
        PluginDescriptionFile description = plugin.getDescription();
        String fullName = description.getFullName();
        
        Logger logger = getLogger();
        logger.info("Successfully hooked into " + fullName);
        return true;
    }
}