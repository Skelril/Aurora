package com.skelril.aurora.events;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Author: Turtle9598
 */
public class ThrowPlayerEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public ThrowPlayerEvent(Player player) {

        super(player);
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}