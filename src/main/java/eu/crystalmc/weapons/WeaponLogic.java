package eu.crystalmc.weapons;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import eu.crystalmc.weapons.api.event.player.EntityHitByWeaponEvent;
import eu.crystalmc.weapons.api.event.player.PlayerReloadWeaponEvent;
import eu.crystalmc.weapons.api.event.player.PlayerShootWeaponEvent;
import eu.crystalmc.weapons.api.event.player.PlayerUseWeaponEvent;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.WeaponUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.util.Transformation;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

class WeaponLogic implements Listener {

    private final CrystalWeapons plugin;
    private final Config.Data config;

    String usePermission;

    public WeaponLogic(CrystalWeapons plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
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

        removeFireModel(item, weapon);

        displayBullets(player, weapon, WeaponUtils.getAmmunitionAmount(player, weapon));
    }

    // Prevent player from dropping a fired weapon
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        if (item.getType() == Material.AIR) return;

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;

        removeFireModel(item, weapon);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRightClick(PlayerInteractEvent event) {
        if (!plugin.hasPermission(event.getPlayer(), usePermission)) return;

        ItemStack item = event.getPlayer().getEquipment().getItem(EquipmentSlot.HAND);

        if (item.getType() == Material.AIR) return;

        Weapon weapon = WeaponUtils.getWeapon(item);
        if (weapon == null) return;

        useWeapon(event.getPlayer(), item, weapon);
    }

    private void removeFireModel(ItemStack item, Weapon weapon) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getItemModel() == null) return;

        if (meta.getItemModel().toString().equals(WeaponUtils.getFireAnimationModel(weapon))) {
            meta.setItemModel(NamespacedKey.fromString(WeaponUtils.getModel(weapon)));
            item.setItemMeta(meta);
        }

    }


    private void useWeapon(Player player, ItemStack item, Weapon weapon) {
        PlayerUseWeaponEvent event = new PlayerUseWeaponEvent(player, weapon, item);
        event.callEvent();
        if (event.isCancelled()) return;

        new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (!player.isHandRaised() || !player.isOnline() || !item.equals(player.getActiveItem())) {
                    WeaponUtils.updateLore(item, weapon);
                    updateAmmunition(item, weapon);
                    this.cancel();
                    return;
                }

                if (weapon.getAmmunition() == 0) {
                    if (config.isMagazineSystemEnabled()) {
                        String ammunitionType = WeaponUtils.getAmmunitionType(player, weapon);
                        if (!WeaponUtils.hasAmmunitionType(player, ammunitionType)) {
                            player.clearActiveItem();
                            showHotbar(player, "No ammunition");
                        } else {
                            reload(player, weapon, item);
                        }
                    } else {
                        reload(player, weapon, item);
                    }
                    this.cancel();
                    return;
                }

                if (timer == weapon.getShotDelay()) {
                    timer = 0;
                }

                if (timer == 0) {
                    if (!weapon.isInfiniteAmmunition()) {
                        weapon.setAmmunition(weapon.getAmmunition() - 1);
                    }
                    displayBullets(player, weapon, WeaponUtils.getAmmunitionAmount(player, weapon));
                    shoot(player, weapon, item);

                    if (!weapon.isAutomatic()) {
                        this.cancel();
                        return;
                    }
                }

                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void showHotbar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

    private Sound playSound(Player player, String soundKey, float pitch) {
        Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.PLAYER, plugin.getConfiguration().weapons.sound.volume, pitch);
        player.playSound(sound, player.getLocation());
        return sound;
    }


    private void updateAmmunition(ItemStack item, Weapon weapon) {
        if (item.getType() == Material.AIR) return;

        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = WeaponUtils.getNBT(nbt);
            int ammunition = weapon.getAmmunition();
            if (compound != null) {
                compound.setInteger(Weapon.Key.AMMUNITION.key, ammunition);
                if (ammunition == 0) {
                    compound.removeKey(AmmunitionTypeConfig.Key.MODIFIERS.key);
                }
            }
        });
    }
    private void reload(Player player, Weapon weapon, ItemStack item) {
        PlayerReloadWeaponEvent event = new PlayerReloadWeaponEvent(player, weapon, item);
        event.callEvent();
        if (event.isCancelled()) return;

        setCooldown(player, item, weapon.getReloadTime());

        Sound sound = playSound(player, "crystalmc:weapon." + weapon.getReloadSound(), 1.0F);

        new BukkitRunnable() {
            private int timer = 0;
            private final EquipmentSlot equipmentSlot = player.getActiveItemHand();

            @Override
            public void run() {
                showHotbar(player, "Reloading...");

                player.clearActiveItem();

                // Cancelled.
                if (!item.equals(player.getEquipment().getItem(equipmentSlot))) {
                    showHotbar(player, "Reload cancelled");
                    player.stopSound(sound);
                    setCooldown(player, item, 0);
                    this.cancel();
                    return;
                }

                //  Success.
                if (timer == weapon.getReloadTime()) {
                    player.setActiveItemRemainingTime(0);
                    player.clearActiveItem();


                    if (config.isMagazineSystemEnabled()) {
                        String ammunitionType = WeaponUtils.getAmmunitionType(player, weapon);
                        if (WeaponUtils.hasAmmunitionType(player, ammunitionType)) {
                            boolean result = WeaponUtils.removeAmmunitionType(player, ammunitionType);
                            if (!result) {
                                showHotbar(player, "Reload cancelled");
                                this.cancel();
                                return;
                            }
                        } else {
                            showHotbar(player, "Reload cancelled");
                            this.cancel();
                            return;
                        }

                        ItemStack ammunitionItem = AmmunitionTypesRegistry.getAmmunitionType(ammunitionType);

                        copyModifiers(item, ammunitionItem);

                    }
                    showHotbar(player, "Reloaded");
                    weapon.setAmmunition(weapon.getMagazineSize());
                    updateAmmunition(item, weapon);

                    WeaponUtils.updateLore(item, weapon);
                    this.cancel();
                    return;
                }
                timer++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void copyModifiers(ItemStack item, ItemStack ammunitionItem) {
        if (item == null || ammunitionItem == null) return;

        NBT.modify(item, nbt -> {
            ReadWriteNBT compound = WeaponUtils.getNBT(nbt);
            String key = AmmunitionTypeConfig.Key.MODIFIERS.key;
            compound.removeKey(key);
            NBT.get(ammunitionItem, nbt2 -> {
                ReadableNBT compound2 = nbt2.getCompound(CrystalWeapons.AMMUNITION_COMPOUND);
                ReadableNBT ammunitionModifiers = compound2.getCompound(key);
                if (ammunitionModifiers != null) {
                    compound.getOrCreateCompound(key).mergeCompound(ammunitionModifiers);
                }

            });
        });
    }

    private boolean shouldBeRefreshed(Player player, ItemStack item, Weapon weapon, Config.Data config) {
        return !(item.equals(player.getActiveItem()) && weapon.getShotDelay() <= config.weapons.visual.fireAnimationFix && weapon.isAutomatic() && weapon.getAmmunition() > 0);
    }

    private void shoot(Player player, Weapon weapon, ItemStack item) {
        PlayerShootWeaponEvent event = new PlayerShootWeaponEvent(player, weapon, item);
        event.callEvent();
        if (event.isCancelled()) return;

        ItemMeta meta;

        String model = WeaponUtils.getModel(weapon);
        String modelFire = WeaponUtils.getFireAnimationModel(weapon);

        if (shouldBeRefreshed(player, item, weapon, config)) {
            updateAmmunition(item, weapon);
        }


        // Firing animation.
        if (config.weapons.visual.enableFireAnimation) {
            meta = item.getItemMeta();
            if (meta != null) {
                meta.setItemModel(NamespacedKey.fromString(modelFire));
                item.setItemMeta(meta);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Fixes the firing animation.
                    if (!shouldBeRefreshed(player, item, weapon, config)) {
                        return;
                    }
                    ItemMeta currentMeta = item.getItemMeta();

                    if (currentMeta != null) {
                        currentMeta.setItemModel(NamespacedKey.fromString(model));

                        item.setItemMeta(WeaponUtils.updateLore(currentMeta, weapon));
                    }
                }
            }.runTaskLater(plugin, 3L);
        } else {
            WeaponUtils.updateLore(item, weapon);
        }

        setCooldown(player, item, weapon.getShotDelay());

        playShootSound(player, weapon, config);

        applyDamage(player, weapon, item);

        if (weapon.getRecoil() > 0) recoil(player, weapon);

    }


    private void applyDamage(Player player, Weapon weapon, ItemStack item) {
        Map<LivingEntity, Double> damageMap = new HashMap<>();



        Location playerPov = player.getEyeLocation();

        for (int i = 0; i < weapon.getBulletsPerShot(); i++) {
            HitResult hit = getHitLocation(player, weapon, playerPov);

            if (config.weapons.visual.enableBulletTracers) createBulletTracer(player, hit, weapon);

            if (config.weapons.visual.bulletHitDecal.enable && weapon.isSpawnBulletHitDecal()) createBulletHitDecal(player, hit, weapon);

            Vector direction = hit.location().toVector().subtract(playerPov.toVector());

            double distance = playerPov.distance(hit.location);

            RayTraceResult result = player.getWorld().rayTraceEntities(
                    playerPov,
                    direction,
                    distance,
                    config.weapons.gameplay.bulletHitboxSize,
                    entity -> entity != player && entity instanceof Damageable
            );

            if (result != null && result.getHitEntity() instanceof LivingEntity entity) {
                damageMap.put(entity, damageMap.getOrDefault(entity, 0.0) + weapon.getDamage());
            }
        }
        // ----------------------------------------------------------------------------------------------------------

        for (Map.Entry<LivingEntity, Double> entry : damageMap.entrySet()) {
            LivingEntity entity = entry.getKey();
            Double damage = entry.getValue();

            EntityHitByWeaponEvent event = new EntityHitByWeaponEvent(entity, weapon, item, player, damage);
            event.callEvent();
            if (event.isCancelled()) return;

            entity.damage(event.getDamage(), player);
            entry.getKey().setNoDamageTicks(0);

        }
    }


    private void setCooldown(Player player, ItemStack item, int cooldown) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey group = meta.getUseCooldown().getCooldownGroup();
        assert group != null;
        player.setCooldown(group, cooldown);
    }

    private void playShootSound(Player player, Weapon weapon, Config.Data config) {
        float pitch;
        if (config.weapons.sound.minPitch < config.weapons.sound.maxPitch) {
            pitch = (float) ThreadLocalRandom.current().nextDouble(config.weapons.sound.minPitch, config.weapons.sound.maxPitch);
        } else {
            pitch = 1;
            plugin.sendError("Value 'min_pitch' cannot be higher than 'max_pitch'. Please change your configuration.");
        }
        playSound(player, "crystalmc:weapon." + weapon.getShootSound(), pitch);
    }

    private HitResult getHitLocation(Player player, Weapon weapon, Location playerPov) {
        double distance = getMaxDistance(weapon);


        boolean isStandingStill = player.getVelocity().getX() == 0.0
                && player.getVelocity().getY() <= 0.0
                && player.getVelocity().getZ() == 0.0
                && player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR;

        double inaccuracy = !isStandingStill ? weapon.getInaccuracy() + plugin.getConfiguration().weapons.gameplay.movementInaccuracy : weapon.getInaccuracy();

        Vector vector;
        if (inaccuracy > 0) {
            vector = new Vector(
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor(),
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor(),
                    ThreadLocalRandom.current().nextDouble(0.0, inaccuracy) * getRandomFactor()
            );
        } else {
            vector = new Vector();
        }

        Vector direction = playerPov.getDirection().add(vector);

        RayTraceResult result = player.getWorld().rayTraceBlocks(
                playerPov,
                direction,
                distance,
                FluidCollisionMode.ALWAYS,
                true
        );

        Location location = result != null
                ? result.getHitPosition().toLocation(playerPov.getWorld())
                : playerPov.clone().add(direction.multiply(distance));

        BlockFace hitFace = result != null ? result.getHitBlockFace() : null;

        return new HitResult(location, hitFace);
    }

    private void recoil(Player player, Weapon weapon) {
        float startYaw = player.getLocation().getYaw();
        float startPitch = player.getLocation().getPitch();

        float yaw = startYaw + (float) (-weapon.getRecoil() / 3.0 * getRandomFactor());
        float pitch = startPitch + (-weapon.getRecoil());

        player.setRotation(yaw, pitch);
    }

    private void createBulletTracer(Player player, HitResult hit, Weapon weapon) {
        Vector direction = player.getEyeLocation().getDirection().multiply(0.25);

        // Fix visual eye location
        Location start = player.getEyeLocation().add(direction);
//        start.subtract(0, 0.05, 0);

        // Calculate length
        Location hitLocation = hit.location();
        double length = start.distance(hitLocation);
        Location center = start.clone().add(
                hitLocation.clone().subtract(start).multiply(0.5)
        );


        ItemDisplay displayTracer = start.getWorld().spawn(center, ItemDisplay.class);

        // Rotate the display's local Z axis to face the hit location
        Vector dir = direction.clone().normalize();
        Location rotation = center.clone();
        rotation.setDirection(dir);
        displayTracer.setRotation(rotation.getYaw(), rotation.getPitch());


        Material tracerBlock;
        try {
            tracerBlock = Material.valueOf(weapon.getBulletTracerBlock().toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.sendWarning("Bullet tracer: '" + weapon.getBulletTracerBlock() + "' is not a valid material");
            tracerBlock = Material.GRAY_STAINED_GLASS;
        }

        displayTracer.setItemStack(new ItemStack(tracerBlock));
        displayTracer.setBillboard(Display.Billboard.FIXED);

        displayTracer.setTransformation(new Transformation(
                new Vector3f(),
                new AxisAngle4f(),
                new Vector3f(0.004f, 0.004f, (float) length),
                new AxisAngle4f()
        ));

        new BukkitRunnable() {
            @Override
            public void run() {
                displayTracer.remove();
            }
        }.runTaskLater(plugin, 2L);
    }




    private void createBulletHitDecal(Player player, HitResult hit, Weapon weapon) {
        Vector direction = player.getEyeLocation().clone().getDirection().multiply(0.25);

        Location hitLocation = hit.location();
        BlockFace blockFace = hit.blockFace();
        Location start = player.getEyeLocation().add(direction);
        start.setY(start.getY() - 0.05);

        Block hitBlock = hitLocation.getBlock();

        // check for negative coordinates (closest block).
        if (!hitLocation.getBlock().getType().isSolid()) {
            Location check = hitLocation.clone();
            check.subtract(0.1, 0.1, 0.1);
            if (!check.getBlock().getType().isSolid()) {
                return;
            } else {
                hitBlock = check.getBlock();
            }
        }

        AxisAngle4f rotation = getAxisAngle4f(blockFace);


        ItemDisplay displayHit = start.getWorld().spawn(hitLocation, ItemDisplay.class);

        displayHit.setItemStack(BulletHitDecalsManager.texture);
        displayHit.setBillboard(Display.Billboard.FIXED);

        displayHit.setTransformation(new Transformation(
                new Vector3f(),
                rotation,
                new Vector3f(0.2F),
                new AxisAngle4f()
        ));

        BulletHitDecalsManager.start(displayHit, hitBlock, plugin, plugin.getConfiguration());
    }



    private double getMaxDistance(Weapon weapon) {
        Config.Data.Weapons.Gameplay.LimitDistanceForAllWeapons limit = plugin.getConfiguration().weapons.gameplay.limitDistanceForAllWeapons;

        if (limit.enabled) {
            return limit.distance;
        } else {
            return weapon.getMaxDistance();
        }
    }

    private static @NonNull AxisAngle4f getAxisAngle4f(BlockFace blockFace) {
        AxisAngle4f rotation;
        if (blockFace == BlockFace.UP) {
            rotation = new AxisAngle4f((float) Math.toRadians(90.0), 1f, 0f, 0f);
        } else if (blockFace == BlockFace.DOWN) {
            rotation = new AxisAngle4f((float) Math.toRadians(-90.0), 1f, 0f, 0f);
        } else if (blockFace == BlockFace.EAST) {
            rotation = new AxisAngle4f((float) Math.toRadians(-90.0), 0f, 1f, 0f);
        } else if (blockFace == BlockFace.WEST) {
            rotation = new AxisAngle4f((float) Math.toRadians(90.0), 0f, 1f, 0f);
        } else if (blockFace == BlockFace.SOUTH) {
            rotation = new AxisAngle4f((float) Math.toRadians(180.0), 0f, 1f, 0f);
        } else {
            rotation = new AxisAngle4f();
        }
        return rotation;
    }

    private void displayBullets(Player player, Weapon weapon, int magazines) {
        showHotbar(player, WeaponUtils.getAmmunitionText(weapon, config, magazines));
    }

    private byte getRandomFactor() {
        return ThreadLocalRandom.current().nextBoolean() ? (byte) 1 : (byte) -1;
    }

    public record HitResult(Location location, BlockFace blockFace) {}
}