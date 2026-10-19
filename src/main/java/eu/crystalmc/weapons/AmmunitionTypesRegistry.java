package eu.crystalmc.weapons;

import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AmmunitionTypesRegistry {
    private static final HashMap<String, ItemStack> items = new HashMap<>();

    private static final Map<String, Integer> priorities = new HashMap<>();

    public static Map<String, ItemStack> getItems() {
        return items;
    }

    public static void registerAmmunitionType(
            String id,
            ItemStack item,
            AmmunitionTypeConfig config
    ) {
        items.put(id, item);
        priorities.put(id, config.getPriority());

        List<String> sortedIds = items.keySet().stream()
                .sorted((a, b) -> Integer.compare(
                        priorities.get(b),
                        priorities.get(a)
                ))
                .toList();

        Map<String, ItemStack> sortedItems = new LinkedHashMap<>();

        for (String sortedId : sortedIds) {
            sortedItems.put(sortedId, items.get(sortedId));
        }

        items.clear();
        items.putAll(sortedItems);
    }

    public static ItemStack getAmmunitionType(String id) {
        if (!items.containsKey(id)) return null;

        return items.get(id).clone();
    }
}