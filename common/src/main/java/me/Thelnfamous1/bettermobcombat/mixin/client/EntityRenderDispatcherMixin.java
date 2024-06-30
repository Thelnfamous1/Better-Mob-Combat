package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.logic.MobTargetFinder;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.BetterCombatClient;
import net.bettercombat.client.collision.OrientedBoundingBox;
import net.bettercombat.client.collision.TargetFinder;
import net.bettercombat.logic.PlayerAttackProperties;
import net.bettercombat.mixin.client.MinecraftClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
    public EntityRenderDispatcherMixin() {
    }

    @Inject(
        method = {"render"},
        at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V", shift = At.Shift.AFTER)}
    )
    public <E extends Entity> void renderColliderDebug(E entity, double $$1, double $$2, double $$3, float $$4, float $$5, PoseStack matrices, MultiBufferSource bufferSource, int $$8, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (((MinecraftClientAccessor)client).getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            if (entity instanceof Mob mob) {
                if (BetterCombatClient.config.isDebugOBBEnabled) {
                    if (mob.getMainHandItem() != null) {
                        PlayerAttackProperties extendedMob = (PlayerAttackProperties)mob;
                        int comboCount = extendedMob.getComboCount();
                        AttackHand hand = MobAttackHelper.getCurrentAttack(mob, comboCount);
                        if (hand != null) {
                            WeaponAttributes attributes = hand.attributes();
                            if (attributes != null) {
                                TargetFinder.TargetResult target = MobTargetFinder.findAttackTargetResult(mob, null, hand.attack(), attributes.attackRange());
                                boolean collides = target.entities.size() > 0;
                                Vec3 cameraOffset = mob.position().reverse();
                                OrientedBoundingBox obb = target.obb.copy().offset(cameraOffset).updateVertex();
                                List<OrientedBoundingBox> collidingObbs = target.entities.stream().map((e) -> (new OrientedBoundingBox(e.getBoundingBox())).offset(cameraOffset).scale(0.95).updateVertex()).collect(Collectors.toList());
                                this.bettermobcombat$drawOutline(matrices, obb, collidingObbs, collides);
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void bettermobcombat$drawOutline(PoseStack matrixStack, OrientedBoundingBox obb, List<OrientedBoundingBox> otherObbs, boolean collides) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0F);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        if (collides) {
            this.bettermobcombat$outlineOBB(matrixStack, obb, bufferBuilder, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.5F);
        } else {
            this.bettermobcombat$outlineOBB(matrixStack, obb, bufferBuilder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.5F);
        }

        this.bettermobcombat$look(matrixStack, obb, bufferBuilder, 0.5F);

        for (OrientedBoundingBox otherObb : otherObbs) {
            this.bettermobcombat$outlineOBB(matrixStack, otherObb, bufferBuilder, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.5F);
        }

        tessellator.end();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableBlend();
    }

    @Unique
    private void bettermobcombat$outlineOBB(PoseStack matrixStack, OrientedBoundingBox box, VertexConsumer buffer, float red1, float green1, float blue1, float red2, float green2, float blue2, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        buffer.vertex(matrix4f, (float)box.vertex1.x, (float)box.vertex1.y, (float)box.vertex1.z).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex1.x, (float)box.vertex1.y, (float)box.vertex1.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex2.x, (float)box.vertex2.y, (float)box.vertex2.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex3.x, (float)box.vertex3.y, (float)box.vertex3.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex4.x, (float)box.vertex4.y, (float)box.vertex4.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex1.x, (float)box.vertex1.y, (float)box.vertex1.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex5.x, (float)box.vertex5.y, (float)box.vertex5.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex6.x, (float)box.vertex6.y, (float)box.vertex6.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex2.x, (float)box.vertex2.y, (float)box.vertex2.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex6.x, (float)box.vertex6.y, (float)box.vertex6.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex7.x, (float)box.vertex7.y, (float)box.vertex7.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex3.x, (float)box.vertex3.y, (float)box.vertex3.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex7.x, (float)box.vertex7.y, (float)box.vertex7.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex8.x, (float)box.vertex8.y, (float)box.vertex8.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex4.x, (float)box.vertex4.y, (float)box.vertex4.z).color(red1, green1, blue1, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex8.x, (float)box.vertex8.y, (float)box.vertex8.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex5.x, (float)box.vertex5.y, (float)box.vertex5.z).color(red2, green2, blue2, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.vertex5.x, (float)box.vertex5.y, (float)box.vertex5.z).color(0, 0, 0, 0).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0, 0, 0, 0).endVertex();
    }

    @Unique
    private void bettermobcombat$look(PoseStack matrixStack, OrientedBoundingBox box, VertexConsumer buffer, float alpha) {
        Matrix4f matrix4f = matrixStack.last().pose();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(1.0F, 0.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.axisZ.x, (float)box.axisZ.y, (float)box.axisZ.z).color(1.0F, 0.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(1.0F, 0.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 1.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.axisY.x, (float)box.axisY.y, (float)box.axisY.z).color(0.0F, 1.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 1.0F, 0.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 0.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.axisX.x, (float)box.axisX.y, (float)box.axisX.z).color(0.0F, 0.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 0.0F, 1.0F, alpha).endVertex();
        buffer.vertex(matrix4f, (float)box.center.x, (float)box.center.y, (float)box.center.z).color(0.0F, 0.0F, 0.0F, alpha).endVertex();
    }
}