package com.skelril.aurora;

import com.sk89q.commandbook.CommandBook;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.EffectUtil;
import com.skelril.aurora.util.ItemUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Turtle9598
 */
@ComponentInformation(friendlyName = "Custom Items Component", desc = "Custom Items")
public class CustomItemsComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Server server = CommandBook.server();

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
    }

    private ConcurrentHashMap<String, Long> fearSpec = new ConcurrentHashMap<>();

    private boolean canFearSpec(String name) {

        return !fearSpec.containsKey(name) || System.currentTimeMillis() - fearSpec.get(name) >= 3800;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {

        Entity damager = event.getDamager();
        if (damager instanceof Projectile && ((Projectile) damager).getShooter() != null) {
            damager = ((Projectile) damager).getShooter();
        }

        Player owner = damager instanceof Player ? (Player) damager : null;
        LivingEntity target = event.getEntity() instanceof LivingEntity ? (LivingEntity) event.getEntity() : null;

        if (owner != null && target != null) {

            if (canFearSpec(owner.getName())) {
                if (ItemUtil.hasFearSword(owner)) {
                    switch (ChanceUtil.getRandom(5)) {
                        case 1:
                            EffectUtil.Fear.confuse(owner, target);
                            break;
                        case 2:
                            EffectUtil.Fear.fearBlaze(owner, target);
                            break;
                        case 3:
                            EffectUtil.Fear.poison(owner, target);
                            break;
                        case 4:
                            EffectUtil.Fear.weaken(owner, target);
                            break;
                        case 5:
                            EffectUtil.Fear.wrath(owner, target, event.getDamage(), ChanceUtil.getRangedRandom(2, 6));
                            break;
                    }
                    fearSpec.put(owner.getName(), System.currentTimeMillis());
                } else if (ItemUtil.hasFearBow(owner)) {
                    int attack = ChanceUtil.getRandom(4);

                    switch (attack) {
                        case 1:
                            if (EffectUtil.Fear.disarm(owner, target)) {
                                break;
                            }
                        case 2:
                            EffectUtil.Fear.poison(owner, target);
                            break;
                        case 3:
                            EffectUtil.Fear.magicChain(owner, target);
                            break;
                        case 4:
                            event.setDamage(EffectUtil.Fear.fearStrike(owner, target, event.getDamage()));
                            break;
                    }

                    if (attack != 4 && ChanceUtil.getChance(6)) {
                        Location targetLoc = target.getLocation();
                        if (!targetLoc.getWorld().isThundering() && targetLoc.getBlock().getLightFromSky() > 0) {
                            targetLoc.getWorld().strikeLightning(targetLoc);
                        }
                    }
                    fearSpec.put(owner.getName(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onXPPickUp(PlayerExpChangeEvent event) {

        Player player = event.getPlayer();

        if (ItemUtil.hasAncientArmour(player)) {
            ItemStack[] armour = player.getInventory().getArmorContents();
            ItemStack is = armour[ChanceUtil.getRandom(armour.length) - 1];
            int exp = event.getAmount();
            if (exp > is.getDurability()) {
                exp -= is.getDurability();
                is.setDurability((short) 0);
            } else {
                is.setDurability((short) (is.getDurability() - exp));
                exp = 0;
            }
            player.getInventory().setArmorContents(armour);
            event.setAmount(exp);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity damaged = event.getEntity();
        Player player = damaged.getKiller();

        if (player != null) {

            World w = player.getWorld();
            Location pLocation = player.getLocation();

            if (ItemUtil.hasMasterBow(player) && !(damaged instanceof Player) && event.getDrops().size() > 0) {

                for (ItemStack is : event.getDrops()) {
                    if (is != null) w.dropItemNaturally(pLocation, is);
                }
                event.getDrops().clear();
                ChatUtil.sendNotice(player, "The Master Bow releases a bright flash.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        String name = event.getPlayer().getName();
        if (fearSpec.containsKey(name)) fearSpec.remove(name);
    }
}
