/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.skelril.aurora.items.custom.Tag;
import org.bukkit.ChatColor;

public class ShardOwnerTag extends Tag {

    private static final ChatColor color = ChatColor.DARK_AQUA;
    private static final String key = "Shard Owner";

    public ShardOwnerTag(String player) {
        super(color, key, player);
    }

    public static ChatColor getTypeColor() {
        return color;
    }

    public static String getTypeKey() {
        return key;
    }
}
