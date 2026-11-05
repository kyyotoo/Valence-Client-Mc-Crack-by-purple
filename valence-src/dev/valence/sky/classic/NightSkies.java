package dev.valence.sky.classic;

final class NightSkies {
   private NightSkies() {
   }

   static final class Amethyst extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{656920, 1707834, 4204418, 3481722, 2693226, 2036312, 1642058};
      private final StarField stars = new StarField(1301, 300, 20.0F);
      private final float[] tmp = new float[3];

      Amethyst() {
         super("amethyst", -12704384, -4944641);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(300, c.stars), 0.6F, 1.0F, 0.5F, 0, 0.0F);
         g.horizonGlow(0.0F, 360.0F, -2.0F, 18.0F, 1890611455);
         float b1 = 0.8F + 0.2F * G.sin(t * 0.3);
         g.ring(SkyTex.AURORA, (float)(-t * 0.5 + 77.0), -4.0F, 52.0F, 0.1F, 0.93F, G.fade(-2134865153, b1), 0.0F, 0.1F);
         g.ring(SkyTex.AURORA, (float)(t * 0.8), -4.0F, 78.0F, 0.0F, 0.93F, -1333109505, 0.0F, 0.12F);
         int n = c.count(30, c.particles);

         for (int i = 0; i < n; i++) {
            float az = 360.0F * G.rnd(91, i) + (float)(t * (0.6 + 0.8 * G.rnd(92, i)));
            float w = 1.5F + 3.0F * G.rnd(93, i);
            float h = 35.0F + 50.0F * G.rnd(94, i);
            float a = 0.5F + 0.5F * G.sin(t * (0.25 + 0.4 * G.rnd(95, i)) + i * 1.7);
            g.band(SkyTex.BEAM, az - w, az + w, -3.0F, h, 0.0F, 1.0F, 0.0F, 1.0F, G.fade(-1865899777, a * a), 0.0F, 0.1F, 0.0F);
         }

         this.stars.drawSparkles(g, c.count(8, c.stars), 0.7F, 0.9F, -1517313);
         Fx.shootingStars(g, 1, 14.0F, 0.45F, -990977, 1311, this.tmp);
      }
   }

   static final class Aurora extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -10.0F, 0.0F, 6.0F, 18.0F, 40.0F, 65.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{132625, 398371, 932414, 731706, 530734, 462630, 526882, 591903};
      private final StarField stars = new StarField(201, 900, -6.0F);
      private final Curtain[] curtains = new Curtain[]{
         new Curtain(6, 120.0F, 140.0F, 30.0F, 26.0F, -11540288, -14173796, -10853633).alpha(0.35F).wave(1.2F),
         new Curtain(4, 200.0F, 150.0F, 26.0F, 30.0F, -10289216, -14038616, -7712513).alpha(0.45F).wave(1.4F),
         new Curtain(3, -70.0F, 110.0F, 12.0F, 30.0F, -8585314, -13246322, -4170497).alpha(0.75F).lean(6.0F),
         new Curtain(2, 55.0F, 130.0F, 22.0F, 36.0F, -10944568, -14494794, -9475073).alpha(0.7F).lean(-3.0F),
         new Curtain(1, 0.0F, 170.0F, 11.0F, 46.0F, -4653112, -11731050, -36904).alpha(1.0F),
         new Curtain(5, 8.0F, 120.0F, 16.0F, 40.0F, -3080224, -9633880, -34084).alpha(0.6F).lean(8.0F).wave(0.8F),
         new Curtain(7, 10.0F, 150.0F, 40.0F, 24.0F, -29992, -4169473, -10862337).alpha(0.5F).lean(4.0F).wave(1.1F)
      };
      private final float[] tmp = new float[3];

      Aurora() {
         super("aurora", -16045517, -11665484);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 220.0F, -3.0F, 16.0F, 975237280);
         g.horizonGlow(180.0F, 160.0F, -3.0F, 12.0F, 540059872);
         g.push();
         g.rotY((float)(t * 0.25));
         this.stars.draw(g, c.count(900, c.stars), 0.95F, 1.0F, 0.5F, 0, 0.0F);
         g.pop();
         g.ring(SkyTex.NEBULA2, (float)(t * 0.6), 8.0F, 62.0F, 0.05F, 0.95F, 574422960, 0.35F, 0.4F);
         g.sprite(SkyTex.GLOW, 0.0F, 24.0F, 120.0F, 0.0F, 806936736);
         float strength = 1.0F - 0.5F * c.rain;
         int n = c.quality == 0 ? 4 : this.curtains.length;

         for (int i = this.curtains.length - n; i < this.curtains.length; i++) {
            this.curtains[i].draw(g, strength);
         }

         if (c.quality > 0) {
            this.curtains[4].draw(g, strength * 0.7F);
            this.curtains[3].draw(g, strength * 0.4F);
         }

         Fx.shootingStars(g, 1, 15.0F, 0.5F, -1507340, 211, this.tmp);
      }
   }

   static final class Crystal extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{135183, 269340, 999486, 800565, 601389, 468261, 401183};
      private final StarField sparkles = new StarField(1201, 60, 5.0F);

      Crystal() {
         super("crystal", -15843780, -12976170);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -2.0F, 16.0F, 1345978326);
         float pulse = 0.72F + 0.28F * G.sin(t * 0.55);
         g.cap(SkyTex.CRYSTAL, 0.0F, 90.0F, 88.0F, (float)(-t * 0.7 + 30.0), 1.6F, G.fade(1345978326, 1.0F - pulse * 0.4F), 0.55F);
         g.cap(SkyTex.CRYSTAL, 0.0F, 90.0F, 88.0F, (float)(t * 1.1), 1.0F, G.fade(-799997984, pulse), 0.62F);
         g.cap(SkyTex.GLOW, 0.0F, 90.0F, 60.0F + 25.0F * G.fract(t * 0.08), 0.0F, 1.0F, G.fade(540671958, 1.0F - G.fract(t * 0.08)), 0.5F);
         this.sparkles.drawSparkles(g, c.count(40, c.particles), 0.9F, 0.8F, -3604492);
      }
   }

   static final class DeepSea extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -10.0F, 0.0F, 15.0F, 40.0F, 70.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{135192, 404022, 1002588, 1268846, 1534332, 1799560, 1932688};

      DeepSea() {
         super("deepsea", -15774628, -12592912);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         float zoom = 1.35F + 0.06F * G.sin(t * 0.37);
         g.cap(SkyTex.CAUSTICS, 0.0F, 90.0F, 86.0F, (float)(-t * 1.1 + 40.0), zoom, 1885397232, 0.5F);
         g.cap(SkyTex.CAUSTICS, 0.0F, 90.0F, 86.0F, (float)(t * 1.5), 1.0F, -795282177, 0.5F);
         int shafts = c.quality == 0 ? 5 : 9;

         for (int i = 0; i < shafts; i++) {
            float az = 360.0F * G.rnd(81, i) + 6.0F * G.sin(t * 0.05 + i);
            float w = 3.0F + 4.0F * G.rnd(82, i);
            float a = 0.5F + 0.5F * G.sin(t * (0.3 + 0.3 * G.rnd(83, i)) + i * 2.1);
            g.band(SkyTex.BEAM, az - w, az + w, 12.0F, 84.0F, 0.0F, 1.0F, 1.0F, 0.0F, G.fade(1355348223, a), 0.25F, 0.1F, 0.0F);
         }

         int n = c.count(46, c.particles);

         for (int i = 0; i < n; i++) {
            float sp = 0.02F + 0.03F * G.rnd(84, i);
            float q = G.fract(t * sp + G.rnd(85, i));
            float az = 360.0F * G.rnd(86, i) + 2.5F * G.sin(t * 0.9 + i * 3.1);
            float el = -4.0F + q * 82.0F;
            float a = G.smooth(0.0F, 0.1F, q) * G.smooth(1.0F, 0.75F, q);
            float size = 0.6F + 1.0F * G.rnd(87, i);
            g.sprite(SkyTex.BUBBLE, az, el, size, 0.0F, G.fade(-1059520513, a));
         }

         int m = c.count(70, c.particles);

         for (int i = 0; i < m; i++) {
            float az = 360.0F * G.rnd(88, i) + (float)(t * (0.4 + G.rnd(89, i)));
            float el = 2.0F + 60.0F * G.rnd(90, i) + 2.0F * G.sin(t * 0.3 + i);
            float a = 0.35F + 0.35F * G.sin(t * 0.7 + i * 1.3);
            g.sprite(SkyTex.DOT, az, el, 0.35F, 0.0F, G.fade(-4197121, a));
         }
      }
   }

   static final class Fireflies extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 4.0F, 12.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{1707560, 3808826, 16751194, 13662826, 9067162, 5913226, 3811946, 2760280};
      private final StarField stars = new StarField(1501, 300, 22.0F);

      Fireflies() {
         super("fireflies", -1537438, -11654);
         this.fadeFull = 0.0F;
         this.fadeTop = 5.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(300, c.stars), 0.6F, 1.0F, 0.6F, 0, 0.0F);
         g.horizonGlow(0.0F, 200.0F, -2.0F, 8.0F, 1627369568);
         int n = c.count(80, c.particles);

         for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < n; i++) {
               float baseAz = 360.0F * G.rnd(111, i);
               float baseEl = 2.0F + 50.0F * G.rnd(112, i) * G.rnd(113, i);
               float f1 = 0.05F + 0.08F * G.rnd(114, i);
               float f2 = 0.11F + 0.1F * G.rnd(115, i);
               float az = baseAz + 7.0F * G.sin(t * f1 + i) + 3.0F * G.sin(t * f2 * 1.7 + i * 2);
               float el = baseEl + 3.0F * G.sin(t * f2 + i * 3);
               float blink = 0.5F + 0.5F * G.sin(t * (0.6 + 0.9 * G.rnd(116, i)) + i * 5.3);
               blink = G.smooth(0.25F, 1.0F, blink);
               float a = 0.2F + 0.8F * blink;
               float near = 0.6F + 0.8F * G.rnd(117, i);
               if (pass == 0) {
                  g.sprite(SkyTex.GLOW, az, el, 5.5F * near, 0.0F, G.fade(-1593849760, a * 0.85F));
               } else {
                  g.sprite(SkyTex.DOT, az, el, 0.9F * near, 0.0F, G.fade(-2360, a));
               }
            }
         }
      }
   }

   static final class Neon extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 4.0F, 12.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{1180958, 2755130, 16727717, 13906074, 9054878, 5119114, 2757728, 1707588};
      private final StarField stars = new StarField(1101, 160, 25.0F);

      Neon() {
         super("neon", -49499, -45096);
         this.fadeFull = 0.0F;
         this.fadeTop = 4.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(160, c.stars), 0.7F, 1.0F, 0.6F, 0, 0.0F);
         g.horizonGlow(0.0F, 360.0F, -2.0F, 12.0F, -1862316112);
         float pulse = 0.85F + 0.15F * G.sin(t * 0.8);
         g.sprite(SkyTex.GLOW, 0.0F, 8.0F, 70.0F, 0.0F, G.fade(-2130755680, pulse));
         g.sprite(SkyTex.GLOW, 0.0F, 8.0F, 34.0F, 0.0F, G.fade(-1862295472, pulse));
         g.sprite(SkyTex.SYNTHSUN, 0.0F, 9.5F, 22.0F, 0.0F, -1);
         int lines = c.quality == 0 ? 6 : 9;

         for (int i = 0; i < lines; i++) {
            float q = G.fract((float)i / lines + t * 0.035);
            float el = 0.6F + 20.0F * (1.0F - q) * (1.0F - q);
            float a = G.smooth(0.0F, 0.15F, q) * G.smooth(1.0F, 0.82F, q);
            float th = 0.35F + 0.5F * q;
            g.hline(0.0F, 360.0F, el, th * 7.0F, G.fade(-53344, a * 0.18F));
            g.hline(0.0F, 360.0F, el, th, G.fade(-29992, a * 0.95F));
         }
      }
   }

   static final class Storm extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{1382171, 2303532, 5264476, 4606545, 3882564, 3356219, 2961204};
      private final float[] tmp = new float[3];
      private final float[] a = new float[3];
      private final float[] b = new float[3];
      private final float[] pts = new float[128];

      Storm() {
         super("storm", -12170670, -3614977);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         float period = 6.5F;
         int k = (int)Math.floor(t / period);
         float offset = 0.5F + 3.5F * G.rnd(71, k);
         float tau = (float)(t - k * period - offset);
         float flash = 0.0F;
         if (tau > 0.0F && tau < 1.2F) {
            flash = (float)(Math.exp(-tau * 7.0) + 0.7 * Math.exp(-Math.pow((tau - 0.22) / 0.05, 2.0)) + 0.35 * Math.exp(-Math.pow((tau - 0.42) / 0.06, 2.0)));
            flash = Math.min(1.0F, flash) * G.smooth(0.0F, 0.06F, tau);
         }

         if (!c.lightning) {
            flash = 0.0F;
         }

         float boltAz = -60.0F + 120.0F * G.rnd(72, k);
         g.dome(DOME_EL, DOME_COL);
         g.cap(SkyTex.NEBULA1, 0.0F, 90.0F, 75.0F, (float)(t * 1.6), 1.0F, -803200216, 0.55F);
         g.cap(SkyTex.NEBULA2, 0.0F, 90.0F, 70.0F, (float)(-t * 1.1 + 60.0), 1.2F, -2140508552, 0.5F);
         g.ring(SkyTex.NEBULA1, (float)(-t * 1.1), 18.0F, 72.0F, 0.1F, 0.9F, -1340071128, 0.25F, 0.35F);
         g.ring(SkyTex.CLOUDS, (float)(t * 1.6 + 90.0), 8.0F, 50.0F, 0.0F, 1.0F, -1603242880, 0.15F, 0.35F);
         if (flash > 0.01F) {
            g.sprite(SkyTex.GLOW, boltAz, 30.0F, 150.0F, 0.0F, G.fade(-3155713, flash * 0.55F));
            g.band(SkyTex.WHITE, 0.0F, 360.0F, -5.0F, 90.0F, 0.5F, 0.5F, 0.5F, 0.5F, G.fade(-1511681, flash * 0.12F), 0.0F, 0.0F, 0.0F);
         }

         g.ring(SkyTex.CLOUDS, (float)(t * 2.6), -2.0F, 28.0F, 0.0F, 1.0F, -534765017, 0.1F, 0.3F);
         if (flash > 0.01F) {
            this.bolt(g, k, boltAz, Math.min(1.0F, flash * 1.4F));
            if (G.rnd(73, k) < 0.4F) {
               this.bolt(g, k + 1000, boltAz + 30.0F + 40.0F * G.rnd(74, k), flash * 0.6F);
            }
         }

         float f2 = G.fract(t / 2.7 + 0.3);
         int k2 = (int)Math.floor(t / 2.7 + 0.3);
         if (f2 < 0.12F && G.rnd(75, k2) < 0.5F) {
            float ff = G.sin(f2 / 0.12F * Math.PI);
            g.sprite(SkyTex.GLOW, 360.0F * G.rnd(76, k2), 12.0F + 15.0F * G.rnd(77, k2), 50.0F, 0.0F, G.fade(-4668184, ff * 0.25F));
         }
      }

      private void bolt(G g, int seed, float az, float alpha) {
         int n = g.ctx.seg(12, 18, 24);
         float el0 = 52.0F + 14.0F * G.rnd(seed, 1);
         float cur = az;

         for (int i = 0; i < n; i++) {
            float s = (float)i / (n - 1);
            this.pts[i * 2] = cur;
            this.pts[i * 2 + 1] = el0 * (1.0F - s) - 1.0F;
            cur += (G.rnd(seed, 10 + i) - 0.5F) * 5.0F;
         }

         this.drawPath(g, this.pts, n, 0.32F, alpha);
         int branches = 2 + (int)(G.rnd(seed, 2) * 3.0F);

         for (int bIdx = 0; bIdx < branches; bIdx++) {
            int start = 2 + (int)(G.rnd(seed, 40 + bIdx) * (n / 2));
            float bAz = this.pts[start * 2];
            float bEl = this.pts[start * 2 + 1];
            float dir = G.rnd(seed, 50 + bIdx) < 0.5F ? -1.0F : 1.0F;
            int m = 6 + (int)(G.rnd(seed, 60 + bIdx) * 6.0F);
            float[] bp = new float[m * 2];

            for (int i = 0; i < m; i++) {
               bp[i * 2] = bAz;
               bp[i * 2 + 1] = bEl;
               bAz += dir * (1.0F + 2.5F * G.rnd(seed, 100 + bIdx * 20 + i));
               bEl -= 1.2F + 2.2F * G.rnd(seed, 200 + bIdx * 20 + i);
            }

            this.drawPath(g, bp, m, 0.18F, alpha * 0.7F);
         }
      }

      private void drawPath(G g, float[] p, int n, float width, float alpha) {
         for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < n - 1; i++) {
               G.dir(p[i * 2], p[i * 2 + 1], this.a);
               G.dir(p[i * 2 + 2], p[i * 2 + 3], this.b);
               if (pass == 0) {
                  g.segment(SkyTex.LINE, this.a, this.b, width * 9.0F, width * 9.0F, G.fade(-7362305, alpha * 0.35F), G.fade(-7362305, alpha * 0.35F), false);
               } else {
                  g.segment(SkyTex.LINE, this.a, this.b, width, width, G.fade(-722945, alpha), G.fade(-722945, alpha), false);
               }
            }
         }
      }
   }

   static final class Winter extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{264728, 662074, 1850460, 1520207, 1124164, 859705, 661293};
      private final StarField stars = new StarField(1401, 1000, -6.0F);
      private final Curtain band = new Curtain(7, 20.0F, 240.0F, 15.0F, 26.0F, -10686248, -13842240, -12947201).alpha(0.9F).wave(0.8F);
      private final float[] tmp = new float[3];

      Winter() {
         super("winter", -15058342, -4331777);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.push();
         g.rotY((float)(t * 0.25));
         this.stars.draw(g, c.count(1000, c.stars), 0.95F, 1.0F, 0.6F, 0, 0.0F);
         this.stars.drawSparkles(g, c.count(14, c.stars), 0.7F, 0.9F, -2232577);
         g.pop();
         this.band.draw(g, 1.0F - 0.5F * c.rain);
         g.sprite(SkyTex.GLOW, -28.0F, 36.0F, 48.0F, 0.0F, 1221124351);
         g.sprite(SkyTex.GLOW, -28.0F, 36.0F, 17.0F, 0.0F, -2132741889);
         g.sprite(SkyTex.MOON, -28.0F, 36.0F, 6.5F, 0.0F, -985345);
         int n = c.count(120, c.particles);

         for (int i = 0; i < n; i++) {
            float depth = G.rnd(101, i);
            float sp = 0.012F + 0.03F * depth;
            float q = G.fract(t * sp + G.rnd(102, i));
            float el = 82.0F - q * 96.0F;
            float az = 360.0F * G.rnd(103, i) + 5.0F * G.sin(t * (0.3 + 0.4 * depth) + i * 2.3);
            float a = G.smooth(0.0F, 0.1F, q) * G.smooth(1.0F, 0.85F, q) * (0.45F + 0.55F * depth);
            float size = 0.6F + 1.3F * depth;
            g.sprite(SkyTex.FLAKE, az, el, size, (float)(t * 25.0 * (G.rnd(104, i) - 0.5F)), G.fade(-520093697, a));
         }

         Fx.shootingStars(g, 1, 16.0F, 0.45F, -1510145, 1411, this.tmp);
      }
   }
}
