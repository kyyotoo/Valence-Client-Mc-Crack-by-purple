package dev.valence.mixin;

import com.mojang.renderpearl.api.commands.RenderPass;
import dev.valence.config.ValenceConfig;
import dev.valence.sky.ValenceSky;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CloudRenderer.class)
public abstract class LevelRendererMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/CloudStatus;Lcom/mojang/renderpearl/api/commands/RenderPass;)V",
      at = @At("HEAD"),
      cancellable = true,
      require = 0
   )
   private void valence$hideClouds(CloudStatus status, RenderPass pass, CallbackInfo ci) {
      if (ValenceConfig.get().skyHideClouds && ValenceSky.visible()) {
         ci.cancel();
      }
   }

   @Inject(method = "renderOit", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$hideCloudsOit(CallbackInfo ci) {
      if (ValenceConfig.get().skyHideClouds && ValenceSky.visible()) {
         ci.cancel();
      }
   }
}
