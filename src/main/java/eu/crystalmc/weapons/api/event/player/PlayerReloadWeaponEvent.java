package eu.crystalmc.weapons.api.event.player;

import eu.crystalmc.weapons.Weapon;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class PlayerReloadWeaponEvent extends PlayerUseWeaponEvent{


    public PlayerReloadWeaponEvent(Player player, Weapon weapon, ItemStack itemStack) {
        super(player, weapon, itemStack);
    }


    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
