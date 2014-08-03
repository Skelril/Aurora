/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.wishingwell;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerItemWishEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PlayerAttemptItemWishEvent parentEvent;
    private boolean cancelled = false;
    private ItemStack itemStack;


    public PlayerItemWishEvent(final Player player, final PlayerAttemptItemWishEvent parentEvent, ItemStack itemStack) {
        super(player);
        this.parentEvent = parentEvent;
        this.itemStack = itemStack;
    }

    public PlayerAttemptItemWishEvent getParentEvent() {
        return parentEvent;
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

    @Override
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
}
