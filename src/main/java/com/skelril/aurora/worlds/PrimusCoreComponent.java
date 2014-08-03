/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.worlds;

import com.sk89q.commandbook.session.SessionComponent;
import com.sk89q.commandbook.util.entity.player.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent;
import com.skelril.aurora.util.item.PartyBook;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent.Result;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

@ComponentInformation(friendlyName = "Primus Core", desc = "Operate Primus.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class PrimusCoreComponent extends BukkitComponent implements Listener {

    @Override
    public void enable() {
        registerEvents(this);
        registerCommands(Commands.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttemptItemWish(PlayerAttemptItemWishEvent event) {
        if (event.getItemStack().getItemMeta() instanceof BookMeta) {
            PartyBook book = new PartyBook((BookMeta) event.getItemStack().getItemMeta());
            List<Player> players = new ArrayList<>();
            for (String player : book.getAllPlayers()) {
                Player aPlayer = Bukkit.getPlayerExact(player);
                if (aPlayer != null) {
                    players.add(aPlayer);
                }
            }
            event.setResult(Result.ALLOW_IGNORE);
            callEvent(new PartyActivateEvent(book.getInstance(), players));
        }
    }

    public class Commands {
        @Command(aliases = {"partybook"},
                usage = "<instance> <player,[player>", desc = "Get a party book",
                flags = "", min = 2)
        public void graveDigger(CommandContext args, CommandSender sender) throws CommandException {
            Player player = PlayerUtil.checkPlayer(sender);
            String instance = args.getString(0);
            String players = args.getJoinedStrings(1);
            Set<String> playerNames = new HashSet<>();
            Collections.addAll(playerNames, players.split(","));
            player.getInventory().addItem(new PartyBook(instance, player.getName(), playerNames).buildBook());
        }
    }
}
