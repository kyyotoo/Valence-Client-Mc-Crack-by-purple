package dev.valence.gui.pages;

import dev.valence.gui.Anim;
import dev.valence.gui.NowPlaying;
import dev.valence.gui.Page;
import dev.valence.gui.RoundedImage;
import dev.valence.gui.Theme;
import dev.valence.gui.UiSounds;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class OverviewPage extends Page {
   private static final List<OverviewPage.Card> CARDS = List.of(
      new OverviewPage.Card("sky", "valence.page.sky", SkyPage.class),
      new OverviewPage.Card("music", "valence.page.lyrics", LyricsPage.class),
      new OverviewPage.Card("sword", "valence.page.styles", StylesPage.class),
      new OverviewPage.Card("bolt", "valence.page.performance", PerformancePage.class),
      new OverviewPage.Card("display", "valence.page.hud", HudPage.class),
      new OverviewPage.Card("gear", "valence.page.settings", SettingsPage.class)
   );
   private final NowPlaying nowPlaying = new NowPlaying(63);
   private static final String DISCORD = "discord.gg/valencemod";
   private static final int DISCORD_SPACE = 13;

   private int discordY() {
      return this.cardsBottom() + 16 + 52 + 5;
   }

   private int cardsBottom() {
      int bh = this.bannerH();
      int top = this.y + bh + 8;
      int rows = (CARDS.size() + 2) / 3;
      int chh = this.cardH(top);
      return top + rows * chh + (rows - 1) * 7;
   }

   private int cardH(int top) {
      int rows = (CARDS.size() + 2) / 3;
      int free = this.y + this.h - top - 7 * (rows - 1) - 75;
      return Math.max(34, Math.min(62, free / rows));
   }

   @Override
   public String nameKey() {
      return "valence.page.overview";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.overview.subtitle";
   }

   @Override
   public String icon() {
      return "home";
   }

   private int bannerH() {
      return Math.max(40, Math.min(64, this.h / 4));
   }

   @Override
   public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
      Font f = this.font();
      int bh = this.bannerH();
      float srcH = Math.min(220.0F, bh * 800.0F / this.w);
      RoundedImage.draw(g, Theme.BANNER, this.x, this.y, this.w, bh, 0.0F, (220.0F - srcH) / 2.0F, 800.0F, srcH, -1);
      Theme.sheen(g, this.x, this.y, this.x + this.w, this.y + bh, 0.6F);
      Theme.outline(g, this.x, this.y, this.x + this.w, this.y + bh, 1086959871);
      Theme.glow(g, this.x + 12, this.y + bh / 2 - 15, this.x + 42, this.y + bh / 2 + 15, -7643914, 0.4F);
      Theme.image(g, Theme.LOGO, this.x + 12, this.y + bh / 2 - 15, 30, 30, 128, 128, -1);
      g.pose().pushMatrix();
      g.pose().translate(this.x + 50, this.y + bh / 2.0F - 10.0F);
      g.pose().scale(1.5F, 1.5F);
      g.text(f, "Valence", 0, 0, -1, true);
      g.pose().popMatrix();
      g.text(f, Component.translatable("valence.menu.tagline"), this.x + 50, this.y + bh / 2 + 5, -4608298, true);
      int top = this.y + bh + 8;
      int cols = 3;
      int gap = 7;
      int cw = (this.w - gap * (cols - 1)) / cols;
      int rows = (CARDS.size() + cols - 1) / cols;
      int chh = this.cardH(top);

      for (int i = 0; i < CARDS.size(); i++) {
         OverviewPage.Card c = CARDS.get(i);
         int cx = this.x + i % cols * (cw + gap);
         int cy = top + i / cols * (chh + gap);
         float hv = Anim.hover(Anim.key(30, i), this.inside(mx, my, cx, cy, cx + cw, cy + chh));
         cy -= Math.round(hv * 1.5F);
         Theme.card(g, cx, cy, cx + cw, cy + chh, hv, false);
         Theme.glow(g, cx + 8, cy + 8, cx + 22, cy + 22, -7643914, 0.2F + 0.3F * hv);
         Theme.pill(g, cx + 6, cy + 6, cx + 24, cy + 24, Theme.lerp(864771318, 2005622006, hv));
         Theme.icon(g, c.icon(), cx + 9, cy + 9, 12, Theme.lerp(-4745985, -1, hv));
         g.text(f, Component.translatable(c.key()), cx + 30, cy + 10, -724737, false);
         g.pose().pushMatrix();
         g.pose().translate(cx + 30, cy + 22);
         g.pose().scale(0.75F, 0.75F);
         g.textWithWordWrap(f, Component.translatable(c.key() + ".desc"), 0, 0, Math.round((cw - 38) / 0.75F), Theme.lerp(-8095322, -4608298, hv), false);
         g.pose().popMatrix();
         Theme.icon(g, "arrow", cx + cw - 15 + Math.round(hv * 3.0F), cy + chh - 13, 9, Theme.lerp(-8095322, -4745985, hv));
      }

      int ny = this.cardsBottom() + 9;
      g.pose().pushMatrix();
      g.pose().translate(this.x + 4, ny - 2);
      g.pose().scale(0.75F, 0.75F);
      g.text(f, Component.translatable("valence.overview.music"), 0, 0, -8095322, false);
      g.pose().popMatrix();
      this.nowPlaying.render(g, this.x, ny + 7, this.w, mx, my);
      int dw = f.width("discord.gg/valencemod");
      int dx = this.x + this.w / 2 - dw / 2;
      int dy = this.discordY();
      int line = Theme.withAlpha(-1, 0.12F);
      g.fill(this.x + this.w / 2 - dw / 2 - 26, dy + 4, this.x + this.w / 2 - dw / 2 - 8, dy + 5, line);
      g.fill(this.x + this.w / 2 + dw / 2 + 8, dy + 4, this.x + this.w / 2 + dw / 2 + 26, dy + 5, line);
      g.text(f, "discord.gg/valencemod", dx, dy, Theme.withAlpha(-1, 0.55F), false);
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      if (this.nowPlaying.click(mx, my, this.x, this.cardsBottom() + 16, this.w)) {
         return true;
      }

      int bh = this.bannerH();
      int top = this.y + bh + 8;
      int cols = 3;
      int gap = 7;
      int cw = (this.w - gap * (cols - 1)) / cols;
      int rows = (CARDS.size() + cols - 1) / cols;
      int chh = this.cardH(top);

      for (int i = 0; i < CARDS.size(); i++) {
         int cx = this.x + i % cols * (cw + gap);
         int cy = top + i / cols * (chh + gap);
         if (this.inside(mx, my, cx, cy, cx + cw, cy + chh)) {
            UiSounds.click();
            this.screen.openPage(this.screen.pageIndexOf(CARDS.get(i).target()));
            return true;
         }
      }

      return false;
   }

   private record Card(String icon, String key, Class<? extends Page> target) {
   }
}
