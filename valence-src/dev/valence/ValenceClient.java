package dev.valence;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.valence.config.ValenceConfig;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.gui.ValenceScreen;
import dev.valence.hud.ValenceHud;
import dev.valence.license.License;
import dev.valence.license.LicenseScreen;
import dev.valence.lyrics.LyricsBridge;
import dev.valence.sky.SkyRegistry;
import dev.valence.styles.StyleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValenceClient implements ClientModInitializer {
   public static final String MOD_ID = "valence";
   public static final String VERSION = "1.9.2";
   public static final Logger LOGGER = LoggerFactory.getLogger("Valence");
   public static final Category CATEGORY = Category.register(Identifier.fromNamespaceAndPath("valence", "main"));
   public static KeyMapping OPEN_MENU;
   public static KeyMapping MEDIA_NEXT;
   public static KeyMapping MEDIA_PREV;
   public static KeyMapping MEDIA_PLAY;
   private static boolean licenseAsked;

   public void onInitializeClient() {
   ValenceConfig.load();
   StyleManager.init();
   SkyRegistry.load();
   NeonLyrics.init();
   OPEN_MENU = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.open_menu", Type.KEYBOARD, 14, CATEGORY));
   MEDIA_NEXT = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.media_next", Type.KEYBOARD, 63, CATEGORY));
   MEDIA_PREV = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.media_prev", Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));
   MEDIA_PLAY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.valence.media_play", Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));
   ClientTickEvents.END_CLIENT_TICK
      .register(
         (EndTick)client -> {
            while (OPEN_MENU.consumeClick()) {
               if (client.gui.screen() == null) {
                  client.gui.setScreen((Screen)new ValenceScreen(null));
               }
            }

            LyricsBridge.tick();

            while (MEDIA_NEXT.consumeClick()) {
               SpotifyClient.control(SpotifyClient.Cmd.NEXT);
            }

            while (MEDIA_PREV.consumeClick()) {
               SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
            }

            while (MEDIA_PLAY.consumeClick()) {
               SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
            }
         }
      );
   HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, Identifier.fromNamespaceAndPath("valence", "hud"), (g, d) -> {
      ValenceHud.render(g, d);
   });
   LOGGER.info("Valence {} ready", "1.9.2");
}
