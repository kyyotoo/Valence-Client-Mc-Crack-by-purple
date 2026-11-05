package dev.valence.sky.classic;

final class Fx {
   private static final float[] A = new float[3];
   private static final float[] B = new float[3];
   private static final float[] R = new float[3];
   private static final float[][] TAIL_ROWS = new float[][]{new float[0], new float[0]};
   private static final int[][] TAIL_COLS = new int[][]{new int[0], new int[0]};
   private static float[] TAIL_US = new float[0];
   private static final float[] TAIL_VS = new float[]{1.0F, 0.0F};
   private static final float[] HEAD = new float[3];
   private static final float[] TAIL = new float[3];

   private Fx() {
   }

   static void planetWithRing(G g, float az, float el, float size, float rollDeg, float tiltDeg, float[] p) {
      G.dir(az, el, p);
      float px = p[0];
      float py = p[1];
      float pz = p[2];
      float ex = -pz;
      float l = (float)Math.sqrt(ex * ex + px * px);
      ex /= l;
      float ez = px / l;
      float ux = -ez * py;
      float uy = ez * px - ex * pz;
      float uz = ex * py;
      float roll = G.rad(rollDeg);
      float tilt = G.rad(tiltDeg);
      float cr = G.cos(roll);
      float sr = G.sin(roll);
      float ax = ex * cr + ux * sr;
      float ay = uy * sr;
      float az2 = ez * cr + uz * sr;
      float upx = ux * cr - ex * sr;
      float upy = uy * cr;
      float upz = uz * cr - ez * sr;
      float r = (float)Math.tan(G.rad(size * 1.05F));
      float st = G.sin(tilt);
      float ct = G.cos(tilt);
      float bx = upx * st + px * ct;
      float by = upy * st + py * ct;
      float bz = upz * st + pz * ct;
      float h = r * 0.5F;
      g.sprite(SkyTex.GLOW, az, el, size * 2.6F, 0.0F, 820564646);
      g.plane(SkyTex.RING, px + bx * h, py + by * h, pz + bz * h, ax * r, ay * r, az2 * r, bx * h, by * h, bz * h, 0.0F, 0.0F, 1.0F, 0.5F, -1);
      g.sprite(SkyTex.SATURN, px, py, pz, size, size, rollDeg, -1, 0.0F, 0.0F, 1.0F, 1.0F);
      g.plane(SkyTex.RING, px - bx * h, py - by * h, pz - bz * h, ax * r, ay * r, az2 * r, bx * h, by * h, bz * h, 0.0F, 0.5F, 1.0F, 1.0F, -1);
   }

   static void cometTail(G g, float[] head, float dirDeg, float bend, float length, float wHead, float wEnd, int colEnd, int colHead, double t, int seed) {
      cometTail(g, head, dirDeg, bend, length, wHead, wEnd, colEnd, colHead, t, seed, false);
   }

   static void cometTail(
      G g, float[] head, float dirDeg, float bend, float length, float wHead, float wEnd, int colEnd, int colHead, double t, int seed, boolean soft
   ) {
      int n = g.ctx.seg(8, 12, 16);
      float hx = head[0];
      float hy = head[1];
      float hz = head[2];
      float ex = -hz;
      float l = (float)Math.sqrt(ex * ex + hx * hx);
      ex /= l;
      float ez = hx / l;
      float ux = -ez * hy;
      float uy = ez * hx - ex * hz;
      float uz = ex * hy;
      if (TAIL_ROWS[0].length < n * 3) {
         TAIL_ROWS[0] = new float[n * 3];
         TAIL_ROWS[1] = new float[n * 3];
         TAIL_COLS[0] = new int[n];
         TAIL_COLS[1] = new int[n];
         TAIL_US = new float[n];
      }

      float[][] rows = TAIL_ROWS;
      int[][] cols = TAIL_COLS;
      float[] us = TAIL_US;

      for (int i = 0; i < n; i++) {
         float s = (float)i / (n - 1);
         float d = (1.0F - s) * length;
         float ang = G.rad(dirDeg + 90.0F + bend * (1.0F - s) * (1.0F - s) + 1.5F * G.sin(t * 0.7 + s * 4.0F + seed));
         float dx = G.cos(ang);
         float dy = G.sin(ang);
         float ox = ex * dx + ux * dy;
         float oy = uy * dy;
         float oz = ez * dx + uz * dy;
         float a = G.rad(d);
         float ca = G.cos(a);
         float sa = G.sin(a);
         float cx = hx * ca + ox * sa;
         float cy = hy * ca + oy * sa;
         float cz = hz * ca + oz * sa;
         float sx = oy * cz - oz * cy;
         float sy = oz * cx - ox * cz;
         float sz = ox * cy - oy * cx;
         float sl = (float)Math.sqrt(sx * sx + sy * sy + sz * sz);
         float w = (float)Math.tan(G.rad(G.lerp(wEnd, wHead, s) / 2.0F)) / Math.max(1.0E-5F, sl);
         int i3 = i * 3;
         rows[0][i3] = cx - sx * w;
         rows[0][i3 + 1] = cy - sy * w;
         rows[0][i3 + 2] = cz - sz * w;
         rows[1][i3] = cx + sx * w;
         rows[1][i3 + 1] = cy + sy * w;
         rows[1][i3 + 2] = cz + sz * w;
         float shimmer = 0.85F + 0.15F * G.sin(t * 1.7 - s * 9.0F + seed);
         int col = G.mix(colEnd, colHead, s);
         cols[0][i] = G.fade(col, shimmer);
         cols[1][i] = G.fade(col, shimmer);
         us[i] = soft ? 0.5F : 0.05F + 0.9F * s;
      }

      g.ribbon(soft ? SkyTex.GLOW : SkyTex.STREAK, rows, cols, TAIL_VS, us, n);
   }

   static void shootingStars(G g, int slots, float avgPeriod, float alpha, int color, int seed, float[] tmp) {
      if (g.ctx.shooting) {
         double t = g.ctx.time;

         for (int j = 0; j < slots; j++) {
            float period = avgPeriod * (0.7F + 0.6F * G.rnd(seed, j));
            double cyc = t / period + G.rnd(seed + 1, j);
            int k = (int)Math.floor(cyc);
            float dur = 0.9F + 0.6F * G.rnd(seed + 2, j * 31 + k);
            float q = G.fract(cyc) * period / dur;
            if (!(q >= 1.0F)) {
               float az = 360.0F * G.rnd(seed + 3, j * 31 + k);
               float el = 25.0F + 45.0F * G.rnd(seed + 4, j * 31 + k);
               float dirAz = az + 30.0F + 120.0F * G.rnd(seed + 5, j * 31 + k);
               float dirEl = el - 25.0F - 20.0F * G.rnd(seed + 6, j * 31 + k);
               G.dir(az, el, A);
               G.dir(dirAz, dirEl, B);
               streak(g, A, B, q, 22.0F, 9.0F, alpha, color);
            }
         }
      }
   }

   static void meteorShower(G g, int slots, float radAz, float radEl, double t, float[] tmp) {
      G.dir(radAz, radEl, R);

      for (int j = 0; j < slots; j++) {
         float period = 4.5F + 6.0F * G.rnd(51, j);
         double cyc = t / period + G.rnd(52, j);
         int k = (int)Math.floor(cyc);
         int id = j * 131 + k;
         if (!(G.rnd(59, id) < 0.2F)) {
            float dur = 0.45F + 0.55F * G.rnd(53, id);
            float q = G.fract(cyc) * period / dur;
            if (!(q >= 1.0F)) {
               awayFromRadiant(radAz, radEl, 15.0F + 50.0F * G.rnd(54, id), 360.0F * G.rnd(55, id));
               float mag = G.rnd(56, id);
               float c = G.rnd(57, id);
               int color = c < 0.65F ? -1 : (c < 0.85F ? -3608321 : -10000);
               float alpha = 0.35F + 0.6F * mag * mag;
               float len = 5.0F + 11.0F * mag;
               float travel = 10.0F + 16.0F * G.rnd(58, id);
               streak(g, A, B, q, travel, len, alpha, color, 0.25F + 0.3F * mag, 1.1F + 1.4F * mag);
            }
         }
      }

      float fbPeriod = 19.0F;
      double cyc = t / fbPeriod + 0.37;
      int k = (int)Math.floor(cyc);
      float q = G.fract(cyc) * fbPeriod / 1.9F;
      if (q < 1.0F) {
         awayFromRadiant(radAz, radEl, 20.0F + 30.0F * G.rnd(91, k), 360.0F * G.rnd(92, k));
         int color = G.rnd(93, k) < 0.5F ? -4653096 : -12128;
         streak(g, A, B, q, 42.0F, 26.0F, 1.0F, color, 0.75F, 5.0F);
         float e = 1.0F - (1.0F - q) * (1.0F - q);

         for (int s = 0; s < 8; s++) {
            float back = 1.5F + s * 1.8F + 0.8F * G.sin(t * 9.0 + s);
            G.towards(A, B, Math.max(0.0F, 42.0F * e - back), HEAD);
            float a = G.smooth(0.0F, 0.15F, q) * G.smooth(1.0F, 0.7F, q) * (1.0F - s / 9.0F) * (0.5F + 0.5F * G.sin(t * 20.0 + s * 2.7));
            g.sprite(SkyTex.DOT, HEAD[0], HEAD[1] - 0.004F * s, HEAD[2], 0.5F, 0.5F, 0.0F, G.fade(color, a), 0.0F, 0.0F, 1.0F, 1.0F);
         }
      }
   }

   private static void awayFromRadiant(float radAz, float radEl, float off, float angDeg) {
      G.dir(radAz + off * G.cos(G.rad(angDeg)), Math.max(8.0F, radEl + off * 0.7F * G.sin(G.rad(angDeg))), A);
      B[0] = A[0] * 2.0F - R[0];
      B[1] = A[1] * 2.0F - R[1];
      B[2] = A[2] * 2.0F - R[2];
      float bl = (float)Math.sqrt(B[0] * B[0] + B[1] * B[1] + B[2] * B[2]);
      B[0] = B[0] / bl;
      B[1] = B[1] / bl;
      B[2] = B[2] / bl;
   }

   private static void streak(G g, float[] from, float[] towards, float q, float travel, float len, float alpha, int color) {
      streak(g, from, towards, q, travel, len, alpha, color, 0.42F, 1.6F);
   }

   private static void streak(G g, float[] from, float[] towards, float q, float travel, float len, float alpha, int color, float width, float glow) {
      float[] head = HEAD;
      float[] tail = TAIL;
      float e = 1.0F - (1.0F - q) * (1.0F - q);
      G.towards(from, towards, travel * e, head);
      G.towards(head, from, Math.min(len * (0.3F + 0.7F * q), travel * e + 0.5F), tail);
      float a = alpha * G.smooth(0.0F, 0.12F, q) * G.smooth(1.0F, 0.65F, q);
      g.segment(SkyTex.STREAK, tail, head, 0.05F, width, G.fade(color, 0.0F), G.fade(color, a), true);
      g.sprite(SkyTex.GLOW, head[0], head[1], head[2], glow, glow, 0.0F, G.fade(color, a * 0.7F), 0.0F, 0.0F, 1.0F, 1.0F);
   }

   static void birds(G g, float az, float el, double t) {
      float[] a = A;
      float[] b = B;
      int n = g.ctx.quality == 0 ? 3 : 5;
      float drift = (float)(t * 0.8 % 160.0) - 40.0F;

      for (int i = 0; i < n; i++) {
         float bAz = az + drift + i * 1.6F + 1.2F * G.sin(t * 0.4 + i);
         float bEl = el + i % 2 * 0.9F + 0.6F * G.sin(t * 0.5 + i * 2);
         float flap = 0.25F + 0.35F * (float)Math.abs(Math.sin(t * 3.2 + i * 1.3));
         float vis = G.smooth(-40.0F, -30.0F, drift) * G.smooth(120.0F, 105.0F, drift);
         int col = G.fade(-1070982614, vis);
         G.dir(bAz, bEl, a);
         G.dir(bAz - 0.45F, bEl + flap, b);
         g.segment(SkyTex.LINE, a, b, 0.12F, 0.08F, col, col, false);
         G.dir(bAz + 0.45F, bEl + flap, b);
         g.segment(SkyTex.LINE, a, b, 0.12F, 0.08F, col, col, false);
      }
   }
}
