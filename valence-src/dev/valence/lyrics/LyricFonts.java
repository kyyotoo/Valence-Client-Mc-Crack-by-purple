package dev.valence.lyrics;

import java.util.List;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;

public final class LyricFonts {
   public static final List<LyricFonts.LyricFont> ALL = List.of(
      new LyricFonts.LyricFont("poppins_bold", "Poppins", vl("poppins_bold")),
      new LyricFonts.LyricFont("poppins_italic", "Poppins Italic", vl("poppins_italic")),
      new LyricFonts.LyricFont("dmserif", "Serif", vl("dmserif")),
      new LyricFonts.LyricFont("abril", "Abril", vl("abril")),
      new LyricFonts.LyricFont("pacifico", "Pacifico", vl("pacifico")),
      new LyricFonts.LyricFont("kaushan", "Brush", vl("kaushan")),
      new LyricFonts.LyricFont("bangers", "Bangers", vl("bangers")),
      new LyricFonts.LyricFont("luckiest", "Luckiest", vl("luckiest")),
      new LyricFonts.LyricFont("lilita", "Lilita", vl("lilita")),
      new LyricFonts.LyricFont("default", "Minecraft", Identifier.withDefaultNamespace("default"))
   );

   private LyricFonts() {
   }

   private static Identifier vl(String name) {
      return Identifier.fromNamespaceAndPath("valence", name);
   }

   public static LyricFonts.LyricFont byId(String id) {
      for (LyricFonts.LyricFont f : ALL) {
         if (f.id().equals(id)) {
            return f;
         }
      }

      return ALL.get(0);
   }

   public record LyricFont(String id, String displayName, Identifier fontId) {
      public FontDescription description() {
         return new Resource(this.fontId);
      }
   }
}
