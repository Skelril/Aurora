/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShardEditor {
    private EditSessionFactory factory;
    private ShardSchematic resolver;

    public ShardEditor(EditSessionFactory factory, ShardSchematic resolver) {
        this.factory = factory;
        this.resolver = resolver;
    }

    public Vector getDimensions() {
        return resolver.getDimensions();
    }

    public void create(World world, ProtectedRegion region) {

        EditSession transaction = factory.getEditSession(world, -1);

        Operation operation = resolver.getHolder()
                .createPaste(transaction, transaction.getWorld().getWorldData())
                .to(region.getMinimumPoint())
                .build();
        try {
            Operations.completeLegacy(operation);
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
            transaction.undo(transaction);
        }
    }
}
