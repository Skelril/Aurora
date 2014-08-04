/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.items.custom;

public interface CustomItemVisitor {
    public void visit(CustomEquipment item);
    public void visit(CustomItem item);
    public void visit(CustomPotion item);
    public void visit(CustomWeapon item);
}
