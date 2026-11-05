package dev.valence.cord.hud;

import com.mojang.blaze3d.platform.NativeImage;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Storage;
import dev.valence.cord.sky.LiveSky;
import dev.valence.cord.sky.SkyPainter;
import dev.valence.cord.sky.SkyPreset;
import dev.valence.cord.sky.SkyTextures;
import dev.valence.gui.RoundedImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class SkyThumbs {
   public static final int TW = 288;
   public static final int TH = 162;
   private static final ExecutorService EXEC = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "Valence-Thumbs");
      t.setDaemon(true);
      t.setPriority(1);
      return t;
   });
   private static final Map<String, String> SHOWN = new HashMap<>();
   private static final Map<String, NativeImage> PIXELS = new HashMap<>();
   private static final Set<String> QUEUED = ConcurrentHashMap.newKeySet();
   private static final ConcurrentLinkedQueue<SkyThumbs.Done> DONE = new ConcurrentLinkedQueue<>();
   private static final int CACHE_VER = 13;
   private static long lastUpload = 0L;

   private SkyThumbs() {
   }

   public static float[] look(String kind) {
      return switch (kind) {
         case "SYNTHWAVE" -> new float[]{180.0F, 11.0F};
         case "BLACK_HOLE" -> new float[]{165.0F, 22.0F};
         case "MIDNIGHT" -> new float[]{200.0F, 22.0F};
         case "BLOOD_MOON" -> new float[]{200.0F, 8.0F};
         case "GOLDEN_HOUR" -> new float[]{200.0F, 12.0F};
         case "COSMOS" -> new float[]{60.0F, 28.0F};
         case "DEEP_SEA" -> new float[]{40.0F, 32.0F};
         case "SAKURA" -> new float[]{60.0F, 12.0F};
         case "COTTON_CANDY" -> new float[]{120.0F, 26.0F};
         case "NEBULA" -> new float[]{40.0F, 26.0F};
         case "NEON_CITY" -> new float[]{60.0F, 16.0F};
         case "FROST" -> new float[]{210.0F, 24.0F};
         case "PULSAR" -> new float[]{30.0F, 34.0F};
         case "PRISM", "MATRIX", "INFERNO", "AQUARELL" -> new float[]{60.0F, 24.0F};
         default -> new float[]{150.0F, 20.0F};
      };
   }

   public static boolean draw(GuiGraphicsExtractor g, SkyPreset p, int x, int y, int w, int h, float alpha) {
      upload();
      SkyPainter.Spec s = SkyTextures.spec(p);
      String key = SkyTextures.key(s);
      String shown = SHOWN.get(p.name());
      if (!key.equals(shown)) {
         request(p, s, key);
      }

      if (shown == null) {
         return false;
      }

      NativeImage px = PIXELS.get(p.name());
      if (px != null) {
         RoundedImage.drawNative(g, "thumb:" + p.name() + "@" + shown, px, x, y, w, h, Gfx.argb(16777215, alpha));
      } else {
         g.blit(RenderPipelines.GUI_TEXTURED, id(p), x, y, 0.0F, 0.0F, w, h, w, h, Gfx.argb(16777215, alpha));
      }

      return true;
   }

   private static Path cacheFile(String key) {
      try {
         return Storage.dir().resolve("thumbs").resolve("v13_" + Integer.toHexString(key.hashCode()) + "_288.png");
      } catch (Throwable var2) {
         return null;
      }
   }

   private static Identifier id(SkyPreset p) {
      return Identifier.fromNamespaceAndPath("valence_cord", "thumb/" + p.name().toLowerCase(Locale.ROOT));
   }

   private static void request(SkyPreset p, SkyPainter.Spec s, String key) {
      if (QUEUED.add(key)) {
         EXEC.execute(() -> {
            try {
               Path file = cacheFile(key);
               if (file != null && Files.isRegularFile(file)) {
                  DONE.add(new SkyThumbs.Done(p.name(), key, NativeImage.read(Files.readAllBytes(file))));
                  return;
               }

               float[] lk = look(s.kind());
               int[] px = new LiveSky().view(p, s.a1(), s.a2(), 12.0, 288, 162, lk[0], -lk[1], 78.0F);
               BufferedImage img = new BufferedImage(288, 162, 2);
               img.setRGB(0, 0, 288, 162, px, 0, 288);
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               ImageIO.write(img, "png", out);
               byte[] png = out.toByteArray();
               if (file != null) {
                  try {
                     Files.createDirectories(file.getParent());
                     Files.write(file, png);
                  } catch (Exception var10) {
                  }
               }

               DONE.add(new SkyThumbs.Done(p.name(), key, NativeImage.read(png)));
            } catch (Throwable var11) {
               NeonLyrics.LOG.debug("Himmel-Vorschau fehlgeschlagen", var11);
            }
         });
      }
   }

   private static void upload() {
      long frame = System.nanoTime() / 5000000L;
      if (frame != lastUpload) {
         SkyThumbs.Done d = DONE.poll();
         if (d != null) {
            lastUpload = frame;

            try {
               SkyPreset p = SkyPreset.valueOf(d.name());
               String old = SHOWN.get(d.name());
               Minecraft.getInstance().getTextureManager().register(id(p), new DynamicTexture(() -> "valence_thumb_" + d.name(), d.img()));
               PIXELS.put(d.name(), d.img());
               if (old != null) {
                  RoundedImage.forget("thumb:" + d.name() + "@" + old);
               }

               SHOWN.put(d.name(), d.key());
            } catch (Throwable var4) {
               d.img().close();
            }

            QUEUED.remove(d.key());
         }
      }
   }

   private record Done(String name, String key, NativeImage img) {
   }
}
