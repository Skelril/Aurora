/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.FreakyFour;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.shard.*;
import com.skelril.aurora.shard.instance.FreakyFour.boss.CharlotteBossManager;
import com.skelril.aurora.shard.instance.FreakyFour.boss.DaBombBossManager;
import com.skelril.aurora.shard.instance.FreakyFour.boss.FrimusBossManager;
import com.skelril.aurora.shard.instance.FreakyFour.boss.SnipeeBossManager;
import net.milkbowl.vault.economy.Economy;

import java.util.EnumMap;

import static com.sk89q.commandbook.CommandBook.registerEvents;

public class FreakyFourShard extends Shard<FreakyFourInstance> {

    private FreakyFourConfig config;
    private EnumMap<FreakyFourBoss, BossManager> bossManagers = new EnumMap<>(FreakyFourBoss.class);

    private AdminComponent admin;
    private ShardManagerComponent manager;

    private Economy economy;

    private FlagProfile flagProfile = new FlagProfile();

    public FreakyFourShard(ShardEditor editor, FreakyFourConfig config,
                           AdminComponent admin, ShardManagerComponent manager,
                           Economy economy) {
        super(ShardType.FREAKY_FOUR, editor);
        this.config = config;
        this.admin = admin;
        this.manager = manager;
        this.economy = economy;

        setUp();
    }

    private void setUp() {
        flagProfile.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);

        bossManagers.put(FreakyFourBoss.CHARLOTTE, new CharlotteBossManager(config));
        bossManagers.put(FreakyFourBoss.FRIMUS, new FrimusBossManager(config));
        bossManagers.put(FreakyFourBoss.DA_BOMB, new DaBombBossManager(config));
        bossManagers.put(FreakyFourBoss.SNIPEE, new SnipeeBossManager(economy, config));
        for (BossManager manager : bossManagers.values()) {
            registerEvents(new BossListener(manager));
        }
    }

    public BossManager getManager(FreakyFourBoss boss) {
        return bossManagers.get(boss);
    }

    public FreakyFourConfig getConfig() {
        return config;
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public ShardManagerComponent getManager() {
        return manager;
    }

    public Economy getEconomy() {
        return economy;
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public FreakyFourInstance load(World world, ProtectedRegion region) {
        return new FreakyFourInstance(this, world, region);
    }
}
