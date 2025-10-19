package com.spyxar.glowup;

import com.spyxar.glowup.config.GlowUpConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class GlowUpMod implements ClientModInitializer
{
    public static final String MOD_ID = "glowup";
    
    public static final Logger LOGGER = LogManager.getLogger("GlowUp");
    
    public static GlowUpConfig config = null;
    
    private static final KeyBinding.Category GLOWUP_CATEGORY = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
    
    @Override
    public void onInitializeClient()
    {
        config = GlowUpConfig.loadConfig();

        KeyBinding toggleKeyBinding = safeRegisterKeyBinding(constructKeyBindingCompat("key.glowup.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.glowup.main"));
        KeyBinding toggleItemGlowKeyBinding = safeRegisterKeyBinding(constructKeyBindingCompat("key.glowup.toggleglow", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.glowup.main"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKeyBinding.wasPressed()) {
                config.isEnabled = !config.isEnabled;
                config.saveConfig();
            }
            if (toggleItemGlowKeyBinding.wasPressed()) {
                if (client.player == null) {
                    return;
                }
                Item item = client.player.getStackInHand(Hand.MAIN_HAND).getItem();
                if (item == Items.AIR) {
                    return;
                }
                String itemId = Registries.ITEM.getEntry(item).getIdAsString();
                boolean isOverlayMessage = config.toggleMessageOnOverlay;
                if (config.items.contains(itemId)) {
                    config.items.remove(itemId);
                    client.player.sendMessage(Text.translatable("text.glowup.disableglow", itemId), isOverlayMessage);
                } else if (config.items.contains(itemId.replace("minecraft:", ""))) {
                    config.items.remove(itemId.replace("minecraft:", ""));
                    client.player.sendMessage(Text.translatable("text.glowup.disableglow", itemId), isOverlayMessage);
                } else {
                    config.items.add(itemId.replace("minecraft:", ""));
                    client.player.sendMessage(Text.translatable("text.glowup.enableglow", itemId), isOverlayMessage);
                }
                config.saveConfig();
            }
        });
    }

    private static net.minecraft.client.option.KeyBinding constructKeyBindingCompat(String id, net.minecraft.client.util.InputUtil.Type type, int keyCode, String categoryTranslationKey)
    {
        // Use the structured Category API introduced in newer Fabric mappings
        return new KeyBinding(id, type, keyCode, GLOWUP_CATEGORY);
    }
    
    /**
     * Register the key binding using KeyBindingHelper when available.
     * If it's not present at runtime, return the original KeyBinding.
     */
    private static KeyBinding safeRegisterKeyBinding(KeyBinding kb) {
        try {
            return KeyBindingHelper.registerKeyBinding(kb);
        } catch (NoClassDefFoundError | Exception e) {
            LOGGER.debug("KeyBindingHelper not available; skipping helper registration for key binding.");
            return kb;
        }
    }
}