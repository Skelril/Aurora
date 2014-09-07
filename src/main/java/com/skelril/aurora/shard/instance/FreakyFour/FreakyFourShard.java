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
