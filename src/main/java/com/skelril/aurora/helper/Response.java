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

package com.skelril.aurora.helper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Response {

    private final Pattern pattern;
    private final List<String> response;

    public Response(Pattern pattern, List<String> response) {
        this.pattern = pattern;
        this.response = response;
    }

    public String getPattern() {
        return pattern.pattern();
    }

    public List<String> getResponse() {
        return Collections.unmodifiableList(response);
    }

    public boolean accept(Player player, String string) {
        if (!pattern.matcher(string).matches()) return false;

        Bukkit.broadcastMessage(ChatColor.YELLOW + "[Auto Reply] @" + player.getName());
        response.forEach(msg -> {
            String finalMessage = msg
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%world%", player.getWorld().getName());
            Bukkit.broadcastMessage("   " + ChatColor.YELLOW + finalMessage);
        });
        return true;
    }
}
