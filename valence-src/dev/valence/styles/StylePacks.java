package dev.valence.styles;

import dev.valence.ValenceClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

public final class StylePacks {
   private static final Map<String, List<String>> ALIASES = new HashMap<>();
   private static final List<StylePacks.Source> SOURCES = new ArrayList<>();
   private static final Map<Path, ZipFile> OPEN = new HashMap<>();
   private static boolean scanned;

   private StylePacks() {
   }

   private static void alias(String modern, String old) {
      ALIASES.computeIfAbsent(modern, k -> new ArrayList<>()).add(old);
   }

   private static List<String> candidates(String modern) {
      List<String> out = new ArrayList<>();
      out.add(modern);
      out.add(modern.replace("textures/item/", "textures/items/").replace("textures/block/", "textures/blocks/"));
      List<String> extra = ALIASES.get(modern);
      if (extra != null) {
         out.addAll(extra);
      }

      return out;
   }

   public static Path ownFolder() {
      return FabricLoader.getInstance().getConfigDir().resolve("valence").resolve("packs");
   }

   public static Path resourcePackFolder() {
      return Minecraft.getInstance().getResourcePackDirectory();
   }

   public static List<StylePacks.Source> all() {
      if (!scanned) {
         rescan();
      }

      return SOURCES;
   }

   public static StylePacks.Source byId(String id) {
      for (StylePacks.Source s : all()) {
         if (s.id.equals(id)) {
            return s;
         }
      }

      return null;
   }

   public static List<StylePacks.Source> forCategory(StyleCategory c) {
      List<StylePacks.Source> out = new ArrayList<>();

      for (StylePacks.Source s : all()) {
         if (s.provides(c)) {
            out.add(s);
         }
      }

      return out;
   }

   public static synchronized void rescan() {
      closeAll();
      SOURCES.clear();
      scanned = true;

      try {
         Files.createDirectories(ownFolder());
      } catch (IOException var18) {
      }

      List<Path> roots = List.of(resourcePackFolder(), ownFolder());
      Set<String> seen = new HashSet<>();

      label126:
      for (Path root : roots) {
         if (Files.isDirectory(root)) {
            try (Stream<Path> s = Files.list(root)) {
               List<Path> list = s.sorted(Comparator.comparing(px -> px.getFileName().toString().toLowerCase(Locale.ROOT))).toList();
               Iterator var6 = list.iterator();

               while (true) {
                  StylePacks.Source src;
                  while (true) {
                     if (!var6.hasNext()) {
                        continue label126;
                     }

                     Path p = (Path)var6.next();
                     String file = p.getFileName().toString();
                     if (!file.equals("Valence Styles")) {
                        boolean zip = Files.isRegularFile(p) && file.toLowerCase(Locale.ROOT).endsWith(".zip");
                        boolean dir = Files.isDirectory(p) && Files.isRegularFile(p.resolve("pack.mcmeta"));
                        if (zip || dir) {
                           String name = cleanName(zip ? file.substring(0, file.length() - 4) : file);
                           String id = "pack:" + name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
                           if (seen.add(id)) {
                              src = new StylePacks.Source(id, name, p, zip);

                              try {
                                 index(src);
                                 break;
                              } catch (IOException e) {
                                 ValenceClient.LOGGER.warn("Valence: could not read pack {}: {}", file, e.toString());
                              }
                           }
                        }
                     }
                  }

                  boolean useful = false;

                  for (StyleCategory c : StyleCategory.ALL) {
                     if (src.provides(c)) {
                        useful = true;
                        break;
                     }
                  }

                  if (useful) {
                     SOURCES.add(src);
                  }
               }
            } catch (IOException e) {
               ValenceClient.LOGGER.warn("Valence: could not list {}", root);
            }
         }
      }

      ValenceClient.LOGGER.info("Valence found {} PvP packs", SOURCES.size());
   }

   static String cleanName(String s) {
      String n = s.replaceAll("§.", "").replace('_', ' ').replaceAll("\\s+", " ").trim();
      return n.isEmpty() ? s : n;
   }

   private static void index(StylePacks.Source src) throws IOException {
      if (src.zip) {
         ZipFile zf = open(src.path);
         Enumeration<? extends ZipEntry> en = zf.entries();

         while (en.hasMoreElements()) {
            ZipEntry e = en.nextElement();
            if (!e.isDirectory()) {
               src.entries.add(e.getName());
            }
         }
      } else {
         try (Stream<Path> walk = Files.walk(src.path)) {
            walk.filter(x$0 -> Files.isRegularFile(x$0)).forEach(p -> src.entries.add(src.path.relativize(p).toString().replace('\\', '/')));
         }
      }

      String prefix = "";
      if (!src.entries.contains("pack.mcmeta")) {
         for (String e : src.entries) {
            int i = e.indexOf("assets/minecraft/");
            if (i > 0) {
               prefix = e.substring(0, i);
               break;
            }
         }
      }

      Map<String, String> lower = new HashMap<>();

      for (String e : src.entries) {
         lower.put(e.toLowerCase(Locale.ROOT), e);
      }

      src.hasIcon = lower.containsKey((prefix + "pack.png").toLowerCase(Locale.ROOT));
      String base = prefix + "assets/minecraft/";

      for (StyleCategory c : StyleCategory.ALL) {
         for (String modern : c.textures()) {
            for (String cand : candidates(modern)) {
               String real = lower.get((base + cand).toLowerCase(Locale.ROOT));
               if (real != null) {
                  src.files.put(modern, real);
                  break;
               }
            }
         }
      }

      for (StyleCategory c : StyleCategory.ALL) {
         for (String modern : c.textures()) {
            if (modern.startsWith("textures/item/")) {
               String item = modern.substring("textures/item/".length(), modern.length() - 4);

               for (String m : List.of("models/item/" + item + ".json", "items/" + item + ".json")) {
                  String real = lower.get((base + m).toLowerCase(Locale.ROOT));
                  if (real != null) {
                     src.models.put(m, real);
                  }
               }
            }
         }
      }
   }

   private static ZipFile open(Path p) throws IOException {
      ZipFile z = OPEN.get(p);
      if (z == null) {
         z = new ZipFile(p.toFile(), StandardCharsets.UTF_8);
         OPEN.put(p, z);
      }

      return z;
   }

   private static void closeAll() {
      for (ZipFile z : OPEN.values()) {
         try {
            z.close();
         } catch (IOException var3) {
         }
      }

      OPEN.clear();
   }

   static byte[] read(StylePacks.Source src, String inPack) {
      if (inPack == null) {
         return null;
      }

      try {
         if (!src.zip) {
            Path f = src.path.resolve(inPack);
            return Files.isRegularFile(f) ? Files.readAllBytes(f) : null;
         }

         ZipFile z = open(src.path);
         ZipEntry e = z.getEntry(inPack);
         if (e == null) {
            return null;
         }

         try (InputStream in = z.getInputStream(e)) {
            return in.readAllBytes();
         }
      } catch (IOException e) {
         return null;
      }
   }

   static byte[] texture(StylePacks.Source src, String modern) {
      return read(src, src.files.get(modern));
   }

   static byte[] textureMeta(StylePacks.Source src, String modern) {
      String real = src.files.get(modern);
      if (real == null) {
         return null;
      }

      String meta = real + ".mcmeta";

      for (String e : src.entries) {
         if (e.equalsIgnoreCase(meta)) {
            return read(src, e);
         }
      }

      return null;
   }

   static Map<String, String> models(StylePacks.Source src) {
      return new LinkedHashMap<>(src.models);
   }

   static byte[] icon(StylePacks.Source src) {
      if (!src.hasIcon) {
         return null;
      }

      for (String e : src.entries) {
         if (e.toLowerCase(Locale.ROOT).endsWith("pack.png") && e.chars().filter(ch -> ch == 47).count() <= 1L) {
            return read(src, e);
         }
      }

      return null;
   }

   static {
      alias("textures/item/totem_of_undying.png", "textures/items/totem.png");
      alias("textures/item/golden_apple.png", "textures/items/apple_golden.png");
      alias("textures/entity/end_crystal/end_crystal.png", "textures/entity/endercrystal/endercrystal.png");
      alias("textures/block/cobweb.png", "textures/blocks/web.png");
      alias("textures/block/fire_0.png", "textures/blocks/fire_layer_0.png");
      alias("textures/block/fire_1.png", "textures/blocks/fire_layer_1.png");

      for (String m : List.of("diamond", "netherite")) {
         alias("textures/entity/equipment/humanoid/" + m + ".png", "textures/models/armor/" + m + "_layer_1.png");
         alias("textures/entity/equipment/humanoid_leggings/" + m + ".png", "textures/models/armor/" + m + "_layer_2.png");
      }
   }

   public static final class Source {
      public final String id;
      public final String name;
      public final Path path;
      public final boolean zip;
      final Map<String, String> files = new HashMap<>();
      final Map<String, String> models = new HashMap<>();
      final Set<String> entries = new HashSet<>();
      boolean hasIcon;

      Source(String id, String name, Path path, boolean zip) {
         this.id = id;
         this.name = name;
         this.path = path;
         this.zip = zip;
      }

      public boolean provides(StyleCategory c) {
         for (String t : c.textures()) {
            if (this.files.containsKey(t)) {
               return true;
            }
         }

         return false;
      }

      public boolean has(String modernPath) {
         return this.files.containsKey(modernPath);
      }

      public int count(StyleCategory c) {
         int n = 0;

         for (String t : c.textures()) {
            if (this.files.containsKey(t)) {
               n++;
            }
         }

         return n;
      }
   }
}
