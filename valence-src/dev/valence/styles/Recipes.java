package dev.valence.styles;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import dev.valence.ValenceClient;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class Recipes {
   private static Map<String, List<Recipes.Design>> designs;

   private Recipes() {
   }

   public static List<Recipes.Design> forCategory(String category) {
      if (designs == null) {
         load();
      }

      return designs.getOrDefault(category, List.of());
   }

   public static Recipes.Design find(String category, String id) {
      for (Recipes.Design d : forCategory(category)) {
         if (d.id().equals(id)) {
            return d;
         }
      }

      return null;
   }

   private static void load() {
      designs = new LinkedHashMap<>();

      try {
         label80: {
            try (InputStream in = Recipes.class.getResourceAsStream("/assets/valence/styles/designs.json")) {
               if (in != null) {
                  JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();

                  for (Entry<String, JsonElement> e : root.entrySet()) {
                     if (e.getValue().isJsonArray()) {
                        List<Recipes.Design> list = new ArrayList<>();

                        for (JsonElement el : e.getValue().getAsJsonArray()) {
                           JsonObject o = el.getAsJsonObject();
                           list.add(new Recipes.Design(o.get("id").getAsString(), 0xFF000000 | parse(o.get("swatch").getAsString()), o.getAsJsonArray("ops")));
                        }

                        designs.put(e.getKey(), list);
                     }
                  }
                  break label80;
               }
            }

            return;
         }
      } catch (Exception e) {
         ValenceClient.LOGGER.error("Valence: could not load designs.json", e);
      }
   }

   private static int parse(String hex) {
      String h = hex.startsWith("#") ? hex.substring(1) : hex;
      return Integer.parseInt(h.substring(0, 6), 16);
   }

   private static int lowbias(int x) {
      x ^= x >>> 16;
      x *= 2146121005;
      x ^= x >>> 15;
      x *= -2073254261;
      return x ^ x >>> 16;
   }

   private static float hash(int x, int y, int seed) {
      int v = lowbias(x + lowbias(y + lowbias(seed)));
      return (float)(v & 4294967295L) / 4.2949673E9F;
   }

   private static float vnoise(float x, float y, int cell, int period, int seed) {
      float gx = x / cell;
      float gy = y / cell;
      int x0 = (int)Math.floor(gx);
      int y0 = (int)Math.floor(gy);
      float fx = gx - x0;
      float fy = gy - y0;
      fx = fx * fx * (3.0F - 2.0F * fx);
      fy = fy * fy * (3.0F - 2.0F * fy);
      int p = period > 0 ? Math.max(1, period / cell) : 1048576;
      float a = hash(Math.floorMod(x0, p), Math.floorMod(y0, p), seed);
      float b = hash(Math.floorMod(x0 + 1, p), Math.floorMod(y0, p), seed);
      float c = hash(Math.floorMod(x0, p), Math.floorMod(y0 + 1, p), seed);
      float d = hash(Math.floorMod(x0 + 1, p), Math.floorMod(y0 + 1, p), seed);
      float top = a + (b - a) * fx;
      float bottom = c + (d - c) * fx;
      return top + (bottom - top) * fy;
   }

   private static float lum(int rgb) {
      return (0.299F * (rgb >> 16 & 0xFF) + 0.587F * (rgb >> 8 & 0xFF) + 0.114F * (rgb & 0xFF)) / 255.0F;
   }

   private static float[] hsv(int rgb) {
      float r = (rgb >> 16 & 0xFF) / 255.0F;
      float g = (rgb >> 8 & 0xFF) / 255.0F;
      float b = (rgb & 0xFF) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      float h = 0.0F;
      if (d > 1.0E-5F) {
         if (max == r) {
            h = (g - b) / d % 6.0F;
         } else if (max == g) {
            h = (b - r) / d + 2.0F;
         } else {
            h = (r - g) / d + 4.0F;
         }

         h /= 6.0F;
         if (h < 0.0F) {
            h++;
         }
      }

      return new float[]{h, max <= 0.0F ? 0.0F : d / max, max};
   }

   private static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = (int)((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
      int g = (int)((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
      int bl = (int)((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
      return r << 16 | g << 8 | bl;
   }

   private static int ramp(int[] cols, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float pos = t * (cols.length - 1);
      int i = Math.min(cols.length - 2, (int)pos);
      return mix(cols[i], cols[i + 1], pos - i);
   }

   private static boolean[] region(String category, String name, int[] px, int w, int h) {
      boolean[] m = new boolean[px.length];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int i = y * w + x;
            if (px[i] >>> 24 != 0) {
               m[i] = switch (name) {
                  case "glass" -> x < w / 2.0F && y < h / 2.0F;
                  case "core" -> x >= w / 2.0F && y < h / 2.0F;
                  case "base" -> y >= h / 2.0F;
                  case "eyes", "body" -> {
                     float[] c = hsv(px[i]);
                     boolean eye = c[0] > 0.22F && c[0] < 0.55F && c[1] > 0.3F;
                     yield "eyes".equals(name) == eye;
                  }
                  case "glow", "frame" -> {
                     float[] c = hsv(px[i]);
                     boolean glow = c[1] > 0.45F && c[2] > 0.4F;
                     yield "glow".equals(name) == glow;
                  }
                  default -> true;
               };
            }
         }
      }

      return m;
   }

   private static String str(JsonObject o, String k, String def) {
      return o.has(k) ? o.get(k).getAsString() : def;
   }

   private static float num(JsonObject o, String k, float def) {
      return o.has(k) ? o.get(k).getAsFloat() : def;
   }

   public static NativeImage apply(String category, NativeImage img, Recipes.Design design) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] px = new int[w * h];

      label352:
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            px[y * w + x] = img.getPixel(x, y);
         }
      }

      for (JsonElement el : design.ops()) {
         JsonObject op = el.getAsJsonObject();
         String kind = op.get("op").getAsString();
         boolean[] mask = region(category, str(op, "region", "all"), px, w, h);
         switch (kind) {
            case "map":
               JsonArray ca = op.getAsJsonArray("colors");
               int[] cols = new int[ca.size()];

               for (int i = 0; i < cols.length; i++) {
                  cols[i] = parse(ca.get(i).getAsString());
               }

               float gamma = num(op, "gamma", 1.0F);
               float lo = num(op, "lo", 0.0F);
               float hi = num(op, "hi", 1.0F);

               for (int i = 0; i < px.length; i++) {
                  if (mask[i]) {
                     float t = (lum(px[i]) - lo) / Math.max(1.0E-6F, hi - lo);
                     t = (float)Math.pow(Math.max(0.0F, Math.min(1.0F, t)), gamma);
                     px[i] = px[i] & 0xFF000000 | ramp(cols, t);
                  }
               }
               break;
            case "gradient":
               int c0 = parse(op.get("from").getAsString());
               int c1 = parse(op.get("to").getAsString());
               boolean vert = !"h".equals(str(op, "dir", "v"));
               float amt = num(op, "amount", 1.0F);
               int fh = (int)num(op, "frame", h);

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     if (mask[i]) {
                        float k = vert ? (float)(y % fh) / Math.max(1, fh - 1) : (float)x / Math.max(1, w - 1);
                        float l = lum(px[i]);
                        int c = mix(c0, c1, k);
                        float f = 0.35F + 1.1F * l;
                        int tr = Math.min(255, (int)((c >> 16 & 0xFF) * f));
                        int tg = Math.min(255, (int)((c >> 8 & 0xFF) * f));
                        int tb = Math.min(255, (int)((c & 0xFF) * f));
                        px[i] = px[i] & 0xFF000000 | mix(px[i] & 16777215, tr << 16 | tg << 8 | tb, amt);
                     }
                  }
               }
               break label352;
            case "specks":
               int c = parse(op.get("color").getAsString());
               float dens = num(op, "density", 0.08F);
               int seed = (int)num(op, "seed", 1.0F);

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     if (mask[i] && hash(x, y, seed) < dens) {
                        float br = 0.6F + 0.4F * hash(x, y, seed + 7);
                        px[i] = px[i] & 0xFF000000 | mix(px[i] & 16777215, c, br);
                     }
                  }
               }
               break label352;
            case "veins":
               int c = parse(op.get("color").getAsString());
               int seed = (int)num(op, "seed", 3.0F);
               int cell = (int)num(op, "cell", 6.0F);
               float width = num(op, "width", 0.06F);

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     if (mask[i]) {
                        float n = vnoise(x, y, cell, w, seed);
                        float dist = Math.abs(n - 0.5F);
                        if (dist < width) {
                           px[i] = px[i] & 0xFF000000 | mix(px[i] & 16777215, c, 1.0F - dist / width);
                        }
                     }
                  }
               }
               break label352;
            case "border":
               int c = parse(op.get("color").getAsString());
               int wd = (int)num(op, "width", 1.0F);
               int fh = (int)num(op, "frame", h);

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     int yy = y % fh;
                     if (mask[i] && (x < wd || x >= w - wd || yy < wd || yy >= fh - wd)) {
                        px[i] = px[i] & 0xFF000000 | c;
                     }
                  }
               }
               break label352;
            case "outline":
               int c = parse(op.get("color").getAsString());
               int[] out = (int[])px.clone();

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     if (mask[i]) {
                        boolean edge = x == 0
                           || y == 0
                           || x == w - 1
                           || y == h - 1
                           || px[i - 1] >>> 24 == 0
                           || px[i + 1] >>> 24 == 0
                           || px[i - w] >>> 24 == 0
                           || px[i + w] >>> 24 == 0;
                        if (edge) {
                           out[i] = px[i] & 0xFF000000 | c;
                        }
                     }
                  }
               }

               px = out;
               break;
            case "smooth":
               float k = num(op, "amount", 0.5F);
               long r = 0L;
               long g = 0L;
               long b = 0L;
               int n = 0;

               for (int i = 0; i < px.length; i++) {
                  if (mask[i]) {
                     r += px[i] >> 16 & 0xFF;
                     g += px[i] >> 8 & 0xFF;
                     b += px[i] & 0xFF;
                     n++;
                  }
               }

               if (n <= 0) {
                  break;
               }

               int mean = (int)(r / n) << 16 | (int)(g / n) << 8 | (int)(b / n);

               for (int i = 0; i < px.length; i++) {
                  if (mask[i]) {
                     px[i] = px[i] & 0xFF000000 | mix(px[i] & 16777215, mean, k);
                  }
               }
               break label352;
            case "contrast":
               float k = num(op, "amount", 1.3F);

               for (int i = 0; i < px.length; i++) {
                  if (mask[i]) {
                     int r = clamp(((px[i] >> 16 & 0xFF) - 128) * k + 128.0F);
                     int g = clamp(((px[i] >> 8 & 0xFF) - 128) * k + 128.0F);
                     int b = clamp(((px[i] & 0xFF) - 128) * k + 128.0F);
                     px[i] = px[i] & 0xFF000000 | r << 16 | g << 8 | b;
                  }
               }
               break label352;
            case "shrink":
               float s = num(op, "scale", 0.75F);
               int dy = (int)num(op, "dy", 0.0F);
               int nw = Math.max(1, Math.round(w * s));
               int nh = Math.max(1, Math.round(h * s));
               int ox = (w - nw) / 2;
               int oy = (h - nh) / 2 + dy;
               int[] out = new int[px.length];

               for (int y = 0; y < nh; y++) {
                  for (int x = 0; x < nw; x++) {
                     int sx = Math.min(w - 1, (int)(x / s));
                     int sy = Math.min(h - 1, (int)(y / s));
                     int tx = x + ox;
                     int ty = y + oy;
                     if (tx >= 0 && ty >= 0 && tx < w && ty < h) {
                        out[ty * w + tx] = px[sy * w + sx];
                     }
                  }
               }

               px = out;
               break;
            case "galaxy":
               int seed = (int)num(op, "seed", 5.0F);
               int c0 = parse(str(op, "dark", "#120a2e"));
               int c1 = parse(str(op, "light", "#7b4dff"));

               for (int y = 0; y < h; y++) {
                  for (int x = 0; x < w; x++) {
                     int i = y * w + x;
                     if (mask[i]) {
                        float l = lum(px[i]);
                        float n = vnoise(x, y, 4, w, seed) * 0.6F + vnoise(x, y, 2, w, seed + 1) * 0.4F;
                        int c = mix(c0, c1, Math.max(0.0F, Math.min(1.0F, 0.25F * l + 0.9F * n - 0.15F)));
                        if (hash(x, y, seed + 9) < 0.05F) {
                           c = 16777215;
                        }

                        px[i] = px[i] & 0xFF000000 | c;
                     }
                  }
               }
         }
      }

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            img.setPixel(x, y, px[y * w + x]);
         }
      }

      return img;
   }

   private static int clamp(float v) {
      return Math.max(0, Math.min(255, Math.round(v)));
   }

   public record Design(String id, int swatch, JsonArray ops) {
   }
}
