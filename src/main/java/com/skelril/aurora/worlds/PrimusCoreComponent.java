/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.worlds;

import com.sk89q.commandbook.session.SessionComponent;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.events.shard.PartyActivateEvent;
import com.skelril.aurora.events.wishingwell.PlayerAttemptItemWishEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.item.PartyBookReader;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.registerEvents;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

@ComponentInformation(friendlyName = "Primus Core", desc = "Operate the Primus World.")
@Depend(components = {AdminComponent.class, SessionComponent.class})
public class PrimusCoreComponent extends BukkitComponent implements Listener {

    private World world;

    @Override
    public void enable() {
        world = Bukkit.getWorld("Primus");
        registerEvents(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAttemptItemWish(PlayerAttemptItemWishEvent event) {

        ItemStack stack = event.getItemStack();

        // Remove party books whether we can fully parse them or not
        if (!ItemUtil.isItem(stack, CustomItems.PARTY_BOOK)) {
            return;
        }

        boolean worldB = world.equals(event.getLocation().getWorld());
        boolean waterB = EnvironmentUtil.isWater(event.getLocation().getBlock());

        if (!worldB) {
            event.setResult(PlayerAttemptItemWishEvent.Result.ALLOW_IGNORE);
            return;
        }

        if (!waterB) return;
        event.setResult(PlayerAttemptItemWishEvent.Result.ALLOW_IGNORE);

        PartyBookReader partyBook = PartyBookReader.getFrom(stack);
        if (partyBook == null) return;
        List<Player> players = new ArrayList<>();
        for (String player : partyBook.getAllPlayers()) {
            Player aPlayer = Bukkit.getPlayerExact(player);
            if (aPlayer != null) {
                players.add(aPlayer);
            }
        }
        callEvent(new PartyActivateEvent(partyBook.getShard(), players));
    }
}
