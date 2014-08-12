/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import org.bukkit.ChatColor;

public enum ShardType {

    // Bosses
    SHNUGGLES_PRIME(ChatColor.BLUE, "Shnuggles Prime", SubType.BOSS),
    PATIENT_X(ChatColor.DARK_RED, "Patient X", SubType.BOSS),

    // Minigames
    GOLD_RUSH(ChatColor.GOLD, "Gold Rush", SubType.MINIGAME);

    private ChatColor color;
    private String name;
    private SubType subType;

    private ShardType(ChatColor color, String name, SubType subType) {
        this.color = color;
        this.name = name;
        this.subType = subType;
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