package com.spyxar.glowup.mixin;

import com.spyxar.glowup.GlowUpMod;
import com.spyxar.glowup.GlowUpUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.util.ARGB;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity>
{
    @Redirect(method = "extractRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;outlineColor:I", opcode = Opcodes.PUTFIELD))
    public void glowup$modifyOutlineColor(EntityRenderState instance, int value, T entity)
    {
        if (GlowUpMod.config.isEnabled && entity instanceof ItemEntity)
        {
            if (GlowUpUtils.shouldItemGlow(BuiltInRegistries.ITEM.getKey(((ItemEntity) entity).getItem().getItem()).toString()))
            {
                int color = GlowUpMod.config.glowColor;
                instance.outlineColor = ARGB.color(255, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
            }
        }
        else
        {
            instance.outlineColor = value;
        }
    }
}