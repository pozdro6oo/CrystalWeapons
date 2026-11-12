package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CreativeWeaponDuplicateFix implements Listener {

    @EventHandler
    public void onCreative(InventoryCreativeEvent event) {

        ItemStack cursor = event.getCursor();

        if (event.getAction() != InventoryAction.PLACE_ALL) return;
        if (cursor.getType() == Material.AIR) return;

        ItemMeta meta = cursor.getItemMeta();
        if (meta == null) return;

        if (!WeaponUtils.isWeapon(cursor)) return;


        NamespacedKey group = meta.getUseCooldown().getCooldownGroup();
        if (group == null) return;

        for (ItemStack itemStack : event.getInventory().getContents()) {

            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                continue;
            } else {
                itemMeta.getUseCooldown();
            }

            NamespacedKey itemGroup = itemMeta.getUseCooldown().getCooldownGroup();

            if (group.equals(itemGroup)) {
                CrystalWeapons.sendDebug("updated");
                WeaponUtils.setCooldownGroup(itemStack);
            }
        }
    }
}