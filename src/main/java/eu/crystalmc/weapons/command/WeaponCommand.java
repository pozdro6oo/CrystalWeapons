package eu.crystalmc.weapons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.WeaponsRegistry;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.config.JsonConfig;
import eu.crystalmc.weapons.config.WeaponConfig;
import eu.crystalmc.weapons.utils.CommandUtils;
import eu.crystalmc.weapons.utils.NBTUtils;
import eu.crystalmc.weapons.utils.WeaponUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class WeaponCommand {
    public LiteralCommandNode<CommandSourceStack> getCommand(CrystalWeapons plugin) {
        Config.Data config = plugin.getConfiguration();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("weapon")
            .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.command))
            .then(Commands.literal("give")
                    .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.give))
                    .then(Commands.argument("entities", ArgumentTypes.entities())
                        .then(Commands.argument("weapon", StringArgumentType.word())
                                .suggests( (_, builder) -> suggestWeapons(builder))
                                .executes(context -> {
                                    giveWeapon(context);
                                    return Command.SINGLE_SUCCESS;
                                })
                    ))
            )
            .then(getModifyArgument(plugin, config))
            .then(Commands.literal("save")
                    .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.save))
                    .then(Commands.argument("weapon", StringArgumentType.word())
                            .suggests( (_, builder) -> suggestWeapons(builder))
                            .executes(context -> {
                                CommandSender sender = context.getSource().getSender();
                                String weapon = context.getArgument("weapon", String.class);

                                File file = plugin.getDataFolder().toPath().resolve("weapons").resolve(weapon + ".json").toFile();


                                if (sender instanceof Player) {
                                    ItemStack item = ((Player) sender).getEquipment().getItem(EquipmentSlot.HAND);
                                    if (item.getType() == Material.AIR) {
                                        sender.sendMessage(Component.text("You are not holding an item").color(NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    NBT.modify(item, nbt -> {
                                        ReadWriteNBT compound = WeaponUtils.getNBT(nbt);
                                        if (compound != null) {
                                            WeaponConfig weaponConfig = NBTUtils.nbtToObject(compound, WeaponConfig.class);
                                            JsonConfig<WeaponConfig>  json = new JsonConfig<>(file, WeaponConfig.class, weaponConfig);
                                            sender.sendMessage("Saved weapon '" + weapon + "' (changes will be applied on next server start)");
                                            json.save();
                                        } else {
                                            sender.sendMessage(Component.text("Item is not a weapon").color(NamedTextColor.RED));
                                        }
                                    });

                                } else {
                                    sender.sendMessage(Component.text("A player is required to run this command here").color(NamedTextColor.RED));
                                }

                                return Command.SINGLE_SUCCESS;
                            })
                    )
            )
                ;


        return command.build();
    }

    private void giveWeapon(CommandContext<CommandSourceStack> context) {
        String ammunition = context.getArgument("weapon", String.class);
        ItemStack itemStack = WeaponsRegistry.getWeapon(ammunition);
        CommandUtils.giveItem(context, itemStack, 1);
    }



    private LiteralArgumentBuilder<CommandSourceStack> getModifyArgument(CrystalWeapons plugin, Config.Data config) {
        LiteralArgumentBuilder<CommandSourceStack> argument = Commands.literal("modify")
                .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.modify));

        for (Property propery : Property.values()) {
            argument.then(propery.getArgument());
        }

        return argument;
    }



    private CompletableFuture<Suggestions> suggestWeapons(SuggestionsBuilder builder) {
        WeaponsRegistry.getItems().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }


    private enum Property {
        infinite_ammunition(BoolArgumentType.bool(), Boolean.class),
        automatic(BoolArgumentType.bool(), Boolean.class),
        synchronize_weapon_data(BoolArgumentType.bool(), Boolean.class),
        magazine_size(IntegerArgumentType.integer(1), int.class),
        max_distance(DoubleArgumentType.doubleArg(0), double.class),
        shot_delay(IntegerArgumentType.integer(1),int.class),
        recoil(FloatArgumentType.floatArg(),float.class),
        damage(DoubleArgumentType.doubleArg(0), double.class),
        inaccuracy(DoubleArgumentType.doubleArg(0), double.class),
        reload_time(IntegerArgumentType.integer(0),int.class),
        bullets_per_shot(IntegerArgumentType.integer(1), int.class);



        private final String path;
        private final Class<?> tClass;
        private final ArgumentType<?> type;


        <T> Property(String path, ArgumentType<T> type, Class<T> tClass) {
            this.path = path;
            this.type = type;
            this.tClass = tClass;
        }

        <T> Property(ArgumentType<T> type, Class<T> tClass) {
            this.type = type;
            this.path = this.name();
            this.tClass = tClass;
        }



        private <T> LiteralArgumentBuilder<CommandSourceStack> getArgument() {

            return Commands.literal(this.name())
                    .then(Commands.argument("value", this.type)
                    .executes(context -> {

                        CommandSender sender = context.getSource().getSender();

                        @SuppressWarnings("unchecked cast")
                        T object = (T) context.getArgument("value", this.tClass);

                        if (sender instanceof Player) {
                            ItemStack item = ((Player) sender).getEquipment().getItem(EquipmentSlot.HAND);
                            if (item.getType() == Material.AIR) {
                                sender.sendMessage(Component.text("You are not holding an item").color(NamedTextColor.RED));
                                return Command.SINGLE_SUCCESS;
                            }
                            NBT.modify(item, nbt -> {
                                ReadWriteNBT compound = WeaponUtils.getNBT(nbt);
                                if (compound != null) {
                                    boolean success = true;
                                    if (tClass == int.class || tClass == Integer.class) {
                                        compound.setInteger(this.path, ((Number) object).intValue());
                                    } else if (tClass == double.class || tClass == Double.class) {
                                        compound.setDouble(this.path, ((Number) object).doubleValue());
                                    } else if (tClass == float.class || tClass == Float.class) {
                                        compound.setFloat(this.path, ((Number) object).floatValue());
                                    } else if (tClass == boolean.class || tClass == Boolean.class) {
                                        compound.setBoolean(this.path, (Boolean) object);
                                    } else if (tClass == String.class) {
                                        compound.setString(this.path, (String) object);
                                    } else {
                                        success = false;
                                    }

                                    if (success) {
                                        sender.sendMessage(Component.text("Property ")
                                                .append(Component.text(this.name()).hoverEvent(HoverEvent.showText(Component.text(this.path))))
                                                .append(Component.text(" of "))
                                                .append(CommandUtils.getItemAsComponent(item))
                                                .append(Component.text(" is now set to "))
                                                .append(Component.text(object.toString()))

                                        );
                                    }


                                } else {
                                    sender.sendMessage(Component.text("Item is not a weapon").color(NamedTextColor.RED));
                                    return;
                                }
                            });

                        } else {
                            sender.sendMessage(Component.text("A player is required to run this command here").color(NamedTextColor.RED));
                        }


                        return Command.SINGLE_SUCCESS;
                    }));
        }

    }

}
