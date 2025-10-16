package com.spyxar.glowup.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.spyxar.glowup.GlowUpMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class GlowUpConfig
{
    private transient File file;

    public boolean isEnabled = true;
    public int glowColor = 16777215;
    public ArrayList<String> items = new ArrayList<>(List.of("diamond", "ancient_debris"));
    public boolean toggleMessageOnOverlay = true;
    public boolean blacklistEnabled = true;
    public ArrayList<String> blacklistedItems = new ArrayList<>();

    private GlowUpConfig() {}

    public static GlowUpConfig loadConfig()
    {
        File file = new File(
                FabricLoader.getInstance().getConfigDir().toString(),
                GlowUpMod.MOD_ID + ".toml"
        );
        GlowUpConfig config;
        if (file.exists())
        {
            Toml configToml = new Toml().read(file);
            config = configToml.to(GlowUpConfig.class);
            config.file = file;
        }
        else
        {
            config = new GlowUpConfig();
            config.file = file;
            config.saveConfig();
        }
        return config;
    }

    public void saveConfig()
    {
        TomlWriter writer = new TomlWriter();
        try
        {
            writer.write(this, file);
        }
        catch (IOException e)
        {
            GlowUpMod.LOGGER.error("An error occurred while trying to save the config", e);
        }
    }
}