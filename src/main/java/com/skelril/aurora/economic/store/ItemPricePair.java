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

public class ItemPricePair implements Comparable<ItemPricePair> {

    private String name;
    private double price;
    private boolean disableBuy, disableSell;

    public ItemPricePair(String name, double price, boolean disableBuy, boolean disableSell) {
        this.name = name;
        this.price = price;
        this.disableBuy = disableBuy;
        this.disableSell = disableSell;
    }

    public String getName() {
        return name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public double getSellPrice() {
        double sellPrice = price > 100000 ? price * .92 : price * .80;
        return sellPrice < .01 ? 0 : sellPrice;
    }

    public boolean isEnabled() {
        return isBuyable() || isSellable();
    }

    public boolean isBuyable() {
        return !disableBuy;
    }

    public boolean isSellable() {
        return !disableSell;
    }

    @Override
    public int compareTo(ItemPricePair record) {
        if (record == null) return -1;
        if (this.getPrice() == record.getPrice()) {
            int c = String.CASE_INSENSITIVE_ORDER.compare(this.getName(), record.getName());
            return c == 0 ? 1 : c;
        }
        return this.getPrice() > record.getPrice() ? 1 : -1;
    }
}
