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

import com.sk89q.commandbook.session.PersistentSession;

import java.util.concurrent.TimeUnit;

public class StoreSession extends PersistentSession {

    private long lastPurchT = 0;
    private String lastPurch = "";
    private long lastSaleT = 0;
    private long recentNotice = 0;

    protected StoreSession() {
        super(TimeUnit.MINUTES.toMicros(10));
    }

    public void setLastPurch(String lastPurch) {
        this.lastPurch = lastPurch;
        lastPurchT = System.currentTimeMillis();
    }

    public String getLastPurch() {
        return lastPurch;
    }

    public boolean recentPurch() {
        return System.currentTimeMillis() - lastPurchT < 10000;
    }

    public void updateSale() {
        lastSaleT = System.currentTimeMillis();
    }

    public boolean recentSale() {
        return System.currentTimeMillis() - lastSaleT < 10000;
    }

    public void updateNotice() {
        recentNotice = System.currentTimeMillis();
    }

    public boolean recentNotice() {
        return System.currentTimeMillis() - recentNotice < 35000;
    }
}
