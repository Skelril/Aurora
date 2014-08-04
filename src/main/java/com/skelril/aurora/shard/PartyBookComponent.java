/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.shard;

import com.sk89q.commandbook.commands.PaginatedResult;
import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import com.skelril.aurora.util.item.PartyBook;
import com.skelril.aurora.util.item.PartyScroll;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent.Result;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Party Book", desc = "Operate Party Books.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class PartyBookComponent extends BukkitComponent implements Listener {

    @Override
    public void enable() {
        registerEvents(this);
        registerCommands(Commands.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttemptItemWish(PlayerAttemptItemWishEvent event) {
        PartyBook partyBook = PartyBook.getPartyBook(event.getItemStack());
        if (partyBook == null) return;
        List<Player> players = new ArrayList<>();
        for (String player : partyBook.getAllPlayers()) {
            Player aPlayer = Bukkit.getPlayerExact(player);
            if (aPlayer != null) {
                players.add(aPlayer);
            }
        }
        event.setResult(Result.ALLOW_IGNORE);
        callEvent(new PartyActivateEvent(partyBook.getShard(), players));
    }

    EDBEExtractor<Player, Player, Projectile> extractor = new EDBEExtractor<>(
            Player.class,
            Player.class,
            Projectile.class
    );

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        CombatantPair<Player, Player, Projectile> result = extractor.extractFrom(event);
        if (result == null) return;

        Player attacker = result.getAttacker();
        Player defender = result.getDefender();

        PartyBook partyBook = PartyBook.getPartyBook(attacker.getItemInHand());
        if (partyBook == null) return;
        defender.getInventory().addItem(new PartyScroll(partyBook.getShard(), attacker.getName()).buildScroll());
        ChatUtil.sendNotice(defender, attacker.getName() + " has given you a party scroll!");
        ChatUtil.sendNotice(defender, "Right click to accept, drop to decline.");
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item stack = event.getItemDrop();
        if (PartyScroll.getPartyScroll(stack.getItemStack()) != null) {
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
                PartyScroll scroll = PartyScroll.getPartyScroll(itemStack);
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
                    PartyBook book = PartyBook.getPartyBook(itemStacks[i]);
                    if (book == null) continue;
                    book.addPlayer(player.getName());
                    itemStacks[i] = book.buildBook();
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
                usage = "[instance name]", desc = "List party books",
                flags = "p:", min = 2)
        public void listCmd(CommandContext args, CommandSender sender) throws CommandException {
            new PaginatedResult<ShardType>(ChatColor.GOLD + "Party Books") {
                @Override
                public String format(ShardType shardType) {
                    return ChatColor.BLUE + shardType.getName().toUpperCase();
                }
            }.display(sender, Arrays.asList(ShardType.values()), args.getFlagInteger('p'));
        }

        @Command(aliases = {"get"},
                usage = "<instance>", desc = "Get a party book",
                flags = "", min = 1)
        public void getCmd(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            ShardType type = ShardType.valueOf(args.getJoinedStrings(0).toUpperCase().replaceAll(" ", "_"));
            player.getInventory().addItem(new PartyBook(type, player.getName()).buildBook());
        }
    }
}
