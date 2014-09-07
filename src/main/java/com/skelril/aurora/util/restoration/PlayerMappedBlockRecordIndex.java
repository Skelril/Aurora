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

package com.skelril.aurora.util.restoration;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMappedBlockRecordIndex extends BlockRecordIndex implements Serializable {

    private Map<String, List<BlockRecord>> recordMap = new ConcurrentHashMap<>();

    public void addItem(String player, BlockRecord record) {

        if (!recordMap.containsKey(player)) {
            recordMap.put(player, new Vector<>());
        }
        recordMap.get(player).add(record);
    }

    @Override
    public void revertByTime(long time) {

        Iterator<List<BlockRecord>> primeIt = recordMap.values().iterator();
        List<BlockRecord> activeRecordList;
        while (primeIt.hasNext()) {
            activeRecordList = primeIt.next();

            Iterator<BlockRecord> it = activeRecordList.iterator();
            BlockRecord activeRecord;
            while (it.hasNext()) {
                activeRecord = it.next();
                if (System.currentTimeMillis() - activeRecord.getTime() >= time) {
                    activeRecord.revert();
                    it.remove();
                }
            }

            if (activeRecordList.isEmpty()) {
                primeIt.remove();
            }
        }
    }

    public boolean hasRecordForPlayer(String player) {

        return recordMap.containsKey(player);
    }

    public void revertByPlayer(String player) {

        if (!hasRecordForPlayer(player)) return;

        List<BlockRecord> activeRecordList = recordMap.get(player);

        if (activeRecordList.isEmpty()) {
            recordMap.remove(player);
            return;
        }

        Iterator<BlockRecord> it = activeRecordList.iterator();
        BlockRecord activeRecord;
        while (it.hasNext()) {
            activeRecord = it.next();
            activeRecord.revert();
            it.remove();
        }

        recordMap.remove(player);
    }

    @Override
    public void revertAll() {

        Iterator<List<BlockRecord>> primeIt = recordMap.values().iterator();
        List<BlockRecord> activeRecordList;
        while (primeIt.hasNext()) {
            activeRecordList = primeIt.next();

            Iterator<BlockRecord> it = activeRecordList.iterator();
            BlockRecord activeRecord;
            while (it.hasNext()) {
                activeRecord = it.next();
                activeRecord.revert();
                it.remove();
            }

            if (activeRecordList.isEmpty()) {
                primeIt.remove();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlayerMappedBlockRecordIndex && recordMap.equals(((PlayerMappedBlockRecordIndex) o).recordMap);
    }

    @Override
    public int size() {

        return recordMap.size();
    }
}
