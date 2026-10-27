package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class DisableItemCrafting implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        for (ItemStack itemStack : inventory.getMatrix()) {
            if (isDisabled(itemStack)) {
                inventory.setResult(null);
                return;
            }
        }
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onPrepareCraft(TradeSelectEvent event) {
//        MerchantInventory inventory = event.getInventory();
//        for (ItemStack itemStack : inventory.getContents()) {
//            if (itemStack != null) {
//                Bukkit.getConsoleSender().sendMessage(itemStack.displayName().append(Component.text(" : " + itemStack.getType() + " : ")));
//                if (isDisabled(itemStack)) {
//                    inventory.setItem(2, null);
//                    return;
//                }
//            }
//        }
//    }


    private boolean isDisabled(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.AIR) return false;
        return WeaponUtils.isCrystalWeaponsItem(item);
    }
}
