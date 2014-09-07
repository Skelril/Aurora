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

package com.skelril.aurora.city.engine.arena.factory;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.skelril.aurora.city.engine.arena.AbstractRegionedArena;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FactoryMech extends AbstractRegionedArena {

    protected Map<Integer, Integer> items = new ConcurrentHashMap<>();
    protected YAMLProcessor processor;
    protected String data;

    public FactoryMech(World world, ProtectedRegion region, YAMLProcessor processor, String data) {
        super(world, region);
        this.processor = processor;
        this.data = data;
    }

    public abstract List<ItemStack> process();

    public void load() {
        try {
            processor.load();
            Map<String, YAMLNode> nodes = processor.getNodes(data);
            if (nodes != null) {
                for (Map.Entry<String, YAMLNode> entry : nodes.entrySet()) {
                    YAMLNode node = entry.getValue();
                    items.put(Integer.parseInt(entry.getKey()), node.getInt("amt"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        processor.removeProperty(data);
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            YAMLNode node = processor.addNode(data + '.' + entry.getKey());
            node.setProperty("amt", entry.getValue());
        }
        processor.save();
    }
}
