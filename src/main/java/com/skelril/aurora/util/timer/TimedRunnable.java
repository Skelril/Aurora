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

package com.skelril.aurora.util.timer;

import org.bukkit.scheduler.BukkitTask;

public class TimedRunnable implements Runnable {

    private BukkitTask task;
    private IntegratedRunnable action;

    private int times;
    private boolean done = false;

    public TimedRunnable(IntegratedRunnable action, int times) {
        this.action = action;
        this.times = times;
    }

    public boolean isComplete() {
        return done;
    }

    public void addTime(int times) {
        this.times += times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getTimes() {
        return times;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (times > 0) {
            if (action.run(times)) {
                times--;
            }
        } else {
            cancel(true);
        }
    }

    public void cancel() {
        cancel(false);
    }

    public void cancel(boolean withEnd) {

        if (done) return; // Task is done

        if (withEnd) action.end();
        task.cancel();
        done = true;
    }
}
