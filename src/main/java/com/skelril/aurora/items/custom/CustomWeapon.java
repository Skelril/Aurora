/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.custom;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class CustomWeapon extends CustomEquipment {

    private final double damageMod;

    public CustomWeapon(CustomItems item, Material type, double damageMod) {
        super(item, type);
        this.damageMod = damageMod;
        addTag(ChatColor.RED, "Damage Modifier", String.valueOf(damageMod));
    }

    public CustomWeapon(CustomWeapon weapon) {
        super(weapon);
        damageMod = weapon.getDamageMod();
    }

    public double getDamageMod() {
        return damageMod;
    }

    @Override
    public void accept(CustomItemVisitor visitor) {
        visitor.visit(this);
    }
}
