/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shards.PatientX;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.BossManager;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.util.player.AdminToolkit;

public class PatientXShard extends Shard<PatientXInstance> {

    private PatientXConfig config;

    private AdminComponent admin;

    private AdminToolkit admintlkt;
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

    private void setUpManager() {

    }

    @Override
    public PatientXInstance load(World world, ProtectedRegion region) {
        return new PatientXInstance(this, world, region);
    }
}
