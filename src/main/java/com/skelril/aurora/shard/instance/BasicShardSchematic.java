/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
