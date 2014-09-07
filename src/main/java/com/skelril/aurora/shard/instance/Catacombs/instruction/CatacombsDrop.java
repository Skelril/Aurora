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

package com.skelril.aurora.shard.instance.Catacombs.instruction;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.aurora.combat.bosses.instruction.DropInstruction;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;

public class CatacombsDrop extends DropInstruction {

    private double mod;

    public CatacombsDrop() {
        this(1);
    }

    public CatacombsDrop(double mod) {
        this.mod = mod;
    }

    @Override
    public List<ItemStack> getDrops(EntityDetail detail) {
        List<ItemStack> drops = new ArrayList<>();
        for (int i = getFrom(detail).getWave(); i > 0; --i) {
            if (ChanceUtil.getChance(180 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.DIVINE_COMBAT_POTION));
            } else if (ChanceUtil.getChance(170 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.HOLY_COMBAT_POTION));
            } else if (ChanceUtil.getChance(160 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.EXTREME_COMBAT_POTION));
            } else if (ChanceUtil.getChance(150 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.COMBAT_POTION));
            } else if (ChanceUtil.getChance(140 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.LESSER_COMBAT_POTION));
            }
            if (ChanceUtil.getChance(3000 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.PHANTOM_CLOCK));
            }
            if (ChanceUtil.getChance(50 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.IMBUED_CRYSTAL));
            }
            if (ChanceUtil.getChance(50 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.GEM_OF_DARKNESS));
            }
            if (ChanceUtil.getChance(35 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.GEM_OF_LIFE));
            }
            if (ChanceUtil.getChance(20 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.PHANTOM_GOLD, ChanceUtil.getRandom(5)));
            }
            if (ChanceUtil.getChance(10 * mod)) {
                drops.add(CustomItemCenter.build(CustomItems.BARBARIAN_BONE, ChanceUtil.getRandom(3)));
            }
            if (ChanceUtil.getChance(1500 * mod)) {
                switch (ChanceUtil.getRandom(4)) {
                    case 1:
                        drops.add(CustomItemCenter.build(CustomItems.FEAR_SWORD));
                        break;
                    case 2:
                        drops.add(CustomItemCenter.build(CustomItems.FEAR_BOW));
                        break;
                    case 3:
                        drops.add(CustomItemCenter.build(CustomItems.UNLEASHED_SWORD));
                        break;
                    case 4:
                        drops.add(CustomItemCenter.build(CustomItems.UNLEASHED_BOW));
                        break;
                }
            }
        }
        return drops;
    }
}
