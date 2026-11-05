package dev.valence.sky.classic;

public final class Curtain {
   public float centerAz;
   public float span;
   public float baseEl;
   public float height;
   public float lean = 4.0F;
   public float wave = 1.0F;
   public float alpha = 0.8F;
   public int bottom;
   public int middle;
   public int top;
   public int seed;
   public float uStart = 0.1F;
   public float uSpan = 0.5F;
   private float[][] rows = new float[0][];
   private int[][] cols = new int[0][];
   private float[] us = new float[0];
   private final float[] tmp = new float[3];
   private static final float[] VS = new float[]{0.95F, 0.56F, 0.0F};

   public Curtain(int seed, float centerAz, float span, float baseEl, float height, int bottom, int middle, int top) {
      this.seed = seed;
      this.centerAz = centerAz;
      this.span = span;
      this.baseEl = baseEl;
      this.height = height;
      this.bottom = bottom;
      this.middle = middle;
      this.top = top;
      this.uStart = 0.05F + 0.25F * G.rnd(seed, 1);
   }

   public Curtain alpha(float a) {
      this.alpha = a;
      return this;
   }

   public Curtain lean(float l) {
      this.lean = l;
      return this;
   }

   public Curtain wave(float w) {
      this.wave = w;
      return this;
   }

   public void draw(G g, float strength) {
      if (!(strength * this.alpha <= 0.01F)) {
         int n = g.ctx.seg(24, 40, 60);
         if (this.rows.length != 3 || this.rows[0].length != n * 3) {
            this.rows = new float[3][n * 3];
            this.cols = new int[3][n];
            this.us = new float[n];
         }

         double t = g.ctx.time;
         float p = this.seed * 1.37F;
         float breathe = 0.78F + 0.22F * G.sin(t * 0.17 + p);
         float drift = 0.12F * G.sin(t * 0.021 + p);

         for (int i = 0; i < n; i++) {
            float s = (float)i / (n - 1);
            float az = this.centerAz
               + this.span * (s - 0.5F)
               + this.wave * (7.0F * G.sin(s * 6.3 + t * 0.09 + p) + 3.0F * G.sin(s * 15.0 - t * 0.21 + p * 2.0F));
            float el = this.baseEl + this.wave * (4.5F * G.sin(s * 4.1 - t * 0.07 + p) + 2.0F * G.sin(s * 11.0 + t * 0.19 + p * 3.0F));
            float h = this.height * (0.72F + 0.28F * G.sin(s * 5.2 + t * 0.13 + p));
            float pulse = 0.5F + 0.5F * G.sin(s * 13.0 - t * 0.85 + p);
            float slow = 0.5F + 0.5F * G.sin(s * 4.7 + t * 0.31 + p * 0.5F);
            float env = G.smooth(0.0F, 0.16F, s) * G.smooth(1.0F, 0.84F, s);
            float k = env * breathe * (0.35F + 0.65F * pulse * (0.4F + 0.6F * slow)) * this.alpha * strength;
            int i3 = i * 3;
            G.dir(az, el - 1.2F, this.tmp);
            this.rows[0][i3] = this.tmp[0];
            this.rows[0][i3 + 1] = this.tmp[1];
            this.rows[0][i3 + 2] = this.tmp[2];
            G.dir(az + this.lean * 0.4F, el + h * 0.4F, this.tmp);
            this.rows[1][i3] = this.tmp[0];
            this.rows[1][i3 + 1] = this.tmp[1];
            this.rows[1][i3 + 2] = this.tmp[2];
            G.dir(az + this.lean, el + h, this.tmp);
            this.rows[2][i3] = this.tmp[0];
            this.rows[2][i3 + 1] = this.tmp[1];
            this.rows[2][i3 + 2] = this.tmp[2];
            this.cols[0][i] = G.fade(this.bottom, k);
            this.cols[1][i] = G.fade(this.middle, k);
            this.cols[2][i] = G.fade(this.top, k);
            this.us[i] = this.uStart + drift + 0.1F + this.uSpan * s * 0.9F;
         }

         g.ribbon(SkyTex.AURORA_BRIGHT, this.rows, this.cols, VS, this.us, n);
      }
   }
}
