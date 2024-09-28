package com.spyxar.glowup;

import com.spyxar.glowup.config.GlowUpConfig;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlowUpMod implements ClientModInitializer
{
    public static final String MOD_ID = "glowup";

    public static final Logger LOGGER = LogManager.getLogger("GlowUp");

    public static GlowUpConfig config = null;

    @Override
    public void onInitializeClient()
    {
        config = GlowUpConfig.loadConfig();
    }
}
