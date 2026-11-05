package dev.valence.sky.classic;

import java.util.List;

public final class Skies {
   public static final List<Sky> ALL = List.of(
      new SpaceSkies.Saturn().starry(),
      new NightSkies.Aurora().starry(),
      new SpaceSkies.GalaxyCore().starry(),
      new SpaceSkies.NebulaStorm().starry(),
      new SpaceSkies.Nebula().starry(),
      new SpaceSkies.Meteors().starry(),
      new SpaceSkies.StarTrails(),
      new SpaceSkies.Comet().starry(),
      new SpaceSkies.Hyperspace(),
      new DaySkies.Sunset(),
      new BrightSkies.GoldenSunset(),
      new BrightSkies.PinkSunset(),
      new BrightSkies.BlueSun(),
      new BrightSkies.WhiteSun(),
      new DaySkies.HeavenGate(),
      new DaySkies.CottonCandy(),
      new DaySkies.Sakura(),
      new DaySkies.Desert(),
      new NightSkies.Winter().starry(),
      new NightSkies.Amethyst().starry(),
      new NightSkies.Crystal(),
      new NightSkies.Neon(),
      new NightSkies.DeepSea(),
      new NightSkies.Fireflies(),
      new NightSkies.Storm()
   );

   private Skies() {
   }

   public static Sky byId(String id) {
      if (id == null) {
         return null;
      }

      for (Sky s : ALL) {
         if (s.id.equals(id)) {
            return s;
         }
      }

      return null;
   }
}
