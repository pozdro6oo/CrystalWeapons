package eu.crystalmc.weapons;

import eu.crystalmc.weapons.command.AmmunitionCommand;
import eu.crystalmc.weapons.command.RemoveBulletHitDecalsCommand;
import eu.crystalmc.weapons.command.WeaponCommand;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.listener.DisableItemCrafting;
import eu.crystalmc.weapons.listener.SynchronizeDataWithConfiguration;
import eu.crystalmc.weapons.listener.modifier.AddDamageModifier;
import eu.crystalmc.weapons.listener.modifier.ApplyFireModifier;
import eu.crystalmc.weapons.resourcepack.ResourcepackCreator;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public final class CrystalWeapons extends JavaPlugin {

    public final static String WEAPON_COMPOUND = "crystalmc:weapon";
    public final static String AMMUNITION_COMPOUND = "crystalmc:ammunition";

    public final static String RUNTIME = UUID.randomUUID().toString();

    @NotNull
    Path configPath = this.getDataPath().resolve("config.yml");

    Config config;

    @Override
    public void onEnable() {

        // save default resources on first start of the plugin
        setupDefaultConfiguration();

        // load or create config.yml
        config = new Config(this.configPath);


        //  register event listener
        //  this one does not need any priority
        Bukkit.getPluginManager().registerEvents(new WeaponLogic(this), this);
        Bukkit.getPluginManager().registerEvents(new DisableItemCrafting(), this);

        registerModifiers();

        if (getConfiguration().isSynchronizeItemWithConfiguration()) {
            Bukkit.getPluginManager().registerEvents(new SynchronizeDataWithConfiguration(this), this);
        }

        // register command
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    new WeaponCommand().getCommand(this)
            );
            event.registrar().register(
                    new AmmunitionCommand().getCommand(this)
            );
            event.registrar().register(
                    new RemoveBulletHitDecalsCommand().getCommand(config.data.permissions.removeBulletDecals)
            );
        });

        BulletHitDecalsManager.startGarbageCollector(this, config.data.weapons.visual.bulletHitDecal.cleanUnusedDecalsInterval);


        //  build and pack resourcepack
        ResourcepackCreator builder = new ResourcepackCreator(this);
        builder.init();
    }

    @Override
    public void onDisable() {
        removeAllBulletHitDecals();
        Bukkit.getScheduler().cancelTasks(this);
    }

    public boolean hasPermission(CommandSender player, String permission) {
        return (player.hasPermission(permission) || permission.equals("default"));
    }



    public void registerModifiers() {
        Bukkit.getPluginManager().registerEvents(new ApplyFireModifier(), this);
        Bukkit.getPluginManager().registerEvents(new AddDamageModifier(), this);
    }



    public static void removeAllBulletHitDecals() {
        BulletHitDecalsManager.removeAll();
    }

    public static ItemStack getWeapon(String weaponId) {
        return WeaponsRegistry.getWeapon(weaponId);
    }

    public static Set<String> getWeapons() {
        return WeaponsRegistry.getItems().keySet();
    }











    public void sendWarning(String message) {
        log(message, NamedTextColor.YELLOW);
    }

    public void sendError(String message) {
        log(message, NamedTextColor.RED);
    }

    public void sendInfo(String message) {
        log(message, NamedTextColor.WHITE);
    }

    private void log(String message, NamedTextColor color) {
        Bukkit.getConsoleSender().sendMessage(getLogPrefix().append(Component.text(message).color(color)));
    }

    private Component getLogPrefix() {
        return
                Component.text("[").color(NamedTextColor.DARK_GRAY)
                .append(Component.text("Crystal").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("Weapons").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text("] ").color(NamedTextColor.DARK_GRAY));
    }







    public @NotNull Config.Data getConfiguration() {
        return config.data;
    }

    public void reloadConfiguration() {
        config.reload(configPath);
    }

    public void saveConfiguration() {
        config.save(configPath);
    }








    private Boolean isSetup() {
        return !configPath.toFile().exists();
    }

    private void setupDefaultConfiguration() {
        if (this.isSetup()) {
            this.saveResource("assets/models/weapon/default.json", false);
            this.saveResource("assets/models/weapon_item/default.json", false);
            this.saveResource("assets/models/ammunition_type/default.json", false);

            setupDefaultFireAnimations("default");
            setupDefaultFireAnimations("taser");
            setupDefaultFireAnimations("laser");

            setupDefaultWeapon("ak47");
            setupDefaultWeapon("taser", false);
            setupDefaultWeapon("mag7");
            setupDefaultWeapon("glock18");

            setupDefaultWeapon("m4a4");
            setupDefaultWeapon("m4a1-s");
            setupDefaultWeapon("sg553");

            setupDefaultAmmunitionType("rifle");
            setupDefaultAmmunitionType("heavy_rifle");
            setupDefaultAmmunitionType("incendiary_rifle");

            setupDefaultAmmunitionType("pistol");

            setupDefaultAmmunitionType("sniper");
            setupDefaultAmmunitionType("heavy_sniper");



            this.saveResource("tutorial.txt", false);
            this.saveResource("assets/textures/bullet_hit.png", false);
        }


        String bulletHitModel = "assets/models/bullet_hit.json";
        if (!this.getDataPath().resolve(bulletHitModel).toFile().exists()) {
            this.saveResource(bulletHitModel, false);
        }
    }

    private void setupDefaultWeapon(String weapon) {
        setupDefaultWeapon(weapon, true);
    }

    private void setupDefaultWeapon(String weapon, boolean reloadSound) {
        this.saveResource("weapons/" + weapon + ".png", false);
        this.saveResource("weapons/" + weapon + ".json", false);

        if (reloadSound) {
            this.saveResource("assets/sounds/" + weapon + ".reload.ogg", false);
        }
        this.saveResource("assets/sounds/" + weapon + ".shoot.ogg", false);
    }

    private void setupDefaultAmmunitionType(String weapon) {
        this.saveResource("ammunition_types/" + weapon + ".png", false);
        this.saveResource("ammunition_types/" + weapon + ".json", false);
    }

    private void setupDefaultFireAnimations(String fireAnimation) {
        this.saveResource("assets/textures/weapon_fire/" + fireAnimation + ".png", false);
        this.saveResource("assets/models/weapon_fire/" + fireAnimation + ".json", false);
    }

}
