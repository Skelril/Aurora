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
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
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

    @EventHandler
    public void onPvP(PlayerVsPlayerEvent event) {
        Player attacker = event.getPlayer();
        Player defender = event.getDefender();

        PartyBookReader partyBook = PartyBookReader.getFrom(attacker.getItemInHand());
        if (partyBook == null) return;

        if (!event.isCancelled()) {
            event.setCancelled(true);
        }

        defender.getInventory().addItem(new PartyScrollReader(partyBook.getShard(), attacker.getName()).build());
        ChatUtil.sendNotice(defender, attacker.getName() + " has given you a party scroll!");
        ChatUtil.sendNotice(defender, "Right click to accept, drop to decline.");
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item stack = event.getItemDrop();
        if (ItemUtil.isItem(stack.getItemStack(), CustomItems.PARTY_SCROLL)) {
            ChatUtil.sendWarning(event.getPlayer(), "Invitation declined!");
            stack.remove();
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
                ItemStack[] itemStacks = target.getInventory().getContents();
                for (int i = 0; i < itemStacks.length; ++i) {
                    if (itemStacks[i] == null) continue;
                    PartyBookReader book = PartyBookReader.getFrom(itemStacks[i]);
                    if (book == null) continue;
                    book.addPlayer(player.getName());
                    itemStacks[i] = book.build();
                    break;
                }
                target.getInventory().setContents(itemStacks);
                ChatUtil.sendNotice(player, "Invitation accepted!");
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
                    if (!it.next().getName().startsWith(prefix)) {
                        it.remove();
                    }
                }
            }
            new PaginatedResult<ShardType>(ChatColor.GOLD + "Party Books") {
                @Override
                public String format(ShardType shardType) {
                    return ChatColor.BLUE + shardType.getName().toUpperCase();
                }
            }.display(sender, shardTypes, args.getFlagInteger('p', 1));
        }

        @Command(aliases = {"get"},
                usage = "<instance>", desc = "Get a party book",
                flags = "", min = 1)
        public void getCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            ShardType type = ShardType.valueOf(args.getJoinedStrings(0).toUpperCase().replaceAll(" ", "_"));
            player.getInventory().addItem(new PartyBookReader(type, player.getName()).build());
        }
    }
}
