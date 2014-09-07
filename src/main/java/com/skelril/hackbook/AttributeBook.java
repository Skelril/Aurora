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

package com.skelril.hackbook;

import com.skelril.hackbook.exceptions.UnsupportedFeatureException;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.IAttribute;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class AttributeBook {

    public enum Attribute {

        MAX_HEALTH(GenericAttributes.maxHealth),
        FOLLOW_RANGE(GenericAttributes.b),
        KNOCKBACK_RESISTANCE(GenericAttributes.c),
        MOVEMENT_SPEED(GenericAttributes.d),
        ATTACK_DAMAGE(GenericAttributes.e);

        public IAttribute attribute;

        Attribute(IAttribute attribute) {

            this.attribute = attribute;
        }

    }

    public static double getAttribute(LivingEntity entity, Attribute attribute) throws UnsupportedFeatureException {

        try {
            EntityInsentient nmsEntity = getNMSEntity(entity);

            return nmsEntity.getAttributeInstance(attribute.attribute).getValue();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new UnsupportedFeatureException();
        }
    }

    public static void setAttribute(LivingEntity entity, Attribute attribute, double value) throws UnsupportedFeatureException {

        try {
            EntityInsentient nmsEntity = getNMSEntity(entity);

            nmsEntity.getAttributeInstance(attribute.attribute).setValue(value);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new UnsupportedFeatureException();
        }
    }

    private static EntityInsentient getNMSEntity(LivingEntity entity) throws UnsupportedFeatureException {

        try {
            return ((EntityInsentient) ((CraftLivingEntity) entity).getHandle());
        } catch (Throwable t) {
            t.printStackTrace();
            throw new UnsupportedFeatureException();
        }
    }
}
