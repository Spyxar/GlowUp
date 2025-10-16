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
import java.lang.reflect.*;

public class GlowUpMod implements ClientModInitializer
{
    public static final String MOD_ID = "glowup";

    public static final Logger LOGGER = LogManager.getLogger("GlowUp");

    public static GlowUpConfig config = null;

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
        // Attempt new mappings where KeyBinding has an inner Category type with a static create(String) method.
        try
        {
            Class<?> categoryClass = Class.forName("net.minecraft.client.option.KeyBinding$Category");
            Method createMethod = null;
            try
            {
                createMethod = categoryClass.getMethod("create", String.class);
            }
            catch (NoSuchMethodException ignored)
            {
                // fall through to fallback
            }

            if (createMethod != null)
            {
                Object categoryInstance = createMethod.invoke(null, categoryTranslationKey);
                Constructor<?> ctor = KeyBinding.class.getConstructor(String.class, net.minecraft.client.util.InputUtil.Type.class, int.class, categoryClass);
                Object kb = ctor.newInstance(id, type, Integer.valueOf(keyCode), categoryInstance);
                return (KeyBinding) kb;
            }
        }
        catch (ReflectiveOperationException ignored)
        {
            // fall through to fallback
        }

        // Fallback to older mappings where KeyBinding constructor takes a String category.
        try
        {
            Constructor<net.minecraft.client.option.KeyBinding> ctor2 = KeyBinding.class.getConstructor(String.class, net.minecraft.client.util.InputUtil.Type.class, int.class, String.class);
            return ctor2.newInstance(id, type, Integer.valueOf(keyCode), categoryTranslationKey);
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException("Could not construct KeyBinding compatible with available mappings for id: " + id, e);
        }
    }

    /**
     * Register the key binding if Fabric API's KeyBindingHelper is available.
     * If it's not present, return the original KeyBinding so the mod remains loadable without that API.
     */
    private static KeyBinding safeRegisterKeyBinding(KeyBinding kb) {
        try {
            Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
            Method registerMethod = helperClass.getMethod("registerKeyBinding", KeyBinding.class);
            Object res = registerMethod.invoke(null, kb);
            return (KeyBinding) res;
        } catch (ClassNotFoundException e) {
            // Fabric KeyBindingHelper not present — continue without registering via helper.
            LOGGER.debug("Fabric KeyBindingHelper not found; skipping helper registration for key binding.");
            return kb;
        } catch (ReflectiveOperationException e) {
            // Unexpected reflection error — propagate as runtime to make debugging visible
            throw new RuntimeException("Error while attempting to register keybinding via KeyBindingHelper", e);
        }
    }
}