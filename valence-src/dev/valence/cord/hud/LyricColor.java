package dev.valence.cord.hud;

import dev.valence.cord.config.Config;

public final class LyricColor {
   public static final int[][] GRADIENTS = new int[][]{
      {16747100, 16735400},
      {16765517, 16731469},
      {16743128, 11566335},
      {14272767, 9075967},
      {6080767, 8019199},
      {5111706, 6080767},
      {16777215, 8057087},
      {16773596, 16758861}
   };

   private LyricColor() {
   }

   public static int word(Config c, int base, int i, int n) {
      if (!c.lyricsGradient) {
         return base;
      }

      float t = n <= 1 ? 0.0F : Math.max(0.0F, Math.min(1.0F, (float)i / (n - 1)));
      return Gfx.lerpColor(c.lyricsGradA, c.lyricsGradB, t);
   }

   public static int first(Config c, int base) {
      return c.lyricsGradient ? c.lyricsGradA : base;
   }
}
