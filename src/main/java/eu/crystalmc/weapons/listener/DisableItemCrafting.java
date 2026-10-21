package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.CrystalWeapons;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DisableItemCrafting implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        for (ItemStack itemStack : inventory.getMatrix()) {
            if (isDisabled(itemStack) && itemStack.getType() != Material.AIR) {
                inventory.setResult(null);
                return;
            }
        }
    }

    private boolean isDisabled(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        NamespacedKey customDataKey = NamespacedKey.fromString("minecraft:custom_data");
        if (customDataKey == null) {
            return false;
        }

        PersistentDataContainer container =
                pdc.get(customDataKey, PersistentDataType.TAG_CONTAINER);

        if (container == null) {
            return false;
        }

        NamespacedKey weaponKey =
                NamespacedKey.fromString(CrystalWeapons.WEAPON_COMPOUND);

        NamespacedKey ammunitionKey =
                NamespacedKey.fromString(CrystalWeapons.AMMUNITION_COMPOUND);

        return (weaponKey != null
                && container.has(weaponKey, PersistentDataType.TAG_CONTAINER))
                || (ammunitionKey != null
                && container.has(ammunitionKey, PersistentDataType.TAG_CONTAINER));
    }
}
