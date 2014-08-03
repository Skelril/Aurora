/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.HashSet;
import java.util.Set;

public class PartyBook {

    private String instance;
    private String owner;
    private Set<String> players = new HashSet<>();

    public PartyBook(String instance, String owner, Set<String> players) {
        this.instance = instance;
        this.owner = owner;
        this.players = players;
    }

    public PartyBook(BookMeta meta) {
        if (!meta.getTitle().startsWith(String.valueOf(ChatColor.BLUE))) {
            throw new IllegalArgumentException();
        }
        instance = ChatColor.stripColor(meta.getTitle());
        owner = meta.getAuthor();
        players.addAll(meta.getPages());
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void addPlayer(String player) {
        players.add(player);
    }

    public boolean hasPlayer(String player) {
        return players.contains(player);
    }

    public void remPlayer(String player) {
        players.remove(player);
    }

    public ItemStack buildBook() {
        ItemStack book = new ItemStack(ItemID.WRITTEN_BOOK);
        BookMeta bMeta = (BookMeta) book.getItemMeta();
        bMeta.setTitle(ChatColor.BLUE + instance);
        bMeta.setAuthor(owner);
        bMeta.setPages(Lists.newArrayList(players));
        book.setItemMeta(bMeta);
        return book;
    }
}
