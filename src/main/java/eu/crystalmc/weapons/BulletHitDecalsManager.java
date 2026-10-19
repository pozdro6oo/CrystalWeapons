package eu.crystalmc.weapons;

import de.tr7zw.nbtapi.NBT;
import eu.crystalmc.weapons.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

class BulletHitDecalsManager {

    private static final Deque<ItemDisplay> decals = new ArrayDeque<>();

    final public static ItemStack texture = createBulletHitTexture();


    public static void start(ItemDisplay displayHit, Block hitBlock, JavaPlugin plugin, Config.Data data) {
        int limit = data.weapons.visual.bulletHitDecal.limit;

//        NBT.modify(displayHit, nbt -> {
//            nbt.setBoolean("is_bullet_hit_decal", true);
//        });

        if (limit <= 0) {
            displayHit.remove();
            return;
        }

        while (decals.size() >= limit) {
            ItemDisplay oldest = decals.pollFirst();

            if (oldest != null && oldest.isValid()) {
                oldest.remove();
            }
        }

        decals.addLast(displayHit);

        final int[] timer = {0};
        int disappearAfter = data.weapons.visual.bulletHitDecal.disappearAfter*20;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!displayHit.isValid()) {
                    decals.remove(displayHit);
                    cancel();
                    return;
                }

                if (timer[0] >= disappearAfter || !hitBlock.getType().isSolid()) {
                    displayHit.remove();
                    decals.remove(displayHit);
                    cancel();
                    return;
                }

                timer[0]++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }


    public static void startGarbageCollector(JavaPlugin plugin, int interval) {
        new BukkitRunnable() {@Override public void run() {
                removeOld();
            }}.runTaskTimer(plugin, 0, interval* 20L);
    }

    public static void removeOld() {
        for (ItemDisplay entity : getAll()) {
            if (isBulletHitDecal(entity) && !decals.contains(entity)) {
                entity.remove();
            }
        }
    }

    public static void removeAll() {
        decals.forEach( Entity::remove );
        decals.clear();

        removeOld();
    }

    private static List<ItemDisplay> getAll() {
        return Bukkit.getWorlds().stream().flatMap(world -> world.getEntitiesByClass(ItemDisplay.class).stream()).toList();
    }

    private static boolean isBulletHitDecal(ItemDisplay entity) {
        NamespacedKey model = entity.getItemStack().getItemMeta().getItemModel();
        if (model == null) return false;
        String model1 = model.toString();
        String model2 = texture.getItemMeta().getItemModel().toString();
        return model1.equals(model2);
    }


     private static ItemStack createBulletHitTexture() {
        ItemStack item = new ItemStack(Material.BLACK_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setItemModel(NamespacedKey.fromString("crystalmc:bullet_hit"));
            item.setItemMeta(meta);
        }
        return item;
    }
}