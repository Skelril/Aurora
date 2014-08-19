/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.PatientX;

import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.WishingWellComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.combat.bosses.instruction.DropInstruction;
import com.skelril.aurora.combat.bosses.instruction.PersistenceInstruction;
import com.skelril.aurora.combat.bosses.instruction.SHBindInstruction;
import com.skelril.aurora.economic.dropparty.DropPartyTask;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.shard.FlagProfile;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.checker.RegionChecker;
import com.skelril.aurora.util.player.AdminToolkit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.sk89q.commandbook.util.entity.EntityUtil.sendProjectilesFromEntity;
import static com.skelril.aurora.shard.instance.PatientX.PatientXInstance.getInst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PatientXShard extends Shard<PatientXInstance> {

    private PatientXConfig config;

    private AdminComponent admin;

    private AdminToolkit admintlkt;
    private FlagProfile flagProfile = new FlagProfile();
    private BossManager manager = new BossManager();

    public PatientXShard(ShardEditor editor, PatientXConfig config, AdminComponent admin) {
        super(ShardType.PATIENT_X, editor);
        this.config = config;
        this.admin = admin;
        this.admintlkt = new AdminToolkit(admin);
        setUpManager();
    }

    public PatientXConfig getConfig() {
        return config;
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public AdminToolkit getToolKit() {
        return admintlkt;
    }

    public BossManager getBossManager() {
        return manager;
    }

    private static Set<DamageCause> blockedDamage = new HashSet<>();

    static {
        blockedDamage.add(DamageCause.BLOCK_EXPLOSION);
        blockedDamage.add(DamageCause.ENTITY_EXPLOSION);
    }

    public Set<DamageCause> getBlockedDamage() {
        return Collections.unmodifiableSet(blockedDamage);
    }

    private void setUpManager() {

        flagProfile.setFlag(DefaultFlag.ICE_MELT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);

        registerEvents(new BossListener(manager));

        BindProcessor bindProcessor = manager.getBindProcessor();
        bindProcessor.addInstruction(new SHBindInstruction("Patient X", config.bossHealth));
        bindProcessor.addInstruction(new PersistenceInstruction());
        bindProcessor.addInstruction(condition -> {
            PatientXInstance inst = getInst(condition.getBoss().getDetail());
            ChatUtil.sendWarning(inst.getContained(Player.class), "Ice to meet you again!");
            return null;
        });

        UnbindProcessor unbindProcessor = manager.getUnbindProcessor();
        unbindProcessor.addInstruction(condition -> {
            PatientXInstance inst = getInst(condition.getBoss().getDetail());

            Collection<Player> spectator = inst.getContained(Player.class);
            Collection<Player> contained = admintlkt.removeAdmin(spectator);
            ChatUtil.sendWarning(spectator, "So you think you've won? Ha!");
            ChatUtil.sendWarning(spectator, "I'll get you next time...");

            List<ItemStack> drops = new ArrayList<>();
            int playerCount = spectator.isEmpty() ? 1 : contained.size();
            int dropVal = config.playerVal * playerCount;
            drops.addAll(WishingWellComponent.getCalculatedLoot(Bukkit.getConsoleSender(), -1, dropVal));

            switch (ChanceUtil.getRandom(4)) {
                case 1:
                    if (ChanceUtil.getChance(8)) {
                        drops.add(CustomItemCenter.build(CustomItems.NECROS_HELMET));
                        break;
                    }
                    drops.add(CustomItemCenter.build(CustomItems.NECTRIC_HELMET));
                    break;
                case 2:
                    if (ChanceUtil.getChance(8)) {
                        drops.add(CustomItemCenter.build(CustomItems.NECROS_CHESTPLATE));
                        break;
                    }
                    drops.add(CustomItemCenter.build(CustomItems.NECTRIC_CHESTPLATE));
                    break;
                case 3:
                    if (ChanceUtil.getChance(8)) {
                        drops.add(CustomItemCenter.build(CustomItems.NECROS_LEGGINGS));
                        break;
                    }
                    drops.add(CustomItemCenter.build(CustomItems.NECTRIC_LEGGINGS));
                    break;
                case 4:
                    if (ChanceUtil.getChance(8)) {
                        drops.add(CustomItemCenter.build(CustomItems.NECROS_BOOTS));
                        break;
                    }
                    drops.add(CustomItemCenter.build(CustomItems.NECTRIC_BOOTS));
                    break;
            }

            if (ChanceUtil.getChance(100)) {
                drops.add(CustomItemCenter.build(CustomItems.HYMN_OF_SUMMATION));
            }

            for (int i = 0; i < 8 * playerCount; ++i) {
                drops.add(CustomItemCenter.build(CustomItems.SCROLL_OF_SUMMATION, ChanceUtil.getRandom(16)));
            }

            LocalDate date = LocalDate.now().with(Month.APRIL).withDayOfMonth(6);
            if (date.equals(LocalDate.now())) {
                ChatUtil.sendNotice(inst.getContained(Player.class), ChatColor.GOLD, "DROPS DOUBLED!");
                drops.addAll(drops.stream().map(ItemStack::clone).collect(Collectors.toList()));
            }

            Location target = inst.getCenter();
            for (Player player : contained) {
                player.teleport(target);
                Vector v = new Vector(
                        ChanceUtil.getChance(2) ? 1 : -1,
                        0,
                        ChanceUtil.getChance(2) ? 1 : -1
                );
                if (ChanceUtil.getChance(2)) {
                    v.setX(0);
                } else {
                    v.setZ(0);
                }
                player.setVelocity(v);
            }
            CuboidRegion rg = inst.getDropRegion();
            DropPartyTask task = new DropPartyTask(inst.getBukkitWorld(), rg, drops, new RegionChecker(rg) {
                @Override
                public Boolean evaluate(com.sk89q.worldedit.Vector v) {
                    Location l = new Location(inst.getBukkitWorld(), v.getX(), v.getY(), v.getZ());
                    return super.evaluate(v) && !l.getBlock().getType().isSolid();
                }
            });
            task.setXPChance(5);
            task.setXPSize(10);
            task.start(inst(), server().getScheduler(), 20 * 5, 20 * 3);
            inst.freezeBlocks(100, false);
            // Reset respawn mechanics
            inst.bossDied();

            return new InstructionResult<>(new DropInstruction() {
                @Override
                public List<ItemStack> getDrops(EntityDetail detail) {
                    return drops;
                }
            });
        });

        DamageProcessor damageProcessor = manager.getDamageProcessor();
        damageProcessor.addInstruction(condition -> {
            PatientXInstance inst = getInst(condition.getBoss().getDetail());
            Entity attacked = condition.getAttacked();
            EntityDamageByEntityEvent event = condition.getEvent();

            if (attacked instanceof Player) {
                Player player = (Player) attacked;
                if (inst().hasPermission(player, "aurora.prayer.intervention") && ChanceUtil.getChance(inst.getDifficulty())) {
                    ChatUtil.sendNotice(player, "A divine force protects you.");
                    return null;
                }
            }
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                if (event.isApplicable(modifier)) {
                    event.setDamage(modifier, 0);
                }
            }
            event.setDamage(EntityDamageEvent.DamageModifier.BASE, inst.getDifficulty() * inst.getMaster().getConfig().baseBossHit);
            return null;
        });


        DamagedProcessor damagedProcessor = manager.getDamagedProcessor();
        damagedProcessor.addInstruction(condition -> {
            EntityDamageEvent event = condition.getEvent();
            if (blockedDamage.contains(event.getCause())) {
                event.setCancelled(true);
                return null;
            }
            return new InstructionResult<>(cond -> {
                PatientXInstance inst = getInst(cond.getBoss().getDetail());
                Entity attacker = null;
                if (event instanceof EntityDamageByEntityEvent) {
                    attacker = ((EntityDamageByEntityEvent) event).getDamager();
                }

                if (attacker instanceof Player) {
                    ItemStack held = ((Player) attacker).getItemInHand();
                    if (held != null && held.getTypeId() == ItemID.BLAZE_ROD) {
                        inst.modifyDifficulty(2);
                    }
                } else if (attacker instanceof Projectile) {
                    sendProjectilesFromEntity(inst.getBoss(), 12, .5F, Snowball.class);
                }
                inst.modifyDifficulty(.5);
                inst.teleportRandom(true);
                return null;
            });
        });
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public PatientXInstance load(World world, ProtectedRegion region) {
        return new PatientXInstance(this, world, region);
    }
}
