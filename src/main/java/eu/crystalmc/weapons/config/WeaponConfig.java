package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;

public class WeaponConfig {

    @SerializedName("item")
    private Item item = new Item();

    @SerializedName("sounds")
    private Sounds sounds = new Sounds();

    @SerializedName("visual")
    private Visual visual = new Visual();

    @SerializedName("magazine_size")
    private int magazineSize = 12;

    @SerializedName("reload_time")
    private int reloadTime = 45;

    @SerializedName("shot_delay")
    private int shotDelay = 4;

    @SerializedName("damage")
    private double damage = 2.0;

    @SerializedName("inaccuracy")
    private double inaccuracy = 0.017;

    @SerializedName("recoil")
    private float recoil = 0.62F;

    @SerializedName("automatic")
    private boolean automatic = true;

    @SerializedName("max_distance")
    private double maxDistance = 58;

    @SerializedName("bullets_per_shot")
    private int bulletsPerShot = 1;

    @SerializedName("infinite_ammunition")
    private boolean infiniteAmmunition = false;

    @SerializedName("type")
    private String type = "rifle";

    // for gson to work
    public WeaponConfig() {
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public String getDisplayName() {
        return item.displayName;
    }


    public int getReloadTime() {
        return reloadTime;
    }


    public int getShotDelay() {
        return shotDelay;
    }


    public double getDamage() {
        return damage;
    }


    public double getInaccuracy() {
        return inaccuracy;
    }


    public float getRecoil() {
        return recoil;
    }


    public boolean isAutomatic() {
        return automatic;
    }

    public double getMaxDistance() {
        return maxDistance;
    }


    public String getShootSound() {
        return sounds.shoot;
    }


    public String getReloadSound() {
        return sounds.reload;
    }


    public int getBulletsPerShot() {
        return bulletsPerShot;
    }


    public boolean isInfiniteAmmunition() {
        return infiniteAmmunition;
    }


    public String getGunFireAnimationModel() {return item.firedModel;}

    public String getModel() {
        return item.model;
    }


    public String getBulletTracerBlock() {
        return visual.bulletTracerBlock;
    }

    public boolean isSpawnBulletHitDecal() {
        return visual.spawnBulletHitDecal;
    }

    public String getType() {
        return type;
    }



    public static class Item {
        Item() {}

        @SerializedName("display_name")
        private String displayName = "Weapon";

        @SerializedName("model")
        private String model = "default";

        @SerializedName("item-model")
        private String itemModel = "default";

        @SerializedName("fired_model")
        private String firedModel = "default";
    }

    public static class Visual {
        Visual() {}

        @SerializedName("bullet_tracer_block")
        private String bulletTracerBlock = "GRAY_STAINED_GLASS";

        @SerializedName("spawn_bullet_hit_decal")
        private boolean spawnBulletHitDecal = true;

    }

    public static class Sounds {
        Sounds() {}

        @SerializedName("shoot")
        private String shoot = "weapon.shoot";

        @SerializedName("reload")
        private String reload = "weapon.reload";
    }

}