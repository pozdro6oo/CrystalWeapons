package eu.crystalmc.weapons.listener;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.WeaponsRegistry;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

public class SynchronizeWeaponWithConfiguration implements Listener {
    CrystalWeapons plugin;
    Config.Data config;

    public SynchronizeWeaponWithConfiguration(CrystalWeapons plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquip(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int slot = event.getNewSlot();


        ItemStack item = player.getInventory().getItem(slot);

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;



        if (!weapon.isSynchronizedWithConfiguration() && weapon.isSynchronizeWeaponData() && config.isSynchronizeWeaponWithConfiguration()) {
            ItemStack newItem = WeaponsRegistry.getWeapon(weapon.getId());

            if (newItem == null) {
                if (config.weapons.data.removeWeaponIfNotExist) {
                    ItemStack air = new ItemStack(Material.AIR);
                    player.getInventory().setItem(slot, air);
                }
                return;
            }

            assert item != null;
            NBT.modify(item, itemNBT -> {
                NBT.get(newItem, newItemNBT -> {
                    ReadWriteNBT compound = WeaponUtils.getNBT(itemNBT);

                    compound.mergeCompound(WeaponUtils.getItemNBT(newItemNBT));
                    compound.setInteger(Weapon.Key.AMMUNITION.key, weapon.getAmmunition());
                });
            });

            Weapon newWeapon = WeaponUtils.getWeapon(item);

            WeaponUtils.updateLore(item, newWeapon);

            plugin.sendInfo("Synchronized weapon of " + player.getName() + " with configuration of weapon '" + weapon.getId() + "'");
            player.getInventory().setItem(slot, item);

        }
    }
}

