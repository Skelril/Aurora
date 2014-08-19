/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard.instance.CursedMine;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.prayer.PrayerComponent;
import com.skelril.aurora.shard.FlagProfile;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.shard.instance.CursedMine.hitlist.HitList;
import com.skelril.aurora.util.restoration.RestorationUtil;

public class CursedMineShard extends Shard<CursedMineInstance> {

    private AdminComponent admin;
    private PrayerComponent prayer;
    private RestorationUtil restorationUtil;

    private HitList hitList = new HitList();

    private FlagProfile flagProfile = new FlagProfile();

    public CursedMineShard(ShardEditor editor, AdminComponent admin,
                           PrayerComponent prayer, RestorationUtil restorationUtil) {
        super(ShardType.CURSED_MINE, editor);
        this.admin = admin;
        this.prayer = prayer;
        this.restorationUtil = restorationUtil;

        setUp();
    }

    private void setUp() {
        flagProfile.setFlag(DefaultFlag.PASSTHROUGH, StateFlag.State.ALLOW);
        flagProfile.setFlag(DefaultFlag.FIRE_SPREAD, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.LAVA_FIRE, StateFlag.State.DENY);
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public PrayerComponent getPrayer() {
        return prayer;
    }

    public RestorationUtil getRestoreUtil() {
        return restorationUtil;
    }

    public HitList getHitList() {
        return hitList;
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public CursedMineInstance load(World world, ProtectedRegion region) {
        return new CursedMineInstance(this, world, region);
    }
}
