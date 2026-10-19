package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class AmmunitionTypeConfig {

    public enum Key {
        SUPPORTED_WEAPONS,
        PRIORITY,
        ID;

        public final String key = this.toString().toLowerCase();

    }

    @SerializedName("display_name")
    private String displayName = "Ammunition";

    @SerializedName("model")
    private String model = "default";

    @SerializedName("supported_weapons")
    private List<String> supportedWeapons = Collections.emptyList();

    @SerializedName("priority")
    private int priority = 0;

    public String getModel() { return model; }

    public int getPriority() { return priority; }

    public List<String> getSupportedWeapons() { return supportedWeapons; }

    public String getDisplayName() { return displayName; }




}