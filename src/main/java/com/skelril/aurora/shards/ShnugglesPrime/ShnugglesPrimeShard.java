/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.ShnugglesPrime;

import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.WishingWellComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.bosses.instruction.DropInstruction;
import com.skelril.aurora.bosses.instruction.PersistenceInstruction;
import com.skelril.aurora.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.events.anticheat.ThrowPlayerEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.FlagProfile;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EntityUtil;
import com.skelril.aurora.util.item.BookUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static com.sk89q.commandbook.CommandBook.*;
import static com.skelril.aurora.items.custom.CustomItems.*;
import static com.skelril.aurora.shards.ShnugglesPrime.ShnugglesPrimeInstance.getInst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class ShnugglesPrimeShard extends Shard<ShnugglesPrimeInstance> {

    private static final double scalOffst = 3;

    private static AdminComponent admin;
    private static PrayerComponent prayers;

    private FlagProfile flagProfile = new FlagProfile();
    private BossManager manager = new BossManager();

    public ShnugglesPrimeShard(ShardEditor editor, AdminComponent admin, PrayerComponent prayers) {
        super(ShardType.SHNUGGLES_PRIME, editor);
        setUpManager();
        ShnugglesPrimeShard.admin = admin;
        ShnugglesPrimeShard.prayers = prayers;
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public PrayerComponent getPrayers() {
        return prayers;
    }

    public BossManager getBossManager() {
        return manager;
    }

    private static Set<EntityDamageByEntityEvent.DamageCause> acceptedReasons = new HashSet<>();

    static {
        acceptedReasons.add(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
        acceptedReasons.add(EntityDamageEvent.DamageCause.PROJECTILE);
        acceptedReasons.add(EntityDamageEvent.DamageCause.MAGIC);
        acceptedReasons.add(EntityDamageEvent.DamageCause.THORNS);
    }

    private void setUpManager() {

        flagProfile.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);

        registerEvents(new BossListener(manager));

        BindProcessor bindProcessor = manager.getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Shnuggles Prime", 750));
        bindProcessor.addInstruction(new PersistenceInstruction());
        bindProcessor.addInstruction(condition -> {
            ChatUtil.sendWarning(getInst(condition.getBoss().getDetail()).getContained(Player.class), "I live again!");
            return null;
        });

        UnbindProcessor unbindProcessor = manager.getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            ShnugglesPrimeInstance inst = getInst(condition.getBoss().getDetail());
            Collection<Player> players = inst.getContained(Player.class);
            Player player = null;
            int amt = players.size();
            int required = ChanceUtil.getRandom(13) + 3;
            // Figure out if someone has Barbarian Bones
            if (amt != 0) {
                for (Player aPlayer : players) {
                    if (admin.isAdmin(aPlayer)) continue;
                    if (ItemUtil.countItemsOfName(aPlayer.getInventory().getContents(), BARBARIAN_BONE) >= required) {
                        player = aPlayer;
                        break;
                    }
                }
            }

            List<ItemStack> drops = new ArrayList<>();

            // Sacrificial drops
            int m = players.size();
            m *= player != null ? 3 : 1;

            drops.addAll(WishingWellComponent.getCalculatedLoot(server().getConsoleSender(), m, 400000));
            drops.addAll(WishingWellComponent.getCalculatedLoot(server().getConsoleSender(), m * 10, 15000));
            drops.addAll(WishingWellComponent.getCalculatedLoot(server().getConsoleSender(), m * 32, 4000));
            // Gold drops
            for (int i = 0; i < Math.sqrt(amt + m) + scalOffst; i++) {
                drops.add(new ItemStack(ItemID.GOLD_BAR, ChanceUtil.getRangedRandom(32, 64)));
            }
            // Unique drops
            if (ChanceUtil.getChance(25) || m > 1 && ChanceUtil.getChance(27 / m)) {
                drops.add(BookUtil.Lore.Monsters.skelril());
            }
            if (ChanceUtil.getChance(138) || m > 1 && ChanceUtil.getChance(84 / m)) {
                if (ChanceUtil.getChance(4)) {
                    drops.add(CustomItemCenter.build(MASTER_SWORD));
                } else {
                    drops.add(CustomItemCenter.build(CORRUPT_MASTER_SWORD));
                }
            }
            if (ChanceUtil.getChance(138) || m > 1 && ChanceUtil.getChance(84 / m)) {
                if (ChanceUtil.getChance(4)) {
                    drops.add(CustomItemCenter.build(MASTER_BOW));
                } else {
                    drops.add(CustomItemCenter.build(CORRUPT_MASTER_BOW));
                }
            }
            if (ChanceUtil.getChance(200) || m > 1 && ChanceUtil.getChance(108 / m)) {
                drops.add(CustomItemCenter.build(MAGIC_BUCKET));
            }
            // Uber rare drops
            if (ChanceUtil.getChance(15000 / m)) {
                drops.add(CustomItemCenter.build(ELDER_CROWN));
            }
            // Add a few Barbarian Bones to the drop list
            drops.add(CustomItemCenter.build(BARBARIAN_BONE, ChanceUtil.getRandom(Math.max(1, amt * 2))));
            // Remove the Barbarian Bones
            if (player != null) {
                int c = ItemUtil.countItemsOfName(player.getInventory().getContents(), BARBARIAN_BONE) - required;
                ItemStack[] nc = ItemUtil.removeItemOfName(player.getInventory().getContents(), BARBARIAN_BONE);
                player.getInventory().setContents(nc);
                int amount = Math.min(c, 64);
                while (amount > 0) {
                    player.getInventory().addItem(CustomItemCenter.build(BARBARIAN_BONE, amount));
                    c -= amount;
                    amount = Math.min(c, 64);
                }
                //noinspection deprecation
                player.updateInventory();
            }
            LocalDate date = LocalDate.now().with(Month.APRIL).withDayOfMonth(6);
            if (date.equals(LocalDate.now())) {
                ChatUtil.sendNotice(players, ChatColor.GOLD, "DROPS DOUBLED!");
                drops.addAll(drops.stream().map(ItemStack::clone).collect(Collectors.toList()));
            }
            // Reset respawn mechanics
            inst.bossDied();
            // Buff babies
            inst.buffBabies();

            return new InstructionResult<>(new DropInstruction() {
                @Override
                public List<ItemStack> getDrops(EntityDetail detail) {
                    return drops;
                }
            });
        });

        DamagedProcessor damagedProcessor = manager.getDamagedProcessor();
        damagedProcessor.addInstruction(condition -> {
            ShnugglesPrimeInstance inst = getInst(condition.getBoss().getDetail());
            LivingEntity boss = condition.getBoss().getEntity();
            EntityDamageEvent event = condition.getEvent();
            // Schedule a task to change the display name to show HP
            server().getScheduler().runTaskLater(inst(), () -> {
                if (!boss.isValid()) return;
                inst.printBossHealth();
            }, 1);
            Entity attacker = null;
            if (event instanceof EntityDamageByEntityEvent) {
                attacker = ((EntityDamageByEntityEvent) event).getDamager();
            }
            if (inst.damageHeals()) {
                double healedDamage = event.getDamage() * 2;
                double attemptedHP = inst.isActiveAttack(7) ? boss.getMaxHealth() : boss.getHealth() + healedDamage;
                if (attemptedHP > boss.getMaxHealth()) {
                    boss.setMaxHealth(attemptedHP);
                }
                EntityUtil.heal(boss, healedDamage);
                event.setCancelled(true);
                if (ChanceUtil.getChance(3) && acceptedReasons.contains(event.getCause())) {
                    int affected = 0;
                    for (Entity e : boss.getNearbyEntities(8, 8, 8)) {
                        if (e.isValid() && e instanceof Player && inst.contains(e)) {
                            callEvent(new ThrowPlayerEvent((Player) e));
                            e.setVelocity(new Vector(
                                    Math.random() * 3 - 1.5,
                                    Math.random() * 4,
                                    Math.random() * 3 - 1.5
                            ));
                            e.setFireTicks(ChanceUtil.getRandom(20 * 60));
                            affected++;
                        }
                    }
                    if (affected > 0) {
                        ChatUtil.sendNotice(inst.getContained(Player.class), "Feel my power!");
                    }
                }
            }
            if (ChanceUtil.getChance(3) && acceptedReasons.contains(event.getCause())) {
                inst.spawnMinions(attacker instanceof LivingEntity ? (LivingEntity) attacker : null);
            }
            if (attacker != null && attacker instanceof Player) {
                if (ItemUtil.hasForgeBook((Player) attacker)) {
                    boss.setHealth(0);
                    final Player finalAttacker = (Player) attacker;
                    if (!finalAttacker.getGameMode().equals(GameMode.CREATIVE)) {
                        server().getScheduler().runTaskLater(inst(), () -> (finalAttacker).setItemInHand(null), 1);
                    }
                }
            }
            return null;
        });
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public ShnugglesPrimeInstance load(World world, ProtectedRegion region) {
        return new ShnugglesPrimeInstance(this, world, region);
    }
}
