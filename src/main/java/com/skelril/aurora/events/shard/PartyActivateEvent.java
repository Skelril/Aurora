/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.events.shard;

import com.skelril.aurora.shard.ShardInstance;
import com.skelril.aurora.shard.ShardType;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PartyActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final ShardType shard;
    private final List<Player> players;

    private ShardInstance<?> instance;

    public PartyActivateEvent(ShardType shard, List<Player> players) {
        this.shard = shard;
        this.players = players;
    }

    public boolean hasInstance() {
        return instance != null;
    }

    public ShardInstance<?> getInstance() {
        return instance;
    }

    public void setInstance(ShardInstance instance) {
        Validate.notNull(instance);
        Validate.isTrue(
                shard.equals(instance.getMaster().getType()),
                "The instance must be the same type as the shard."
        );
        if (this.instance != null) {
            throw new IllegalStateException("The instance has already been set!");
        }
        this.instance = instance;
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
