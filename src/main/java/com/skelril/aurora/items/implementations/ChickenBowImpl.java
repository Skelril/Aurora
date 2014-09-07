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

import com.skelril.aurora.events.custom.item.SpecialAttackEvent;
import com.skelril.aurora.events.entity.ProjectileTickEvent;
import com.skelril.aurora.items.CustomItemSession;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.specialattack.SpecType;
import com.skelril.aurora.items.specialattack.attacks.ranged.misc.MobAttack;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class ChickenBowImpl extends AbstractItemFeatureImpl {
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

            if (session.canSpec(SpecType.ANIMAL_BOW)) {
                Class<? extends LivingEntity> type = null;
                if (ItemUtil.isItem(launcher, CustomItems.CHICKEN_BOW)) {
                    type = Chicken.class;
                }

                if (type != null) {
                    SpecialAttackEvent specEvent = callSpec(owner, launcher, SpecType.RANGED, new MobAttack(owner, targetLoc, type));
                    if (!specEvent.isCancelled()) {
                        session.updateSpec(SpecType.ANIMAL_BOW, specEvent.getSpec().getCoolDown());
                        specEvent.getSpec().activate();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArrowTick(ProjectileTickEvent event) {

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

            final Location location = projectile.getLocation();
            if (ItemUtil.isItem(launcher, CustomItems.CHICKEN_BOW)) {

                if (!ChanceUtil.getChance(5)) return;
                server().getScheduler().runTaskLater(inst(), () -> {
                    final Chicken chicken = location.getWorld().spawn(location, Chicken.class);
                    chicken.setRemoveWhenFarAway(true);
                    server().getScheduler().runTaskLater(inst(), () -> {
                        if (chicken.isValid()) {
                            chicken.remove();
                            for (int i = 0; i < 20; i++) {
                                chicken.getWorld().playEffect(chicken.getLocation(), Effect.SMOKE, 0);
                            }
                        }
                    }, 20 * 3);
                }, 3);
            }
        }
    }

}
