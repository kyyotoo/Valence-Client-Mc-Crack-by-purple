package dev.valence.cord.sky;

import dev.valence.cord.config.Config;
import dev.valence.cord.hud.Gfx;
import dev.valence.cord.hud.LyricColor;
import dev.valence.cord.hud.SingleWord;
import dev.valence.cord.lyrics.LyricTiming;
import dev.valence.cord.lyrics.NowPlaying;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.cord.util.AutoPerf;
import dev.valence.render.MultiBufferSource;
import dev.valence.render.Txt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class FloatWords {
   private static final float[][] SLOTS = new float[][]{
      {-15.0F, 5.0F}, {14.0F, -3.0F}, {-3.0F, -9.0F}, {12.0F, 9.0F}, {-19.0F, -6.0F}, {4.0F, 7.0F}, {18.0F, 3.0F}, {-11.0F, 11.0F}
   };
   private static final float BLOCKS_PER_PX = 0.085F;
   private static final int ROW_MAX_PX = 150;
   private static final int ROW_H = 13;
   private static final float REVEAL_MS = 190.0F;
   private static final float FADE_MS = 420.0F;
   private static final float SINGLE_FADE_MS = 320.0F;
   private static final float VIEW_UP = 6.0F;
   private static final float MIN_EL = -10.0F;
   private static final int LEAD_MS = 45;
   private static final List<FloatWords.Phrase> PHRASES = new ArrayList<>();
   private static double smYaw = Double.NaN;
   private static double smEl;
   private static long lastFrame;
   private static final Random RNG = new Random();
   private static String lastTrack = "";
   private static int lastLine = -1;
   private static int lastSlot = -1;
   private static int prevSlot = -1;
   private static final Map<String, Component> COMP = new HashMap<>();
   private static int compFont = -1;
   private static final Matrix4f M0 = new Matrix4f();
   private static final Matrix4f TMP = new Matrix4f();
   private static final Matrix4f WM = new Matrix4f();
   private static final Matrix4f SCR = new Matrix4f();

   private FloatWords() {
   }

   public static void clear() {
      PHRASES.clear();
      lastLine = -1;
   }

   public static void renderWorld(Minecraft mc, Config c, MultiBufferSource buffers, Matrix4f base, Vec3 cam, float pt, boolean through) {
      if (mc != null && c != null && base != null && cam != null) {
         if (c.lyricsSky && c.lyricsStyle == 1 && !mc.gui.hud.isHidden() && mc.player != null && mc.level != null) {
            long now = System.currentTimeMillis();
            NowPlaying np = SpotifyClient.now();
            long progress = 0L;
            int li = -1;
            if (np != null && np.hasTrack() && np.playing && !np.lines.isEmpty() && np.lyricsState == NowPlaying.LyricsState.SYNCED) {
               String tid = String.valueOf(np.trackId);
               if (!tid.equals(lastTrack)) {
                  lastTrack = tid;
                  PHRASES.clear();
                  lastLine = -1;
               }

               progress = np.progressNow(c.lyricsOffsetMs + 45);
               li = np.lineIndex(progress);
               if (li >= 0 && progress > np.lineEnd(li) + 450L) {
                  li = -1;
               }
            }

            if (li != lastLine) {
               for (FloatWords.Phrase p : PHRASES) {
                  if (p.gone == 0L) {
                     p.gone = now;
                  }
               }

               lastLine = li;
               if (li >= 0) {
                  NowPlaying.Line line = np.lines.get(li);
                  if (line.text() != null && !line.text().isBlank()) {
                     LyricTiming t = LyricTiming.of(np.lines);
                     long[] starts = t != null && li < t.wordStart.length ? t.wordStart[li] : null;
                     spawn(mc, c, line, starts, cam, pt, now);
                     SongIntro.cut();
                  }
               }
            }

            PHRASES.removeIf(p -> p.gone != 0L && (float)(now - p.gone) > fadeMs(c));

            while (PHRASES.size() > 2) {
               PHRASES.remove(0);
            }

            draw(mc, c, buffers, base, cam, pt, now, progress, false, np);
         } else {
            clear();
         }
      }
   }

   private static float fadeMs(Config c) {
      return c.lyricsReveal == 2 ? 320.0F : 420.0F;
   }

   private static float sz(Config c) {
      return c.lyricsSize / 1.7F;
   }

   private static void spawn(Minecraft mc, Config c, NowPlaying.Line line, long[] starts, Vec3 cam, float pt, long now) {
      String[] words = line.words();
      if (words == null || words.length == 0) {
         words = line.text().trim().split("\\s+");
      }

      FloatWords.Phrase p = new FloatWords.Phrase(words, starts);
      int space = Gfx.w(" ", Gfx.LYRICS);
      List<int[]> rows = new ArrayList<>();
      int i = 0;

      while (i < words.length) {
         int from = i;
         int width = Gfx.w(words[i], Gfx.LYRICS);
         i++;

         while (i < words.length) {
            int ww = Gfx.w(words[i], Gfx.LYRICS);
            if (width + space + ww > 150) {
               break;
            }

            width += space + ww;
            i++;
         }

         rows.add(new int[]{from, i, width});
      }

      if (rows.size() >= 2) {
         int[] last = rows.get(rows.size() - 1);
         int[] before = rows.get(rows.size() - 2);
         if (last[1] - last[0] == 1 && before[1] - before[0] >= 3) {
            int moved = Gfx.w(words[before[1] - 1], Gfx.LYRICS);
            before[1]--;
            before[2] -= moved + space;
            last[0]--;
            last[2] += moved + space;
         }
      }

      int maxW = 0;

      for (int[] r : rows) {
         maxW = Math.max(maxW, r[2]);
      }

      float stagger = 0.0F;
      float totalH = rows.size() * 13;

      for (int r = 0; r < rows.size(); r++) {
         int[] row = rows.get(r);
         float x = -row[2] / 2.0F + (r - (rows.size() - 1) / 2.0F) * stagger;
         float y = -totalH / 2.0F + r * 13;

         for (int k = row[0]; k < row[1]; k++) {
            p.wx[k] = x;
            p.wy[k] = y;
            x += Gfx.w(words[k], Gfx.LYRICS) + space;
         }
      }

      float yaw = mc.gameRenderer.mainCamera().yRot();
      float el0 = Math.max(-10.0F, -mc.gameRenderer.mainCamera().xRot() + 6.0F);
      double bestScore = Double.MAX_VALUE;
      double bAz = 0.0;
      double bEl = 0.0;
      double bDist = 9.0;
      float bSize = 1.0F;
      int bestSlot = 0;

      for (int attempt = 0; attempt < 24; attempt++) {
         int tries = 0;

         int slot;
         do {
            slot = RNG.nextInt(SLOTS.length);
            tries++;
         } while ((slot == lastSlot || slot == prevSlot) && tries < 12);

         double az = Math.toRadians(yaw + SLOTS[slot][0] + (RNG.nextFloat() - 0.5F) * 6.0F);
         double el = Math.toRadians(Math.max(-60.0F, Math.min(60.0F, el0 + SLOTS[slot][1] + (RNG.nextFloat() - 0.5F) * 5.0F)));
         double dist = 6.5 + RNG.nextDouble() * 3.0;
         float size = 0.92F + RNG.nextFloat() * 0.2F;
         double kk = 0.085F * sz(c) * size;
         double score = overlapScore(az, el, dist, maxW * kk * 0.5, totalH * kk * 0.5, cam);
         if (score < bestScore) {
            bestScore = score;
            bestSlot = slot;
         }

         if (score <= 0.0) {
            break;
         }
      }

      prevSlot = lastSlot;
      lastSlot = bestSlot;
      bAz = Math.toRadians(yaw);
      bEl = Math.toRadians(Math.max(-80.0F, Math.min(80.0F, el0)));
      bDist = 7.5;
      bSize = 1.0F;
      p.size = bSize;
      double kk = 0.085F * sz(c) * bSize;
      p.halfW = (float)(maxW * kk * 0.5);
      p.halfH = (float)(totalH * kk * 0.5);
      p.offAz = Math.toDegrees(bAz) - yaw;
      p.offEl = Math.toDegrees(bEl) - el0;
      p.dist = bDist;
      boolean single = c.lyricsReveal == 2;
      p.yawTilt = 0.0;
      p.roll = 0.0;
      p.seed = line.text().hashCode();
      orient(p, bAz, bEl, cam);
      p.born = now;
      PHRASES.add(p);
   }

   private static void orient(FloatWords.Phrase p, double az, double el, Vec3 cam) {
      double dx = -Math.sin(az) * Math.cos(el);
      double dy = Math.sin(el);
      double dz = Math.cos(az) * Math.cos(el);
      p.x = cam.x + dx * p.dist;
      p.y = cam.y + dy * p.dist;
      p.z = cam.z + dz * p.dist;
      double r0x = -Math.cos(az);
      double r0z = -Math.sin(az);
      double u0x = -r0z * dy;
      double u0y = r0z * dx - r0x * dz;
      double u0z = r0x * dy;
      double ct = Math.cos(p.yawTilt);
      double st = Math.sin(p.yawTilt);
      double rX = r0x * ct + dx * st;
      double rY = dy * st;
      double rZ = r0z * ct + dz * st;
      double cr = Math.cos(p.roll);
      double sr = Math.sin(p.roll);
      p.rx = rX * cr + u0x * sr;
      p.ry = rY * cr + u0y * sr;
      p.rz = rZ * cr + u0z * sr;
      p.ux = u0x * cr - rX * sr;
      p.uy = u0y * cr - rY * sr;
      p.uz = u0z * cr - rZ * sr;
   }

   private static double overlapScore(double az, double el, double dist, double hw, double hh, Vec3 cam) {
      double score = 0.0;
      double angW = hw / dist;
      double angH = hh / dist;

      for (FloatWords.Phrase o : PHRASES) {
         double ex = o.x - cam.x;
         double ey = o.y - cam.y;
         double ez = o.z - cam.z;
         double d = Math.sqrt(ex * ex + ey * ey + ez * ez);
         if (!(d < 0.5)) {
            double oAz = Math.atan2(-ex, ez);
            double oEl = Math.asin(Math.max(-1.0, Math.min(1.0, ey / d)));
            double dAz = Math.abs(((az - oAz) % (Math.PI * 2) + (Math.PI * 3)) % (Math.PI * 2) - Math.PI) * Math.cos(el);
            double dEl = Math.abs(el - oEl);
            double needW = angW + o.halfW / d + 0.04;
            double needH = angH + o.halfH / d + 0.035;
            if (dAz < needW && dEl < needH) {
               score += 1.0 - dAz / needW + (1.0 - dEl / needH) + 0.5;
            }
         }
      }

      return score;
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   private static float easeOut(float t) {
      float u = 1.0F - clamp01(t);
      return 1.0F - u * u * u;
   }

   private static Component comp(String s, int fontIdx) {
      if (fontIdx != compFont || COMP.size() > 600) {
         COMP.clear();
         compFont = fontIdx;
      }

      return COMP.computeIfAbsent(s, k -> Gfx.comp(k, Gfx.LYRICS));
   }

   private static void draw(
      Minecraft mc, Config c, MultiBufferSource buffers, Matrix4f base, Vec3 cam, float pt, long now, long progress, boolean through, NowPlaying np
   ) {
      if (!PHRASES.isEmpty()) {
         Font font = mc.font;
         int fontIdx = c.lyricsFont;
         int baseCol = c.lyricsColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsColor, WorldSky.LYRIC_COLORS.length)] : 16777215;
         int glowBase = c.lyricsGlowColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsGlowColor, WorldSky.LYRIC_COLORS.length)] : baseCol;
         boolean glow = c.lyricsGlow && AutoPerf.allow(3);
         int q = AutoPerf.quality();
         int glowN = q <= 0 ? 0 : (q == 1 ? 2 : 4);
         DisplayMode mode = through ? DisplayMode.SEE_THROUGH : DisplayMode.NORMAL;
         double yr = Math.toRadians(mc.gameRenderer.mainCamera().yRot());
         double pr = Math.toRadians(mc.gameRenderer.mainCamera().xRot());
         double vfx = -Math.sin(yr) * Math.cos(pr);
         double vfy = -Math.sin(pr);
         double vfz = Math.cos(yr) * Math.cos(pr);
         float beat = c.fxBeatPulse && np != null && np.hasTrack() ? np.beatPulse(np.progressNow(c.lyricsOffsetMs)) : 0.0F;
         int shadowCol = Gfx.lerpColor(baseCol, 328714, 0.85F);
         double viewYaw = mc.gameRenderer.mainCamera().yRot();
         double viewEl = Math.max(-10.0F, -mc.gameRenderer.mainCamera().xRot() + 6.0F);
         float fdt = lastFrame == 0L ? 0.016F : Math.min(0.1F, (float)(now - lastFrame) / 1000.0F);
         lastFrame = now;
         if (Double.isNaN(smYaw)) {
            smYaw = viewYaw;
            smEl = viewEl;
         }

         double kf = 1.0 - Math.exp(-fdt * 7.0);
         double dYaw = ((viewYaw - smYaw) % 360.0 + 540.0) % 360.0 - 180.0;
         smYaw += dYaw * kf;
         smEl = smEl + (viewEl - smEl) * kf;
         boolean follow = c.lyricsFollow;

         for (FloatWords.Phrase p : PHRASES) {
            if (follow) {
               double el = Math.max(-10.0, Math.min(85.0, smEl + p.offEl));
               orient(p, Math.toRadians(smYaw + p.offAz), Math.toRadians(el), cam);
            }
         }

         for (FloatWords.Phrase p : PHRASES) {
            boolean active = p.gone == 0L;
            float fade = active ? 1.0F : 1.0F - easeOut((float)(now - p.gone) / fadeMs(c));
            float phraseA = fade * c.lyricsOpacity;
            if (!(phraseA < 0.02F)) {
               double drift = active ? 0.0 : easeOut((float)(now - p.gone) / fadeMs(c)) * (c.lyricsReveal == 2 ? 0.25 : 0.6);
               double rx = p.x - cam.x;
               double ry = p.y + drift - cam.y;
               double rz = p.z - cam.z;
               if (!(rx * vfx + ry * vfy + rz * vfz < 0.3)) {
                  double Rx = p.rx;
                  double Ry = p.ry;
                  double Rz = p.rz;
                  double Ux = p.ux;
                  double Uy = p.uy;
                  double Uz = p.uz;
                  double Nx = Ry * Uz - Rz * Uy;
                  double Ny = Rz * Ux - Rx * Uz;
                  double Nz = Rx * Uy - Ry * Ux;
                  if (rx * Nx + ry * Ny + rz * Nz > 0.0) {
                     Rx = -Rx;
                     Ry = -Ry;
                     Rz = -Rz;
                     Nx = -Nx;
                     Ny = -Ny;
                     Nz = -Nz;
                  }

                  float kk = 0.085F * sz(c) * p.size;
                  Matrix4f m0 = M0.set(base)
                     .mul(
                        TMP.set(
                           (float)Rx * kk,
                           (float)Ry * kk,
                           (float)Rz * kk,
                           0.0F,
                           (float)(-Ux) * kk,
                           (float)(-Uy) * kk,
                           (float)(-Uz) * kk,
                           0.0F,
                           (float)Nx * kk,
                           (float)Ny * kk,
                           (float)Nz * kk,
                           0.0F,
                           (float)rx,
                           (float)ry,
                           (float)rz,
                           1.0F
                        )
                     );
                  int current = -1;

                  for (int i = 0; i < p.words.length; i++) {
                     boolean sung = !active || c.lyricsReveal == 1 || p.sung(i, progress);
                     if (sung && p.shownAt[i] == 0L) {
                        p.shownAt[i] = now;
                     }

                     if (p.shownAt[i] != 0L && active) {
                        current = i;
                     }
                  }

                  boolean single = c.lyricsReveal == 2;
                  if (single) {
                     drawSingle(font, buffers, m0, p, c, now, active, phraseA, fontIdx, baseCol, glowBase, glow, glowN, shadowCol, beat, mode);
                  } else {
                     for (int i = 0; i < p.words.length; i++) {
                        if (p.shownAt[i] != 0L) {
                           float e = easeOut((float)(now - p.shownAt[i]) / 190.0F);
                           float a = e * phraseA;
                           if (!(a < 0.03F)) {
                              String word = p.words[i];
                              int ww = Gfx.w(word, Gfx.LYRICS);
                              Component shown = comp(word, fontIdx);
                              Matrix4f m = WM.set(m0);
                              float cx = p.wx[i] + ww / 2.0F;
                              float scale = 1.0F + 0.12F * (1.0F - e);
                              if (i == current && beat > 0.01F) {
                                 scale *= 1.0F + 0.05F * beat;
                              }

                              m.translate(cx, p.wy[i] + 4.5F + (1.0F - e) * 3.0F, 0.0F).scale(scale, scale, 1.0F).translate(-ww / 2.0F, -4.5F, 0.0F);
                              boolean cur = i == current;
                              int wc = LyricColor.word(c, baseCol, i, p.words.length);
                              int col = cur ? Gfx.lerpColor(wc, 16777215, 0.25F) : wc;
                              int gcol = c.lyricsGradient ? wc : glowBase;
                              if (glow && glowN > 0) {
                                 float ga = a * 0.14F * c.lyricsGlowStrength * (cur ? 1.3F : 1.0F) * 4.0F / glowN;

                                 for (int g = 0; g < glowN; g++) {
                                    double ang = g * Math.PI * 2.0 / glowN + (Math.PI / 8);
                                    put(font, buffers, m, shown, (float)Math.cos(ang) * 1.0F, (float)Math.sin(ang) * 1.0F, 0.1F, gcol, ga, mode);
                                 }
                              } else {
                                 put(font, buffers, m, shown, 0.55F, 0.75F, 0.05F, shadowCol, a * 0.5F, mode);
                              }

                              put(font, buffers, m, shown, 0.0F, 0.0F, 0.3F, col, a, mode);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void drawSingle(
      Font font,
      MultiBufferSource buffers,
      Matrix4f m0,
      FloatWords.Phrase p,
      Config c,
      long now,
      boolean active,
      float phraseA,
      int fontIdx,
      int baseCol,
      int glowBase,
      boolean glow,
      int glowN,
      int shadowCol,
      float beat,
      DisplayMode mode
   ) {
      int n = p.words.length;
      int last = -1;

      for (int i = 0; i < n; i++) {
         if (p.shownAt[i] != 0L) {
            last = i;
         }
      }

      for (int i = 0; i < n; i++) {
         long at = p.shownAt[i];
         if (at != 0L && (active || i == last)) {
            long next = i + 1 < n ? p.shownAt[i + 1] : 0L;
            float outK = active ? SingleWord.fadeOut(next, now) : 1.0F;
            float a = phraseA * SingleWord.fadeIn(at, now) * outK;
            if (!(a < 0.03F)) {
               float out = 1.0F - outK + (active ? 0.0F : 1.0F - phraseA);
               String word = p.words[i];
               int ww = Gfx.w(word, Gfx.LYRICS);
               Component shown = comp(word, fontIdx);
               float pop = SingleWord.pop(at, now);
               float scale = 1.55F * (0.7F + 0.3F * pop) * (1.0F + 0.1F * out);
               boolean cur = active && i == last;
               if (cur && beat > 0.01F) {
                  scale *= 1.0F + 0.05F * beat;
               }

               float px = SingleWord.x(p.seed, i) * 46.0F;
               float py = SingleWord.y(p.seed, i) * 13.0F + (1.0F - SingleWord.ease(at, now)) * 5.0F - out * 7.0F;
               Matrix4f m = WM.set(m0);
               m.translate(px, py, 0.0F)
                  .rotateZ((float)Math.toRadians(SingleWord.tilt(p.seed, i)))
                  .scale(scale, scale, 1.0F)
                  .translate(-ww / 2.0F, -4.5F, 0.0F);
               int wc = LyricColor.word(c, baseCol, i, p.words.length);
               int col = cur ? Gfx.lerpColor(wc, 16777215, 0.25F) : wc;
               int gcol = c.lyricsGradient ? wc : glowBase;
               if (glow && glowN > 0) {
                  float ga = a * 0.14F * c.lyricsGlowStrength * 1.2F * 4.0F / glowN;

                  for (int g = 0; g < glowN; g++) {
                     double ang = g * Math.PI * 2.0 / glowN + (Math.PI / 8);
                     put(font, buffers, m, shown, (float)Math.cos(ang), (float)Math.sin(ang), 0.1F, gcol, ga, mode);
                  }
               } else {
                  put(font, buffers, m, shown, 0.55F, 0.75F, 0.05F, shadowCol, a * 0.5F, mode);
               }

               put(font, buffers, m, shown, 0.0F, 0.0F, 0.3F, col, a, mode);
            }
         }
      }
   }

   private static void put(Font font, MultiBufferSource buffers, Matrix4f m, Component text, float x, float y, float z, int rgb, float a, DisplayMode mode) {
      if (!(a <= 0.03F)) {
         Matrix4f mm = SCR.set(m).translate(x, y, z);
         Txt.drawInBatch(font, text, 0.0F, 0.0F, Gfx.argbRaw(rgb, a), false, mm, buffers, mode, 0, 15728880);
      }
   }

   private static final class Phrase {
      final String[] words;
      final long[] starts;
      final long[] shownAt;
      final float[] wx;
      final float[] wy;
      double x;
      double y;
      double z;
      double rx;
      double ry;
      double rz;
      double ux;
      double uy;
      double uz;
      float size = 1.0F;
      double offAz;
      double offEl;
      double dist = 8.0;
      double yawTilt;
      double roll;
      float halfW;
      float halfH;
      long born;
      long gone = 0L;
      int seed;

      Phrase(String[] words, long[] starts) {
         this.words = words;
         this.starts = starts != null && starts.length == words.length ? starts : null;
         this.shownAt = new long[words.length];
         this.wx = new float[words.length];
         this.wy = new float[words.length];
      }

      boolean sung(int i, long progress) {
         return this.starts == null ? true : progress >= this.starts[i];
      }
   }
}
