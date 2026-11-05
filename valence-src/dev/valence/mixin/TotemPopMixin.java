package dev.valence.mixin;

import dev.valence.config.ValenceConfig;
import dev.valence.license.License;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class TotemPopMixin {
   @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$noTotemOverlay(ItemStack stack, CallbackInfo ci) {
      try {
         if (stack != null && stack.is(Items.TOTEM_OF_UNDYING) && ValenceConfig.get().totemPop == 0 && License.active()) {
            ci.cancel();
         }
      } catch (Throwable var4) {
      }
   }
}
