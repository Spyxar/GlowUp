package com.spyxar.glowup.mixin;

import com.spyxar.glowup.GlowUpMod;
import com.spyxar.glowup.GlowUpUtils;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.ColorHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState>
{
    //? if >=1.21.9 {
    @Redirect(method = "updateRenderState", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;outlineColor:I", opcode = Opcodes.PUTFIELD))
    public void glowup$modifyOutlineColor(EntityRenderState instance, int value, T entity)
    {
        if (GlowUpMod.config.isEnabled && entity instanceof ItemEntity)
        {
            if (GlowUpUtils.shouldItemGlow(Registries.ITEM.getEntry(((ItemEntity) entity).getStack().getItem()).getIdAsString()))
            {
                int color = GlowUpMod.config.glowColor;
                instance.outlineColor = ColorHelper.getArgb(255, ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color));
            }
        }
    }
    //?}
}