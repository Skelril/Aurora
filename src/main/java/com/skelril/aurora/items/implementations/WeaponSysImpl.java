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
import com.skelril.aurora.items.CustomItemSession;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.items.generic.AbstractItemFeatureImpl;
import com.skelril.aurora.items.generic.weapons.SpecWeaponImpl;
import com.skelril.aurora.items.specialattack.SpecType;
import com.skelril.aurora.items.specialattack.SpecialAttack;
import com.skelril.aurora.util.DamageUtil;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import com.skelril.aurora.util.item.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static com.sk89q.commandbook.CommandBook.inst;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

public class WeaponSysImpl extends AbstractItemFeatureImpl {

    private static Queue<EntityDamageByEntityEvent> attackQueue = new LinkedList<>();

    private static EDBEExtractor<Player, LivingEntity, Projectile> specExtractor = new EDBEExtractor<>(
            Player.class,
            LivingEntity.class,
            Projectile.class
    );

    private Map<CustomItems, SpecWeaponImpl> rangedWeapons = new HashMap<>();
    private Map<CustomItems, SpecWeaponImpl> meleeWeapons = new HashMap<>();

    public void addRanged(CustomItems item, SpecWeaponImpl weapon) {
        rangedWeapons.put(item, weapon);
    }

    public void addMelee(CustomItems item, SpecWeaponImpl weapon) {
        meleeWeapons.put(item, weapon);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void specAttack(EntityDamageByEntityEvent event) {

        // Handle cancellation here so that we don't end up with a memory leak from the queue
        if (attackQueue.poll() != null || event.isCancelled()) return;

        CombatantPair<Player, LivingEntity, Projectile> result = specExtractor.extractFrom(event);

        if (result == null) return;

        ItemStack launcher = null;
        if (result.hasProjectile()) {
            Projectile projectile = result.getProjectile();
            if (projectile.hasMetadata("launcher")) {
                Object test = projectile.getMetadata("launcher").get(0).value();
                if (test instanceof ItemStack) {
                    launcher = (ItemStack) test;
                }
            }

            if (launcher == null) return;
        }

        Player owner = result.getAttacker();
        LivingEntity target = result.getDefender();

        if (target != null && owner != target) {

            CustomItemSession session = getSession(owner);

            Map<CustomItems, SpecWeaponImpl> weaponMap;

            ItemStack weapon;
            SpecType specType;

            SpecialAttack spec = null;

            if (launcher != null) {
                weapon = launcher;
                specType = SpecType.RANGED;
                weaponMap = rangedWeapons;
            } else {
                weapon = owner.getItemInHand();
                specType = SpecType.MELEE;
                weaponMap = meleeWeapons;
            }

            for (Map.Entry<CustomItems, SpecWeaponImpl> entry : weaponMap.entrySet()) {
                if (ItemUtil.isItem(weapon, entry.getKey())) {
                    SpecWeaponImpl impl = entry.getValue();
                    if (impl.activate(owner, target)) {
                        spec = entry.getValue().getSpecial(owner, target);
                    }
                    break;
                }
            }

            if (spec != null && session.canSpec(specType)) {
                final SpecialAttack finalSpec = spec;
                server().getScheduler().runTaskLater(inst(), () -> {
                    if (!target.isDead()) {
                        SpecialAttackEvent specEvent = callSpec(owner, weapon, specType, finalSpec);

                        if (!specEvent.isCancelled()) {
                            session.updateSpec(specType, specEvent.getSpec().getCoolDown());
                            specEvent.getSpec().activate();
                        }
                    }
                }, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void damageModifier(EntityDamageByEntityEvent event) {

        if (DamageUtil.remove(event.getDamager(), event.getEntity())) {
            attackQueue.add(event);
            return;
        }

        CombatantPair<Player, LivingEntity, Projectile> result = specExtractor.extractFrom(event);

        if (result == null) return;

        ItemStack launcher = null;
        if (result.hasProjectile()) {
            Projectile projectile = result.getProjectile();
            if (projectile.hasMetadata("launcher")) {
                Object test = projectile.getMetadata("launcher").get(0).value();
                if (test instanceof ItemStack) {
                    launcher = (ItemStack) test;
                }
            }

            if (launcher == null) return;
        }

        Player owner = result.getAttacker();

        double modifier = 1;

        ItemStack targetItem = launcher;

        if (targetItem == null) {
            targetItem = owner.getItemInHand();
        }

        Map<String, String> map = ItemUtil.getItemTags(targetItem);

        if (map != null) {
            String modifierString = map.get(ChatColor.RED + "Damage Modifier");
            if (modifierString != null) {
                try {
                    modifier = Double.parseDouble(modifierString);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        event.setDamage(event.getDamage() * modifier);
    }
}
