package dev.valence.gui.pages;

import dev.valence.config.ValenceConfig;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.config.Config;
import dev.valence.cord.hud.Island;
import dev.valence.cord.sky.FloatWords;
import dev.valence.cord.util.AutoPerf;
import dev.valence.cord.util.FpsBoost;
import dev.valence.cord.util.Profiles;
import dev.valence.gui.Anim;
import dev.valence.gui.OptionList;
import dev.valence.gui.Page;
import dev.valence.gui.Theme;
import dev.valence.gui.UiSounds;
import dev.valence.gui.ValenceScreen;
import dev.valence.perf.ModInstaller;
import dev.valence.perf.Occlusion;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PerformancePage extends Page {
   private final OptionList options = new OptionList();

   @Override
   public String nameKey() {
      return "valence.page.performance";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.performance.subtitle";
   }

   @Override
   public String icon() {
      return "bolt";
   }

   private static Component t(String key, Object... args) {
      return Component.translatable(key, args);
   }

   @Override
   public void init(ValenceScreen screen, int x, int y, int w, int h) {
      super.init(screen, x, y, w, h);
      this.options.setBounds(x, y, w, h);
      this.options.clear();
      Config c = NeonLyrics.cfg();
      this.options.add(new PerformancePage.FpsCard());
      this.options.add(new OptionList.Header(t("valence.perf.automatic")));
      this.options.add(new OptionList.Toggle(t("neonlyrics.perf.auto"), t("neonlyrics.ui.perf.auto.sub"), () -> c.autoPerf, v -> {
         c.autoPerf = v;
         AutoPerf.reset();
      }));
      int[] targets = new int[]{0, 60, 90, 120, 144, 240};
      List<Component> targetNames = new ArrayList<>();

      for (int tf : targets) {
         targetNames.add((Component)(tf == 0 ? t("valence.perf.target.auto") : Component.literal(tf + " FPS")));
      }

      this.options.add(OptionList.choice(t("neonlyrics.perf.target"), targetNames, () -> {
         for (int i = 0; i < targets.length; i++) {
            if (targets[i] == c.targetFps) {
               return i;
            }
         }

         return 0;
      }, v -> {
         c.targetFps = targets[v];
         AutoPerf.reset();
      }, true));
      this.options
         .add(
            new OptionList.Segmented(
               t("valence.perf.graphics"),
               t("valence.perf.graphics.desc"),
               List.of(t("valence.perf.graphics.0"), t("valence.perf.graphics.1"), t("valence.perf.graphics.2")),
               () -> c.quality,
               v -> {
                  c.quality = v;
                  AutoPerf.reset();
               }
            )
         );
      this.options
         .add(
            new OptionList.Segmented(
               t("neonlyrics.ui.perf.profile"),
               t("valence.perf.profile.desc"),
               List.of(t("neonlyrics.ui.perf.chill"), t("neonlyrics.ui.perf.pvp")),
               () -> c.profile,
               Profiles::set
            )
         );
      this.options.add(new OptionList.Toggle(t("neonlyrics.opt.fpsboost"), t("neonlyrics.ui.perf.boost.sub"), () -> c.fpsBoost, FpsBoost::apply));
      this.options.add(new OptionList.Header(t("neonlyrics.ui.perf.effects")));
      ValenceConfig vc = ValenceConfig.get();
      this.options.add(new OptionList.Toggle(t("valence.perf.sky"), null, () -> vc.skyEnabled, v -> vc.skyEnabled = v));
      this.options.add(new OptionList.Toggle(t("valence.sky.particles"), null, () -> vc.skyParticles, v -> vc.skyParticles = v));
      this.options.add(new OptionList.Toggle(t("valence.sky.shooting"), null, () -> vc.skyShootingStars, v -> vc.skyShootingStars = v));
      this.options
         .add(
            new OptionList.Cycle(
                  t("valence.sky.quality"),
                  List.of("auto", "high", "balanced", "performance"),
                  List.of(t("valence.sky.quality.auto"), t("valence.sky.quality.high"), t("valence.sky.quality.balanced"), t("valence.sky.quality.performance")),
                  () -> vc.skyQuality,
                  v -> vc.skyQuality = v
               )
               .arrows()
         );
      this.options
         .add(
            new OptionList.Slider(
               t("valence.styles.fire.height"),
               0.1,
               1.0,
               0.05,
               () -> (double)vc.fireHeight,
               v -> vc.fireHeight = v.floatValue(),
               v -> Math.round(v * 100.0) + "%"
            )
         );
      this.options
         .add(new OptionList.Toggle(t("valence.perf.glow"), t("valence.perf.glow.desc"), () -> vc.lyricsGlow > 0.01F, v -> vc.lyricsGlow = v ? 0.6F : 0.0F));
      this.options
         .add(new OptionList.Toggle(t("valence.perf.maxfps"), t("valence.perf.maxfps.desc"), () -> ValenceConfig.get().maxFpsMode, PerformancePage::setMaxFps));
      this.options.add(new OptionList.Info(() -> t(ValenceConfig.get().maxFpsMode ? "valence.perf.maxfps.on" : "valence.perf.maxfps.off"), 2));
      this.options.add(new OptionList.Header(t("valence.perf.extra")));
      this.options
         .add(
            new OptionList.Toggle(
               t("valence.settings.performance.culling"), t("valence.settings.performance.culling.desc"), () -> ValenceConfig.get().perfCulling, v -> {
                  ValenceConfig.get().perfCulling = v;
                  Occlusion.clear();
               }
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               t("valence.settings.performance.background"),
               t("valence.settings.performance.background.desc"),
               () -> ValenceConfig.get().perfBackgroundFps,
               v -> ValenceConfig.get().perfBackgroundFps = v
            )
         );
      this.options.add(new OptionList.Info(() -> t("valence.settings.performance.status", status("lithium"), status("sodium")), 4));
      if (!ModInstaller.sodiumLoaded()) {
         this.options
            .add(new OptionList.Button(t("valence.settings.performance.sodium"), t("valence.settings.performance.install"), ModInstaller::installSodium));
         this.options.add(new OptionList.Info(() -> {
            return switch (ModInstaller.state()) {
               case WORKING -> t("valence.settings.performance.install.working");
               case DONE -> t("valence.settings.performance.install.done");
               case FAILED -> t("valence.settings.performance.install.failed");
               default -> t("valence.settings.performance.install.info");
            };
         }, 2));
      }
   }

   static void setMaxFps(boolean on) {
      Config c = NeonLyrics.cfg();
      ValenceConfig vc = ValenceConfig.get();
      if (on != vc.maxFpsMode) {
         if (on) {
            vc.maxFpsSaved = (vc.skyEnabled ? "1" : "0")
               + (vc.skyParticles ? "1" : "0")
               + (vc.skyShootingStars ? "1" : "0")
               + (vc.skyConstellations ? "1" : "0")
               + (vc.skyLightning ? "1" : "0")
               + (c.lyricsGlow ? "1" : "0")
               + "|"
               + vc.skyQuality;
            vc.skyParticles = false;
            vc.skyShootingStars = false;
            vc.skyConstellations = false;
            vc.skyLightning = false;
            vc.skyQuality = "performance";
            c.lyricsGlow = false;
            vc.maxFpsLyricGlow = vc.lyricsGlow;
            vc.lyricsGlow = 0.0F;
         } else {
            String s = vc.maxFpsSaved == null ? "" : vc.maxFpsSaved;
            String flags = s.contains("|") ? s.substring(0, s.indexOf(124)) : "111111";
            String q = s.contains("|") ? s.substring(s.indexOf(124) + 1) : "auto";
            if (flags.length() < 6) {
               flags = "111111";
            }

            vc.skyEnabled = flags.charAt(0) == '1';
            vc.skyParticles = flags.charAt(1) == '1';
            vc.skyShootingStars = flags.charAt(2) == '1';
            vc.skyConstellations = flags.charAt(3) == '1';
            vc.skyLightning = flags.charAt(4) == '1';
            c.lyricsGlow = flags.charAt(5) == '1';
            vc.skyQuality = q.isBlank() ? "auto" : q;
            vc.lyricsGlow = vc.maxFpsLyricGlow;
         }

         vc.maxFpsMode = on;
         ValenceConfig.save();
         NeonLyrics.saveConfig();
         FloatWords.clear();
         Island.notify(Component.translatable(on ? "valence.perf.maxfps.toast.on" : "valence.perf.maxfps.toast.off").getString(), !on);
      }
   }

   static void setAllEffects(boolean on) {
      Config c = NeonLyrics.cfg();
      ValenceConfig vc = ValenceConfig.get();
      vc.skyEnabled = on;
      vc.skyParticles = on;
      vc.skyShootingStars = on;
      vc.skyConstellations = on;
      vc.skyLightning = on;
      c.lyricsGlow = on;
      ValenceConfig.save();
      Island.notify(Component.translatable(on ? "neonlyrics.perf.allon.done" : "neonlyrics.perf.alloff.done").getString(), on);
      UiSounds.toggle(on);
   }

   private static Component status(String modId) {
      return Component.translatable(FabricLoader.getInstance().isModLoaded(modId) ? "valence.settings.performance.on" : "valence.settings.performance.off");
   }

   @Override
   public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
      this.options.render(g, mx, my);
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      return this.options.mouseClicked(mx, my, button);
   }

   @Override
   public boolean mouseDragged(double mx, double my, int button) {
      return this.options.mouseDragged(mx, my);
   }

   @Override
   public boolean mouseReleased(double mx, double my, int button) {
      return this.options.mouseReleased();
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      return this.options.mouseScrolled(mx, my, amount);
   }

   private static final class FpsCard extends OptionList.Row {
      private float shown = -1.0F;

      @Override
      public int height() {
         return 46;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         Font f = Minecraft.getInstance().font;
         Theme.card(g, x, y + 2, x + w, y + 42, 0.0F, false);
         int fps = Minecraft.getInstance().getFps();
         this.shown = this.shown < 0.0F ? fps : this.shown + (fps - this.shown) * Anim.k(6.0F);
         g.pose().pushMatrix();
         g.pose().translate(x + 9, y + 7);
         g.pose().scale(0.7F, 0.7F);
         g.text(f, Component.translatable("valence.perf.now"), 0, 0, -8095322, false);
         g.pose().popMatrix();
         g.pose().pushMatrix();
         g.pose().translate(x + 9, y + 16);
         g.pose().scale(2.0F, 2.0F);
         String num = String.valueOf(Math.round(this.shown));
         g.text(f, num, 0, 0, -1, false);
         g.pose().popMatrix();
         g.pose().pushMatrix();
         g.pose().translate(x + 13 + f.width(num) * 2, y + 26);
         g.pose().scale(0.75F, 0.75F);
         g.text(f, "FPS", 0, 0, -8095322, false);
         g.pose().popMatrix();
         Config c = NeonLyrics.cfg();
         int lv = AutoPerf.level();
         Component st = !c.autoPerf
            ? Component.translatable("neonlyrics.perf.auto.off")
            : (lv == 0 ? Component.translatable("valence.perf.allfine") : Component.translatable("neonlyrics.perf.auto.level", new Object[]{lv, 6}));
         String mod = Component.translatable("valence.perf.modcost", new Object[]{String.format(Locale.ROOT, "%.1f", AutoPerf.modMs())}).getString();
         g.pose().pushMatrix();
         g.pose().translate(x + w - 9, y + 17);
         g.pose().scale(0.75F, 0.75F);
         g.text(f, mod, -f.width(mod), 0, -4608298, false);
         g.text(f, st, -f.width(st), 11, lv == 0 ? -11870592 : -278748, false);
         g.pose().popMatrix();
      }
   }
}
