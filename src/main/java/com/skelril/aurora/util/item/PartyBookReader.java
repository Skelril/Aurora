/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.google.common.collect.Lists;
import com.skelril.aurora.items.custom.CustomItem;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.shard.ShardOwnerTag;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.shard.ShardTypeTag;
import net.minecraft.util.com.google.common.collect.Sets;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartyBookReader {

    private ShardType shard;
    private String owner;
    private Set<String> players = new HashSet<>();

    public PartyBookReader(ShardType shard, String owner) {
        this.shard = shard;
        this.owner = owner;
    }

    public PartyBookReader(ShardType shard, String owner, Set<String> players) {
        this(shard, owner);
        this.players = players;
    }

    public static PartyBookReader getFrom(ItemStack itemStack) {
        if (!ItemUtil.isItem(itemStack, CustomItems.PARTY_BOOK)) {
            return null;
        }

        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta instanceof BookMeta) {
            BookMeta meta = (BookMeta) iMeta;
            Map<String, String> map = ItemUtil.getItemTags(itemStack);

            if (map != null) {
                String ownerTag = map.get(ShardOwnerTag.getTypeColor() + ShardOwnerTag.getTypeKey());
                String shardTag = map.get(ShardTypeTag.getTypeColor() + ShardTypeTag.getTypeKey());
                if (ownerTag == null || shardTag == null) return null;
                ShardType type = ShardType.matchFrom(shardTag);
                if (type == null) return null;
                return new PartyBookReader(type, ownerTag, Sets.newHashSet(meta.getPages()));
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

    public ItemStack build() {
        CustomItem partyBook = CustomItemCenter.get(CustomItems.PARTY_BOOK);
        partyBook.addTag(new ShardTypeTag(shard));
        partyBook.addTag(new ShardOwnerTag(owner));
        ItemStack book = partyBook.build();
        BookMeta bMeta = (BookMeta) book.getItemMeta();
        bMeta.setPages(Lists.newArrayList(players));
        book.setItemMeta(bMeta);
        return book;
    }
}
