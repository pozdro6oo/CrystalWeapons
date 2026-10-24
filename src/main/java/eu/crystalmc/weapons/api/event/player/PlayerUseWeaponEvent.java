package eu.crystalmc.weapons.api.event.player;

import eu.crystalmc.weapons.Weapon;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;


public class PlayerUseWeaponEvent extends Event implements Cancellable {

    private Player player;
    private Weapon weapon;
    private ItemStack itemStack;

    public PlayerUseWeaponEvent(Player player, Weapon weapon, ItemStack itemStack) {
        this.weapon = weapon;
        this.player = player;
        this.itemStack = itemStack;
    }

    public Player getPlayer() {return player;}

    public Weapon getWeapon() {return weapon;}

    public ItemStack getItemStack() {return itemStack;}

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
