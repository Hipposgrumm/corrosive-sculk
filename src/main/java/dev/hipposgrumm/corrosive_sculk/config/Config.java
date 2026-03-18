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

//? if forge {
import net.minecraftforge.fml.loading.FMLPaths;
//?} else {
/*import net.fabricmc.loader.api.FabricLoader;
*///?}

public class Config {
    private static final String[] OPTION_NAMES = {
            "Sculk Resistance Heals Sculk",
            "Sculk Resistance Grants Immunity"
    };
    private static final boolean[] OPTION_DEFAULTS = {
            false,  // Sculk Resistance Heals Sculk
            false   // Sculk Resistance Grants Immunity
    };

    static final File file;
    static final Watcher watcher;
    private static final HashMap<String, Boolean> config = new HashMap<>();

    static {
        //? if fabric {
        /*Path path = FabricLoader.getInstance().getConfigDir();
         *///?} elif forge {
        Path path = FMLPaths.CONFIGDIR.get();
        //?}
        file = path.resolve(CorrosiveSculk.MODID + ".toml").toFile();

        watcher = new Watcher(file);
    }

    public static boolean sculkResistHeals;
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

        refreshOptions();
    }

    static String writeConfig(boolean[] vals) {
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

        String contents = """
        ["Assist Mode"]
        # Options that may make the mod easier or not as punishing.
        
        """;
        contents += writeOption(OPTION_NAMES[0], vals[0], get.apply("gui.corrosive_sculk.config.option.sculk_resist_heals.desc"));
        contents += writeOption(OPTION_NAMES[1], vals[1], get.apply("gui.corrosive_sculk.config.option.sculk_resist_invul.desc"));
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
                                config.put(key.toString(), Boolean.valueOf(entry));
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
        sculkResistHeals = config.getOrDefault(OPTION_NAMES[0], OPTION_DEFAULTS[0]);
        sculkResistInvul = config.getOrDefault(OPTION_NAMES[1], OPTION_DEFAULTS[1]);
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
}