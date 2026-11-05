package dev.valence.lyrics;

import dev.valence.config.ValenceConfig;
import java.util.List;

public final class LyricStyles {
   public static final List<LyricStyles.Preset> ALL = List.of(
      new LyricStyles.Preset("lavender", "poppins_bold", -3693825, -1, "gradient", 0.6F, false, -15069136, false, 0.6F),
      new LyricStyles.Preset("dreamy", "dmserif", -608001, -2963201, "gradient", 0.7F, false, -14020544, false, 0.7F),
      new LyricStyles.Preset("fight", "poppins_italic", -3872513, -1, "gradient", 0.45F, true, -15260609, true, 0.9F),
      new LyricStyles.Preset("comic", "bangers", -8090, -38187, "gradient", 0.3F, true, -14020544, true, 0.8F),
      new LyricStyles.Preset("script", "pacifico", -11800, -1, "gradient", 0.55F, false, -12971984, false, 0.6F),
      new LyricStyles.Preset("fire", "luckiest", -8090, -46564, "gradient", 0.55F, true, -12973568, true, 0.7F),
      new LyricStyles.Preset("ice", "lilita", -983553, -8398593, "gradient", 0.65F, false, -16111040, false, 0.6F),
      new LyricStyles.Preset("royal", "abril", -5464, -2842038, "gradient", 0.4F, true, -14018048, false, 0.5F),
      new LyricStyles.Preset("clean", "poppins_bold", -1, -1, "solid", 0.2F, false, -16777216, false, 0.3F)
   );

   private LyricStyles() {
   }

   public static LyricStyles.Preset byId(String id) {
      for (LyricStyles.Preset p : ALL) {
         if (p.id().equals(id)) {
            return p;
         }
      }

      return null;
   }

   public record Preset(
      String id, String font, int colorA, int colorB, String colorMode, float glow, boolean outline, int outlineColor, boolean uppercase, float tilt
   ) {
      public void apply() {
         ValenceConfig c = ValenceConfig.get();
         c.lyricsPreset = this.id;
         c.lyricsFont = this.font;
         c.lyricsColorA = this.colorA;
         c.lyricsColorB = this.colorB;
         c.lyricsColorMode = this.colorMode;
         c.lyricsOutline = this.outline;
         c.lyricsOutlineColor = this.outlineColor;
         c.lyricsUppercase = this.uppercase;
         c.lyricsTilt = this.tilt;
         ValenceConfig.save();
      }
   }
}
