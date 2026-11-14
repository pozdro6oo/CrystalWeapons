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
import org.bukkit.scheduler.BukkitRunnable;

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

//    public Sound playSound(Player player, String soundKey, Float volume, float pitch) {
//        Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, volume, pitch);
//        player.playSound(sound, player.getLocation());
//        return sound;
//    }



    public Map<Player, Sound> playSoundForEveryone(Location location, String soundKey, float volume, float pitch, double minDistance, double maxDistance, Player ignore) {
        Map<Player, Sound> sounds = new HashMap<>();

        Key key = Key.key(soundKey);
        float vol;
        float calculatedPitch;
        double distance;
        Sound sound;

        for (Player player : location.getNearbyPlayers(maxDistance)) {
            if (player == ignore) {
                continue;
            }

            distance = player.getEyeLocation().distance(location);

            if (distance < minDistance || distance > maxDistance) {
                continue;
            }

            double progress = (distance - minDistance) / (maxDistance - minDistance);

            vol = volume * (float) (1.0 - progress);

            if (vol <= 0.0f) {
                continue;
            }


            double pitchStartDistance = 30.0;

            double pitchProgress = Math.clamp((distance - pitchStartDistance) / (maxDistance - pitchStartDistance), 0.0, 2.0);

            calculatedPitch = pitch * (float) (1.0 - pitchProgress * 0.25);


            sound = Sound.sound(key, Sound.Source.PLAYER, vol, calculatedPitch);

            sounds.put(player, sound);
            player.playSound(sound, player.getLocation());
        }

        return sounds;
    }

    public void playShootSound(Location location, Weapon weapon) {
        float volume = getRandomPitch();
        float pitch = getRandomPitch();
        String sound = "crystalmc:weapon." + weapon.getShootSound();
        double maxDistance = weapon.getMaxHearDistance();

//        float echoVolume = volume * (float) 0.55;

        playSoundForEveryone(location, sound, volume, pitch, 0.0, maxDistance, null);

//        // echo
//
//        new BukkitRunnable() {@Override public void run() {
//            playSoundForEveryone(location, sound, echoVolume, pitch - (float) 0.3, 25.0, maxDistance + 25.0, null);
//        }}.runTaskLater(plugin, 3);
//
//        new BukkitRunnable() {@Override public void run() {
//            playSoundForEveryone(location, sound, echoVolume, pitch - (float) 0.7, 50.0, maxDistance + 50.0, null);
//        }}.runTaskLater(plugin, 5);

    }
    public void playBulletPassbySound(Location location, Player shooter) {
        playSoundForEveryone(location, "crystalmc:shot.bullet.passby", 0.38f, 1.0f, 0.0, 5.3, shooter);
    }
    public Map<Player, Sound> playReloadSound(Location location, Weapon weapon) {
        return playSoundForEveryone(location, "crystalmc:weapon." + weapon.getReloadSound(), 0.8f, 1.23f, 0.0, 9.0, null);
    }


    private float getRandomPitch() {
        float pitch = getRandomFloat(config.weapons.sound.minPitch, config.weapons.sound.maxPitch);
        if (pitch <= 0.0f) {
            plugin.sendError("Value 'min_pitch' cannot be higher than 'max_pitch'. Please change your configuration.");
            return config.weapons.sound.maxPitch;
        } else {
            return pitch;
        }
    }
    private float getRandomVolume() {
        float volume = getRandomFloat(config.weapons.sound.minVolume, config.weapons.sound.maxVolume);
        if (volume <= 0.0f) {
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
            return 0.0f;
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
