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

package com.skelril.aurora.data;

import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import com.zachsthings.libcomponents.config.ConfigurationBase;
import com.zachsthings.libcomponents.config.Setting;

@ComponentInformation(friendlyName = "Database", desc = "MySQL database handler.")
public class DataBaseComponent extends BukkitComponent {

    private LocalConfiguration config;

    @Override
    public void enable() {
        config = configure(new LocalConfiguration());
        updateHandle();
    }

    @Override
    public void reload() {
        super.reload();
        configure(config);
        updateHandle();
    }

    public class LocalConfiguration extends ConfigurationBase {
        @Setting("database")
        public String database = "";
        @Setting("username")
        public String username = "";
        @Setting("password")
        public String password = "";
    }

    public void updateHandle() {
        MySQLHandle.setDatabase(config.database);
        MySQLHandle.setUsername(config.username);
        MySQLHandle.setPassword(config.password);
    }
}
