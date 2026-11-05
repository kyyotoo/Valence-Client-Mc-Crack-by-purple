package dev.valence.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.valence.config.ValenceConfig;
import dev.valence.license.License;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class LowFireMixin {
   @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true, require = 0)
   private static void valence$lowFire(PoseStack pose, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
      float h = Math.max(0.1F, Math.min(1.0F, ValenceConfig.get().fireHeight));
      if (!(h >= 0.999F) && License.active()) {
         ci.cancel();
         float u0 = sprite.getU0();
         float u1 = sprite.getU1();
         float v0 = sprite.getV0();
         float v1 = sprite.getV1();
         float drop = (1.0F - h) * 0.55F;

         for (int i = 0; i < 2; i++) {
            pose.pushPose();
            pose.translate(-(i * 2 - 1) * 0.24F, -0.3F - drop, 0.0F);
            pose.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees((i * 2 - 1) * 10.0F)));
            collector.submitCustomGeometry(pose, RenderTypes.fireScreenEffect(sprite.atlasLocation()), (p, vc) -> {
               Matrix4f m = p.pose();
               vc.addVertex(m, -0.5F, -0.5F, -0.5F).setUv(u1, v1).setColor(1.0F, 1.0F, 1.0F, 0.9F);
               vc.addVertex(m, 0.5F, -0.5F, -0.5F).setUv(u0, v1).setColor(1.0F, 1.0F, 1.0F, 0.9F);
               vc.addVertex(m, 0.5F, 0.5F, -0.5F).setUv(u0, v0).setColor(1.0F, 1.0F, 1.0F, 0.9F);
               vc.addVertex(m, -0.5F, 0.5F, -0.5F).setUv(u1, v0).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            });
            pose.popPose();
         }
      }
   }
}
