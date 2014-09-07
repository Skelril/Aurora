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
import com.sk89q.worldedit.blocks.BlockID;
import com.skelril.OpenBoss.*;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.detail.WBossDetail;
import com.skelril.aurora.combat.bosses.instruction.*;
import com.skelril.aurora.items.specialattack.attacks.melee.guild.rogue.Nightmare;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.DamageUtil;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LostRogue {
    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager lostRogue = new BossManager();

    public LostRogue() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(lostRogue));
        setupLostRogue();
    }

    public void bind(Zombie entity, WBossDetail detail) {
        lostRogue.bind(new Boss(entity, detail));
    }

    private void setupLostRogue() {
        BindProcessor bindProcessor = lostRogue.getBindProcessor();
        bindProcessor.addInstruction(new DynamicHPInstruction("Lost Rogue") {
            @Override
            public double getHealth(EntityDetail detail) {
                return 20 * 75 * WBossDetail.getLevel(detail);
            }
        });

        UnbindProcessor unbindProcessor = lostRogue.getUnbindProcessor();
        DropInstruction instruction = new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                int baseLevel = WBossDetail.getLevel(detail);
                List<ItemStack> itemStacks = new ArrayList<>();
                for (int i = ChanceUtil.getRandom(baseLevel) * ChanceUtil.getRandom(5); i > 0; --i) {
                    itemStacks.add(new ItemStack(BlockID.GOLD_BLOCK, ChanceUtil.getRandom(32)));
                }
                return itemStacks;
            }
        };
        unbindProcessor.addInstruction(new ExplosiveUnbind(new InstructionResult<>(instruction), true, false) {
            @Override
            public float getExplosionStrength(EntityDetail detail) {
                double min = 4;
                double max = 9;
                return (float) Math.min(max, Math.max(min, (min + WBossDetail.getLevel(detail)) / 2));
            }
        });

        DamageProcessor damageProcessor = lostRogue.getDamageProcessor();
        damageProcessor.addInstruction(new WDamageModifier());
        damageProcessor.addInstruction(condition -> {
            Entity eToHit = condition.getAttacked();
            if (!(eToHit instanceof LivingEntity)) return null;
            LivingEntity toHit = (LivingEntity) eToHit;
            toHit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 15, 2), true);

            DamageUtil.multiplyFinalDamage(condition.getEvent(), 1.5);
            return null;
        });
        damageProcessor.addInstruction(condition -> {
            Entity boss = condition.getBoss().getEntity();
            Entity eToHit = condition.getAttacked();
            if (!(eToHit instanceof LivingEntity)) return null;
            LivingEntity toHit = (LivingEntity) eToHit;
            toHit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 15, 2), true);

            if (ChanceUtil.getChance(5)) {
                new Nightmare((LivingEntity) boss, toHit).activate();
            }
            return null;
        });

        DamagedProcessor damagedProcessor = lostRogue.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
        damagedProcessor.addInstruction(condition -> {
            LivingEntity boss = condition.getBoss().getEntity();
            EntityDamageEvent event = condition.getEvent();
            if (event instanceof EntityDamageByEntityEvent) {
                Entity hitBy = ((EntityDamageByEntityEvent) event).getDamager();
                if (hitBy instanceof LivingEntity) {
                    boss.getActivePotionEffects().clear();
                    boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 25, 2), true);
                }
                DamageUtil.multiplyFinalDamage(event, .75);
            }
            return null;
        });
        damagedProcessor.addInstruction(new BlipDefense());
    }
}
