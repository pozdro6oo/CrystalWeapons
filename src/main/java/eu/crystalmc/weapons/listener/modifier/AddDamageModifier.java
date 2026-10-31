package eu.crystalmc.weapons.listener.modifier;

import eu.crystalmc.weapons.api.event.player.EntityHitByWeaponEvent;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

public class AddDamageModifier implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareCraft(EntityHitByWeaponEvent event) {
        AmmunitionTypeConfig.Modifiers modifiers = event.getWeapon().getModifiers();
        if (modifiers == null) return;

        AmmunitionTypeConfig.AddDamage modifier = modifiers.addDamage;
        if (modifier == null) return;

        event.setDamage(event.getDamage() + modifier.amount);
    }
}
