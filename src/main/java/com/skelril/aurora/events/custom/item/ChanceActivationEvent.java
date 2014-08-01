/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.events.custom.item;

import com.sk89q.worldedit.event.Cancellable;
import net.minecraft.util.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class ChanceActivationEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private int chance;
    private final ChanceType type;

    public ChanceActivationEvent(Player who, int chance) {
        this(who, chance, ChanceType.GENERIC);
    }

    public ChanceActivationEvent(Player who, int chance, ChanceType type) {
        super(who);
        setChance(chance);
        this.type = type;
    }

    /**
     * Returns a {@link com.skelril.aurora.events.custom.item.ChanceActivationEvent.ChanceType}
     * representing the general concept this chance is being evaluated for.
     *
     * @return the type of chance
     */
    public ChanceType getType() {
        return type;
    }

    /**
     * Gets the chance divisor. If the chance is 1/50,
     * this will return 50.
     *
     * @return the divisor of the chance
     */
    public int getChance() {
        return chance;
    }

    /**
     * Takes the provided argument, and increases the chance by that
     * amount. For example, if the current chance is 20, and you supply
     * 5, the new chance will be 15.
     *
     * @param amt the amount to subtract from the current chance
     * @return {@link ChanceActivationEvent#getChance()}
     */
    public int increaseChance(int amt) {
        chance = Math.max(1, chance - amt);
        return chance;
    }

    /**
     * Takes the provided argument, and decreases the chance by that
     * amount. For example, if the current chance is 20, and you supply
     * 5, the new chance will be 25.
     *
     * @param amt the amount to subtract from the current chance
     * @return {@link ChanceActivationEvent#getChance()}
     */
    public int decreaseChance(int amt) {
        chance = Math.max(1, chance + amt);
        return chance;
    }

    /**
     * Sets the chance divisor. Supplying 20 for example,
     * will result in a 1/20 chance.
     *
     * @param chance the new divisor
     */
    public void setChance(int chance) {
        Validate.isTrue(chance > 0, "Chance must be greater than 0.");
        this.chance = chance;
    }

    public static enum ChanceType {
        GENERIC,
        WEAPON,
        ARMOR
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
