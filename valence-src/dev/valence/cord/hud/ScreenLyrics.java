package dev.valence.cord.hud;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.lyrics.LyricTiming;
import dev.valence.cord.lyrics.NowPlaying;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.cord.sky.WorldSky;
import dev.valence.gui.ValenceScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ScreenLyrics {
   private static final long LEAD = 45L;
   private static String curKey = "";
   private static long curSince;
   private static NowPlaying.Line prevLine;
   private static long prevSince;
   private static long[] shownAt = new long[0];
   private static NowPlaying.Line currentLine;

   private ScreenLyrics() {
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      Config c = NeonLyrics.cfg();
      if (c.lyricsSky && c.lyricsStyle == 2 && !mc.gui.hud.isHidden() && mc.player != null && !(mc.gui.screen() instanceof ValenceScreen)) {
         long now = System.currentTimeMillis();
         NowPlaying np = SpotifyClient.now();
         NowPlaying.Line line = null;
         int li = -1;
         long prog = 0L;
         if (np.hasTrack() && np.playing && np.lyricsState == NowPlaying.LyricsState.SYNCED && !np.lines.isEmpty()) {
            prog = np.progressNow(c.lyricsOffsetMs + 45);
            li = np.lineIndex(prog);
            if (li >= 0 && prog <= np.lineEnd(li) + 450L && !np.lines.get(li).text().isBlank()) {
               line = np.lines.get(li);
            }
         }

         String key = line == null ? "" : np.trackId + "#" + li;
         if (!key.equals(curKey)) {
            if (!curKey.isEmpty()) {
               prevLine = currentLine;
               prevSince = now;
            }

            curKey = key;
            curSince = now;
            currentLine = line;
            shownAt = new long[line == null ? 0 : line.words().length];
         }

         float old = Gfx.alpha;
         int col = c.lyricsColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsColor, WorldSky.LYRIC_COLORS.length)] : 16777215;
         float scale = Math.max(0.6F, c.lyricsSize) / 1.6F * 1.5F;
         int baseY = Math.round(g.guiHeight() * 0.7F);
         if (prevLine != null) {
            float k = Math.min(1.0F, (float)(now - prevSince) / 320.0F);
            if (k < 1.0F) {
               drawLine(g, mc.font, prevLine, null, 0L, now, col, scale, baseY - Math.round(k * 10.0F), (1.0F - k) * (1.0F - k) * c.lyricsOpacity, c, false);
            } else {
               prevLine = null;
            }
         }

         if (currentLine != null) {
            long[] starts = null;
            LyricTiming t = LyricTiming.of(np.lines);
            if (t != null && li >= 0 && li < t.wordStart.length && t.wordStart[li].length == currentLine.words().length) {
               starts = t.wordStart[li];
            }

            drawLine(g, mc.font, currentLine, starts, prog, now, col, scale, baseY, c.lyricsOpacity, c, true);
         }

         Gfx.alpha = old;
      }
   }

   private static void drawLine(
      GuiGraphicsExtractor g,
      Font font,
      NowPlaying.Line line,
      long[] starts,
      long prog,
      long now,
      int col,
      float scale,
      int baseY,
      float alpha,
      Config c,
      boolean live
   ) {
      if (!(alpha <= 0.02F)) {
         String[] words = line.words();
         int space = Gfx.w(" ", Gfx.LYRICS);
         int maxW = Math.round(g.guiWidth() * 0.7F / scale);
         List<int[]> rows = new ArrayList<>();
         int i = 0;

         while (i < words.length) {
            int from = i;
            int w = Gfx.w(words[i], Gfx.LYRICS);
            i++;

            while (i < words.length && w + space + Gfx.w(words[i], Gfx.LYRICS) <= maxW) {
               w += space + Gfx.w(words[i], Gfx.LYRICS);
               i++;
            }

            rows.add(new int[]{from, i, w});
         }

         if (c.lyricsReveal != 2 || starts == null && live) {
            int rowH = 12;
            g.pose().pushMatrix();
            g.pose().translate(g.guiWidth() / 2.0F, baseY - (rows.size() - 1) * rowH * scale);
            g.pose().scale(scale, scale);
            int shadow = Gfx.lerpColor(col, 328714, 0.85F);

            for (int r = 0; r < rows.size(); r++) {
               int[] row = rows.get(r);
               float x = -row[2] / 2.0F;
               float y = r * rowH;

               for (int k = row[0]; k < row[1]; k++) {
                  String word = words[k];
                  int ww = Gfx.w(word, Gfx.LYRICS);
                  float e;
                  if (!live) {
                     e = 1.0F;
                  } else {
                     boolean sung = c.lyricsReveal == 1 || starts == null || prog >= starts[k];
                     if (sung && shownAt.length == words.length && shownAt[k] == 0L) {
                        shownAt[k] = now;
                     }

                     long at = shownAt.length == words.length ? shownAt[k] : 0L;
                     if (at == 0L) {
                        x += ww + space;
                        continue;
                     }

                     float raw = Math.min(1.0F, (float)(now - at) / 200.0F);
                     e = 1.0F - (1.0F - raw) * (1.0F - raw) * (1.0F - raw);
                  }

                  float a = alpha * e;
                  Component comp = Gfx.comp(word, Gfx.LYRICS);
                  int wcol = LyricColor.word(c, col, k, words.length);
                  g.pose().pushMatrix();
                  switch (c.lyricsAnim) {
                     case 1:
                        float ps = 1.0F + 0.35F * (1.0F - e);
                        g.pose().translate(x + ww / 2.0F, y + 4.5F);
                        g.pose().scale(ps, ps);
                        g.pose().translate(-ww / 2.0F, -4.5F);
                        break;
                     case 2:
                        g.pose().translate(x - (1.0F - e) * 8.0F, y);
                        break;
                     default:
                        g.pose().translate(x, y + (1.0F - e) * 3.0F);
                  }

                  if (c.lyricsGlow) {
                     for (int q = 0; q < 4; q++) {
                        double ang = q * Math.PI / 2.0 + (Math.PI / 4);
                        g.pose().pushMatrix();
                        g.pose().translate((float)Math.cos(ang) * 0.8F, (float)Math.sin(ang) * 0.8F);
                        g.text(font, comp, 0, 0, Gfx.argbRaw(wcol, a * 0.18F), false);
                        g.pose().popMatrix();
                     }
                  } else {
                     g.pose().pushMatrix();
                     g.pose().translate(0.6F, 0.8F);
                     g.text(font, comp, 0, 0, Gfx.argbRaw(shadow, a * 0.55F), false);
                     g.pose().popMatrix();
                  }

                  g.text(font, comp, 0, 0, Gfx.argbRaw(wcol, a), false);
                  g.pose().popMatrix();
                  x += ww + space;
               }
            }

            g.pose().popMatrix();
         } else {
            drawSingle(g, font, line, starts, prog, now, col, scale * 1.35F, baseY, alpha, c, live);
         }
      }
   }

   private static void drawSingle(
      GuiGraphicsExtractor g,
      Font font,
      NowPlaying.Line line,
      long[] starts,
      long prog,
      long now,
      int col,
      float scale,
      int baseY,
      float alpha,
      Config c,
      boolean live
   ) {
      String[] words = line.words();
      int n = words.length;
      if (n != 0) {
         int seed = line.text().hashCode();
         float spreadX = Math.min(90.0F, g.guiWidth() * 0.12F);
         float spreadY = 16.0F;
         int shadow = Gfx.lerpColor(col, 328714, 0.85F);

         for (int k = 0; k < n; k++) {
            long at;
            long next;
            float a;
            if (live) {
               if (prog >= starts[k] && shownAt.length == n && shownAt[k] == 0L) {
                  shownAt[k] = now;
               }

               at = shownAt.length == n ? shownAt[k] : 0L;
               if (at == 0L) {
                  continue;
               }

               next = k + 1 < n && shownAt.length == n ? shownAt[k + 1] : 0L;
               a = alpha * SingleWord.fadeIn(at, now) * SingleWord.fadeOut(next, now);
            } else {
               if (k != n - 1) {
                  continue;
               }

               at = now - 10000L;
               next = 0L;
               a = alpha;
            }

            if (!(a <= 0.02F)) {
               float out = live ? 1.0F - SingleWord.fadeOut(next, now) : 1.0F - alpha;
               float pop = SingleWord.pop(at, now);
               float sc = scale * (0.7F + 0.3F * pop) * (1.0F + 0.1F * out);
               float px = g.guiWidth() / 2.0F + SingleWord.x(seed, k) * spreadX;
               float py = baseY + SingleWord.y(seed, k) * spreadY + (1.0F - SingleWord.ease(at, now)) * 6.0F - out * 8.0F;
               String word = words[k];
               int ww = Gfx.w(word, Gfx.LYRICS);
               Component comp = Gfx.comp(word, Gfx.LYRICS);
               int wcol = LyricColor.word(c, col, k, words.length);
               g.pose().pushMatrix();
               g.pose().translate(px, py);
               g.pose().rotate((float)Math.toRadians(SingleWord.tilt(seed, k)));
               g.pose().scale(sc, sc);
               g.pose().translate(-ww / 2.0F, -4.5F);
               if (c.lyricsGlow) {
                  for (int q = 0; q < 4; q++) {
                     double ang = q * Math.PI / 2.0 + (Math.PI / 4);
                     g.pose().pushMatrix();
                     g.pose().translate((float)Math.cos(ang) * 0.8F, (float)Math.sin(ang) * 0.8F);
                     g.text(font, comp, 0, 0, Gfx.argbRaw(wcol, a * 0.18F), false);
                     g.pose().popMatrix();
                  }
               } else {
                  g.pose().pushMatrix();
                  g.pose().translate(0.6F, 0.8F);
                  g.text(font, comp, 0, 0, Gfx.argbRaw(shadow, a * 0.55F), false);
                  g.pose().popMatrix();
               }

               g.text(font, comp, 0, 0, Gfx.argbRaw(wcol, a), false);
               g.pose().popMatrix();
            }
         }
      }
   }
}
