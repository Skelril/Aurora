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

package com.skelril.aurora.util;

import com.google.common.collect.Lists;
import com.skelril.aurora.util.checker.Checker;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtil {
    /**
     * Obtains a random element from the provided {@link java.util.List}
     */
    public static <T> T getElement(List<T> list) {
        return list.get(ChanceUtil.getRandom(list.size()) - 1);
    }

    /**
     * Obtains a random element from the provided {@link java.util.Collection}
     */
    public static <T> T getElement(Collection<T> collection) {
        return getElement(Lists.newArrayList(collection));
    }

    /**
     * Obtains a random element from the provided array
     */
    public static <T> T getElement(T[] arr) {
        return arr[ChanceUtil.getRandom(arr.length) - 1];
    }

    /**
     * Removes elements where the checker evaluates true
     */
    public static <T> List<T> removalAll(List<T> collection, Checker<?, T> checker) {
        return collection.stream().filter(element -> !checker.evaluate(element)).collect(Collectors.toList());
    }
}
