package dev.valence.sky.classic;

public final class StarField {
   private final int n;
   private final float[] x;
   private final float[] y;
   private final float[] z;
   private final float[] size;
   private final float[] bright;
   private final float[] phase;
   private final float[] speed;
   private final int[] tint;
   private final float[] ex;
   private final float[] ey;
   private final float[] ez;
   private final float[] ux;
   private final float[] uy;
   private final float[] uz;

   public StarField(int seed, int count, float minEl) {
      this.n = count;
      this.x = new float[this.n];
      this.y = new float[this.n];
      this.z = new float[this.n];
      this.size = new float[this.n];
      this.bright = new float[this.n];
      this.phase = new float[this.n];
      this.speed = new float[this.n];
      this.tint = new int[this.n];
      this.ex = new float[this.n];
      this.ey = new float[this.n];
      this.ez = new float[this.n];
      this.ux = new float[this.n];
      this.uy = new float[this.n];
      this.uz = new float[this.n];
      float sMin = G.sin(Math.toRadians(minEl));

      for (int i = 0; i < this.n; i++) {
         float h = sMin + (1.0F - sMin) * G.rnd(seed, i * 7);
         float a = (float)((Math.PI * 2) * G.rnd(seed, i * 7 + 1));
         float r = (float)Math.sqrt(Math.max(0.0F, 1.0F - h * h));
         this.x[i] = r * G.sin(a);
         this.y[i] = h;
         this.z[i] = -r * G.cos(a);
         float k = G.rnd(seed, i * 7 + 2);
         this.bright[i] = 0.25F + 0.75F * k * k * k;
         this.size[i] = 0.16F + 0.34F * this.bright[i] + 0.08F * G.rnd(seed, i * 7 + 3);
         this.phase[i] = (float)((Math.PI * 2) * G.rnd(seed, i * 7 + 4));
         this.speed[i] = 0.6F + 2.2F * G.rnd(seed, i * 7 + 5);
         float c = G.rnd(seed, i * 7 + 6);
         this.tint[i] = c < 0.18F ? 13228799 : (c < 0.3F ? 16771020 : (c < 0.36F ? 14732543 : 16777215));
         float bx = -this.z[i];
         float bz = this.x[i];
         float bl = (float)Math.sqrt(bx * bx + bz * bz);
         if (bl < 1.0E-4F) {
            bx = 1.0F;
            bz = 0.0F;
            bl = 1.0F;
         }

         this.ex[i] = bx / bl;
         this.ey[i] = 0.0F;
         this.ez[i] = bz / bl;
         this.ux[i] = -this.ez[i] * this.y[i];
         this.uy[i] = this.ez[i] * this.x[i] - this.ex[i] * this.z[i];
         this.uz[i] = this.ex[i] * this.y[i];
      }
   }

   public int size() {
      return this.n;
   }

   public void draw(G g, int count, float alpha, float sizeMul, float twinkle, int tintColor, float tintMix) {
      if (!(alpha <= 0.01F)) {
         double t = g.ctx.time;
         count = Math.min(count, this.n);
         float k = sizeMul * 0.5F * (float) (Math.PI / 180.0);

         for (int i = 0; i < count; i++) {
            float tw = 1.0F - twinkle * 0.5F * (1.0F + G.sin(t * this.speed[i] + this.phase[i]));
            float hor = G.smooth(-0.02F, 0.12F, this.y[i]);
            float a = alpha * this.bright[i] * tw * hor;
            if (!(a < 0.02F)) {
               int rgb = tintMix > 0.0F ? G.mix(this.tint[i] | 0xFF000000, tintColor | 0xFF000000, tintMix) : this.tint[i];
               float h = this.size[i] * k;
               g.spriteFast(
                  SkyTex.DOT,
                  this.x[i],
                  this.y[i],
                  this.z[i],
                  this.ex[i] * h,
                  this.ey[i] * h,
                  this.ez[i] * h,
                  this.ux[i] * h,
                  this.uy[i] * h,
                  this.uz[i] * h,
                  G.argb(Math.round(G.clamp(a, 0.0F, 1.0F) * 255.0F), rgb)
               );
            }
         }
      }
   }

   public void drawSparkles(G g, int count, float alpha, float sizeMul, int color) {
      double t = g.ctx.time;
      count = Math.min(count, this.n);

      for (int i = 0; i < count; i++) {
         float pulse = 0.5F + 0.5F * G.sin(t * this.speed[i] * 0.7F + this.phase[i]);
         float hor = G.smooth(-0.02F, 0.12F, this.y[i]);
         float a = alpha * (0.35F + 0.65F * pulse) * hor;
         if (!(a < 0.02F)) {
            float s = (1.1F + 1.6F * this.bright[i]) * sizeMul * (0.85F + 0.3F * pulse);
            g.sprite(
               SkyTex.STAR, this.x[i], this.y[i], this.z[i], s, s, (float)(t * 4.0 + this.phase[i] * 10.0F) % 90.0F, G.fade(color, a), 0.0F, 0.0F, 1.0F, 1.0F
            );
         }
      }
   }
}
