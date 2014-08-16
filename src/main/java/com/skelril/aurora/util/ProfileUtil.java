/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
