package dev.valence.sky.classic;

public final class SkyCtx {
   public double time;
   public float dt;
   public int quality = 1;
   public float brightness = 1.0F;
   public float saturation = 1.0F;
   public float hue = 0.0F;
   public float stars = 1.0F;
   public float particles = 1.0F;
   public float rain;
   public float alpha = 1.0F;
   public boolean shooting = true;
   public boolean lightning = true;
   public boolean constellations = true;

   public int count(int base, float userMul) {
      float q = this.quality == 0 ? 0.45F : (this.quality == 1 ? 0.75F : 1.0F);
      return Math.max(0, Math.round(base * q * userMul));
   }

   public int seg(int low, int mid, int high) {
      return this.quality == 0 ? low : (this.quality == 1 ? mid : high);
   }
}
