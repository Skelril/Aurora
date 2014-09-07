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
