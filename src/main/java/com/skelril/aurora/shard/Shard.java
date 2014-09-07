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

package com.skelril.aurora.shard;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class Shard<T extends ShardInstance> {

    private final ShardType shard;
    private final ShardEditor editor;
    private int quantity = 0;

    public Shard(ShardType shard, ShardEditor editor) {
        this.shard = shard;
        this.editor = editor;
    }

    public ShardType getType() {
        return shard;
    }
    public abstract FlagProfile getFlagProfile();

    public String getRGName() {
        return shard.getName().toLowerCase().replace(" ", "-");
    }

    public ShardEditor getEditor() {
        return editor;
    }
    public abstract T load(World world, ProtectedRegion region);

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void modifyQuantity(int quantity) {
        this.quantity += quantity;
    }
}
