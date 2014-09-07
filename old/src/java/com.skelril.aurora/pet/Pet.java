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

package com.skelril.aurora.pet;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class Pet {

    private final String playerName;
    private final EntityType petType;
    private LivingEntity pet;
    private LivingEntity target = null;

    public Pet(String playerName, EntityType petType, LivingEntity pet) {

        this.playerName = playerName;
        this.petType = petType;
        this.pet = pet;
    }

    public String getOwner() {

        return playerName;
    }

    public EntityType getType() {

        return petType;
    }

    public void setPet(LivingEntity pet) {

        this.pet = pet;
    }

    public LivingEntity getPet() {

        return pet;
    }

    public void setTarget(LivingEntity target) {

        this.target = target;
    }

    public LivingEntity getTarget() {

        return target;
    }
}
