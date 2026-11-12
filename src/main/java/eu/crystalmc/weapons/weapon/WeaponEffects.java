package eu.crystalmc.weapons.weapon;

import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.WeaponUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
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



    public Map<Player, Sound> playSoundForEveryone(Location location, String soundKey, Float volume, float pitch) {

        Map<Player, Sound> sounds = new HashMap<>();


        double maxDistance = 16.0;

        for (Player player : location.getNearbyPlayers(maxDistance)) {
            double distance = location.distance(player.getLocation());
            CrystalWeapons.sendDebug(distance + "");

            float vol = volume * (float)(1.0 - distance / maxDistance);
            float volFinal = Math.clamp(vol, 0f, volume);

            Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, volFinal, pitch);

            sounds.put(player, sound);
            player.playSound(sound, player.getLocation());
        }

        return sounds;
    }

    public void playShootSound(Location location, Weapon weapon) {
        playSoundForEveryone(location, "crystalmc:weapon." + weapon.getShootSound(), getRandomVolume(), getRandomPitch());
    }
    public void playBulletPassbySound(Player player) {
        playSound(player, "crystalmc:shot.bullet.passby", getRandomVolume(), 1);
    }
    public Map<Player, Sound> playReloadSound(Location location, Weapon weapon) {
        return playSoundForEveryone(location, "crystalmc:weapon." + weapon.getReloadSound(), config.weapons.sound.maxVolume, 1F);
    }


    private float getRandomPitch() {
        float volume = getRandomFloat(config.weapons.sound.minPitch, config.weapons.sound.maxPitch);
        if (Float.isNaN(volume)) {
            plugin.sendError("Value 'min_pitch' cannot be higher than 'max_pitch'. Please change your configuration.");
            return config.weapons.sound.maxVolume;
        } else {
            return volume;
        }
    }
    private float getRandomVolume() {
        float volume = getRandomFloat(config.weapons.sound.minVolume, config.weapons.sound.maxVolume);
        if (Float.isNaN(volume)) {
            plugin.sendError("Value 'min_volume' cannot be higher than 'max_volume'. Please change your configuration.");
            return config.weapons.sound.maxVolume;
        } else {
            return volume;
        }
    }

    private float getRandomFloat(float min, float max) {
        if (min < max) {
            return (float) ThreadLocalRandom.current().nextDouble(min, max);
        } else {
            return Float.NaN;
        }
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


    public void displayBullets(Player player, Weapon weapon, int magazines) {
        showHotbar(player, WeaponUtils.getAmmunitionText(weapon, config, magazines));
    }
}
