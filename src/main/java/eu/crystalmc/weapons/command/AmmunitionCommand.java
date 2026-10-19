package eu.crystalmc.weapons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.crystalmc.weapons.AmmunitionTypesRegistry;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.utils.CommandUtils;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public class AmmunitionCommand {
    public LiteralCommandNode<CommandSourceStack> getCommand(CrystalWeapons plugin) {
        Config.Data config = plugin.getConfiguration();

        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ammunition")
                .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.command))
                .then(Commands.literal("give")
                        .requires(source -> plugin.hasPermission(source.getSender(), config.permissions.give))
                        .then(Commands.argument("entities", ArgumentTypes.entities())
                                .then(Commands.argument("ammunition-type", StringArgumentType.word())
                                        .suggests( (_, builder) -> suggestAmmunitionType(builder))
                                        .executes(context -> {

                                            giveAmmunitionType(context, 1);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                                .then(Commands.argument("count", IntegerArgumentType.integer(0))
                                        .executes(context -> {

                                            int count = context.getArgument("count", int.class);

                                            giveAmmunitionType(context, count);
                                            return Command.SINGLE_SUCCESS;


                                        })
                                )
                        )
                );


        return command.build();
    }

    private void giveAmmunitionType(CommandContext<CommandSourceStack> context, int count) {
        String ammunition = context.getArgument("ammunition-type", String.class);
        ItemStack itemStack = AmmunitionTypesRegistry.getAmmunitionType(ammunition);
        CommandUtils.giveItem(context, itemStack, count);
    }

    private CompletableFuture<Suggestions> suggestAmmunitionType(SuggestionsBuilder builder) {
        AmmunitionTypesRegistry.getItems().keySet().forEach(builder::suggest);
        return builder.buildFuture();
    }

}


