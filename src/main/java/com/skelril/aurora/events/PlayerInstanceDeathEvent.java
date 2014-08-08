/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events;

import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerInstanceDeathEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private PlayerRespawnProfile_1_7_10 profile;

    public PlayerInstanceDeathEvent(Player player, PlayerRespawnProfile_1_7_10 profile) {
        super(player);
        this.profile = profile;
    }

    public PlayerRespawnProfile_1_7_10 getProfile() {
        return profile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}