/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.GoldRush;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.shard.*;
import net.milkbowl.vault.economy.Economy;

public class GoldRushShard extends Shard<GoldRushInstance> {

    private AdminComponent admin;
    private ShardManagerComponent manager;

    private Economy economy;

    private FlagProfile flagProfile = new FlagProfile();

    public GoldRushShard(ShardEditor editor, AdminComponent admin, ShardManagerComponent manager, Economy economy) {
        super(ShardType.GOLD_RUSH, editor);
        this.admin = admin;
        this.manager = manager;
        this.economy = economy;

        setUp();
    }

    private void setUp() {
        flagProfile.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);

        flagProfile.setFlag(DefaultFlag.CHEST_ACCESS, StateFlag.State.ALLOW);
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public ShardManagerComponent getManager() {
        return manager;
    }

    public Economy getEcon() {
        return economy;
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public GoldRushInstance load(World world, ProtectedRegion region) {
        return new GoldRushInstance(this, world, region);
    }
}
