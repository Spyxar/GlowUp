package com.spyxar.glowup;

public class GlowUpUtils
{
    public static boolean shouldItemGlow(String itemId)
    {
        if (GlowUpMod.config.blacklistEnabled)
        {
            if (itemId.startsWith("minecraft:") && GlowUpMod.config.blacklistedItems.contains(itemId.replace("minecraft:", "")))
            {
                return false;
            }
            else
            {
                return !GlowUpMod.config.blacklistedItems.contains(itemId);
            }
        }

        if (GlowUpMod.config.items.contains("*"))
        {
            return true;
        }
        if (itemId.startsWith("minecraft:") && GlowUpMod.config.items.contains(itemId.replace("minecraft:", "")))
        {
            return true;
        }
        else
        {
            return GlowUpMod.config.items.contains(itemId);
        }
    }
}