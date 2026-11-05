package dev.valence.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import dev.valence.lyrics.SkyLyrics;
import dev.valence.render.MultiBufferSource;
import dev.valence.sky.ValenceSky;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType.Skybox;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
   @Inject(method = "render", at = @At("HEAD"), require = 0)
   private void valence$prepareSky(GpuBufferSlice fog, SkyRenderState state, CallbackInfo ci) {
      MultiBufferSource.BufferSource.beginDefer();

      try {
         if (state.skybox == Skybox.END) {
            if (ValenceSky.replacesEnd()) {
               ValenceSky.renderEnd();
            }
         } else {
            ValenceSky.render(state.rainBrightness, state.starBrightness);
         }

         SkyLyrics.renderInSky();
      } finally {
         MultiBufferSource.BufferSource.endDefer();
      }
   }

   @Inject(method = "renderSunMoonAndStars", at = @At("TAIL"), require = 0)
   private void valence$drawSky(
      RenderPass pass,
      PoseStack poseStack,
      float sunAngle,
      float moonAngle,
      float starAngle,
      MoonPhase moonPhase,
      float rainBrightness,
      float starBrightness,
      CallbackInfo ci
   ) {
      MultiBufferSource.BufferSource.flushDeferred(pass);
   }

   @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipSkyDisc(RenderPass pass, Vector3fc color, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipSunrise(RenderPass pass, PoseStack poseStack, float sunAngle, Vector4fc color, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipDarkDisc(RenderPass pass, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipSun(RenderPass pass, float alpha, PoseStack poseStack, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipMoon(RenderPass pass, MoonPhase phase, float alpha, PoseStack poseStack, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$skipStars(RenderPass pass, float brightness, PoseStack poseStack, CallbackInfo ci) {
      if (ValenceSky.replacesOverworld()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$endSkyCover(RenderPass pass, CallbackInfo ci) {
      if (ValenceSky.replacesEnd() && ValenceSky.replacesOverworld()) {
         MultiBufferSource.BufferSource.flushDeferred(pass);
         ci.cancel();
      }
   }

   @Inject(method = "renderEndSky", at = @At("TAIL"), require = 0)
   private void valence$endSkyFade(RenderPass pass, CallbackInfo ci) {
      MultiBufferSource.BufferSource.flushDeferred(pass);
   }
}
