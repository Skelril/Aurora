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

package com.skelril.aurora.modifier;

import com.sk89q.commandbook.util.ChatUtil;
import com.sk89q.commandbook.util.InputUtil;
import com.sk89q.minecraft.util.commands.*;
import com.sk89q.util.yaml.YAMLFormat;
import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import com.skelril.aurora.util.CollectionUtil;
import com.zachsthings.libcomponents.ComponentInformation;
import com.zachsthings.libcomponents.bukkit.BukkitComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sk89q.commandbook.CommandBook.*;
import static com.zachsthings.libcomponents.bukkit.BasePlugin.server;

@ComponentInformation(friendlyName = "Modifiers", desc = "Commands and saving for the Modifier system.")
public class ModifierComponent extends BukkitComponent implements Listener {

    private static ModifierManager modifierManager = new ModifierManager();

    private YAMLProcessor processor;

    public ModifierComponent() {
        this(new File(inst().getDataFolder() + "/modifiers.yml"));
    }

    public ModifierComponent(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }
            if (!file.exists()) {
                    file.createNewFile();

            }
        } catch (IOException e) {
            logger().warning("Failed to create the modifiers file!");
            e.printStackTrace();
        }
        processor = new YAMLProcessor(
                file,
                false,
                YAMLFormat.EXTENDED
        );
    }

    @Override
    public void enable() {
        load();
        registerCommands(Commands.class);

        registerEvents(this);
    }

    @Override
    public void disable() {
        save();
    }

    public static ModifierManager getModifierManager() {
        return modifierManager;
    }

    public class Commands {
        @Command(aliases = {"modifiers"}, desc = "Modifier Commands")
        @NestedCommand({ModifierCommands.class})
        public void modCommands(CommandContext args, CommandSender sender) throws CommandException {

        }
    }

    public class ModifierCommands {
        @Command(aliases = {"extend"}, desc = "Extend the duration of a modifier",
                usage = "<modifier> <time> [player]",
                flags = "", min = 2, max = 3)
        @CommandPermissions("aurora.modifiers.extend")
        public void extendCmd(CommandContext args, CommandSender sender) throws CommandException {
            ModifierType modifierType;
            try {
                String modifierStr = args.getString(0);
                if (modifierStr.equalsIgnoreCase("rand")) {
                    modifierType = CollectionUtil.getElement(ModifierType.values());
                } else {
                    modifierType = ModifierType.valueOf(modifierStr);
                }
            } catch (IllegalArgumentException ex) {
                throw new CommandException("No modifier by that name could be found!");
            }
            long amount = InputUtil.TimeParser.matchDate(args.getString(1));

            boolean wasOn = modifierManager.isActive(modifierType);
            modifierManager.extend(modifierType, amount);
            save();

            String friendlyTime = ChatUtil.getFriendlyTime(System.currentTimeMillis() + modifierManager.status(modifierType));
            String change = wasOn ? " extended" : " enabled";
            String by = args.argsLength() > 2 ? " by " + args.getString(2) : "";
            Bukkit.broadcastMessage(ChatColor.GOLD + modifierType.fname() + change + by + " till " + friendlyTime + "!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        List<String> messages = new ArrayList<>();
        for (ModifierType type : ModifierType.values()) {
            long dur = modifierManager.status(type);
            if (dur == 0) continue;
            String friendlyTime = ChatUtil.getFriendlyTime(System.currentTimeMillis() + dur);
            messages.add(" - " + type.fname() + " till " + friendlyTime);
        }
        if (messages.isEmpty()) return;

        Collections.sort(messages, String.CASE_INSENSITIVE_ORDER);
        messages.add(0, "\n\nThe following donation perks are enabled:");

        Player player = event.getPlayer();
        server().getScheduler().runTaskLater(inst(), () -> {
            for (String message : messages) {
                com.skelril.aurora.util.ChatUtil.send(player, ChatColor.GOLD, message);
            }
        }, 20);
    }

    public void load() {
        try {
            processor.load();
            YAMLNode node = processor.getNode("modifiers");
            if (node != null) {
                for (ModifierType type : ModifierType.values()) {
                    modifierManager.set(type, Long.parseLong(node.getString(type.name())));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        YAMLNode node = processor.getNode("modifiers");
        if (node == null) {
            node = processor.addNode("modifiers");
        }
        for (ModifierType type : ModifierType.values()) {
            node.setProperty(type.name(), String.valueOf(modifierManager.get(type)));
        }
        processor.save();
    }
}
