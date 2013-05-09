package com.skelril.aurora.util;

import com.sk89q.commandbook.CommandBook;
import com.skelril.aurora.prayer.PrayerFX.HulkFX;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Author: Turtle9598
 */
public class EffectUtil {

    public static class Fear {

        public static void fearBlaze(Player owner, LivingEntity target) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, target.getHealth() * 20, 1));
            target.setFireTicks(owner.getHealth() * 20);
        }

        public static void poison(Player owner, LivingEntity target) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, target.getHealth() * 10, 3));
        }

        public static void weaken(Player owner, LivingEntity target) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, owner.getHealth() * 18, 2));
        }

        public static void confuse(Player owner, LivingEntity target) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, owner.getHealth() * 18, 1));
        }

        public static void wrath(final Player owner, final LivingEntity target, int x) {

            for (int i = 0; i < x; i++) {
                CommandBook.server().getScheduler().scheduleSyncDelayedTask(CommandBook.inst(), new Runnable() {
                    @Override
                    public void run() {

                        if (!target.isValid()) target.damage(5, owner);
                    }
                }, (1 + i) * 20);
            }
        }
    }

    public static class Master {

        public static void blind(Player owner, LivingEntity target) {

            if (target instanceof Player) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 1));
                ChatUtil.sendNotice(owner, "Your weapon blinds your victim.");
            } else {
                healingLight(owner, target);
            }
        }

        public static void healingLight(Player owner, LivingEntity target) {

            owner.setHealth(Math.min(owner.getMaxHealth(), owner.getHealth() + 5));
            for (int i = 0; i < 4; i++) {
                target.getWorld().playEffect(target.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
            }

            target.damage(20);
            ChatUtil.sendNotice(owner, "Your weapon glows dimly.");
        }

        public static void ultimateStrength(Player owner) {

            new HulkFX().add(owner);
            ChatUtil.sendNotice(owner, "You gain a new sense of true power.");
        }

        public static void doomBlade(Player owner, Collection<LivingEntity> entities) {

            ChatUtil.sendNotice(owner, "The Master Sword releases a huge burst of energy.");

            int dmgTotal = 0;
            for (LivingEntity e : entities) {
                for (int i = 0; i < 20; i++) e.getWorld().playEffect(e.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
                e.damage(200);
                dmgTotal += 200;
            }
            ChatUtil.sendNotice(owner, "Your sword dishes out an incredible " + dmgTotal + " damage!");
        }
    }

    public static class Ancient {

        public static void powerBurst(Player player, int attackDamage) {

            ChatUtil.sendNotice(player, "Your armour releases a burst of energy.");
            ChatUtil.sendNotice(player, "You are healed by an ancient force.");

            player.setHealth(Math.min(player.getHealth() + attackDamage, player.getMaxHealth()));

            for (Entity e : player.getNearbyEntities(8, 8, 8)) {
                if (e.isValid() && e instanceof LivingEntity) {
                    if (e instanceof Player) {
                        ((Player) e).setHealth(Math.min(((Player) e).getHealth() + attackDamage,
                                ((Player) e).getMaxHealth()));
                        ChatUtil.sendNotice((Player) e, "You are healed by an ancient force.");
                    } else if (EnvironmentUtil.isHostileEntity(e)) {
                        e.setVelocity(new Vector(
                                Math.random() * 1.7 - 1.5,
                                Math.random() * 4,
                                Math.random() * 1.7 - 1.5
                        ));
                        e.setFireTicks(ChanceUtil.getRandom(20 * 60));
                    }
                }
            }
        }
    }
}
