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

package com.skelril.aurora.prayer.PrayerFX;

import com.sk89q.worldedit.blocks.BlockID;
import com.skelril.aurora.prayer.PrayerType;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlassBoxFX extends AbstractEffect {

    @Override
    public PrayerType getType() {

        return PrayerType.GLASSBOX;
    }

    @Override
    public void add(Player player) {

        LocationUtil.toGround(player);

        List<Location> queList = new ArrayList<>();
        for (Location loc : Arrays.asList(player.getLocation(), player.getEyeLocation())) {
            for (BlockFace face : EnvironmentUtil.getNearbyBlockFaces()) {
                if (face == BlockFace.SELF) continue;
                queList.add(loc.getBlock().getRelative(face).getLocation());
            }
        }
        for (Location loc : queList) {
            player.sendBlockChange(loc, BlockID.GLASS, (byte) 0);
        }
    }

    @Override
    public void clean(Player player) {

        // Nothing to do here
    }
}
