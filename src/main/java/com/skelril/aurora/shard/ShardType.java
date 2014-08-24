/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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