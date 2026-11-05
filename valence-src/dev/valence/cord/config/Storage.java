package dev.valence.cord.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.valence.cord.NeonLyrics;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public final class Storage {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

   private Storage() {
   }

   public static Path dir() {
      return FabricLoader.getInstance().getConfigDir().resolve("valence_cord");
   }

   public static void migrateFromLyricsCord() {
      try {
         Path old = FabricLoader.getInstance().getConfigDir().resolve("neonlyrics");
         if (!Files.isDirectory(old) || Files.exists(dir().resolve("cord.json"))) {
            return;
         }

         Files.createDirectories(dir());
         if (Files.exists(old.resolve("config.json"))) {
            Files.copy(old.resolve("config.json"), dir().resolve("cord.json"));
         }

         for (String f : new String[]{"spotify.json", "favorites.json"}) {
            Path src = old.resolve(f);
            if (Files.exists(src) && !Files.exists(dir().resolve(f))) {
               Files.copy(src, dir().resolve(f));
            }
         }

         NeonLyrics.LOG.info("Valence: took over the Lyrics Cord settings");
      } catch (Exception e) {
         NeonLyrics.LOG.warn("Valence: could not take over the Lyrics Cord settings", e);
      }
   }

   public static <T> T load(String file, Type type, Supplier<T> fallback) {
      Path p = dir().resolve(file);
      if (!Files.exists(p)) {
         return fallback.get();
      }

      try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
         T t = (T)GSON.fromJson(r, type);
         return t != null ? t : fallback.get();
      } catch (Exception var9) {
         NeonLyrics.LOG.warn("Konnte {} nicht laden, nutze Standardwerte", file, var9);
         return fallback.get();
      }
   }

   public static void save(String file, Object data) {
      try {
         Files.createDirectories(dir());
         Path p = dir().resolve(file);
         Path tmp = dir().resolve(file + ".tmp");

         try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            GSON.toJson(data, w);
         }

         Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception var9) {
         NeonLyrics.LOG.warn("Konnte {} nicht speichern", file, var9);
      }
   }
}
