package dev.valence.sky.classic;

final class DaySkies {
   private DaySkies() {
   }

   static final class CottonCandy extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{14199000, 15382754, 16173288, 15252704, 14071000, 13021392, 12365002};
      private final StarField sparkles = new StarField(2101, 50, 10.0F);

      CottonCandy() {
         super("cottoncandy", -731410, -18202);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.cap(SkyTex.NEBULA1, 0.0F, 90.0F, 70.0F, (float)(t * 1.0), 1.0F, -1862998284, 0.5F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.45 + 100.0), 18.0F, 64.0F, 0.0F, 1.0F, -1056972558, 0.2F, 0.35F);
         g.ring(SkyTex.NEBULA2, (float)(t * 0.3 + 40.0), 4.0F, 34.0F, 0.4F, 0.9F, 1892198640, 0.3F, 0.4F);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.7), 0.0F, 34.0F, 0.0F, 1.0F, -654314758, 0.15F, 0.3F);
         this.sparkles.drawSparkles(g, c.count(36, c.particles), 1.0F, 1.1F, -1);
      }
   }

   static final class Desert extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 6.0F, 20.0F, 45.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{8014378, 13140030, 16171130, 15773802, 14719573, 13666888, 12745280};

      Desert() {
         super("desert", -869768, -18330);
         this.fadeTop = 7.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.sprite(SkyTex.GLOW, -12.0F, 10.0F, 80.0F, 0.0F, -1862281072);
         g.sprite(SkyTex.GLOW, -12.0F, 10.0F, 26.0F, 0.0F, -788533056);
         g.sprite(SkyTex.GLOW, -12.0F, 10.0F, 13.0F, 0.0F, -520096552);
         g.sprite(SkyTex.DISC, -12.0F, 10.0F, 5.6F, 0.0F, -1824);
         g.ring(SkyTex.NEBULA1, (float)(t * 0.6), 20.0F, 78.0F, 0.0F, 1.0F, -2136450520, 0.3F, 0.35F);
         g.ring(SkyTex.NEBULA2, (float)(-t * 0.9 + 50.0), 3.0F, 52.0F, 0.0F, 1.0F, -1593842512, 0.25F, 0.35F);
         g.ring(SkyTex.NEBULA2, (float)(t * 1.5), -2.0F, 26.0F, 0.2F, 0.9F, -1330091986, 0.15F, 0.35F);
         int n = c.count(80, c.particles);

         for (int i = 0; i < n; i++) {
            float az = 360.0F * G.rnd(131, i) + (float)(t * (1.5 + 3.0 * G.rnd(132, i)));
            float el = 1.0F + 40.0F * G.rnd(133, i) * G.rnd(134, i) + 1.5F * G.sin(t * 0.5 + i);
            float a = 0.3F + 0.3F * G.sin(t * 0.8 + i * 1.7);
            g.sprite(SkyTex.DOT, az, el, 0.4F + 0.4F * G.rnd(135, i), 0.0F, G.fade(-5952, a));
         }
      }
   }

   static final class HeavenGate extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 10.0F, 30.0F, 60.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{11046494, 14204032, 16180408, 15652766, 14993542, 14202996, 13413480};
      private final StarField sparkles = new StarField(2201, 30, 15.0F);

      HeavenGate() {
         super("heavengate", -596808, -5984);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         float lightEl = 42.0F;
         g.ring(SkyTex.CLOUDS, (float)(t * 0.5), -2.0F, 34.0F, 0.0F, 1.0F, -1868527520, 0.15F, 0.3F);
         g.cap(SkyTex.NEBULA1, 0.0F, lightEl, 80.0F, (float)(t * 1.6), 0.8F, -1868001168, 0.45F);
         g.sprite(SkyTex.GLOW, 0.0F, lightEl, 80.0F, 0.0F, -1862273320);
         float spin = (float)(t * 1.0);
         g.sprite(SkyTex.RAYS, 0.0F, lightEl, 165.0F, spin, -520094742);
         g.sprite(SkyTex.RAYS, 0.0F, lightEl, 130.0F, -spin * 0.7F + 17.0F, -1325400065);
         g.cap(SkyTex.NEBULA2, 0.0F, lightEl, 66.0F, (float)(-t * 1.3), 1.1F, -1056965906, 0.4F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.35 + 90.0), 4.0F, 26.0F, 0.2F, 0.9F, -1593837336, 0.2F, 0.35F);
         g.sprite(SkyTex.GLOW, 0.0F, lightEl, 26.0F, 0.0F, -251658241);
         g.sprite(SkyTex.GLOW, 0.0F, lightEl, 9.0F, 0.0F, -1);
         this.sparkles.drawSparkles(g, c.count(16, c.particles), 0.9F, 1.3F, -1);
      }
   }

   static final class Sakura extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 15.0F, 40.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{15255766, 16047334, 16575216, 16178151, 15716576, 15124184};
      private final float[] d = new float[3];

      Sakura() {
         super("sakura", -268562, -18481);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.sprite(SkyTex.GLOW, 40.0F, 50.0F, 70.0F, 0.0F, 1358954495);
         g.cap(SkyTex.NEBULA1, 0.0F, 90.0F, 72.0F, (float)(t * 0.8), 1.0F, 1895825407, 0.5F);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.5), 14.0F, 66.0F, 0.0F, 1.0F, -1862273800, 0.2F, 0.35F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.3 + 70.0), 0.0F, 26.0F, 0.2F, 0.9F, 1895817452, 0.2F, 0.3F);
         int n = c.count(130, c.particles);

         for (int i = 0; i < n; i++) {
            float depth = G.rnd(121, i);
            float sp = 0.01F + 0.028F * depth;
            float q = G.fract(t * sp + G.rnd(122, i));
            float el = 76.0F - q * 88.0F;
            float az = 360.0F * G.rnd(123, i) + 9.0F * G.sin(t * 0.25 + i * 1.9) + q * 25.0F;
            float a = G.smooth(0.0F, 0.1F, q) * G.smooth(1.0F, 0.85F, q);
            float size = 1.2F + 1.9F * depth;
            float flip = 0.45F + 0.55F * Math.abs(G.sin(t * (1.2 + depth) + i));
            int col = G.mix(-31056, -15142, G.rnd(124, i));
            G.dir(az, el, this.d);
            g.sprite(
               SkyTex.PETAL,
               this.d[0],
               this.d[1],
               this.d[2],
               size * flip,
               size,
               (float)(t * 40.0 * (G.rnd(125, i) - 0.5F) + i * 37),
               G.fade(col, a),
               0.0F,
               0.0F,
               1.0F,
               1.0F
            );
         }
      }
   }

   static final class Sunset extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 3.0F, 10.0F, 25.0F, 50.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{3807786, 9058874, 16773304, 16765562, 16752742, 16743050, 15756443, 14183080};

      Sunset() {
         super("sunset", -14192, -26006);
         this.fadeFull = 0.0F;
         this.fadeTop = 5.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 220.0F, -3.0F, 16.0F, -1593839440);
         g.sprite(SkyTex.GLOW, 0.0F, 3.0F, 100.0F, 0.0F, -2130714464);
         float spin = (float)(t * 1.4);
         g.sprite(SkyTex.RAYS, 0.0F, 3.0F, 118.0F, spin, -1862273840);
         g.sprite(SkyTex.RAYS, 0.0F, 3.0F, 96.0F, -spin * 0.7F + 20.0F, 1627389951);
         g.sprite(SkyTex.GLOW, 0.0F, 3.0F, 36.0F, 0.0F, -1056967480);
         g.sprite(SkyTex.GLOW, 0.0F, 3.0F, 15.0F, 0.0F, -251661616);
         g.sprite(SkyTex.DISC, 0.0F, 2.6F, 6.6F, 0.0F, -12);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.6), 1.0F, 13.0F, 0.3F, 0.8F, -1325430150, 0.2F, 0.3F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.4 + 140.0), 14.0F, 36.0F, 0.2F, 0.7F, -2130732360, 0.25F, 0.35F);
         Fx.birds(g, 4.0F, 18.0F, t);
      }
   }
}
