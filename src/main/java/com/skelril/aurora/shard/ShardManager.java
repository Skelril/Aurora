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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShardManager {

    private Set<String> activeShards = new HashSet<>();

    private World world;
    private RegionManager rgManager;

    public ShardManager(World world, RegionManager rgManager) {
        this.world = world;
        this.rgManager = rgManager;
    }

    public boolean isActiveRegion(String name) {
        return activeShards.contains(name);
    }

    public <T extends ShardInstance> T findOrCreateInstance(Shard<T> shard) {
        T instance = searchFor(shard);
        if (instance == null) {
            instance = create(shard);
        }
        activeShards.add(instance.getRegion().getId());
        return instance;
    }

    public void unloadInstance(ShardInstance instance) {
        instance.cleanUp();
        activeShards.remove(instance.getRegion().getId());
    }

    private <T extends ShardInstance> T searchFor(Shard<T> shard) {
        int highestHeld = 0;
        for (Map.Entry<String, ProtectedRegion> entry : rgManager.getRegions().entrySet()) {
            String shardName = shard.getRGName();
            if (entry.getKey().startsWith(shardName)) {
                shard.setQuantity(++highestHeld);
                if (!activeShards.contains(entry.getKey())) {
                    T shardInst = shard.load(world, entry.getValue());
                    shardInst.prepare();
                    return shardInst;
                }
            }
        }
        return null;
    }

    private <T extends ShardInstance> T create(Shard<T> shard) {
        ProtectedRegion targetRG;
        Vector nextSearchPt = new Vector(0, 0, 0);
        ShardEditor editor = shard.getEditor();
        do {
            targetRG = constructRegion(shard, nextSearchPt);
            nextSearchPt = nextSearchPt.add(editor.getDimensions().setY(0));
        } while (isCollision(targetRG));
        editor.create(world, targetRG);
        return shard.load(world, storeRegion(shard, targetRG));
    }

    private boolean isCollision(ProtectedRegion region) {
        return rgManager.getApplicableRegions(region).size() > 0;
    }

    private ProtectedRegion constructRegion(Shard shard, Vector targetPt) {
        return new ProtectedCuboidRegion(
                shard.getRGName() + '-' + shard.getQuantity(),
                new BlockVector(targetPt),
                new BlockVector(targetPt.add(shard.getEditor().getDimensions().subtract(1, 1, 1)))
        );
    }

    private ProtectedRegion storeRegion(Shard shard, ProtectedRegion region) {
        region.setFlags(shard.getFlagProfile().construct());
        rgManager.addRegion(region);
        try {
            rgManager.save();
            shard.modifyQuantity(1);
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return region;
    }
}
