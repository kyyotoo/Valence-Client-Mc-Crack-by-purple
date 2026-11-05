package dev.valence.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class Theme {
   public static final int PANEL = -1207105001;
   public static final int SIDEBAR = 452984831;
   public static final int CARD = 318767103;
   public static final int CARD_HOVER = 587202559;
   public static final int CARD_ACTIVE = 1149983990;
   public static final int BORDER = 536870911;
   public static final int BORDER_HOVER = 1439281407;
   public static final int PANEL_BORDER = 1086959871;
   public static final int TRACK = 872415231;
   public static final int ACCENT = -7643914;
   public static final int ACCENT_LIGHT = -4745985;
   public static final int ACCENT_DARK = -11785570;
   public static final int TEXT = -724737;
   public static final int TEXT_DIM = -4608298;
   public static final int TEXT_MUTED = -8095322;
   public static final int GOOD = -11870592;
   public static final int WARN = -278748;
   public static final int BAD = -495247;
   public static final Identifier LOGO = id("textures/gui/logo.png");
   public static final Identifier SPOTLIGHT = id("textures/gui/spotlight.png");
   public static final Identifier BANNER = id("textures/gui/banner.png");
   private static final Identifier[] FILL = new Identifier[]{sprite("r3"), sprite("r5"), sprite("r8")};
   private static final Identifier[] LINE = new Identifier[]{sprite("r3_line"), sprite("r5_line"), sprite("r8_line")};
   private static final Identifier[] SHEEN = new Identifier[]{sprite("r3_sheen"), sprite("r5_sheen"), sprite("r8_sheen")};
   private static final Identifier PILL = sprite("pill");
   private static final Identifier PILL_LINE = sprite("pill_line");
   private static final Identifier SHADOW = sprite("shadow");
   private static final Identifier GLOW = sprite("glow");

   private Theme() {
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("valence", path);
   }

   private static Identifier sprite(String name) {
      return id("glass/" + name);
   }

   public static Identifier icon(String name) {
      return id("textures/gui/icons/" + name + ".png");
   }

   private static int radiusIndex(int w, int h) {
      int m = Math.min(w, h);
      if (m >= 44) {
         return 2;
      } else {
         return m >= 14 ? 1 : 0;
      }
   }

   public static void round(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
      int w = x2 - x1;
      int h = y2 - y1;
      if (w > 0 && h > 0 && color >>> 24 != 0) {
         if (w >= 4 && h >= 4) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, FILL[radiusIndex(w, h)], x1, y1, w, h, color);
         } else {
            g.fill(x1, y1, x2, y2, color);
         }
      }
   }

   public static void outline(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
      int w = x2 - x1;
      int h = y2 - y1;
      if (w >= 4 && h >= 4 && color >>> 24 != 0) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, LINE[radiusIndex(w, h)], x1, y1, w, h, color);
      }
   }

   public static void sheen(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, float strength) {
      int w = x2 - x1;
      int h = y2 - y1;
      if (w >= 4 && h >= 4 && !(strength <= 0.01F)) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, SHEEN[radiusIndex(w, h)], x1, y1, w, h, withAlpha(-1, strength));
      }
   }

   public static void card(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int fill, int border) {
      round(g, x1, y1, x2, y2, fill);
      outline(g, x1, y1, x2, y2, border);
   }

   public static void glass(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int fill, int border, float shadow) {
      if (shadow > 0.0F) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, SHADOW, x1 - 14, y1 - 12, x2 - x1 + 28, y2 - y1 + 28, withAlpha(-1, shadow));
      }

      round(g, x1, y1, x2, y2, fill);
      sheen(g, x1, y1, x2, y2, 0.9F);
      outline(g, x1, y1, x2, y2, border);
   }

   public static void glow(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color, float strength) {
      if (!(strength <= 0.01F)) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, GLOW, x1 - 14, y1 - 14, x2 - x1 + 28, y2 - y1 + 28, withAlpha(color, strength));
      }
   }

   public static void pill(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
      if (x2 - x1 >= 2 && y2 - y1 >= 2 && color >>> 24 != 0) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, PILL, x1, y1, x2 - x1, y2 - y1, color);
      }
   }

   public static void pillLine(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
      if (x2 - x1 >= 2 && y2 - y1 >= 2 && color >>> 24 != 0) {
         g.blitSprite(RenderPipelines.GUI_TEXTURED, PILL_LINE, x1, y1, x2 - x1, y2 - y1, color);
      }
   }

   public static void card(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, float hover, boolean selected) {
      int fill = selected ? lerp(1149983990, 1519082742, hover) : lerp(318767103, 587202559, hover);
      int border = selected ? lerp(-1282712330, -4745985, hover) : lerp(536870911, 1439281407, hover);
      if (selected) {
         glow(g, x1, y1, x2, y2, -7643914, 0.35F + 0.25F * hover);
      } else {
         glow(g, x1, y1, x2, y2, -7643914, 0.18F * hover);
      }

      round(g, x1, y1, x2, y2, fill);
      if (hover > 0.01F || selected) {
         sheen(g, x1, y1, x2, y2, 0.35F + 0.4F * hover);
      }

      outline(g, x1, y1, x2, y2, border);
   }

   public static void icon(GuiGraphicsExtractor g, String name, int x, int y, int size, int color) {
      g.blit(RenderPipelines.GUI_TEXTURED, icon(name), x, y, 0.0F, 0.0F, size, size, 16, 16, 16, 16, color);
   }

   public static void image(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h, int texW, int texH, int color) {
      g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0F, 0.0F, w, h, texW, texH, texW, texH, color);
   }

   public static int withAlpha(int color, float alpha) {
      int a = (int)((color >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return a << 24 | color & 16777215;
   }

   public static int lerp(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int aa = a >>> 24 & 0xFF;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24 & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)(aa + (ba - aa) * t) << 24 | (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   public static float easeInOutCubic(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
   }

   public static float easeOutCubic(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float k = 1.0F - t;
      return 1.0F - k * k * k;
   }
}
