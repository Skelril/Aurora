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

package com.skelril.aurora.util;

import com.sk89q.commandbook.util.InputUtil;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class ChatUtil {

    public enum MessageType {
        NOTICE(ChatColor.YELLOW),
        ERROR(ChatColor.RED),
        WARNING(ChatColor.RED);

        private final ChatColor color;
        MessageType(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }

    public static void message(CommandSender sender, MessageType type, String message) {
        if (sender == null) return;
        sender.sendMessage(type.getColor() + message);
    }

    public static void message(Collection<? extends CommandSender> targets, MessageType type, String message) {
        for (CommandSender target : targets) {
            message(target, type, message);
        }
    }

    public static void sendDebug(Object o) {
        Bukkit.broadcast(
                ChatColor.BLACK + "[" + ChatColor.DARK_RED + "DEBUG" + ChatColor.BLACK + "] "
                        + ChatColor.GRAY + String.valueOf(o),
                "aurora.debug");
    }

    public static void send(String playerName, String notice) {
        try {
            send(InputUtil.PlayerParser.matchPlayerExactly(null, playerName), notice);
        } catch (CommandException ignored) {
        }
    }

    public static void send(CommandSender sender, String notice) {
        message(sender, MessageType.NOTICE, notice);
    }

    public static void send(Collection<? extends CommandSender> senders, String notice) {
        message(senders, MessageType.NOTICE, notice);
    }

    public static void send(CommandSender sender, ChatColor chatColor, String notice) {
        sender.sendMessage(chatColor + notice.replace("%p%", chatColor.toString()));
    }

    public static void send(Collection<? extends CommandSender> senders, ChatColor chatColor, String notice) {
        senders.forEach(s -> send(s, chatColor, notice));
    }

    public static void sendError(CommandSender sender, String error) {
        message(sender, MessageType.ERROR, error);
    }

    public static void sendError(Collection<? extends CommandSender> senders, String error) {
        message(senders, MessageType.ERROR, error);
    }

    public static void sendWarning(CommandSender sender, String warning) {
        message(sender, MessageType.WARNING, warning);
    }

    public static void sendWarning(Collection<? extends CommandSender> senders, String warning) {
        message(senders, MessageType.WARNING, warning);
    }

    public static String makeCountString(int value, String currencyName) {

        return makeCountString(ChatColor.YELLOW, String.valueOf(value), currencyName);
    }

    public static String makeCountString(double value, String currencyName) {

        return makeCountString(ChatColor.YELLOW, String.valueOf(value), currencyName);
    }

    public static String makeCountString(String value, String currencyName) {

        return makeCountString(ChatColor.YELLOW, value, currencyName);
    }

    public static String makeCountString(ChatColor color, int value, String currencyName) {

        return ChatColor.WHITE + String.valueOf(value) + color + currencyName;
    }

    public static String makeCountString(ChatColor color, double value, String currencyName) {

        return ChatColor.WHITE + String.valueOf(value) + color + currencyName;
    }

    public static String makeCountString(ChatColor color, String value, String currencyName) {

        return ChatColor.WHITE + value + color + currencyName;
    }

    public static char loonyCharacter() {

        switch (ChanceUtil.getRandom(7)) {
            case 7:
                return '?';
            case 6:
                return '&';
            case 5:
                return '!';
            case 4:
                return '#';
            case 3:
                return '@';
            case 2:
                return '%';
            default:
                return '$';
        }
    }

    public static ChatColor loonyColor() {

        switch (ChanceUtil.getRandom(6)) {
            case 6:
                return ChatColor.RED;
            case 5:
                return ChatColor.GREEN;
            case 4:
                return ChatColor.BLUE;
            case 3:
                return ChatColor.AQUA;
            case 2:
                return ChatColor.YELLOW;
            case 1:
                return ChatColor.DARK_PURPLE;
            default:
                return ChatColor.WHITE;
        }
    }

    public static String runeizeString(String string) {

        StringBuilder out = new StringBuilder();

        char[] chars = string.toCharArray();
        boolean nextAllowed = true;

        for (int i = 0; i < chars.length; i++) {

            if (!Character.isAlphabetic(chars[i])) {
                nextAllowed = true;
                continue;
            }

            if ((i == 0 || Character.isSpaceChar(chars[i - 1])) && chars[i] == 'X') {
                nextAllowed = true;
                continue;
            }

            if (!nextAllowed) {
                chars[i] = Character.toLowerCase(chars[i]);
            }
            nextAllowed = false;
        }

        for (char character : chars) {
            out.append(character);
        }

        return out.toString();
    }
}
