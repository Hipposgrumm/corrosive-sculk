package dev.hipposgrumm.corrosive_sculk.config;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import net.minecraft.locale.Language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

//? if neoforge {
/*import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
*///?} elif forge {
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
//?} else {
/*import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
*///?}

public class Config {
    private static final String[] OPTION_NAMES = {
            "Sculk Warning Sound", // Sculk warning sound is unavailable because I couldn't make anything that didn't sound obnoxious
            "Sculk Healing Circumstance",
            "Sculk Resistance Grants Immunity"
    };
    private static final Object[] OPTION_DEFAULTS = {
            true,                       // Sculk Warning Sound
            HealingCircumstance.NORMAL, // Sculk Healing Circumstance
            false                       // Sculk Resistance Grants Immunity
    };

    static final File file;
    static final Watcher watcher;
    private static final HashMap<String, Object> config = new HashMap<>();

    static {
        Path path =
                //? if neoforge {
                /*FMLPaths.CONFIGDIR.get();
                *///?} elif forge {
                FMLPaths.CONFIGDIR.get();
                //?} else {
                /*FabricLoader.getInstance().getConfigDir();
                *///?}
        file = path.resolve(CorrosiveSculk.MODID + ".toml").toFile();

        watcher = new Watcher(file);
    }

    public static boolean sculkWarnSound;
    public static HealingCircumstance sculkHealCircumstance;
    public static boolean sculkResistInvul;

    public static void registerConfig() {
        try {
            file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                String contents = writeConfig(OPTION_DEFAULTS);
                changeConfig(contents);
            }
        } catch (SecurityException e) {
            CorrosiveSculk.LOGGER.warn("config/{} could not be created: No permission", file.getName());
        } catch (IOException e) {
            CorrosiveSculk.LOGGER.warn("config/{} could not be created!", file.getName());
        }

        watcher.start();

        loadConfig();
    }

    static String writeConfig(Object[] vals) {
        Language lang = Language.getInstance();
        Map<String, String> mine = new HashMap<>();

        try (InputStream file = Config.class.getResourceAsStream(String.format("/assets/%s/lang/en_us.json", CorrosiveSculk.MODID))) {
            if (file != null) Language.loadFromJson(file, mine::put);
        } catch (Exception ignored) {}

        Function<String, String> get = trans -> {
            if (lang.has(trans)) {
                return lang.getOrDefault(trans);
            } else {
                return mine.get(trans);
            }
        };

        String contents = "";

        //? if forgebase {
        if (FMLEnvironment.dist.isClient())
        //?} else {
        /*if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
        *///?}
            ;//contents += writeOption(OPTION_NAMES[0], vals[0], get.apply("gui.corrosive_sculk.config.option.sculk_warn_sound.desc"));
        contents += """
        ["Assist Mode"]
        # Options that may make the mod easier or not as punishing.
        
        """;
        contents += writeOption(OPTION_NAMES[1], vals[1], get.apply("gui.corrosive_sculk.config.option.healing_circumstance.desc")
                +"\nNORMAL - "+get.apply("gui.corrosive_sculk.config.option.healing_circumstance.desc.0")
                +"\nRESIST - "+get.apply("gui.corrosive_sculk.config.option.healing_circumstance.desc.1")
                +"\nALWAYS - "+get.apply("gui.corrosive_sculk.config.option.healing_circumstance.desc.2")
        );
        contents += writeOption(OPTION_NAMES[2], vals[2], get.apply("gui.corrosive_sculk.config.option.sculk_resist_invul.desc"));
        return contents;
    }

    private static String writeOption(String name, Object defaultValue, String description) {
        String contents = "";
        if (!description.isEmpty()) contents += String.format("# %s\n", description.replace("\n", "\n# "));
        contents += String.format("\"%s\" = %s\n\n", name, defaultValue);
        return contents;
    }

    static void changeConfig(String contents) throws IOException {
        PrintWriter writer = new PrintWriter(file);
        writer.write(contents);
        writer.close();
    }

    private static void loadConfig() {
        config.clear();
        for (int i=0;i<OPTION_NAMES.length;i++) {
            config.put(OPTION_NAMES[i], OPTION_DEFAULTS[i]);
        }
        try {
            Scanner reader = new Scanner(file);
            for(int l=1;reader.hasNextLine();l++) {
                String entry = reader.nextLine().trim();
                if(!entry.isEmpty() && !entry.startsWith("#") && !entry.startsWith("[")) {
                    if (entry.startsWith("\"")) {
                        StringBuilder key = new StringBuilder();
                        boolean building = true;
                        for (char c:entry.toCharArray()) {
                            if (!building && c == '"') {
                                building = true;
                                break;
                            } else if (building) {
                                building = false;
                            } else {
                                key.append(c);
                            }
                        }
                        if (building) {
                            entry = entry.substring(key.length()+2);
                            int index = entry.indexOf('=');
                            if (entry.indexOf('=') >= 0) {
                                entry = entry.substring(index+1).trim();
                                String keyString = key.toString();
                                if (!config.containsKey(keyString)) continue;
                                Class<?> objtype = config.get(keyString).getClass();
                                if (objtype == Boolean.class) {
                                    config.put(keyString, Boolean.valueOf(entry));
                                } else if (objtype == HealingCircumstance.class) {
                                    for (HealingCircumstance val:HealingCircumstance.values()) {
                                        if (val.name.equals(entry)) {
                                            config.put(keyString, val);
                                        }
                                    }
                                    // If none match, it has already been set to default, nothing bad happens.
                                } else throw new UnsupportedOperationException("class type "+objtype.getName()+" is not handled by config");
                            } else {
                                CorrosiveSculk.LOGGER.warn("Syntax error in config/{}: no value found on line {} (missing (\"=\"))", file.getName(), l);
                            }
                        } else {
                            CorrosiveSculk.LOGGER.warn("Syntax error in config/{}: Missing closing quote mark on line {}", file.getName(), l);
                        }
                    } else {
                        CorrosiveSculk.LOGGER.warn("Syntax error in config/{}: Missing opening quote mark on line {}", file.getName(), l);
                    }
                }
            }
            refreshOptions();
        } catch (IOException ignored) {}
    }

    private static void refreshOptions() {
        sculkWarnSound = (boolean) config.getOrDefault(OPTION_NAMES[0], OPTION_DEFAULTS[0]);
        sculkHealCircumstance = (HealingCircumstance) config.getOrDefault(OPTION_NAMES[1], OPTION_DEFAULTS[1]);
        sculkResistInvul = (boolean) config.getOrDefault(OPTION_NAMES[2], OPTION_DEFAULTS[2]);
    }

    public static class Watcher extends TimerTask {
        private final File file;
        private long timeStamp;

        public Watcher(File file) {
            this.file = file;
        }

        public void start() {
            skipUpdate();
            new Timer().schedule(this,new Date(),1000);
        }

        public void skipUpdate() {
            this.timeStamp = file.lastModified();
        }

        public void run() {
            long timeStamp = file.lastModified();

            if (this.timeStamp != timeStamp) {
                loadConfig();
                CorrosiveSculk.LOGGER.info("Config {} was updated!", file.getName());
                this.timeStamp = timeStamp;
            }
        }
    }

    public enum HealingCircumstance {
        NORMAL("NORMAL", true, false),
        RESIST("RESIST", true, true),
        ALWAYS("ALWAYS", false, true);

        public final String name;
        public final boolean hasSculkDamage;
        public final boolean resistDoesHealing;

        HealingCircumstance(String name, boolean hasSculkDamage, boolean resistDoesHealing) {
            this.name = name;
            this.hasSculkDamage = hasSculkDamage;
            this.resistDoesHealing = resistDoesHealing;
        }


        @Override
        public String toString() {
            return name;
        }
    }
}