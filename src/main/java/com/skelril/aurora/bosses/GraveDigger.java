/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.bosses;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import com.skelril.aurora.util.ChanceUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GraveDigger {
    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private BossManager graveDigger = new BossManager();

    public GraveDigger() {
        //noinspection AccessStaticViaInstance
        inst.registerEvents(new BossListener(graveDigger));
        setupFangz();
    }

    public void bind(Skeleton entity, WBossDetail detail) {
        graveDigger.bind(new Boss(entity, detail));
    }

    private void setupFangz() {
        BindProcessor bindProcessor = graveDigger.getBindProcessor();
        bindProcessor.addInstruction(new WBindInstruction("Grave Digger") {
            @Override
            public double getHealth(EntityDetail detail) {
                return 20 * 43 * WBossDetail.getLevel(detail);
            }
        });

        UnbindProcessor unbindProcessor = graveDigger.getUnbindProcessor();
        unbindProcessor.addInstruction(new WDropInstruction() {
            @Override
            public List<ItemStack> getDrops(EntityDetail detail) {
                int baseLevel = WBossDetail.getLevel(detail);
                List<ItemStack> itemStacks = new ArrayList<>();
                for (int i = baseLevel * ChanceUtil.getRandom(2); i > 0; --i) {
                    itemStacks.add(new ItemStack(BlockID.TNT, ChanceUtil.getRandom(16)));
                }
                for (int i = baseLevel * ChanceUtil.getRandom(4); i > 0; --i) {
                    itemStacks.add(new ItemStack(ItemID.DIAMOND, ChanceUtil.getRandom(32)));
                }
                for (int i = baseLevel * ChanceUtil.getRandom(4); i > 0; --i) {
                    itemStacks.add(new ItemStack(ItemID.EMERALD, ChanceUtil.getRandom(32)));
                }
                for (int i = baseLevel * ChanceUtil.getRandom(8); i > 0; --i) {
                    itemStacks.add(new ItemStack(ItemID.INK_SACK, ChanceUtil.getRandom(64), (short) 4));
                }
                return itemStacks;
            }
        });

        DamageProcessor damageProcessor = graveDigger.getDamageProcessor();
        damageProcessor.addInstruction(new WDamageModifier());
        damageProcessor.addInstruction(condition -> {
            Entity eToHit = condition.getAttacked();
            if (!(eToHit instanceof LivingEntity)) return null;
            LivingEntity toHit = (LivingEntity) eToHit;

            Location target = toHit.getLocation();
            makeSphere(target, 3, 3, 3);
            for (int i = 0; i < WBossDetail.getLevel(condition.getBoss().getDetail()); ++i) {
                target.getWorld().spawn(target, TNTPrimed.class);
            }
            return null;
        });

        DamagedProcessor damagedProcessor = graveDigger.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
        damagedProcessor.addInstruction(condition -> {
            Boss boss = condition.getBoss();
            EntityDamageEvent event = condition.getEvent();
            if (event instanceof EntityDamageByEntityEvent) {
                Entity toHit = ((EntityDamageByEntityEvent) event).getDamager();
                if (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                    Location target = toHit.getLocation();
                    makeSphere(target, 3, 3, 3);
                    for (int i = 0; i < 4 * WBossDetail.getLevel(boss.getDetail()); ++i) {
                        target.getWorld().spawn(target, TNTPrimed.class);
                    }
                    // target.getWorld().spawn(target, Pig.class).setCustomName("Help Me!");
                }
            }
            return null;
        });
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    public void makeSphere(Location pos, double radiusX, double radiusY, double radiusZ) {

        BaseBlock block = new BaseBlock(1);

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break;
                    }

                    if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                        continue;
                    }

                    setBlock(pos.clone().add(x, y, z), block);
                    setBlock(pos.clone().add(-x, y, z), block);
                    setBlock(pos.clone().add(x, -y, z), block);
                    setBlock(pos.clone().add(x, y, -z), block);
                    setBlock(pos.clone().add(-x, -y, z), block);
                    setBlock(pos.clone().add(x, -y, -z), block);
                    setBlock(pos.clone().add(-x, y, -z), block);
                    setBlock(pos.clone().add(-x, -y, -z), block);
                }
            }
        }
    }

    private WorldGuardPlugin WG = null;

    private void setBlock(Location l, BaseBlock b) {
        if (WG == null) WG = WGBukkit.getPlugin();
        if (WG.getRegionManager(l.getWorld()).getApplicableRegions(l).size() > 0) return;
        Block blk = l.getBlock();
        if (blk.getType() != Material.AIR) return;
        blk.setTypeIdAndData(b.getType(), (byte) b.getData(), true);
    }
}
