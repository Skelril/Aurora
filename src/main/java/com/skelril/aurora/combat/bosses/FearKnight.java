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

package com.skelril.aurora.combat.bosses;

import com.sk89q.commandbook.CommandBook;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.detail.WBossDetail;
import com.skelril.aurora.combat.bosses.instruction.*;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.implementations.FearSwordImpl;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.Server;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.skelril.aurora.items.custom.CustomItems.*;

public class FearKnight {
    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager fearKnight = new BossManager();

    public FearKnight() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(fearKnight));
        setupFearKnight();
    }

    public void bind(Zombie entity, WBossDetail detail) {
        fearKnight.bind(new Boss(entity, detail));
    }

    private void setupFearKnight() {
        BindProcessor bindProcessor = fearKnight.getBindProcessor();
        bindProcessor.addInstruction(new DynamicHPInstruction("Fear Knight") {
            @Override
            public double getHealth(EntityDetail detail) {
                return 20 * 30 * WBossDetail.getLevel(detail);
            }
        });
        bindProcessor.addInstruction(condition -> {
            LivingEntity anEntity = condition.getBoss().getEntity();

            EntityEquipment equipment = anEntity.getEquipment();
            equipment.setHelmet(CustomItemCenter.build(GOD_HELMET));
            equipment.setHelmetDropChance(.25F);
            equipment.setChestplate(CustomItemCenter.build(GOD_CHESTPLATE));
            equipment.setChestplateDropChance(.25F);
            equipment.setLeggings(CustomItemCenter.build(GOD_LEGGINGS));
            equipment.setLeggingsDropChance(.25F);
            equipment.setBoots(CustomItemCenter.build(GOD_BOOTS));
            equipment.setBootsDropChance(.25F);

            equipment.setItemInHand(CustomItemCenter.build(FEAR_SWORD));
            equipment.setItemInHandDropChance(.001F);
            return null;
        });

        UnbindProcessor unbindProcessor = fearKnight.getUnbindProcessor();
        unbindProcessor.addInstruction(new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                int baseLevel = WBossDetail.getLevel(detail);
                List<ItemStack> itemStacks = new ArrayList<>();
                for (int i = 0; i < baseLevel; i++) {
                    ItemStack stack;
                    switch (ChanceUtil.getRandom(3)) {
                        case 1:
                            stack = CustomItemCenter.build(GEM_OF_DARKNESS);
                            break;
                        case 2:
                            stack = CustomItemCenter.build(GEM_OF_LIFE);
                            break;
                        case 3:
                            stack = CustomItemCenter.build(IMBUED_CRYSTAL);
                            break;
                        default:
                            return null;
                    }
                    itemStacks.add(stack);
                    itemStacks.add(CustomItemCenter.build(PHANTOM_GOLD));
                }
                if (ChanceUtil.getChance(5 * (100 - baseLevel))) {
                    itemStacks.add(CustomItemCenter.build(PHANTOM_HYMN));
                }
                if (ChanceUtil.getChance(10 * (100 - baseLevel))) {
                    itemStacks.add(CustomItemCenter.build(PHANTOM_CLOCK));
                }
                return itemStacks;
            }
        });

        DamageProcessor damageProcessor = fearKnight.getDamageProcessor();
        damageProcessor.addInstruction(new WDamageModifier());
        damageProcessor.addInstruction(new SpecialWeaponAttack(new FearSwordImpl()));

        DamagedProcessor damagedProcessor = fearKnight.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
