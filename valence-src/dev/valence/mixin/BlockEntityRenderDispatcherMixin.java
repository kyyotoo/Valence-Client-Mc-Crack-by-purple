package dev.valence.mixin;

import dev.valence.perf.Occlusion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
   @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true, require = 0)
   private void valence$occlusion(
      BlockEntity blockEntity, float partialTick, CrumblingOverlay crumbling, boolean flag, CallbackInfoReturnable<BlockEntityRenderState> cir
   ) {
      Minecraft mc = Minecraft.getInstance();
      if (crumbling == null && mc.gameRenderer != null) {
         Vec3 cam = mc.gameRenderer.mainCamera().position();
         BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = ((BlockEntityRenderDispatcher)this).getRenderer(blockEntity);
         if (renderer != null && !renderer.shouldRenderOffScreen()) {
            if (Occlusion.hideBlock(blockEntity.getBlockPos(), cam.x, cam.y, cam.z)) {
               cir.setReturnValue(null);
            }
         }
      }
   }
}
