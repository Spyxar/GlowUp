package com.spyxar.glowup.config;

import com.spyxar.glowup.GlowUpMod;
import me.shedaniel.clothconfig2.api.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClothConfigScreenFactory
{
    public static Screen buildConfigScreen(Screen parent)
    {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.glowup.title"))
                .setSavingRunnable(GlowUpMod.config::saveConfig);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.glowup.categories.general"));
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.glowup.option.isenabled"), GlowUpMod.config.isEnabled)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.glowup.description.isenabled"))
                .setSaveConsumer(newValue -> GlowUpMod.config.isEnabled = newValue)
                .build());
        general.addEntry(entryBuilder.startColorField(Component.translatable("config.glowup.option.glowcolor"), GlowUpMod.config.glowColor)
                .setDefaultValue(16777215)
                .setTooltip(Component.translatable("config.glowup.description.glowcolor"))
                .setSaveConsumer(newValue -> GlowUpMod.config.glowColor = newValue)
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.glowup.option.togglemessageonoverlay"), GlowUpMod.config.toggleMessageOnOverlay)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.glowup.description.togglemessageonoverlay"))
                .setSaveConsumer(newValue -> GlowUpMod.config.toggleMessageOnOverlay = newValue)
                .build());
        general.addEntry(entryBuilder.startStrList(Component.translatable("config.glowup.option.items"), GlowUpMod.config.items)
                .setDefaultValue(Arrays.asList("diamond", "ancient_debris"))
                .setTooltip(Component.translatable("config.glowup.description.items"))
                .setExpanded(true)
                .setSaveConsumer(list -> {
                    ArrayList<String> items;
                    items = new ArrayList<>(list.stream().map(String::toLowerCase).filter(s -> !s.trim().isEmpty()).distinct().toList());
                    GlowUpMod.config.items = items;
                })
                .build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.glowup.option.blacklistenabled"), GlowUpMod.config.blacklistEnabled)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.glowup.description.blacklistenabled"))
                .setSaveConsumer(newValue -> GlowUpMod.config.blacklistEnabled = newValue)
                .build());
        general.addEntry(entryBuilder.startStrList(Component.translatable("config.glowup.option.blacklisteditems"), GlowUpMod.config.blacklistedItems)
                .setTooltip(Component.translatable("config.glowup.description.blacklisteditems"))
                .setExpanded(true)
                .setSaveConsumer(list -> {
                    ArrayList<String> blacklistedItems;
                    blacklistedItems = new ArrayList<>(list.stream().map(String::toLowerCase).filter(s -> !s.trim().isEmpty()).distinct().toList());
                    GlowUpMod.config.blacklistedItems = blacklistedItems;
                })
                .build());
        return builder.build();
    }
}