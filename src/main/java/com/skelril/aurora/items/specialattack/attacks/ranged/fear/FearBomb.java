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

package com.skelril.aurora.items.specialattack.attacks.ranged.fear;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;
import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.ranged.RangedSpecial;
import com.skelril.aurora.util.DamageUtil;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FearBomb extends EntityAttack implements RangedSpecial {

    public FearBomb(LivingEntity owner, LivingEntity target) {
        super(owner, target);
    }

    @Override
    public void activate() {

        final List<Block> blocks = new ArrayList<>();
        Block block = target.getLocation().getBlock();
        blocks.add(block);
        for (BlockFace blockFace : EnvironmentUtil.getNearbyBlockFaces()) {
            blocks.add(block.getRelative(blockFace));
        }

        List<Block> blockList = new ArrayList<>();
        for (BlockFace blockFace : EnvironmentUtil.getNearbyBlockFaces()) {
            for (Block aBlock : blocks) {
                Block testBlock = aBlock.getRelative(blockFace);
                if (!blocks.contains(testBlock) && !blockList.contains(testBlock)) blockList.add(testBlock);
            }
        }

        Collections.addAll(blocks, blockList.toArray(new Block[blockList.size()]));

        IntegratedRunnable bomb = new IntegratedRunnable() {

            @Override
            public boolean run(int times) {

                Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
                List<Player> players = null;

                for (Block block : blocks) {

                    if (players == null) {
                        players = block.getWorld().getPlayers();
                    }

                    loc = block.getLocation(loc);
                    World world = loc.getWorld();

                    while (loc.getY() > 0 && BlockType.canPassThrough(world.getBlockTypeIdAt(loc))) {
                        loc.add(0, -1, 0);
                    }

                    if (times % 2 == 0) {
                        for (Player player : players) {
                            if (!player.isValid()) continue;
                            player.sendBlockChange(loc, BlockID.CLOTH, (byte) ClothColor.WHITE.getID());
                        }
                    } else {
                        for (Player player : players) {
                            if (!player.isValid()) continue;
                            player.sendBlockChange(loc, BlockID.CLOTH, (byte) ClothColor.RED.getID());
                        }
                    }
                }
                return true;
            }

            @Override
            public void end() {

                Location loc = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
                List<Chunk> chunks = new ArrayList<>();

                if (owner instanceof Player) {
                    server.getPluginManager().callEvent(new RapidHitEvent((Player) owner));
                }

                for (Block block : blocks) {

                    loc = block.getLocation(loc);
                    World world = loc.getWorld();

                    while (loc.getY() > 0 && BlockType.canPassThrough(world.getBlockTypeIdAt(loc))) {
                        loc.add(0, -1, 0);
                    }

                    block.getWorld().createExplosion(loc, 0F);
                    for (Entity entity : block.getWorld().getEntitiesByClasses(Monster.class, Player.class)) {
                        if (!entity.isValid()) continue;
                        if (entity instanceof Player) {
                            if (owner instanceof Player && !PvPComponent.allowsPvP((Player) owner, (Player) entity)) {
                                continue;
                            }
                        }
                        if (entity.getLocation().distanceSquared(loc) <= 4) {
                            DamageUtil.damage(owner, (LivingEntity) entity, 10000);
                        }
                    }

                    Chunk chunk = block.getChunk();
                    int x = chunk.getX();
                    int z = chunk.getZ();

                    findChunk:
                    {
                        for (Chunk aChunk : chunks) {
                            if (aChunk.getX() == x && aChunk.getZ() == z) break findChunk;
                        }

                        chunks.add(chunk);
                    }
                }

                for (Chunk chunk : chunks) {
                    loc.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
                }
            }
        };

        TimedRunnable timedRunnable = new TimedRunnable(bomb, 6);

        BukkitTask task = server.getScheduler().runTaskTimer(inst, timedRunnable, 0, 20);
        timedRunnable.setTask(task);

        inform("Your bow creates a powerful bomb.");
    }
}
