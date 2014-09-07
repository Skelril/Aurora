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
