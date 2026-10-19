package eu.crystalmc.weapons.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommandUtils {

    public static Component getItemAsComponent(ItemStack itemStack) {
        List<Component> lore = itemStack.lore();

        Component hover = itemStack.getItemMeta().customName();
        if (lore != null && hover != null) {
            hover = hover.appendNewline().append(
                    Component.join(
                            JoinConfiguration.separator(Component.newline()),
                            lore
                    ));
        }
        assert hover != null;

        return itemStack.displayName().hoverEvent(HoverEvent.showText(itemStack.getItemMeta().customName()
        .append(itemStack.displayName().hoverEvent(HoverEvent.showText(hover)))));
    }

    public static void giveItem(CommandContext<CommandSourceStack> context, ItemStack item, int count) {
        CommandSender sender = context.getSource().getSender();

        if (item == null) {
            sender.sendMessage(Component.text("&cUnknown item").color(NamedTextColor.RED));
            return;
        }

        item.setAmount(count);

        List<Entity> entities;
        try {
            entities = context.getArgument("entities", EntitySelectorArgumentResolver.class).resolve(context.getSource());
        } catch (CommandSyntaxException e) {
            sender.sendMessage(Component.text("&cUnknown error").color(NamedTextColor.RED));
            return;
        }

        LivingEntity livingEntity;
        Player player;
        for (Entity entity : entities) {
            if (!entity.isDead()) {
                if (entity instanceof Player) {
                    player = (Player) entity;
                    player.give(item);
                } else if (entity instanceof LivingEntity) {
                    livingEntity = (LivingEntity) entity;
                    livingEntity.getEquipment().setItemInMainHand(item);
                }
            }
        }
        Component text = Component.text("Gave ").append(CommandUtils.getItemAsComponent(item));

        if (entities.size() > 1) {
            sender.sendMessage(text.append(Component.text(" to " + entities.size() + " entities")));
        }
        if (entities.size() == 1) {
            sender.sendMessage(text.append(Component.text(" to " + entities.getFirst().getName())));
        }
        if (entities.isEmpty()) {
            sender.sendMessage("No entity was found");
        }
    }

}
