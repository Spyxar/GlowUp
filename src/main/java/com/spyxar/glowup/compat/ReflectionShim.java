package com.spyxar.glowup.compat;

import com.spyxar.glowup.GlowUpMod;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ReflectionShim {
    private ReflectionShim() {}

    public static KeyBinding constructKeyBindingCompat(String id, InputUtil.Type type, int keyCode, String categoryTranslationKey) {
        // Attempt new mappings where KeyBinding has an inner Category type with a static create(String) method.
        try {
            Class<?> categoryClass = Class.forName("net.minecraft.client.option.KeyBinding$Category");
            Method createMethod = null;
            try {
                createMethod = categoryClass.getMethod("create", String.class);
            } catch (NoSuchMethodException ignored) {
                // fall through to fallback
            }

            if (createMethod != null) {
                Object categoryInstance = createMethod.invoke(null, categoryTranslationKey);
                Constructor<?> ctor = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, categoryClass);
                Object kb = ctor.newInstance(id, type, Integer.valueOf(keyCode), categoryInstance);
                return (KeyBinding) kb;
            }
        } catch (ReflectiveOperationException ignored) {
            // fall through to fallback
        }

        // Fallback to older mappings where KeyBinding constructor takes a String category.
        try {
            Constructor<KeyBinding> ctor2 = KeyBinding.class.getConstructor(String.class, InputUtil.Type.class, int.class, String.class);
            return ctor2.newInstance(id, type, Integer.valueOf(keyCode), categoryTranslationKey);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not construct KeyBinding compatible with available mappings for id: " + id, e);
        }
    }

    /**
     * Register the key binding if Fabric API's KeyBindingHelper is available.
     * If it's not present, return the original KeyBinding so the mod remains loadable without that API.
     */
    public static KeyBinding safeRegisterKeyBinding(KeyBinding kb) {
        try {
            Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
            Method registerMethod = helperClass.getMethod("registerKeyBinding", KeyBinding.class);
            Object res = registerMethod.invoke(null, kb);
            return (KeyBinding) res;
        } catch (ClassNotFoundException e) {
            // Fabric KeyBindingHelper not present — continue without registering via helper.
            GlowUpMod.LOGGER.debug("Fabric KeyBindingHelper not found; skipping helper registration for key binding.");
            return kb;
        } catch (ReflectiveOperationException e) {
            // Unexpected reflection error — propagate as runtime to make debugging visible
            throw new RuntimeException("Error while attempting to register keybinding via KeyBindingHelper", e);
        }
    }
}