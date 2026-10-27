package eu.crystalmc.weapons.config.resourcepack;

import com.google.gson.annotations.SerializedName;
import org.eclipse.sisu.Nullable;

import java.util.List;

public class Item {

    @SerializedName("model")
    public CustomModel model = new CustomModel();

    // for gson to work
    public Item() {
    }

    public static class CustomModel {
        public CustomModel() {}

        @SerializedName("type")
        public String type = "minecraft:model";

        @SerializedName("model")
        public String model = "minecraft:item/paper";

        @Nullable
        @SerializedName("models")
        public List<CustomModel> models = null;



    }

}
