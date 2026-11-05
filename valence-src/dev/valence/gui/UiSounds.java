package dev.valence.gui;

import dev.valence.config.ValenceConfig;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class UiSounds {
   public static final List<String> PACKS = List.of("soft", "creamy", "glass");
   private static final UiSounds.Pack GLASS = new UiSounds.Pack(
      event("ui.hover"), event("ui.click"), event("ui.toggle_on"), event("ui.toggle_off"), event("ui.open")
   );
   private static final UiSounds.Pack SOFT = pack("soft");
   private static final UiSounds.Pack CREAMY = pack("creamy");
   private static long lastHover;

   private UiSounds() {
   }

   private static SoundEvent event(String name) {
      return SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("valence", name));
   }

   private static UiSounds.Pack pack(String id) {
      return new UiSounds.Pack(
         event("ui." + id + ".hover"),
         event("ui." + id + ".click"),
         event("ui." + id + ".toggle_on"),
         event("ui." + id + ".toggle_off"),
         event("ui." + id + ".open")
      );
   }

   private static UiSounds.Pack current() {
      return switch (ValenceConfig.get().uiSoundPack) {
         case "creamy" -> CREAMY;
         case "glass" -> GLASS;
         default -> SOFT;
      };
   }

   private static void play(SoundEvent event, float pitch, float volume) {
      ValenceConfig cfg = ValenceConfig.get();
      if (cfg.uiSounds && !(cfg.uiSoundVolume <= 0.01F) && event != null) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, Math.min(1.0F, volume * cfg.uiSoundVolume * 2.0F)));
      }
   }

   public static void hover() {
      long now = System.currentTimeMillis();
      if (now - lastHover >= 45L) {
         lastHover = now;
         play(current().hover(), 0.97F + (float)Math.random() * 0.06F, 0.3F);
      }
   }

   public static void click() {
      play(current().click(), 0.97F + (float)Math.random() * 0.06F, 0.5F);
   }

   public static void toggle(boolean on) {
      play(on ? current().on() : current().off(), 1.0F, 0.45F);
   }

   public static void open() {
      play(current().open(), 1.0F, 0.35F);
   }

   public static void preview() {
      click();
   }

   private record Pack(SoundEvent hover, SoundEvent click, SoundEvent on, SoundEvent off, SoundEvent open) {
   }
}
