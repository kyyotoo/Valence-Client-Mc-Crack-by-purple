package dev.valence.cord.util;

import dev.valence.gui.UiSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class Fx {
   private Fx() {
   }

   public static String t(String key, Object... args) {
      return I18n.get(key, args);
   }

   public static void sound(Fx.Sfx s) {
      switch (s) {
         case CLICK:
            UiSounds.click();
            break;
         case WIN:
            UiSounds.toggle(true);
            break;
         case ERROR:
            UiSounds.toggle(false);
            break;
         case TICK:
            UiSounds.hover();
      }
   }

   public static void info(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gui != null) {
         mc.gui
            .hud
            .getChat()
            .addClientSystemMessage(
               Component.literal("[Valence] ").withStyle(ChatFormatting.AQUA).append(Component.literal(text).withStyle(ChatFormatting.GRAY))
            );
      }
   }

   public static void error(String text) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gui != null) {
         mc.gui
            .hud
            .getChat()
            .addClientSystemMessage(
               Component.literal("[Valence] ").withStyle(ChatFormatting.DARK_RED).append(Component.literal(text).withStyle(ChatFormatting.RED))
            );
         sound(Fx.Sfx.ERROR);
      }
   }

   public static void clipboard(String s) {
      Minecraft.getInstance().keyboardHandler.setClipboard(s);
   }

   public static String myName() {
      return Minecraft.getInstance().getUser().getName();
   }

   public enum Sfx {
      CLICK,
      WIN,
      ERROR,
      TICK;
   }
}
