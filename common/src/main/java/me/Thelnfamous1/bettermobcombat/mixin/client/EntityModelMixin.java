package me.Thelnfamous1.bettermobcombat.mixin.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(EntityModel.class)
public abstract class EntityModelMixin<T extends Entity> extends Model {
    public EntityModelMixin(Function<ResourceLocation, RenderType> $$0) {
        super($$0);
    }

    @Inject(method = "copyPropertiesTo", at = @At("RETURN"))
    private void post_copy_PropertiesTo(EntityModel<T> otherModel, CallbackInfo ci){
        this.bettermobcombat$copyMutatedAttributes(otherModel);
    }

    @Unique
    protected void bettermobcombat$copyMutatedAttributes(EntityModel<T> otherModel) {
    }
}
