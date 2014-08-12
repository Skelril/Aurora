/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.FreakyFour;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.BossListener;
import com.skelril.OpenBoss.BossManager;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.shards.FreakyFour.boss.CharlotteBossManager;
import com.skelril.aurora.shards.FreakyFour.boss.DaBombBossManager;
import com.skelril.aurora.shards.FreakyFour.boss.FrimusBossManager;
import com.skelril.aurora.shards.FreakyFour.boss.SnipeeBossManager;
import net.milkbowl.vault.economy.Economy;

import java.util.EnumMap;

import static com.sk89q.commandbook.CommandBook.registerEvents;

public class FreakyFourShard extends Shard<FreakyFourInstance> {

    private FreakyFourConfig config;
    private EnumMap<FreakyFourBoss, BossManager> bossManagers = new EnumMap<>(FreakyFourBoss.class);

    private AdminComponent admin;
    private ShardManagerComponent manager;

    private Economy economy;

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
    public FreakyFourInstance load(World world, ProtectedRegion region) {
        return new FreakyFourInstance(this, world, region);
    }
}
