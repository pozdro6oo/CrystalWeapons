package eu.crystalmc.weapons.config;

import com.google.gson.annotations.SerializedName;
import eu.crystalmc.weapons.CrystalWeapons;

public class Synchronizable {
    public static String SYNCHRONIZE_DATA = "synchronize_data";
    public static String RUNTIME = "runtime";

    @SerializedName("runtime")
    private String runtime;

    @SerializedName("synchronize_data")
    private boolean synchronizeData;

    @SerializedName("id")
    private String id;

    public boolean isSynchronizedWithConfiguration() {
        return runtime.equals(CrystalWeapons.RUNTIME);
    }

    public boolean isSynchronizeData() { return synchronizeData; }

    public String getRuntime() { return runtime; }

    public String getId() {
        return id;
    }
}
