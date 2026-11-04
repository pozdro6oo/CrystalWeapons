package eu.crystalmc.weapons.weapon;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.WeaponUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.concurrent.ThreadLocalRandom;

public class WeaponEffects {

    private final CrystalWeapons plugin;
    private final Config.Data config;

    public WeaponEffects(CrystalWeapons plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }

    public void showHotbar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

    public Sound playSound(Player player, String soundKey, Float volume, float pitch) {
        Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, volume, pitch);
        player.playSound(sound, player.getLocation());
        return sound;
    }




    public void setCooldown(Player player, ItemStack item, int cooldown) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey group = meta.getUseCooldown().getCooldownGroup();
        if (group == null) return;
        player.setCooldown(group, cooldown);
    }


    public int getCooldown(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        NamespacedKey group = meta.getUseCooldown().getCooldownGroup();
        if (group == null) return 0;
        return player.getCooldown(group);
    }

    public void playShootSound(Player player, Weapon weapon, Config.Data config) {
        float pitch;
        if (config.weapons.sound.minPitch < config.weapons.sound.maxPitch) {
            pitch = (float) ThreadLocalRandom.current().nextDouble(config.weapons.sound.minPitch, config.weapons.sound.maxPitch);
        } else {
            pitch = 1;
            plugin.sendError("Value 'min_pitch' cannot be higher than 'max_pitch'. Please change your configuration.");
        }
        playSound(player, "crystalmc:weapon." + weapon.getShootSound(), config.weapons.sound.volume, pitch);
    }

    public void displayBullets(Player player, Weapon weapon, int magazines) {
        showHotbar(player, WeaponUtils.getAmmunitionText(weapon, config, magazines));
    }
}
