package eu.crystalmc.weapons.listener.modifier;

import eu.crystalmc.weapons.api.event.player.EntityHitByWeaponEvent;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class ApplyFireModifier implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPrepareCraft(EntityHitByWeaponEvent event) {
        @Nullable AmmunitionTypeConfig.Modifiers modifiers = event.getWeapon().getModifiers();
        if (modifiers == null) return;

        AmmunitionTypeConfig.ApplyFireModifier modifier = modifiers.applyFire;
        if (modifier == null) return;
        event.getEntity().setFireTicks(modifier.time);
    }
}
