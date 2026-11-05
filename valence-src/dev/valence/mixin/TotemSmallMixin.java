package dev.valence.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.valence.config.ValenceConfig;
import dev.valence.license.License;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class TotemSmallMixin {
   @Unique
   private boolean valence$scaled;

   @Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), require = 0)
   private void valence$smallStart(PlayerRenderState state, PoseStack pose, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
      this.valence$scaled = false;
      ItemStack item = state != null && state.itemActivation != null ? state.itemActivation.item : null;
      if (item != null && item.is(Items.TOTEM_OF_UNDYING) && ValenceConfig.get().totemPop == 1 && License.active()) {
         pose.pushPose();
         pose.scale(0.4F, 0.4F, 1.0F);
         this.valence$scaled = true;
      }
   }

   @Inject(method = "renderItemActivationAnimation", at = @At("RETURN"), require = 0)
   private void valence$smallEnd(PlayerRenderState state, PoseStack pose, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
      if (this.valence$scaled) {
         pose.popPose();
         this.valence$scaled = false;
      }
   }
}
