/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.events.custom.item.ChanceActivationEvent;
import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.master.Blind;
import com.skelril.aurora.items.specialattack.attacks.melee.master.DoomBlade;
import com.skelril.aurora.items.specialattack.attacks.melee.master.HealingLight;
import com.skelril.aurora.items.specialattack.attacks.melee.master.UltimateStrength;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class MasterSwordImpl extends AbstractItemFeatureImpl implements SpecWeaponImpl {

    private static final int BASE_CHANCE = 17;

    @Override
    public boolean activate(LivingEntity owner, LivingEntity target) {
        if (!(owner instanceof Player)) {
            return ChanceUtil.getChance(BASE_CHANCE);
        }
        ChanceActivationEvent activationEvent = new ChanceActivationEvent(
                (Player) owner,
                target.getLocation(),
                BASE_CHANCE,
                ChanceActivationEvent.ChanceType.WEAPON
        );
        callEvent(activationEvent);
        return !activationEvent.isCancelled() && ChanceUtil.getChance(activationEvent.getChance());
    }

    @Override
    public SpecialAttack getSpecial(LivingEntity owner, LivingEntity target) {
        switch (ChanceUtil.getRandom(4)) {
            case 1:
                return new Blind(owner, target);
            case 2:
                return new DoomBlade(owner, target);
            case 3:
                return new HealingLight(owner, target);
            case 4:
                return new UltimateStrength(owner, target);
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSacrifice(PlayerItemWishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemStack();
        if (ItemUtil.isItem(item, CustomItems.MASTER_SWORD)) {
            item.setDurability((short) 0);
            player.getInventory().addItem(item);
            event.setItemStack(null);
        }
    }
}
