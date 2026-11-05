package dev.valence.sky.classic;

final class BrightSkies {
   private BrightSkies() {
   }

   static final class BlueSun extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 4.0F, 15.0F, 40.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{2109512, 7245488, 14873343, 12377855, 8962298, 5938416, 3833054};

      BlueSun() {
         super("blue_sun", -4399873, -8600321);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         float sunEl = 36.0F;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 360.0F, -3.0F, 14.0F, 1895825407);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.45), 16.0F, 60.0F, 0.0F, 1.0F, -1862270977, 0.2F, 0.35F);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 90.0F, 0.0F, 1890114815);
         float spin = (float)(t * 0.9);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 110.0F, spin, 1357313791);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 84.0F, -spin * 0.8F + 30.0F, 1090519039);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 30.0F, 0.0F, -1059654913);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 11.0F, 0.0F, -251658241);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 16.0F, 0.0F, -1);
         g.sprite(SkyTex.DISC, 0.0F, sunEl, 6.0F, 0.0F, -1);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.3 + 90.0), 0.0F, 22.0F, 0.2F, 0.9F, -922746881, 0.15F, 0.3F);
      }
   }

   static final class GoldenSunset extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 3.0F, 10.0F, 25.0F, 50.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{3810328, 9067056, 16772016, 16765822, 16758882, 16030828, 12221596, 7101598};

      GoldenSunset() {
         super("golden_sunset", -868742, -14224);
         this.fadeFull = 0.0F;
         this.fadeTop = 5.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         float sunEl = 6.0F;
         g.dome(DOME_EL, DOME_COL);
         g.horizonGlow(0.0F, 240.0F, -3.0F, 18.0F, -1593851792);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 110.0F, 0.0F, -2130720640);
         float spin = (float)(t * 1.2);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 128.0F, spin, -1862279008);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 100.0F, -spin * 0.7F + 25.0F, 1627389951);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 34.0F, 0.0F, -922752848);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 12.0F, 0.0F, -251660580);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 18.0F, 0.0F, -2864);
         g.sprite(SkyTex.DISC, 0.0F, sunEl, 7.0F, 0.0F, -1046);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.5), 2.0F, 15.0F, 0.3F, 0.8F, -1325415286, 0.2F, 0.3F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.35 + 120.0), 15.0F, 40.0F, 0.2F, 0.7F, -2130717016, 0.25F, 0.35F);
         Fx.birds(g, -6.0F, 16.0F, t);
      }
   }

   static final class PinkSunset extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 3.0F, 10.0F, 25.0F, 50.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{2757680, 7223902, 16766668, 16757948, 16749238, 14840506, 10247354, 4602510};
      private final StarField stars = new StarField(2301, 160, 40.0F);

      PinkSunset() {
         super("pink_sunset", -1533264, -29504);
         this.fadeFull = 0.0F;
         this.fadeTop = 5.0F;
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         float sunEl = 3.5F;
         g.dome(DOME_EL, DOME_COL);
         this.stars.draw(g, c.count(160, c.stars), 0.55F, 0.9F, 0.6F, 0, 0.0F);
         g.horizonGlow(0.0F, 230.0F, -3.0F, 16.0F, -1593855808);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 104.0F, 0.0F, -2130728760);
         float spin = (float)(t * 1.3);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 120.0F, spin, -2130720548);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 96.0F, -spin * 0.7F + 15.0F, 1493172223);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 34.0F, 0.0F, -1056974620);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 16.0F, 0.0F, -251664658);
         g.sprite(SkyTex.DISC, 0.0F, sunEl - 0.4F, 6.8F, 0.0F, -3338);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.55), 1.0F, 13.0F, 0.3F, 0.8F, -1325424956, 0.2F, 0.3F);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.4 + 60.0), 14.0F, 38.0F, 0.2F, 0.7F, -1997494048, 0.25F, 0.35F);
         Fx.birds(g, 8.0F, 17.0F, t);
      }
   }

   static final class WhiteSun extends Sky {
      private static final float[] DOME_EL = new float[]{-90.0F, -5.0F, 0.0F, 5.0F, 20.0F, 50.0F, 90.0F};
      private static final int[] DOME_COL = new int[]{4998752, 11051712, 16512767, 15130874, 13156594, 11050724, 9076950};
      private final StarField sparkles = new StarField(2401, 40, 15.0F);

      WhiteSun() {
         super("white_sun", -1186049, -1);
      }

      @Override
      public void render(G g, SkyCtx c) {
         double t = c.time;
         float sunEl = 30.0F;
         g.dome(DOME_EL, DOME_COL);
         g.cap(SkyTex.NEBULA1, 0.0F, 90.0F, 70.0F, (float)(t * 0.8), 1.0F, 1090519039, 0.5F);
         g.ring(SkyTex.CLOUDS, (float)(t * 0.4), 14.0F, 58.0F, 0.0F, 1.0F, -1862270977, 0.2F, 0.35F);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 90.0F, 0.0F, 1895825407);
         float spin = (float)(t * 0.8);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 140.0F, spin, 1626664703);
         g.sprite(SkyTex.RAYS, 0.0F, sunEl, 104.0F, -spin * 0.6F + 10.0F, 1358954495);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 40.0F, 0.0F, -788529153);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 14.0F, 0.0F, -1);
         g.sprite(SkyTex.GLOW, 0.0F, sunEl, 20.0F, 0.0F, -1);
         g.sprite(SkyTex.DISC, 0.0F, sunEl, 8.0F, 0.0F, -1);
         g.ring(SkyTex.CLOUDS, (float)(-t * 0.3 + 40.0), 0.0F, 24.0F, 0.2F, 0.9F, -1191182337, 0.15F, 0.3F);
         this.sparkles.drawSparkles(g, c.count(24, c.particles), 0.8F, 1.1F, -1);
      }
   }
}
