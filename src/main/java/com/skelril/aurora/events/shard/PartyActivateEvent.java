/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.shard;

import com.skelril.aurora.shards.ShardType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PartyActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final ShardType shard;
    private final List<Player> players;

    public PartyActivateEvent(ShardType shard, List<Player> players) {
        this.shard = shard;
        this.players = players;
    }

    public ShardType getShard() {
        return shard;
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
