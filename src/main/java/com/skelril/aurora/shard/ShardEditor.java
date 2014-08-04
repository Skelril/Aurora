/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.IOException;

public class ShardEditor {
    private ShardSchematic resolver;

    public ShardEditor(ShardSchematic resolver) {
        this.resolver = resolver;
    }

    public Vector getDimensions() {
        return resolver.getDimensions();
    }

    public void create(World world, ProtectedRegion region) {

        EditSession transaction = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);

        Operation operation;
        try {
            ClipboardHolder holder = resolver.getHolder();
            Region clipReg = holder.getClipboard().getRegion();
            holder.getClipboard().setOrigin(clipReg.getMinimumPoint());
            operation = holder
                    .createPaste(transaction, transaction.getWorld().getWorldData())
                    .to(region.getMinimumPoint())
                    .build();

            Operations.completeLegacy(operation);
        } catch (IOException | MaxChangedBlocksException e) {
            e.printStackTrace();
            transaction.undo(transaction);
        }
    }
}
