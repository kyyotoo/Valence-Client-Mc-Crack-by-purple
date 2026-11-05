package dev.valence.cord;

import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.valence.ValenceClient;
import dev.valence.cord.config.Config;
import dev.valence.cord.config.Storage;
import dev.valence.cord.cosmetic.DeathFx;
import dev.valence.cord.hud.BeatGlow;
import dev.valence.cord.hud.Crosshair;
import dev.valence.cord.hud.GlassHotbar;
import dev.valence.cord.hud.Island;
import dev.valence.cord.hud.StartupIntro;
import dev.valence.cord.lyrics.Favorites;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.cord.sky.SkyRenderer;
import dev.valence.cord.sky.WorldSky;
import dev.valence.cord.util.AutoPerf;
import dev.valence.cord.util.Fx;
import dev.valence.cord.util.Profiles;
import dev.valence.license.License;
import dev.valence.lyrics.SkyLyrics;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;

public final class NeonLyrics {
   public static final String MOD_ID = "valence_cord";
   public static final Logger LOG = ValenceClient.LOGGER;
   private static Config config = new Config();
   public static KeyMapping keyLyrics;
   public static KeyMapping keySync;
   public static KeyMapping keyFav;
   public static KeyMapping keyClip;
   public static KeyMapping keyProfile;
   private static boolean loaded;

   private NeonLyrics() {
   }

   public static Config cfg() {
      return config;
   }

   public static void saveConfig() {
      if (loaded) {
         config.clamp();
         Storage.save("cord.json", config);
      }
   }

   public static void init() {
      Storage.migrateFromLyricsCord();
      config = Storage.load("cord.json", Config.class, Config::new);
      loaded = true;
      config.clamp();
      saveConfig();
      Category cat = ValenceClient.CATEGORY;
      keyFav = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.fav", Type.KEYBOARD, 78, cat));
      keyClip = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.clip", Type.KEYBOARD, 299, cat));
      keyProfile = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.profile", Type.KEYBOARD, 297, cat));
      keySync = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.sync", Type.KEYBOARD, -1, cat));
      keyLyrics = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.lyrics", Type.KEYBOARD, -1, cat));
      HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("valence_cord", "sky"), gated(SkyRenderer::render));
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("valence_cord", "island"), gated(Island::render));
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("valence_cord", "screen_lyrics"), gated((g, d) -> SkyLyrics.renderScreen(g)));
      HudElementRegistry.replaceElement(VanillaHudElements.HOTBAR, v -> gatedOr(GlassHotbar.wrap(v), v));
      HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, v -> gatedOr(Crosshair.wrap(v), v));
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("valence_cord", "beatglow"), gated(BeatGlow::render));
      LevelRenderEvents.END_MAIN.register(WorldSky::render);
      LevelRenderEvents.END_MAIN.register(WorldSky::renderLyrics);
      SpotifyClient.init();
      StartupIntro.register();
      AttackEntityCallback.EVENT.register((AttackEntityCallback)(player, level, hand, entity, hit) -> {
         DeathFx.onAttack(entity);
         return InteractionResult.PASS;
      });
      ClientTickEvents.END_CLIENT_TICK.register(NeonLyrics::tick);
   }

   private static void tick(Minecraft mc) {
      AutoPerf.tick(mc);
      DeathFx.tick(mc);
      SkyRenderer.tick(mc);

      while (keySync.consumeClick()) {
         SpotifyClient.syncHere();
      }

      while (keyFav.consumeClick()) {
         Favorites.saveCurrent();
      }

      while (keyClip.consumeClick()) {
         mc.gui.hud.toggle();
      }

      while (keyProfile.consumeClick()) {
         Profiles.toggle();
      }

      while (keyLyrics.consumeClick()) {
         config.lyricsSky = !config.lyricsSky;
         saveConfig();
         Island.notify(Fx.t(config.lyricsSky ? "neonlyrics.media.lyricson" : "neonlyrics.media.lyricsoff"), config.lyricsSky);
      }
   }

   private static HudElement gated(HudElement e) {
      return (g, d) -> {
         if (License.active()) {
            e.extractRenderState(g, d);
         }
      };
   }

   private static HudElement gatedOr(HudElement mine, HudElement vanilla) {
      return (g, d) -> (License.active() ? mine : vanilla).extractRenderState(g, d);
   }
}
