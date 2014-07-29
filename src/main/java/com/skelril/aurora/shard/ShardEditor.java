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
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ShardEditor {
    private EditSessionFactory factory;
    private ClipboardHolder holder;

    public ShardEditor(EditSessionFactory factory, ClipboardHolder holder) {
        this.factory = factory;
        this.holder = holder;
    }

    public Vector getDemensions() {
        return holder.getClipboard().getDimensions();
    }

    public void create(World world, ProtectedRegion region) {

        EditSession transaction = factory.getEditSession(world, -1);

        Operation operation = holder
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
