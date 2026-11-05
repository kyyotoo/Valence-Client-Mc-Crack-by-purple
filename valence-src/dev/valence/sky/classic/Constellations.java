package dev.valence.sky.classic;

final class Constellations {
   private static final Constellations.Sign[] SIGNS = new Constellations.Sign[]{
      new Constellations.Sign(
         330.0F,
         52.0F,
         new float[][]{{0.0F, 0.0F}, {6.0F, 1.0F}, {11.0F, 0.0F}, {15.0F, -2.0F}, {16.0F, -8.0F}, {23.0F, -9.0F}, {22.0F, -2.0F}},
         new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 3}}
      ),
      new Constellations.Sign(
         160.0F,
         36.0F,
         new float[][]{{0.0F, 0.0F}, {10.0F, 1.0F}, {3.0F, -7.0F}, {5.0F, -7.5F}, {7.0F, -8.0F}, {1.0F, -15.0F}, {10.0F, -14.0F}, {5.0F, 4.0F}},
         new int[][]{{0, 7}, {7, 1}, {0, 2}, {1, 4}, {2, 3}, {3, 4}, {2, 5}, {4, 6}}
      ),
      new Constellations.Sign(
         45.0F, 62.0F, new float[][]{{0.0F, 0.0F}, {4.0F, -4.0F}, {8.0F, -1.0F}, {12.0F, -5.0F}, {16.0F, 0.0F}}, new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}}
      ),
      new Constellations.Sign(
         245.0F, 50.0F, new float[][]{{0.0F, 8.0F}, {0.0F, 0.0F}, {0.0F, -10.0F}, {-8.0F, 1.0F}, {8.0F, -1.0F}}, new int[][]{{0, 1}, {1, 2}, {3, 1}, {1, 4}}
      ),
      new Constellations.Sign(
         95.0F,
         44.0F,
         new float[][]{{0.0F, 0.0F}, {2.0F, -3.0F}, {5.0F, -3.0F}, {4.0F, -7.0F}, {1.0F, -7.0F}},
         new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 1}}
      )
   };
   private static final float[] A = new float[3];
   private static final float[] B = new float[3];

   private Constellations() {
   }

   static void draw(G g, SkyCtx c, float alpha) {
      if (!(alpha <= 0.01F)) {
         double t = c.time;
         g.push();
         g.rotY((float)(t * 0.2));

         for (int s = 0; s < SIGNS.length; s++) {
            Constellations.Sign sign = SIGNS[s];
            float pulse = 0.55F + 0.45F * G.sin(t * 0.09 + s * 1.7);
            float la = alpha * pulse;
            float ce = (float)Math.cos(Math.toRadians(sign.el));

            for (int[] l : sign.lines) {
               dir(sign, ce, sign.stars[l[0]], A);
               dir(sign, ce, sign.stars[l[1]], B);
               g.segment(SkyTex.LINE, A, B, 0.16F, 0.16F, G.fade(-4205313, la * 0.5F), G.fade(-4205313, la * 0.5F), false);
            }

            for (int i = 0; i < sign.stars.length; i++) {
               float[] p = sign.stars[i];
               float az = sign.az + p[0] / Math.max(0.2F, ce);
               float el = sign.el + p[1];
               float tw = 0.85F + 0.15F * G.sin(t * (1.3 + 0.2 * i) + i * 2.1 + s);
               g.sprite(SkyTex.GLOW, az, el, 2.4F, 0.0F, G.fade(-3153665, alpha * 0.35F * tw));
               g.sprite(SkyTex.DOT, az, el, 0.75F, 0.0F, G.fade(-1, alpha * tw));
            }
         }

         g.pop();
      }
   }

   private static void dir(Constellations.Sign sign, float ce, float[] p, float[] out) {
      G.dir(sign.az + p[0] / Math.max(0.2F, ce), sign.el + p[1], out);
   }

   private record Sign(float az, float el, float[][] stars, int[][] lines) {
   }
}
