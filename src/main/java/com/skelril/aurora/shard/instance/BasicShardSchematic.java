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

package com.skelril.aurora.shard.instance;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.registry.WorldData;
import com.skelril.aurora.shard.ShardSchematic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BasicShardSchematic implements ShardSchematic {

    private final File targetFile;
    private final WorldData worldData;
    private final Vector dimensions;

    public BasicShardSchematic(String shardName, WorldData worldData) throws IOException {
        this(new File(CommandBook.inst().getDataFolder(), "shards/" + shardName + "/" + "arena.schematic"), worldData);
    }

    public BasicShardSchematic(File targetFile, WorldData worldData) throws IOException {
        this.targetFile = targetFile;
        this.worldData = worldData;

        dimensions = getHolder().getClipboard().getDimensions();
    }

    @Override
    public ClipboardHolder getHolder() throws IOException {
        try (FileInputStream fis = new FileInputStream(targetFile)) {
            try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(bis);
                Clipboard clipboard = reader.read(worldData);
                return new ClipboardHolder(clipboard, worldData);
            }
        }
    }

    @Override
    public Vector getDimensions() {
        return dimensions;
    }
}
