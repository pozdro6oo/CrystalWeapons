package eu.crystalmc.weapons.listener;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.*;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class SynchronizeDataWithConfiguration implements Listener {
    private final CrystalWeapons plugin;
    private final Config.Data config;

    public SynchronizeDataWithConfiguration(CrystalWeapons plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquip(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int slot = event.getNewSlot();

        if (!config.isSynchronizeItemWithConfiguration()) return;

        ItemStack item = player.getInventory().getItem(slot);

        if (item == null || item.getType() == Material.AIR) return;

        if (WeaponUtils.isWeapon(item)) {
            synchronize(player, CrystalWeapons.WEAPON_COMPOUND, slot, item);
        } else if (WeaponUtils.isAmmunitionType(item)) {
            synchronize(player, CrystalWeapons.AMMUNITION_COMPOUND, slot, item);
        }


    }

    private void synchronize(Player player, String compoundKey, int slot, ItemStack item) {
        Synchronizable data = WeaponUtils.getSynchronizable(item, compoundKey);
        if (data == null) return;
        if (data.isSynchronizeData() && config.isSynchronizeItemWithConfiguration() && !data.isSynchronizedWithConfiguration()) {
            ItemStack newItem;

            if (compoundKey.equals(CrystalWeapons.WEAPON_COMPOUND)) {
                newItem = CrystalWeapons.getWeapon(data.getId());
            } else if (compoundKey.equals(CrystalWeapons.AMMUNITION_COMPOUND)) {
                newItem = AmmunitionTypesRegistry.getAmmunitionType(data.getId());
            } else {
                return;
            }

            if (config.weapons.data.removeItemIfNotExist) {
                ItemStack air = new ItemStack(Material.AIR);
                player.getInventory().setItem(slot, air);
            }

            NBT.modify(item, itemNBT -> {
                assert newItem != null;
                NBT.get(newItem, newItemNBT -> {
                    ReadWriteNBT compound = itemNBT.getCompound(compoundKey);

                    assert compound != null;
                    int ammunition = compound.getInteger(Weapon.Key.AMMUNITION.key);

                    compound.mergeCompound(newItemNBT.getCompound(compoundKey));
                    if (compoundKey.equals(CrystalWeapons.WEAPON_COMPOUND)) {
                        compound.setInteger(Weapon.Key.AMMUNITION.key, ammunition);
                    }
                });
            });

            if (compoundKey.equals(CrystalWeapons.WEAPON_COMPOUND)) {
                Weapon newWeapon = WeaponUtils.getWeapon(item);

                WeaponUtils.updateLore(item, newWeapon);
            }

            if (config.isLogOnSynchronization()) {
                plugin.sendInfo("Synchronized item of " + player.getName() + " with configuration of '" + data.getId() + "'");
            }
            player.getInventory().setItem(slot, item);
        }
    }
}

