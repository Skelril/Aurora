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

package com.skelril.aurora.homes;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.util.InputUtil;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.databases.RegionDBUtil;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.economic.store.AdminStoreComponent;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.RegionUtil;
import com.skelril.aurora.util.item.BookUtil;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

//@ComponentInformation(friendlyName = "Home Manager", desc = "Home ECS")
//@Depend(plugins = {"WorldEdit", "WorldGuard"}, components = AdminStoreComponent.class)
public class HomeManagerComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private WorldEditPlugin WE;
    private WorldGuardPlugin WG;
    private Economy econ;

    private static Map<BaseBlock, BaseBlock> blockMapping = new HashMap<>();

    static {
        blockMapping.put(new BaseBlock(BlockID.GRASS), new BaseBlock(BlockID.STAINED_CLAY, 11));
        blockMapping.put(new BaseBlock(BlockID.SAND), new BaseBlock(BlockID.STAINED_CLAY, 3));
    }

    private static PlotOutliner outliner = new PlotOutliner(blockMapping, BlockType::isNaturalTerrainBlock);

    @Override
    public void enable() {

        registerCommands(Commands.class);
        setUpWorldEdit();
        setUpWorldGuard();
        setUpEconomy();

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    private void setUpWorldEdit() {

        Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");

        // WorldEdit may not be loaded
        if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
            return;
        }

        this.WE = (WorldEditPlugin) plugin;
    }

    private void setUpWorldGuard() {

        Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return;
        }

        this.WG = (WorldGuardPlugin) plugin;
    }

    private void setUpEconomy() {

        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void playerPreProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (event.getMessage().contains("%cur-house%")) {
            RegionManager manager = WG.getRegionManager(player.getWorld());

            for (ProtectedRegion region : manager.getApplicableRegions(player.getLocation())) {
                if (region.getId().endsWith("-s") && region.getFlag(DefaultFlag.PRICE) != null) {
                    event.setMessage(event.getMessage().replace("%cur-house%", region.getId()));
                    ChatUtil.send(player, "Injected region: " + region.getId() + " as current house.");
                    return;
                }
            }
        }
    }

    public class Commands {

        @Command(aliases = {"home"}, desc = "Home Manager")
        @NestedCommand({NestedCommands.class})
        public void homeCmd(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class NestedCommands {

        @Command(aliases = {"info"}, usage = "", desc = "View info about your home",
                min = 0, max = 0)
        @CommandPermissions({"aurora.home.self.info"})
        public void infoHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);

            RegionManager manager = WG.getRegionManager(player.getWorld());
            List<ProtectedRegion> houses = RegionUtil.getHouses(new BukkitPlayer(WG, player), manager);

            StringBuilder builtInfo = new StringBuilder();
            builtInfo.append(ChatColor.YELLOW).append("You own ").append(houses.size()).append(" houses.");
            builtInfo.append("\n").append("Total Chunks: ").append(RegionUtil.sumChunks(houses.stream()));

            /*

            builtInfo.append(ChatColor.YELLOW + "Home Owner: ").append(sender.getName()).append(", ");
            builtInfo.append(ChatColor.GRAY + "District: ").append(district.toProperName());
            builtInfo.append("\n");
            builtInfo.append(ChatColor.AQUA + "Server Managers: ").append(District.GLOBAL.getManagersFriendly());
            builtInfo.append("\n");
            builtInfo.append(ChatColor.AQUA + "District Managers: ").append(district.getManagersFriendly());


            if (region.getMembers().size() > 0) {
                builtInfo.append("\n");
                builtInfo.append(ChatColor.BLUE + "Friends: ").append(region.getMembers().toUserFriendlyString());
            }

            boolean hasFlags = false;
            final StringBuilder s = new StringBuilder("\n" + ChatColor.BLUE + "Properties: ");
            for (Flag<?> flag : DefaultFlag.getFlags()) {
                Object val = region.getFlag(flag), group = null;

                if (val == null) {
                    continue;
                }

                if (hasFlags) {
                    s.append(", ");
                }

                RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
                if (groupFlag != null) {
                    group = region.getFlag(groupFlag);
                }

                if (group == null) {
                    s.append(flag.getName()).append(": ").append(String.valueOf(val));
                } else {
                    s.append(flag.getName()).append(" -g ").append(String.valueOf(group)).append(": ")
                            .append(String.valueOf(val));
                }

                hasFlags = true;
            }
            if (hasFlags) {
                builtInfo.append(s.toString());
            }

            */

            ChatUtil.send(sender, ChatColor.RESET, builtInfo.toString());
        }

        @Command(aliases = {"pcchunks"}, usage = "<# of chunks>", desc = "Get the cost of buying x number of chunks",
                min = 1, max = 1)
        @CommandPermissions({"aurora.home.self.info"})
        public void buyChunk(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);

            RegionManager manager = WG.getRegionManager(player.getWorld());

            int acquisitionSize = args.getInteger(0);
            int chunks = RegionUtil.sumChunks(new BukkitPlayer(WG, player), manager);
            int newChunks = chunks + acquisitionSize;

            double price = RegionUtil.calcChunkPrice(newChunks) - RegionUtil.calcChunkPrice(chunks);

            String priceString = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(price), "");

            ChatUtil.send(sender, acquisitionSize + " additional chunks will cost: " + priceString);
        }

        @Command(aliases = {"addplayer"}, usage = "<player>", desc = "Add a player to your home",
                min = 1, max = 1)
        @CommandPermissions({"aurora.home.self.add"})
        public void addMemberToHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);
            RegionManager manager = WG.getRegionManager(player.getWorld());
            ProtectedRegion region = getStoodInHome(player, manager);

            if (region == null) {
                throw new CommandException("You are not currently standing in a house you own.");
            }
            RegionDBUtil.addToDomain(region.getMembers(), args.getPaddedSlice(1, 0), 0);
            try {
                manager.save();
            } catch (ProtectionDatabaseException e) {
                ChatUtil.sendError(sender, "Failed to add player to your home.");
            }
            ChatUtil.send(player, "Home successfully updated!");
        }

        @Command(aliases = {"removeplayer"}, usage = "<player>", desc = "Remove a player from your home",
                min = 1, max = 1)
        @CommandPermissions({"aurora.home.self.remove"})
        public void removeMemberToHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);
            RegionManager manager = WG.getRegionManager(player.getWorld());
            ProtectedRegion region = getStoodInHome(player, manager);

            if (region == null) {
                throw new CommandException("You are not currently standing in a house you own.");
            }
            RegionDBUtil.removeFromDomain(region.getMembers(), args.getPaddedSlice(1, 0), 0);
            try {
                manager.save();
            } catch (ProtectionDatabaseException e) {
                ChatUtil.sendError(sender, "Failed to remove players from your home.");
            }
            ChatUtil.send(player, "Home successfully updated!");
        }

        @Command(aliases = {"flag"}, usage = "<flag>", desc = "Flag a home",
                min = 1)
        @CommandPermissions({"aurora.home.self.flag"})
        public void flagHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);
            ProtectedRegion region = getStoodInHome(player, WG.getRegionManager(player.getWorld()));

            if (region == null) {
                throw new CommandException("You are not currently standing in a house you own.");
            }
            player.performCommand("rg flag " + region.getId() + " " + args.getJoinedStrings(0));

        }

        @Command(aliases = {"location", "loc"}, desc = "Home Manager")
        @NestedCommand({TeleportCommands.class})
        public void homeTeleportCmds(CommandContext args, CommandSender sender) throws CommandException {

        }

        @Command(aliases = {"rules"}, usage = "[district]", desc = "District Rules", min = 0, max = 1)
        public void homeRuleCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);

            String district;

            if (args.argsLength() == 0) {
                ProtectedRegion region = getStoodInHome(player, WG.getRegionManager(player.getWorld()));

                if (region == null) {
                    throw new CommandException("You are not currently standing in a house you own.");
                }
                district = region.getParent().getId().replace("-district", "");
            } else {
                district = args.getString(0).toLowerCase().replace("-district", "");
            }
        }

        @Command(aliases = {"buy"}, desc = "Buy a house", min = 0, max = 0, flags = "y")
        @CommandPermissions("aurora.auth.member")
        public void homeBuyCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);

            RegionManager manager = WG.getRegionManager(player.getWorld());
            ProtectedRegion region = null;

            for (ProtectedRegion aRegion : manager.getApplicableRegions(player.getLocation())) {
                if (aRegion.getId().endsWith("-s")) {
                    region = aRegion;
                    break;
                }
            }

            if (region == null) {
                throw new CommandException("No plots for purchase were found where you're standing." +
                        "\nAvailable plots will have a blue outline around them.");
            }

            // Get the price and send it to the player
            Double price = region.getFlag(DefaultFlag.PRICE);

            if (price == null) {
                throw new CommandException("This house cannot currently be bought.");
            }

            int chunks = RegionUtil.sumChunks(new BukkitPlayer(WG, player), manager);
            int newChunks = chunks + RegionUtil.countChunks(region);

            price += RegionUtil.calcChunkPrice(newChunks) - RegionUtil.calcChunkPrice(chunks);

            String priceString = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(price), "");
            ChatUtil.send(player, "This property will cost you: " + priceString + ".");

            // If they have used the flag y proceed to buy the house
            // otherwise inform them about how to buy the house
            if (args.hasFlag('y')) {
                if (!econ.has(player, price)) {
                    throw new CommandException("Sorry, you cannot currently afford this house.");
                }
                try {
                    String homeName = getHomeName(player, manager);
                    if (RegionUtil.renameRegion(manager, region.getId(), homeName, true)) {

                        region = manager.getRegion(homeName);
                        region.getOwners().addPlayer(player.getName());
                        region.setFlag(DefaultFlag.PRICE, null);
                        manager.addRegion(region);
                        manager.save();

                        outliner.revert(player.getWorld(), region);

                        econ.withdrawPlayer(player, price);
                        ChatUtil.send(player, "Home successfully purchased!");
                    } else {
                        throw new CommandException();
                    }
                } catch (Exception ex) {
                    throw new CommandException("Failed to purchase this home.");
                }
            } else {
                ChatUtil.send(player, "If you would like to buy this home please use /home buy -y");
            }
        }

        @Command(aliases = {"sell"}, desc = "Sells your house", min = 0, max = 0, flags = "y")
        @CommandPermissions("aurora.auth.member")
        public void homeSellCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player player = PlayerUtil.checkPlayer(sender);

            RegionManager manager = WG.getRegionManager(player.getWorld());
            ProtectedRegion region = getStoodInHome(player, manager);

            if (region == null) throw new CommandException("You are not currently standing in any regions which you can sell.");

            // Get the price and send it to the player
            double blockPrice = RegionUtil.calcBlockPrice(region, new BukkitWorld(player.getWorld()));

            if (blockPrice < 0) {
                throw new CommandException("Your house cannot currently be sold.");
            }

            int chunks = RegionUtil.sumChunks(new BukkitPlayer(WG, player), manager);
            int newChunks = chunks - RegionUtil.countChunks(region);

            double price = blockPrice + (RegionUtil.calcChunkPrice(chunks) - RegionUtil.calcChunkPrice(newChunks)) * .9;

            String priceString = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(price), "");
            ChatUtil.send(player, "Your will get: " + priceString + ".");

            // If they have used the flag y proceed to sell the house
            // otherwise inform them about how to sell their house
            if (args.hasFlag('y')) {
                try {
                    String newName = System.currentTimeMillis() + "-s";
                    if (RegionUtil.renameRegion(manager, region.getId(), newName, true)) {

                        // Set the price flag's value and then resave the database
                        region = manager.getRegion(newName);
                        region.setFlag(DefaultFlag.PRICE, blockPrice * 1.1);
                        manager.addRegion(region);
                        manager.save();

                        outliner.outline(player.getWorld(), region);

                        econ.depositPlayer(player, price);
                        ChatUtil.send(player, "Home successfully sold!");
                    } else {
                        throw new CommandException();
                    }
                } catch (Exception ex) {
                    throw new CommandException("Failed to sell your home.");
                }
            } else {
                ChatUtil.send(player, "If you would like to sell your home please use /home sell -y");
            }
        }


        @Command(aliases = {"admin"}, desc = "Admin Commands")
        @NestedCommand({HomeAdminCommands.class})
        public void homeAdminCmds(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class HomeAdminCommands {

        @Command(aliases = {"create"}, usage = "[player] <district>", desc = "Create a home",
                min = 1, max = 2)
        @CommandPermissions({"aurora.home.admin.create"})
        public void createHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player admin = PlayerUtil.checkPlayer(sender);
            String player, regionString, district;

            RegionManager manager = WG.getRegionManager(admin.getWorld());

            if (args.argsLength() == 2) {
                player = args.getString(0).toLowerCase();
                regionString = getHomeName(player, manager);
                district = args.getString(1).toLowerCase().replace("-district", "");
            } else {
                player = null;
                regionString = System.currentTimeMillis() + "-s";
                district = args.getString(0).toLowerCase().replace("-district", "");
            }

            ProtectedRegion region;

            if (manager.hasRegion(regionString)) throw new CommandException("That player already has a home.");

            Selection sel = WE.getSelection(admin);
            if (sel == null) throw new CommandException("Select a region with WorldEdit first.");

            if (sel instanceof Polygonal2DSelection) {
                Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
                int minY = polySel.getNativeMinimumPoint().getBlockY();
                int maxY = polySel.getNativeMaximumPoint().getBlockY();
                region = new ProtectedPolygonalRegion(regionString, polySel.getNativePoints(), minY, maxY);
            } else if (sel instanceof CuboidSelection) {
                BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
                BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
                region = new ProtectedCuboidRegion(regionString, min, max);
            } else {
                throw new CommandException("The type of region selected in WorldEdit is unsupported.");
            }

            if (player == null) {
                region.setFlag(DefaultFlag.PRICE,
                        RegionUtil.calcBlockPrice(region, new BukkitWorld(admin.getWorld())));
            } else {
                region.getOwners().addPlayer(player);
            }
            region.setPriority(10);
            ProtectedRegion districtRegion = manager.getRegion(district + "-district");
            if (districtRegion == null) throw new CommandException("Invalid district specified.");
            try {
                region.setParent(districtRegion);
            } catch (ProtectedRegion.CircularInheritanceException e) {
                throw new CommandException("Circular inheritance detected.");
            }

            manager.addRegion(region);
            try {
                manager.save();

                outliner.outline(admin.getWorld(), region);
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to create the region: " + regionString + ".");
            }

            ChatUtil.send(admin, "The home: " + regionString + " has been created successfully in "
                    + district + ".");
            if (player != null) {
                ChatUtil.send(player, "A home has been created for you by: " + admin.getDisplayName() + ".");
                log.info(admin.getName() + " created a home for: " + player
                        + " in the district: " + district + ".");
            }
        }

        @Command(aliases = {"recompile"}, usage = "", desc = "Re evaluates all purchasable plots " +
                "in the location the admin is standing",
                min = 0, max = 0)
        @CommandPermissions({"aurora.home.admin.recompile"})
        public void recompileHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player admin = PlayerUtil.checkPlayer(sender);

            RegionManager manager = WG.getRegionManager(admin.getWorld());

            for (ProtectedRegion region : manager.getApplicableRegions(admin.getLocation())) {
                if (region.getId().endsWith("-s") && region.getFlag(DefaultFlag.PRICE) != null) {
                    region.setFlag(
                            DefaultFlag.PRICE,
                            RegionUtil.calcBlockPrice(
                                    region,
                                    new BukkitWorld(admin.getWorld())
                            )
                    );
                    outliner.outline(admin.getWorld(), region);
                }
            }
            try {
                manager.save();
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to save the database.");
            }

            ChatUtil.send(admin, "Region(s) recalculated successfully.");
        }

        @Command(aliases = {"pc"}, desc = "Price Checker",
                usage = "[player]",
                flags = "cf", min = 0, max = 1)
        @CommandPermissions({"aurora.home.admin.pc"})
        public void priceCheckHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Player sendingPlayer = PlayerUtil.checkPlayer(sender);

            Selection selection = WE.getSelection(sendingPlayer);

            if (selection == null) throw new CommandException("Select a region with WorldEdit first.");

            if (!(selection instanceof Polygonal2DSelection || selection instanceof CuboidSelection)) {
                throw new CommandException("The type of region selected in WorldEdit is unsupported.");
            }

            Player player = args.argsLength() == 0 ? null : InputUtil.PlayerParser.matchPlayerExactly(sender, args.getString(0));

            double size = (selection.getLength() * selection.getWidth()) / (16D * 16D);

            List<CommandSender> target = Arrays.asList(
                    sender, player
            );

            ChatUtil.send(target, "Chunks: " + size);

            double p1 = RegionUtil.calcChunkPrice(size);

            String chunkPrice = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(p1), "");
            ChatUtil.send(target, "Chunk Price: " + chunkPrice + ".");

            // Block Price
            double p2 = 0;

            if (!args.hasFlag('f')) {
                Region region = selection.getRegionSelector().getIncompleteRegion();
                World world = region.getWorld();

                if (region instanceof CuboidRegion) {

                    // Doing this for speed
                    Vector min = region.getMinimumPoint();
                    Vector max = region.getMaximumPoint();

                    int minX = min.getBlockX();
                    int minY = min.getBlockY();
                    int minZ = min.getBlockZ();
                    int maxX = max.getBlockX();
                    int maxY = max.getBlockY();
                    int maxZ = max.getBlockZ();

                    for (int x = minX; x <= maxX; ++x) {
                        for (int y = minY; y <= maxY; ++y) {
                            for (int z = minZ; z <= maxZ; ++z) {
                                Vector pt = new Vector(x, y, z);
                                BaseBlock b = world.getBlock(pt);
                                p2 += AdminStoreComponent.priceCheck(b.getId(), b.getData());
                            }
                        }
                    }
                } else {
                    throw new CommandException("Not yet supported.");
                }
            }

            String housePrice = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(p2), "");
            ChatUtil.send(target, "Block Price: " + housePrice + ".");

            double total = p1 + p2;
            if (args.hasFlag('c')) {
                String commission = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(total * .1), "");

                ChatUtil.send(target, "Commission change: " + commission);
                total *= 1.1;
            }

            String totalPrice = ChatUtil.makeCountString(ChatColor.YELLOW, econ.format(total), "");
            ChatUtil.send(target, "Total Price: " + totalPrice + ".");
        }

        /*
        @Command(aliases = {"flag"}, usage = "<player> <flag>", desc = "Flag a home",
                min = 2)
        @CommandPermissions({"aurora.home.admin.flag"})
        public void flagHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            Bukkit.dispatchCommand(sender, "rg flag " + getHomeName(args.getString(0)) + " " + args.getJoinedStrings(1));
        }
        */

        @Command(aliases = {"move"}, usage = "<player> <newdistrict>", desc = "Move a home",
                min = 2, max = 2)
        @CommandPermissions({"aurora.home.admin.move"})
        public void moveHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            throw new CommandException("This command needs updated.");

            /*
            Player admin = PlayerUtil.checkPlayer(sender);

            String player = args.getString(0).toLowerCase();
            String district = args.getString(1).toLowerCase().replace("-district", "");

            RegionManager manager = WG.getRegionManager(admin.getWorld());
            ProtectedRegion existing = manager.getRegionExact(getHomeName(player));
            if (existing == null) throw new CommandException("That player doesn't have a home.");
            Selection sel = WE.getSelection(admin);
            if (sel == null) throw new CommandException("Select a region with WorldEdit first.");

            ProtectedRegion region;

            // Detect the type of region from WorldEdit
            if (sel instanceof Polygonal2DSelection) {
                Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
                int minY = polySel.getNativeMinimumPoint().getBlockY();
                int maxY = polySel.getNativeMaximumPoint().getBlockY();
                region = new ProtectedPolygonalRegion(getHomeName(player), polySel.getNativePoints(), minY, maxY);
            } else if (sel instanceof CuboidSelection) {
                BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
                BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
                region = new ProtectedCuboidRegion(getHomeName(player), min, max);
            } else {
                throw new CommandException("The type of region selected in WorldEdit is unsupported.");
            }

            region.setMembers(existing.getMembers());
            region.setOwners(existing.getOwners());
            region.setFlags(existing.getFlags());
            region.setPriority(existing.getPriority());
            ProtectedRegion districtRegion = manager.getRegion(district + "-district");
            if (districtRegion == null) throw new CommandException("Invalid district specified.");
            try {
                region.setParent(districtRegion);
            } catch (ProtectedRegion.CircularInheritanceException e) {
                throw new CommandException("Circular inheritance detected.");
            }

            manager.addRegion(region);
            try {
                manager.save();
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to create a home for: " + player + ".");
            }

            giveRuleBook(sender, player, district);

            ChatUtil.send(admin, "The player: " + player + "'s house has been moved to: " + district + ".");
            ChatUtil.send(player, "Your home has been moved for you by: " + admin.getDisplayName() + ".");
            log.info(admin.getName() + " moved a home for: " + player + " into the district: " + district + ".");
            */
        }

        @Command(aliases = {"remove"}, usage = "<player>", desc = "Remove a home", min = 1, max = 1)
        @CommandPermissions({"aurora.home.admin.remove"})
        public void removeHomeCmd(CommandContext args, CommandSender sender) throws CommandException {

            throw new CommandException("This command needs updated.");

            /*
            Player admin = PlayerUtil.checkPlayer(sender);
            String player = args.getString(0).toLowerCase();

            RegionManager manager = WG.getRegionManager(admin.getWorld());
            ProtectedRegion region = manager.getRegionExact(getHomeName(player));
            if (region == null) throw new CommandException("That player doesn't have a home.");

            manager.removeRegion(region.getId());
            try {
                manager.save();
            } catch (ProtectionDatabaseException e) {
                throw new CommandException("Failed to remove the home of: " + player + ".");
            }
            ChatUtil.send(admin, "The player: " + player + "'s house has been removed.");
            ChatUtil.send(player, "Your home has been removed by: " + admin.getDisplayName() + ".");
            log.info(admin.getName() + " deleted the player: " + player + "'s home.");
            */
        }

        @Command(aliases = {"help"}, desc = "Admin Help", min = 0, max = 0)
        @CommandPermissions({"aurora.home.admin.help"})
        public void adminHelpCmd(CommandContext args, CommandSender sender) throws CommandException {

            PlayerUtil.checkPlayer(sender).getInventory().addItem(BookUtil.Help.Admin.housing());
        }
    }

    public static String getHomeName(String player, RegionManager manager) {
        String name = player.toLowerCase() + "'s-%-house";
        String cName = "";
        for (int cur = 0; cName.isEmpty() || manager.hasRegion(cName); cur++) {
            cName = name.replace("%", String.valueOf(cur));
        }
        return cName;
    }

    public static ProtectedRegion getStoodInHome(Player player, RegionManager manager) {
        ProtectedRegion region = null;
        for (ProtectedRegion aRegion : manager.getApplicableRegions(player.getLocation())) {
            if (isPlayerHouse(aRegion, new BukkitPlayer(WorldGuardPlugin.inst(), player))) {
                region = aRegion;
                break;
            }
        }
        return region;
    }

    public static String getHomeName(Player player, RegionManager manager) {

        return getHomeName(player.getName(), manager);
    }

    public static boolean isPlayerHouse(ProtectedRegion region, LocalPlayer player) {
        return region.getId().endsWith("-house") && region.getOwners().contains(player.getName());
    }

    public static boolean isPlayerHouse(ProtectedRegion region, Player player) {
        return isPlayerHouse(region, new BukkitPlayer(WorldGuardPlugin.inst(), player));
    }

    public class TeleportCommands {

        @Command(aliases = {"set"}, desc = "Set your home")
        public void setHomeLoc(CommandContext args, CommandSender sender) {

            ChatUtil.send(sender, "To set your home, sleep in a bed during the night.");
        }

        @Command(aliases = {"teleport", "tp"}, desc = "Go to your home")
        public void teleportHome(CommandContext args, CommandSender sender) {

            ChatUtil.send(sender, "To go to your home, throw an ender pearl at your feet.");
        }

    }
}
