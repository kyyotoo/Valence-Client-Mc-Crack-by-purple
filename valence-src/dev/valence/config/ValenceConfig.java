package dev.valence.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.valence.ValenceClient;
import dev.valence.cord.NeonLyrics;
import dev.valence.gui.UiSounds;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public class ValenceConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int VERSION = 4;
   private static ValenceConfig instance = new ValenceConfig();
   private static int revision = 0;
   public int configVersion = 0;
   public boolean hudLogo = false;
   public boolean hudLogoDefaultOff = false;
   public float hudLogoScale = 1.0F;
   public boolean mouseGlow = true;
   public boolean menuAnimations = true;
   public boolean menuBlur = true;
   public boolean uiSounds = true;
   public float uiSoundVolume = 0.2F;
   public String uiSoundPack = "soft";
   public boolean skyEnabled = true;
   public String skyRenderMode = "auto";
   public String skyQuality = "auto";
   public String skyId = "aurora";
   public float skyAnimationSpeed = 1.0F;
   public float skyBrightness = 1.0F;
   public boolean skyStars = true;
   public boolean skyShootingStars = true;
   public boolean skyConstellations = true;
   public boolean skyParticles = true;
   public boolean skyLightning = true;
   public boolean skyAnimations = true;
   public boolean skyHideClouds = true;
   public boolean skyRotation = true;
   public boolean skyInEnd = false;
   public float skyFogTint = 0.75F;
   public boolean skyDimInRain = true;
   public Map<String, String> styles = new LinkedHashMap<>();
   public String styleTotemSize = "normal";
   public float fireHeight = 0.35F;
   public int totemPop = 0;
   public boolean lyricsEnabled = true;
   public String lyricsMode = "sky";
   public String lyricsPreset = "lavender";
   public String lyricsFont = "poppins_bold";
   public int lyricsColorA = -3693825;
   public int lyricsColorB = -1;
   public String lyricsColorMode = "gradient";
   public float lyricsGlow = 0.0F;
   public boolean lyricsGlowDefaultOff = false;
   public boolean lyricsOutline = false;
   public int lyricsOutlineColor = -15069136;
   public boolean lyricsUppercase = false;
   public float lyricsScale = 1.0F;
   public float lyricsElevation = 22.0F;
   public float lyricsTilt = 0.6F;
   public String lyricsAnimation = "word";
   public float lyricsHoldSeconds = 2.5F;
   public boolean lyricsShowTitleWithoutLyrics = true;
   public boolean maxFpsMode = false;
   public String maxFpsSaved = "";
   public float maxFpsLyricGlow = 0.0F;
   public boolean perfCulling = true;
   public boolean perfBackgroundFps = true;

   public static ValenceConfig get() {
      return instance;
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("valence.json");
   }

   private static void migrateLegacy() {
      Path dir = FabricLoader.getInstance().getConfigDir();
      Path oldFile = dir.resolve("valance.json");

      try {
         if (Files.exists(oldFile) && !Files.exists(path())) {
            Files.copy(oldFile, path());
         }

         Path oldPacks = dir.resolve("valance").resolve("packs");
         Path newPacks = dir.resolve("valence").resolve("packs");
         if (Files.isDirectory(oldPacks)) {
            Files.createDirectories(newPacks);

            try (Stream<Path> list = Files.list(oldPacks)) {
               for (Path f : list::iterator) {
                  Path target = newPacks.resolve(f.getFileName().toString());
                  if (!Files.exists(target)) {
                     Files.move(f, target);
                  }
               }
            }
         }
      } catch (Exception e) {
         ValenceClient.LOGGER.warn("Could not take over the old settings", e);
      }
   }

   public static void load() {
      migrateLegacy();
      Path p = path();
      if (Files.exists(p)) {
         try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            ValenceConfig loaded = (ValenceConfig)GSON.fromJson(r, ValenceConfig.class);
            if (loaded != null) {
               instance = loaded;
            }
         } catch (Exception e) {
            ValenceClient.LOGGER.warn("Could not read valence.json, using defaults", e);
         }
      }

      if (instance.styles == null) {
         instance.styles = new LinkedHashMap<>();
      }

      instance.styles.replaceAll((k, v) -> (String)(!"galaxy".equals(v) && !"d:galaxy".equals(v) ? v : "vanilla"));
      if (instance.configVersion < 4) {
         instance.uiSoundPack = "soft";
         instance.uiSoundVolume = 0.2F;
      }

      if (!UiSounds.PACKS.contains(instance.uiSoundPack)) {
         instance.uiSoundPack = "soft";
      }

      instance.configVersion = 4;
      save();
   }

   public static int revision() {
      return revision;
   }

   public static void save() {
      revision++;
      NeonLyrics.saveConfig();

      try {
         Path p = path();
         Files.createDirectories(p.getParent());

         try (Writer w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            GSON.toJson(instance, w);
         }
      } catch (Exception e) {
         ValenceClient.LOGGER.warn("Could not save valence.json", e);
      }
   }
}
