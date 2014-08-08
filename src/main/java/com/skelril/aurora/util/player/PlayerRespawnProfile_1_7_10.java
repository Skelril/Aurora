/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
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
    private SerializableItemStack[] armourContents = null;
    private SerializableItemStack[] inventoryContents = null;
    // For Usage
    private transient ItemStack[] cacheArmourContents = null;
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

    public PlayerRespawnProfile_1_7_10(UUID owner, ItemStack[] armour,
                                       ItemStack[] inv, int level, float exp, float droppedExp,
                                       KeepAction armorAction, KeepAction invAction,
                                       KeepAction levelAction, KeepAction experienceAction) {
        this.owner = owner;
        setArmourContents(armour);
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

    public ItemStack[] getArmourContents() {
        if (cacheArmourContents == null) {
            cacheArmourContents = ItemUtil.unSerialize(armourContents);
        }
        return ItemUtil.clone(cacheArmourContents);
    }

    public void setArmourContents(ItemStack[] armourContents) {
        Validate.notNull(armourContents);
        this.cacheArmourContents = null;
        this.armourContents = ItemUtil.serialize(armourContents);
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
