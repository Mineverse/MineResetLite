package com.koletar.jj.mineresetlite.listeners;

import com.koletar.jj.mineresetlite.types.Mine;
import com.koletar.jj.mineresetlite.MineResetLitePlugin;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AllArgsConstructor
public class MineListener implements Listener
{
    private static final PotionEffect NV_EFFECT = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2);
    private MineResetLitePlugin plugin;

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event)
    {
        plugin.handleBlockBreak(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onExplode(BlockExplodeEvent event)
    {
        for (Block block : event.blockList())
        {
            plugin.handleBlockBreak(block);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onExplode(EntityExplodeEvent event)
    {
        for (Block block : event.blockList())
        {
            plugin.handleBlockBreak(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();

        int fX = from.getBlockX();
        int fY = from.getBlockY();
        int fZ = from.getBlockZ();

        int tX = to.getBlockX();
        int tY = to.getBlockY();
        int tZ = to.getBlockZ();

        if (Math.abs(fX - tX) >= 1 || Math.abs(fY - tY) >= 1 ||
                Math.abs(fZ - tZ) >= 1) {
            for (Mine mine : plugin.getMines()) {
                if (mine.getWorld().equals(to.getWorld()) && !mine.isInRegion(fX, fY, fZ) && mine
                        .isInRegion(tX, tY, tZ)) {
                    event.getPlayer().addPotionEffect(NV_EFFECT);
                } else if (mine.isInRegion(fX, fY, fZ) && !mine.isInRegion(tX, tY, tZ)) {
                    event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                }
            }
        }
    }
}
