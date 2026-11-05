package dev.valence.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {
   @ModifyVariable(method = "createReload", at = @At("HEAD"), argsOnly = true, require = 0)
   private List<PackResources> valence$stylesOnTop(List<PackResources> packs) {
      PackResources ours = null;

      for (PackResources p : packs) {
         if ("file/Valence Styles".equals(p.packId())) {
            ours = p;
         }
      }

      if (ours != null && packs.get(packs.size() - 1) != ours) {
         List<PackResources> out = new ArrayList<>(packs);
         out.remove(ours);
         out.add(ours);
         return out;
      } else {
         return packs;
      }
   }
}
