package dev.valence.styles;

import com.mojang.blaze3d.platform.NativeImage;
import dev.valence.ValenceClient;
import dev.valence.config.ValenceConfig;
import dev.valence.license.License;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Mth;

public final class StyleManager {
   public static final String PACK_FOLDER = "Valence Styles";
   public static final String PACK_ID = "file/Valence Styles";
   private static final String LEGACY_PACK_FOLDER = "Valance Styles";
   public static final String VANILLA = "vanilla";
   public static final List<StyleManager.Recolor> RECOLORS = List.of(
      new StyleManager.Recolor("lavender", 270.0F, 0.75F, 1.1F, -4745994),
      new StyleManager.Recolor("blueberry", 228.0F, 1.0F, 1.0F, -11572225),
      new StyleManager.Recolor("crimson", 352.0F, 1.1F, 0.95F, -2024120),
      new StyleManager.Recolor("mint", 158.0F, 0.7F, 1.05F, -9508937),
      new StyleManager.Recolor("gold", 45.0F, 1.0F, 1.05F, -340971),
      new StyleManager.Recolor("mono", -1.0F, 0.0F, 1.05F, -1710101),
      new StyleManager.Recolor("noir", -1.0F, 0.0F, 0.55F, -12632250)
   );
   private static final Map<String, StyleManager.Tex> TEXTURES = new HashMap<>();
   private static final Map<String, Identifier> ICONS = new HashMap<>();
   private static boolean dirty = false;
   private static int counter = 0;

   static boolean whole(StyleCategory c) {
      return switch (c.id()) {
         case "crystal", "obsidian", "anchor", "shield", "cobweb", "fire" -> true;
         default -> false;
      };
   }

   private StyleManager() {
   }

   public static void init() {
      try {
         Files.createDirectories(StylePacks.ownFolder());
         Path readme = StylePacks.ownFolder().resolve("README.txt");
         if (!Files.exists(readme)) {
            Files.writeString(
               readme,
               String.join(
                  "\n",
                  "Valence PvP packs",
                  "=================",
                  "Put your PvP texture packs (.zip or unpacked folders) into this folder or into the normal",
                  "resourcepacks folder. Old 1.8 - 1.20 packs work too.",
                  "Valence shows every pack as a style in each category it changes (sword, totem, crystal ...).",
                  "Open the Valence menu -> Styles, pick a pack per category and press Apply.",
                  ""
               ),
               StandardCharsets.UTF_8
            );
         }
      } catch (IOException e) {
         ValenceClient.LOGGER.warn("Valence: could not create the packs folder", e);
      }
   }

   public static String selected(StyleCategory c) {
      String s = ValenceConfig.get().styles.getOrDefault(c.id(), "vanilla");
      return !"vanilla".equals(s) && !isBuiltIn(c, s) && StylePacks.byId(s) == null ? "vanilla" : s;
   }

   public static void select(StyleCategory c, String styleId) {
      if (!styleId.equals(selected(c))) {
         ValenceConfig.get().styles.put(c.id(), styleId);
         ValenceConfig.save();
         dirty = true;
      }
   }

   public static void markDirty() {
      dirty = true;
   }

   public static boolean isDirty() {
      return dirty;
   }

   public static List<StyleManager.Style> stylesFor(StyleCategory c) {
      List<StyleManager.Style> list = new ArrayList<>();
      list.add(new StyleManager.Style("vanilla", "Vanilla", null, -7631989));
      List<Recipes.Design> designs = Recipes.forCategory(c.id());
      if (!designs.isEmpty()) {
         for (Recipes.Design d : designs) {
            list.add(new StyleManager.Style("d:" + d.id(), name(d.id()), null, d.swatch()));
         }
      } else {
         for (String p : StyleEffects.PATTERNS) {
            list.add(new StyleManager.Style(p, name(p), null, StyleEffects.swatch(p)));
         }

         for (StyleManager.Recolor r : RECOLORS) {
            list.add(new StyleManager.Style(r.id(), name(r.id()), null, r.swatch()));
         }
      }

      for (StylePacks.Source s : StylePacks.forCategory(c)) {
         list.add(new StyleManager.Style(s.id, s.name, s, -1));
      }

      return list;
   }

   private static String name(String id) {
      return Component.translatable("valence.styles.style." + id).getString();
   }

   private static boolean isBuiltIn(StyleCategory c, String styleId) {
      if (styleId.startsWith("d:")) {
         return Recipes.find(c.id(), styleId.substring(2)) != null;
      }

      if (StyleEffects.isPattern(styleId)) {
         return true;
      }

      for (StyleManager.Recolor r : RECOLORS) {
         if (r.id().equals(styleId)) {
            return true;
         }
      }

      return false;
   }

   public static int count(StyleCategory c) {
      int builtIn = Recipes.forCategory(c.id()).isEmpty() ? StyleEffects.PATTERNS.length + RECOLORS.size() : Recipes.forCategory(c.id()).size();
      return 1 + builtIn + StylePacks.forCategory(c).size();
   }

   public static void rescan() {
      StylePacks.rescan();
      clearPreviewCache();
   }

   private static byte[] vanillaBytes(String path) {
      Minecraft mc = Minecraft.getInstance();
      IoSupplier<InputStream> sup = mc.getVanillaPackResources().fullResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace(path));
      if (sup == null) {
         return null;
      }

      try (InputStream in = (InputStream)sup.get()) {
         return in.readAllBytes();
      } catch (IOException e) {
         return null;
      }
   }

   static NativeImage buildBuiltIn(StyleCategory c, String styleId, String path) {
      byte[] bytes = vanillaBytes(path);
      if (bytes == null) {
         return null;
      }

      NativeImage img;
      try {
         img = NativeImage.read(new ByteArrayInputStream(bytes));
      } catch (IOException | RuntimeException e) {
         return null;
      }

      if (styleId.startsWith("d:")) {
         Recipes.Design d = Recipes.find(c.id(), styleId.substring(2));
         return d == null ? img : Recipes.apply(c.id(), img, d);
      }

      if (StyleEffects.isPattern(styleId)) {
         StyleEffects.apply(img, styleId, whole(c));
         return img;
      }

      for (StyleManager.Recolor r : RECOLORS) {
         if (r.id().equals(styleId)) {
            recolor(img, r, whole(c));
            return img;
         }
      }

      return img;
   }

   public static StyleManager.Tex texture(StyleCategory c, String styleId, String path) {
      boolean builtIn = c != null && isBuiltIn(c, styleId);
      StylePacks.Source src = !"vanilla".equals(styleId) && !builtIn ? StylePacks.byId(styleId) : null;
      boolean own = src != null && src.has(path);
      String key = (builtIn ? c.id() + "/" + styleId : (own ? styleId : "vanilla")) + "|" + path;
      StyleManager.Tex cached = TEXTURES.get(key);
      if (cached != null) {
         return cached;
      }

      NativeImage img;
      if (builtIn) {
         img = buildBuiltIn(c, styleId, path);
         if (img == null) {
            return null;
         }
      } else {
         byte[] bytes = own ? StylePacks.texture(src, path) : vanillaBytes(path);
         if (bytes == null) {
            return null;
         }

         try {
            img = NativeImage.read(new ByteArrayInputStream(bytes));
         } catch (IOException | RuntimeException e) {
            ValenceClient.LOGGER.warn("Valence: broken texture {} in {}", path, styleId);
            return null;
         }
      }

      int w = img.getWidth();
      int h = img.getHeight();
      int[] px = new int[w * h];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            px[y * w + x] = img.getPixel(x, y);
         }
      }

      int frames = h > w && h % w == 0 ? h / w : 1;
      NativeImage mirror = new NativeImage(w, h, true);

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            mirror.setPixel(w - 1 - x, y, px[y * w + x]);
         }
      }

      int n = counter++;
      Identifier id = Identifier.fromNamespaceAndPath("valence", "dynamic/style/t" + n);
      Identifier mid = Identifier.fromNamespaceAndPath("valence", "dynamic/style/m" + n);
      String label = key;
      Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "Valence style " + label, img));
      Minecraft.getInstance().getTextureManager().register(mid, new DynamicTexture(() -> "Valence style mirror " + label, mirror));
      StyleManager.Tex t = new StyleManager.Tex(id, mid, w, h, px, frames);
      TEXTURES.put(key, t);
      return t;
   }

   public static String mainPath(StyleCategory c, String styleId) {
      StylePacks.Source src = !"vanilla".equals(styleId) && !isBuiltIn(c, styleId) ? StylePacks.byId(styleId) : null;
      if (src != null) {
         for (String p : c.textures()) {
            if (src.has(p)) {
               return p;
            }
         }
      }

      return c.textures().get(0);
   }

   public static Identifier icon(String styleId) {
      if ("vanilla".equals(styleId) || !styleId.startsWith("pack:")) {
         return null;
      }

      if (ICONS.containsKey(styleId)) {
         return ICONS.get(styleId);
      }

      Identifier id = null;
      StylePacks.Source src = StylePacks.byId(styleId);
      byte[] bytes = src == null ? null : StylePacks.icon(src);
      if (bytes != null) {
         try {
            NativeImage img = NativeImage.read(new ByteArrayInputStream(bytes));
            id = Identifier.fromNamespaceAndPath("valence", "dynamic/style/icon" + counter++);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "Valence pack icon", img));
         } catch (IOException | RuntimeException ignored) {
            id = null;
         }
      }

      ICONS.put(styleId, id);
      return id;
   }

   public static void clearPreviewCache() {
      for (StyleManager.Tex t : TEXTURES.values()) {
         Minecraft.getInstance().getTextureManager().release(t.id());
         Minecraft.getInstance().getTextureManager().release(t.mirrorId());
      }

      for (Identifier id : ICONS.values()) {
         if (id != null) {
            Minecraft.getInstance().getTextureManager().release(id);
         }
      }

      TEXTURES.clear();
      ICONS.clear();
   }

   private static String totemModel(float hand, float gui) {
      return String.format(
         Locale.ROOT,
         String.join(
            "\n",
            "{",
            "  \"parent\": \"minecraft:item/generated\",",
            "  \"textures\": { \"layer0\": \"minecraft:item/totem_of_undying\" },",
            "  \"display\": {",
            "    \"thirdperson_righthand\": { \"rotation\": [0, 0, 0], \"translation\": [0, 3, 1], \"scale\": [%1$.3f, %1$.3f, %1$.3f] },",
            "    \"thirdperson_lefthand\": { \"rotation\": [0, 0, 0], \"translation\": [0, 3, 1], \"scale\": [%1$.3f, %1$.3f, %1$.3f] },",
            "    \"firstperson_righthand\": { \"rotation\": [0, -90, 25], \"translation\": [1.13, 3.2, 1.13], \"scale\": [%2$.3f, %2$.3f, %2$.3f] },",
            "    \"firstperson_lefthand\": { \"rotation\": [0, -90, 25], \"translation\": [1.13, 3.2, 1.13], \"scale\": [%2$.3f, %2$.3f, %2$.3f] },",
            "    \"gui\": { \"rotation\": [0, 0, 0], \"translation\": [0, 0, 0], \"scale\": [%3$.3f, %3$.3f, %3$.3f] },",
            "    \"ground\": { \"rotation\": [0, 0, 0], \"translation\": [0, 2, 0], \"scale\": [0.5, 0.5, 0.5] },",
            "    \"fixed\": { \"rotation\": [0, 180, 0], \"scale\": [1, 1, 1] }",
            "  }",
            "}",
            ""
         ),
         0.55F * hand,
         0.68F * hand,
         gui
      );
   }

   private static boolean modernModel(byte[] json) {
      String s = new String(json, StandardCharsets.UTF_8);
      return !s.contains("items/") && !s.contains("builtin/");
   }

   public static void apply() {
      Minecraft mc = Minecraft.getInstance();
      Path packDir = mc.getResourcePackDirectory().resolve("Valence Styles");
      Path assets = packDir.resolve("assets/minecraft");
      boolean any = false;
      boolean totemModelWritten = false;

      try {
         deleteRecursive(packDir.resolve("assets"));
         Files.createDirectories(packDir);

         for (StyleCategory c : StyleCategory.ALL) {
            String styleId = selected(c);
            if (!"vanilla".equals(styleId)) {
               if (isBuiltIn(c, styleId)) {
                  for (String path : c.textures()) {
                     NativeImage img;
                     try {
                        img = buildBuiltIn(c, styleId, path);
                     } catch (RuntimeException e) {
                        ValenceClient.LOGGER.warn("Valence: could not build style {} for {}", new Object[]{styleId, path, e});
                        continue;
                     }

                     if (img != null) {
                        Path out = assets.resolve(path);
                        Files.createDirectories(out.getParent());
                        img.writeToFile(out);
                        img.close();
                        byte[] meta = vanillaBytes(path + ".mcmeta");
                        if (meta != null) {
                           Files.write(assets.resolve(path + ".mcmeta"), meta);
                        }

                        any = true;
                     }
                  }
               } else {
                  StylePacks.Source src = StylePacks.byId(styleId);
                  if (src != null) {
                     for (String path : c.textures()) {
                        byte[] bytes = StylePacks.texture(src, path);
                        if (bytes != null) {
                           Path out = assets.resolve(path);
                           Files.createDirectories(out.getParent());
                           Files.write(out, bytes);
                           byte[] meta = StylePacks.textureMeta(src, path);
                           if (meta == null) {
                              meta = vanillaBytes(path + ".mcmeta");
                           }

                           if (meta != null) {
                              Files.write(assets.resolve(path + ".mcmeta"), meta);
                           }

                           any = true;
                        }
                     }

                     for (Entry<String, String> m : StylePacks.models(src).entrySet()) {
                        String model = m.getKey();
                        boolean belongs = false;

                        for (String t : c.textures()) {
                           String item = t.substring(t.lastIndexOf(47) + 1, t.length() - 4);
                           if (model.endsWith("/" + item + ".json")) {
                              belongs = true;
                           }
                        }

                        byte[] json = belongs ? StylePacks.read(src, m.getValue()) : null;
                        if (json != null && modernModel(json)) {
                           Path out = assets.resolve(model);
                           Files.createDirectories(out.getParent());
                           Files.write(out, json);
                           if (model.startsWith("models/item/totem_of_undying")) {
                              totemModelWritten = true;
                           }
                        }
                     }
                  }
               }
            }
         }

         String size = ValenceConfig.get().styleTotemSize;
         if (!totemModelWritten && !"normal".equals(size)) {
            float hand = "tiny".equals(size) ? 0.55F : 0.75F;
            float gui = "tiny".equals(size) ? 0.72F : 0.86F;
            Path out = assets.resolve("models/item/totem_of_undying.json");
            Files.createDirectories(out.getParent());
            Files.writeString(out, totemModel(hand, gui), StandardCharsets.UTF_8);
            any = true;
         }

         Files.writeString(
            packDir.resolve("pack.mcmeta"),
            String.join(
               "\n",
               "{",
               "  \"pack\": {",
               "    \"description\": \"§dValence §7PvP styles\",",
               "    \"pack_format\": 75,",
               "    \"min_format\": 75,",
               "    \"max_format\": 75",
               "  }",
               "}",
               ""
            ),
            StandardCharsets.UTF_8
         );

         try (InputStream logo = StyleManager.class.getResourceAsStream("/assets/valence/icon.png")) {
            if (logo != null) {
               Files.write(packDir.resolve("pack.png"), logo.readAllBytes());
            }
         }
      } catch (IOException e) {
         ValenceClient.LOGGER.error("Valence: could not write the styles pack", e);
         return;
      }

      PackRepository repo = mc.getResourcePackRepository();
      Path legacy = mc.getResourcePackDirectory().resolve("Valance Styles");
      if (Files.isDirectory(legacy)) {
         try (Stream<Path> walk = Files.walk(legacy)) {
            walk.sorted(Comparator.reverseOrder()).forEach(f -> f.toFile().delete());
         } catch (IOException var19) {
         }
      }

      repo.reload();
      repo.removePack("file/Valance Styles");
      any = any && License.active();
      List<String> before = new ArrayList<>(mc.options.resourcePacks);
      if (any) {
         repo.removePack("file/Valence Styles");
         repo.addPack("file/Valence Styles");
      } else {
         repo.removePack("file/Valence Styles");
      }

      mc.options.updateResourcePacks(repo);
      if (before.equals(mc.options.resourcePacks)) {
         mc.reloadResourcePacks();
      }

      dirty = false;
   }

   static boolean[] mainColorMask(int[] src, int w, int h) {
      float[] hist = new float[36];
      float satSum = 0.0F;
      float count = 0.0F;
      float[][] hsv = new float[src.length][];

      for (int i = 0; i < src.length; i++) {
         if (src[i] >>> 24 >= 16) {
            hsv[i] = hsv(src[i]);
            hist[Math.min(35, (int)(hsv[i][0] * 36.0F))] += hsv[i][1] * hsv[i][2];
            satSum += hsv[i][1];
            count++;
         }
      }

      int best = 0;

      for (int i = 1; i < 36; i++) {
         if (hist[i] > hist[best]) {
            best = i;
         }
      }

      float dominant = (best + 0.5F) / 36.0F;
      boolean grayish = count == 0.0F || satSum / count < 0.18F;
      boolean[] mask = new boolean[src.length];

      for (int i = 0; i < src.length; i++) {
         if (hsv[i] != null) {
            float d = Math.abs(hsv[i][0] - dominant);
            d = Math.min(d, 1.0F - d);
            mask[i] = grayish || hsv[i][1] > 0.1F && d < 0.14F;
         }
      }

      return mask;
   }

   static void recolor(NativeImage img, StyleManager.Recolor rc, boolean whole) {
      int w = img.getWidth();
      int h = img.getHeight();
      int[] src = new int[w * h];

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            src[y * w + x] = img.getPixel(x, y);
         }
      }

      boolean[] mask;
      if (whole) {
         mask = new boolean[src.length];

         for (int i = 0; i < src.length; i++) {
            mask[i] = src[i] >>> 24 > 0;
         }
      } else {
         mask = mainColorMask(src, w, h);
      }

      float satSum = 0.0F;
      int n = 0;

      for (int i = 0; i < src.length; i++) {
         if (mask[i]) {
            satSum += hsv(src[i])[1];
            n++;
         }
      }

      boolean grayish = n == 0 || satSum / n < 0.18F;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int i = y * w + x;
            if (mask[i]) {
               int a = src[i] >>> 24;
               float[] c = hsv(src[i]);
               float hue = rc.hue() < 0.0F ? c[0] : rc.hue() / 360.0F;
               float sat;
               if (rc.sat() <= 0.0F) {
                  sat = 0.0F;
               } else if (grayish) {
                  sat = Math.max(0.0F, Math.min(1.0F, 0.35F + c[1] * rc.sat()));
               } else {
                  sat = Math.max(0.0F, Math.min(1.0F, c[1] * rc.sat()));
               }

               float val = Math.max(0.0F, Math.min(1.0F, c[2] * rc.val()));
               int rgb = Mth.hsvToRgb(hue, sat, val);
               img.setPixel(x, y, a << 24 | rgb & 16777215);
            }
         }
      }
   }

   private static float[] hsv(int argb) {
      float r = (argb >> 16 & 0xFF) / 255.0F;
      float g = (argb >> 8 & 0xFF) / 255.0F;
      float b = (argb & 0xFF) / 255.0F;
      float max = Math.max(r, Math.max(g, b));
      float min = Math.min(r, Math.min(g, b));
      float d = max - min;
      float hue = 0.0F;
      if (d > 1.0E-5F) {
         if (max == r) {
            hue = (g - b) / d % 6.0F;
         } else if (max == g) {
            hue = (b - r) / d + 2.0F;
         } else {
            hue = (r - g) / d + 4.0F;
         }

         hue /= 6.0F;
         if (hue < 0.0F) {
            hue++;
         }
      }

      float sat = max <= 0.0F ? 0.0F : d / max;
      return new float[]{hue, sat, max};
   }

   public static void resetAll() {
      ValenceConfig.get().styles.clear();
      ValenceConfig.save();
      dirty = true;
   }

   private static void deleteRecursive(Path p) throws IOException {
      if (Files.exists(p)) {
         try (Stream<Path> walk = Files.walk(p)) {
            for (Path x : walk.sorted(Comparator.reverseOrder()).toList()) {
               Files.deleteIfExists(x);
            }
         }
      }
   }

   public record Recolor(String id, float hue, float sat, float val, int swatch) {
   }

   public record Style(String id, String name, StylePacks.Source source, int swatch) {
      public boolean vanilla() {
         return "vanilla".equals(this.id);
      }

      public boolean builtIn() {
         return this.source == null && !this.vanilla();
      }
   }

   public record Tex(Identifier id, Identifier mirrorId, int width, int height, int[] argb, int frames) {
      public int frameHeight() {
         return this.height / Math.max(1, this.frames);
      }

      public int pixel(int x, int y) {
         return this.argb[y * this.width + x];
      }
   }
}
