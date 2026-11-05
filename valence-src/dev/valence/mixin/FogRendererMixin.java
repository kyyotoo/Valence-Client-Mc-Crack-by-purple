package dev.valence.mixin;

import dev.valence.config.ValenceConfig;
import dev.valence.sky.ValenceSky;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.dimension.DimensionType.Skybox;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
   @Inject(method = "computeFogColor", at = @At("RETURN"), require = 0)
   private void valence$tintFog(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorld, Vector4f v, CallbackInfo ci) {
      if (camera.getFluidInCamera() == FogType.NONE) {
         Skybox skybox = level.dimensionType().skybox();
         boolean end = skybox == Skybox.END;
         if (skybox != Skybox.NONE && (!end || ValenceConfig.get().skyInEnd)) {
            int vanilla = Math.round(clamp(v.w) * 255.0F) << 24
               | Math.round(clamp(v.x) * 255.0F) << 16
               | Math.round(clamp(v.y) * 255.0F) << 8
               | Math.round(clamp(v.z) * 255.0F);
            int c = ValenceSky.fogColor(vanilla);
            if (c != vanilla) {
               v.set((c >> 16 & 0xFF) / 255.0F, (c >> 8 & 0xFF) / 255.0F, (c & 0xFF) / 255.0F, v.w);
            }
         }
      }
   }

   private static float clamp(float f) {
      return f < 0.0F ? 0.0F : (f > 1.0F ? 1.0F : f);
   }
}
