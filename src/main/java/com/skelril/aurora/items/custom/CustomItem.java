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

package com.skelril.aurora.items.custom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomItem {
    private final CustomItems item;
    private final ItemStack base;
    private List<Tag> tags = new ArrayList<>();
    private List<String> lore = new ArrayList<>();
    private List<Enchant> enchants = new ArrayList<>();

    private List<String> useDocs = new ArrayList<>();
    private List<ItemSource> sources = new ArrayList<>();

    public CustomItem(CustomItems item, ItemStack base) {
        this.item = item;
        this.base = base;
    }

    public CustomItem(CustomItems item, Material type) {
        this(item, new ItemStack(type));
    }

    public CustomItem(CustomItem item) {
        this(item.getItem(), item.getBase());
        tags.addAll(item.getTags());
        lore.addAll(item.getLore());
        enchants.addAll(item.getEnchants());
        useDocs.addAll(item.getUseDocs());
        sources.addAll(item.getSources());
    }

    public CustomItems getItem() {
        return item;
    }

    public ItemStack getBase() {
        return base.clone();
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void addTag(ChatColor color, String key, String prop) {
        addTag(new Tag(color, key, prop));
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addLore(String line) {
        lore.add(line);
    }

    public List<String> getLore() {
        return lore;
    }

    public void addEnchant(Enchant enchant) {
        enchants.add(enchant);
    }

    public void addEnchant(Enchantment enchant, int level) {
        addEnchant(new Enchant(enchant, level));
    }

    public List<Enchant> getEnchants() {
        return enchants;
    }

    public void addUse(String use) {
        useDocs.add(use);
    }

    public List<String> getUseDocs() {
        return useDocs;
    }

    public void addSource(ItemSource source) {
        sources.add(source);
    }

    public List<ItemSource> getSources() {
        List<ItemSource> sources = new ArrayList<>();
        for (ItemSource source : this.sources) {
            Collections.addAll(sources, source.getSubSources());
            sources.add(source);
        }
        return sources;
    }

    public void accept(CustomItemVisitor visitor) {
        visitor.visit(this);
    }

    public ItemStack build() {
        ItemStack itemStack = base.clone();
        ItemMeta meta = itemStack.getItemMeta();
        for (Enchant enchant : enchants) {
            meta.addEnchant(enchant.getEnchant(), enchant.getLevel(), true);
        }
        List<String> lore = tags.stream().map(e -> e.getColor() + e.getKey() + ": " + e.getProp()).collect(Collectors.toList());
        lore.addAll(this.lore);
        if (!lore.isEmpty()) meta.setLore(lore);
        meta.setDisplayName(item.toString());
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
