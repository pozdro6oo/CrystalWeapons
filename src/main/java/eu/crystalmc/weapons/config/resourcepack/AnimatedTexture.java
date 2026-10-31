package eu.crystalmc.weapons.config.resourcepack;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnimatedTexture {

    @SerializedName("animation")
    public Animation model = new Animation();

    public AnimatedTexture() { }

    public static class Animation {
        public Animation() {}

        @SerializedName("frametime")
        public int frameTime = 1;

        @SerializedName("interpolate")
        public boolean interpolate = true;

        @SerializedName("frames")
        public List<Integer> frames = List.of(0, 1, 2);
    }
}
