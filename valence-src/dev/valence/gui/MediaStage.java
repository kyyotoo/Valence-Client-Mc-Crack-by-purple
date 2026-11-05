package dev.valence.gui;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.hud.Cover;
import dev.valence.cord.hud.Gfx;
import dev.valence.cord.hud.Icons;
import dev.valence.cord.lyrics.Favorites;
import dev.valence.cord.lyrics.SpotifyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class MediaStage {
   private float spin;
   private float viz;
   private String lastTrack = "";
   private long trackAt;
   private long pressedPlay;
   private final long[] pressed = new long[4];
   private int bx;
   private int by;
   private boolean laid;
   public static final int MIN_W = 150;

   public void render(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my, float appear) {
      dev.valence.cord.lyrics.NowPlaying np = SpotifyClient.nowOrLast();
      if (np.hasTrack() && !(appear < 0.01F)) {
         long now = System.currentTimeMillis();
         float dt = Anim.dt();
         String track = String.valueOf(np.trackId);
         if (!track.equals(this.lastTrack)) {
            this.lastTrack = track;
            this.trackAt = now;
         }

         float old = Gfx.alpha;
         Gfx.alpha = appear;
         float trackIn = Theme.easeOutCubic(Math.min(1.0F, (float)(now - this.trackAt) / 450.0F));
         int cs = Math.round(Math.max(56.0F, Math.min(124.0F, Math.min(w / 1.55F, h * 0.34F))));
         int cy = y + Math.round(h * 0.06F);
         if (np.playing) {
            this.spin = (this.spin + dt * 70.0F) % 360.0F;
         }

         float vin = Theme.easeOutCubic(Math.min(1.0F, appear * 1.2F)) * trackIn;
         float vr = cs * 0.47F;
         float vcx = x + cs / 2.0F + vin * cs * 0.55F;
         float vcy = cy + cs / 2.0F;
         Gfx.aaCircle(g, vcx, vcy, vr, 789518, 1.0F);

         for (int i = 1; i <= 7; i++) {
            Gfx.aaCircle(g, vcx, vcy, vr * (1.0F - 0.075F * i), (i & 1) == 1 ? 1513243 : 789518, 1.0F);
         }

         float ls = vr * 0.62F;
         g.pose().pushMatrix();
         g.pose().translate(vcx, vcy);
         g.pose().rotate((float)Math.toRadians(this.spin));
         Gfx.aaRound(g, -vr * 0.92F, -0.75F, vr * 1.84F, 1.5F, 0.75F, 16777215, 0.05F);
         Gfx.aaRound(g, -0.75F, -vr * 0.92F, 1.5F, vr * 1.84F, 0.75F, 16777215, 0.03F);
         if (!Cover.draw(g, Math.round(-ls / 2.0F), Math.round(-ls / 2.0F), Math.round(ls), 1.0F)) {
            Gfx.aaCircle(g, 0.0F, 0.0F, ls / 2.0F, 2763315, 1.0F);
         }

         Gfx.aaCircle(g, 0.0F, 0.0F, 2.2F, 789518, 1.0F);
         g.pose().popMatrix();
         float pop = 0.94F + 0.06F * trackIn;
         g.pose().pushMatrix();
         g.pose().translate(x + cs / 2.0F, cy + cs / 2.0F);
         g.pose().scale(pop, pop);
         Gfx.aaRound(g, -cs / 2.0F - 2.0F, -cs / 2.0F + 4.0F, cs + 4, cs + 4, 10.0F, 0, 0.35F);
         if (!Cover.drawSquare(g, -cs / 2, -cs / 2, cs, 1.0F)) {
            Gfx.aaRound(g, -cs / 2.0F, -cs / 2.0F, cs, cs, 8.0F, 1973798, 1.0F);
            Icons.draw(g, Icons.Icon.LYRICS, 0.0F, 0.0F, cs * 0.26F, 4539725, 1.0F);
         }

         g.pose().popMatrix();
         int ty = cy + cs + 14;
         float meta = trackIn;
         int lift = Math.round((1.0F - meta) * 8.0F);
         float ts = 1.35F;
         g.pose().pushMatrix();
         g.pose().translate(x, ty + lift);
         g.pose().scale(ts, ts);
         Gfx.textBold(g, Gfx.ellipsize(np.title, Math.round(w / ts)), 0, 0, 16052479, meta, true);
         g.pose().popMatrix();
         Gfx.text(g, Gfx.ellipsize(np.artist, w), x, ty + 15 + lift, 9406376, meta, false);
         if ("last".equals(np.trackId)) {
            Gfx.text(g, Gfx.ellipsize(Component.translatable("valence.stage.last").getString(), w), x, ty + 33, 7235208, 1.0F, false);
         }

         int lyY = ty + 33;
         Config c = NeonLyrics.cfg();
         if (np.lyricsState == dev.valence.cord.lyrics.NowPlaying.LyricsState.SYNCED) {
            long prog = np.progressNow(c.lyricsOffsetMs + 40);
            int li = np.lineIndex(prog);
            if (li >= 0 && li < np.lines.size() && !np.lines.get(li).text().isBlank()) {
               dev.valence.cord.lyrics.NowPlaying.Line ln = np.lines.get(li);
               String[] words = ln.words();
               float wp = np.wordPosition(li, prog);
               int sung = wp < 0.0F ? words.length : (int)Math.floor(wp) + 1;
               float la = Anim.approach(Anim.key(91, li), 1.0F, 10.0F);
               int xx = x;
               int yy = lyY;
               int rows = 0;
               int space = Gfx.w(" ");

               for (int k = 0; k < words.length && rows < 2; k++) {
                  int ww = Gfx.w(words[k]);
                  if (xx + ww > x + w && xx > x) {
                     xx = x;
                     yy += 11;
                     if (++rows >= 2) {
                        break;
                     }
                  }

                  Gfx.text(g, words[k], xx, yy, k < sung ? 16052479 : 5920104, la, false);
                  xx += ww + space;
               }

               if (li + 1 < np.lines.size()) {
                  Gfx.text(g, Gfx.ellipsize(np.lines.get(li + 1).text(), w), x, yy + 14, 5920104, la * 0.9F, false);
               }
            }
         } else {
            String key = switch (np.lyricsState) {
               case LOADING -> "valence.lyrics.state.loading";
               case NOT_FOUND -> "valence.lyrics.state.not_found";
               case ERROR -> "valence.lyrics.state.error";
               default -> "";
            };
            if (!key.isEmpty()) {
               Gfx.text(g, Gfx.ellipsize(Component.translatable(key).getString(), w), x, lyY, 9406376, 1.0F, false);
            }
         }

         int rowY = y + h - 16;
         int controlsW = 118;
         boolean twoRows = w < 210;
         int barsW = twoRows ? w : w - controlsW - 10;
         int bars = Math.max(8, barsW / 6);
         int bh = 26;
         int waveY = twoRows ? rowY - 22 : rowY + 10;
         long prog = np.progressNow(c.lyricsOffsetMs);
         float beat = np.playing ? np.beatPulse(prog) : 0.0F;
         this.viz = this.viz + ((np.playing ? 1.0F : 0.12F) - this.viz) * (1.0F - (float)Math.exp(-6.0F * dt));
         double tt = now / 1000.0;

         for (int i = 0; i < bars; i++) {
            double wave = Math.abs(Math.sin(i * 0.7 + tt * 6.0) * Math.sin(i * 0.23 + tt * 3.1));
            float hh = 3.0F + (bh - 3) * (float)wave * this.viz * (0.55F + 0.6F * beat);
            Gfx.aaRound(g, x + i * 6, waveY - hh, 3.0F, hh, 1.5F, 16777215, 0.8F);
         }

         this.bx = twoRows ? x + 4 : x + bars * 6 + 14;
         this.by = rowY;
         this.laid = true;
         this.button(g, 0, Icons.Icon.PREV, this.bx + 10, this.by, 10, mx, my);
         float hv = Anim.hover(Anim.key(90, 9), inside(mx, my, this.bx + 28, this.by - 14, 28.0, 28.0));
         float press = 1.0F - Theme.easeOutCubic(Math.min(1.0F, (float)(now - this.pressedPlay) / 220.0F));
         Gfx.aaCircle(g, this.bx + 42, this.by, 13.0F * (1.0F + 0.06F * hv) * (1.0F - 0.08F * press), 16777215, 1.0F);
         Icons.draw(g, np.playing ? Icons.Icon.PAUSE : Icons.Icon.PLAY, this.bx + 42 + (np.playing ? 0.0F : 0.8F), this.by, 9.5F, 1118484, 1.0F);
         this.button(g, 1, Icons.Icon.NEXT, this.bx + 74, this.by, 10, mx, my);
         this.button(g, 2, Icons.Icon.FAV, this.bx + 102, this.by, 10, mx, my);
         Gfx.alpha = old;
      } else {
         this.laid = false;
      }
   }

   private void button(GuiGraphicsExtractor g, int id, Icons.Icon icon, int cx, int cy, int r, int mx, int my) {
      float hv = Anim.hover(Anim.key(90, id), inside(mx, my, cx - r, cy - r, r * 2, r * 2));
      float press = 1.0F - Theme.easeOutCubic(Math.min(1.0F, (float)(System.currentTimeMillis() - this.pressed[id]) / 220.0F));
      if (hv > 0.01F) {
         Gfx.aaCircle(g, cx, cy, r * (0.9F + 0.1F * hv), 16777215, 0.12F * hv);
      }

      Icons.draw(g, icon, cx, cy, 8.0F * (1.0F - 0.1F * press), 16777215, 0.65F + 0.35F * hv);
   }

   private static boolean inside(double mx, double my, double x, double y, double w, double h) {
      return mx >= x && mx < x + w && my >= y && my < y + h;
   }

   public boolean click(double mx, double my) {
      if (!this.laid) {
         return false;
      } else {
         long now = System.currentTimeMillis();
         if (inside(mx, my, this.bx, this.by - 10, 20.0, 20.0)) {
            this.pressed[0] = now;
            UiSounds.click();
            SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
            return true;
         } else if (inside(mx, my, this.bx + 28, this.by - 14, 28.0, 28.0)) {
            this.pressedPlay = now;
            UiSounds.click();
            SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
            return true;
         } else if (inside(mx, my, this.bx + 64, this.by - 10, 20.0, 20.0)) {
            this.pressed[1] = now;
            UiSounds.click();
            SpotifyClient.control(SpotifyClient.Cmd.NEXT);
            return true;
         } else if (inside(mx, my, this.bx + 92, this.by - 10, 20.0, 20.0)) {
            this.pressed[2] = now;
            UiSounds.toggle(true);
            Favorites.saveCurrent();
            return true;
         } else {
            return false;
         }
      }
   }
}
