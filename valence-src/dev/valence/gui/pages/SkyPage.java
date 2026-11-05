package dev.valence.gui.pages;

import dev.valence.config.ValenceConfig;
import dev.valence.gui.Anim;
import dev.valence.gui.OptionList;
import dev.valence.gui.Page;
import dev.valence.gui.RoundedImage;
import dev.valence.gui.Theme;
import dev.valence.gui.UiSounds;
import dev.valence.gui.ValenceScreen;
import dev.valence.sky.SkyDefinition;
import dev.valence.sky.SkyRegistry;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SkyPage extends Page {
   private int tab = 0;
   private float scroll;
   private float targetScroll;
   private final OptionList options = new OptionList();
   private static final int TOP = 26;
   private float tabSwitch = 1.0F;

   @Override
   public String nameKey() {
      return "valence.page.sky";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.sky.subtitle";
   }

   @Override
   public String icon() {
      return "sky";
   }

   @Override
   public void init(ValenceScreen screen, int x, int y, int w, int h) {
      super.init(screen, x, y, w, h);
      this.options.setBounds(x, y + 24, w, h - 24);
      this.options.clear();
      ValenceConfig c = ValenceConfig.get();
      this.options.add(new OptionList.Header(Component.translatable("valence.sky.section.look")));
      this.options
         .add(
            new OptionList.Slider(
               Component.translatable("valence.sky.brightness"),
               0.3,
               1.6,
               0.05,
               () -> (double)c.skyBrightness,
               v -> c.skyBrightness = v.floatValue(),
               v -> Math.round(v * 100.0) + "%"
            )
         );
      this.options
         .add(
            new OptionList.Slider(
               Component.translatable("valence.sky.speed"),
               0.0,
               3.0,
               0.1,
               () -> (double)c.skyAnimationSpeed,
               v -> c.skyAnimationSpeed = v.floatValue(),
               v -> String.format("%.1fx", v)
            )
         );
      this.options
         .add(
            new OptionList.Cycle(
                  Component.translatable("valence.sky.quality"),
                  List.of("auto", "high", "balanced", "performance"),
                  List.of(
                     Component.translatable("valence.sky.quality.auto"),
                     Component.translatable("valence.sky.quality.high"),
                     Component.translatable("valence.sky.quality.balanced"),
                     Component.translatable("valence.sky.quality.performance")
                  ),
                  () -> c.skyQuality,
                  v -> c.skyQuality = v
               )
               .arrows()
         );
      this.options.add(new OptionList.Info(() -> Component.translatable("valence.sky.quality.desc"), 2));
      this.options.add(new OptionList.Header(Component.translatable("valence.sky.section.effects")));
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.stars"), null, () -> c.skyStars, v -> c.skyStars = v));
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.shooting"),
               Component.translatable("valence.sky.shooting.desc"),
               () -> c.skyShootingStars,
               v -> c.skyShootingStars = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.constellations"),
               Component.translatable("valence.sky.constellations.desc"),
               () -> c.skyConstellations,
               v -> c.skyConstellations = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.particles"),
               Component.translatable("valence.sky.particles.desc"),
               () -> c.skyParticles,
               v -> c.skyParticles = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.lightning"),
               Component.translatable("valence.sky.lightning.desc"),
               () -> c.skyLightning,
               v -> c.skyLightning = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.clouds"), Component.translatable("valence.sky.clouds.desc"), () -> c.skyHideClouds, v -> c.skyHideClouds = v
            )
         );
      this.options.add(new OptionList.Header(Component.translatable("valence.sky.section.where")));
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.rain"), Component.translatable("valence.sky.rain.desc"), () -> c.skyDimInRain, v -> c.skyDimInRain = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.sky.end"), Component.translatable("valence.sky.end.desc"), () -> c.skyInEnd, v -> c.skyInEnd = v
            )
         );
   }

   private int columns() {
      return this.w > 380 ? 4 : 3;
   }

   private int tabX(int i) {
      int tw = 74;
      return this.x + this.w - (2 - i) * tw;
   }

   @Override
   public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
      Font f = this.font();
      ValenceConfig cfg = ValenceConfig.get();
      boolean on = cfg.skyEnabled;
      boolean hoverToggle = this.inside(mx, my, this.x, this.y, this.x + 100, this.y + 18);
      float th = Anim.hover(Anim.key(20, 0), hoverToggle);
      Theme.card(g, this.x, this.y, this.x + 100, this.y + 18, th, false);
      float k = Theme.easeOutCubic(Anim.approach(Anim.key(20, 1), on ? 1.0F : 0.0F, 14.0F));
      OptionList.drawSwitch(g, this.x + 6, this.y + 3, k, th);
      g.text(f, Component.translatable(on ? "valence.sky.enabled" : "valence.sky.disabled"), this.x + 36, this.y + 5, on ? -724737 : -8095322, false);
      String[] tabs = new String[]{"valence.sky.tab.skies", "valence.sky.tab.options"};
      int segX = this.tabX(0);
      Theme.pill(g, segX, this.y, this.x + this.w, this.y + 18, 603979776);
      Theme.pillLine(g, segX, this.y, this.x + this.w, this.y + 18, 452984831);
      float tabPos = Anim.approach(Anim.key(20, 2), this.tab, 16.0F);
      int selX = segX + Math.round(tabPos * 74.0F);
      Theme.glow(g, selX + 2, this.y + 2, selX + 72, this.y + 16, -7643914, 0.3F);
      Theme.pill(g, selX + 2, this.y + 2, selX + 72, this.y + 16, -7643914);

      for (int i = 0; i < tabs.length; i++) {
         int tx = this.tabX(i);
         float hv = Anim.hover(Anim.key(20, 3 + i), this.inside(mx, my, tx, this.y, tx + 74, this.y + 18) && this.tab != i);
         float near = 1.0F - Math.min(1.0F, Math.abs(tabPos - i));
         int tc = Theme.lerp(Theme.lerp(-8095322, -724737, hv), -1, near);
         g.centeredText(f, Component.translatable(tabs[i]), tx + 37, this.y + 5, tc);
      }

      this.tabSwitch = Math.min(1.0F, this.tabSwitch + Anim.dt() * 5.0F);
      float te = Theme.easeOutCubic(this.tabSwitch);
      float shift = (1.0F - te) * 12.0F * (this.tab == 1 ? 1 : -1);
      g.pose().pushMatrix();
      g.pose().translate(shift, 0.0F);
      this.renderContent(g, Math.round(mx - shift), my);
      g.pose().popMatrix();
      if (te < 0.999F) {
         Theme.round(g, this.x - 4, this.y + 26 - 2, this.x + this.w + 4, this.y + this.h + 2, Theme.withAlpha(-804451817, 1.0F - te));
      }
   }

   private void renderContent(GuiGraphicsExtractor g, int mx, int my) {
      Font f = this.font();
      ValenceConfig cfg = ValenceConfig.get();
      boolean on = cfg.skyEnabled;
      if (this.tab == 1) {
         this.options.render(g, mx, my);
      } else {
         List<SkyDefinition> skies = SkyRegistry.all();
         int cols = this.columns();
         int gap = 7;
         int top = this.y + 26;
         int areaH = this.h - 26;
         int cw = (this.w - 6 - gap * (cols - 1)) / cols;
         int ch = Math.round((cw - 6) * 108.0F / 192.0F) + 19;
         int rows = (skies.size() + cols - 1) / cols;
         int contentH = rows * (ch + gap) + 4;
         float max = Math.max(0, contentH - areaH);
         this.targetScroll = Math.max(0.0F, Math.min(max, this.targetScroll));
         this.scroll = this.scroll + (this.targetScroll - this.scroll) * Anim.k(16.0F);
         g.enableScissor(this.x - 14, top, this.x + this.w + 14, top + areaH);
         boolean mouseInArea = my >= top && my < top + areaH;

         for (int i = 0; i < skies.size(); i++) {
            SkyDefinition sky = skies.get(i);
            int cx = this.x + i % cols * (cw + gap);
            int cy = top + 3 + i / cols * (ch + gap) - Math.round(this.scroll);
            if (cy + ch >= top - 14 && cy <= top + areaH + 14) {
               boolean selected = on && sky.id.equals(cfg.skyId);
               boolean hovered = mouseInArea && this.inside(mx, my, cx, cy, cx + cw, cy + ch);
               float hv = Anim.hover(Anim.key(21, i), hovered);
               int lift = Math.round(hv * 1.5F);
               cy -= lift;
               Theme.card(g, cx, cy, cx + cw, cy + ch, hv, selected);
               int iw = cw - 6;
               int ih = ch - 19;
               Identifier thumb = Theme.id("textures/gui/skies/" + sky.id + ".png");
               float zoom = 0.06F * hv;
               float uw = 192.0F * (1.0F - zoom);
               float vh = 108.0F * (1.0F - zoom);
               RoundedImage.draw(
                  g,
                  thumb,
                  cx + 3,
                  cy + 3,
                  iw,
                  ih,
                  (192.0F - uw) / 2.0F,
                  (108.0F - vh) / 2.0F,
                  uw,
                  vh,
                  Theme.lerp(-3094304, -1, Math.max(hv, selected ? 1.0F : 0.0F))
               );
               Theme.outline(g, cx + 3, cy + 3, cx + 3 + iw, cy + 3 + ih, 587202559);
               Component name = Component.translatable("valence.sky.name." + sky.id);
               String txt = f.plainSubstrByWidth(name.getString(), cw - 10);
               g.text(f, txt, cx + 5, cy + ch - 13, selected ? -1 : Theme.lerp(-4608298, -724737, hv), false);
               if (selected) {
                  Theme.glow(g, cx + cw - 16, cy + 6, cx + cw - 6, cy + 16, -7643914, 0.6F);
                  Theme.pill(g, cx + cw - 17, cy + 5, cx + cw - 5, cy + 17, -7643914);
                  Theme.icon(g, "check", cx + cw - 15, cy + 7, 8, -1);
               }

               int[] pb = this.previewBox(cx, cy, iw, ih);
               boolean pHover = hovered && this.inside(mx, my, pb[0], pb[1], pb[2], pb[3]);
               float ph = Anim.hover(Anim.key(22, i), pHover, false);
               float vis = 0.35F + 0.65F * hv;
               float rise = (1.0F - hv) * 3.0F;
               g.pose().pushMatrix();
               g.pose().translate(0.0F, rise);
               Theme.glow(g, pb[0], pb[1], pb[2], pb[3], -7643914, 0.35F * ph);
               Theme.pill(g, pb[0], pb[1], pb[2], pb[3], Theme.withAlpha(Theme.lerp(-1273885666, -7643914, ph), vis));
               Theme.pillLine(g, pb[0], pb[1], pb[2], pb[3], Theme.withAlpha(Theme.lerp(1157627903, -1711276033, ph), vis));
               g.pose().translate(pb[0] + 5, pb[1] + 3.0F);
               g.pose().scale(0.75F, 0.75F);
               g.text(f, Component.translatable("valence.sky.preview"), 0, 0, Theme.withAlpha(-1, Math.min(1.0F, vis + 0.2F)), false);
               g.pose().popMatrix();
            }
         }

         g.disableScissor();
         if (max > 0.0F) {
            int barH = Math.max(18, Math.round((float)(areaH * areaH) / contentH));
            int barY = top + Math.round((areaH - barH) * (this.scroll / max));
            Theme.pill(g, this.x + this.w - 3, top, this.x + this.w, top + areaH, 419430399);
            Theme.pill(g, this.x + this.w - 3, barY, this.x + this.w, barY + barH, Theme.withAlpha(-4745985, 0.8F));
         }
      }
   }

   private int[] previewBox(int cx, int cy, int iw, int ih) {
      int tw = Math.round(this.font().width(Component.translatable("valence.sky.preview")) * 0.75F);
      int x1 = cx + 6;
      int y2 = cy + 3 + ih - 4;
      return new int[]{x1, y2 - 12, x1 + tw + 10, y2};
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      Font f = this.font();
      ValenceConfig cfg = ValenceConfig.get();
      if (this.inside(mx, my, this.x, this.y, this.x + 100, this.y + 18)) {
         cfg.skyEnabled = !cfg.skyEnabled;
         ValenceConfig.save();
         UiSounds.toggle(cfg.skyEnabled);
         return true;
      }

      for (int i = 0; i < 2; i++) {
         int tx = this.tabX(i);
         if (this.inside(mx, my, tx, this.y, tx + 74, this.y + 18)) {
            if (this.tab != i) {
               this.tab = i;
               this.tabSwitch = 0.0F;
               UiSounds.click();
            }

            return true;
         }
      }

      if (this.tab == 1) {
         return this.options.mouseClicked(mx, my, button);
      }

      List<SkyDefinition> skies = SkyRegistry.all();
      int cols = this.columns();
      int gap = 7;
      int top = this.y + 26;
      int areaH = this.h - 26;
      if (!(my < top) && !(my >= top + areaH)) {
         int cw = (this.w - 6 - gap * (cols - 1)) / cols;
         int ch = Math.round((cw - 6) * 108.0F / 192.0F) + 19;

         for (int i = 0; i < skies.size(); i++) {
            int cx = this.x + i % cols * (cw + gap);
            int cy = top + 3 + i / cols * (ch + gap) - Math.round(this.scroll);
            if (this.inside(mx, my, cx, cy - 2, cx + cw, cy + ch)) {
               int[] pb = this.previewBox(cx, cy, cw - 6, ch - 19);
               if (this.inside(mx, my, pb[0] - 2, pb[1] - 3, pb[2] + 2, pb[3] + 2)) {
                  UiSounds.click();
                  Minecraft.getInstance().gui.setScreen(new SkyPreviewScreen(this.screen, skies.get(i)));
                  return true;
               }

               cfg.skyId = skies.get(i).id;
               cfg.skyEnabled = true;
               ValenceConfig.save();
               UiSounds.toggle(true);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mx, double my, int button) {
      return this.tab == 1 && this.options.mouseDragged(mx, my);
   }

   @Override
   public boolean mouseReleased(double mx, double my, int button) {
      return this.tab == 1 && this.options.mouseReleased();
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      if (this.tab == 1) {
         return this.options.mouseScrolled(mx, my, amount);
      }

      this.targetScroll -= (float)amount * 30.0F;
      return true;
   }
}
