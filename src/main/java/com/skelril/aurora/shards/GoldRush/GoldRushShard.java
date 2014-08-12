/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.GoldRush;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardManagerComponent;
import com.skelril.aurora.shard.ShardType;
import net.milkbowl.vault.economy.Economy;

public class GoldRushShard extends Shard<GoldRushInstance> {

    private AdminComponent admin;
    private ShardManagerComponent manager;

    private Economy economy;

    public GoldRushShard(ShardEditor editor, AdminComponent admin, ShardManagerComponent manager, Economy economy) {
        super(ShardType.GOLD_RUSH, editor);
        this.admin = admin;
        this.manager = manager;
        this.economy = economy;
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
    public GoldRushInstance load(World world, ProtectedRegion region) {
        return new GoldRushInstance(this, world, region);
    }
}
