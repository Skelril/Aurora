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

package com.skelril.aurora.shard.instance.Catacombs;

import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.OpenBoss.DistributedBossListener;
import com.skelril.OpenBoss.DistributedBossManager;
import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.instruction.processor.BindProcessor;
import com.skelril.OpenBoss.instruction.processor.DamageProcessor;
import com.skelril.OpenBoss.instruction.processor.DamagedProcessor;
import com.skelril.OpenBoss.instruction.processor.UnbindProcessor;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.combat.bosses.instruction.DynamicHPInstruction;
import com.skelril.aurora.combat.bosses.instruction.HealthPrint;
import com.skelril.aurora.combat.bosses.instruction.PersistenceInstruction;
import com.skelril.aurora.shard.FlagProfile;
import com.skelril.aurora.shard.Shard;
import com.skelril.aurora.shard.ShardEditor;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.shard.instance.Catacombs.instruction.CatacombsDrop;
import com.skelril.aurora.shard.instance.Catacombs.instruction.CheckedSpawnWave;
import com.skelril.aurora.shard.instance.Catacombs.instruction.WaveDamageModifier;
import com.skelril.aurora.util.player.AdminToolkit;

import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;

public class CatacombsShard extends Shard<CatacombsInstance> {

    private AdminComponent admin;

    private AdminToolkit admintlkt;
    private FlagProfile flagProfile = new FlagProfile();
    private DistributedBossManager bossManager = new DistributedBossManager();
    private DistributedBossManager waveMobManager = new DistributedBossManager();

    public CatacombsShard(ShardEditor editor, AdminComponent admin) {
        super(ShardType.CATACOMBS, editor);
        this.admin = admin;
        this.admintlkt = new AdminToolkit(admin);
        setUp();
    }

    public AdminComponent getAdmin() {
        return admin;
    }

    public AdminToolkit getToolKit() {
        return admintlkt;
    }

    public DistributedBossManager getBossManager() {
        return bossManager;
    }

    public DistributedBossManager getWaveMobManager() {
        return waveMobManager;
    }

    private void setUp() {

        flagProfile.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        flagProfile.setFlag(DefaultFlag.OTHER_EXPLOSION, StateFlag.State.DENY);

        setUpBoss();
        setUpWave();
    }

    private void setUpBoss() {
        registerEvents(new DistributedBossListener(bossManager));

        BindProcessor bindProcessor = bossManager.getBindProcessor();
        bindProcessor.addInstruction(new DynamicHPInstruction("Necromancer") {
            @Override
            public double getHealth(EntityDetail detail) {
                return getFrom(detail).getWave() * 250;
            }
        });
        bindProcessor.addInstruction(new PersistenceInstruction());

        UnbindProcessor unbindProcessor = bossManager.getUnbindProcessor();
        unbindProcessor.addInstruction(new CheckedSpawnWave());
        unbindProcessor.addInstruction(new CatacombsDrop());

        DamageProcessor damageProcessor = bossManager.getDamageProcessor();
        damageProcessor.addInstruction(new WaveDamageModifier());

        DamagedProcessor damagedProcessor = bossManager.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }

    private void setUpWave() {

        registerEvents(new DistributedBossListener(waveMobManager));

        BindProcessor bindProcessor = waveMobManager.getBindProcessor();
        bindProcessor.addInstruction(new PersistenceInstruction());

        UnbindProcessor unbindProcessor = waveMobManager.getUnbindProcessor();
        unbindProcessor.addInstruction(new CheckedSpawnWave());
        unbindProcessor.addInstruction(new CatacombsDrop(10));

        DamageProcessor damageProcessor = waveMobManager.getDamageProcessor();
        damageProcessor.addInstruction(new WaveDamageModifier());

        DamagedProcessor damagedProcessor = waveMobManager.getDamagedProcessor();
        damagedProcessor.addInstruction(new HealthPrint());
    }

    @Override
    public FlagProfile getFlagProfile() {
        return flagProfile;
    }

    @Override
    public CatacombsInstance load(World world, ProtectedRegion region) {
        return new CatacombsInstance(this, world, region);
    }
}

