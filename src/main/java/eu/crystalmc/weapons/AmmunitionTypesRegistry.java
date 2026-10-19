package eu.crystalmc.weapons;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AmmunitionTypesRegistry {
    private static final HashMap<String, ItemStack> items = new HashMap<>();

    public static Map<String, ItemStack> getItems() {
        return items;
    }

    public static void registerAmmunitionType(String id, ItemStack item) {
        items.put(id, item);
    }

    public static ItemStack getAmmunitionType(String id) {
        if (!items.containsKey(id)) return null;

        return items.get(id).clone();
    }
}