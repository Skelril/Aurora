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

public class BaseItem {

    private int id;
    private int data;

    /**
     * Construct the object.
     *
     * @param id ID of the item
     */
    public BaseItem(int id) {

        this.id = id;
        this.data = 0;
    }

    /**
     * Construct the object.
     *
     * @param id   ID of the item
     * @param data data value of the item
     */
    public BaseItem(int id, int data) {

        this.id = id;
        this.data = data;
    }

    /**
     * Get the type of item.
     *
     * @return the id
     */
    public int getType() {

        return id;
    }

    /**
     * Get the type of item.
     *
     * @param id the id to set
     */
    public void setType(int id) {

        this.id = id;
    }

    /**
     * Gets the raw data value
     *
     * @return the raw data
     */
    public int getRawData() {

        return data;
    }

    /**
     * Get the data value.
     *
     * @return the data
     */
    public int getData() {

        return data < 0 ? 0 : data;
    }

    /**
     * Set the data value.
     *
     * @param data the damage to set
     */
    public void setData(int data) {

        this.data = data;
    }

    /**
     * Checks whether the type ID and data value are equal.
     */
    public boolean equalsExact(BaseItem o) {

        return o != null && getType() == o.getType() && getData() == o.getData();
    }

    /**
     * Checks if the type is the same, and if data is the same if only data != -1.
     *
     * @param o other block
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {

        return o instanceof BaseItem && (getType() == ((BaseItem) o).getType())
                && (getRawData() == ((BaseItem) o).getRawData() || getRawData() == -1 || ((BaseItem) o).getRawData() == -1);
    }
}