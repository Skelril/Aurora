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

package com.skelril.aurora.items.implementations;

import com.skelril.aurora.combat.PvPComponent;
import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.events.wishingwell.PlayerItemWishEvent;
import com.skelril.aurora.items.CustomItemSession;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecType;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.items.specialattack.attacks.hybrid.fear.Curse;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.Disarm;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.FearBomb;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.FearStrike;
import com.skelril.aurora.items.specialattack.attacks.ranged.fear.MagicChain;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import static com.zachsthings.libcomponents.bukkit.BasePlugin.callEvent;

public class FearBowImpl extends AbstractItemFeatureImpl implements SpecWeaponImpl {
    @Override
    public boolean activate(LivingEntity owner, LivingEntity target) {
        return true;
    }

    @Override
    public SpecialAttack getSpecial(LivingEntity owner, LivingEntity target) {
        switch (ChanceUtil.getRandom(5)) {
            case 1:
                Disarm disarmSpec = new Disarm(owner, target);
                if (disarmSpec.getItemStack() != null) {
                    return disarmSpec;
                }
            case 2:
                return new Curse(owner, target);
            case 3:
                return new MagicChain(owner, target);
            case 4:
                return new FearStrike(owner, target);
            case 5:
                return new FearBomb(owner, target);
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
        if (ItemUtil.isItem(item, CustomItems.FEAR_BOW)) {
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

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {

        Projectile projectile = event.getEntity();
        Entity shooter = null;

        ProjectileSource source = projectile.getShooter();
        if (source instanceof Entity) {
            shooter = (Entity) source;
        }

        if (shooter != null && shooter instanceof Player && projectile.hasMetadata("launcher")) {

            Object test = projectile.getMetadata("launcher").get(0).value();

            if (!(test instanceof ItemStack)) return;

            ItemStack launcher = (ItemStack) test;

            final Player owner = (Player) shooter;
            final Location targetLoc = projectile.getLocation();

            CustomItemSession session = getSession(owner);

            if (!session.canSpec(SpecType.RANGED)) {

                if (ItemUtil.isItem(launcher, CustomItems.FEAR_BOW)) {
                    if (!targetLoc.getWorld().isThundering() && targetLoc.getBlock().getLightFromSky() > 0) {

                        callEvent(new RapidHitEvent(owner));

                        // Simulate a lightning strike
                        targetLoc.getWorld().strikeLightningEffect(targetLoc);
                        for (Entity e : projectile.getNearbyEntities(2, 4, 2)) {
                            if (!e.isValid() || !(e instanceof LivingEntity)) continue;
                            // Pig Zombie
                            if (e instanceof Pig) {
                                e.getWorld().spawn(e.getLocation(), PigZombie.class);
                                e.remove();
                                continue;
                            }
                            // Creeper
                            if (e instanceof Creeper) {
                                ((Creeper) e).setPowered(true);
                            }
                            // Player
                            if (e instanceof Player) {
                                if (!PvPComponent.allowsPvP(owner, (Player) e)) continue;
                            }

                            ((LivingEntity) e).damage(5, owner);
                        }
                    }
                }
            }
        }
    }
}
