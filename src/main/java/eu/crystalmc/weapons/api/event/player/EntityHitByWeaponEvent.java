package eu.crystalmc.weapons.api.event.player;

import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public class EntityHitByWeaponEvent extends Event implements Cancellable {

    private final Entity entity;
    private final Weapon weapon;
    private final ItemStack itemStack;
    private final Entity damager;
    private Double damage;
    private final Double originalDamage;



    public EntityHitByWeaponEvent(Entity entity, Weapon weapon, ItemStack itemStack, Entity damager, Double damage) {
        this.weapon = weapon;
        this.entity = entity;
        this.itemStack = itemStack;
        this.damager = damager;
        this.damage = damage;
        this.originalDamage = damage;
    }

    public Entity getEntity() {
        return entity;

    }
    public Entity getDamager() {
        return damager;
    }

    public Double getDamage() {
        return damage;
    }

    public void setDamage(Double damage) {
        this.damage = damage;
    }

    public Double getOriginalDamage() {
        return originalDamage;
    }
    public AmmunitionTypeConfig.Modifiers getModifiers() {
        return weapon.getModifiers();
    }


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
