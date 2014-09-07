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
import com.skelril.aurora.combat.bosses.instruction.DynamicHPInstruction;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.WDamageModifier;
import com.skelril.aurora.combat.bosses.instruction.WDropInstruction;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.skelril.aurora.items.custom.CustomItems.*;

public class StormBringer {
    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager stormBringer = new BossManager();

    public StormBringer() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(stormBringer));
        setupFangz();
    }

    public void bind(Skeleton entity, WBossDetail detail) {
        stormBringer.bind(new Boss(entity, detail));
    }

    private void setupFangz() {
        BindProcessor bindProcessor = stormBringer.getBindProcessor();
        bindProcessor.addInstruction(new DynamicHPInstruction("Storm Bringer") {
            @Override
            public double getHealth(EntityDetail detail) {
                return 20 * 30 * WBossDetail.getLevel(detail);
            }
        });

        UnbindProcessor unbindProcessor = stormBringer.getUnbindProcessor();
        unbindProcessor.addInstruction(new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                int baseLevel = WBossDetail.getLevel(detail);
                List<ItemStack> itemStacks = new ArrayList<>();
                itemStacks.add(CustomItemCenter.build(BAT_BOW));
                for (int i = baseLevel * ChanceUtil.getRandom(3); i > 0; --i) {
                    itemStacks.add(CustomItemCenter.build(BARBARIAN_BONE));
                }
                for (int i = baseLevel * ChanceUtil.getRandom(9); i > 0; --i) {
                    itemStacks.add(CustomItemCenter.build(GOD_FISH));
                }
                return itemStacks;
            }
        });

        DamageProcessor damageProcessor = stormBringer.getDamageProcessor();
        damageProcessor.addInstruction(new WDamageModifier());
        damageProcessor.addInstruction(condition -> {
            Boss boss = condition.getBoss();
            Entity eToHit = condition.getAttacked();
            if (!(eToHit instanceof LivingEntity) || !condition.getEvent().getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) return null;
            LivingEntity toHit = (LivingEntity) eToHit;

            Location target = toHit.getLocation();
            for (int i = WBossDetail.getLevel(boss.getDetail()) * ChanceUtil.getRangedRandom(1, 10); i >= 0; --i) {
                server.getScheduler().runTaskLater(inst, () -> {
                    // Simulate a lightning strike
                    LightningStrike strike = target.getWorld().strikeLightningEffect(target);
                    for (Entity e : strike.getNearbyEntities(2, 4, 2)) {
                        if (!e.isValid() || !(e instanceof LivingEntity)) continue;
                        // Pig Zombie
                        if (e instanceof Pig) {
                            e.getWorld().spawn(e.getLocation(), PigZombie.class);
                            e.remove();
                            continue;
                        }
                        // Creeper
                        if (e instanceof Creeper) {
                            ((Creeper) e).setPowered(true);
                        }
                        ((LivingEntity) e).damage(1, boss.getEntity());
                    }
                }, (5 * (6 + i)));
            }
            return null;
        });

        DamagedProcessor damagedProcessor = stormBringer.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
