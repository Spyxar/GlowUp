package com.spyxar.glowup;

import com.mojang.blaze3d.platform.InputConstants;
import com.spyxar.glowup.config.GlowUpConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class GlowUpMod implements ClientModInitializer
{
    public static final String MOD_ID = "glowup";

    public static final Logger LOGGER = LogManager.getLogger("GlowUp");

    public static GlowUpConfig config = null;

    public static final KeyMapping.Category MAIN_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    @Override
    public void onInitializeClient()
    {
        config = GlowUpConfig.loadConfig();

        // ToDo: These keybinds repeat when held for a while, can also cause it to still run when key is already up
        KeyMapping toggleKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.glowup.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MAIN_CATEGORY));
        KeyMapping toggleItemGlowKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.glowup.toggleglow", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, MAIN_CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKeyMapping.consumeClick())
            {
                config.isEnabled = !config.isEnabled;
                config.saveConfig();
            }
            if (toggleItemGlowKeyMapping.consumeClick())
            {
                if (client.player == null)
                {
                    return;
                }

                Item item = client.player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
                if (item == Items.AIR)
                {
                    return;
                }

                String itemId = BuiltInRegistries.ITEM.getKey(item).toString();

                Component message;
                if (config.items.contains(itemId))
                {
                    config.items.remove(itemId);
                    message = Component.translatable("text.glowup.disableglow", itemId);
                }
                else if (config.items.contains(itemId.replace("minecraft:", "")))
                {
                    config.items.remove(itemId.replace("minecraft:", ""));
                    message = Component.translatable("text.glowup.disableglow", itemId);
                }
                else
                {
                    config.items.add(itemId.replace("minecraft:", ""));
                    message = Component.translatable("text.glowup.enableglow", itemId);
                }

                if (config.toggleMessageOnOverlay)
                {
                    client.player.sendOverlayMessage(message);
                }
                else
                {
                    client.player.sendSystemMessage(message);
                }

                config.saveConfig();
            }
        });
    }
}