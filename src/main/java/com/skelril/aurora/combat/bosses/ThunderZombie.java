/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.combat.bosses;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.OpenBoss.*;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.combat.bosses.detail.GenericDetail;
import com.skelril.aurora.combat.bosses.instruction.*;
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ThunderZombie {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager thunderZombie = new BossManager();

    public ThunderZombie() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(thunderZombie));
        setupThunderZombie();
    }

    public void bind(Zombie entity) {
        thunderZombie.bind(new Boss(entity, new GenericDetail()));
    }

    private void setupThunderZombie() {
        BindProcessor bindProcessor = thunderZombie.getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Thor Zombie", 500));

        UnbindProcessor unbindProcessor = thunderZombie.getUnbindProcessor();
        DropInstruction instruction = new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                List<ItemStack> itemStacks = new ArrayList<ItemStack>();
                for (int i = ChanceUtil.getRangedRandom(12, 150); i > 0; --i) {
                    itemStacks.add(new ItemStack(ItemID.GOLD_BAR));
                }
                return itemStacks;
            }
        };
        unbindProcessor.addInstruction(new ExplosiveUnbind(new InstructionResult<>(instruction), false, false) {
            @Override
            public float getExplosionStrength(EntityDetail detail) {
                return 4F;
            }
        });
        
        DamageProcessor damageProcessor = thunderZombie.getDamageProcessor();
        damageProcessor.addInstruction(condition -> {
            Entity boss = condition.getBoss().getEntity();
            Entity toHit = condition.getAttacked();
            toHit.setVelocity(boss.getLocation().getDirection().multiply(2));

            server.getScheduler().runTaskLater(inst, () -> {
                Location targetLocation = toHit.getLocation();
                server.getScheduler().runTaskLater(inst, () -> {
                    targetLocation.getWorld().strikeLightning(targetLocation);
                }, 15);
            }, 30);
            return null;
        });

        DamagedProcessor damagedProcessor = thunderZombie.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }
}
