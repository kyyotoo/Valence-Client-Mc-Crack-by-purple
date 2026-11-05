package dev.valence.cord.hud;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.sky.SongIntro;
import dev.valence.cord.sky.WorldSky;
import dev.valence.gui.ValenceScreen;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class SongCard {
   private static final long IN = 420L;
   private static final long HOLD = 4200L;
   private static final long OUT = 380L;
   private static volatile String title = "";
   private static volatile String artist = "";
   private static volatile long shownAt = 0L;

   private SongCard() {
   }

   public static void show(String songTitle, String songArtist) {
      title = songTitle == null ? "" : songTitle;
      artist = songArtist == null ? "" : songArtist;
      shownAt = System.currentTimeMillis();
   }

   private static float outCubic(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float k = 1.0F - t;
      return 1.0F - k * k * k;
   }

   private static float outBack(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   public static String title() {
      return title;
   }

   public static String artist() {
      return artist;
   }

   public static long shownAt() {
      return shownAt;
   }

   public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
      Minecraft mc = Minecraft.getInstance();
      Config c = NeonLyrics.cfg();
      long start = shownAt;
      if (start != 0L
         && !mc.gui.hud.isHidden()
         && mc.player != null
         && c.fxSongIntro
         && c.lyricsSky
         && c.lyricsStyle == 2
         && !(mc.gui.screen() instanceof ValenceScreen)) {
         long age = System.currentTimeMillis() - start;
         if (age >= 0L && age <= 4700L && !title.isBlank()) {
            SongIntro.Phase ph = SongIntro.Phase.at(age);
            float a = (1.0F - ph.out()) * c.lyricsOpacity;
            if (!(a < 0.02F)) {
               int color = c.lyricsGradient
                  ? c.lyricsGradA
                  : (c.lyricsColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsColor, WorldSky.LYRIC_COLORS.length)] : 14272767);
               int lineCol = Gfx.lerpColor(c.lyricsGradient ? c.lyricsGradB : color, 16777215, 0.35F);
               Font font = mc.font;
               String t = title.length() > 34 ? title.substring(0, 33) + "…" : title;
               String ar = artist.length() > 44 ? artist.substring(0, 43) + "…" : artist;
               float scale = Math.max(0.6F, c.lyricsSize) / 1.6F;
               float ts = 1.9F;
               float tw = Gfx.w(t, Gfx.LYRICS) * ts;
               float aw = font.width(ar);
               float half = Math.min(150.0F, Math.max(tw, aw) / 2.0F + 16.0F);
               float rise = ph.out() * 5.0F;
               float oldA = Gfx.alpha;
               Gfx.alpha = 1.0F;
               g.pose().pushMatrix();
               g.pose().translate(g.guiWidth() / 2.0F, g.guiHeight() * 0.7F - 46.0F * scale);
               g.pose().scale(scale, scale);
               float len = half * ph.lines() * (1.0F - 0.6F * ph.out());
               line(g, len, -16.0F - rise, lineCol, a * 0.9F);
               line(g, len * 0.82F, 24.0F - rise, lineCol, a * 0.9F);
               String label = "N O W   P L A Y I N G";
               float la = a * ph.label() * 0.75F;
               if (la > 0.02F) {
                  g.pose().pushMatrix();
                  g.pose().translate(0.0F, -27.0F - rise + (1.0F - ph.label()) * 3.0F);
                  g.pose().scale(0.62F, 0.62F);
                  g.text(font, label, -font.width(label) / 2, 0, Gfx.argbRaw(lineCol, la), false);
                  g.pose().popMatrix();
               }

               float ta = a * ph.title();
               if (ta > 0.02F) {
                  float pop = 1.0F + 0.12F * (1.0F - ph.title());
                  Component comp = Gfx.comp(t, Gfx.LYRICS);
                  int w = Gfx.w(t, Gfx.LYRICS);
                  g.pose().pushMatrix();
                  g.pose().translate(0.0F, -2.0F - rise + (1.0F - ph.title()) * 6.0F);
                  g.pose().scale(ts * pop, ts * pop);
                  g.pose().translate(-w / 2.0F, -4.5F);
                  if (c.lyricsGlow) {
                     for (int q = 0; q < 4; q++) {
                        double ang = q * Math.PI / 2.0 + (Math.PI / 4);
                        g.pose().pushMatrix();
                        g.pose().translate((float)Math.cos(ang) * 0.8F, (float)Math.sin(ang) * 0.8F);
                        g.text(font, comp, 0, 0, Gfx.argbRaw(color, ta * 0.2F), false);
                        g.pose().popMatrix();
                     }
                  } else {
                     g.pose().pushMatrix();
                     g.pose().translate(0.6F, 0.8F);
                     g.text(font, comp, 0, 0, Gfx.argbRaw(Gfx.lerpColor(color, 328714, 0.85F), ta * 0.5F), false);
                     g.pose().popMatrix();
                  }

                  g.text(font, comp, 0, 0, Gfx.argbRaw(Gfx.lerpColor(color, 16777215, 0.3F), ta), false);
                  g.pose().popMatrix();
               }

               float aa = a * ph.artist() * 0.85F;
               if (aa > 0.02F && !ar.isBlank()) {
                  g.pose().pushMatrix();
                  g.pose().translate(-aw / 2.0F, 11.0F - rise + (1.0F - ph.artist()) * 4.0F);
                  g.text(font, ar, 0, 0, Gfx.argbRaw(16777215, aa), false);
                  g.pose().popMatrix();
               }

               g.pose().popMatrix();
               Gfx.alpha = oldA;
            }
         }
      }
   }

   private static void line(GuiGraphicsExtractor g, float half, float y, int rgb, float a) {
      if (!(half < 1.0F) && !(a < 0.02F)) {
         int seg = 14;
         float step = half * 2.0F / seg;

         for (int i = 0; i < seg; i++) {
            float mid = (i + 0.5F) / seg * 2.0F - 1.0F;
            float edge = 1.0F - Math.abs(mid);
            float sa = a * (edge < 0.35F ? edge / 0.35F : 1.0F);
            if (!(sa < 0.02F)) {
               float x0 = -half + i * step;
               g.pose().pushMatrix();
               g.pose().translate(x0, y - 0.5F);
               g.pose().scale(step / 10.0F, 0.1F);
               g.fill(0, 0, 10, 10, Gfx.argbRaw(rgb, sa));
               g.pose().popMatrix();
            }
         }
      }
   }
}
