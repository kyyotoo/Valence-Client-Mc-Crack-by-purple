package dev.valence.styles;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Mth;

public final class StyleEffects {
   public static final String[] PATTERNS = new String[]{"neon", "frost", "aurora", "sunset", "chrome", "candy", "void", "glass"};

   private StyleEffects() {
   }

   public static boolean isPattern(String id) {
      for (String p : PATTERNS) {
         if (p.equals(id)) {
            return true;
         }
      }

      return false;
   }

   public static int swatch(String id) {
      return switch (id) {
         case "galaxy" -> -10801200;
         case "neon" -> -49680;
         case "lava" -> -38374;
         case "frost" -> -6298369;
         case "aurora" -> -12714064;
         case "sunset" -> -34166;
         case "chrome" -> -2563856;
         case "candy" -> -25899;
         case "void" -> -14022070;
         case "glass" -> -4197633;
         default -> -1;
      };
   }

   public static void apply(NativeImage img, String id, boolean whole) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] src = new int[w * h];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            src[y * w + x] = img.getPixel(x, y);
         }
      }

      boolean[] mask = whole ? fullMask(src) : StyleManager.mainColorMask(src, w, h);
      float[] lum = new float[w * h];

      for (int i = 0; i < src.length; i++) {
         lum[i] = luminance(src[i]);
      }

      int cell = Math.max(1, Math.min(w, h) / 16);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int i = y * w + x;
            int a = src[i] >>> 24;
            if (a != 0 && mask[i]) {
               float l = lum[i];
               boolean edge = isEdge(src, lum, mask, w, h, x, y);
               int px = x / cell;
               int py = y / cell;

               int rgb = switch (id) {
                  case "galaxy" -> galaxy(px, py, l, w / cell);
                  case "neon" -> neon(py, h / cell, l, edge);
                  case "lava" -> lava(px, py, l, w / cell);
                  case "frost" -> frost(px, py, l, edge);
                  case "aurora" -> aurora(px, py, w / cell, h / cell, l);
                  case "sunset" -> sunset(py, h / cell, l);
                  case "chrome" -> chrome(py, l);
                  case "candy" -> candy(px, py, l);
                  case "void" -> voidStyle(l, edge);
                  case "glass" -> glass(src[i], l, edge);
                  default -> src[i] & 16777215;
               };
               img.setPixel(x, y, a << 24 | rgb & 16777215);
            }
         }
      }
   }

   private static boolean[] fullMask(int[] src) {
      boolean[] m = new boolean[src.length];

      for (int i = 0; i < src.length; i++) {
         m[i] = src[i] >>> 24 > 0;
      }

      return m;
   }

   private static boolean isEdge(int[] src, float[] lum, boolean[] mask, int w, int h, int x, int y) {
      int i = y * w + x;
      int[][] n = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

      for (int[] d : n) {
         int nx = x + d[0];
         int ny = y + d[1];
         if (nx >= 0 && ny >= 0 && nx < w && ny < h) {
            int j = ny * w + nx;
            if (src[j] >>> 24 == 0 || !mask[j]) {
               return true;
            }

            if (Math.abs(lum[i] - lum[j]) > 0.28F) {
               return true;
            }
         }
      }

      return false;
   }

   private static int galaxy(int x, int y, float l, int period) {
      float n = noise(x, y, 4, period, 11);
      float n2 = noise(x, y, 3, period, 29);
      int base = lerp(919590, 3808390, (float)Math.pow(l, 1.1));
      base = lerp(base, 14174952, Math.max(0.0F, n - 0.55F) * 1.4F);
      base = lerp(base, 3837183, Math.max(0.0F, n2 - 0.6F) * 1.2F);
      float star = hash(x, y, 7);
      if (star > 0.93F) {
         base = lerp(base, 16777215, 0.6F + (star - 0.93F) * 5.0F);
      }

      return base;
   }

   private static int neon(int y, int h, float l, boolean edge) {
      float t = h <= 1 ? 0.0F : (float)y / (h - 1);
      int neonCol = lerp(16727536, 4059391, t);
      return edge ? lerp(neonCol, 16777215, 0.2F) : lerp(787992, multiply(neonCol, 0.35F), 0.3F + 0.5F * l);
   }

   private static int lava(int x, int y, float l, int period) {
      float n = noise(x, y, 4, period, 3);
      float ridge = 1.0F - Math.abs(2.0F * n - 1.0F);
      int base = multiply(2754565, 0.55F + 0.6F * l);
      if (ridge > 0.78F) {
         float k = (ridge - 0.78F) / 0.22F;
         return lerp(16730634, 16769136, k);
      } else {
         return lerp(base, 8002056, Math.max(0.0F, ridge - 0.55F) * 2.0F);
      }
   }

   private static int frost(int x, int y, float l, boolean edge) {
      int c = lerp(4165600, 16055551, (float)Math.pow(l, 0.75));
      if (edge) {
         c = lerp(c, 15269887, 0.6F);
      }

      if (hash(x, y, 5) > 0.95F) {
         c = 16777215;
      }

      return c;
   }

   private static int aurora(int x, int y, int w, int h, float l) {
      float t = (float)(x + y) / Math.max(1, w + h);
      float hue = 0.38F + t * 0.42F;
      return Mth.hsvToRgb(hue % 1.0F, 0.65F, 0.35F + 0.65F * l) & 16777215;
   }

   private static int sunset(int y, int h, float l) {
      float t = h <= 1 ? 0.0F : (float)y / (h - 1);
      int c = t < 0.5F ? lerp(16765802, 16734858, t * 2.0F) : lerp(16734858, 6961919, (t - 0.5F) * 2.0F);
      return multiply(c, 0.45F + 0.65F * l);
   }

   private static int chrome(int y, float l) {
      float v = 0.5F + 0.5F * (float)Math.sin(l * 9.0F + y * 0.35F);
      return lerp(3423312, 16054271, v * 0.8F + l * 0.2F);
   }

   private static int candy(int x, int y, float l) {
      boolean stripe = (x + y) / 2 % 2 == 0;
      int c = stripe ? 16748496 : 16773882;
      return multiply(c, 0.62F + 0.45F * l);
   }

   private static int voidStyle(float l, boolean edge) {
      return edge ? lerp(10107647, 14725375, l) : lerp(131077, 2755146, 1.0F - l);
   }

   private static int glass(int argb, float l, boolean edge) {
      int c = lerp(multiply(argb & 16777215, 0.4F), 10479871, 0.6F);
      c = lerp(c, 16777215, l * l * 0.6F);
      if (edge) {
         c = lerp(c, 16777215, 0.55F);
      }

      return c;
   }

   static float luminance(int argb) {
      float r = (argb >> 16 & 0xFF) / 255.0F;
      float g = (argb >> 8 & 0xFF) / 255.0F;
      float b = (argb & 0xFF) / 255.0F;
      return 0.299F * r + 0.587F * g + 0.114F * b;
   }

   private static int lerp(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return (int)(ar + (br - ar) * t) << 16 | (int)(ag + (bg - ag) * t) << 8 | (int)(ab + (bb - ab) * t);
   }

   private static int multiply(int rgb, float f) {
      int r = Math.min(255, (int)((rgb >> 16 & 0xFF) * f));
      int g = Math.min(255, (int)((rgb >> 8 & 0xFF) * f));
      int b = Math.min(255, (int)((rgb & 0xFF) * f));
      return r << 16 | g << 8 | b;
   }

   private static float hash(int x, int y, int seed) {
      int h = x * 374761393 + y * 668265263 + seed * Integer.MAX_VALUE;
      h = (h ^ h >>> 13) * 1274126177;
      h ^= h >>> 16;
      return (h & 16777215) / 1.6777215E7F;
   }

   private static float noise(int x, int y, int cell, int period, int seed) {
      int cells = Math.max(1, period / cell);
      float fx = (float)x / cell;
      float fy = (float)y / cell;
      int x0 = (int)Math.floor(fx);
      int y0 = (int)Math.floor(fy);
      float tx = fx - x0;
      float ty = fy - y0;
      tx = tx * tx * (3.0F - 2.0F * tx);
      ty = ty * ty * (3.0F - 2.0F * ty);
      float a = hash(Math.floorMod(x0, cells), Math.floorMod(y0, cells), seed);
      float b = hash(Math.floorMod(x0 + 1, cells), Math.floorMod(y0, cells), seed);
      float c = hash(Math.floorMod(x0, cells), Math.floorMod(y0 + 1, cells), seed);
      float d = hash(Math.floorMod(x0 + 1, cells), Math.floorMod(y0 + 1, cells), seed);
      return Mth.lerp(ty, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d));
   }
}
