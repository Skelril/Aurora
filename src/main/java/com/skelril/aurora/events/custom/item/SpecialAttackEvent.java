package com.skelril.aurora.events.custom.item;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SpecialAttackEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final LivingEntity target;
    private final Location location;
    private final Specs spec;


    public SpecialAttackEvent(final Player player, final LivingEntity target, final Specs spec) {

        super(player);
        this.target = target;
        this.location = target.getLocation();
        this.spec = spec;
    }

    public SpecialAttackEvent(final Player player, final Location location, final Specs spec) {

        super(player);
        this.target = null;
        this.location = location;
        this.spec = spec;
    }

    public LivingEntity getTarget() {

        return target;
    }

    public Location getLocation() {

        return location;
    }

    public Specs getSpec() {

        return spec;
    }

    public HandlerList getHandlers() {

        return handlers;
    }

    public static HandlerList getHandlerList() {

        return handlers;
    }

    public boolean isCancelled() {

        return cancelled;
    }

    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;
    }

    public static enum Specs {

        BLAZE,
        CURSE,
        WEAKEN,
        CONFUSE,
        SOUL_SMITE,

        DISARM,
        RANGE_CURSE,
        MAGIC_CHAIN,
        FEAR_STRIKE,

        REGEN,
        SPEED,
        LIFE_LEECH,
        BLIND,
        HEALING_LIGHT,
        DOOM_BLADE,

        BAT_ATTACK
    }
}
