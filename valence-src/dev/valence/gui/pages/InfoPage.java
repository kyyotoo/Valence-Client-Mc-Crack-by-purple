package dev.valence.gui.pages;

import dev.valence.gui.OptionList;
import dev.valence.gui.Page;
import dev.valence.gui.ValenceScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class InfoPage extends Page {
   private final OptionList list = new OptionList();

   @Override
   public String nameKey() {
      return "valence.page.info";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.info.subtitle";
   }

   @Override
   public String icon() {
      return "info";
   }

   @Override
   public void init(ValenceScreen screen, int x, int y, int w, int h) {
      super.init(screen, x, y, w, h);
      this.list.setBounds(x, y, w, h);
      this.list.clear();
      this.list.add(new OptionList.Header(Component.translatable("valence.info.section.about")));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.version", new Object[]{"1.9.2", "1.21.11"}), 1));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.about"), 3));
      this.list.add(new OptionList.Header(Component.translatable("valence.info.section.changelog")));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.192"), 4));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.191"), 5));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.190"), 7));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.181"), 6));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.180"), 8));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.170"), 8));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.160"), 10));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.150"), 9));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.140"), 12));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.130"), 12));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.120"), 8));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.110"), 8));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.changelog.100"), 6));
      this.list.add(new OptionList.Header(Component.translatable("valence.info.section.credits")));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.credits"), 5));
      this.list.add(new OptionList.Info(() -> Component.translatable("valence.info.credits.cord"), 2));
   }

   @Override
   public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
      this.list.render(g, mx, my);
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      return this.list.mouseScrolled(mx, my, amount);
   }
}
