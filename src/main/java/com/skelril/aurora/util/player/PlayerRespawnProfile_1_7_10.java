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

package com.skelril.aurora.util.player;

import com.skelril.aurora.util.KeepAction;
import com.skelril.aurora.util.item.ItemUtil;
import com.skelril.aurora.util.item.itemstack.SerializableItemStack;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;

public class PlayerRespawnProfile_1_7_10 implements Serializable {

    private UUID owner;

    // For Serialization
    private SerializableItemStack[] armorContents = null;
    private SerializableItemStack[] inventoryContents = null;
    // For Usage
    private transient ItemStack[] cacheArmorContents = null;
    private transient ItemStack[] cacheInventoryContents = null;

    private int level = 0;
    private float experience = 0;

    private float droppedExp;

    private KeepAction armorAction;
    private KeepAction invAction;
    private KeepAction levelAction;
    private KeepAction experienceAction;

    public PlayerRespawnProfile_1_7_10(Player player, float droppedExp,
                                       KeepAction armorAction, KeepAction invAction,
                                       KeepAction levelAction, KeepAction experienceAction) {
        this(
                player.getUniqueId(),
                player.getInventory().getArmorContents(),
                player.getInventory().getContents(), player.getLevel(),
                player.getExp(),
                droppedExp,
                armorAction,
                invAction,
                levelAction,
                experienceAction
        );
    }

    public PlayerRespawnProfile_1_7_10(UUID owner, ItemStack[] armor,
                                       ItemStack[] inv, int level, float exp, float droppedExp,
                                       KeepAction armorAction, KeepAction invAction,
                                       KeepAction levelAction, KeepAction experienceAction) {
        this.owner = owner;
        setArmorContents(armor);
        setInventoryContents(inv);
        setLevel(level);
        setExperience(exp);
        setDroppedExp(droppedExp);
        setArmorAction(armorAction);
        setInvAction(invAction);
        setLevelAction(levelAction);
        setExperienceAction(experienceAction);
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack[] getArmorContents() {
        if (cacheArmorContents == null) {
            cacheArmorContents = ItemUtil.unSerialize(armorContents);
        }
        return ItemUtil.clone(cacheArmorContents);
    }

    public void setArmorContents(ItemStack[] armorContents) {
        Validate.notNull(armorContents);
        this.cacheArmorContents = null;
        this.armorContents = ItemUtil.serialize(armorContents);
    }

    public ItemStack[] getInventoryContents() {
        if (cacheInventoryContents == null) {
            cacheInventoryContents = ItemUtil.unSerialize(inventoryContents);
        }
        return ItemUtil.clone(cacheInventoryContents);
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        Validate.notNull(inventoryContents);
        this.cacheInventoryContents = null;
        this.inventoryContents = ItemUtil.serialize(inventoryContents);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getExperience() {
        return experience;
    }

    public void setExperience(float experience) {
        this.experience = experience;
    }

    public float getDroppedExp() {
        return droppedExp;
    }

    public void setDroppedExp(float droppedExp) {
        this.droppedExp = droppedExp;
    }

    public KeepAction getArmorAction() {
        return armorAction;
    }

    public void setArmorAction(KeepAction armorAction) {
        this.armorAction = armorAction;
    }

    public KeepAction getInvAction() {
        return invAction;
    }

    public void setInvAction(KeepAction invAction) {
        this.invAction = invAction;
    }

    public KeepAction getLevelAction() {
        return levelAction;
    }

    public void setLevelAction(KeepAction levelAction) {
        Validate.isTrue(levelAction != KeepAction.DROP);
        this.levelAction = levelAction;
    }

    public KeepAction getExperienceAction() {
        return experienceAction;
    }

    public void setExperienceAction(KeepAction experienceAction) {
        Validate.isTrue(experienceAction != KeepAction.DROP);
        this.experienceAction = experienceAction;
    }
}
