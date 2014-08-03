/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.wishingwell;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerAttemptItemWishEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Result result = Result.DENY;
    private final Location target;
    private ItemStack itemStack;


    public PlayerAttemptItemWishEvent(final Player player, Location target, ItemStack itemStack) {
        super(player);
        this.target = target;
        this.itemStack = itemStack;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Location getLocation() {
        return target.clone();
    }

    public void setItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            itemStack = new ItemStack(BlockID.AIR);
        }

        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum Result {
        ALLOW,
        ALLOW_IGNORE,
        DENY
    }
}