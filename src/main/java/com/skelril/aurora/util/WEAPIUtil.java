/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.World;

public class WEAPIUtil {
    public static World getWorld(com.sk89q.worldedit.world.World world) {
        if (world instanceof BukkitWorld) {
            return ((BukkitWorld) world).getWorld();
        }
        return null;
    }
}
