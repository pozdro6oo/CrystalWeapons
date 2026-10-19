package eu.crystalmc.weapons.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import eu.crystalmc.weapons.AmmunitionTypesRegistry;
import eu.crystalmc.weapons.CrystalWeapons;
import eu.crystalmc.weapons.Weapon;
import eu.crystalmc.weapons.WeaponsRegistry;
import eu.crystalmc.weapons.config.AmmunitionTypeConfig;
import eu.crystalmc.weapons.config.Config;
import eu.crystalmc.weapons.config.JsonConfig;
import eu.crystalmc.weapons.config.WeaponConfig;
import eu.crystalmc.weapons.config.resourcepack.FireAnimationTexture;
import eu.crystalmc.weapons.config.resourcepack.Item;
import eu.crystalmc.weapons.config.resourcepack.Model;
import eu.crystalmc.weapons.utils.*;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class ResourcepackBuilder {
    private final CrystalWeapons plugin;
    private final Config.Data config;

    public ResourcepackBuilder(CrystalWeapons plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
    }


    public void init() {
        File outputFolder = getResourcepackFolder().toFile();
        Path crystalFolder = getCrystalmcFolder();



        try {
            FileUtils.forceMkdir(outputFolder);

            FileUtils.cleanDirectory(outputFolder);

            Path assets = outputFolder.toPath().resolve("assets");

            Files.createDirectories(assets);

            Files.createDirectories(assets);
            Files.createDirectories(crystalFolder.resolve("items"));
            Files.createDirectories(crystalFolder.resolve("sounds"));
            Files.createDirectories(crystalFolder.resolve("textures").resolve("item").resolve("weapon"));
            Files.createDirectories(crystalFolder.resolve("textures").resolve("item").resolve("ammunition_type"));
            Files.createDirectories(crystalFolder.resolve("models"));
            Files.createDirectories(crystalFolder.resolve("textures"));

        } catch (IOException e) {
            plugin.sendError("Cannot create directories");
            return;
        }


        plugin.saveResource("resourcepack_output/pack.png", true);
        plugin.saveResource("resourcepack_output/pack.mcmeta", true);

        createWeapons();
        createAmmunitionTypes();

        createSounds();

        copySubAssets("textures", "item");
        copySubAssets("models", null);

        createBulletHitItem();
        createFireAnimationTextures();


        File resourcepackOutput = outputFolder.toPath().getParent().resolve("resourcepack" + ".zip").toFile();

        pack(outputFolder, resourcepackOutput);

        host(resourcepackOutput);

        try {
            FileUtils.cleanDirectory(outputFolder);
            Files.delete(outputFolder.toPath());
        } catch (IOException e) {
            plugin.sendError("Cannot delete output folder");
        }
    }


    @FunctionalInterface
    interface FileConfig<T> {
        void apply(String name, T jsonConfig, File file);
    }

    private <T> void createFiles(String folder, String outputFolder, FileConfig<T> logic, Class<T> configClass) {
        File directory = plugin.getDataPath().resolve(folder).toFile();

        try {
            FileUtils.forceMkdir(directory);
        } catch (IOException e) {
            plugin.sendError("Cannot create directory '" + folder + "'");
        }

        int count = 0;



        File[] files = directory.listFiles((_, name) -> name.endsWith(".json"));

        if (files == null) {
            plugin.sendWarning("No " + folder + " found");
            return;
        }

        File texture;
        String name;
        JsonConfig<T> config;

        for (File file : files) {
            name = file.getName().toLowerCase();
            if (name.endsWith(".json")) {
                texture = directory.toPath().resolve(name.replace(".json", ".png")).toFile();
                if (texture.exists()) {
                    config = new JsonConfig<>(file, configClass);
                    name = name.replaceFirst("[.][^.]+$", "");
                    config.load();
                    boolean generated = copyTexture(texture, outputFolder);
                    config.save();
                    if (generated) {
                        logic.apply(name, config.config, file);
                        count++;
                    } else {
                        plugin.sendWarning("Failed to register " + outputFolder + " '" + name + "'");
                    }
                }
            }
        }
        String info = "Registered " + folder + ": " + count;
        if (count > 0) {
            plugin.sendInfo(info);
        } else {
            plugin.sendWarning(info);
        }
    }

    private void createWeapons() {
        createFiles("weapons", "weapon", this::generateWeapon, WeaponConfig.class);
    }
    private void createAmmunitionTypes() {
        createFiles("ammunition_types", "ammunition_type", this::generateAmmunitionType, AmmunitionTypeConfig.class);
    }


    private boolean copyTexture(File texture, String folder) {
        try {
            Files.copy(texture.toPath(), getCrystalmcFolder().resolve("textures").resolve("item").resolve(folder).resolve(texture.getName()), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            plugin.sendError("Failed to copy " + folder + " texture");
            return false;
        }
    }








    private void generateAmmunitionType(String name, AmmunitionTypeConfig ammunitionType, File texture) {
        ItemStack item = createAmmunitionTypeItemStack(name, ammunitionType);

        createModel(name, "ammunition_type", ammunitionType.getModel());
        createAmmunitionTypeItem(name);

        AmmunitionTypesRegistry.registerAmmunitionType(name, item);
    }

    private void generateWeapon(String name, WeaponConfig config, File texture) {
        ItemStack item = createWeaponItemStack(name, config);

        createModel(name, "weapon", config.getModel());
        createWeaponItem(name, config);

        WeaponsRegistry.registerWeapon(name, item);
    }










    private ItemStack createWeaponItemStack(String name, WeaponConfig weapon) {
        Material material = config.getItemMaterial();

        ItemUseAnimation animation = config.getShootAnimation();

        // create item
        ItemStack item = new ItemStack(material);


        ReadWriteNBT compound = NBTUtils.nbtFromObject(weapon);
        compound.setInteger(Weapon.Key.AMMUNITION.key, weapon.getMaxAmmunition());
        compound.setBoolean(Weapon.Key.SYNCHRONIZE_WEAPON_DATA.key, true);
        compound.setString(Weapon.Key.RUNTIME.key, CrystalWeapons.RUNTIME);
        compound.setString(Weapon.Key.ID.key, name);

        NBT.modify(item, nbt -> {
                nbt.getOrCreateCompound(CrystalWeapons.WEAPON_COMPOUND).mergeCompound(compound);
            });

        ItemMeta meta = item.getItemMeta();


        Component itemName = Component.text(weapon.getDisplayName()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        meta.itemName(itemName);
        meta.customName(itemName);
        meta.setItemModel(new NamespacedKey("crystalmc", "weapon/" + name));

        Double fireRate = Math.floor((20.0 / weapon.getShotDelay()) * 1000) / 1000;
        Double accuracy = 1 - weapon.getInaccuracy();

        meta.lore(List.of(
            getLoreLine("Ammunition: " + weapon.getMaxAmmunition() + "/" + weapon.getMaxAmmunition()),
            Component.empty(),
            getLoreLine("Statistics:", NamedTextColor.GRAY),
            getLoreLine(weapon.getDamage() + " Damage"),
            getLoreLine(fireRate + " Fire Rate"),
            getLoreLine(accuracy + " Accuracy"),
            getLoreLine(weapon.getRecoil() + " Recoil")
            )
        );

        meta.setMaxStackSize(1);
        item.setItemMeta(meta);
        WeaponUtils.setAnimation(item, animation);

        return item;
    }

    private ItemStack createAmmunitionTypeItemStack(String name, AmmunitionTypeConfig ammunitionTypeConfig) {
        Material material = config.getItemMaterial();

        // Create item
        ItemStack item = new ItemStack(material);

        NBT.modify(item, nbt -> {
            nbt.getOrCreateCompound(CrystalWeapons.AMMUNITION_COMPOUND)
                    .setString(AmmunitionTypeConfig.Key.ID.key, name);
        });

        ItemMeta meta = item.getItemMeta();


        Component itemName = Component.text(ammunitionTypeConfig.getDisplayName()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        meta.itemName(itemName);
        meta.customName(itemName);
        meta.setItemModel(new NamespacedKey("crystalmc", "ammunition_type/" + name));

        meta.setMaxStackSize(config.getMaxMagazineItemStackSize());
        item.setItemMeta(meta);

        return item;
    }
















    private Component getLoreLine(String string, NamedTextColor color) {
        return Component.text(string).decoration(TextDecoration.ITALIC, false).color(color);
    }
    private Component getLoreLine(String string) {
        return getLoreLine(string, NamedTextColor.BLUE);
    }


















    private void createModel(String name, String folder, String modelName) {
        File modelFile = getCrystalmcFolder().resolve("models").resolve(folder).resolve(name + ".json").toFile();

        JsonConfig<Model> model = new JsonConfig<>(modelFile, Model.class);

        model.load();
        model.config.textures.layer0 = "crystalmc:item/"+folder+"/" + name;
        model.config.parent = "crystalmc:"+folder+"/" + modelName;

        model.save();
    }


    private void createItem(String name, String folder) {
        File modelFile = getCrystalmcFolder().resolve("items").resolve(folder).resolve(name + ".json").toFile();

        JsonConfig<Item> model = new JsonConfig<>(modelFile, Item.class);

        model.load();
        model.config.model.type = "minecraft:model";
        model.config.model.model = "";
        model.config.model.model = "crystalmc:" +folder+ "/" + name;
        model.save();

    }




    private void createWeaponItem(String name, WeaponConfig weaponConfig) {
        File modelFireFile = getCrystalmcFolder().resolve("items").resolve("weapon").resolve(name + "_fire.json").toFile();

        JsonConfig<Item> modelFire = new JsonConfig<>(modelFireFile, Item.class);

        modelFire.load();
        modelFire.config.model.type = "minecraft:composite";
        modelFire.config.model.model = "";
        modelFire.config.model.models = List.of(
                new Item.CustomModel("minecraft:model", "crystalmc:weapon/" + name),
                new Item.CustomModel("minecraft:model", "crystalmc:weapon_fire/" + weaponConfig.getGunFireAnimationModel())
        );
        modelFire.save();


        createItem(name, "weapon");
    }

    private void createAmmunitionTypeItem(String name) {
        createItem(name, "ammunition_type");
    }
















    private void createBulletHitItem() {
        File modelFile = getCrystalmcFolder().resolve("items").resolve("bullet_hit.json").toFile();

        JsonConfig<Item> model = new JsonConfig<>(modelFile, Item.class);

        model.load();
        model.config.model.type = "minecraft:model";
        model.config.model.model = "";
        model.config.model.model = "crystalmc:bullet_hit";
        model.save();
    }




    private void createFireAnimationTextures() {
        Path folder = plugin.getDataPath().resolve("assets").resolve("textures").resolve("weapon_fire");

        Path outputFolder = getCrystalmcFolder().resolve("textures").resolve("item").resolve("weapon_fire");

        File[] files = folder.toFile().listFiles((_, name) -> name.endsWith(".png"));

        if (files == null) {
            plugin.sendWarning("No fire animations found");
            return;
        }

        File textureFile;
        for (File file : files) {
            textureFile = outputFolder.resolve(file.getName() + ".mcmeta").toFile();

            JsonConfig<FireAnimationTexture> texture = new JsonConfig<>(textureFile, FireAnimationTexture.class);
            texture.save();
        }

    }




    private void createSounds() {
        Path folder = plugin.getDataPath().resolve("assets").resolve("sounds");

        Path outputFolder = getCrystalmcFolder().resolve("sounds");

        File[] files = folder.toFile().listFiles((_, name) -> name.endsWith(".ogg"));

        if (files == null) {
            plugin.sendWarning("No sounds found");
            return;
        }

        Path output;
        for (File file : files) {
            try {
                output = outputFolder.resolve(file.getName()).toAbsolutePath();
                Files.copy(file.toPath().toAbsolutePath(), output);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        createSoundsFile(files);

    }
    private void createSoundsFile(File[] sounds) {
        Path output = getCrystalmcFolder().resolve("sounds.json").toAbsolutePath();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject json = new JsonObject();

        JsonObject sound;
        List<String> soundsList;

        String soundName;

        for (File file : sounds) {
            soundName = file.getName().replaceFirst("[.][^.]+$", "");
            sound = new JsonObject();
            soundsList = List.of("crystalmc:" + soundName);



            sound.add("sounds", gson.toJsonTree(soundsList));


            json.add("weapon."+ soundName, sound);
        }



        try {
            Files.writeString(output, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.sendError("Error while writing '" + output + "'.");
        }
    }

    private void copySubAssets(String subFolder, String add) {
        Path crystalFolder = getCrystalmcFolder();

        File folder = plugin.getDataPath().resolve("assets").resolve(subFolder).toFile();


        Path outputFolder = crystalFolder.resolve(subFolder);

        if (add != null) {
            outputFolder = outputFolder.resolve(add);
        }

        try {
            Files.createDirectories(outputFolder);

            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isDirectory()) {
                    copyEverything(file.toPath(), outputFolder.resolve(file.getName()));
                } else {
                    Path output = outputFolder.resolve(file.getName());
                    Files.copy(file.toPath(), output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            plugin.sendError("Error while copying files. ('" + subFolder + "')");
        }
    }




    private void host(File resourcepack) {
        Config.Data data = plugin.getConfiguration();
        try {
            switch (data.resourcepack.mode) {
                case "upload" -> {
                    hostUpload(resourcepack, data.resourcepack.modeUpload);
                }
                case "provided" -> {
                    return;
                }
                // auto
                default -> {
                    hostAuto(resourcepack, data.resourcepack.modeAuto);
                    String address = "http://" + data.resourcepack.modeAuto.address + ":" + data.resourcepack.modeAuto.port + "/" + resourcepack.getName();
                    addResourcepack(address, resourcepack.toPath());
                }
            }
        } catch (IOException e) {
            plugin.sendError("Error while trying to host/upload resourcepack");
        } catch (URISyntaxException | NoSuchAlgorithmException e) {
            plugin.sendError("Error while trying add resourcepack");
        }
    }

    private void hostAuto(File resourcepack, Config.Data.Resourcepack.ModeAuto config) throws IOException {
        new BukkitRunnable() {
            @Override
            public void run() {
                HTTPServer server = new HTTPServer();
                server.setPath(resourcepack.toPath().toAbsolutePath());
                try {
                    server.host(config.port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskAsynchronously(plugin);

    }

    private void hostUpload(File resourcepack, Config.Data.Resourcepack.ModeUpload config) throws IOException {
        FTPUtils.uploadFile(config.host, config.user, config.password, resourcepack.getAbsolutePath(), config.uploadFile);
    }






    private void pack(File folder, File output) {
        try {
            ZIPUtils.zipFolder(folder, output);
        } catch (IOException e) {
            plugin.sendError("Error while packing resourcepack");
            return;
        }

        try {
            for (String path : plugin.getConfiguration().resourcepack.copyResourcepackOutputTo) {
                Files.copy(output.toPath(), Path.of(path), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            plugin.sendError("Error while coping resourcepack outputs");
        }
    }

    private Path getResourcepackFolder() {
        return plugin.getDataPath().resolve("resourcepack_output");
    }

    private Path getCrystalmcFolder() {
        return getResourcepackFolder().resolve("assets").resolve("crystalmc");
    }


    private void copyEverything(Path source, Path target) throws IOException {
        Files.createDirectories(target);

        try (Stream<Path> files = Files.list(source)) {
            for (Path file : files.toList()) {
                Path output = target.resolve(file.getFileName());

                if (Files.isDirectory(file)) {
                    copyEverything(file, output);
                } else {
                    Files.copy(file, output, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }










    private void addResourcepack(String address, Path path) throws URISyntaxException, NoSuchAlgorithmException, IOException {
        UUID uuid = UUID.randomUUID();

        ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
                .id(uuid)
                .hash(getSHA1(path))
                .uri(new URI(address))
                .build();


        ResourcePackRequest packRequest = ResourcePackRequest.resourcePackRequest()
                .packs(packInfo)
                .required(true)
                .prompt(Component.text("Please download resourcepack"))
                .build();


        Bukkit.getPluginManager().registerEvents(new Send(packRequest), plugin);
    }


    public static String getSHA1(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}
