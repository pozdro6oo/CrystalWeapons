package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.utils.WeaponUtils;
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
            if (itemStack == null) return;
            if (itemStack.getType() != Material.AIR && isDisabled(itemStack)) {
                inventory.setResult(null);
                return;
            }
        }
    }

    private boolean isDisabled(ItemStack item) {
        return WeaponUtils.isCrystalWeaponsItem(item);
    }
}
