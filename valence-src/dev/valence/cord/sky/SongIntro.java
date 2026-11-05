package dev.valence.cord.sky;

import dev.valence.cord.config.Config;
import dev.valence.cord.hud.Gfx;
import dev.valence.cord.hud.SongCard;
import dev.valence.render.MultiBufferSource;
import dev.valence.render.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public final class SongIntro {
   public static final long LENGTH = 4700L;
   private static final int LIGHT = 15728880;
   private static final Matrix4f M0 = new Matrix4f();
   private static final Matrix4f TMP = new Matrix4f();
   private static final Matrix4f M = new Matrix4f();
   private static double smYaw = Double.NaN;
   private static double smEl;
   private static long lastFrame;
   private static long lastStart;
   private static long cutAt;

   public static void cut() {
      long start = SongCard.shownAt();
      if (start != 0L && cutAt <= start && System.currentTimeMillis() - start < 4700L) {
         cutAt = System.currentTimeMillis();
      }
   }

   private SongIntro() {
   }

   static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   static float outCubic(float t) {
      float u = 1.0F - clamp01(t);
      return 1.0F - u * u * u;
   }

   static void renderWorld(Minecraft mc, Config c, MultiBufferSource buffers, Matrix4f base, double camYaw, double camPitch, double skyDist, double dpp) {
      long start = SongCard.shownAt();
      String title = SongCard.title();
      if (start != 0L && !title.isBlank() && c.fxSongIntro && c.lyricsSky && c.lyricsStyle != 2 && !mc.gui.hud.isHidden()) {
         long now = System.currentTimeMillis();
         long age = now - start;
         if (age >= 0L && age <= 4700L) {
            boolean sky = c.lyricsStyle == 0;
            double targetYaw = camYaw;
            double targetEl = sky ? Math.max(2.0, Math.min(66.0, -camPitch + 14.0)) : Math.max(-10.0, Math.min(85.0, -camPitch + 6.0));
            float dt = lastFrame == 0L ? 0.016F : Math.min(0.1F, (float)(now - lastFrame) / 1000.0F);
            lastFrame = now;
            if (Double.isNaN(smYaw) || start != lastStart) {
               lastStart = start;
               smYaw = targetYaw;
               smEl = targetEl;
            }

            double kf = c.lyricsFollow ? 1.0 - Math.exp(-dt * (sky ? 3.0 : 7.0)) : 0.0;
            double dYaw = ((targetYaw - smYaw) % 360.0 + 540.0) % 360.0 - 180.0;
            smYaw += dYaw * kf;
            smEl = smEl + (targetEl - smEl) * kf;
            double d = sky ? 30.0 : 7.5;
            float k = sky ? (float)(Math.toRadians(dpp) * d) : 0.085F * (c.lyricsSize / 1.7F) * 0.8F;
            double az = Math.toRadians(smYaw);
            double el = Math.toRadians(smEl);
            double dx = -Math.sin(az) * Math.cos(el);
            double dy = Math.sin(el);
            double dz = Math.cos(az) * Math.cos(el);
            double r0x = -Math.cos(az);
            double r0z = -Math.sin(az);
            double ux0 = -r0z * dy;
            double uy0 = r0z * dx - r0x * dz;
            double uz0 = r0x * dy;
            M0.set(base)
               .mul(
                  TMP.set(
                     (float)r0x * k,
                     0.0F,
                     (float)r0z * k,
                     0.0F,
                     (float)(-ux0) * k,
                     (float)(-uy0) * k,
                     (float)(-uz0) * k,
                     0.0F,
                     (float)(-dx) * k,
                     (float)(-dy) * k,
                     (float)(-dz) * k,
                     0.0F,
                     (float)(dx * d),
                     (float)(dy * d),
                     (float)(dz * d),
                     1.0F
                  )
               );
            int color = c.lyricsGradient
               ? c.lyricsGradA
               : (c.lyricsColor >= 0 ? WorldSky.LYRIC_COLORS[Math.floorMod(c.lyricsColor, WorldSky.LYRIC_COLORS.length)] : 14272767);
            SongIntro.Phase ph = SongIntro.Phase.at(age);
            if (cutAt > start) {
               float o = outCubic((float)(now - cutAt) / 350.0F);
               ph = new SongIntro.Phase(ph.lines(), ph.label(), ph.title(), ph.artist(), Math.max(ph.out(), o));
            }

            draw(mc.font, buffers, M0, title, SongCard.artist(), ph, color, c.lyricsGradient ? c.lyricsGradB : color, c.lyricsGlow, c.lyricsOpacity);
         }
      }
   }

   private static void draw(
      Font font, MultiBufferSource buffers, Matrix4f m0, String title, String artist, SongIntro.Phase ph, int color, int color2, boolean glow, float opacity
   ) {
      if (title.length() > 34) {
         title = title.substring(0, 33) + "…";
      }

      if (artist.length() > 44) {
         artist = artist.substring(0, 43) + "…";
      }

      float ts = 1.9F;
      float tw = Gfx.w(title, Gfx.LYRICS) * ts;
      float aw = font.width(artist);
      float half = Math.min(150.0F, Math.max(tw, aw) / 2.0F + 16.0F);
      float rise = ph.out() * 5.0F;
      float a = (1.0F - ph.out()) * opacity;
      if (!(a < 0.02F)) {
         int lineCol = Gfx.lerpColor(color2, 16777215, 0.35F);
         DisplayMode mode = DisplayMode.NORMAL;
         float lineLen = half * ph.lines() * (1.0F - 0.6F * ph.out());
         line(font, buffers, m0, lineLen, -16.0F - rise, 0.9F, lineCol, a * 0.9F, mode);
         line(font, buffers, m0, lineLen * 0.82F, 24.0F - rise, 0.9F, lineCol, a * 0.9F, mode);
         String label = "N O W   P L A Y I N G";
         float la = a * ph.label() * 0.75F;
         if (la > 0.02F) {
            float ls = 0.62F;
            float lw = font.width(label) * ls;
            Matrix4f m = M.set(m0).translate(-lw / 2.0F, -27.0F - rise + (1.0F - ph.label()) * 3.0F, 0.02F).scale(ls, ls, 1.0F);
            Txt.drawInBatch(font, Component.literal(label), 0.0F, 0.0F, Gfx.argbRaw(lineCol, la), false, m, buffers, mode, 0, 15728880);
         }

         float ta = a * ph.title();
         if (ta > 0.02F) {
            float pop = 1.0F + 0.12F * (1.0F - ph.title());
            Component t = Gfx.comp(title, Gfx.LYRICS);
            Matrix4f m = M.set(m0)
               .translate(0.0F, -2.0F - rise + (1.0F - ph.title()) * 6.0F, 0.03F)
               .scale(ts * pop, ts * pop, 1.0F)
               .translate(-Gfx.w(title, Gfx.LYRICS) / 2.0F, -4.5F, 0.0F);
            int tc = Gfx.lerpColor(color, 16777215, 0.3F);
            if (glow) {
               for (int q = 0; q < 4; q++) {
                  double ang = q * Math.PI / 2.0 + (Math.PI / 4);
                  Matrix4f g = TMP.set(m).translate((float)Math.cos(ang) * 0.8F, (float)Math.sin(ang) * 0.8F, -0.01F);
                  Txt.drawInBatch(font, t, 0.0F, 0.0F, Gfx.argbRaw(color, ta * 0.2F), false, g, buffers, mode, 0, 15728880);
               }
            } else {
               Matrix4f s = TMP.set(m).translate(0.6F, 0.8F, -0.01F);
               Txt.drawInBatch(font, t, 0.0F, 0.0F, Gfx.argbRaw(Gfx.lerpColor(color, 328714, 0.85F), ta * 0.5F), false, s, buffers, mode, 0, 15728880);
            }

            Txt.drawInBatch(font, t, 0.0F, 0.0F, Gfx.argbRaw(tc, ta), false, m, buffers, mode, 0, 15728880);
         }

         float aa = a * ph.artist() * 0.85F;
         if (aa > 0.02F && !artist.isBlank()) {
            Matrix4f m = M.set(m0).translate(-aw / 2.0F, 11.0F - rise + (1.0F - ph.artist()) * 4.0F, 0.03F);
            Txt.drawInBatch(font, Component.literal(artist), 0.0F, 0.0F, Gfx.argbRaw(16777215, aa), false, m, buffers, mode, 0, 15728880);
         }
      }
   }

   private static void line(Font font, MultiBufferSource buffers, Matrix4f m0, float halfLen, float y, float thick, int rgb, float a, DisplayMode mode) {
      if (!(halfLen < 1.0F) && !(a < 0.02F)) {
         int seg = 14;
         float step = halfLen * 2.0F / seg;

         for (int i = 0; i < seg; i++) {
            float x0 = -halfLen + i * step;
            float mid = (i + 0.5F) / seg * 2.0F - 1.0F;
            float edge = 1.0F - Math.abs(mid);
            float sa = a * (edge < 0.35F ? edge / 0.35F : 1.0F);
            if (!(sa < 0.02F)) {
               Matrix4f m = M.set(m0).translate(x0, y - thick / 2.0F, 0.02F).scale(step / 5.0F, thick / 10.0F, 1.0F).translate(1.0F, 1.0F, 0.0F);
               Txt.drawInBatch(font, " ", 0.0F, 0.0F, 16777215, false, m, buffers, mode, Gfx.argbRaw(rgb, sa), 15728880);
            }
         }
      }
   }

   public record Phase(float lines, float label, float title, float artist, float out) {
      public static SongIntro.Phase at(long age) {
         float out = age > 4000L ? SongIntro.outCubic((float)(age - 4000L) / 700.0F) : 0.0F;
         return new SongIntro.Phase(
            SongIntro.outCubic((float)age / 650.0F),
            SongIntro.outCubic((float)(age - 100L) / 420.0F),
            SongIntro.outCubic((float)(age - 180L) / 560.0F),
            SongIntro.outCubic((float)(age - 380L) / 520.0F),
            out
         );
      }
   }
}
