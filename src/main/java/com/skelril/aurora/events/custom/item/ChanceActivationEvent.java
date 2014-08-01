/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.custom.item;

import com.sk89q.worldedit.event.Cancellable;
import net.minecraft.util.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ChanceActivationEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private int chance;

    public ChanceActivationEvent(Player who, int chance) {
        super(who);
        setChance(chance);
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        Validate.isTrue(chance > 0, "Chance must be greater than 0.");
        this.chance = chance;
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
