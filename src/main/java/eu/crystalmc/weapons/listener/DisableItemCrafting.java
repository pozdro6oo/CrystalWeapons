package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.utils.WeaponUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;

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
        Bukkit.getConsoleSender().sendMessage("Cancelled crafting or trade");
        return WeaponUtils.isCrystalWeaponsItem(item);
    }
}
