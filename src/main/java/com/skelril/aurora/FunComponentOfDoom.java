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
import com.sk89q.commandbook.util.InputUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.LocationUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ComponentInformation(friendlyName = "Fun of Doom", desc = "Fun of Doom")
@Depend(components = {WishingWellComponent.class})
public class FunComponentOfDoom extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    private Set<String> players = new HashSet<>();

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
        //noinspection AccessStaticViaInstance
        //inst.registerEvents(new ChristmasGhast());

        registerCommands(Commands.class);
    }

    private class ChristmasGhast implements Listener {

        private Random r = new Random(System.currentTimeMillis());

        @EventHandler(ignoreCancelled = true)
        public void onEntityExplode(EntityExplodeEvent event) {

            Entity e = event.getEntity();
            if (e != null && e.getType().equals(EntityType.FIREBALL)) {
                for (ItemStack aDrop : WishingWellComponent.getCalculatedLoot(server.getConsoleSender(), 16, 1500)) {
                    Item item = e.getWorld().dropItem(event.getLocation(), aDrop);
                    item.setVelocity(new org.bukkit.util.Vector(
                            r.nextDouble() * 2 - 1,
                            r.nextDouble() * 1,
                            r.nextDouble() * 2 - 1
                    ));
                }
                event.blockList().clear();
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onEntityDamageEvent(EntityDamageByEntityEvent event) {

            Entity a = event.getEntity();
            Entity b = event.getDamager();

            if (a instanceof Player && b instanceof LargeFireball) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        if (player.getWorld().getName().contains("City")) {
            String name = player.getName();
            if (name.equals("darkkrai13")) {
                event.getDrops().add(new ItemStack(ItemID.RAW_BEEF));
            }
        }
    }

    private enum AColor {

        AQUA(0x00, 0xFF, 0xFF),
        BLACK(0x00, 0x00, 0x00),
        BLUE(0x00, 0x00, 0xFF),
        BRONZE(0xCD, 0x7F, 0x32),
        BROWN(0x96, 0x4B, 0x00),
        FUSCHSIA(0xFF, 0x00, 0xFF),
        GRAY(0x80, 0x80, 0x80),
        GREEN(0x00, 0x80, 0x00),
        LIME(0x00, 0xFF, 0x00),
        MAROON(0x80, 0x00, 0x00),
        NAVY(0x00, 0x00, 0x80),
        OLIVE(0x80, 0x80, 0x00),
        ORANGE(0xFF, 0xA5, 0x00),
        PURPLE(0x80, 0x00, 0x80),
        RED(0xFF, 0x00, 0x00),
        SILVER(0xC0, 0xC0, 0xC0),
        TEAL(0x00, 0x80, 0x80),
        WHITE(0xFF, 0xFF, 0xFF),
        YELLOW(0xFF, 0xFF, 0x00);

        private final int red;
        private final int green;
        private final int blue;

        AColor(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    public class Commands {

        @Command(aliases = {"dispatch"}, desc = "Run a command",
                usage = "[-t times] <command>",
                flags = "t:", min = 1)
        @CommandPermissions({"aurora.commmandofdoom"})
        public void dispatchCmd(final CommandContext args, final CommandSender sender) throws CommandException {

            if (sender instanceof Player) {
                int times = 1;
                if (args.hasFlag('t')) {
                    times = args.getFlagInteger('t');
                }
                for (int i = 0; i < times; i++) {
                    server.getScheduler().runTaskLater(inst,
                            () -> ((Player) sender).performCommand(args.getJoinedStrings(0)), 1);
                }
            }
        }

        @Command(aliases = {"firework", "firewrk", "fwrk"}, desc = "Launches fireworks into the sky",
                usage = "[color[,...]] [fade color[,...]] [type] [power] [amount] [player]",
                flags = "r", min = 0, max = 6)
        @CommandPermissions({"aurora.fireworks"})
        public void fireworkCmd(CommandContext args, CommandSender sender) throws CommandException {

            final String name = sender.getName();
            if (!inst.hasPermission(sender, "aurora.fireworks.unlimited")) {
                if (players.contains(name)) {
                    throw new CommandException("This command is still on cool down.");
                }
            }

            final List<Color> colors;
            if (args.argsLength() > 0) {
                String activeString = "???";
                try {
                    colors = new ArrayList<>();
                    String[] colorStrings = args.getString(0).toUpperCase().split(",");

                    for (String aColorString : colorStrings) {
                        activeString = aColorString;
                        AColor aColor = AColor.valueOf(aColorString);
                        colors.add(Color.fromRGB(aColor.red, aColor.green, aColor.blue));
                    }
                } catch (Exception ex) {
                    throw new CommandException("No color found by the name of: " + activeString + "!");
                }
            } else {
                colors = Arrays.asList(Color.PURPLE);
            }

            final List<Color> fades;
            if (args.argsLength() > 1) {
                String activeString = "???";
                try {
                    fades = new ArrayList<>();
                    String[] colorStrings = args.getString(1).toUpperCase().split(",");

                    for (String aColorString : colorStrings) {
                        activeString = aColorString;
                        AColor aColor = AColor.valueOf(aColorString);
                        fades.add(Color.fromRGB(aColor.red, aColor.green, aColor.blue));
                    }
                } catch (Exception ex) {
                    throw new CommandException("No color found by the name of: " + activeString + "!");
                }
            } else {
                fades = Arrays.asList(Color.BLACK);
            }

            final FireworkEffect.Type type;
            if (args.argsLength() > 2) {
                try {
                    type = FireworkEffect.Type.valueOf(args.getString(2).toUpperCase());
                } catch (Exception ex) {
                    throw new CommandException("No type found by the name of: " + args.getString(2).toUpperCase() + "!");
                }
            } else {
                type = FireworkEffect.Type.BURST;
            }

            final int rocketPower;
            if (args.argsLength() > 3) {
                rocketPower = Math.max(0, Math.min(5, args.getInteger(3)));
            } else {
                rocketPower = 5;
            }

            final int amount;
            if (args.argsLength() > 4) {
                int cap = inst.hasPermission(sender, "aurora.fireworks.big") ? 200 : 50;
                cap = inst.hasPermission(sender, "aurora.fireworks.mega") ? 1000 : cap;
                amount = Math.max(0, Math.min(cap, args.getInteger(4)));
            } else {
                amount = 12;
            }

            final List<Location> playerLocList;
            if (args.argsLength() > 5) {

                inst.checkPermission(sender, "aurora.fireworks.other");

                List<Player> players = InputUtil.PlayerParser.matchPlayers(sender, args.getString(5));
                playerLocList = new ArrayList<>();
                playerLocList.addAll(players.stream().map(player -> player.getLocation()).collect(Collectors.toList()));
            } else if (sender instanceof Player) {
                playerLocList = Arrays.asList(((Player) sender).getLocation());
            } else {
                throw new CommandException("You must be a player or specify a player to use this command.");
            }

            final boolean random = args.hasFlag('r');

            for (final Location playerLoc : playerLocList) {
                for (int i = 0; i < amount; i++) {
                    final int finalI = i;
                    server.getScheduler().runTaskLater(inst, () -> {
                        Location targetLocation = playerLoc.clone();
                        if (random) {
                            targetLocation = LocationUtil.findRandomLoc(playerLoc, 7, true, false);
                        }
                        Firework firework = playerLoc.getWorld().spawn(targetLocation, Firework.class);
                        FireworkMeta meta = firework.getFireworkMeta();
                        FireworkEffect.Builder builder = FireworkEffect.builder();
                        builder.flicker(ChanceUtil.getChance(2));
                        builder.trail(ChanceUtil.getChance(2));
                        builder.withColor(colors);
                        builder.withFade(fades);
                        builder.with(type);
                        meta.addEffect(builder.build());
                        meta.setPower(ChanceUtil.getRangedRandom(rocketPower / 2, rocketPower));
                        firework.setFireworkMeta(meta);

                        if (finalI == amount - 1 && players.contains(name)) {
                            players.remove(name);
                        }
                    }, i * 4);
                }
            }
            players.add(name);
            ChatUtil.send(sender, "Firework(s) launching!");
        }
    }
}