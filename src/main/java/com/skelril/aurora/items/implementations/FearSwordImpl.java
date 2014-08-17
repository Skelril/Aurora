/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.hybrid.fear.Curse;
import com.skelril.aurora.items.specialattack.attacks.melee.fear.*;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

public class FearSwordImpl extends AbstractItemFeatureImpl implements SpecWeaponImpl {
    @Override
    public boolean activate(LivingEntity owner, LivingEntity target) {
        return true;
    }

    @Override
    public SpecialAttack getSpecial(LivingEntity owner, LivingEntity target) {
        switch (ChanceUtil.getRandom(6)) {
            case 1:
                return new Confuse(owner, target);
            case 2:
                return new FearBlaze(owner, target);
            case 3:
                return new Curse(owner, target);
            case 4:
                return new Weaken(owner, target);
            case 5:
                return new Decimate(owner, target);
            case 6:
                return new SoulSmite(owner, target);
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSacrifice(PlayerItemWishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        int c;
        int o = 1;
        int m = item.getType().getMaxDurability();
        ItemStack[] i;
        if (ItemUtil.isItem(item, CustomItems.FEAR_SWORD)) {
            //if (!isInRewardsRoom) {
            //    o = 2;
            //}
            c = ItemUtil.countItemsOfName(player.getInventory().getContents(), CustomItems.GEM_OF_DARKNESS.toString());
            i = ItemUtil.removeItemOfName(player.getInventory().getContents(), CustomItems.GEM_OF_DARKNESS.toString());
            player.getInventory().setContents(i);
            while (item.getDurability() > 0 && c >= o) {
                item.setDurability((short) Math.max(0, item.getDurability() - (m / 9)));
                c -= o;
            }
            player.getInventory().addItem(item);
            int amount = Math.min(c, 64);
            while (amount > 0) {
                player.getInventory().addItem(CustomItemCenter.build(CustomItems.GEM_OF_DARKNESS, amount));
                c -= amount;
                amount = Math.min(c, 64);
            }
            player.updateInventory();
            event.setItemStack(null);
        }
    }
}
