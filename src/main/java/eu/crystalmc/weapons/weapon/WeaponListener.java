package eu.crystalmc.weapons.weapon;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.utils.WeaponUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class WeaponListener implements Listener {

    private final CrystalWeapons plugin;
    private final WeaponLogic weaponLogic;
    private final WeaponEffects weaponEffects;

    String usePermission;

    public WeaponListener(CrystalWeapons plugin, WeaponLogic weaponLogic, WeaponEffects weaponEffects) {
        this.plugin = plugin;
        this.weaponLogic = weaponLogic;
        this.weaponEffects = weaponEffects;
        this.usePermission = plugin.getConfiguration().permissions.useWeapon;
    }


    // Display bullets when player equips a weapon
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEquip(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null) return;
        if (item.getType() == Material.AIR) return;

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;


        weaponEffects.displayBullets(player, weapon, WeaponUtils.getAmmunitionAmount(player, weapon));
    }



    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.playerRequiresPermission(event.getPlayer(), usePermission)) return;

        ItemStack item = event.getPlayer().getEquipment().getItem(EquipmentSlot.HAND);

        if (item.getType() == Material.AIR) return;

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;

        weaponLogic.useWeapon(event.getPlayer(), item, weapon);
    }
}
