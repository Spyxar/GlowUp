package com.spyxar.glowup.compat;

import com.spyxar.glowup.GlowUpMod;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection helper that attempts multiple constructor permutations for KeyBinding
 * to support changes across Minecraft 1.21.x (notably 1.21.9+).
 *
 * This version also emits lightweight debug logs so users can paste runtime output
 * if a constructor mismatch still occurs.
 */
public final class ReflectionShim {
    private ReflectionShim() {}

    public static KeyBinding constructKeyBindingCompat(String id, InputUtil.Type type, int keyCode, String categoryTranslationKey) {
        GlowUpMod.LOGGER.debug("ReflectionShim: constructing KeyBinding for id='{}', keyCode={}, category='{}'", id, keyCode, categoryTranslationKey);

        // First: try to find a Category inner class and any factory methods for it.
        Class<?> categoryClass = null;
        Method categoryCreateMethod = null;
        Object categoryInstance = null;

        try {
            categoryClass = Class.forName("net.minecraft.client.option.KeyBinding$Category");
            GlowUpMod.LOGGER.debug("ReflectionShim: found category class: {}", categoryClass.getName());

            // Try create(String) first (older 1.21.9+ style might offer create(Identifier) too)
            try {
                categoryCreateMethod = categoryClass.getMethod("create", String.class);
                categoryInstance = categoryCreateMethod.invoke(null, categoryTranslationKey);
                GlowUpMod.LOGGER.debug("ReflectionShim: created category instance via create(String)");
            } catch (NoSuchMethodException ignored) {
                // try Identifier overload if present
                try {
                    Class<?> identifierClass = Class.forName("net.minecraft.util.Identifier");
                    try {
                        categoryCreateMethod = categoryClass.getMethod("create", identifierClass);
                        // Try to construct an Identifier from the translation key; fall back to using the raw string if constructor not present
                        Object identifierInstance = null;
                        try {
                            // Try Identifier(String)
                            Constructor<?> idCtor = identifierClass.getConstructor(String.class);
                            identifierInstance = idCtor.newInstance(categoryTranslationKey);
                        } catch (NoSuchMethodException idCtorEx) {
                            // Try static of(String, String) by splitting on a delimiter if sensible
                            try {
                                Method ofMethod = identifierClass.getMethod("of", String.class, String.class);
                                String ns = "glowup";
                                String path = categoryTranslationKey.contains(".") ? categoryTranslationKey.substring(categoryTranslationKey.lastIndexOf('.') + 1) : categoryTranslationKey;
                                identifierInstance = ofMethod.invoke(null, ns, path);
                            } catch (ReflectiveOperationException e) {
                                GlowUpMod.LOGGER.debug("ReflectionShim: could not build Identifier from '{}': {}", categoryTranslationKey, e.toString());
                            }
                        }
                        if (identifierInstance != null) {
                            categoryInstance = categoryCreateMethod.invoke(null, identifierInstance);
                            GlowUpMod.LOGGER.debug("ReflectionShim: created category instance via create(Identifier)");
                        }
                    } catch (NoSuchMethodException ignored2) {
                        // no create(Identifier)
                    }
                } catch (ClassNotFoundException cnfe) {
                    // Identifier class not present (unlikely), fall through
                }
            }
        } catch (ClassNotFoundException cnfe) {
            // No Category inner class - possible on older mappings; will attempt other constructors below.
            GlowUpMod.LOGGER.debug("ReflectionShim: no KeyBinding.Category inner class found");
            categoryClass = null;
        } catch (ReflectiveOperationException roe) {
            GlowUpMod.LOGGER.debug("ReflectionShim: error while trying to create category instance: {}", roe.toString());
            categoryInstance = null;
        }

        // Collect candidate constructors and attempt to match parameters heuristically.
        List<Constructor<?>> ctors = new ArrayList<>();
        try {
            ctors.addAll(Arrays.asList(KeyBinding.class.getConstructors()));
        } catch (SecurityException se) {
            // Should not normally happen; fall back to the widely-used signatures below
            GlowUpMod.LOGGER.debug("ReflectionShim: could not list KeyBinding constructors: {}", se.toString());
        }

        List<Exception> attempts = new ArrayList<>();

        for (Constructor<?> ctor : ctors) {
            Class<?>[] params = ctor.getParameterTypes();
            Object[] args = new Object[params.length];
            boolean ok = true;

            for (int i = 0; i < params.length; i++) {
                Class<?> p = params[i];
                if (p.equals(String.class)) {
                    // Heuristic: the first String is the id; any subsequent String is the translation key category
                    if (i == 0) args[i] = id;
                    else args[i] = categoryTranslationKey;
                } else if (p.equals(int.class) || p.equals(Integer.class)) {
                    args[i] = Integer.valueOf(keyCode);
                } else if (p.equals(InputUtil.Type.class)) {
                    args[i] = type;
                } else if (categoryClass != null && p.equals(categoryClass)) {
                    if (categoryInstance != null) {
                        args[i] = categoryInstance;
                    } else {
                        // If we need a category instance but couldn't create one earlier, try to find a constructor on the category itself
                        try {
                            Constructor<?> catCtor = categoryClass.getConstructor(String.class);
                            args[i] = catCtor.newInstance(categoryTranslationKey);
                        } catch (ReflectiveOperationException e) {
                            GlowUpMod.LOGGER.debug("ReflectionShim: could not instantiate category class directly: {}", e.toString());
                            ok = false;
                            break;
                        }
                    }
                } else {
                    // Unknown parameter type - we can't supply it
                    ok = false;
                    break;
                }
            }

            if (!ok) continue;

            try {
                ctor.setAccessible(true);
                Object instantiated = ctor.newInstance(args);
                GlowUpMod.LOGGER.debug("ReflectionShim: successfully constructed KeyBinding using constructor: {}", Arrays.toString(params));
                return (KeyBinding) instantiated;
            } catch (Exception e) {
                attempts.add(e);
                GlowUpMod.LOGGER.debug("ReflectionShim: constructor {} failed: {}", Arrays.toString(params), e.toString());
            }
        }

        // Try some well-known permutations as a final effort (covers most known versions)
        List<TrySignature> fallbacks = Arrays.asList(
            new TrySignature(new Class<?>[]{String.class, InputUtil.Type.class, int.class, String.class}, new Object[]{id, type, Integer.valueOf(keyCode), categoryTranslationKey}),
            new TrySignature(new Class<?>[]{String.class, InputUtil.Type.class, int.class, categoryClass}, new Object[]{id, type, Integer.valueOf(keyCode), categoryInstance}),
            new TrySignature(new Class<?>[]{String.class, categoryClass, int.class, String.class}, new Object[]{id, categoryInstance, Integer.valueOf(keyCode), categoryTranslationKey}),
            new TrySignature(new Class<?>[]{String.class, InputUtil.Type.class, categoryClass, int.class}, new Object[]{id, type, categoryInstance, Integer.valueOf(keyCode)})
        );

        for (TrySignature ts : fallbacks) {
            try {
                Constructor<?> c = KeyBinding.class.getConstructor(ts.paramTypes);
                c.setAccessible(true);
                Object inst = c.newInstance(ts.args);
                GlowUpMod.LOGGER.debug("ReflectionShim: constructed KeyBinding via fallback signature: {}", Arrays.toString(ts.paramTypes));
                return (KeyBinding) inst;
            } catch (Exception e) {
                GlowUpMod.LOGGER.debug("ReflectionShim: fallback {} failed: {}", Arrays.toString(ts.paramTypes), e.toString());
                attempts.add(e);
            }
        }

        // If all attempts fail, throw with collected causes for easier debugging.
        RuntimeException re = new RuntimeException("Could not construct KeyBinding compatible with available mappings for id: " + id);
        for (Exception e : attempts) {
            re.addSuppressed(e);
        }
        throw re;
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

    // Small helper for trying explicit signatures
    private static final class TrySignature {
        final Class<?>[] paramTypes;
        final Object[] args;
        TrySignature(Class<?>[] paramTypes, Object[] args) {
            this.paramTypes = paramTypes;
            this.args = args;
        }
    }
}