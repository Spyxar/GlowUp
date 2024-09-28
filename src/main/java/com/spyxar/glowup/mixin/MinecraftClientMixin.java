package com.spyxar.glowup.mixin;

import com.spyxar.glowup.GlowUpUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    public void glowup$shouldItemHaveOutline(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (entity instanceof ItemEntity)
        {
            if (GlowUpUtils.shouldItemGlow(Registries.ITEM.getEntry(((ItemEntity) entity).getStack().getItem()).getIdAsString()))
            {
                cir.setReturnValue(true);
            }
        }
    }
}