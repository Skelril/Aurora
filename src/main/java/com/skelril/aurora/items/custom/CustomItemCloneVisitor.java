/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.custom;

public class CustomItemCloneVisitor implements CustomItemVisitor {

    private CustomItem cloned = null;

    public void visit(CustomEquipment item) {
        cloned = new CustomEquipment(item);
    }

    public void visit(CustomItem item) {
        cloned = new CustomItem(item);
    }

    public void visit(CustomPotion item) {
        cloned = new CustomPotion(item);
    }

    public void visit(CustomWeapon item) {
        cloned = new CustomWeapon(item);
    }

    public CustomItem out() {
        return cloned;
    }
}
