package dev.valence.gui.pages;

import dev.valence.ValenceClient;
import dev.valence.config.ValenceConfig;
import dev.valence.gui.OptionList;
import dev.valence.gui.Page;
import dev.valence.gui.UiSounds;
import dev.valence.gui.ValenceScreen;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

public class SettingsPage extends Page {
   private final OptionList options = new OptionList();

   @Override
   public String nameKey() {
      return "valence.page.settings";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.settings.subtitle";
   }

   @Override
   public String icon() {
      return "gear";
   }

   @Override
   public void init(ValenceScreen screen, int x, int y, int w, int h) {
      super.init(screen, x, y, w, h);
      this.options.setBounds(x, y, w, h);
      this.options.clear();
      final ValenceConfig c = ValenceConfig.get();
      this.options.add(new OptionList.Header(Component.translatable("valence.settings.section.menu")));
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.settings.glow"), Component.translatable("valence.settings.glow.desc"), () -> c.mouseGlow, v -> c.mouseGlow = v
            )
         );
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.settings.blur"), Component.translatable("valence.settings.blur.desc"), () -> c.menuBlur, v -> c.menuBlur = v
            )
         );
      this.options.add(new OptionList.Toggle(Component.translatable("valence.settings.animations"), null, () -> c.menuAnimations, v -> c.menuAnimations = v));
      this.options
         .add(
            new OptionList.Toggle(
               Component.translatable("valence.settings.sounds"), Component.translatable("valence.settings.sounds.desc"), () -> c.uiSounds, v -> c.uiSounds = v
            )
         );
      this.options
         .add(
            new OptionList.Slider(
               Component.translatable("valence.settings.sounds.volume"),
               0.0,
               1.0,
               0.05,
               () -> (double)c.uiSoundVolume,
               v -> c.uiSoundVolume = v.floatValue(),
               v -> Math.round(v * 100.0) + "%"
            ) {
               @Override
               public boolean visible() {
                  return c.uiSounds;
               }
            }
         );
      this.options
         .add(
            new OptionList.Segmented(
               Component.translatable("valence.settings.sounds.pack"),
               null,
               UiSounds.PACKS.stream().map(id -> Component.translatable("valence.settings.sounds.pack." + id)).toList(),
               () -> Math.max(0, UiSounds.PACKS.indexOf(c.uiSoundPack)),
               v -> c.uiSoundPack = UiSounds.PACKS.get(v)
            ) {
               @Override
               public boolean visible() {
                  return c.uiSounds;
               }
            }
         );
      this.options
         .add(
            new OptionList.Button(
               Component.translatable("valence.settings.key", new Object[]{KeyMappingHelper.getBoundKeyOf(ValenceClient.OPEN_MENU).getDisplayName()}),
               Component.translatable("valence.settings.key.button"),
               () -> Minecraft.getInstance().gui.setScreen(new KeyBindsScreen(screen, Minecraft.getInstance().options))
            )
         );
      this.options.add(new OptionList.Info(() -> Component.translatable("valence.settings.language"), 2));
      this.options
         .add(
            new OptionList.Button(
               Component.translatable("valence.settings.performance.open"),
               Component.translatable("valence.settings.performance.open.button"),
               () -> screen.openPage(screen.pageIndexOf(PerformancePage.class))
            )
         );
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
}
