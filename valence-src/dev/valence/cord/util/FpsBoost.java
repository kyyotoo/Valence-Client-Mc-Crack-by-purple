package dev.valence.cord.util;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class FpsBoost {
   private FpsBoost() {
   }

   public static void apply(boolean on) {
      Minecraft mc = Minecraft.getInstance();
      Config c = NeonLyrics.cfg();

      try {
         if (on) {
            c.boostBefore = save(mc.options.entityShadows()) + ";" + save(mc.options.particles()) + ";" + save(mc.options.biomeBlendRadius());
            set(mc.options.entityShadows(), "false");
            set(mc.options.particles(), "MINIMAL");
            set(mc.options.biomeBlendRadius(), "0");
            c.quality = 0;
         } else if (c.boostBefore != null && !c.boostBefore.isEmpty()) {
            String[] v = c.boostBefore.split(";", -1);
            if (v.length >= 3) {
               set(mc.options.entityShadows(), v[0]);
               set(mc.options.particles(), v[1]);
               set(mc.options.biomeBlendRadius(), v[2]);
            }

            c.boostBefore = "";
            if (c.quality == 0) {
               c.quality = 1;
            }
         }

         mc.options.save();
      } catch (Exception var4) {
         NeonLyrics.LOG.warn("FPS-Boost konnte nicht alles umstellen", var4);
      }

      c.fpsBoost = on;
      NeonLyrics.saveConfig();
   }

   private static String save(OptionInstance<?> o) {
      Object v = o.get();
      return v instanceof Enum<?> e ? e.name() : String.valueOf(v);
   }

   private static void set(OptionInstance o, String value) {
      Object cur = o.get();
      Object nv;
      if (cur instanceof Boolean) {
         nv = Boolean.parseBoolean(value);
      } else if (cur instanceof Integer) {
         nv = Integer.parseInt(value);
      } else {
         if (!(cur instanceof Enum<?> e)) {
            return;
         }

         nv = Enum.valueOf(e.getDeclaringClass(), value);
      }

      o.set(nv);
   }
}
