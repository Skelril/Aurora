/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.google.common.collect.Lists;
import com.sk89q.commandbook.commands.PaginatedResult;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.*;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.PlayerVsPlayerEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.item.PartyBookReader;
import com.skelril.aurora.util.item.PartyScrollReader;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Party Book", desc = "Operate Party Books.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class PartyBookComponent extends BukkitComponent implements Listener {

    @Override
    public void enable() {
        registerEvents(this);
        registerCommands(Commands.class);
    }

    public static boolean hasPartyPermission(CommandSender sender, ShardType shard) {
        return inst().hasPermission(sender, "aurora.partybook." + shard.name());
    }

    @EventHandler
    public void onPvP(PlayerVsPlayerEvent event) {
        Player attacker = event.getPlayer();
        Player defender = event.getDefender();

        PartyBookReader partyBook = PartyBookReader.getFrom(attacker.getItemInHand());
        if (partyBook == null) return;

        if (!event.isCancelled()) {
            event.setCancelled(true);
        }

        if (!attacker.getName().equals(partyBook.getOwner())) {
            ChatUtil.sendError(attacker, "You are not the owner of that Party Book!");
            return;
        }

        int max = partyBook.getShard().getMaxPlayers();
        if (max != -1 && partyBook.getAllPlayers().size() > max) {
            ChatUtil.sendError(attacker, "You've reached the maximum players allowed in this Party Book!");
            return;
        }

        if (partyBook.getAllPlayers().contains(defender.getName())) {
            ChatUtil.sendError(attacker, defender.getName() + " is already in that party!");
            return;
        }

        ItemStack scroll = new PartyScrollReader(partyBook.getShard(), attacker.getName()).build();
        if (defender.getInventory().containsAtLeast(scroll, 1)) {
            ChatUtil.sendError(attacker, defender.getName() + " already has an invite to that party!");
            return;
        }
        defender.getInventory().addItem(scroll);
        ChatUtil.sendNotice(defender, attacker.getName() + " has given you a party scroll!");
        ChatUtil.sendNotice(defender, "Right click to accept, drop to decline.");
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item stack = event.getItemDrop();
        ItemStack itemStack = stack.getItemStack();
        Player player = event.getPlayer();
        if (ItemUtil.isItem(itemStack, CustomItems.PARTY_SCROLL)) {
            ChatUtil.sendWarning(player, "Invitation declined!");
            stack.remove();
        } else if (ItemUtil.isItem(itemStack, CustomItems.PARTY_BOOK)) {
            // Remove any extra Party Books
            ItemStack[] itemStacks = player.getInventory().getContents();
            for (int i = 0; i < itemStacks.length; ++i) {
                if (ItemUtil.isItem(itemStacks[i], CustomItems.PARTY_BOOK)) {
                    itemStacks[i] = null;
                }
            }
            player.getInventory().setContents(itemStacks);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getItemInHand();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                PartyScrollReader scroll = PartyScrollReader.getPartyScroll(itemStack);
                if (scroll == null) {
                    return;
                }

                server().getScheduler().runTaskLater(inst(), () -> player.setItemInHand(null), 1);
                Player target = Bukkit.getPlayerExact(scroll.getOwner());
                if (target == null) {
                    ChatUtil.sendError(player, "Invitation expired!");
                    return;
                }
                boolean foundBook = false;
                ItemStack[] itemStacks = target.getInventory().getContents();
                for (int i = 0; i < itemStacks.length; ++i) {
                    if (!ItemUtil.isItem(itemStacks[i], CustomItems.PARTY_BOOK)) continue;
                    if (!foundBook) {
                        PartyBookReader book = PartyBookReader.getFrom(itemStacks[i]);
                        if (book != null && book.getShard() == scroll.getShard()) {
                            book.addPlayer(player.getName());
                            itemStacks[i] = book.build();
                            foundBook = true;
                            continue;
                        }
                    }
                    // Notify Invite Holder
                    ChatUtil.sendError(player, target.getName() + " has too many Party Books, or no longer has a Party Book.");
                    ChatUtil.sendError(player, "You will need a new invitation.");

                    // Notify Book Holder
                    ChatUtil.sendError(target, "Your invitation to " + scroll.getShard() + " for " + player.getName() + " has been invalidated.");
                    ChatUtil.sendNotice(target, "If you still want to play with " + player.getName() + " please make sure");
                    ChatUtil.sendNotice(target, "you have only one Party Book, then reinvite " + player.getName() + ".");
                    return;
                }
                target.getInventory().setContents(itemStacks);
                ChatUtil.sendNotice(player, "Invitation accepted!");
                ChatUtil.sendNotice(target, player.getName() + " accepted your invitation!");
                break;
        }
    }

    public class Commands {

        @Command(aliases = {"partybook", "pbook"}, desc = "Party book commands")
        @NestedCommand(PartyBookCommands.class)
        public void partyBookCmds(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class PartyBookCommands {
        @Command(aliases = {"list"},
                usage = "[-p page] [instance name]", desc = "List party books",
                flags = "p:", min = 0)
        public void listCmd(CommandContext args, CommandSender sender) throws CommandException {
            List<ShardType> shardTypes = Lists.newArrayList(ShardType.values());
            if (args.argsLength() > 0) {
                String prefix = args.getJoinedStrings(0);
                Iterator<ShardType> it = shardTypes.iterator();
                while (it.hasNext()) {
                    ShardType next = it.next();
                    if (!next.getName().startsWith(prefix) || !hasPartyPermission(sender, next)) {
                        it.remove();
                    }
                }
            }
            new PaginatedResult<ShardType>(ChatColor.GOLD + "Party Books") {
                @Override
                public String format(ShardType shardType) {
                    int maxPlayers = shardType.getMaxPlayers();
                    return ChatColor.BLUE + shardType.getName().toUpperCase()
                            + ChatColor.YELLOW + " (Type: "
                            + ChatColor.WHITE + shardType.getSubType().getProperName()
                            + ChatColor.YELLOW + ", Max players: "
                            + ChatColor.WHITE + (maxPlayers == -1 ? "Unlimted" : maxPlayers)
                            + ChatColor.YELLOW + ")";
                }
            }.display(sender, shardTypes, args.getFlagInteger('p', 1));
        }

        @Command(aliases = {"get"},
                usage = "<instance>", desc = "Get a party book",
                flags = "", min = 1)
        public void getCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            try {
                ShardType type = ShardType.valueOf(args.getJoinedStrings(0).toUpperCase().replaceAll(" ", "_"));
                if (!hasPartyPermission(player, type)) {
                    throw new CommandPermissionsException();
                }
                player.getInventory().addItem(new PartyBookReader(type, player.getName()).build());
            } catch (IllegalArgumentException ex) {
                throw new CommandException("There's no instance by that name!");
            }
        }
    }
}
