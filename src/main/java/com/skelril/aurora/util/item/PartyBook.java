/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.blocks.ItemID;
import com.skelril.aurora.shard.ShardType;
import net.minecraft.util.com.google.common.collect.Sets;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PartyBook {

    private ShardType shard;
    private String owner;
    private Set<String> players = new HashSet<>();

    public PartyBook(ShardType shard, String owner) {
        this.shard = shard;
        this.owner = owner;
    }

    public PartyBook(ShardType shard, String owner, Set<String> players) {
        this(shard, owner);
        this.players = players;
    }

    public static PartyBook getPartyBook(ItemStack itemStack) {
        if (itemStack == null || itemStack.getTypeId() != ItemID.WRITTEN_BOOK) {
            return null;
        }

        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta instanceof BookMeta) {
            BookMeta meta = (BookMeta) iMeta;
            ShardType type = null;
            for (ShardType shard : ShardType.values()) {
                if (shard.getColoredName().equals(meta.getTitle())) {
                    type = shard;
                    break;
                }
            }
            if (type == null) return null;
            return new PartyBook(type, meta.getAuthor(), Sets.newHashSet(meta.getPages()));
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
