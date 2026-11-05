package dev.valence.cord.util;

import dev.valence.config.ValenceConfig;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.hud.Gfx;
import dev.valence.cord.hud.Island;
import dev.valence.cord.sky.FloatWords;

public final class Profiles {
   private Profiles() {
   }

   private static void swapLyricLook(Config c, boolean toChill) {
      if (toChill) {
         c.pvpLyricsStyle = c.lyricsStyle;
         c.pvpLyricsFont = c.lyricsFont;
         c.pvpLyricsColor = c.lyricsColor;
         c.pvpLyricsGlow = c.lyricsGlow;
         c.pvpLyricsSize = c.lyricsSize;
         c.lyricsStyle = c.chillLyricsStyle;
         c.lyricsFont = c.chillLyricsFont;
         c.lyricsColor = c.chillLyricsColor;
         c.lyricsGlow = c.chillLyricsGlow;
         c.lyricsSize = c.chillLyricsSize;
      } else {
         c.chillLyricsStyle = c.lyricsStyle;
         c.chillLyricsFont = c.lyricsFont;
         c.chillLyricsColor = c.lyricsColor;
         c.chillLyricsGlow = c.lyricsGlow;
         c.chillLyricsSize = c.lyricsSize;
         c.lyricsStyle = c.pvpLyricsStyle;
         c.lyricsFont = c.pvpLyricsFont;
         c.lyricsColor = c.pvpLyricsColor;
         c.lyricsGlow = c.pvpLyricsGlow;
         c.lyricsSize = c.pvpLyricsSize;
      }

      Gfx.clearWidths();
      FloatWords.clear();
   }

   public static void toggle() {
      set(NeonLyrics.cfg().profile == 0 ? 1 : 0);
   }

   public static void set(int profile) {
      Config c = NeonLyrics.cfg();
      boolean on = profile == 0;
      if (profile != c.profile) {
         swapLyricLook(c, on);
      }

      ValenceConfig vc = ValenceConfig.get();
      vc.skyEnabled = on;
      vc.skyParticles = on;
      vc.skyShootingStars = on;
      vc.skyConstellations = on;
      ValenceConfig.save();
      c.fxSkyColor = on;
      c.fxWeather = on;
      c.fxAurora = on;
      c.fxStars = on;
      c.fxBeams = on;
      c.blur = on;
      c.fxMenuAnim = on;
      c.fxConstellations = on;
      c.fxSkyMusic = on;
      c.quality = on ? 1 : 0;
      c.profile = profile;
      if (!on && !c.fpsBoost) {
         FpsBoost.apply(true);
      }

      if (on && c.fpsBoost) {
         FpsBoost.apply(false);
      }

      NeonLyrics.saveConfig();
      Island.notify(Fx.t(on ? "neonlyrics.profile.chill" : "neonlyrics.profile.pvp"), true);
   }
}
