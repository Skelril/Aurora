/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.shard.ShardType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PartyBook {

    private ShardType shard;
    private String owner;
    private Set<String> players = new HashSet<>();

    public PartyBook(ShardType shard, String owner, Set<String> players) {
        this.shard = shard;
        this.owner = owner;
        this.players = players;
    }

    public PartyBook(BookMeta meta) {
        ShardType shard = getShardFromBook(meta);
        if (shard == null) {
            throw new IllegalArgumentException("Invalid shard book");
        }
        this.shard = shard;
        this.owner = meta.getAuthor();
        this.players.addAll(meta.getPages());
    }

    public static ShardType getShardFromBook(BookMeta meta) {
        for (ShardType shard : ShardType.values()) {
            if (shard.getColoredName().equals(meta.getTitle())) {
                return shard;
            }
        }
        return null;
    }

    public ShardType getShard() {
        return shard;
    }

    public void setShard(ShardType shard) {
        this.shard = shard;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Set<String> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public Set<String> getAllPlayers() {
        HashSet<String> allPlayers = new HashSet<>();
        allPlayers.add(owner);
        allPlayers.addAll(players);
        return Collections.unmodifiableSet(allPlayers);
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
        bMeta.setTitle(shard.getColoredName());
        bMeta.setAuthor(owner);
        bMeta.setPages(Lists.newArrayList(players));
        book.setItemMeta(bMeta);
        return book;
    }
}
