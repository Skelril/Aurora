/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.shard;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PartyActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final String shardName;
    private final List<Player> players;

    public PartyActivateEvent(String shardName, List<Player> players) {
        this.shardName = shardName;
        this.players = players;
    }

    public String getShardName() {
        return shardName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }
}
