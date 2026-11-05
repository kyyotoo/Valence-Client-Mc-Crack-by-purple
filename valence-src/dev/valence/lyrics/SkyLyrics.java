package dev.valence.lyrics;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.valence.config.ValenceConfig;
import dev.valence.gui.Theme;
import dev.valence.license.License;
import dev.valence.render.MultiBufferSource;
import dev.valence.render.Txt;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType.Skybox;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

public final class SkyLyrics {
   private static final Identifier GLOW = Identifier.fromNamespaceAndPath("valence", "textures/lyrics/glow.png");
   private static final int MAX_ENTRIES = 3;
   private static final float SKY_DISTANCE = 90.0F;
   private static final float WORLD_SCALE = 0.034F;
   private static final float MAX_ROW_PX = 150.0F;
   private static final float LINE_H = 13.0F;
   private static final long APPEAR_MS = 300L;
   private static final long FADE_MS = 600L;
   private static final long EXIT_MS = 450L;
   private static final float[][] OUTLINE = new float[][]{{0.75F, 0.0F}, {-0.75F, 0.0F}, {0.0F, 0.75F}, {0.0F, -0.75F}};
   private static final List<SkyLyrics.Entry> ENTRIES = new ArrayList<>();
   private static final Random RANDOM = new Random();
   private static MultiBufferSource.BufferSource skyBuffers;
   private static long lastCombatMs = 0L;
   private static final List<SkyLyrics.Burst> BURSTS = new ArrayList<>();
   private static final int MAX_BURSTS = 40;
   private static final long BURST_IN_MS = 260L;
   private static final long BURST_OUT_MS = 520L;
   private static float[] lastSpots = new float[0];
   private static final Matrix4f TMP = new Matrix4f();

   private SkyLyrics() {
   }

   public static void tickCombat() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         boolean attacking = mc.options.keyAttack.isDown() && mc.crosshairPickEntity != null;
         if (mc.player.hurtTime > 0 || attacking) {
            lastCombatMs = System.currentTimeMillis();
         }
      }
   }

   public static SkyLyrics.Mode currentMode() {
      ValenceConfig cfg = ValenceConfig.get();
      String modeId = cfg.lyricsMode == null ? "sky" : cfg.lyricsMode;

      SkyLyrics.Mode m = switch (modeId) {
         case "world" -> SkyLyrics.Mode.WORLD;
         case "screen" -> SkyLyrics.Mode.SCREEN;
         case "bar" -> SkyLyrics.Mode.NONE;
         case "auto" -> System.currentTimeMillis() - lastCombatMs < 8000L ? SkyLyrics.Mode.WORLD : SkyLyrics.Mode.SKY;
         default -> SkyLyrics.Mode.SKY;
      };
      Minecraft mc = Minecraft.getInstance();
      if (m == SkyLyrics.Mode.SKY && mc.level != null && mc.level.dimensionType().skybox() == Skybox.NONE) {
         m = SkyLyrics.Mode.SCREEN;
      }

      return m;
   }

   public static synchronized void clear() {
      ENTRIES.clear();
      BURSTS.clear();
   }

   private static boolean singleWord() {
      return "single".equals(ValenceConfig.get().lyricsAnimation);
   }

   public static synchronized void addLine(String[] lineWords, long[] wordStartMs, long startMs, long endMs) {
      if (singleWord()) {
         String[] words = transform(lineWords);

         for (int i = 0; i < words.length; i++) {
            addBurst(words[i], wordStartMs[i], false);
         }
      } else {
         SkyLyrics.Entry e = new SkyLyrics.Entry();
         e.words = transform(lineWords);
         e.wordTimes = (long[])wordStartMs.clone();
         if ("line".equals(ValenceConfig.get().lyricsAnimation)) {
            for (int i = 0; i < e.wordTimes.length; i++) {
               e.wordTimes[i] = startMs;
            }
         }

         e.endMs = endMs;
         e.realTime = false;
         push(e);
      }
   }

   public static synchronized void showTitle(String title, String artist) {
      SkyLyrics.Entry e = new SkyLyrics.Entry();
      String label = "♪  " + Component.translatable("valence.lyrics.nowplaying").getString().toUpperCase(Locale.ROOT);
      boolean hasArtist = artist != null && !artist.isBlank();
      e.words = hasArtist ? new String[]{label, title, artist} : new String[]{label, title};
      e.wordTimes = hasArtist ? new long[]{0L, 160L, 520L} : new long[]{0L, 160L};
      e.endMs = 3600L;
      e.realTime = true;
      e.title = true;
      push(e);
   }

   public static synchronized void showDemo(String text) {
      if (!singleWord()) {
         SkyLyrics.Entry e = new SkyLyrics.Entry();
         e.words = transform(text.trim().split("\\s+"));
         e.wordTimes = new long[e.words.length];
         boolean line = "line".equals(ValenceConfig.get().lyricsAnimation);

         for (int i = 0; i < e.words.length; i++) {
            e.wordTimes[i] = line ? 0L : i * 260L;
         }

         e.endMs = e.words.length * 260L + 1500L;
         e.realTime = true;
         push(e);
      } else {
         String[] words = transform(text.trim().split("\\s+"));

         for (int i = 0; i < words.length; i++) {
            addBurst(words[i], i * 380L, true);
         }
      }
   }

   private static String[] transform(String[] words) {
      if (!ValenceConfig.get().lyricsUppercase) {
         return (String[])words.clone();
      }

      String[] out = new String[words.length];

      for (int i = 0; i < words.length; i++) {
         out[i] = words[i].toUpperCase(Locale.ROOT);
      }

      return out;
   }

   private static void push(SkyLyrics.Entry e) {
      place(e);
      if (e.mode != SkyLyrics.Mode.NONE) {
         Font font = Minecraft.getInstance().font;
         build(e, font);
         long now = System.nanoTime();

         for (SkyLyrics.Entry other : ENTRIES) {
            if (other.leavingNanos == 0L) {
               other.leavingNanos = now;
            }

            if (other.mode == e.mode) {
               build(other, font);
               other.targetLift = Math.max(other.targetLift, other.lift) + (e.height * e.fit + other.height * other.fit) * 0.5F + 5.0F;
            }
         }

         ENTRIES.add(e);

         while (ENTRIES.size() > 3) {
            ENTRIES.remove(0);
         }
      }
   }

   private static void place(SkyLyrics.Entry e) {
      ValenceConfig cfg = ValenceConfig.get();
      Minecraft mc = Minecraft.getInstance();
      e.createdNanos = System.nanoTime();
      e.mode = currentMode();
      float tilt = e.title ? 0.15F : cfg.lyricsTilt;
      float side = RANDOM.nextBoolean() ? 1.0F : -1.0F;
      e.yawTilt = (float)Math.toRadians(side * (10.0F + RANDOM.nextFloat() * 14.0F) * tilt);
      e.pitchTilt = (float)Math.toRadians((RANDOM.nextFloat() - 0.5F) * 12.0F * tilt);
      e.roll = (float)Math.toRadians((RANDOM.nextFloat() - 0.5F) * 8.0F * tilt);
      if (e.mode == SkyLyrics.Mode.WORLD) {
         e.yawTilt = 0.0F;
         e.pitchTilt = 0.0F;
         e.roll = 0.0F;
      }

      e.az = cameraAz();
      e.el = cfg.lyricsElevation;
      if (e.mode == SkyLyrics.Mode.WORLD && mc.level != null && mc.gameRenderer != null) {
         Camera cam = mc.gameRenderer.mainCamera();
         Vec3 eye = cam.position();
         double yawRad = Math.toRadians(cam.yRot());
         double dx = -Math.sin(yawRad);
         double dz = Math.cos(yawRad);
         double dist = 6.0;
         double height = 1.2;

         for (int tries = 0; tries < 5; tries++) {
            BlockPos pos = BlockPos.containing(eye.x + dx * dist, eye.y + height, eye.z + dz * dist);
            if (mc.level.getBlockState(pos).isAir() && mc.level.getBlockState(pos.above()).isAir()) {
               break;
            }

            dist *= 0.72;
            height += 0.4;
         }

         e.wx = eye.x + dx * dist;
         e.wy = eye.y + height;
         e.wz = eye.z + dz * dist;
         e.faceYaw = (float)Math.atan2(eye.x - e.wx, eye.z - e.wz);
      }
   }

   private static Minecraft mc() {
      return Minecraft.getInstance();
   }

   private static float cameraAz() {
      Minecraft mc = Minecraft.getInstance();
      return mc.gameRenderer == null ? 0.0F : mc.gameRenderer.mainCamera().yRot() + 180.0F;
   }

   private static long now(SkyLyrics.Entry e) {
      return e.realTime ? (System.nanoTime() - e.createdNanos) / 1000000L : LyricsBridge.positionMs();
   }

   private static float entryAlpha(SkyLyrics.Entry e, long t, long nowNanos) {
      long hold = (long)(ValenceConfig.get().lyricsHoldSeconds * 1000.0F);
      long fadeStart = e.endMs + hold;
      float a = t <= fadeStart ? 1.0F : Math.max(0.0F, 1.0F - (float)(t - fadeStart) / 600.0F);
      if (e.leavingNanos != 0L) {
         float k = (float)(nowNanos - e.leavingNanos) / 1000000.0F / 450.0F;
         a *= Math.max(0.0F, 1.0F - k * k);
      }

      return a;
   }

   private static boolean expired(SkyLyrics.Entry e) {
      long nowNanos = System.nanoTime();
      if (e.leavingNanos != 0L && (nowNanos - e.leavingNanos) / 1000000L > 450L) {
         return true;
      }

      long t = now(e);
      long hold = (long)(ValenceConfig.get().lyricsHoldSeconds * 1000.0F);
      return t > e.endMs + hold + 600L || t < -5000L;
   }

   private static Style baseStyle() {
      return Style.EMPTY.withFont(LyricFonts.byId(ValenceConfig.get().lyricsFont).description());
   }

   private static void build(SkyLyrics.Entry e, Font font) {
      ValenceConfig cfg = ValenceConfig.get();
      boolean rainbow = "rainbow".equals(cfg.lyricsColorMode);
      long stamp = rainbow ? System.nanoTime() / 100000000L : 0L;
      if (e.revision != ValenceConfig.revision() || e.layout == null || e.rainbowStamp != stamp) {
         boolean relayout = e.revision != ValenceConfig.revision() || e.layout == null;
         e.revision = ValenceConfig.revision();
         e.rainbowStamp = stamp;
         Style base = baseStyle();
         int n = e.words.length;
         if (relayout && e.title) {
            layoutTitle(e, font, base);
         } else if (relayout) {
            e.layout = new SkyLyrics.Word[n];
            float space = font.width(Component.literal(" ").withStyle(base)) + 1.5F;
            float[] widths = new float[n];
            float total = 0.0F;

            for (int i = 0; i < n; i++) {
               widths[i] = font.width(Component.literal(e.words[i]).withStyle(base));
               total += widths[i] + (i > 0 ? space : 0.0F);
            }

            int split = n;
            if (total > 150.0F && n > 1) {
               float best = Float.MAX_VALUE;
               float left = 0.0F;

               for (int k = 1; k < n; k++) {
                  left += widths[k - 1] + (k > 1 ? space : 0.0F);
                  float right = total - left - space;
                  float worst = Math.max(left, right);
                  if (worst < best) {
                     best = worst;
                     split = k;
                  }
               }
            }

            int rows = split < n ? 2 : 1;
            float widest = 0.0F;

            for (int r = 0; r < rows; r++) {
               int from = r == 0 ? 0 : split;
               int to = r == 0 ? split : n;
               float rw = 0.0F;

               for (int i = from; i < to; i++) {
                  rw += widths[i] + (i > from ? space : 0.0F);
               }

               widest = Math.max(widest, rw);
               float x = -rw / 2.0F;
               float cy = rows == 1 ? 0.0F : (r == 0 ? -7.15F : 7.15F);
               float half = Math.max(12.0F, rw / 2.0F);

               for (int i = from; i < to; i++) {
                  SkyLyrics.Word w = new SkyLyrics.Word();
                  w.text = e.words[i];
                  w.time = e.wordTimes[i];
                  w.index = i;
                  w.width = widths[i];
                  w.x = x + widths[i] / 2.0F;
                  float kx = w.x / half;
                  w.y = cy - 3.5F * (1.0F - kx * kx);
                  w.rot = e.title ? 0.0F : (float)Math.atan(7.0F * kx / half);
                  e.layout[i] = w;
                  x += widths[i] + space;
               }
            }

            e.height = rows * 13.0F * 1.1F;
            e.fit = Math.min(1.0F, 187.5F / Math.max(1.0F, widest)) * (e.title ? 0.75F : 1.0F);
            e.glowColor = lerpColor(cfg.lyricsColorA, -1, 0.35F);
         }

         for (int i = 0; i < n; i++) {
            SkyLyrics.Word w = e.layout[i];
            float t0 = n <= 1 ? 0.0F : (float)i / n;
            float t1 = n <= 1 ? 1.0F : (float)(i + 1) / n;
            if (e.title && w.index != 1) {
               int c = w.index == 0 ? lerpColor(cfg.lyricsColorA, -1, 0.25F) : -856321;
               w.main = Component.literal(w.text).withStyle(base.withColor(c & 16777215)).getVisualOrderText();
            } else {
               w.main = colored(w.text, base, e.title ? 0.0F : t0, e.title ? 1.0F : t1, (float)stamp * 0.1F);
            }

            w.outline = cfg.lyricsOutline ? Component.literal(w.text).withStyle(base.withColor(cfg.lyricsOutlineColor & 16777215)).getVisualOrderText() : null;
         }
      }
   }

   private static void layoutTitle(SkyLyrics.Entry e, Font font, Style base) {
      ValenceConfig cfg = ValenceConfig.get();
      int n = e.words.length;
      e.layout = new SkyLyrics.Word[n];
      float[] scales = new float[]{0.55F, 1.35F, 0.8F};
      float[] ys = new float[]{-17.550001F, 0.0F, 16.25F};
      float widest = 0.0F;

      for (int i = 0; i < n; i++) {
         SkyLyrics.Word w = new SkyLyrics.Word();
         w.text = e.words[i];
         w.time = e.wordTimes[i];
         w.index = i;
         w.width = font.width(Component.literal(w.text).withStyle(base));
         w.scale = scales[i];
         if (i == 1 && w.width * w.scale > 225.0F) {
            w.scale = 225.0F / w.width;
         }

         w.x = 0.0F;
         w.y = ys[i];
         w.rot = 0.0F;
         widest = Math.max(widest, w.width * w.scale);
         e.layout[i] = w;
      }

      e.height = 44.2F;
      e.fit = 1.0F;
      e.glowColor = lerpColor(cfg.lyricsColorA, -1, 0.35F);
   }

   private static FormattedCharSequence colored(String s, Style base, float t0, float t1, float time) {
      ValenceConfig cfg = ValenceConfig.get();
      if (!"solid".equals(cfg.lyricsColorMode) && (cfg.lyricsColorA != cfg.lyricsColorB || "rainbow".equals(cfg.lyricsColorMode))) {
         MutableComponent out = Component.empty();
         int n = Math.max(1, s.length());

         for (int i = 0; i < s.length(); i++) {
            float t = t0 + (t1 - t0) * (i + 0.5F) / n;
            int rgb = "rainbow".equals(cfg.lyricsColorMode)
               ? hsv((t * 0.8F + time * 0.08F) % 1.0F, 0.45F, 1.0F)
               : lerpColor(cfg.lyricsColorA, cfg.lyricsColorB, t);
            out.append(Component.literal(String.valueOf(s.charAt(i))).withStyle(base.withColor(rgb & 16777215)));
         }

         return out.getVisualOrderText();
      } else {
         return Component.literal(s).withStyle(base.withColor(cfg.lyricsColorA & 16777215)).getVisualOrderText();
      }
   }

   public static int lerpColor(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      int r = (int)(ar + (br - ar) * t);
      int g = (int)(ag + (bg - ag) * t);
      int bl = (int)(ab + (bb - ab) * t);
      return 0xFF000000 | r << 16 | g << 8 | bl;
   }

   public static int hsv(float h, float s, float v) {
      return 0xFF000000 | Mth.hsvToRgb(h, s, v) & 16777215;
   }

   private static float easeOutBack(float x) {
      float c1 = 1.4F;
      float c3 = c1 + 1.0F;
      float k = x - 1.0F;
      return 1.0F + c3 * k * k * k + c1 * k * k;
   }

   private static int alphaColor(float a) {
      int ia = Math.max(0, Math.min(255, Math.round(a * 255.0F)));
      return ia << 24 | 16777215;
   }

   private static int tint(int rgb, float a) {
      int ia = Math.max(0, Math.min(255, Math.round(a * 255.0F)));
      return ia << 24 | rgb & 16777215;
   }

   private static void addBurst(String text, long time, boolean realTime) {
      if (text != null && !text.isBlank() && currentMode() != SkyLyrics.Mode.NONE) {
         SkyLyrics.Burst b = new SkyLyrics.Burst();
         b.text = text;
         b.time = time;
         b.realTime = realTime;
         b.createdNanos = System.nanoTime();
         BURSTS.add(b);

         while (BURSTS.size() > 40) {
            BURSTS.remove(0);
         }
      }
   }

   private static long burstNow(SkyLyrics.Burst b) {
      return b.realTime ? (System.nanoTime() - b.createdNanos) / 1000000L : LyricsBridge.positionMs();
   }

   private static long burstLife() {
      return 900L + (long)(ValenceConfig.get().lyricsHoldSeconds * 250.0F);
   }

   private static int newerShown(SkyLyrics.Burst b, long t) {
      int n = 0;

      for (SkyLyrics.Burst o : BURSTS) {
         if (o != b && o.realTime == b.realTime && o.time > b.time && t + 40L >= o.time) {
            n++;
         }
      }

      return n;
   }

   private static void placeBurst(SkyLyrics.Burst b) {
      ValenceConfig cfg = ValenceConfig.get();
      Minecraft mc = Minecraft.getInstance();
      b.placed = true;
      b.mode = currentMode();
      float bestX = 0.0F;
      float bestY = 0.0F;
      float bestD = -1.0F;

      for (int i = 0; i < 10; i++) {
         float x = RANDOM.nextFloat() * 2.0F - 1.0F;
         float y = RANDOM.nextFloat() * 1.5F - 0.6F;
         float d = 9.0F;

         for (int k = 0; k + 1 < lastSpots.length; k += 2) {
            float dx = (x - lastSpots[k]) * 1.1F;
            float dy = y - lastSpots[k + 1];
            d = Math.min(d, dx * dx + dy * dy);
         }

         d -= Math.max(0.0F, 0.25F - (x * x + y * y)) * 2.0F;
         if (d > bestD) {
            bestD = d;
            bestX = x;
            bestY = y;
         }
      }

      float[] spots = new float[Math.min(6, lastSpots.length + 2)];
      spots[0] = bestX;
      spots[1] = bestY;
      System.arraycopy(lastSpots, 0, spots, 2, spots.length - 2);
      lastSpots = spots;
      b.ox = bestX;
      b.oy = bestY;
      int len = b.text.length();
      b.size = (len <= 2 ? 0.55F : (len == 3 ? 0.78F : 1.0F)) * (0.88F + RANDOM.nextFloat() * 0.24F);
      float tilt = 0.35F + 0.65F * cfg.lyricsTilt;
      float side = bestX >= 0.0F ? 1.0F : -1.0F;
      b.yaw = (float)Math.toRadians(-side * (14.0F + RANDOM.nextFloat() * 20.0F) * tilt);
      b.pitch = (float)Math.toRadians((RANDOM.nextFloat() - 0.5F) * 16.0F * tilt);
      b.roll = (float)Math.toRadians(((RANDOM.nextFloat() - 0.5F) * 14.0F + 6.0F) * tilt);
      if (b.mode == SkyLyrics.Mode.WORLD) {
         b.yaw = 0.0F;
         b.pitch = 0.0F;
         b.roll = 0.0F;
      }

      b.az = cameraAz() + bestX * 26.0F;
      b.el = cfg.lyricsElevation + bestY * 11.0F;
      if (b.mode == SkyLyrics.Mode.WORLD && mc.level != null && mc.gameRenderer != null) {
         Camera cam = mc.gameRenderer.mainCamera();
         Vec3 eye = cam.position();
         double yawRad = Math.toRadians(cam.yRot());
         double fx = -Math.sin(yawRad);
         double fz = Math.cos(yawRad);
         double dist = 5.5 + RANDOM.nextFloat() * 2.5;
         double lateral = bestX * 3.4;
         double up = -0.2 + (bestY + 0.6) * 1.35;

         for (int tries = 0; tries < 5; tries++) {
            double px = eye.x + fx * dist - fz * lateral;
            double pz = eye.z + fz * dist + fx * lateral;
            BlockPos pos = BlockPos.containing(px, eye.y + up, pz);
            if (mc.level.getBlockState(pos).isAir()) {
               break;
            }

            dist *= 0.75;
            up += 0.35;
         }

         b.wx = eye.x + fx * dist - fz * lateral;
         b.wy = eye.y + up;
         b.wz = eye.z + fz * dist + fx * lateral;
         b.faceYaw = (float)Math.atan2(eye.x - b.wx, eye.z - b.wz);
      }
   }

   private static void buildBurst(SkyLyrics.Burst b, Font font) {
      ValenceConfig cfg = ValenceConfig.get();
      boolean rainbow = "rainbow".equals(cfg.lyricsColorMode);
      long stamp = rainbow ? System.nanoTime() / 100000000L : 0L;
      if (b.revision != ValenceConfig.revision() || b.main == null || b.rainbowStamp != stamp) {
         b.revision = ValenceConfig.revision();
         b.rainbowStamp = stamp;
         Style st = baseStyle().withBold(true);
         b.width = font.width(Component.literal(b.text).withStyle(st));
         b.main = colored(b.text, st, 0.0F, 1.0F, (float)stamp * 0.1F);
         b.outline = cfg.lyricsOutline ? Component.literal(b.text).withStyle(st.withColor(cfg.lyricsOutlineColor & 16777215)).getVisualOrderText() : null;
         b.glowColor = lerpColor(cfg.lyricsColorA, -1, 0.35F);
      }
   }

   private static long outStart(SkyLyrics.Burst b) {
      long life = b.time + burstLife();
      long first = Long.MAX_VALUE;
      long second = Long.MAX_VALUE;
      long third = Long.MAX_VALUE;

      for (SkyLyrics.Burst o : BURSTS) {
         if (o != b && o.realTime == b.realTime && o.time > b.time) {
            long ot = o.time;
            if (ot < first) {
               third = second;
               second = first;
               first = ot;
            } else if (ot < second) {
               third = second;
               second = ot;
            } else if (ot < third) {
               third = ot;
            }
         }
      }

      return Math.min(life, third);
   }

   private static float newerSmooth(SkyLyrics.Burst b, long t) {
      float n = 0.0F;

      for (SkyLyrics.Burst o : BURSTS) {
         if (o != b && o.realTime == b.realTime && o.time > b.time && t > o.time) {
            n += Math.min(1.0F, (float)(t - o.time) / 250.0F);
         }
      }

      return Math.min(2.0F, n);
   }

   private static float smoothStep(float x) {
      x = Math.max(0.0F, Math.min(1.0F, x));
      return x * x * (3.0F - 2.0F * x);
   }

   private static float[] burstState(SkyLyrics.Burst b) {
      long t = burstNow(b);
      long age = t - b.time;
      if (age < 0L) {
         return new float[]{-1.0F, 0.0F, 0.0F};
      }

      float in = Theme.easeOutCubic(Math.min(1.0F, (float)age / 260.0F));
      long os = outStart(b);
      float out = t <= os ? 0.0F : smoothStep((float)(t - os) / 520.0F);
      float a = in * (1.0F - out);
      float settle = 1.0F + 0.16F * (1.0F - in);
      float fadeGrow = 1.0F + 0.08F * out;
      float shrink = 1.0F - 0.12F * newerSmooth(b, t);
      float drift = Math.min(1.0F, (float)age / 2500.0F) + 0.5F * out;
      return new float[]{a, settle * fadeGrow * shrink * b.size, drift};
   }

   private static boolean burstExpired(SkyLyrics.Burst b) {
      long t = burstNow(b);
      long age = t - b.time;
      return t > outStart(b) + 520L + 30L || age < -15000L;
   }

   private static void prepareBursts() {
      BURSTS.removeIf(SkyLyrics::burstExpired);

      for (SkyLyrics.Burst b : BURSTS) {
         if (!b.placed && burstNow(b) >= b.time) {
            placeBurst(b);
         }
      }
   }

   private static boolean anyBurst(SkyLyrics.Mode mode) {
      for (SkyLyrics.Burst b : BURSTS) {
         if (b.placed && b.mode == mode) {
            return true;
         }
      }

      return false;
   }

   private static void drawBurst3D(SkyLyrics.Burst b, Matrix4f m, Font font, MultiBufferSource buffers, DisplayMode mode, float depthStep) {
      ValenceConfig cfg = ValenceConfig.get();
      buildBurst(b, font);
      float[] st = burstState(b);
      float a = st[0];
      if (!(a <= 0.01F)) {
         float sc = st[1] * 2.6F;
         m.translate(0.0F, -st[2] * 3.0F, 0.0F).scale(sc, sc, 1.0F);
         int light = 15728880;
         if (cfg.lyricsGlow > 0.01F) {
            VertexConsumer vc = buffers.getBuffer(mode == DisplayMode.SEE_THROUGH ? RenderTypes.textSeeThrough(GLOW) : RenderTypes.text(GLOW));
            float ga = a * cfg.lyricsGlow * 0.42F;
            float hw = b.width / 0.53F / 2.0F + 2.0F;
            float hh = 10.5F;
            Matrix4f g = new Matrix4f(m).translate(0.0F, 0.0F, -depthStep * 2.0F);
            int col = tint(b.glowColor, Math.min(1.0F, ga));
            vc.addVertex(g, -hw, -hh, 0.0F).setColor(col).setUv(0.0F, 0.0F).setLight(light);
            vc.addVertex(g, -hw, hh, 0.0F).setColor(col).setUv(0.0F, 1.0F).setLight(light);
            vc.addVertex(g, hw, hh, 0.0F).setColor(col).setUv(1.0F, 1.0F).setLight(light);
            vc.addVertex(g, hw, -hh, 0.0F).setColor(col).setUv(1.0F, 0.0F).setLight(light);
         }

         float x = -b.width / 2.0F;
         float y = -4.5F;
         if (b.outline != null && mode != DisplayMode.NORMAL) {
            int oa = alphaColor(a * 0.95F);
            m.translate(0.0F, 0.0F, -depthStep);

            for (float[] o : OUTLINE) {
               Txt.drawInBatch(font, b.outline, x + o[0], y + o[1], oa, false, m, buffers, mode, 0, light);
            }

            m.translate(0.0F, 0.0F, depthStep);
         }

         Txt.drawInBatch(font, b.main, x, y, alphaColor(a), false, m, buffers, mode == DisplayMode.NORMAL ? DisplayMode.POLYGON_OFFSET : mode, 0, light);
      }
   }

   private static void renderBurstsSky(Font font, MultiBufferSource buffers) {
      ValenceConfig cfg = ValenceConfig.get();
      float s = (float)(90.0 * Math.tan(Math.toRadians(0.36F * cfg.lyricsScale)));

      for (SkyLyrics.Burst b : BURSTS) {
         if (b.placed && b.mode == SkyLyrics.Mode.SKY) {
            Matrix4f m = new Matrix4f()
               .rotateY((float)Math.toRadians(-b.az))
               .rotateX((float)Math.toRadians(b.el))
               .translate(0.0F, 0.0F, -90.0F)
               .rotateY(b.yaw)
               .rotateX(b.pitch)
               .rotateZ(-b.roll)
               .scale(s, -s, s);
            drawBurst3D(b, m, font, buffers, DisplayMode.SEE_THROUGH, 0.0F);
         }
      }
   }

   private static void renderBurstsWorld(Matrix4f pose, Font font, MultiBufferSource buffers, Vec3 cam) {
      float s = 0.034F * ValenceConfig.get().lyricsScale;

      for (SkyLyrics.Burst b : BURSTS) {
         if (b.placed && b.mode == SkyLyrics.Mode.WORLD) {
            Matrix4f m = new Matrix4f(pose)
               .translate((float)(b.wx - cam.x), (float)(b.wy - cam.y), (float)(b.wz - cam.z))
               .rotateY(b.faceYaw + b.yaw)
               .rotateX(b.pitch)
               .rotateZ(-b.roll)
               .scale(s, -s, s);
            drawBurst3D(b, m, font, buffers, DisplayMode.NORMAL, 0.0F);
         }
      }
   }

   private static void renderBurstsScreen(GuiGraphicsExtractor g, Font font) {
      ValenceConfig cfg = ValenceConfig.get();
      float base = 1.5F * cfg.lyricsScale;

      for (SkyLyrics.Burst b : BURSTS) {
         if (b.placed && b.mode == SkyLyrics.Mode.SCREEN) {
            buildBurst(b, font);
            float[] st = burstState(b);
            float a = st[0];
            if (!(a <= 0.03F)) {
               float sc = base * st[1] * 2.6F;
               float cosY = (float)Math.cos(b.yaw);
               float sinY = (float)Math.sin(b.yaw);
               g.pose().pushMatrix();
               g.pose().translate(g.guiWidth() * (0.5F + b.ox * 0.3F), g.guiHeight() * (0.3F - b.oy * 0.13F) - st[2] * 4.0F);
               g.pose().mul(new Matrix3x2f(cosY, sinY * 0.22F, 0.0F, 1.0F, 0.0F, 0.0F));
               g.pose().rotate(b.roll);
               g.pose().scale(sc, sc);
               int iw = Math.round(b.width);
               if (cfg.lyricsGlow > 0.01F) {
                  int hw = Math.round(b.width / 0.53F / 2.0F + 2.0F);
                  g.blit(
                     RenderPipelines.GUI_TEXTURED,
                     GLOW,
                     -hw,
                     -11,
                     0.0F,
                     0.0F,
                     hw * 2,
                     21,
                     128,
                     48,
                     128,
                     48,
                     tint(b.glowColor, Math.min(1.0F, a * cfg.lyricsGlow * 0.42F))
                  );
               }

               if (b.outline != null) {
                  int oa = alphaColor(a * 0.95F);
                  g.text(font, b.outline, -iw / 2 - 1, -4, oa, false);
                  g.text(font, b.outline, -iw / 2 + 1, -4, oa, false);
                  g.text(font, b.outline, -iw / 2, -5, oa, false);
                  g.text(font, b.outline, -iw / 2, -3, oa, false);
               }

               g.text(font, b.main, -iw / 2, -4, alphaColor(a), false);
               g.pose().popMatrix();
            }
         }
      }
   }

   private static Matrix4f wordMatrix(Matrix4f line, SkyLyrics.Entry e, SkyLyrics.Word w, float k) {
      float s = (0.62F + 0.38F * easeOutBack(k)) * e.fit * w.scale;
      float rise = (1.0F - k) * 5.0F;
      return TMP.set(line).translate(w.x * e.fit, (w.y - e.lift) * e.fit + rise, 0.0F).rotateZ(w.rot).scale(s, s, 1.0F);
   }

   private static float appear(long t, SkyLyrics.Word w) {
      return t < w.time ? -1.0F : Math.min(1.0F, (float)(t - w.time) / 300.0F);
   }

   private static void draw3D(SkyLyrics.Entry e, Matrix4f line, Font font, MultiBufferSource buffers, DisplayMode mode, long nowNanos, float depthStep) {
      ValenceConfig cfg = ValenceConfig.get();
      build(e, font);
      e.lift = e.lift + (e.targetLift - e.lift) * 0.15F;
      long t = now(e);
      float lineAlpha = entryAlpha(e, t, nowNanos);
      if (!(lineAlpha <= 0.01F)) {
         int light = 15728880;
         if (cfg.lyricsGlow > 0.01F) {
            VertexConsumer vc = buffers.getBuffer(mode == DisplayMode.SEE_THROUGH ? RenderTypes.textSeeThrough(GLOW) : RenderTypes.text(GLOW));

            for (SkyLyrics.Word w : e.layout) {
               float k = appear(t, w);
               if (!(k < 0.0F)) {
                  float a = lineAlpha * Math.min(1.0F, k * 1.6F) * cfg.lyricsGlow * (0.32F + 0.45F * (1.0F - k));
                  if (!(a < 0.01F)) {
                     Matrix4f m = wordMatrix(line, e, w, k).translate(0.0F, 0.0F, -depthStep * 2.0F);
                     float hw = w.width / 0.53F / 2.0F + 2.0F;
                     float hh = 10.5F;
                     int col = tint(e.glowColor, Math.min(1.0F, a));
                     vc.addVertex(m, -hw, -hh, 0.0F).setColor(col).setUv(0.0F, 0.0F).setLight(light);
                     vc.addVertex(m, -hw, hh, 0.0F).setColor(col).setUv(0.0F, 1.0F).setLight(light);
                     vc.addVertex(m, hw, hh, 0.0F).setColor(col).setUv(1.0F, 1.0F).setLight(light);
                     vc.addVertex(m, hw, -hh, 0.0F).setColor(col).setUv(1.0F, 0.0F).setLight(light);
                  }
               }
            }
         }

         if (e.title && e.layout.length > 2) {
            float k = Theme.easeOutCubic(Math.max(0.0F, (float)(t - 300L) / 700.0F));
            if (k > 0.0F) {
               VertexConsumer vc = buffers.getBuffer(mode == DisplayMode.SEE_THROUGH ? RenderTypes.textSeeThrough(GLOW) : RenderTypes.text(GLOW));
               SkyLyrics.Word title = e.layout[1];
               float hw = title.width * title.scale * 0.5F * k + 4.0F;
               float yy = 8.84F - e.lift;
               Matrix4f m = TMP.set(line).translate(0.0F, yy * e.fit, -depthStep * 2.0F);
               int col = tint(e.glowColor, 0.9F * lineAlpha);
               vc.addVertex(m, -hw, -2.2F, 0.0F).setColor(col).setUv(0.0F, 0.0F).setLight(light);
               vc.addVertex(m, -hw, 2.2F, 0.0F).setColor(col).setUv(0.0F, 1.0F).setLight(light);
               vc.addVertex(m, hw, 2.2F, 0.0F).setColor(col).setUv(1.0F, 1.0F).setLight(light);
               vc.addVertex(m, hw, -2.2F, 0.0F).setColor(col).setUv(1.0F, 0.0F).setLight(light);
            }
         }

         for (SkyLyrics.Word wx : e.layout) {
            float k = appear(t, wx);
            if (!(k < 0.0F)) {
               float a = lineAlpha * Math.min(1.0F, k * 1.6F);
               if (!(a < 0.03F)) {
                  Matrix4f m = wordMatrix(line, e, wx, k);
                  float x = -wx.width / 2.0F;
                  float y = -4.5F;
                  if (wx.outline != null && mode != DisplayMode.NORMAL) {
                     int oa = alphaColor(a * 0.95F);
                     m.translate(0.0F, 0.0F, -depthStep);

                     for (float[] o : OUTLINE) {
                        Txt.drawInBatch(font, wx.outline, x + o[0], y + o[1], oa, false, m, buffers, mode, 0, light);
                     }

                     m.translate(0.0F, 0.0F, depthStep);
                  }

                  Txt.drawInBatch(
                     font, wx.main, x, y, alphaColor(a), false, m, buffers, mode == DisplayMode.NORMAL ? DisplayMode.POLYGON_OFFSET : mode, 0, light
                  );
               }
            }
         }
      }
   }

   public static synchronized void renderInSky() {
      ValenceConfig cfg = ValenceConfig.get();
      if (cfg.lyricsEnabled && License.active() && (!ENTRIES.isEmpty() || !BURSTS.isEmpty())) {
         ENTRIES.removeIf(SkyLyrics::expired);
         prepareBursts();
         boolean any = anyBurst(SkyLyrics.Mode.SKY);

         for (SkyLyrics.Entry e : ENTRIES) {
            if (e.mode == SkyLyrics.Mode.SKY) {
               any = true;
               break;
            }
         }

         if (any) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            if (skyBuffers == null) {
               skyBuffers = MultiBufferSource.immediate(new ByteBufferBuilder(131072));
            }

            long nowNanos = System.nanoTime();
            float s = (float)(90.0 * Math.tan(Math.toRadians(0.36F * cfg.lyricsScale)));
            Matrix4f line = new Matrix4f();

            for (SkyLyrics.Entry ex : ENTRIES) {
               if (ex.mode == SkyLyrics.Mode.SKY) {
                  line.identity()
                     .rotateY((float)Math.toRadians(-ex.az))
                     .rotateX((float)Math.toRadians(ex.el))
                     .translate(0.0F, 0.0F, -90.0F)
                     .rotateY(ex.yawTilt)
                     .rotateX(ex.pitchTilt)
                     .rotateZ(-ex.roll)
                     .scale(s, -s, s);
                  draw3D(ex, line, font, skyBuffers, DisplayMode.SEE_THROUGH, nowNanos, 0.0F);
               }
            }

            renderBurstsSky(font, skyBuffers);
            skyBuffers.endBatch();
         }
      }
   }

   public static synchronized void renderInWorld(Matrix4f pose, MultiBufferSource buffers) {
      ValenceConfig cfg = ValenceConfig.get();
      if (cfg.lyricsEnabled && License.active() && (!ENTRIES.isEmpty() || !BURSTS.isEmpty())) {
         prepareBursts();
         boolean any = anyBurst(SkyLyrics.Mode.WORLD);

         for (SkyLyrics.Entry e : ENTRIES) {
            if (e.mode == SkyLyrics.Mode.WORLD && !expired(e)) {
               any = true;
               break;
            }
         }

         if (any) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            Vec3 cam = mc.gameRenderer.mainCamera().position();
            long nowNanos = System.nanoTime();
            float s = 0.034F * cfg.lyricsScale;
            Matrix4f line = new Matrix4f();

            for (SkyLyrics.Entry ex : ENTRIES) {
               if (ex.mode == SkyLyrics.Mode.WORLD) {
                  line.set(pose)
                     .translate((float)(ex.wx - cam.x), (float)(ex.wy - cam.y), (float)(ex.wz - cam.z))
                     .rotateY(ex.faceYaw + ex.yawTilt)
                     .rotateX(ex.pitchTilt)
                     .rotateZ(-ex.roll)
                     .scale(s, -s, s);
                  draw3D(ex, line, font, buffers, DisplayMode.NORMAL, nowNanos, 0.0F);
               }
            }

            renderBurstsWorld(pose, font, buffers, cam);
         }
      }
   }

   public static synchronized void renderScreen(GuiGraphicsExtractor g) {
      ValenceConfig cfg = ValenceConfig.get();
      if (cfg.lyricsEnabled && License.active() && (!ENTRIES.isEmpty() || !BURSTS.isEmpty())) {
         ENTRIES.removeIf(SkyLyrics::expired);
         prepareBursts();
         renderBurstsScreen(g, mc().font);
         Minecraft mc = Minecraft.getInstance();
         Font font = mc.font;
         long nowNanos = System.nanoTime();
         float cx = g.guiWidth() / 2.0F;
         float cy = g.guiHeight() * 0.27F;
         float base = 1.5F * cfg.lyricsScale;

         for (SkyLyrics.Entry e : ENTRIES) {
            if (e.mode == SkyLyrics.Mode.SCREEN) {
               build(e, font);
               e.lift = e.lift + (e.targetLift - e.lift) * 0.15F;
               long t = now(e);
               float lineAlpha = entryAlpha(e, t, nowNanos);
               if (!(lineAlpha <= 0.01F)) {
                  float cosY = (float)Math.cos(e.yawTilt);
                  float sinY = (float)Math.sin(e.yawTilt);

                  for (SkyLyrics.Word w : e.layout) {
                     float k = appear(t, w);
                     if (!(k < 0.0F)) {
                        float a = lineAlpha * Math.min(1.0F, k * 1.6F);
                        if (!(a < 0.03F)) {
                           float animScale = (0.62F + 0.38F * easeOutBack(k)) * base * e.fit * w.scale;
                           g.pose().pushMatrix();
                           g.pose().translate(cx, cy);
                           g.pose().mul(new Matrix3x2f(cosY, sinY * 0.22F, 0.0F, 1.0F, 0.0F, 0.0F));
                           g.pose().rotate(e.roll);
                           g.pose().translate(w.x * base * e.fit, (w.y - e.lift) * base * e.fit + (1.0F - k) * 6.0F);
                           g.pose().rotate(w.rot);
                           g.pose().scale(animScale, animScale);
                           int iw = Math.round(w.width);
                           if (cfg.lyricsGlow > 0.01F) {
                              float ga = a * cfg.lyricsGlow * (0.32F + 0.45F * (1.0F - k));
                              int hw = Math.round(w.width / 0.53F / 2.0F + 2.0F);
                              g.blit(
                                 RenderPipelines.GUI_TEXTURED, GLOW, -hw, -11, 0.0F, 0.0F, hw * 2, 21, 128, 48, 128, 48, tint(e.glowColor, Math.min(1.0F, ga))
                              );
                           }

                           if (w.outline != null) {
                              int oa = alphaColor(a * 0.95F);
                              g.text(font, w.outline, -iw / 2 - 1, -4, oa, false);
                              g.text(font, w.outline, -iw / 2 + 1, -4, oa, false);
                              g.text(font, w.outline, -iw / 2, -5, oa, false);
                              g.text(font, w.outline, -iw / 2, -3, oa, false);
                           }

                           g.text(font, w.main, -iw / 2, -4, alphaColor(a), false);
                           g.pose().popMatrix();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static final class Burst {
      String text;
      long time;
      boolean realTime;
      long createdNanos;
      boolean placed;
      SkyLyrics.Mode mode;
      float ox;
      float oy;
      float az;
      float el;
      double wx;
      double wy;
      double wz;
      float faceYaw;
      float yaw;
      float pitch;
      float roll;
      float size;
      float width;
      int revision = -1;
      long rainbowStamp = -1L;
      FormattedCharSequence main;
      FormattedCharSequence outline;
      int glowColor;
   }

   private static final class Entry {
      String[] words;
      long[] wordTimes;
      long endMs;
      boolean realTime;
      long createdNanos;
      long leavingNanos;
      boolean title;
      SkyLyrics.Mode mode;
      float az;
      float el;
      double wx;
      double wy;
      double wz;
      float faceYaw;
      float yawTilt;
      float pitchTilt;
      float roll;
      float lift;
      float targetLift;
      int revision = -1;
      long rainbowStamp = -1L;
      SkyLyrics.Word[] layout;
      float height;
      float fit = 1.0F;
      int glowColor;
   }

   public enum Mode {
      SKY,
      WORLD,
      SCREEN,
      NONE;
   }

   private static final class Word {
      float scale = 1.0F;
      String text;
      long time;
      int index;
      float x;
      float y;
      float rot;
      float width;
      FormattedCharSequence main;
      FormattedCharSequence outline;
   }
}
