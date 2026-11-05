package dev.valence.cord.hud;

import dev.valence.cord.NeonLyrics;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class GlassHotbar {
   private static final Anim sel = new Anim(0.0, 22.0);
   public static final String[] STYLES = new String[]{"vanilla", "glass", "minimal", "dark", "neon"};

   private GlassHotbar() {
   }

   public static HudElement wrap(HudElement original) {
      return (g, delta) -> {
         Minecraft mc = Minecraft.getInstance();
         int style = NeonLyrics.cfg().hotbarStyle;
         if (style > 0 && mc.player != null && !mc.player.isSpectator()) {
            render(g, mc, style);
         } else {
            original.extractRenderState(g, delta);
         }
      };
   }

   private static void render(GuiGraphicsExtractor g, Minecraft mc, int style) {
      float old = Gfx.alpha;
      Gfx.alpha = 1.0F;
      int w = 182;
      int h = 22;
      int x = g.guiWidth() / 2 - w / 2;
      int y = g.guiHeight() - h - 1;
      Inventory inv = mc.player.getInventory();
      float sx = sel.target(inv.getSelectedSlot()).get();
      ItemStack[] items = new ItemStack[9];

      for (int i = 0; i < 9; i++) {
         items[i] = inv.getItem(i);
      }

      drawBar(g, mc, style, x, y, items, sx, mc.player.getOffhandItem(), true);
      Gfx.alpha = old;
   }

   public static void preview(GuiGraphicsExtractor g, int style, float x, float y, float scale) {
      Minecraft mc = Minecraft.getInstance();
      ItemStack[] items = new ItemStack[9];

      for (int i = 0; i < 9; i++) {
         items[i] = mc.player != null ? mc.player.getInventory().getItem(i) : ItemStack.EMPTY;
      }

      g.pose().pushMatrix();
      g.pose().translate(x, y);
      g.pose().scale(scale, scale);
      float old = Gfx.alpha;
      Gfx.alpha = 1.0F;
      if (style == 0) {
         Gfx.rect(g, 0, 0, 182, 22, 1052688, 0.55F);

         for (int i = 0; i < 9; i++) {
            Gfx.rect(g, 1 + i * 20, 1, 20, 20, 9145227, 0.35F);
         }

         Gfx.rect(g, 41, 0, 20, 22, 16777215, 0.55F);
         Gfx.rect(g, 43, 2, 16, 18, 1052688, 0.6F);

         for (int i = 0; i < 9; i++) {
            if (!items[i].isEmpty()) {
               g.item(items[i], 3 + i * 20, 3);
            }
         }
      } else {
         drawBar(g, mc, style, 0, 0, items, 2.0F, ItemStack.EMPTY, false);
      }

      Gfx.alpha = old;
      g.pose().popMatrix();
   }

   private static void drawBar(GuiGraphicsExtractor g, Minecraft mc, int style, int x, int y, ItemStack[] items, float sx, ItemStack off, boolean decorations) {
      int acc = Theme.accent();
      int w = 182;
      int h = 22;
      int slotX = x + 1 + Math.round(sx * 20.0F);
      switch (style) {
         case 2:
            Gfx.round(g, slotX + 2, y + 2, 16, 18, 4, 16777215, 0.1F);
            Gfx.round(g, slotX + 4, y + h - 2, 12, 2, 1, acc, 0.95F);
            break;
         case 3:
            for (int i = 0; i < 9; i++) {
               Gfx.round(g, x + 1 + i * 20, y + 1, 20, 20, 5, 921106, 0.62F);
            }

            Gfx.round(g, slotX, y + 1, 20, 20, 5, 16777215, 0.16F);
            outline(g, slotX, y + 1, 20, 20, 16777215);
            break;
         case 4:
            Gfx.glow(g, x, y, w, h, 6, acc, 0.45F, 5);
            Gfx.round(g, x, y, w, h, 6, 656914, 0.78F);
            outline(g, x, y, w, h, acc);
            Gfx.glow(g, slotX, y + 1, 20, 20, 5, acc, 1.0F, 5);
            Gfx.round(g, slotX, y + 1, 20, 20, 5, acc, 0.3F);
            outline(g, slotX, y + 1, 20, 20, Gfx.lerpColor(acc, 16777215, 0.5F));
            break;
         default:
            Gfx.round(g, x, y, w, h, 6, 1183260, 0.72F);
            Gfx.round(g, x, y, w, 1, 1, 16777215, 0.07F);
            Gfx.glow(g, slotX, y + 1, 20, 20, 5, acc, 0.9F, 4);
            Gfx.round(g, slotX, y + 1, 20, 20, 5, acc, 0.22F);
            outline(g, slotX, y + 1, 20, 20, acc);
      }

      for (int i = 0; i < 9; i++) {
         ItemStack st = items[i];
         if (st != null && !st.isEmpty()) {
            int ix = x + 3 + i * 20;
            int iy = y + 3;
            g.item(st, ix, iy);
            if (decorations) {
               g.itemDecorations(mc.font, st, ix, iy);
            }
         }
      }

      if (off != null && !off.isEmpty()) {
         int ox = x - 26;
         if (style != 2) {
            Gfx.round(g, ox, y, 22, 22, 6, style == 4 ? 656914 : (style == 3 ? 921106 : 1183260), style == 3 ? 0.62F : 0.72F);
         }

         g.item(off, ox + 3, y + 3);
         g.itemDecorations(mc.font, off, ox + 3, y + 3);
      }
   }

   private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int col) {
      Gfx.rect(g, x + 3, y, w - 6, 1, col, 0.95F);
      Gfx.rect(g, x + 3, y + h - 1, w - 6, 1, col, 0.95F);
      Gfx.rect(g, x, y + 3, 1, h - 6, col, 0.95F);
      Gfx.rect(g, x + w - 1, y + 3, 1, h - 6, col, 0.95F);
      Gfx.rect(g, x + 1, y + 1, 2, 1, col, 0.95F);
      Gfx.rect(g, x + 1, y + 2, 1, 1, col, 0.95F);
      Gfx.rect(g, x + w - 3, y + 1, 2, 1, col, 0.95F);
      Gfx.rect(g, x + w - 2, y + 2, 1, 1, col, 0.95F);
      Gfx.rect(g, x + 1, y + h - 2, 2, 1, col, 0.95F);
      Gfx.rect(g, x + 1, y + h - 3, 1, 1, col, 0.95F);
      Gfx.rect(g, x + w - 3, y + h - 2, 2, 1, col, 0.95F);
      Gfx.rect(g, x + w - 2, y + h - 3, 1, 1, col, 0.95F);
   }
}
