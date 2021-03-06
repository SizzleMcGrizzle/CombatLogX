package com.SirBlobman.combatlogx.expansion.cheat.prevention.listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import com.SirBlobman.combatlogx.expansion.cheat.prevention.CheatPrevention;

public class ListenerRiptide extends CheatPreventionListener {
    public ListenerRiptide(CheatPrevention expansion) {
        super(expansion);
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if(!player.isRiptiding()) return;

        FileConfiguration config = getConfig();
        if(!config.getBoolean("items.prevent-riptide")) return;
        if(!isInCombat(player)) return;

        e.setCancelled(true);
        sendMessageWithCooldown(player, "cheat-prevention.no-riptide");
    }
}