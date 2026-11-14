package eu.crystalmc.weapons.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.jspecify.annotations.NonNull;


public class LocationUtils {
    public static @Nullable Block getSolidBlock(Location location) {
        Block block = location.getBlock();

        if (!block.getType().isSolid()) {
            block = location.clone().subtract(0.1, 0.1, 0.1).getBlock();
        }

        return block.getType().isSolid() ? block : null;
    }

    public static @NonNull AxisAngle4f getAxisAngle4f(BlockFace blockFace) {
        return switch (blockFace) {
            case UP -> new AxisAngle4f((float) Math.toRadians(90), 1f, 0f, 0f);
            case DOWN -> new AxisAngle4f((float) Math.toRadians(-90), 1f, 0f, 0f);
            case EAST -> new AxisAngle4f((float) Math.toRadians(-90), 0f, 1f, 0f);
            case WEST -> new AxisAngle4f((float) Math.toRadians(90), 0f, 1f, 0f);
            case SOUTH -> new AxisAngle4f((float) Math.toRadians(180), 0f, 1f, 0f);
            default -> new AxisAngle4f();
        };
    }
}
