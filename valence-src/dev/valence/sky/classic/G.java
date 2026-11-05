package dev.valence.sky.classic;

public final class G {
   public static final float R = 12.0F;
   private static final float D2R = (float) (Math.PI / 180.0);
   private SkyCanvas canvas;
   public SkyCtx ctx;
   private SkyTex current;
   private float bright = 1.0F;
   private float sat = 1.0F;
   private float alpha = 1.0F;
   private float hueCos = 1.0F;
   private float hueSin = 0.0F;
   private final float[] m = new float[9];
   private final float[][] stack = new float[16][9];
   private int sp;
   private final float[] qp = new float[12];
   private final float[] quv = new float[8];
   private final int[] qc = new int[4];
   private final float[] ta = new float[3];
   private final float[] tb = new float[3];
   private final float[] tcc = new float[3];
   private float[] gx = new float[0];
   private float[] gy = new float[0];
   private float[] gz = new float[0];
   private float[] gu = new float[0];
   private float[] gv = new float[0];
   private int[] gc = new int[0];
   private float[] t1 = new float[0];
   private float[] t2 = new float[0];
   private float[] t3 = new float[0];
   private float[] t4 = new float[0];
   private static final int SIN_N = 4096;
   private static final float[] SIN = new float[4097];
   public int quads;
   public int flips;
   private static final float[] DOME_ROWS;

   private void ensure(int n) {
      if (this.gx.length < n) {
         this.gx = new float[n];
         this.gy = new float[n];
         this.gz = new float[n];
         this.gu = new float[n];
         this.gv = new float[n];
         this.gc = new int[n];
      }
   }

   private void ensureT(int n) {
      if (this.t1.length < n) {
         this.t1 = new float[n];
         this.t2 = new float[n];
         this.t3 = new float[n];
         this.t4 = new float[n];
      }
   }

   private void cell(int cols, int i, int j, boolean reverse) {
      int a = j * cols + i;
      int b = a + 1;
      int c = a + cols + 1;
      int d = a + cols;
      if (reverse) {
         int tmp = b;
         b = d;
         d = tmp;
      }

      this.set(0, this.gx[a], this.gy[a], this.gz[a], this.gu[a], this.gv[a], this.gc[a]);
      this.set(1, this.gx[b], this.gy[b], this.gz[b], this.gu[b], this.gv[b], this.gc[b]);
      this.set(2, this.gx[c], this.gy[c], this.gz[c], this.gu[c], this.gv[c], this.gc[c]);
      this.set(3, this.gx[d], this.gy[d], this.gz[d], this.gu[d], this.gv[d], this.gc[d]);
      this.emitFast();
   }

   public static float sin(double rad) {
      double f = rad * 651.8986469044033;
      double fl = Math.floor(f);
      int i = (int)(fl - Math.floor(fl / 4096.0) * 4096.0);
      float k = (float)(f - fl);
      return SIN[i] + (SIN[i + 1] - SIN[i]) * k;
   }

   public static float cos(double rad) {
      return sin(rad + (Math.PI / 2));
   }

   public void begin(SkyCanvas canvas, SkyCtx ctx) {
      this.canvas = canvas;
      this.ctx = ctx;
      this.current = null;
      this.bright = ctx.brightness;
      this.sat = ctx.saturation;
      this.alpha = ctx.alpha;
      this.hueCos = (float)Math.cos(Math.toRadians(ctx.hue));
      this.hueSin = (float)Math.sin(Math.toRadians(ctx.hue));
      identity(this.m);
      this.sp = 0;
   }

   public void setAlpha(float a) {
      this.alpha = a;
   }

   public static float rad(float deg) {
      return deg * (float) (Math.PI / 180.0);
   }

   public static float clamp(float v, float lo, float hi) {
      return v < lo ? lo : (v > hi ? hi : v);
   }

   public static float smooth(float e0, float e1, float x) {
      float t = clamp((x - e0) / (e1 - e0), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   public static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   public static float fract(double v) {
      return (float)(v - Math.floor(v));
   }

   public static float rnd(int seed, int i) {
      int h = seed * 668265261 ^ i * 374761393;
      h ^= h >>> 15;
      h *= -2048144789;
      h ^= h >>> 13;
      h *= -1028477387;
      h ^= h >>> 16;
      return (h >>> 8) / 1.6777216E7F;
   }

   public static float noise(int seed, double x) {
      double fl = Math.floor(x);
      int i = (int)fl;
      float f = (float)(x - fl);
      float u = f * f * (3.0F - 2.0F * f);
      float a = rnd(seed, i) * 2.0F - 1.0F;
      float b = rnd(seed, i + 1) * 2.0F - 1.0F;
      return a + (b - a) * u;
   }

   public static void dir(float azDeg, float elDeg, float[] out) {
      float az = azDeg * (float) (Math.PI / 180.0);
      float el = elDeg * (float) (Math.PI / 180.0);
      float ce = cos(el);
      out[0] = ce * sin(az);
      out[1] = sin(el);
      out[2] = -ce * cos(az);
   }

   public static int argb(int a, int rgb) {
      return a << 24 | rgb & 16777215;
   }

   public static int mix(int c1, int c2, float t) {
      t = clamp(t, 0.0F, 1.0F);
      int a1 = c1 >>> 24;
      int r1 = c1 >> 16 & 0xFF;
      int g1 = c1 >> 8 & 0xFF;
      int b1 = c1 & 0xFF;
      int a2 = c2 >>> 24;
      int r2 = c2 >> 16 & 0xFF;
      int g2 = c2 >> 8 & 0xFF;
      int b2 = c2 & 0xFF;
      return (int)(a1 + (a2 - a1) * t) << 24 | (int)(r1 + (r2 - r1) * t) << 16 | (int)(g1 + (g2 - g1) * t) << 8 | (int)(b1 + (b2 - b1) * t);
   }

   public static int fade(int c, float a) {
      int al = Math.round((c >>> 24) * clamp(a, 0.0F, 1.0F));
      return al << 24 | c & 16777215;
   }

   private static void identity(float[] a) {
      a[0] = 1.0F;
      a[1] = 0.0F;
      a[2] = 0.0F;
      a[3] = 0.0F;
      a[4] = 1.0F;
      a[5] = 0.0F;
      a[6] = 0.0F;
      a[7] = 0.0F;
      a[8] = 1.0F;
   }

   public void push() {
      System.arraycopy(this.m, 0, this.stack[this.sp++], 0, 9);
   }

   public void pop() {
      System.arraycopy(this.stack[--this.sp], 0, this.m, 0, 9);
   }

   private void mul(float r0, float r1, float r2, float r3, float r4, float r5, float r6, float r7, float r8) {
      float a0 = this.m[0] * r0 + this.m[1] * r3 + this.m[2] * r6;
      float a1 = this.m[0] * r1 + this.m[1] * r4 + this.m[2] * r7;
      float a2 = this.m[0] * r2 + this.m[1] * r5 + this.m[2] * r8;
      float a3 = this.m[3] * r0 + this.m[4] * r3 + this.m[5] * r6;
      float a4 = this.m[3] * r1 + this.m[4] * r4 + this.m[5] * r7;
      float a5 = this.m[3] * r2 + this.m[4] * r5 + this.m[5] * r8;
      float a6 = this.m[6] * r0 + this.m[7] * r3 + this.m[8] * r6;
      float a7 = this.m[6] * r1 + this.m[7] * r4 + this.m[8] * r7;
      float a8 = this.m[6] * r2 + this.m[7] * r5 + this.m[8] * r8;
      this.m[0] = a0;
      this.m[1] = a1;
      this.m[2] = a2;
      this.m[3] = a3;
      this.m[4] = a4;
      this.m[5] = a5;
      this.m[6] = a6;
      this.m[7] = a7;
      this.m[8] = a8;
   }

   public void rotY(float deg) {
      float c = (float)Math.cos(deg * (float) (Math.PI / 180.0));
      float s = (float)Math.sin(deg * (float) (Math.PI / 180.0));
      this.mul(c, 0.0F, -s, 0.0F, 1.0F, 0.0F, s, 0.0F, c);
   }

   public void rotX(float deg) {
      float c = (float)Math.cos(deg * (float) (Math.PI / 180.0));
      float s = (float)Math.sin(deg * (float) (Math.PI / 180.0));
      this.mul(1.0F, 0.0F, 0.0F, 0.0F, c, -s, 0.0F, s, c);
   }

   public void rotZ(float deg) {
      float c = (float)Math.cos(deg * (float) (Math.PI / 180.0));
      float s = (float)Math.sin(deg * (float) (Math.PI / 180.0));
      this.mul(c, -s, 0.0F, s, c, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public int col(int c) {
      float a = (c >>> 24) / 255.0F * this.alpha;
      float r = (c >> 16 & 0xFF) / 255.0F;
      float gg = (c >> 8 & 0xFF) / 255.0F;
      float b = (c & 0xFF) / 255.0F;
      if (this.hueSin != 0.0F) {
         float yy = 0.299F * r + 0.587F * gg + 0.114F * b;
         float ii = 0.596F * r - 0.274F * gg - 0.322F * b;
         float qq = 0.211F * r - 0.523F * gg + 0.312F * b;
         float i2 = ii * this.hueCos - qq * this.hueSin;
         float q2 = ii * this.hueSin + qq * this.hueCos;
         r = yy + 0.956F * i2 + 0.621F * q2;
         gg = yy - 0.272F * i2 - 0.647F * q2;
         b = yy - 1.106F * i2 + 1.703F * q2;
      }

      if (this.sat != 1.0F) {
         float l = 0.299F * r + 0.587F * gg + 0.114F * b;
         r = l + (r - l) * this.sat;
         gg = l + (gg - l) * this.sat;
         b = l + (b - l) * this.sat;
      }

      r *= this.bright;
      gg *= this.bright;
      b *= this.bright;
      return (int)(clamp(a, 0.0F, 1.0F) * 255.0F + 0.5F) << 24
         | (int)(clamp(r, 0.0F, 1.0F) * 255.0F + 0.5F) << 16
         | (int)(clamp(gg, 0.0F, 1.0F) * 255.0F + 0.5F) << 8
         | (int)(clamp(b, 0.0F, 1.0F) * 255.0F + 0.5F);
   }

   public void layer(SkyTex t) {
      this.canvas.layer(t);
      this.current = t;
   }

   private void use(SkyTex t) {
      if (this.current != t) {
         this.layer(t);
      }
   }

   private void set(int i, float x, float y, float z, float u, float v, int c) {
      this.qp[i * 3] = this.m[0] * x + this.m[1] * y + this.m[2] * z;
      this.qp[i * 3 + 1] = this.m[3] * x + this.m[4] * y + this.m[5] * z;
      this.qp[i * 3 + 2] = this.m[6] * x + this.m[7] * y + this.m[8] * z;
      this.quv[i * 2] = u;
      this.quv[i * 2 + 1] = v;
      this.qc[i] = c;
   }

   private void emitFast() {
      if ((this.qc[0] | this.qc[1] | this.qc[2] | this.qc[3]) >>> 24 != 0) {
         for (int i = 0; i < 4; i++) {
            this.canvas
               .vertex(this.qp[i * 3] * 12.0F, this.qp[i * 3 + 1] * 12.0F, this.qp[i * 3 + 2] * 12.0F, this.quv[i * 2], this.quv[i * 2 + 1], this.qc[i]);
         }

         this.quads++;
      }
   }

   private void emit() {
      if ((this.qc[0] | this.qc[1] | this.qc[2] | this.qc[3]) >>> 24 != 0) {
         float ax = this.qp[6] - this.qp[0];
         float ay = this.qp[7] - this.qp[1];
         float az = this.qp[8] - this.qp[2];
         float bx = this.qp[9] - this.qp[3];
         float by = this.qp[10] - this.qp[4];
         float bz = this.qp[11] - this.qp[5];
         float nx = ay * bz - az * by;
         float ny = az * bx - ax * bz;
         float nz = ax * by - ay * bx;
         float cx = this.qp[0] + this.qp[3] + this.qp[6] + this.qp[9];
         float cy = this.qp[1] + this.qp[4] + this.qp[7] + this.qp[10];
         float cz = this.qp[2] + this.qp[5] + this.qp[8] + this.qp[11];
         boolean flip = nx * cx + ny * cy + nz * cz > 0.0F;
         if (flip) {
            this.flips++;
         }

         for (int k = 0; k < 4; k++) {
            int i = flip ? 4 - k & 3 : k;
            this.canvas
               .vertex(this.qp[i * 3] * 12.0F, this.qp[i * 3 + 1] * 12.0F, this.qp[i * 3 + 2] * 12.0F, this.quv[i * 2], this.quv[i * 2 + 1], this.qc[i]);
         }

         this.quads++;
      }
   }

   public static int gradient(float el, float[] els, int[] cols) {
      if (el <= els[0]) {
         return cols[0];
      }

      for (int i = 1; i < els.length; i++) {
         if (el <= els[i]) {
            float t = (el - els[i - 1]) / (els[i] - els[i - 1]);
            return mix(cols[i - 1], cols[i], t * t * (3.0F - 2.0F * t));
         }
      }

      return cols[cols.length - 1];
   }

   public void dome(float[] els, int[] cols) {
      this.layer(SkyTex.WHITE);
      int nAz = this.ctx.seg(12, 18, 24);
      int rows = DOME_ROWS.length;
      int w = nAz + 1;
      this.ensure(rows * w);

      for (int r = 0; r < rows; r++) {
         float el = DOME_ROWS[r] * (float) (Math.PI / 180.0);
         float ce = cos(el);
         float se = sin(el);
         int c = this.col(gradient(DOME_ROWS[r], els, cols) | 0xFF000000);

         for (int i = 0; i < w; i++) {
            float az = (float)((Math.PI * 2) * i / nAz);
            int k = r * w + i;
            this.gx[k] = ce * sin(az);
            this.gy[k] = se;
            this.gz[k] = -ce * cos(az);
            this.gu[k] = 0.5F;
            this.gv[k] = 0.5F;
            this.gc[k] = c;
         }
      }

      for (int r = 0; r < rows - 1; r++) {
         for (int i = 0; i < nAz; i++) {
            this.cell(w, i, r, false);
         }
      }
   }

   public void horizonFade(int rgb, float elFull, float elTop) {
      this.use(SkyTex.WHITE);
      float[] rowsEl = new float[]{
         -90.0F, -40.0F, -10.0F, elFull, lerp(elFull, elTop, 0.2F), lerp(elFull, elTop, 0.4F), lerp(elFull, elTop, 0.6F), lerp(elFull, elTop, 0.8F), elTop
      };
      int nAz = 24;
      int rows = rowsEl.length;
      int w = nAz + 1;
      this.ensure(rows * w);

      for (int r = 0; r < rows; r++) {
         float el = rowsEl[r];
         float a = 1.0F - smooth(elFull, elTop, el);
         int c = this.col((Math.round(a * 255.0F) & 0xFF) << 24 | rgb & 16777215);
         float er = el * (float) (Math.PI / 180.0);
         float ce = cos(er);
         float se = sin(er);

         for (int i = 0; i < w; i++) {
            float az = (float)((Math.PI * 2) * i / nAz);
            int k = r * w + i;
            this.gx[k] = ce * sin(az);
            this.gy[k] = se;
            this.gz[k] = -ce * cos(az);
            this.gu[k] = 0.5F;
            this.gv[k] = 0.5F;
            this.gc[k] = c;
         }
      }

      for (int r = 0; r < rows - 1; r++) {
         for (int i = 0; i < nAz; i++) {
            this.cell(w, i, r, false);
         }
      }
   }

   public static int bright(int rgb, float brightness) {
      int r = Math.min(255, Math.round((rgb >> 16 & 0xFF) * brightness));
      int gg = Math.min(255, Math.round((rgb >> 8 & 0xFF) * brightness));
      int b = Math.min(255, Math.round((rgb & 0xFF) * brightness));
      return rgb & 0xFF000000 | r << 16 | gg << 8 | b;
   }

   public void horizonGlow(float centerAz, float widthDeg, float el0, float el1, int color) {
      this.use(SkyTex.WHITE);
      int n = this.ctx.seg(10, 16, 24);
      float a0 = centerAz - widthDeg / 2.0F;
      float step = widthDeg / n;
      int rows = 4;

      for (int i = 0; i < n; i++) {
         float s0 = (float)i / n;
         float s1 = (float)(i + 1) / n;
         float w0 = widthDeg >= 359.0F ? 1.0F : (float)Math.pow(Math.sin(Math.PI * s0), 1.5);
         float w1 = widthDeg >= 359.0F ? 1.0F : (float)Math.pow(Math.sin(Math.PI * s1), 1.5);

         for (int r = 0; r < rows; r++) {
            float t0 = (float)r / rows;
            float t1 = (float)(r + 1) / rows;
            float e0 = lerp(el0, el1, t0);
            float e1 = lerp(el0, el1, t1);
            float f0 = profile(t0);
            float f1 = profile(t1);
            dir(a0 + step * i, e0, this.ta);
            this.set(0, this.ta[0], this.ta[1], this.ta[2], 0.5F, 0.5F, this.col(fade(color, w0 * f0)));
            dir(a0 + step * (i + 1), e0, this.ta);
            this.set(1, this.ta[0], this.ta[1], this.ta[2], 0.5F, 0.5F, this.col(fade(color, w1 * f0)));
            dir(a0 + step * (i + 1), e1, this.ta);
            this.set(2, this.ta[0], this.ta[1], this.ta[2], 0.5F, 0.5F, this.col(fade(color, w1 * f1)));
            dir(a0 + step * i, e1, this.ta);
            this.set(3, this.ta[0], this.ta[1], this.ta[2], 0.5F, 0.5F, this.col(fade(color, w0 * f1)));
            this.emit();
         }
      }
   }

   private static float profile(float t) {
      return smooth(0.0F, 0.2F, t) * (1.0F - smooth(0.2F, 1.0F, t));
   }

   public void band(
      SkyTex t, float az0, float az1, float el0, float el1, float u0, float u1, float v0, float v1, int color, float fadeLow, float fadeHigh, float fadeSide
   ) {
      this.use(t);
      float span = az1 - az0;
      int nAz = Math.max(2, (int)Math.ceil(Math.abs(span) / (this.ctx.quality == 0 ? 15.0F : (this.ctx.quality == 1 ? 10.0F : 7.5F))));
      int nEl = Math.max(2, (int)Math.ceil(Math.abs(el1 - el0) / this.ctx.seg(10, 7, 5)));
      int w = nAz + 1;
      this.ensure(w * (nEl + 1));
      this.ensureT(w);

      for (int i = 0; i < w; i++) {
         float sx = (float)i / nAz;
         float az = (az0 + span * sx) * (float) (Math.PI / 180.0);
         this.t1[i] = sin(az);
         this.t2[i] = cos(az);
         this.t3[i] = edge(sx, fadeSide, fadeSide);
         this.t4[i] = lerp(u0, u1, sx);
      }

      for (int j = 0; j <= nEl; j++) {
         float ty = (float)j / nEl;
         float el = lerp(el0, el1, ty) * (float) (Math.PI / 180.0);
         float ce = cos(el);
         float se = sin(el);
         float vv = lerp(v1, v0, ty);
         float fa = edge(ty, fadeLow, fadeHigh);
         int base = this.col(color);
         int ba = base >>> 24;

         for (int i = 0; i < w; i++) {
            int k = j * w + i;
            this.gx[k] = ce * this.t1[i];
            this.gy[k] = se;
            this.gz[k] = -ce * this.t2[i];
            this.gu[k] = this.t4[i];
            this.gv[k] = vv;
            this.gc[k] = Math.round(ba * fa * this.t3[i]) << 24 | base & 16777215;
         }
      }

      for (int j = 0; j < nEl; j++) {
         for (int i = 0; i < nAz; i++) {
            this.cell(w, i, j, false);
         }
      }
   }

   private static float edge(float t, float low, float high) {
      float a = low > 0.0F ? smooth(0.0F, low, t) : 1.0F;
      float b = high > 0.0F ? smooth(1.0F, 1.0F - high, t) : 1.0F;
      return a * b;
   }

   public void ring(SkyTex t, float offsetDeg, float el0, float el1, float v0, float v1, int color, float fadeLow, float fadeHigh) {
      float off = (offsetDeg % 360.0F + 360.0F) % 360.0F;
      float uCut = off / 360.0F;
      float split = 360.0F - off;
      if (split > 0.01F) {
         this.band(t, 0.0F, split, el0, el1, uCut, 1.0F, v0, v1, color, fadeLow, fadeHigh, 0.0F);
      }

      if (off > 0.01F) {
         this.band(t, split, 360.0F, el0, el1, 0.0F, uCut, v0, v1, color, fadeLow, fadeHigh, 0.0F);
      }
   }

   public void hline(float az0, float az1, float el, float thickDeg, int color) {
      this.use(SkyTex.LINE);
      float span = az1 - az0;
      boolean full = Math.abs(span) >= 359.0F;
      int n = Math.max(2, (int)Math.ceil(Math.abs(span) / (this.ctx.quality == 0 ? 15.0F : 10.0F)));
      float e0 = (el - thickDeg / 2.0F) * (float) (Math.PI / 180.0);
      float e1 = (el + thickDeg / 2.0F) * (float) (Math.PI / 180.0);
      float c0 = cos(e0);
      float s0 = sin(e0);
      float c1 = cos(e1);
      float s1 = sin(e1);
      int w = n + 1;
      this.ensure(w * 2);
      int base = this.col(color);
      int ba = base >>> 24;

      for (int i = 0; i < w; i++) {
         float sx = (float)i / n;
         float az = (az0 + span * sx) * (float) (Math.PI / 180.0);
         float sa = sin(az);
         float ca = cos(az);
         int c = full ? base : Math.round(ba * edge(sx, 0.2F, 0.2F)) << 24 | base & 16777215;
         this.gx[i] = c0 * sa;
         this.gy[i] = s0;
         this.gz[i] = -c0 * ca;
         this.gu[i] = 0.0F;
         this.gv[i] = sx;
         this.gc[i] = c;
         this.gx[w + i] = c1 * sa;
         this.gy[w + i] = s1;
         this.gz[w + i] = -c1 * ca;
         this.gu[w + i] = 1.0F;
         this.gv[w + i] = sx;
         this.gc[w + i] = c;
      }

      for (int i = 0; i < n; i++) {
         this.cell(w, i, 0, false);
      }
   }

   public void cap(SkyTex t, float azDeg, float elDeg, float radiusDeg, float rotDeg, float zoom, int color, float fadeFrom) {
      this.use(t);
      dir(azDeg, elDeg, this.tcc);
      float px = this.tcc[0];
      float py = this.tcc[1];
      float pz = this.tcc[2];
      float ex = -pz;
      float ez = px;
      float el = (float)Math.sqrt(ex * ex + px * px);
      if (el < 1.0E-4F) {
         ex = 1.0F;
         ez = 0.0F;
         el = 1.0F;
      }

      ex /= el;
      ez /= el;
      float ux = -ez * py;
      float uy = ez * px - ex * pz;
      float uz = ex * py;
      int rings = this.ctx.seg(5, 8, 10);
      int sectors = this.ctx.seg(16, 24, 32);
      float rho = radiusDeg * (float) (Math.PI / 180.0);
      float tr = (float)Math.tan(rho / 2.0F);
      float rot = rotDeg * (float) (Math.PI / 180.0);
      int w = sectors + 1;
      this.ensure(w * (rings + 1));
      this.ensureT(w);

      for (int j = 0; j < w; j++) {
         double th = (Math.PI * 2) * j / sectors;
         this.t1[j] = (float)Math.cos(th);
         this.t2[j] = (float)Math.sin(th);
         this.t3[j] = (float)Math.cos(th + rot);
         this.t4[j] = (float)Math.sin(th + rot);
      }

      int base = this.col(color);
      int ba = base >>> 24;

      for (int i = 0; i <= rings; i++) {
         float al = rho * i / rings;
         float k = (float)Math.tan(al / 2.0F) / tr / zoom;
         float f = 1.0F - smooth(fadeFrom, 1.0F, (float)i / rings);
         float sa = (float)Math.sin(al);
         float ca = (float)Math.cos(al);
         int c = Math.round(ba * f) << 24 | base & 16777215;

         for (int j = 0; j < w; j++) {
            int q = i * w + j;
            float ct = this.t1[j];
            float st = this.t2[j];
            this.gx[q] = px * ca + (ex * ct + ux * st) * sa;
            this.gy[q] = py * ca + uy * st * sa;
            this.gz[q] = pz * ca + (ez * ct + uz * st) * sa;
            this.gu[q] = 0.5F + 0.5F * k * this.t3[j];
            this.gv[q] = 0.5F - 0.5F * k * this.t4[j];
            this.gc[q] = c;
         }
      }

      for (int i = 0; i < rings; i++) {
         for (int j = 0; j < sectors; j++) {
            this.cell(w, j, i, true);
         }
      }
   }

   public void sprite(SkyTex t, float azDeg, float elDeg, float sizeDeg, float rotDeg, int color) {
      dir(azDeg, elDeg, this.tcc);
      this.sprite(t, this.tcc[0], this.tcc[1], this.tcc[2], sizeDeg, sizeDeg, rotDeg, color, 0.0F, 0.0F, 1.0F, 1.0F);
   }

   public void sprite(SkyTex t, float dx, float dy, float dz, float wDeg, float hDeg, float rotDeg, int color, float u0, float v0, float u1, float v1) {
      this.use(t);
      int c = this.col(color);
      if (c >>> 24 != 0) {
         float ex = -dz;
         float ez = dx;
         float el = (float)Math.sqrt(ex * ex + dx * dx);
         if (el < 1.0E-4F) {
            ex = 1.0F;
            ez = 0.0F;
            el = 1.0F;
         }

         ex /= el;
         ez /= el;
         float ux = -ez * dy;
         float uy = ez * dx - ex * dz;
         float uz = ex * dy;
         float hw = (float)Math.tan(wDeg * (float) (Math.PI / 180.0) / 2.0F);
         float hh = (float)Math.tan(hDeg * (float) (Math.PI / 180.0) / 2.0F);
         float cr = (float)Math.cos(rotDeg * (float) (Math.PI / 180.0));
         float sr = (float)Math.sin(rotDeg * (float) (Math.PI / 180.0));
         float rx = ex * cr + ux * sr;
         float ry = uy * sr;
         float rz = ez * cr + uz * sr;
         float qx = ux * cr - ex * sr;
         float qy = uy * cr;
         float qz = uz * cr - ez * sr;
         rx *= hw;
         ry *= hw;
         rz *= hw;
         qx *= hh;
         qy *= hh;
         qz *= hh;
         this.set(0, dx - rx - qx, dy - ry - qy, dz - rz - qz, u0, v1, c);
         this.set(1, dx + rx - qx, dy + ry - qy, dz + rz - qz, u1, v1, c);
         this.set(2, dx + rx + qx, dy + ry + qy, dz + rz + qz, u1, v0, c);
         this.set(3, dx - rx + qx, dy - ry + qy, dz - rz + qz, u0, v0, c);
         this.emit();
      }
   }

   public void spriteFast(SkyTex t, float dx, float dy, float dz, float rx, float ry, float rz, float qx, float qy, float qz, int color) {
      this.use(t);
      int c = this.col(color);
      if (c >>> 24 != 0) {
         this.set(0, dx - rx - qx, dy - ry - qy, dz - rz - qz, 0.0F, 1.0F, c);
         this.set(1, dx + rx - qx, dy + ry - qy, dz + rz - qz, 1.0F, 1.0F, c);
         this.set(2, dx + rx + qx, dy + ry + qy, dz + rz + qz, 1.0F, 0.0F, c);
         this.set(3, dx - rx + qx, dy - ry + qy, dz - rz + qz, 0.0F, 0.0F, c);
         this.emitFast();
      }
   }

   public void plane(
      SkyTex t, float cx, float cy, float cz, float ax, float ay, float az, float bx, float by, float bz, float u0, float v0, float u1, float v1, int color
   ) {
      this.use(t);
      int c = this.col(color);
      this.set(0, cx - ax - bx, cy - ay - by, cz - az - bz, u0, v1, c);
      this.set(1, cx + ax - bx, cy + ay - by, cz + az - bz, u1, v1, c);
      this.set(2, cx + ax + bx, cy + ay + by, cz + az + bz, u1, v0, c);
      this.set(3, cx - ax + bx, cy - ay + by, cz - az + bz, u0, v0, c);
      this.emit();
   }

   public void segment(SkyTex t, float[] a, float[] b, float wA, float wB, int colA, int colB, boolean streak) {
      this.use(t);
      float tx = b[0] - a[0];
      float ty = b[1] - a[1];
      float tz = b[2] - a[2];
      float sax = ty * a[2] - tz * a[1];
      float say = tz * a[0] - tx * a[2];
      float saz = tx * a[1] - ty * a[0];
      float sbx = ty * b[2] - tz * b[1];
      float sby = tz * b[0] - tx * b[2];
      float sbz = tx * b[1] - ty * b[0];
      float la = (float)Math.sqrt(sax * sax + say * say + saz * saz);
      float lb = (float)Math.sqrt(sbx * sbx + sby * sby + sbz * sbz);
      if (!(la < 1.0E-6F) && !(lb < 1.0E-6F)) {
         float ha = (float)Math.tan(wA * (float) (Math.PI / 180.0) / 2.0F) / la;
         float hb = (float)Math.tan(wB * (float) (Math.PI / 180.0) / 2.0F) / lb;
         sax *= ha;
         say *= ha;
         saz *= ha;
         sbx *= hb;
         sby *= hb;
         sbz *= hb;
         int ca = this.col(colA);
         int cb = this.col(colB);
         if (streak) {
            this.set(0, a[0] - sax, a[1] - say, a[2] - saz, 0.0F, 1.0F, ca);
            this.set(1, b[0] - sbx, b[1] - sby, b[2] - sbz, 1.0F, 1.0F, cb);
            this.set(2, b[0] + sbx, b[1] + sby, b[2] + sbz, 1.0F, 0.0F, cb);
            this.set(3, a[0] + sax, a[1] + say, a[2] + saz, 0.0F, 0.0F, ca);
         } else {
            this.set(0, a[0] - sax, a[1] - say, a[2] - saz, 0.0F, 0.0F, ca);
            this.set(1, b[0] - sbx, b[1] - sby, b[2] - sbz, 0.0F, 1.0F, cb);
            this.set(2, b[0] + sbx, b[1] + sby, b[2] + sbz, 1.0F, 1.0F, cb);
            this.set(3, a[0] + sax, a[1] + say, a[2] + saz, 1.0F, 0.0F, ca);
         }

         this.emit();
      }
   }

   public void ribbon(SkyTex t, float[][] rows, int[][] cols, float[] vs, float[] us, int n) {
      this.use(t);

      for (int r = 0; r < rows.length - 1; r++) {
         float[] lo = rows[r];
         float[] hi = rows[r + 1];
         int[] cl = cols[r];
         int[] ch = cols[r + 1];

         for (int i = 0; i < n - 1; i++) {
            int ia = i * 3;
            int ib = (i + 1) * 3;
            this.set(0, lo[ia], lo[ia + 1], lo[ia + 2], us[i], vs[r], this.col(cl[i]));
            this.set(1, lo[ib], lo[ib + 1], lo[ib + 2], us[i + 1], vs[r], this.col(cl[i + 1]));
            this.set(2, hi[ib], hi[ib + 1], hi[ib + 2], us[i + 1], vs[r + 1], this.col(ch[i + 1]));
            this.set(3, hi[ia], hi[ia + 1], hi[ia + 2], us[i], vs[r + 1], this.col(ch[i]));
            this.emit();
         }
      }
   }

   public static void towards(float[] d, float[] target, float deg, float[] out) {
      float dot = d[0] * target[0] + d[1] * target[1] + d[2] * target[2];
      float px = target[0] - d[0] * dot;
      float py = target[1] - d[1] * dot;
      float pz = target[2] - d[2] * dot;
      float l = (float)Math.sqrt(px * px + py * py + pz * pz);
      if (l < 1.0E-6F) {
         out[0] = d[0];
         out[1] = d[1];
         out[2] = d[2];
      } else {
         float c = (float)Math.cos(deg * (float) (Math.PI / 180.0));
         float s = (float)Math.sin(deg * (float) (Math.PI / 180.0));
         out[0] = d[0] * c + px / l * s;
         out[1] = d[1] * c + py / l * s;
         out[2] = d[2] * c + pz / l * s;
      }
   }

   public void finish() {
      this.canvas.finish();
   }

   static {
      for (int i = 0; i <= 4096; i++) {
         SIN[i] = (float)Math.sin(i * 2 * Math.PI / 4096.0);
      }

      DOME_ROWS = new float[]{-90.0F, -45.0F, -20.0F, -8.0F, -2.0F, 2.0F, 6.0F, 11.0F, 17.0F, 24.0F, 32.0F, 42.0F, 54.0F, 68.0F, 80.0F, 90.0F};
   }
}
