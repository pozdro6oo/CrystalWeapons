package eu.crystalmc.weapons.utils;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableItemNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import eu.crystalmc.weapons.AmmunitionTypesRegistry;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import eu.crystalmc.weapons.config.Config;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class WeaponUtils {
    public static Weapon getWeapon(ItemStack item) {
        if (!isWeapon(item)) return null;

        Weapon[] weaponHolder = new Weapon[1];

        NBT.get(item, nbt -> {
            ReadableNBT compound = WeaponUtils.getItemNBT(nbt);
            if (compound != null) {
                weaponHolder[0] = NBTUtils.nbtToObject(compound, Weapon.class);
            }
        });

        return weaponHolder[0];
    }

    public static boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        ReadableNBT[] compound = new ReadableNBT[1];

         NBT.get(item, nbt -> {
            compound[0] = getItemNBT(nbt);
        });


        return compound[0] != null;
    }


    public static String getFireAnimationModel(Weapon weapon) {
        return "crystalmc:weapon/" + weapon.getId() + "_fire";
    }

    public static String getModel(Weapon weapon) {
        return "crystalmc:weapon/" + weapon.getId();
    }

    public static NamespacedKey createCooldownGroup() {
        return NamespacedKey.fromString("crystalmc:weapon/"+ UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public static ReadWriteNBT getNBT(ReadWriteNBT nbt) {
        return nbt.getCompound(CrystalWeapons.WEAPON_COMPOUND);
    }
    public static ReadableNBT getItemNBT(ReadableItemNBT nbt) {
        return nbt.getCompound(CrystalWeapons.WEAPON_COMPOUND);
    }


    public static String getAmmunitionText(Weapon weapon, Config.Data config, int magazines) {
        String text = getAmmunitionText(weapon);
        if (config.weapons.gameplay.magazineSystem.enabled == true)  {
            text = text + " [" + magazines + "]";
        }
        return text;
    }
    public static String getAmmunitionText(Weapon weapon) {
        return "Ammunition: " + weapon.getAmmunition() + "/" + weapon.getMagazineSize();
    }


    static public int getAmmunitionAmount(Player player, Weapon weapon) {
        int count = 0;

        for (ItemStack ammunition : AmmunitionTypesRegistry.getItems().values()) {
            if (player.getInventory().containsAtLeast(ammunition, 1)) {
                ReadableNBT compound = NBT.get(ammunition, nbt ->
                        (ReadableNBT) nbt.getCompound(CrystalWeapons.WEAPON_COMPOUND));
                if (compound != null) {
                    if (compound.getStringList(AmmunitionTypeConfig.Key.SUPPORTED_WEAPONS.key).contains(weapon.getId())) {
                        count++;
                    }
                }
            }

        }

        return count;
    }


    static public String getAmmunitionType(Player player, Weapon weapon) {
        for (ItemStack ammunition : AmmunitionTypesRegistry.getItems().values()) {
            if (player.getInventory().containsAtLeast(ammunition, 1)) {
                ReadableNBT compound = NBT.get(ammunition, nbt -> (ReadableNBT) nbt.getCompound(CrystalWeapons.WEAPON_COMPOUND));
                if (compound != null) {
                    if (compound.getStringList(AmmunitionTypeConfig.Key.SUPPORTED_WEAPONS.key).contains(weapon.getId())) {
                        return compound.getString(AmmunitionTypeConfig.Key.ID.key);
                    }
                }
            }

        }
        return null;
    }


    static public boolean removeAmmunitionType(Player player, String ammunitionType) {
        ItemStack item =  AmmunitionTypesRegistry.getAmmunitionType(ammunitionType);
        if (item != null && ammunitionType != null) {
            player.getInventory().removeItemAnySlot(item);
            return true;
        } else {
            return false;
        }


    }
    static public boolean hasAmmunitionType(Player player, String ammunitionType) {
        ItemStack item =  AmmunitionTypesRegistry.getAmmunitionType(ammunitionType);
        if (item != null && ammunitionType != null) {
            return player.getInventory().containsAtLeast(item, 1);
        } else {
            return false;
        }
    }





    public static void updateLore(ItemStack item, Weapon weapon) {
        ItemMeta meta = updateLore(item.getItemMeta(), weapon);
        if (meta == null) return;
        item.setItemMeta(meta);
    }
    public static ItemMeta updateLore(ItemMeta meta, Weapon weapon) {
        if (meta == null) return null;
        List<Component> lore = meta.lore();
        if (lore != null && !lore.isEmpty()) {
            lore.set(0, getBlueText(getAmmunitionText(weapon)));
            meta.lore(lore);
        }
        return meta;
    }

    public static Component getBlueText(String string) {
        return Component.text(string).color(NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false);
    }

    public static void setAnimation(ItemStack item, ItemUseAnimation animation) {
        Consumable.Builder builder = Consumable.consumable()
                .hasConsumeParticles(false)
                .animation(animation)
                .consumeSeconds(20);
        item.setData(DataComponentTypes.CONSUMABLE, builder.build());
    }


}
