package dev.valence.sky;

import dev.valence.config.ValenceConfig;
import dev.valence.sky.classic.Skies;
import dev.valence.sky.classic.Sky;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class SkyRegistry {
   private static final List<SkyDefinition> SKIES = new ArrayList<>();
   private static final Map<String, String> OLD_IDS = Map.ofEntries(
      Map.entry("aurora_green", "aurora"),
      Map.entry("end_beams", "hyperspace"),
      Map.entry("caustics", "deepsea"),
      Map.entry("meteor_shower", "meteors"),
      Map.entry("star_trails", "startrails"),
      Map.entry("thunderstorm", "storm"),
      Map.entry("solar_eclipse", "comet"),
      Map.entry("neon_horizon", "neon"),
      Map.entry("winter_night", "winter"),
      Map.entry("moonlit", "winter"),
      Map.entry("firefly_dusk", "fireflies"),
      Map.entry("cosmic_storm", "nebula"),
      Map.entry("bloodmoon", "sunset"),
      Map.entry("blood_moon", "sunset"),
      Map.entry("twosuns", "golden_sunset")
   );

   private SkyRegistry() {
   }

   public static void load() {
      SKIES.clear();

      for (Sky s : Skies.ALL) {
         SKIES.add(new SkyDefinition(s));
      }

      ValenceConfig cfg = ValenceConfig.get();
      String mapped = OLD_IDS.get(cfg.skyId);
      if (mapped != null) {
         cfg.skyId = mapped;
         ValenceConfig.save();
      }
   }

   public static List<SkyDefinition> all() {
      return Collections.unmodifiableList(SKIES);
   }

   public static SkyDefinition byId(String id) {
      for (SkyDefinition s : SKIES) {
         if (s.id.equals(id)) {
            return s;
         }
      }

      return null;
   }

   public static SkyDefinition active() {
      ValenceConfig cfg = ValenceConfig.get();
      if (!cfg.skyEnabled) {
         return null;
      }

      SkyDefinition s = byId(cfg.skyId);
      if (s == null && !SKIES.isEmpty()) {
         s = SKIES.get(0);
      }

      return s;
   }
}
