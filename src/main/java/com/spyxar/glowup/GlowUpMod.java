package com.spyxar.glowup;

import com.spyxar.glowup.config.GlowUpConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class GlowUpMod implements ClientModInitializer
{
    public static final String MOD_ID = "glowup";

    public static final Logger LOGGER = LogManager.getLogger("GlowUp");

    public static GlowUpConfig config = null;

    @Override
    public void onInitializeClient()
    {
        config = GlowUpConfig.loadConfig();

        KeyBinding toggleItemGlowKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.glowup.toggleglow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.glowup.main"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleItemGlowKeyBinding.wasPressed())
            {
                if (client.player == null)
                {
                    return;
                }

                Item item = client.player.getStackInHand(Hand.MAIN_HAND).getItem();
                if (item == Items.AIR)
                {
                    return;
                }

                String itemId = Registries.ITEM.getEntry(item).getIdAsString();

                boolean isOverlayMessage = config.toggleMessageOnOverlay;
                if (config.items.contains(itemId))
                {
                    config.items.remove(itemId);
                    client.player.sendMessage(Text.translatable("text.glowup.disableglow", itemId), isOverlayMessage);
                }
                else if (config.items.contains(itemId.replace("minecraft:", "")))
                {
                    config.items.remove(itemId.replace("minecraft:", ""));

                    client.player.sendMessage(Text.translatable("text.glowup.disableglow", itemId), isOverlayMessage);
                }
                else
                {
                    config.items.add(itemId.replace("minecraft:", ""));
                    client.player.sendMessage(Text.translatable("text.glowup.enableglow", itemId), isOverlayMessage);
                }
                config.saveConfig();
            }
        });
    }
}