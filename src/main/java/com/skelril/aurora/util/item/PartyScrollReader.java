/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * All Rights Reserved
 */

package com.skelril.aurora.util.item;

import com.skelril.aurora.items.custom.CustomItem;
import com.skelril.aurora.items.custom.CustomItemCenter;
import com.skelril.aurora.items.custom.CustomItems;
import com.skelril.aurora.shard.ShardOwnerTag;
import com.skelril.aurora.shard.ShardType;
import com.skelril.aurora.shard.ShardTypeTag;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PartyScrollReader {

    private ShardType shard;
    private String owner;

    public PartyScrollReader(ShardType shard, String owner) {
        this.shard = shard;
        this.owner = owner;
    }

    public ShardType getShard() {
        return shard;
    }

    public String getOwner() {
        return owner;
    }

    public static PartyScrollReader getPartyScroll(ItemStack itemStack) {
        if (!ItemUtil.isItem(itemStack, CustomItems.PARTY_SCROLL)) {
            return null;
        }

        Map<String, String> map = ItemUtil.getItemTags(itemStack);

        if (map != null) {
            String ownerTag = map.get(ShardOwnerTag.getTypeColor() + ShardOwnerTag.getTypeKey());
            String shardTag = map.get(ShardTypeTag.getTypeColor() + ShardTypeTag.getTypeKey());
            if (ownerTag == null || shardTag == null) return null;
            ShardType type = ShardType.matchFrom(shardTag);
            if (type == null) return null;
            return new PartyScrollReader(type, ownerTag);
        }
        return null;
    }

    public ItemStack build() {
        CustomItem partyScroll = CustomItemCenter.get(CustomItems.PARTY_SCROLL);
        partyScroll.addTag(new ShardTypeTag(shard));
        partyScroll.addTag(new ShardOwnerTag(owner));
        return partyScroll.build();
    }
}
