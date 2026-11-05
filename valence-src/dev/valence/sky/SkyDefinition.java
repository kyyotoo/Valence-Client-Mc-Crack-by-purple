package dev.valence.sky;

import dev.valence.sky.classic.Sky;
import java.util.Map;

public final class SkyDefinition {
   public final String id;
   public final Sky sky;
   public final float[] view;
   private static final Map<String, float[]> VIEWS = Map.ofEntries(
      Map.entry("saturn", new float[]{28.0F, 28.0F}),
      Map.entry("aurora", new float[]{0.0F, 20.0F}),
      Map.entry("galaxy", new float[]{0.0F, 25.0F}),
      Map.entry("nebula", new float[]{90.0F, 30.0F}),
      Map.entry("nebulacloud", new float[]{30.0F, 30.0F}),
      Map.entry("meteors", new float[]{60.0F, 30.0F}),
      Map.entry("startrails", new float[]{0.0F, 35.0F}),
      Map.entry("comet", new float[]{-12.0F, 25.0F}),
      Map.entry("hyperspace", new float[]{0.0F, 15.0F}),
      Map.entry("sunset", new float[]{0.0F, 10.0F}),
      Map.entry("golden_sunset", new float[]{0.0F, 12.0F}),
      Map.entry("pink_sunset", new float[]{0.0F, 10.0F}),
      Map.entry("blue_sun", new float[]{0.0F, 28.0F}),
      Map.entry("white_sun", new float[]{0.0F, 25.0F}),
      Map.entry("heavengate", new float[]{0.0F, 35.0F}),
      Map.entry("desert", new float[]{-12.0F, 15.0F}),
      Map.entry("winter", new float[]{-20.0F, 20.0F}),
      Map.entry("neon", new float[]{-10.0F, 10.0F})
   );

   SkyDefinition(Sky sky) {
      this.id = sky.id;
      this.sky = sky;
      this.view = VIEWS.getOrDefault(sky.id, new float[]{0.0F, 25.0F});
   }

   public int fogColor() {
      return this.sky.fogColor & 16777215;
   }

   public boolean starry() {
      return this.sky.isStarry();
   }

   public static float[] rgb(int rgb) {
      return new float[]{(rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F};
   }
}
