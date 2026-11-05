package dev.valence.cord.hud;

public final class SingleWord {
   public static final float IN_MS = 320.0F;
   public static final float OUT_MS = 300.0F;

   private SingleWord() {
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   public static float pop(long at, long now) {
      float t = clamp01((float)(now - at) / 320.0F);
      float c1 = 1.4F;
      float c3 = c1 + 1.0F;
      float u = t - 1.0F;
      return 1.0F + c3 * u * u * u + c1 * u * u;
   }

   public static float ease(long at, long now) {
      float u = 1.0F - clamp01((float)(now - at) / 320.0F);
      return 1.0F - u * u * u;
   }

   public static float fadeIn(long at, long now) {
      return clamp01((float)(now - at) / 176.0F);
   }

   public static float fadeOut(long nextAt, long now) {
      if (nextAt == 0L) {
         return 1.0F;
      }

      float u = clamp01((float)(now - nextAt) / 300.0F);
      return 1.0F - u * u * (3.0F - 2.0F * u);
   }

   private static float hash(int seed, int i) {
      int h = seed * 668265261 ^ i * 374761393;
      h ^= h >>> 15;
      h *= -2048144789;
      h ^= h >>> 13;
      return (h >>> 8) / 1.6777216E7F;
   }

   public static float x(int seed, int i) {
      double ang = i * 2.39996 + seed * 0.37;
      float r = 0.45F + 0.55F * hash(seed, i);
      return (float)Math.cos(ang) * r;
   }

   public static float y(int seed, int i) {
      double ang = i * 2.39996 + seed * 0.37;
      float r = 0.45F + 0.55F * hash(seed, i);
      return (float)Math.sin(ang) * r;
   }

   public static float tilt(int seed, int i) {
      return (hash(seed + 7, i) - 0.5F) * 9.0F;
   }
}
