package com.skelril.aurora.items.specialattack.attacks.melee.guild.rogue;

import com.skelril.aurora.items.specialattack.EntityAttack;
import com.skelril.aurora.items.specialattack.attacks.melee.MeleeSpecial;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.timer.IntegratedRunnable;
import com.skelril.aurora.util.timer.TimedRunnable;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by wyatt on 12/26/13.
 */
public class Nightmare extends EntityAttack implements MeleeSpecial {

    private Random r;

    public Nightmare(LivingEntity owner, LivingEntity target) {
        super(owner, target);
        r = new Random(System.currentTimeMillis());
    }

    @Override
    public void activate() {

        inform("You unleash a nightmare upon the plane.");

        final Set<Location> locations = new HashSet<>();

        Location origin = target.getLocation().add(0, 5, 0);

        for (int i = 0; i < 100; i++) {

            double angle = r.nextDouble() * Math.PI * 2;
            double radius = r.nextDouble() * 12;

            Location pt = origin.clone();
            pt.setX(origin.getX() + radius * Math.cos(angle));
            pt.setZ(origin.getZ() + radius * Math.sin(angle));

            locations.add(pt);
        }

        IntegratedRunnable hellFire = new IntegratedRunnable() {
            @Override
            public boolean run(int times) {
                for (Location location : locations) {
                    if (ChanceUtil.getChance(3)) {
                        Snowball snowball = (Snowball) location.getWorld().spawnEntity(location, EntityType.SNOWBALL);
                        snowball.setMetadata("rogue-snowball", new FixedMetadataValue(inst, true));
                        snowball.setMetadata("nightmare", new FixedMetadataValue(inst, true));
                        snowball.setShooter(owner);
                    }
                }
                return true;
            }

            @Override
            public void end() {
                inform("Your nightmare fades away...");
            }
        };

        TimedRunnable runnable = new TimedRunnable(hellFire, 40);
        runnable.setTask(server.getScheduler().runTaskTimer(inst, runnable, 50, 10));
    }
}
