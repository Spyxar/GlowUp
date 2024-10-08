package com.spyxar.glowup.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2"))
        {
            return ClothConfigScreenFactory::buildConfigScreen;
        }
        else
        {
            return (parent) -> null;
        }
    }
}