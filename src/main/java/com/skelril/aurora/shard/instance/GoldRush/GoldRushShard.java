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
