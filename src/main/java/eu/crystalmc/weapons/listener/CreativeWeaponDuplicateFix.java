package eu.crystalmc.weapons.listener;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;

public class CreativeWeaponDuplicateFix implements Listener {
    @EventHandler
    public void onCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();

        CrystalWeapons.sendDebug(" --------------------------");
        CrystalWeapons.sendDebug(event.getClick() + "");
        CrystalWeapons.sendDebug(event.getAction() + "");
        CrystalWeapons.sendDebug(event.getCursor().getType() + "");

        if (WeaponUtils.isWeapon(item)) {

            ItemMeta meta = item.getItemMeta();
            UseCooldownComponent component = meta.getUseCooldown();


            if (event.getAction() == InventoryAction.CLONE_STACK) {
                CrystalWeapons.sendDebug("test1");

                if (item.getType() != Material.AIR) {

                    CrystalWeapons.sendDebug("test2");

                    if (WeaponUtils.isWeapon(item)) {

                        CrystalWeapons.sendDebug("test3");


                        component.setCooldownGroup(WeaponUtils.createCooldownGroup());


                        meta.setUseCooldown(component);
                        item.setItemMeta(meta);

                    }
                }
            }
        }
    }
}
