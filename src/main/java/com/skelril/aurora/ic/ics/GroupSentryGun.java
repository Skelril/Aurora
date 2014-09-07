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

package com.skelril.aurora.ic.ics;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.*;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.SearchArea;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class GroupSentryGun extends AbstractSelfTriggeredIC {

    private static final CommandBook inst = CommandBook.inst();
    private static final Logger log = inst.getLogger();
    private static final Server server = CommandBook.server();

    private SearchArea area;
    private String group;
    private float speed;

    public GroupSentryGun(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        speed = 0.8f;
        String radius = "";

        String[] parts = getSign().getLine(2).split(":");
        for (int i = 0; i < parts.length; i++) {
            switch (i) {
                case 0:
                    try {
                        speed = Float.parseFloat(parts[i]);
                    } catch (Exception ignored) {
                    }
                    break;
                case 1:
                    radius = parts[i];
                    break;
            }
        }

        area = SearchArea.createArea(getBackBlock(), radius.isEmpty() ? "10" : radius);
        group = getSign().getLine(3);
    }

    @Override
    public String getTitle() {

        return "Group Sentry";
    }

    @Override
    public String getSignTitle() {

        return "GROUP SENTRY";
    }

    @Override
    public void trigger(ChipState chip) {

        shoot();
    }

    @Override
    public void think(ChipState chip) {

        if (((Factory) getFactory()).inverted ? chip.getInput(0) : !chip.getInput(0)) {
            trigger(chip);
        }
    }

    public void shoot() {

        for (Entity ent : area.getEntitiesInArea()) {
            if (!(ent instanceof LivingEntity)) continue;

            if (ent instanceof Player && !group.isEmpty()) {
                if (inst.getPermissionsResolver().inGroup((OfflinePlayer) ent, group)) continue;
            }

            Location k = LocationUtil.getCenterOfBlock(LocationUtil.getNextFreeSpace(getBackBlock(), BlockFace.UP));
            Arrow ar = area.getWorld().spawnArrow(k, ent.getLocation().add(0, ((LivingEntity) ent).getEyeHeight(), 0).subtract(k.clone().add(0.5, 0.5, 0.5)).toVector().normalize(), speed, 0);
            if (!((LivingEntity) ent).hasLineOfSight(ar)) {
                ar.remove();
                continue;
            }
            break;
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC, ConfigurableIC {

        public boolean inverted = false;

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new GroupSentryGun(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Shoots nearby mobs with arrows.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[]{"Speed[:radius]", "GroupName"};
        }

        @Override
        public void addConfiguration(YAMLProcessor config, String path) {

            inverted = config.getBoolean(path + "inverted", false);
        }
    }
}