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

package com.skelril.aurora.helper;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class YAMLResponseList {

    private YAMLProcessor processor;

    public YAMLResponseList(YAMLProcessor processor) {
        this.processor = processor;
    }

    public Map<String, Response> obtainResponses() {
        Map<String, Response> responses = new HashMap<>();
        try {
            processor.load();
            Map<String, YAMLNode> nodes = processor.getNodes("responses");
            for (Map.Entry<String, YAMLNode> entry : nodes.entrySet()) {
                YAMLNode node = entry.getValue();
                Pattern pattern = Pattern.compile(node.getString("regex"));
                List<String> response = node.getStringList("response", new ArrayList<>());
                responses.put(entry.getKey(), new Response(pattern, response));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responses;
    }

    public void saveResponses(Map<String, Response> responses) {
        for (Map.Entry<String, Response> entry : responses.entrySet()) {
            Response response = entry.getValue();
            YAMLNode node = processor.addNode("responses." + entry.getKey());
            node.setProperty("regex", response.getPattern());
            node.setProperty("response", response.getResponse());
        }
        processor.save();
    }
}
