package com.protegra.sdecdemo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Speakers {

    /**
     * An array of speakers.
     */
    public static final List<Speaker> ITEMS = new ArrayList<Speaker>();

    /**
     * A map of speakers, by ID.
     */
    public static final Map<String, Speaker> ITEM_MAP = new HashMap<String, Speaker>();

    public static void addItem(Speaker item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }
}
