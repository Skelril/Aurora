/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses;

import com.sk89q.commandbook.CommandBook;
import com.skelril.OpenBoss.Boss;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.bosses.detail.WBossDetail;
import com.skelril.aurora.bosses.instruction.HealthPrint;
import com.skelril.aurora.bosses.instruction.WBindInstruction;
import com.skelril.aurora.bosses.instruction.WDamageModifier;
import com.skelril.aurora.bosses.instruction.WDropInstruction;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.DamageUtil;
import com.skelril.aurora.util.EntityUtil;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.skelril.aurora.items.custom.CustomItems.POTION_OF_RESTITUTION;
import static com.skelril.aurora.items.custom.CustomItems.SCROLL_OF_SUMMATION;

public class Fangz {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager fangz = new BossManager();

    public Fangz() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(fangz));
        setupFangz();
    }

    public void bind(Spider entity, WBossDetail detail) {
        fangz.bind(new Boss(entity, detail));
    }

    private void setupFangz() {
        BindProcessor bindProcessor = fangz.getBindProcessor();
        bindProcessor.addInstruction(new WBindInstruction("Fangz") {
            @Override
            public double getHealth(EntityDetail detail) {
                return 20 * 50 * WBossDetail.getLevel(detail);
            }
        });

        UnbindProcessor unbindProcessor = fangz.getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            Entity boss = condition.getBoss().getEntity();
            for (Entity aEntity : boss.getNearbyEntities(7, 4, 7)) {
                if (!(aEntity instanceof LivingEntity)) continue;
                ((LivingEntity) aEntity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 30, 1), true);
                ((LivingEntity) aEntity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 30, 1), true);
            }
            return null;
        });
        unbindProcessor.addInstruction(new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                int baseLevel = WBossDetail.getLevel(detail);
                List<ItemStack> itemStacks = new ArrayList<>();
                for (int i = baseLevel * ChanceUtil.getRandom(3); i > 0; --i) {
                    itemStacks.add(CustomItemCenter.build(POTION_OF_RESTITUTION));
                }
                for (int i = baseLevel * ChanceUtil.getRandom(10); i > 0; --i) {
                    itemStacks.add(CustomItemCenter.build(SCROLL_OF_SUMMATION));
                }
                return itemStacks;
            }
        });

        DamageProcessor damageProcessor = fangz.getDamageProcessor();
        damageProcessor.addInstruction(new WDamageModifier());
        damageProcessor.addInstruction(condition -> {
            LivingEntity boss = condition.getBoss().getEntity();
            Entity eToHit = condition.getAttacked();
            if (!(eToHit instanceof LivingEntity)) return null;
            LivingEntity toHit = (LivingEntity) eToHit;

            EntityDamageByEntityEvent event = condition.getEvent();
            DamageUtil.multiplyFinalDamage(event, 2);
            EntityUtil.heal(boss, event.getDamage());

            toHit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 15, 0), true);
            toHit.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 15, 0), true);
            return null;
        });

        DamagedProcessor damagedProcessor = fangz.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
