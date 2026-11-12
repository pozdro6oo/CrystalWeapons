package eu.crystalmc.weapons;

import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WeaponsRegistry {
    private static final HashMap<String, ItemStack> items = new HashMap<>();

    public static Map<String, ItemStack> getItems() {
        return items;
    }

    public static void registerWeapon(String id, ItemStack item) {
        items.put(id, item);
    }

    public static ItemStack getWeapon(String id) {
        if (!items.containsKey(id)) return null;

        ItemStack item = items.get(id).clone();

        WeaponUtils.setCooldownGroup(item);

        return item;
    }

}