package dev.valence.perf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.valence.ValenceClient;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

public final class ModInstaller {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(Redirect.NORMAL).build();
   private static final String UA = "Valence Minecraft Mod/1.9.2 (one click Sodium install)";
   private static volatile ModInstaller.State state = ModInstaller.State.IDLE;
   private static volatile String file = "";

   private ModInstaller() {
   }

   public static ModInstaller.State state() {
      return state;
   }

   public static String installedFile() {
      return file;
   }

   public static boolean sodiumLoaded() {
      return FabricLoader.getInstance().isModLoaded("sodium");
   }

   public static void installSodium() {
      if (state != ModInstaller.State.WORKING && state != ModInstaller.State.DONE) {
         state = ModInstaller.State.WORKING;
         CompletableFuture.runAsync(
            () -> {
               try {
                  String mc = SharedConstants.getCurrentVersion().name();
                  String url = "https://api.modrinth.com/v2/project/sodium/version?loaders="
                     + URLEncoder.encode("[\"fabric\"]", StandardCharsets.UTF_8)
                     + "&game_versions="
                     + URLEncoder.encode("[\"" + mc + "\"]", StandardCharsets.UTF_8);
                  HttpResponse<String> res = HTTP.send(
                     HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(15L))
                        .header("User-Agent", "Valence Minecraft Mod/1.9.2 (one click Sodium install)")
                        .GET()
                        .build(),
                     BodyHandlers.ofString()
                  );
                  if (res.statusCode() != 200) {
                     throw new IllegalStateException("Modrinth answered " + res.statusCode());
                  }

                  JsonArray versions = JsonParser.parseString(res.body()).getAsJsonArray();
                  JsonObject pick = null;

                  for (JsonElement v : versions) {
                     JsonObject o = v.getAsJsonObject();
                     String type = o.has("version_type") ? o.get("version_type").getAsString() : "release";
                     if ("release".equals(type)) {
                        pick = o;
                        break;
                     }

                     if (pick == null) {
                        pick = o;
                     }
                  }

                  if (pick == null) {
                     throw new IllegalStateException("no Sodium version for " + mc);
                  }

                  JsonObject f = null;

                  for (JsonElement e : pick.getAsJsonArray("files")) {
                     JsonObject o = e.getAsJsonObject();
                     if (f == null || o.has("primary") && o.get("primary").getAsBoolean()) {
                        f = o;
                     }
                  }

                  if (f == null) {
                     throw new IllegalStateException("no file");
                  }

                  String name = f.get("filename").getAsString().replaceAll("[\\\\/:*?\"<>|]", "_");
                  String sha512 = f.getAsJsonObject("hashes").get("sha512").getAsString();
                  Path mods = FabricLoader.getInstance().getGameDir().resolve("mods");
                  Files.createDirectories(mods);
                  Path tmp = mods.resolve(name + ".download");
                  HttpResponse<InputStream> dl = HTTP.send(
                     HttpRequest.newBuilder(URI.create(f.get("url").getAsString()))
                        .timeout(Duration.ofSeconds(60L))
                        .header("User-Agent", "Valence Minecraft Mod/1.9.2 (one click Sodium install)")
                        .GET()
                        .build(),
                     BodyHandlers.ofInputStream()
                  );
                  MessageDigest md = MessageDigest.getInstance("SHA-512");

                  try (InputStream in = dl.body()) {
                     byte[] bytes = in.readAllBytes();
                     md.update(bytes);
                     Files.write(tmp, bytes);
                  }

                  String got = HexFormat.of().formatHex(md.digest());
                  if (!got.equalsIgnoreCase(sha512)) {
                     Files.deleteIfExists(tmp);
                     throw new IllegalStateException("download damaged (hash mismatch)");
                  }

                  Files.move(tmp, mods.resolve(name), StandardCopyOption.REPLACE_EXISTING);
                  file = name;
                  state = ModInstaller.State.DONE;
                  ValenceClient.LOGGER.info("Valence: installed {} into the mods folder (active after a restart)", name);
               } catch (Exception e) {
                  ValenceClient.LOGGER.warn("Valence: Sodium install failed: {}", e.toString());
                  state = ModInstaller.State.FAILED;
               }
            }
         );
      }
   }

   public enum State {
      IDLE,
      WORKING,
      DONE,
      FAILED;
   }
}
