package dev.valence.sky.classic;

final class SpaceSkies {
   private SpaceSkies() {
   }

   static void basis(float[] d, float[] out) {
      float ex = -d[2];
      float ez = d[0];
      float l = (float)Math.sqrt(ex * ex + ez * ez);
      if (l < 1.0E-5F) {
         ex = 1.0F;
         ez = 0.0F;
         l = 1.0F;
      }

      ex /= l;
      ez /= l;
      out[0] = ex;
      out[1] = 0.0F;
      out[2] = ez;
      out[3] = -ez * d[1];
      out[4] = ez * d[0] - ex * d[2];
      out[5] = ex * d[1];
   }

   static void offset(float[] c, float[] b, float angDeg, float deg, float[] out) {
      float a = G.rad(angDeg);
      float ca = G.cos(a);
      float sa = G.sin(a);
      float ox = b[0] * ca + b[3] * sa;
      float oy = b[1] * ca + b[4] * sa;
      float oz = b[2] * ca + b[5] * sa;
      float r = G.rad(deg);
      float cr = G.cos(r);
      float sr = G.sin(r);
      out[0] = c[0] * cr + ox * sr;
      out[1] = c[1] * cr + oy * sr;
      out[2] = c[2] * cr + oz * sr;
   }

   static final class Comet extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{264212, 660784, 1846348, 1517124, 1121850, 858160, 660264};
      private final StarField stars = new StarField(701, 1000, -6.0F);
      private final float[] head = new float[3];
      private final float[] sun = new float[3];
      private final float[] b = new float[6];
      private final float[] pt = new float[3];
      private final float[] tmp = new float[3];

      Comet() {
         super("comet", -15062454, -6363393);
         G.dir(150.0F, -25.0F, this.sun);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 16.0F, 808471168);
         g.push();
         g.rotY((float)(t * 0.25));
         g.push();
         g.rotZ(-40.0F);
         g.ring(SkyTex.NEBULA2, 30.0F, -14.0F, 14.0F, 0.3F, 0.7F, 810582208, 0.4F, 0.4F);
         g.ring(SkyTex.MILKYWAY, 60.0F, -10.0F, 10.0F, 0.1F, 0.9F, 1086904575, 0.2F, 0.2F);
         g.pop();
         this.stars.draw(g, c.count(1000, c.stars), 0.9F, 1.0F, 0.5F, 0, 0.0F);
         g.pop();
         float hAz = -28.0F + 34.0F * G.sin(t * 0.011);
         float hEl = 36.0F + 9.0F * G.sin(t * 0.0085 + 1.2);
         G.dir(hAz, hEl, this.head);
         SpaceSkies.basis(this.head, this.b);
         float d = this.head[0] * this.sun[0] + this.head[1] * this.sun[1] + this.head[2] * this.sun[2];
         float tx = this.head[0] * d - this.sun[0];
         float ty = this.head[1] * d - this.sun[1];
         float tz = this.head[2] * d - this.sun[2];
         float away = (float)Math.toDegrees(Math.atan2(tx * this.b[3] + ty * this.b[4] + tz * this.b[5], tx * this.b[0] + ty * this.b[1] + tz * this.b[2]));
         float dirDeg = away - 90.0F;
         float sway = 2.5F * G.sin(t * 0.13);
         Fx.cometTail(g, this.head, dirDeg + 9.0F, 22.0F + sway, 40.0F, 10.0F, 30.0F, 16769728, -1325406000, t, 2, true);
         Fx.cometTail(g, this.head, dirDeg + 6.0F, 16.0F + sway, 32.0F, 6.0F, 18.0F, 16773344, -1056967446, t, 4, true);
         Fx.cometTail(g, this.head, dirDeg + 4.0F, 10.0F + sway, 22.0F, 3.0F, 8.0F, 285212671, -520093697, t, 5, true);

         for (int k = 0; k < 3; k++) {
            float fl = 0.7F + 0.3F * G.sin(t * (2.1 + k * 0.7) + k * 2.0);
            float spread = (k - 1) * 1.6F + 0.6F * G.sin(t * 0.4 + k);
            int colHead = G.fade(-522653441, fl * (k == 1 ? 1.0F : 0.6F));
            Fx.cometTail(g, this.head, dirDeg + spread, 1.5F, 50.0F + 6.0F * k, k == 1 ? 2.2F : 1.2F, k == 1 ? 9.0F : 5.0F, 11067647, colHead, t, 1 + k, true);
         }

         for (int k = 0; k < 3; k++) {
            float q = G.fract(t * 0.045 + k / 3.0);
            float dist = 4.0F + q * 44.0F;
            SpaceSkies.offset(this.head, this.b, dirDeg + 90.0F, dist, this.pt);
            float a = G.smooth(0.0F, 0.1F, q) * (1.0F - q);
            g.sprite(
               SkyTex.GLOW, this.pt[0], this.pt[1], this.pt[2], 3.5F + 3.0F * q, 3.5F + 3.0F * q, 0.0F, G.fade(-4659969, a * 0.55F), 0.0F, 0.0F, 1.0F, 1.0F
            );
         }

         int n = c.count(40, c.particles);

         for (int i = 0; i < n; i++) {
            float q = G.fract(t * (0.02 + 0.025 * G.rnd(721, i)) + G.rnd(722, i));
            float dist = 2.0F + q * 30.0F;
            float side = (G.rnd(723, i) - 0.5F) * (4.0F + q * 18.0F);
            SpaceSkies.offset(this.head, this.b, dirDeg + 90.0F + 8.0F + side, dist, this.pt);
            float a = G.smooth(0.0F, 0.08F, q) * (1.0F - q) * 0.8F;
            g.sprite(
               SkyTex.DOT,
               this.pt[0],
               this.pt[1],
               this.pt[2],
               0.35F + 0.3F * G.rnd(724, i),
               0.35F + 0.3F * G.rnd(724, i),
               0.0F,
               G.fade(-5936, a),
               0.0F,
               0.0F,
               1.0F,
               1.0F
            );
         }

         float flick = 0.9F + 0.1F * G.sin(t * 2.3);
         g.sprite(SkyTex.GLOW, hAz, hEl, 26.0F, 0.0F, G.fade(1081131263, flick));
         g.sprite(SkyTex.GLOW, hAz, hEl, 10.0F, 0.0F, G.fade(-1329010433, flick));
         g.sprite(SkyTex.GLOW, hAz, hEl, 4.5F, 0.0F, G.fade(-521603841, flick));
         g.sprite(SkyTex.GLOW, hAz, hEl, 1.8F, 0.0F, -1);
         Fx.shootingStars(g, 1, 12.0F, 0.5F, -1, 31, this.tmp);
      }
   }

   static final class GalaxyCore extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{328456, 656914, 1839145, 1510693, 1116703, 854041, 656916};
      private final StarField stars = new StarField(301, 1200, -6.0F);
      private final float[] tmp = new float[3];

      GalaxyCore() {
         super("galaxy", -15069144, -9800);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 14.0F, 674898000);
         g.push();
         g.rotY((float)(t * 0.45));
         this.stars.draw(g, c.count(1200, c.stars), 0.9F, 1.0F, 0.6F, 0, 0.0F);
         g.push();
         g.rotX(38.0F);
         g.rotZ(-32.0F);
         g.ring(SkyTex.NEBULA1, (float)(t * 1.1), -28.0F, 28.0F, 0.15F, 0.85F, 1519014096, 0.45F, 0.45F);
         g.ring(SkyTex.NEBULA2, (float)(200.0 - t * 0.8), -20.0F, 20.0F, 0.1F, 0.9F, 1213885152, 0.4F, 0.4F);
         g.ring(SkyTex.MILKYWAY, (float)(t * 0.5), -16.0F, 16.0F, 0.0F, 1.0F, -922746881, 0.15F, 0.15F);
         g.ring(SkyTex.MILKYWAY, (float)(180.0 - t * 0.35), -9.0F, 9.0F, 0.2F, 0.8F, 1627384016, 0.2F, 0.2F);
         g.ring(SkyTex.NEBULA2, (float)(140.0 + t * 0.6), -7.0F, 7.0F, 0.35F, 0.65F, -2146433000, 0.3F, 0.3F);
         float pulse = 0.85F + 0.15F * G.sin(t * 0.7);
         g.cap(SkyTex.NEBULA1, 8.0F, 0.0F, 30.0F, (float)(t * 5.0), 1.3F, G.fade(1895799402, pulse), 0.35F);
         g.cap(SkyTex.NEBULA2, 8.0F, 0.0F, 22.0F, (float)(-t * 7.0 + 90.0), 1.6F, G.fade(-2130720624, pulse), 0.35F);
         g.sprite(SkyTex.RAYS, 8.0F, 0.0F, 58.0F, (float)(t * 3.0), G.fade(956291512, pulse));
         g.sprite(SkyTex.RAYS, 8.0F, 0.0F, 44.0F, (float)(-t * 2.2 + 30.0), G.fade(822077648, pulse));
         g.sprite(SkyTex.GLOW, 8.0F, 0.0F, 70.0F, 0.0F, G.fade(1358928490, pulse));
         g.sprite(SkyTex.GLOW, 8.0F, 0.0F, 34.0F, 0.0F, G.fade(-1862280776, pulse));
         g.sprite(SkyTex.GLOW, 8.0F, 0.0F, 13.0F, 0.0F, G.fade(-520097056, 0.8F + 0.2F * pulse));
         g.sprite(SkyTex.GLOW, 8.0F, 0.0F, 4.0F, 0.0F, -1);
         int orbiters = c.count(70, c.particles);

         for (int i = 0; i < orbiters; i++) {
            float r = 2.5F + 14.0F * G.rnd(311, i) * G.rnd(312, i);
            float speed = 0.35F / (0.4F + r * 0.12F);
            float a = (float)(t * speed + G.rnd(313, i) * 6.283);
            float x = 8.0F + r * G.cos(a);
            float y = r * 0.32F * G.sin(a);
            float tw = 0.55F + 0.45F * G.sin(t * (1.5 + G.rnd(314, i) * 2.0F) + i);
            int col = G.mix(-6456, -19846, G.rnd(315, i));
            g.sprite(SkyTex.DOT, x, y, 0.35F + 0.35F * G.rnd(316, i), 0.0F, G.fade(col, tw));
         }

         g.pop();
         this.stars.drawSparkles(g, c.count(12, c.stars), 0.75F, 1.0F, -3872);
         g.pop();
         Fx.shootingStars(g, 2, 10.0F, 0.55F, -2840, 51, this.tmp);
      }
   }

   static final class Hyperspace extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{131848, 263695, 527130, 461335, 395282, 329487, 263693};
      private final StarField stars = new StarField(501, 350, -6.0F);
      private final float[] center = new float[3];
      private final float[] head = new float[3];
      private final float[] tail = new float[3];

      Hyperspace() {
         super("hyperspace", -16315880, -9459457);
         this.fadeTop = 8.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(350, c.stars), 0.5F, 0.9F, 0.3F, 0, 0.0F);
         float cAz = 0.0F;
         float cEl = 14.0F;
         G.dir(cAz, cEl, this.center);
         float pulse = 0.85F + 0.15F * G.sin(t * 1.3);
         g.sprite(SkyTex.GLOW, cAz, cEl, 46.0F, 0.0F, G.fade(1346005759, pulse));
         g.sprite(SkyTex.GLOW, cAz, cEl, 14.0F, 0.0F, G.fade(-1601129217, pulse));
         g.sprite(SkyTex.GLOW, cAz, cEl, 4.0F, 0.0F, -253169409);
         float ex = -this.center[2];
         float ez = this.center[0];
         float l = (float)Math.sqrt(ex * ex + ez * ez);
         ex /= l;
         ez /= l;
         float ux = -ez * this.center[1];
         float uy = ez * this.center[0] - ex * this.center[2];
         float uz = ex * this.center[1];
         int n = c.count(240, c.particles);
         float spin = (float)(t * 0.05);

         for (int i = 0; i < n; i++) {
            float sp = 0.18F + 0.26F * G.rnd(7, i);
            double cyc = t * sp + G.rnd(8, i);
            int k = (int)Math.floor(cyc);
            float q = G.fract(cyc);
            float th = (float)((Math.PI * 2) * G.rnd(9, i * 977 + k)) + spin;
            float headA = 2.0F + q * q * q * 178.0F;
            float len = 0.6F + q * q * 60.0F;
            float tailA = Math.max(1.5F, headA - len);
            float a = G.smooth(0.0F, 0.1F, q);
            if (!(a < 0.02F)) {
               float ct = G.cos(th);
               float st = G.sin(th);
               float ox = ex * ct + ux * st;
               float oy = uy * st;
               float oz = ez * ct + uz * st;
               polar(this.center, ox, oy, oz, Math.min(headA, 179.0F), this.head);
               polar(this.center, ox, oy, oz, Math.min(tailA, 178.0F), this.tail);
               int colr = G.mix(-1, -7359233, G.rnd(10, i));
               g.segment(SkyTex.STREAK, this.tail, this.head, 0.1F, 0.18F + 0.9F * q, G.fade(colr, 0.0F), G.fade(colr, a), true);
            }
         }
      }

      private static void polar(float[] c, float ox, float oy, float oz, float deg, float[] out) {
         float a = G.rad(deg);
         float ca = G.cos(a);
         float sa = G.sin(a);
         out[0] = c[0] * ca + ox * sa;
         out[1] = c[1] * ca + oy * sa;
         out[2] = c[2] * ca + oz * sa;
      }
   }

   static final class Meteors extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{459792, 1182244, 2496569, 2102324, 1642284, 1313572, 1050910};
      private final StarField stars = new StarField(801, 1000, -6.0F);
      private final float[] tmp = new float[3];

      Meteors() {
         super("meteors", -14412232, -2049793);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.push();
         g.rotY((float)(t * 0.25));
         g.push();
         g.rotX(30.0F);
         g.rotZ(48.0F);
         g.ring(SkyTex.NEBULA1, 0.0F, -22.0F, 22.0F, 0.2F, 0.8F, 811612816, 0.45F, 0.45F);
         g.ring(SkyTex.MILKYWAY, 50.0F, -12.0F, 12.0F, 0.0F, 1.0F, 1491654911, 0.15F, 0.15F);
         g.pop();
         this.stars.draw(g, c.count(1000, c.stars), 0.85F, 1.0F, 0.5F, 0, 0.0F);
         g.pop();
         Fx.meteorShower(g, 5, 150.0F, 58.0F, t, this.tmp);
      }
   }

   static final class Nebula extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{262922, 657174, 1577516, 1314600, 920096, 657176, 460050};
      private final StarField stars = new StarField(451, 1000, -6.0F);
      private final float[] tmp = new float[3];

      Nebula() {
         super("nebulacloud", -15199696, -2068225);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 14.0F, 709503600);
         g.push();
         g.rotY((float)(t * 0.2));
         this.stars.draw(g, c.count(1000, c.stars), 0.9F, 1.0F, 0.55F, 0, 0.0F);
         float cAz = 32.0F;
         float cEl = 40.0F;
         float b1 = 0.82F + 0.18F * G.sin(t * 0.21);
         float b2 = 0.82F + 0.18F * G.sin(t * 0.17 + 2.0);
         float b3 = 0.8F + 0.2F * G.sin(t * 0.27 + 4.0);
         g.cap(SkyTex.NEBULA2, cAz - 6.0F, cEl - 4.0F, 74.0F, (float)(t * 0.5), 0.9F, G.fade(1883908784, b2), 0.35F);
         g.cap(SkyTex.NEBULA1, cAz, cEl, 56.0F, (float)(t * 0.9), 1.0F, G.fade(-6669057, b1), 0.4F);
         g.cap(SkyTex.NEBULA1, cAz - 3.0F, cEl + 1.0F, 50.0F, (float)(-t * 0.5 + 77.0), 1.15F, G.fade(-793752321, b3), 0.4F);
         g.cap(SkyTex.NEBULA2, cAz + 4.0F, cEl + 2.0F, 50.0F, (float)(-t * 0.7 + 40.0), 1.25F, G.fade(-45112, b2), 0.42F);
         g.cap(SkyTex.NEBULA2, cAz + 6.0F, cEl + 3.0F, 40.0F, (float)(t * 0.8 + 160.0), 1.4F, G.fade(-520133928, b1), 0.4F);
         g.cap(SkyTex.NEBULA1, cAz + 8.0F, cEl + 4.0F, 34.0F, (float)(t * 1.4 + 120.0), 1.6F, G.fade(-520123672, b3), 0.38F);
         g.sprite(SkyTex.GLOW, cAz + 6.0F, cEl + 3.0F, 38.0F, 0.0F, G.fade(1625317631, b2));
         g.cap(SkyTex.NEBULA2, cAz - 14.0F, cEl - 6.0F, 32.0F, (float)(-t * 1.1), 1.8F, G.fade(-1604682753, b1), 0.3F);
         g.cap(SkyTex.NEBULA1, cAz + 2.0F, cEl - 2.0F, 24.0F, (float)(t * 0.6 + 200.0), 2.4F, 1476723216, 0.4F);

         for (int i = 0; i < 7; i++) {
            float kAz = cAz - 14.0F + 30.0F * G.rnd(461, i);
            float kEl = cEl - 12.0F + 24.0F * G.rnd(462, i);
            float k = 0.55F + 0.45F * G.sin(t * (0.3 + 0.3 * G.rnd(463, i)) + i * 1.9);
            int col = G.mix(-25872, -3628801, G.rnd(464, i));
            float s = 5.0F + 9.0F * G.rnd(465, i);
            g.sprite(SkyTex.GLOW, kAz, kEl, s, 0.0F, G.fade(col, 0.35F * k));
         }

         for (int i = 0; i < 9; i++) {
            float sAz = cAz - 18.0F + 36.0F * G.rnd(471, i);
            float sEl = cEl - 14.0F + 28.0F * G.rnd(472, i);
            float tw = 0.6F + 0.4F * G.sin(t * (1.2 + G.rnd(473, i)) + i * 2.3);
            g.sprite(SkyTex.STAR, sAz, sEl, 1.6F + 2.0F * G.rnd(474, i), (float)(t * 3.0 % 90.0), G.fade(-3841, tw));
         }

         g.pop();
         g.ring(SkyTex.NEBULA2, (float)(t * 0.4 + 160.0), 6.0F, 48.0F, 0.1F, 0.6F, 813710528, 0.35F, 0.4F);
         this.stars.drawSparkles(g, c.count(10, c.stars), 0.8F, 1.0F, -727297);
         Fx.shootingStars(g, 1, 13.0F, 0.5F, -5889, 61, this.tmp);
      }
   }

   static final class NebulaStorm extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{459791, 1444914, 3416154, 2890330, 2232908, 1707584, 1313332};
      private final StarField stars = new StarField(401, 700, -6.0F);

      NebulaStorm() {
         super("nebula", -14019000, -5215233);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(700, c.stars), 0.8F, 1.0F, 0.5F, 0, 0.0F);
         float b1 = 0.75F + 0.25F * G.sin(t * 0.23);
         float b2 = 0.75F + 0.25F * G.sin(t * 0.19 + 2.0);
         float b3 = 0.75F + 0.25F * G.sin(t * 0.29 + 4.0);
         g.ring(SkyTex.NEBULA1, (float)(t * 1.4), 0.0F, 72.0F, 0.05F, 0.95F, G.fade(-527807233, b1), 0.2F, 0.3F);
         g.ring(SkyTex.NEBULA2, (float)(-t * 1.0 + 120.0), 4.0F, 78.0F, 0.0F, 1.0F, G.fade(-1057005604, b2), 0.2F, 0.3F);
         g.ring(SkyTex.NEBULA1, (float)(t * 0.6 + 200.0), -2.0F, 46.0F, 0.5F, 1.0F, G.fade(-1605600257, b3), 0.25F, 0.35F);
         g.cap(SkyTex.NEBULA2, 0.0F, 90.0F, 55.0F, (float)(t * 2.4), 1.0F, G.fade(-1064678145, b2), 0.45F);
         g.cap(SkyTex.NEBULA1, 0.0F, 90.0F, 50.0F, (float)(-t * 1.7), 1.3F, G.fade(-1862305048, b1), 0.4F);
         float f = G.fract(t / 3.3);
         int k = (int)Math.floor(t / 3.3);
         if (f < 0.1F && G.rnd(481, k) < 0.6F) {
            float ff = G.sin(f / 0.1F * Math.PI);
            g.sprite(SkyTex.GLOW, 360.0F * G.rnd(482, k), 20.0F + 40.0F * G.rnd(483, k), 45.0F, 0.0F, G.fade(-1525505, ff * 0.3F));
         }

         this.stars.drawSparkles(g, c.count(10, c.stars), 0.8F, 1.0F, -727297);
      }
   }

   static final class Saturn extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 8.0F, 25.0F, 55.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{329230, 724254, 1711416, 1382458, 921648, 658212, 460826};
      private final StarField stars = new StarField(101, 1000, -6.0F);
      private final float[] p = new float[3];
      private final float[] tmp = new float[3];

      Saturn() {
         super("saturn", -15065800, -1518938);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 16.0F, 808074368);
         g.push();
         g.rotY((float)(t * 0.35));
         g.band(SkyTex.NEBULA2, -40.0F, 110.0F, 10.0F, 75.0F, 0.08F, 0.55F, 0.1F, 0.9F, 1517966800, 0.3F, 0.35F, 0.3F);
         g.band(SkyTex.NEBULA1, 120.0F, 230.0F, 5.0F, 50.0F, 0.6F, 0.95F, 0.2F, 0.8F, 977955552, 0.3F, 0.4F, 0.3F);
         this.stars.draw(g, c.count(1000, c.stars), 0.95F, 1.0F, 0.55F, 0, 0.0F);
         this.stars.drawSparkles(g, c.count(8, c.stars), 0.6F, 1.0F, -1);
         g.pop();
         float az = 28.0F + 2.0F * G.sin(t * 0.05);
         float el = 30.0F + 1.2F * G.sin(t * 0.07 + 1.0);
         float orbit = (float)(t * 0.045);
         float mAz = az + 27.0F * G.cos(orbit);
         float mEl = el - 5.0F + 7.0F * G.sin(orbit);
         boolean front = G.sin(orbit) < 0.0F;
         if (!front) {
            moon(g, mAz, mEl);
         }

         Fx.planetWithRing(g, az, el, 17.0F, -20.0F, 18.0F, this.p);
         if (front) {
            moon(g, mAz, mEl);
         }

         Fx.shootingStars(g, 1, 14.0F, 0.5F, -1, 41, this.tmp);
      }

      private static void moon(G g, float az, float el) {
         g.sprite(SkyTex.GLOW, az, el, 6.0F, 0.0F, 819513520);
         g.sprite(SkyTex.DISC, az, el, 1.6F, 0.0F, -2504274);
         g.sprite(SkyTex.DISC, az - 0.25F, el + 0.2F, 1.5F, 0.0F, 1881151504);
      }
   }

   static final class StarTrails extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -8.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{197900, 462111, 1385535, 1056054, 726315, 528418, 396314};
      private static final int TRAILS = 300;
      private static final float POLE_AZ = 0.0F;
      private static final float POLE_EL = 40.0F;
      private final float[] radius = new float[300];
      private final float[] phase = new float[300];
      private final float[] length = new float[300];
      private final float[] bright = new float[300];
      private final float[] width = new float[300];
      private final int[] tint = new int[300];
      private final float[] pole = new float[3];
      private final float[] b = new float[6];
      private final float[] pa = new float[3];
      private final float[] pb = new float[3];
      private final StarField stars = new StarField(601, 120, -6.0F);

      StarTrails() {
         super("startrails", -15523268, -6503681);

         for (int i = 0; i < 300; i++) {
            this.radius[i] = 1.5F + 88.0F * (float)Math.sqrt(G.rnd(611, i));
            this.phase[i] = 360.0F * G.rnd(612, i);
            float k = G.rnd(613, i);
            this.bright[i] = 0.35F + 0.65F * k * k;
            this.length[i] = 30.0F + 55.0F * G.rnd(614, i);
            this.width[i] = 0.2F + 0.22F * this.bright[i];
            float c = G.rnd(615, i);
            this.tint[i] = c < 0.25F ? -4666113 : (c < 0.4F ? -7486 : (c < 0.48F ? -2571009 : -854273));
         }

         G.dir(0.0F, 40.0F, this.pole);
         SpaceSkies.basis(this.pole, this.b);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 20.0F, 809130650);
         g.band(SkyTex.MILKYWAY, -60.0F, 60.0F, 12.0F, 70.0F, 0.1F, 0.9F, 0.2F, 0.8F, 540691056, 0.4F, 0.4F, 0.4F);
         this.stars.draw(g, c.count(120, c.stars), 0.45F, 0.9F, 0.4F, 0, 0.0F);
         float turn = (float)(t * 2.4);

         for (int i = 0; i < 300; i++) {
            float r = this.radius[i];
            float arcLen = this.length[i];
            float head = this.phase[i] + turn;
            float visible = arcLen * G.sin(G.rad(Math.min(90.0F, r)));
            int seg = Math.max(2, Math.min(16, Math.round(visible / 2.5F) + 2));
            float step = arcLen / seg;

            for (int s = 0; s < seg; s++) {
               float a0 = head - arcLen + s * step;
               float a1 = a0 + step;
               SpaceSkies.offset(this.pole, this.b, a0, r, this.pa);
               SpaceSkies.offset(this.pole, this.b, a1, r, this.pb);
               if (!(this.pa[1] < -0.05F) || !(this.pb[1] < -0.05F)) {
                  float f0 = (float)s / seg;
                  float f1 = (float)(s + 1) / seg;
                  float al0 = this.bright[i] * (float)Math.pow(f0, 1.4);
                  float al1 = this.bright[i] * (float)Math.pow(f1, 1.4);
                  g.segment(SkyTex.LINE, this.pa, this.pb, this.width[i], this.width[i], G.fade(this.tint[i], al0), G.fade(this.tint[i], al1), false);
               }
            }

            SpaceSkies.offset(this.pole, this.b, head, r, this.pa);
            if (this.pa[1] > -0.05F) {
               g.sprite(
                  SkyTex.DOT,
                  this.pa[0],
                  this.pa[1],
                  this.pa[2],
                  this.width[i] * 3.2F,
                  this.width[i] * 3.2F,
                  0.0F,
                  G.fade(this.tint[i], Math.min(1.0F, this.bright[i] * 1.3F)),
                  0.0F,
                  0.0F,
                  1.0F,
                  1.0F
               );
            }
         }

         float tw = 0.85F + 0.15F * G.sin(t * 1.7);
         g.sprite(SkyTex.GLOW, 0.0F, 40.0F, 7.0F, 0.0F, G.fade(-2132940545, tw));
         g.sprite(SkyTex.STAR, 0.0F, 40.0F, 3.4F, (float)(t * 4.0 % 90.0), G.fade(-251658241, tw));
      }
   }
}
