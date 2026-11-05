package dev.valence.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public final class UiScale {
   private UiScale() {
   }

   public static int pixelScale() {
      Window w = Minecraft.getInstance().getWindow();
      float fit = Math.min(w.getWidth() / 700.0F, w.getHeight() / 400.0F);
      return Math.max(1, (int)Math.floor(fit + 0.25F));
   }

   public static float factor() {
      double gui = Minecraft.getInstance().getWindow().getGuiScale();
      return (float)(pixelScale() / Math.max(1.0, gui));
   }
}
