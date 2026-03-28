package dev.hipposgrumm.corrosive_sculk.config;

import dev.hipposgrumm.corrosive_sculk.CorrosiveSculk;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class ConfigScreen {
    private static Object[] vals = new Object[3];

    public static Screen create(Screen parent) {
        vals = new Object[] {
                Config.sculkWarnSound,
                Config.sculkHealCircumstance,
                Config.sculkResistInvul
        };

        ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.translatable("gui.corrosive_sculk.config.title"));

        if (Config.file.exists()) {
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            /*
            builder.getOrCreateCategory(Component.empty())
                    .addEntry(entryBuilder
                            .startBooleanToggle(Component.translatable("gui.corrosive_sculk.config.option.sculk_warn_sound"), Config.sculkWarnSound)
                            .setTooltip(Component.translatable("gui.corrosive_sculk.config.option.sculk_warn_sound.desc"))
                            .setDefaultValue(true)
                            .setSaveConsumer(val -> {
                                vals[0] = val;
                                Config.sculkWarnSound = val;
                                save();
                            }).build()
                    );
            */

            builder.getOrCreateCategory(Component.translatable("gui.corrosive_sculk.config.category.assist_mode"))
                    .addEntry(entryBuilder
                            .startEnumSelector(Component.translatable("gui.corrosive_sculk.config.option.healing_circumstance"), Config.HealingCircumstance.class, Config.sculkHealCircumstance)
                            .setTooltip(Component.translatable("gui.corrosive_sculk.config.option.healing_circumstance.desc")
                                    .append("\nNORMAL - ").append(Component.translatable("gui.corrosive_sculk.config.option.healing_circumstance.desc.0"))
                                    .append("\nRESIST - ").append(Component.translatable("gui.corrosive_sculk.config.option.healing_circumstance.desc.1"))
                                    .append("\nALWAYS - ").append(Component.translatable("gui.corrosive_sculk.config.option.healing_circumstance.desc.2"))
                            ).setDefaultValue(Config.HealingCircumstance.NORMAL)
                            .setSaveConsumer(val -> {
                                vals[1] = val;
                                Config.sculkHealCircumstance = val;
                                save();
                            }).build()
                    ).addEntry(entryBuilder
                            .startBooleanToggle(Component.translatable("gui.corrosive_sculk.config.option.sculk_resist_invul"), Config.sculkResistInvul)
                            .setTooltip(Component.translatable("gui.corrosive_sculk.config.option.sculk_resist_invul.desc"))
                            .setDefaultValue(false)
                            .setSaveConsumer(val -> {
                                vals[2] = val;
                                Config.sculkResistInvul = val;
                                save();
                            }).build()
                    );
        } else {
            builder.alwaysShowTabs().getOrCreateCategory(Component.translatable("gui.corrosive_sculk.config.absent"));
        }

        return builder.build();
    }

    private static void save() {
        try {
            String contents = Config.writeConfig(vals);
            Config.watcher.skipUpdate();
            Config.changeConfig(contents);
        } catch (IOException e) {
            CorrosiveSculk.LOGGER.error("Error saving to config file {}.toml", CorrosiveSculk.MODID, e);
        }
    }
}