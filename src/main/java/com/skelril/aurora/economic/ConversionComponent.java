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

package com.skelril.aurora.economic;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.admin.AdminComponent;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EnvironmentUtil;
import com.skelril.aurora.util.item.ItemUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Conversion", desc = "Convert your cash.")
@Depend(plugins = {"Vault"}, components = {AdminComponent.class, ImpersonalComponent.class})
public class ConversionComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    @InjectComponent
    AdminComponent adminComponent;
    @InjectComponent
    ImpersonalComponent impersonalComponent;

    private static Economy economy = null;
    private List<Player> recentList = new ArrayList<>();

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
        setupEconomy();
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !EnvironmentUtil.isSign(block)
                || recentList.contains(player) || adminComponent.isAdmin(player)) return;

        Sign sign = (Sign) block.getState();

        if (sign.getLine(1).equals("[Bank]") || sign.getLine(1).equals("[Conversion]")) {
            if (!impersonalComponent.check(block, true)) return;
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                try {
                    int tranCount = Integer.parseInt(sign.getLine(2));
                    int goldCount = 0;
                    int amount = 0;
                    int flexAmount;

                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null) {
                            if (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName()) continue;
                            if (itemStack.getTypeId() == ItemID.GOLD_NUGGET) {
                                goldCount += itemStack.getAmount();
                            } else if (itemStack.getTypeId() == ItemID.GOLD_BAR) {
                                goldCount += itemStack.getAmount() * 9;
                            } else if (itemStack.getTypeId() == BlockID.GOLD_BLOCK) {
                                goldCount += itemStack.getAmount() * 81;
                            }
                        }
                    }

                    if (tranCount >= goldCount) {
                        amount = goldCount;
                    } else if (goldCount > tranCount) {
                        amount = tranCount;
                    }

                    flexAmount = amount;
                    while (flexAmount / 81 > 0 && ItemUtil.countItemsOfType(player.getInventory().getContents(), BlockID.GOLD_BLOCK) > 0) {
                        flexAmount -= 81;
                        player.getInventory().removeItem(new ItemStack(BlockID.GOLD_BLOCK));
                    }

                    while (flexAmount / 9 > 0 && ItemUtil.countItemsOfType(player.getInventory().getContents(), ItemID.GOLD_BAR) > 0) {
                        flexAmount -= 9;
                        player.getInventory().removeItem(new ItemStack(ItemID.GOLD_BAR));
                    }

                    while (flexAmount > 0 && ItemUtil.countItemsOfType(player.getInventory().getContents(), ItemID.GOLD_NUGGET) > 0) {
                        flexAmount--;
                        player.getInventory().removeItem(new ItemStack(ItemID.GOLD_NUGGET));
                    }

                    player.updateInventory();

                    economy.depositPlayer(player, amount - flexAmount);
                    if (amount - flexAmount != 1) {
                        ChatUtil.send(player, "You deposited: "
                                + ChatUtil.makeCountString(economy.format(amount - flexAmount), "."));
                    } else {
                        ChatUtil.send(player, "You deposited: "
                                + ChatUtil.makeCountString(economy.format(amount - flexAmount), "."));
                    }

                } catch (NumberFormatException ignored) {
                }
            } else if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                if (player.isSneaking()) return;
                try {
                    int tranCount = Integer.parseInt(sign.getLine(2));
                    int bankGold = (int) economy.getBalance(player);
                    int amount = 0;
                    int flexAmount = 0;

                    if (tranCount >= bankGold) {
                        amount = bankGold;
                    } else if (bankGold > tranCount) {
                        amount = tranCount;
                    }

                    flexAmount = amount;
                    while (flexAmount / 81 > 0 && player.getInventory().firstEmpty() != -1) {
                        flexAmount -= 81;
                        player.getInventory().addItem(new ItemStack(BlockID.GOLD_BLOCK));
                    }

                    while (flexAmount / 9 > 0 && player.getInventory().firstEmpty() != -1) {
                        flexAmount -= 9;
                        player.getInventory().addItem(new ItemStack(ItemID.GOLD_BAR));
                    }

                    while (flexAmount > 0 && player.getInventory().firstEmpty() != -1) {
                        flexAmount--;
                        player.getInventory().addItem(new ItemStack(ItemID.GOLD_NUGGET));
                    }

                    while (ItemUtil.countItemsOfType(player.getInventory().getContents(), ItemID.GOLD_NUGGET) / 9 > 0
                            && player.getInventory().firstEmpty() != -1) {
                        player.getInventory().removeItem(new ItemStack(ItemID.GOLD_NUGGET, 9));
                        player.getInventory().addItem(new ItemStack(ItemID.GOLD_BAR));
                    }

                    while (ItemUtil.countItemsOfType(player.getInventory().getContents(), ItemID.GOLD_BAR) / 9 > 0
                            && player.getInventory().firstEmpty() != -1) {
                        player.getInventory().removeItem(new ItemStack(ItemID.GOLD_BAR, 9));
                        player.getInventory().addItem(new ItemStack(BlockID.GOLD_BLOCK));
                    }

                    player.updateInventory();

                    economy.withdrawPlayer(player, amount - flexAmount);
                    if (amount - flexAmount != 1) {
                        ChatUtil.send(player, "You withdrew: "
                                + ChatUtil.makeCountString(economy.format(amount - flexAmount), "."));
                    } else {
                        ChatUtil.send(player, "You withdrew: "
                                + ChatUtil.makeCountString(economy.format(amount - flexAmount), "."));
                    }

                } catch (NumberFormatException ignored) {
                }
            }
            recentList.add(player);
            server.getScheduler().scheduleSyncDelayedTask(inst, () -> recentList.remove(player), 10);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBlockPlace(SignChangeEvent event) {

        Block block = event.getBlock();
        Player player = event.getPlayer();

        String header = event.getLine(1);
        boolean validHeader = false;

        if (header.equalsIgnoreCase("[Bank]")) {
            if (inst.hasPermission(player, "aurora.conversion.sign")) {
                event.setLine(1, "[Bank]");
                validHeader = true;
            }
        } else if (header.equalsIgnoreCase("[Conversion]")) {
            if (inst.hasPermission(player, "aurora.conversion.sign")) {
                event.setLine(1, "[Conversion]");
                validHeader = true;
            }
        } else {
            return;
        }

        if (!validHeader) {
            event.setCancelled(true);
            block.breakNaturally(new ItemStack(ItemID.SIGN));
        }

        try {
            Integer.parseInt(event.getLine(2).trim());
        } catch (NumberFormatException e) {
            ChatUtil.sendError(player, "The third line must be the amount of "
                    + economy.currencyNamePlural() + " to be transferred.");
            event.setCancelled(true);
            block.breakNaturally(new ItemStack(ItemID.SIGN));
        }
    }

    private boolean setupEconomy() {

        RegisteredServiceProvider<Economy> economyProvider = server.getServicesManager().getRegistration(net.milkbowl
                .vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
