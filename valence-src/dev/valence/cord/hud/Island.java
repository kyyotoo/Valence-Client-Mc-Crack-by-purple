package dev.valence.cord.hud;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.lyrics.NowPlaying;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.cord.lyrics.Translator;
import dev.valence.cord.util.AutoPerf;
import dev.valence.gui.ValenceScreen;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public final class Island {
   private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
   private static final Island.Spring W = new Island.Spring(60.0F, 170.0F, 19.0F);
   private static final Island.Spring H = new Island.Spring(14.0F, 170.0F, 19.0F);
   private static final Island.Spring HL_X = new Island.Spring(0.0F, 260.0F, 30.0F);
   private static final Island.Spring HL_W = new Island.Spring(0.0F, 260.0F, 30.0F);
   private static final Island.Spring SCROLL = new Island.Spring(0.0F, 90.0F, 18.0F);
   private static final Island.Spring SHOW = new Island.Spring(0.0F, 120.0F, 20.0F);
   private static String note = null;
   private static boolean noteOk = true;
   private static long noteTime = 0L;
   private static String curKey = "";
   private static long switchTime = 0L;
   private static Island.Kind prevKind = Island.Kind.IDLE;
   private static String prevText = "";
   private static Island.Kind curKind = Island.Kind.IDLE;
   private static String curText = "";
   private static long lastNanos = System.nanoTime();

   private Island() {
   }

   public static void notify(String text, boolean ok) {
      note = text;
      noteOk = ok;
      noteTime = System.currentTimeMillis();
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      Config c = NeonLyrics.cfg();
      if (!mc.gui.hud.isHidden() && mc.player != null && c.islandEnabled && !(mc.gui.screen() instanceof ValenceScreen)) {
         AutoPerf.begin();

         try {
            renderIsland(g, mc, c);
         } finally {
            AutoPerf.end();
         }
      }
   }

   private static void renderIsland(GuiGraphicsExtractor g, Minecraft mc, Config c) {
      long nowNs = System.nanoTime();
      float dt = Math.min(0.05F, (float)(nowNs - lastNanos) / 1.0E9F);
      lastNanos = nowNs;
      long now = System.currentTimeMillis();
      int sw = g.guiWidth();
      int cx = sw / 2;
      int top = 5;
      float old = Gfx.alpha;
      Gfx.alpha = 1.0F;
      int acc = Theme.accent();
      boolean noteActive = note != null && now - noteTime < 2400L;
      NowPlaying np = SpotifyClient.now();
      boolean hasSong = c.lyricsIsland && np.hasTrack();
      long prog = np.progressNow(c.lyricsOffsetMs + 40);
      int li = hasSong && np.lyricsState == NowPlaying.LyricsState.SYNCED && np.playing ? np.lineIndex(prog) : -1;
      NowPlaying.Line line = li >= 0 ? np.lines.get(li) : null;
      boolean hasLine = line != null && !line.text().isBlank() && prog <= np.lineEnd(li) + 400L;
      Island.Kind kind;
      String text;
      String key;
      if (noteActive) {
         kind = Island.Kind.NOTE;
         text = note;
         key = "n" + noteTime;
      } else if (hasLine) {
         kind = Island.Kind.LYRIC;
         text = line.text();
         key = "l" + np.trackId + li;
      } else if (hasSong) {
         kind = Island.Kind.SONG;
         text = np.title + (np.artist.isBlank() ? "" : "  ·  " + np.artist);
         key = "s" + np.trackId;
      } else {
         kind = Island.Kind.IDLE;
         text = "";
         key = "i";
      }

      if (!key.equals(curKey)) {
         prevKind = curKind;
         prevText = curText;
         curKey = key;
         curKind = kind;
         curText = text;
         switchTime = now;
         if (kind == Island.Kind.LYRIC) {
            SCROLL.snap(0.0F);
            HL_X.snap(0.0F);
            HL_W.snap(0.0F);
         }
      }

      int musicW = Math.min(176, sw - 140);
      float targetW;
      float targetH;
      switch (kind) {
         case NOTE:
            targetW = Math.min(musicW, Gfx.w(text) + 40);
            targetH = 18.0F;
            break;
         case SONG:
         case LYRIC:
            targetW = musicW;
            targetH = 18.0F;
            break;
         default:
            targetW = 40.0F;
            targetH = 14.0F;
      }

      boolean idleHidden = kind == Island.Kind.IDLE && !c.showClock && !c.showPing && !c.showFps;
      float show = SHOW.update(idleHidden ? 0.0F : 1.0F, dt);
      float w = W.update(targetW, dt);
      float h = H.update(targetH, dt);
      if (show < 0.02F) {
         Gfx.alpha = old;
      } else {
         int pw = Math.max(12, Math.round(w));
         int ph = Math.max(8, Math.round(h));
         int px = cx - pw / 2;
         int r = ph / 2;
         int midY = top + ph / 2;
         Gfx.alpha = show;
         int lx = px - 10;
         if (c.showFps) {
            String fs = mc.getFps() + " fps";
            lx -= Gfx.w(fs, Gfx.BOLD);
            Gfx.textBold(g, fs, lx, midY - 4, 16777215, 0.9F, true);
            lx -= 10;
         }

         if (c.showClock) {
            String t = LocalTime.now().format(HM);
            lx -= Gfx.w(t, Gfx.BOLD);
            Gfx.textBold(g, t, lx, midY - 4, 16777215, 0.95F, true);
         }

         int rx = px + pw + 10;
         if (c.showPing && mc.getConnection() != null) {
            PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            int ping = pi == null ? 0 : pi.getLatency();
            int bars = ping <= 0 ? 0 : (ping < 60 ? 4 : (ping < 120 ? 3 : (ping < 250 ? 2 : 1)));
            float[] ax = new float[9];
            float[] ay = new float[9];

            for (int i = 0; i < 9; i++) {
               double an = Math.PI + i * Math.PI / 8.0;
               ax[i] = rx + 3.5F + (float)Math.cos(an) * 3.0F;
               ay[i] = midY + 0.5F + (float)Math.sin(an) * 3.0F;
            }

            Gfx.aaStroke(g, ax, ay, 1.1F, 10326968, 0.9F);
            String ps = Integer.toString(ping);
            Gfx.textBold(g, ps, rx + 9, midY - 4, 16777215, 0.95F, true);
            int bx = rx + 9 + Gfx.w(ps, Gfx.BOLD) + 5;

            for (int i = 0; i < 4; i++) {
               int bh = 2 + i * 2;
               Gfx.aaRound(g, bx + i * 3, midY + 4 - bh, 2.0F, bh, 1.0F, 16777215, i < bars ? 0.95F : 0.22F);
            }

            rx = bx + 16;
         }

         float fx = cx - w / 2.0F;
         float fh = h;
         float fr = h / 2.0F;
         if (AutoPerf.allow(2)) {
            for (int k = 3; k >= 1; k--) {
               Gfx.aaRound(g, fx - k, top - k + 1.5F, w + k * 2, fh + k * 2, fr + k, 0, 0.05F);
            }
         }

         Gfx.aaRound(g, fx - 0.75F, top - 0.75F, w + 1.5F, fh + 1.5F, fr + 0.75F, 12167664, 0.38F);
         Gfx.aaRound(g, fx, top, w, fh, fr, 1380895, 0.94F);
         g.enableScissor(px + 2, top, px + pw - 2, top + ph);
         float e = Anim.outCubic(Anim.since(switchTime, 420L));
         float slide = ph * 0.75F;
         if (e < 1.0F && prevKind != Island.Kind.IDLE && !prevText.isEmpty()) {
            Gfx.alpha = show * (1.0F - e) * (1.0F - e);
            g.pose().pushMatrix();
            g.pose().translate(0.0F, -e * slide);
            drawSimple(g, prevKind, prevText, px, top, pw, ph, acc, np, now, false);
            g.pose().popMatrix();
         }

         Gfx.alpha = show * e;
         g.pose().pushMatrix();
         g.pose().translate(0.0F, (1.0F - e) * slide);
         switch (kind) {
            case IDLE:
               float br = 0.5F + 0.5F * (float)Math.sin(now / 900.0);
               Gfx.aaCircle(g, cx, top + h / 2.0F, 2.2F, acc, 0.55F + 0.35F * br);
               break;
            case LYRIC:
               drawLyric(g, line, np, li, prog, px, top, pw, ph, acc, dt, e);
               break;
            default:
               drawSimple(g, kind, text, px, top, pw, ph, acc, np, now, true);
         }

         g.pose().popMatrix();
         if ((kind == Island.Kind.LYRIC || kind == Island.Kind.SONG) && np.durationMs > 0L) {
            float frac = Math.max(0.0F, Math.min(1.0F, (float)np.progressNow(0) / (float)np.durationMs));
            float lx0 = fx + fr;
            float lw = Math.max(0.0F, w - fr * 2.0F);
            Gfx.alpha = show;
            Gfx.aaRound(g, lx0, top + fh - 2.0F, lw, 1.0F, 0.5F, 16777215, 0.08F);
            Gfx.aaRound(g, lx0, top + fh - 2.0F, lw * frac, 1.0F, 0.5F, acc, 0.75F);
         }

         g.disableScissor();
         if (kind == Island.Kind.LYRIC && c.translate) {
            String tr = Translator.get(line.text());

            for (int k2 = 1; k2 <= 2 && li + k2 < np.lines.size(); k2++) {
               Translator.prefetch(np.lines.get(li + k2).text());
            }

            if (tr != null) {
               Gfx.alpha = show * e;
               g.pose().pushMatrix();
               g.pose().translate(cx, top + ph + 4);
               g.pose().scale(0.85F, 0.85F);
               Gfx.center(g, Gfx.ellipsize(tr, Math.round(pw / 0.85F)), 0, 0, 14275058, 0.9F, true);
               g.pose().popMatrix();
            }
         }

         Gfx.alpha = old;
      }
   }

   private static void cover(GuiGraphicsExtractor g, int cx, int cy, int acc, boolean playing, long now) {
      if (!Cover.draw(g, cx - 6, cy - 6, 12, 1.0F)) {
         Gfx.aaCircle(g, cx, cy, 6.0F, Gfx.lerpColor(acc, 2760266, 0.35F), 1.0F);
         Gfx.aaCircle(g, cx, cy, 4.6F, Gfx.lerpColor(acc, 16777215, 0.25F), 0.9F);
         double an = playing ? now / 450.0 : 0.8;
         Gfx.aaCircle(g, (float)(cx + Math.cos(an) * 3.0), (float)(cy + Math.sin(an) * 3.0), 1.1F, 16777215, 0.75F);
         Gfx.aaCircle(g, cx, cy, 1.4F, 1380895, 1.0F);
      }
   }

   private static void drawSimple(
      GuiGraphicsExtractor g, Island.Kind kind, String text, int px, int top, int pw, int ph, int acc, NowPlaying np, long now, boolean live
   ) {
      int midY = top + ph / 2;
      int ty = midY - 4;
      if (kind == Island.Kind.NOTE) {
         int col = noteOk ? 6022299 : 16735354;
         float pop = live ? Anim.outBack(Anim.since(noteTime, 380L)) : 1.0F;
         float ccx = px + 11;
         float ccy = top + ph / 2.0F;
         Gfx.aaCircle(g, ccx, ccy, 5.0F * pop, col, 1.0F);
         if (noteOk) {
            Gfx.aaStroke(g, new float[]{ccx - 2.3F, ccx - 0.6F, ccx + 2.4F}, new float[]{ccy + 0.1F, ccy + 1.8F, ccy - 1.8F}, 1.3F * pop, 16777215, 1.0F);
         } else {
            Gfx.aaRound(g, ccx - 2.5F, ccy - 0.7F, 5.0F, 1.4F, 0.7F, 16777215, 1.0F);
         }

         Gfx.text(g, Gfx.ellipsize(text, pw - 30), px + 21, ty, 16777215, 1.0F, false);
      } else if (kind == Island.Kind.SONG || kind == Island.Kind.LYRIC) {
         cover(g, px + 11, midY, acc, np.playing, now);
         equalizer(g, px + pw - 20, midY, acc, np.playing, now);
         Gfx.text(g, Gfx.ellipsize(text, pw - 50), px + 22, ty, 16777215, kind == Island.Kind.SONG ? 0.95F : 0.45F, false);
      }
   }

   private static void equalizer(GuiGraphicsExtractor g, int x, int cy, int acc, boolean playing, long now) {
      if (!AutoPerf.allow(4)) {
         playing = false;
      }

      double t = now / 1000.0;
      NowPlaying np = SpotifyClient.now();
      float beat = np.beatPulse(np.progressNow(NeonLyrics.cfg().lyricsOffsetMs));

      for (int i = 0; i < 4; i++) {
         float hh = playing ? (float)(3.0 + 3.5 * Math.abs(Math.sin(t * (2.6 + i * 1.1) + i * 1.7)) + 3.5 * beat * (i % 2 == 0 ? 1.0 : 0.7)) : 2.5F;
         int bh = Math.round(hh);
         Gfx.aaRound(g, x + i * 3, cy - hh / 2.0F, 2.0F, hh, 1.0F, acc, 0.95F);
      }
   }

   private static void drawLyric(
      GuiGraphicsExtractor g, NowPlaying.Line line, NowPlaying np, int li, long prog, int px, int top, int pw, int ph, int acc, float dt, float in
   ) {
      long now = System.currentTimeMillis();
      int midY = top + ph / 2;
      cover(g, px + 11, midY, acc, np.playing, now);
      equalizer(g, px + pw - 20, midY, acc, np.playing, now);
      int tx = px + 22;
      int tw = pw - 22 - 26;
      String[] words = line.words();
      if (words.length != 0) {
         float wp = np.wordPosition(li, prog);
         int cw = wp < 0.0F ? words.length - 1 : Math.min(words.length - 1, (int)Math.floor(wp));
         float frac = wp < 0.0F ? 1.0F : Math.max(0.0F, Math.min(1.0F, wp - (float)Math.floor(wp)));
         int space = Gfx.w(" ");
         int[] wx = new int[words.length];
         int[] ww = new int[words.length];
         int x = 0;

         for (int i = 0; i < words.length; i++) {
            wx[i] = x;
            ww[i] = Gfx.w(words[i]);
            x += ww[i] + space;
         }

         int full = x - space;
         float sx = full > tw ? Math.max(0.0F, Math.min(full - tw, wx[cw] + ww[cw] / 2.0F - tw * 0.45F)) : 0.0F;
         float scroll = SCROLL.update(sx, dt);
         float hx = HL_X.update(wx[cw], dt);
         float hw = HL_W.update(ww[cw], dt);
         int base = tx - Math.round(scroll) + (full < tw ? (tw - full) / 2 : 0);
         int ty = midY - 4;
         g.enableScissor(tx - 2, top, tx + tw + 2, top + ph);
         Gfx.aaRound(g, base + hx - 3.0F, top + 3.5F, hw + 6.0F, ph - 7.0F, (ph - 7.0F) / 2.0F, acc, 0.28F);

         for (int i = 0; i < words.length; i++) {
            int wx0 = base + wx[i];
            if (wx0 + ww[i] >= tx - 4 && wx0 <= tx + tw + 4) {
               if (i < cw) {
                  Gfx.text(g, words[i], wx0, ty, 16777215, 1.0F, false);
               } else if (i > cw) {
                  Gfx.text(g, words[i], wx0, ty, 16777215, 0.36F, false);
               } else {
                  Gfx.text(g, words[i], wx0, ty, 16777215, 0.36F, false);
                  int fillX = wx0 + Math.round(ww[i] * frac);
                  if (fillX > wx0) {
                     g.enableScissor(Math.max(tx - 2, wx0 - 1), top, Math.min(tx + tw + 2, fillX), top + ph);
                     Gfx.text(g, words[i], wx0, ty, Gfx.lerpColor(acc, 16777215, 0.55F), 1.0F, false);
                     g.disableScissor();
                  }
               }
            }
         }

         g.disableScissor();
      }
   }

   private enum Kind {
      IDLE,
      NOTE,
      SONG,
      LYRIC;
   }

   private static final class Spring {
      float x;
      float v;
      final float k;
      final float d;

      Spring(float x, float stiffness, float damping) {
         this.x = x;
         this.k = stiffness;
         this.d = damping;
      }

      float update(float target, float dt) {
         int steps = Math.max(1, (int)Math.ceil(dt / 0.008F));
         float h = dt / steps;

         for (int i = 0; i < steps; i++) {
            float a = this.k * (target - this.x) - this.d * this.v;
            this.v += a * h;
            this.x = this.x + this.v * h;
         }

         return this.x;
      }

      void snap(float t) {
         this.x = t;
         this.v = 0.0F;
      }
   }
}
