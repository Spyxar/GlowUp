package com.spyxar.glowup.config;

import com.spyxar.glowup.GlowUpMod;
import me.shedaniel.clothconfig2.api.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class ClothConfigScreenFactory
{
    public static Screen buildConfigScreen(Screen parent)
    {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.glowup.title"))
                .setSavingRunnable(GlowUpMod.config::saveConfig);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("config.glowup.categories.general"));
        general.addEntry(entryBuilder.startColorField(Text.translatable("config.glowup.option.glowcolor"), GlowUpMod.config.glowColor)
                .setDefaultValue(16777215)
                .setTooltip(Text.translatable("config.glowup.description.glowcolor"))
                .setSaveConsumer(newValue -> GlowUpMod.config.glowColor = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.glowup.option.togglemessageonoverlay"), GlowUpMod.config.toggleMessageOnOverlay)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.glowup.description.togglemessageonoverlay"))
                .setSaveConsumer(newValue -> GlowUpMod.config.toggleMessageOnOverlay = newValue)
                .build());
        general.addEntry(entryBuilder.startStrList(Text.translatable("config.glowup.option.items"), GlowUpMod.config.items)
                .setDefaultValue(Arrays.asList("diamond", "ancient_debris"))
                .setTooltip(Text.translatable("config.glowup.description.items"))
                .setExpanded(true)
                .setSaveConsumer(list -> {
                    ArrayList<String> items;
                    items = new ArrayList<>(list.stream().map(String::toLowerCase).filter(s -> !s.trim().isEmpty()).distinct().toList());
                    GlowUpMod.config.items = items;
                })
                .build());
        return builder.build();
    }
}