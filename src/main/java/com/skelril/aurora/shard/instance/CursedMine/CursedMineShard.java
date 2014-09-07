/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
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
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);
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
