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

package com.skelril.aurora;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.session.PersistentSession;
import com.sk89q.commandbook.session.SessionComponent;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.Depend;
import com.zachsthings.libcomponents.InjectComponent;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: Wyatt Childers
 * Date: 9/27/13
 */
@ComponentInformation(friendlyName = "Chat Manager", desc = "Manages chat")
@Depend(components = {SessionComponent.class})
public class ChatManagerComponent extends BukkitComponent implements Listener {

    private final CommandBook inst = CommandBook.inst();
    private final Logger log = CommandBook.logger();
    private final Server server = CommandBook.server();

    @InjectComponent
    SessionComponent sessions;

    @Override
    public void enable() {

        //noinspection AccessStaticViaInstance
        //inst.registerEvents(this);
    }

    public String getChannel(Player player) {

        return sessions.getSession(ChatManagerSession.class, player).getCurrentChannel();
    }

    public boolean isEavesdropping(Player player) {

        return sessions.getSession(ChatManagerSession.class, player).canSeeOtherChannels();
    }

    public List<String> getIgnoreList(Player player) {

        return sessions.getSession(ChatManagerSession.class, player).getIgnoreList();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {

        String senderName = event.getPlayer().getName();
        String channel = getChannel(event.getPlayer());

        Iterator<Player> it = event.getRecipients().iterator();

        while (it.hasNext()) {

            Player next = it.next();

            // Remove if channels don't match & the player is not listening to other channels
            if (!getChannel(next).equals(channel) && !isEavesdropping(next)) {
                it.remove();
                continue;
            }

            // Remove if this player is being ignored
            if (getIgnoreList(next).contains(senderName.toLowerCase())) {
                it.remove();
                continue;
            }
        }
    }

    private class ChatManagerSession extends PersistentSession {

        // Perssistant
        private boolean showOtherChannels = true;
        private String currentChannel = "Main";

        // Per-Session
        private List<String> ignoreList = new ArrayList<>();

        protected ChatManagerSession() {
            super(-1);
        }

        public boolean canSeeOtherChannels() {

            return showOtherChannels;
        }

        public void allowOtherChannelSight(boolean showOtherChannels) {

            this.showOtherChannels = showOtherChannels;
        }

        public String getCurrentChannel() {

            return currentChannel;
        }

        public void setCurrentChannel(String currentChannel) {

            this.currentChannel = currentChannel;
        }

        public void addToIgnoreList(String name) {

            ignoreList.add(0, name.toLowerCase());
        }

        public void removeFromIgnoreList(String name) {

            ignoreList.remove(name.toLowerCase());
        }

        public List<String> getIgnoreList() {

            return Collections.unmodifiableList(ignoreList);
        }
    }
}
