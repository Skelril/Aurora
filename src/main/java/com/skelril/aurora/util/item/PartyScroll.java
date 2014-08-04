/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.shard.ShardType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PartyScroll {

    private ShardType shard;
    private String owner;

    public PartyScroll(ShardType shard, String owner) {
        this.shard = shard;
        this.owner = owner;
    }

    public ShardType getShard() {
        return shard;
    }

    public String getOwner() {
        return owner;
    }

    public static PartyScroll getPartyScroll(ItemStack itemStack) {
        if (itemStack == null || itemStack.getTypeId() != ItemID.PAPER) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();
        String displayName = meta.getDisplayName();
        String[] parts = displayName.split(" - ");
        if (parts.length < 2) {
            return null;
        }
        ShardType shardType = null;
        for (ShardType type : ShardType.values()) {
            if (parts[0].equals(type.getColoredName())) {
                shardType = type;
                break;
            }
        }
        if (shardType == null) return null;
        return new PartyScroll(shardType, parts[1]);
    }

    public ItemStack buildScroll() {
        ItemStack scroll = new ItemStack(ItemID.PAPER);
        ItemMeta meta = scroll.getItemMeta();
        meta.setDisplayName(shard.getColoredName() + " - " + owner);
        scroll.setItemMeta(meta);
        return scroll;
    }
}
