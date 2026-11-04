package eu.crystalmc.weapons.utils;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.iface.ReadableNBTList;
import eu.crystalmc.weapons.AmmunitionTypesRegistry;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AmmunitionUtils {
    static public String getAvailableAmmunitionType(Player player, Weapon weapon) {
        for (ItemStack ammunition : AmmunitionTypesRegistry.getItems().values()) {
            if (player.getInventory().containsAtLeast(ammunition, 1)) {
                boolean contains = isAmmunitionSupportsWeapon(ammunition, weapon);

                if (contains) {
                    return NBT.get(ammunition, nbt -> (String) nbt.getCompound(CrystalWeapons.AMMUNITION_COMPOUND).getString(AmmunitionTypeConfig.Key.ID.key));
                }
            }

        }
        return null;
    }


    static public boolean removeAmmunitionType(Player player, String ammunitionType) {
        ItemStack item = AmmunitionTypesRegistry.getAmmunitionType(ammunitionType);
        if (item != null && ammunitionType != null) {
            player.getInventory().removeItemAnySlot(item);
            return true;
        } else {
            return false;
        }


    }
    static public boolean hasAmmunitionType(Player player, String ammunitionType) {
        ItemStack item = AmmunitionTypesRegistry.getAmmunitionType(ammunitionType);
        if (item != null && ammunitionType != null) {
            return player.getInventory().containsAtLeast(item, 1);
        } else {
            return false;
        }
    }


    static public int getAmmunitionAmount(Player player, Weapon weapon) {
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (isAmmunitionSupportsWeapon(item, weapon)) {
                    count += item.getAmount();
                }
            }

        }

        return count;
    }

    private static boolean isAmmunitionSupportsWeapon(ItemStack itemStack, Weapon weapon) {
        if (itemStack == null) return false;
        if (itemStack.getType() == Material.AIR) return false;

        return NBT.get(itemStack, nbt -> {
            ReadableNBT compound = nbt.getCompound(CrystalWeapons.AMMUNITION_COMPOUND);
            if (compound == null) return false;
            ReadableNBTList<String> list = compound.getStringList(AmmunitionTypeConfig.Key.SUPPORTED_WEAPON_TYPES.key);
            return list.contains(weapon.getType()) || list.contains(weapon.getId());
        });
    }
}
