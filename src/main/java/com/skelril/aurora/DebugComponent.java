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

package com.skelril.aurora;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@ComponentInformation(friendlyName = "ADebug", desc = "Debug tools")
public class DebugComponent extends BukkitComponent {

    private final CommandBook inst = CommandBook.inst();
    private final Server server = CommandBook.server();

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new InventoryCorruptionFixer());
        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new BlockDebug());

        //registerCommands(FoodInfo.class);
        //registerCommands(ChunkLighter.class);
        //registerCommands(LocationDebug.class);
        //registerCommands(RegionVolumeTest.class);

        // Bug fixes

        // Fixes an issue where potion effects are not removed from players on death
        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new PotionDeathFix());
        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new ItemSpawnPrinter());
        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new DamageSys());
    }

    private class InventoryCorruptionFixer implements Listener {

        @EventHandler
        public void onLogin(PlayerJoinEvent event) {

            Player player = event.getPlayer();

            if (!player.getName().equals("Dark_Arc")) return;
            ItemStack[] inventory = player.getInventory().getContents();
            inventory = ItemUtil.removeItemOfType(inventory, Material.DOUBLE_PLANT.getId());
            player.getInventory().setContents(inventory);
        }
    }

    public class FoodInfo {

        @Command(aliases = {"foodstats"}, desc = "Report hunger info",
                flags = "", min = 0, max = 0)
        @CommandPermissions("aurora.debug.foodstats")
        public void myLocCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);
            ChatUtil.send(player, "Food level: " + player.getFoodLevel());
            ChatUtil.send(player, "Sat. level: " + player.getSaturation());
            ChatUtil.send(player, "Exh. level: " + player.getExhaustion());
        }
    }

    public class LocationDebug {

        @Command(aliases = {"myloc"}, desc = "Get your location",
                flags = "", min = 0, max = 0)
        public void myLocCmd(CommandContext args, CommandSender sender) throws CommandException {

            Location l = PlayerUtil.checkPlayer(sender).getLocation();
            ChatUtil.send(sender, "X: " + l.getX() + ", Y:" + l.getY() + ", Z: " + l.getZ());
            ChatUtil.send(sender, "Pitch: " + l.getPitch() + ", Yaw: " + l.getYaw());
        }
    }

    public class RegionVolumeTest {

        @Command(aliases = {"rgvol"}, desc = "Calculate the volume of the currently selected region",
                flags = "", min = 0, max = 0)
        public void myLocCmd(CommandContext args, CommandSender sender) throws CommandException {

            WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            Selection selection = worldEdit.getSelection(PlayerUtil.checkPlayer(sender));

            if (selection != null) {
                if (selection instanceof Polygonal2DSelection) {
                    ProtectedPolygonalRegion region = new ProtectedPolygonalRegion(
                            "test",
                            ((Polygonal2DSelection) selection).getNativePoints(),
                            selection.getMinimumPoint().getBlockY(),
                            selection.getMaximumPoint().getBlockY()
                    );
                    ChatUtil.send(sender, "Region Volume: " + region.volume());
                }
                // Do something with min/max
            } else {
                throw new CommandException("No selection found!");
            }
        }
    }

    private class BlockDebug implements Listener {

        @EventHandler
        public void onRightClick(PlayerInteractEvent event) {

            ItemStack held = event.getItem();
            if (held != null && held.getType() == Material.COAL && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Block block = event.getClickedBlock();
                ChatUtil.send(event.getPlayer(),
                        "Block name: " + block.getType()
                                + ", Type ID: " + block.getTypeId()
                                + ", Block data: " + block.getData()
                );
                event.setUseInteractedBlock(Event.Result.DENY);
            }
        }
    }

    private class PotionDeathFix implements Listener {

        @EventHandler
        public void onRespawn(final PlayerRespawnEvent event) {

            server.getScheduler().runTaskLater(inst, () -> {
                Player player = event.getPlayer();

                for (PotionEffect next : player.getActivePotionEffects()) {
                    player.addPotionEffect(new PotionEffect(next.getType(), 0, 0), true);
                }
            }, 1);
        }
    }

    private class ItemSpawnPrinter implements Listener {

        @EventHandler
        public void onItemSpawn(final ItemSpawnEvent event) {
            Location l = event.getEntity().getLocation();
            ChatUtil.sendDebug("X: " + l.getX() + ", Y:" + l.getY() + ", Z: " + l.getZ());
        }
    }

    private class DamageSys implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

            double damage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
            double origDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE);

            if (damage == origDamage) return;

            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values()) {
                if (modifier == EntityDamageEvent.DamageModifier.BASE) continue;
                if (!event.isApplicable(modifier)) continue;

                event.setDamage(modifier, event.getDamage(modifier) * (damage / origDamage));
            }
        }
    }
}
