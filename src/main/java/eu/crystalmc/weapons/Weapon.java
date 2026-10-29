package eu.crystalmc.weapons;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import eu.crystalmc.weapons.config.WeaponConfig;
import org.jetbrains.annotations.Nullable;

public class Weapon extends WeaponConfig {

    public enum Key {
        AMMUNITION,
        ID;

        public final String key = this.toString().toLowerCase();


    }

    @SerializedName("ammunition")
    private int ammunition;

    @SerializedName("id")
    private String id;


    @Nullable
    @SerializedName("modifiers")
    private AmmunitionTypeConfig.Modifiers modifiers;


    public int getAmmunition() {
        return ammunition;
    }
    public void setAmmunition(int ammunition) {
        this.ammunition = ammunition;
    }

    public @Nullable AmmunitionTypeConfig.Modifiers getModifiers() {
        return modifiers;
    }


    public String getId() {
        return id;
    }



}