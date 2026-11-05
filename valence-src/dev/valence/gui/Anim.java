package dev.valence.gui;

import java.util.HashMap;
import java.util.Map;

public final class Anim {
   private static final Map<Long, float[]> VALUES = new HashMap<>();
   private static long lastFrame = System.nanoTime();
   private static float dt = 0.016F;

   private Anim() {
   }

   public static void frame() {
      long now = System.nanoTime();
      dt = Math.min(0.033333335F, (float)(now - lastFrame) / 1.0E9F);
      lastFrame = now;
   }

   public static void restartClock() {
      lastFrame = System.nanoTime();
      dt = 0.016F;
   }

   public static float k(float speed) {
      return 1.0F - (float)Math.exp(-dt * speed);
   }

   public static float dt() {
      return dt;
   }

   public static long key(int group, int index) {
      return (long)group << 32 | index & 4294967295L;
   }

   public static float hover(long key, boolean hovered, boolean sound) {
      float[] v = VALUES.get(key);
      if (v == null) {
         v = new float[]{hovered ? 1.0F : 0.0F, hovered ? 1.0F : 0.0F};
         VALUES.put(key, v);
      } else if (hovered && v[1] < 0.5F && sound) {
         UiSounds.hover();
      }

      v[1] = hovered ? 1.0F : 0.0F;
      float speed = hovered ? 14.0F : 8.0F;
      v[0] += (v[1] - v[0]) * k(speed);
      return v[0];
   }

   public static float hover(long key, boolean hovered) {
      return hover(key, hovered, true);
   }

   public static float approach(long key, float target, float speed) {
      float[] v = VALUES.get(key);
      if (v == null) {
         v = new float[]{target, target};
         VALUES.put(key, v);
      }

      v[0] += (target - v[0]) * k(speed);
      return v[0];
   }

   public static void reset() {
      VALUES.clear();
   }
}
