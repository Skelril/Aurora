/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.custom;

public enum ItemSource {
    SHNUGGLES_PRIME("Shnuggles Prime"),
    WILDERNESS_MOBS("Wilderness Mobs"),
    PATIENT_X("Patient X"),
    WISHING_WELL("Wishing Well",
            SHNUGGLES_PRIME,
            WILDERNESS_MOBS,
            PATIENT_X
    ),
    MARKET("Market"),
    ARROW_FISHING("Arrow Fishing"),
    GOLD_RUSH("Gold Rush");

    private String friendlyName;
    private ItemSource[] subSources;
    private ItemSource(String friendlyName, ItemSource... itemSource) {
        this.friendlyName = friendlyName;
        subSources = itemSource;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public ItemSource[] getSubSources() {
        return subSources;
    }
}
