package eu.crystalmc.weapons;

import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;

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

        ItemMeta meta = item.getItemMeta();
        UseCooldownComponent component = meta.getUseCooldown();


        component.setCooldownGroup(WeaponUtils.createCooldownGroup());


        meta.setUseCooldown(component);
        item.setItemMeta(meta);

        return item;
    }
}