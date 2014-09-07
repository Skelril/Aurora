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

package com.skelril.aurora.util;

import com.skelril.aurora.util.player.PlayerRespawnProfile_1_7_10;
import org.bukkit.entity.Player;

public class ProfileUtil {

    /**
     * Restores a player to a former state, if no former state is provided all inventory
     * contents and experience will be removed.
     *
     * @param player
     * @param identity
     */
    public static void restore(Player player, PlayerRespawnProfile_1_7_10 identity) {
        if (identity == null) {
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0);
            return;
        }

        if (identity.getInvAction() == KeepAction.KEEP) {
            player.getInventory().setContents(identity.getInventoryContents());
        }
        if (identity.getArmorAction() == KeepAction.KEEP) {
            player.getInventory().setArmorContents(identity.getArmorContents());
        }
        if (identity.getLevelAction() == KeepAction.KEEP) {
            player.setLevel(identity.getLevel());
        }
        if (identity.getExperienceAction() == KeepAction.KEEP) {
            player.setExp(identity.getExperience());
        }
    }
}
