package com.spyxar.glowup.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.spyxar.glowup.GlowUpMod;
import com.spyxar.glowup.GlowUpUtils;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import com.spyxar.glowup.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
    // INT-args variant (older mappings)
    @ModifyArgs(method = "renderEntities",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V"),
                require = 0)
    public void glowup$modifySetColorArgs_int(Args args, @Local Entity entity) {
        if (GlowUpMod.config.isEnabled && entity instanceof ItemEntity) {
            if (GlowUpUtils.shouldItemGlow(Registries.ITEM.getEntry(((ItemEntity) entity).getStack().getItem()).getIdAsString())) {
                int color = GlowUpMod.config.glowColor;
                args.set(0, ColorHelper.getRed(color));
                args.set(1, ColorHelper.getGreen(color));
                args.set(2, ColorHelper.getBlue(color));
                args.set(3, 255);
            }
        }
    }

    // FLOAT-args variant (newer mappings)
    @ModifyArgs(method = "renderEntities",
                at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(FFFF)V"),
                require = 0)
    public void glowup$modifySetColorArgs_float(Args args, @Local Entity entity) {
        if (GlowUpMod.config.isEnabled && entity instanceof ItemEntity) {
            if (GlowUpUtils.shouldItemGlow(Registries.ITEM.getEntry(((ItemEntity) entity).getStack().getItem()).getIdAsString())) {
                int color = GlowUpMod.config.glowColor;
                args.set(0, ColorHelper.getRed(color) / 255.0f);
                args.set(1, ColorHelper.getGreen(color) / 255.0f);
                args.set(2, ColorHelper.getBlue(color) / 255.0f);
                args.set(3, 1.0f);
            }
        }
    }
}