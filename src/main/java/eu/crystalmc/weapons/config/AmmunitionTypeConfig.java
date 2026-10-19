package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;

public class AmmunitionTypeConfig {

    public enum Key {
        ID;

        public final String key = this.toString().toLowerCase();

    }

    @SerializedName("display_name")
    private String displayName = "Ammunition";

    @SerializedName("model")
    private String model = "default";

    public String getModel() { return model; }

    public String getDisplayName() { return displayName; }




}