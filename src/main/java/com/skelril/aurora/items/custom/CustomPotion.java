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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class CustomPotion extends CustomItem {
    List<Potion> effects = new ArrayList<>();

    public CustomPotion(CustomItems item, ItemStack base) {
        super(item, base);
        assert base.getType() == Material.POTION;
    }

    public CustomPotion(CustomPotion potion) {
        super(potion);
        effects.addAll(potion.getEffects());
    }

    public void addEffect(Potion effect) {
        effects.add(effect);
    }

    public void addEffect(PotionEffectType type, int time, int level) {
        addEffect(new Potion(type, time, level));
    }

    public List<Potion> getEffects() {
        return effects;
    }

    @Override
    public void accept(CustomItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ItemStack build() {
        ItemStack base = super.build();
        PotionMeta meta = (PotionMeta) base.getItemMeta();
        for (Potion potion : effects) {
            meta.addCustomEffect(new PotionEffect(potion.getType(), potion.getTime(), potion.getLevel()), false);
        }
        base.setItemMeta(meta);
        return base;
    }
}
