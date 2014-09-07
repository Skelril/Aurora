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

import org.bukkit.ChatColor;

public enum ShardType {

    // Bosses
    SHNUGGLES_PRIME(ChatColor.BLUE, "Shnuggles Prime", SubType.BOSS, true),
    PATIENT_X(ChatColor.DARK_RED, "Patient X", SubType.BOSS, true),
    FREAKY_FOUR(ChatColor.DARK_RED, "Freaky Four", SubType.BOSS, false, 1),

    // Minigames
    CATACOMBS(ChatColor.DARK_RED, "Catacombs", SubType.MINIGAME, false),
    GOLD_RUSH(ChatColor.GOLD, "Gold Rush", SubType.MINIGAME, false),
    CURSED_MINE(ChatColor.GOLD, "Cursed Mine", SubType.MINIGAME, true);

    private ChatColor color;
    private String name;
    private SubType subType;
    private boolean rejoin;
    private int maxPlayers;

    private ShardType(ChatColor color, String name, SubType subType, boolean rejoin) {
        this(color, name, subType, rejoin, -1);
    }

    private ShardType(ChatColor color, String name, SubType subType, boolean rejoin, int maxPlayers) {
        this.color = color;
        this.name = name;
        this.subType = subType;
        this.rejoin = rejoin;
        this.maxPlayers = maxPlayers;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getColoredName() {
        return color + name;
    }

    public SubType getSubType() {
        return subType;
    }

    public boolean allowsRejoin() {
        return rejoin;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public String toString() {
        return color + name;
    }

    public static ShardType matchFrom(String string) {
        for (ShardType type : values()) {
            if (type.getColoredName().equals(string)) {
                return type;
            }
        }
        return null;
    }

    public enum SubType {
        BOSS("Boss"),
        MINIGAME("Minigame");

        private String properName;

        private SubType(String properName) {
            this.properName = properName;
        }

        public String getProperName() {
            return properName;
        }
    }
}