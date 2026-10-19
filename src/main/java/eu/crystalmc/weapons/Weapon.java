package eu.crystalmc.weapons;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.config.WeaponConfig;

public class Weapon extends WeaponConfig {

    public enum Key {
        AMMUNITION,
        ID,
        SYNCHRONIZE_WEAPON_DATA,
        RUNTIME;

        public final String key = this.toString().toLowerCase();

    }

    @SerializedName("ammunition")
    private int ammunition;

    @SerializedName("id")
    private String id;

    @SerializedName("synchronize_weapon_data")
    private boolean synchronize_weapon_data;

    @SerializedName("runtime")
    private String runtime;


    public int getAmmunition() {
        return ammunition;
    }
    public void setAmmunition(int ammunition) {
        this.ammunition = ammunition;
    }


    public String getId() {
        return id;
    }


    public boolean isSynchronizeWeaponData() {return synchronize_weapon_data; }

    public String getRuntime() {return runtime; }

    public void setRuntime(String runtime) { this.runtime = runtime; }

    public boolean isSynchronizedWithConfiguration() {
        return runtime.equals(CrystalWeapons.RUNTIME);
    }
}