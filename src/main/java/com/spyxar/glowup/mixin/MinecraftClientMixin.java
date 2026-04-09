package com.spyxar.glowup.mixin;

import com.spyxar.glowup.GlowUpMod;
import com.spyxar.glowup.GlowUpUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin
{
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    public void glowup$shouldItemHaveOutline(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (GlowUpMod.config.isEnabled && entity instanceof ItemEntity)
        {
            if (GlowUpUtils.shouldItemGlow(BuiltInRegistries.ITEM.getKey(((ItemEntity) entity).getItem().getItem()).toString()))
            {
                cir.setReturnValue(true);
            }
        }
    }
}