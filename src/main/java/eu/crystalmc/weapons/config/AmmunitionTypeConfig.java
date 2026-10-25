package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AmmunitionTypeConfig {

    public enum Key {
        SUPPORTED_WEAPON_TYPES,
        PRIORITY,
        MODIFIERS,
        ID;

        public final String key = this.toString().toLowerCase();

    }

    @SerializedName("display_name")
    private String displayName = "Ammunition";

    @SerializedName("modifiers")
    private Modifiers modifiers = new Modifiers();

    @SerializedName("model")
    private String model = "default";

    @SerializedName("supported_weapon_types")
    private List<String> supportedWeaponTypes = Collections.emptyList();

    @SerializedName("priority")
    private int priority = 0;

    public String getModel() { return model; }

    public int getPriority() { return priority; }

    public List<String> getSupportedWeaponTypes() { return supportedWeaponTypes; }

    public String getDisplayName() { return displayName; }


    public static class Modifiers {
        public Modifiers() {}

        @Nullable
        @SerializedName("apply_fire")
        public ApplyFireModifier applyFire = null;

        @Nullable
        @SerializedName("add_damage")
        public AddDamage addDamage = null;

        @SerializedName("custom")
        public List<String> custom = Collections.emptyList();
    }

    public static class Modifier {
        Modifier() {}
    }

    public static class ApplyFireModifier extends Modifier {
        ApplyFireModifier() {}
        @SerializedName("time")
        public int time = 60;
    }

    public static class AddDamage extends Modifier {
        AddDamage() {}
        @SerializedName("amount")
        public double amount = 2.0;

    }


}