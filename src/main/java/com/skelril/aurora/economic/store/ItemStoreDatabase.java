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

package com.skelril.aurora.economic.store;

import java.util.List;

public interface ItemStoreDatabase {

    /**
     * Load the item database.
     *
     * @return whether the operation was fully successful
     */
    public boolean load();

    /**
     * Save the database.
     *
     * @return whether the operation was fully successful
     */
    public boolean save();

    /**
     * Add/Set an item
     */
    public void addItem(String playerName, String itemName, double price, boolean disableBuy, boolean disableSell);

    public void removeItem(String playerName, String itemName);

    /**
     * Gets the item that was requested
     *
     * @param name the name of the item
     * @return the ItemPricePair that was requested or null if nothing was found
     */
    public ItemPricePair getItem(String name);

    /**
     * Returns a list of items
     *
     * @param filter the item name must start with this to be returned
     * @return A list of items
     */
    public List<ItemPricePair> getItemList();

    /**
     * Returns a list of items
     *
     * @param filter     the item name must start with this to be returned
     * @param showHidden return items which are database only
     * @return A list of items
     */
    public List<ItemPricePair> getItemList(String filter, boolean showHidden);
}
