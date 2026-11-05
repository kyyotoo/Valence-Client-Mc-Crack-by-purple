package dev.valence.mixin;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import dev.valence.config.ValenceConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public abstract class FramerateLimitTrackerMixin {
   @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true, require = 0)
   private void valence$backgroundLimit(CallbackInfoReturnable<Integer> cir) {
      if (ValenceConfig.get().perfBackgroundFps && !Minecraft.getInstance().isWindowActive()) {
         cir.setReturnValue(Math.min((Integer)cir.getReturnValue(), 30));
      }
   }
}
