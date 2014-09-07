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

package com.skelril.aurora.anticheat;

import com.sk89q.commandbook.CommandBook;
import com.skelril.Pitfall.bukkit.event.PitfallTriggerEvent;
import com.skelril.aurora.events.PrayerApplicationEvent;
import com.skelril.aurora.events.anticheat.FallBlockerEvent;
import com.skelril.aurora.events.anticheat.RapidBlockBreakEvent;
import com.skelril.aurora.events.anticheat.RapidHitEvent;
import com.skelril.aurora.events.anticheat.ThrowPlayerEvent;
import com.skelril.aurora.events.guild.RogueGrenadeEvent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@ComponentInformation(friendlyName = "Anit-Cheat Compat", desc = "Compatibility layer for Anti-Cheat plugins.")
@Depend(plugins = {"NoCheatPlus"})
public class AntiCheatCompatibilityComponent extends BukkitComponent implements Listener, Runnable {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = inst.getLogger();
    private final Server server = CommandBook.server();

    private LocalConfiguration config;
    private static ConcurrentHashMap<String, ConcurrentHashMap<CheckType, Long>> playerList = new ConcurrentHashMap<>();

    @Override
    public void enable() {
        config = configure(new LocalConfiguration());
        //noinspection AccessStaticViaInstance
        inst.registerEvents(this);
        server.getScheduler().scheduleSyncRepeatingTask(inst, this, 20 * 20, 20 * 5);
    }

    @Override
    public void reload() {
        super.reload();
        configure(config);
    }

    private static class LocalConfiguration extends ConfigurationBase {
        @Setting("removal-delay")
        public int removalDelay = 3;
    }

    @Override
    public void run() {
        for (Map.Entry<String, ConcurrentHashMap<CheckType, Long>> e : playerList.entrySet()) {
            Player player = Bukkit.getPlayerExact(e.getKey());
            if (player == null) {
                playerList.remove(e.getKey());
                continue;
            }

            e.getValue().entrySet().stream().filter(p -> (System.currentTimeMillis() - p.getValue()) / 1000 > config.removalDelay).forEach(p -> {
                unexempt(player, p.getKey());
                e.getValue().remove(p.getKey());
            });
        }
    }

    public static void exempt(Player player, CheckType... checkTypes) {
        for (CheckType checkType : checkTypes) {
            NCPExemptionManager.exemptPermanently(player, checkType);
        }
    }

    public static void unexempt(Player player, CheckType... checkTypes) {
        for (CheckType checkType : checkTypes) {
            NCPExemptionManager.unexempt(player, checkType);
        }
    }

    public static void bypass(Player player, CheckType... checkTypes) {

        ConcurrentHashMap<CheckType, Long> hashMap;
        if (playerList.containsKey(player.getName())) hashMap = playerList.get(player.getName());
        else hashMap = new ConcurrentHashMap<>();

        for (CheckType checkType : checkTypes) {
            if (NCPExemptionManager.isExempted(player, checkType) && !hashMap.containsKey(checkType)) continue;
            hashMap.put(checkType, System.currentTimeMillis());
            exempt(player, checkType);
        }
        playerList.put(player.getName(), hashMap);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        if (playerList.containsKey(player.getName())) {
            for (Map.Entry<CheckType, Long> e : playerList.get(player.getName()).entrySet()) {
                NCPExemptionManager.unexempt(player, e.getKey());
            }
            playerList.remove(player.getName());
        }
    }

    public static final CheckType[] PLAYER_FLY = new CheckType[]{
            CheckType.MOVING_SURVIVALFLY, CheckType.MOVING_CREATIVEFLY,
    };
    public static final CheckType[] PLAYER_THROW = PLAYER_FLY;
    public static final CheckType[] FALL_BLOCKER = new CheckType[]{CheckType.MOVING_NOFALL};
    public static final CheckType[] RAPID_HIT = new CheckType[]{
            CheckType.FIGHT_ANGLE, CheckType.FIGHT_DIRECTION, CheckType.FIGHT_NOSWING,
            CheckType.FIGHT_REACH, CheckType.FIGHT_SPEED
    };
    public static final CheckType[] RAPID_BLOCK_BREAK = new CheckType[]{
            CheckType.BLOCKBREAK
    };
    public static final CheckType[] MULTI_PROJECTILE = new CheckType[]{
            CheckType.BLOCKPLACE_SPEED
    };

    @EventHandler
    public void onPitfallTrigger(PitfallTriggerEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            bypass((Player) entity, CheckType.MOVING_SURVIVALFLY);
        }
    }

    @EventHandler
    public void onPlayerThrow(ThrowPlayerEvent event) {
        bypass(event.getPlayer(), PLAYER_THROW);
    }

    @EventHandler
    public void onFallBlocker(FallBlockerEvent event) {
        bypass(event.getPlayer(), FALL_BLOCKER);
    }

    @EventHandler
    public void onRapidBlockBreak(RapidBlockBreakEvent event) {
        bypass(event.getPlayer(), RAPID_BLOCK_BREAK);
    }

    @EventHandler
    public void onRapidHit(RapidHitEvent event) {
        bypass(event.getPlayer(), RAPID_HIT);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRogueSnowball(RogueGrenadeEvent event) {
        bypass(event.getPlayer(), MULTI_PROJECTILE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrayerApplication(PrayerApplicationEvent event) {
        List<CheckType> checkTypes = new ArrayList<>();
        switch (event.getCause().getEffect().getType()) {
            case TNT:
            case ROCKET:
            case SLAP:
            case DOOM:
                checkTypes.add(CheckType.MOVING);
                checkTypes.add(CheckType.MOVING_CREATIVEFLY);
                checkTypes.add(CheckType.MOVING_SURVIVALFLY);
            case MERLIN:
            case BUTTERFINGERS:
                checkTypes.add(CheckType.INVENTORY_DROP);
                break;
            default:
                return;
        }
        bypass(event.getPlayer(), checkTypes.toArray(new CheckType[checkTypes.size()]));
    }
}
