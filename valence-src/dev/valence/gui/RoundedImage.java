package dev.valence.gui;

import com.mojang.blaze3d.platform.NativeImage;
import dev.valence.ValenceClient;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class RoundedImage {
   private static final int MAX_BAKED = 96;
   private static final Map<Identifier, NativeImage> SOURCES = new HashMap<>();
   private static final LinkedHashMap<String, RoundedImage.Baked> BAKED = new LinkedHashMap<>(64, 0.75F, true);
   private static int counter;
   public static float menuScale = 1.0F;

   private RoundedImage() {
   }

   public static int radiusFor(int w, int h) {
      int m = Math.min(w, h);
      return m >= 44 ? 8 : (m >= 14 ? 5 : 3);
   }

   private static NativeImage source(Identifier tex) {
      NativeImage img = SOURCES.get(tex);
      if (img != null) {
         return img;
      }

      try (InputStream in = Minecraft.getInstance().getResourceManager().open(tex)) {
         img = NativeImage.read(in);
         SOURCES.put(tex, img);
      } catch (Exception e) {
         ValenceClient.LOGGER.warn("Valence: could not load {}", tex, e);
      }

      return img;
   }

   private static int scale() {
      return Math.max(1, (int)Math.ceil(Minecraft.getInstance().getWindow().getGuiScale() * menuScale - 0.01));
   }

   public static void draw(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h, float u, float v, float uw, float vh, int color) {
      if (w > 0 && h > 0) {
         NativeImage src = source(tex);
         if (src == null) {
            Theme.image(g, tex, x, y, w, h, Math.round(uw), Math.round(vh), color);
         } else {
            String key = tex + "|" + Math.round(u * 10.0F) + "," + Math.round(v * 10.0F) + "," + Math.round(uw * 10.0F) + "," + Math.round(vh * 10.0F);
            blit(g, key, src, x, y, w, h, u, v, uw, vh, radiusFor(w, h), color);
         }
      }
   }

   public static void drawNative(GuiGraphicsExtractor g, String key, NativeImage src, int x, int y, int w, int h, int color) {
      drawNative(g, key, src, x, y, w, h, radiusFor(w, h), color);
   }

   public static void drawNative(GuiGraphicsExtractor g, String key, NativeImage src, int x, int y, int w, int h, int radius, int color) {
      if (src != null && w > 0 && h > 0) {
         blit(g, key, src, x, y, w, h, 0.0F, 0.0F, src.getWidth(), src.getHeight(), radius, color);
      }
   }

   private static void blit(
      GuiGraphicsExtractor g, String srcKey, NativeImage src, int x, int y, int w, int h, float u, float v, float uw, float vh, int radius, int color
   ) {
      int scale = scale();
      String key = srcKey + "|" + w + "x" + h + "|" + scale + "|r" + radius;
      RoundedImage.Baked b = BAKED.get(key);
      if (b == null) {
         b = bake(src, w, h, scale, radius, u, v, uw, vh);
         if (b == null) {
            return;
         }

         BAKED.put(key, b);
         trim();
      }

      g.blit(RenderPipelines.GUI_TEXTURED, b.id(), x, y, 0.0F, 0.0F, w, h, b.pw(), b.ph(), b.pw(), b.ph(), color);
   }

   public static void forget(String srcKeyPrefix) {
      Iterator<Entry<String, RoundedImage.Baked>> it = BAKED.entrySet().iterator();

      while (it.hasNext()) {
         Entry<String, RoundedImage.Baked> e = it.next();
         if (e.getKey().startsWith(srcKeyPrefix)) {
            release(e.getValue());
            it.remove();
         }
      }
   }

   private static void trim() {
      Iterator<Entry<String, RoundedImage.Baked>> it = BAKED.entrySet().iterator();

      while (BAKED.size() > 96 && it.hasNext()) {
         RoundedImage.Baked old = it.next().getValue();
         it.remove();
         release(old);
      }
   }

   private static void release(RoundedImage.Baked b) {
      try {
         Minecraft.getInstance().getTextureManager().release(b.id());
      } catch (Throwable var2) {
      }
   }

   private static RoundedImage.Baked bake(NativeImage src, int w, int h, int scale, int radius, float u, float v, float uw, float vh) {
      int pw = w * scale;
      int ph = h * scale;
      float rad = radius * scale;
      NativeImage out = new NativeImage(pw, ph, true);
      int sw = src.getWidth();
      int sh = src.getHeight();
      float stepX = uw / pw;
      float stepY = vh / ph;
      int taps = !(stepX > 1.6F) && !(stepY > 1.6F) ? 1 : 2;

      for (int py = 0; py < ph; py++) {
         for (int px = 0; px < pw; px++) {
            int argb;
            if (taps == 1) {
               argb = bilinear(src, u + (px + 0.5F) * stepX - 0.5F, v + (py + 0.5F) * stepY - 0.5F, sw, sh);
            } else {
               int a = 0;
               int r = 0;
               int gg = 0;
               int bb = 0;

               for (int ty = 0; ty < 2; ty++) {
                  for (int tx = 0; tx < 2; tx++) {
                     int c = bilinear(src, u + (px + 0.25F + 0.5F * tx) * stepX - 0.5F, v + (py + 0.25F + 0.5F * ty) * stepY - 0.5F, sw, sh);
                     a += c >>> 24 & 0xFF;
                     r += c >> 16 & 0xFF;
                     gg += c >> 8 & 0xFF;
                     bb += c & 0xFF;
                  }
               }

               argb = a >> 2 << 24 | r >> 2 << 16 | gg >> 2 << 8 | bb >> 2;
            }

            float cx = Math.min(px + 0.5F, pw - px - 0.5F);
            float cy = Math.min(py + 0.5F, ph - py - 0.5F);
            float cover = 1.0F;
            if (cx < rad && cy < rad) {
               float dx = rad - cx;
               float dy = rad - cy;
               float d = (float)Math.sqrt(dx * dx + dy * dy);
               cover = Math.max(0.0F, Math.min(1.0F, rad - d + 0.5F));
            }

            int a = Math.round((argb >>> 24 & 0xFF) * cover);
            out.setPixel(px, py, a << 24 | argb & 16777215);
         }
      }

      Identifier id = Theme.id("dynamic/rounded/" + counter++);
      Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "Valence rounded image", out));
      return new RoundedImage.Baked(id, pw, ph);
   }

   private static int bilinear(NativeImage img, float x, float y, int w, int h) {
      int x0 = (int)Math.floor(x);
      int y0 = (int)Math.floor(y);
      float fx = x - x0;
      float fy = y - y0;
      int c00 = px(img, x0, y0, w, h);
      int c10 = px(img, x0 + 1, y0, w, h);
      int c01 = px(img, x0, y0 + 1, w, h);
      int c11 = px(img, x0 + 1, y0 + 1, w, h);
      int out = 0;

      for (int sh = 0; sh < 32; sh += 8) {
         float a = (c00 >>> sh & 0xFF) * (1.0F - fx) + (c10 >>> sh & 0xFF) * fx;
         float b = (c01 >>> sh & 0xFF) * (1.0F - fx) + (c11 >>> sh & 0xFF) * fx;
         int ch = Math.round(a * (1.0F - fy) + b * fy);
         out |= Math.max(0, Math.min(255, ch)) << sh;
      }

      return out;
   }

   private static int px(NativeImage img, int x, int y, int w, int h) {
      return img.getPixel(Math.max(0, Math.min(w - 1, x)), Math.max(0, Math.min(h - 1, y)));
   }

   private record Baked(Identifier id, int pw, int ph) {
   }
}
