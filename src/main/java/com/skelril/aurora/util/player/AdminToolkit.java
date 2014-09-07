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

package com.skelril.aurora.util.player;

import com.skelril.aurora.admin.AdminComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;

public class AdminToolkit {

    private AdminComponent admin;

    public AdminToolkit(AdminComponent admin) {
        this.admin = admin;
    }

    public <T extends Entity> Collection<T> removeAdmin(Collection<T> entities) {
        return entities.stream()
                .filter(e -> !(e instanceof Player && admin.isAdmin((Player) e)))
                .collect(Collectors.toSet());
    }
}
