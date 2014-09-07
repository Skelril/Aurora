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
