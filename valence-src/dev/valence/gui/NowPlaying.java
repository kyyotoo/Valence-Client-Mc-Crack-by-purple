package dev.valence.gui;

import dev.valence.cord.NeonLyrics;
import dev.valence.cord.hud.Cover;
import dev.valence.cord.lyrics.SpotifyClient;
import dev.valence.hud.ValenceHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class NowPlaying {
   public static final int HEIGHT = 52;
   private static final Identifier PLAY = Theme.id("textures/gui/icons/hd_play.png");
   private static final Identifier PAUSE = Theme.id("textures/gui/icons/hd_pause.png");
   private static final Identifier NEXT = Theme.id("textures/gui/icons/hd_next.png");
   private static final Identifier PREV = Theme.id("textures/gui/icons/hd_prev.png");
   private final int keyGroup;
   private String shownKey = "";
   private String oldTitle = "";
   private String oldArtist = "";
   private long changeNanos;
   private int changeDir = 1;
   private final long[] pressNanos = new long[3];

   public NowPlaying(int keyGroup) {
      this.keyGroup = keyGroup;
   }

   private static float since(long nanos) {
      return (float)(System.nanoTime() - nanos) / 1.0E9F;
   }

   private static int buttonX(int x, int w, int i) {
      int right = x + w - 10;

      return switch (i) {
         case 0 -> right - 82;
         case 1 -> right - 55;
         default -> right - 24;
      };
   }

   private static int buttonSize(int i) {
      return i == 1 ? 26 : 22;
   }

   public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
      Font f = Minecraft.getInstance().font;
      dev.valence.cord.lyrics.NowPlaying m = SpotifyClient.now();
      boolean empty = !m.hasTrack();
      boolean playing = !empty && m.playing;
      String key = empty ? "" : m.trackId;
      float play = Anim.approach(Anim.key(this.keyGroup, 90), playing ? 1.0F : 0.0F, 10.0F);
      Theme.card(g, x, y, x + w, y + 52, 0.0F, false);
      Theme.glow(g, x + 7, y + 7, x + 43, y + 43, -7643914, 0.2F + 0.2F * play);
      Theme.round(g, x + 7, y + 7, x + 43, y + 43, 1435196662);
      Theme.sheen(g, x + 7, y + 7, x + 43, y + 43, 0.6F);
      if (!empty && NeonLyrics.cfg().albumCover && Cover.drawSquare(g, x + 7, y + 7, 36, 1.0F)) {
         Theme.outline(g, x + 7, y + 7, x + 43, y + 43, 872415231);
      } else if (!empty) {
         float time = (float)System.nanoTime() / 1.0E9F;

         for (int i = 0; i < 4; i++) {
            float live = 0.5F + 0.5F * (float)Math.sin(time * (5.0F + i * 1.7F) + i * 1.3F);
            float hgt = 4.0F + (2.0F + 9.0F * live) * play + 2.0F * (1.0F - play);
            int bx = x + 14 + i * 6;
            Theme.pill(g, bx, Math.round(y + 35 - hgt), bx + 4, y + 35, -1);
         }
      } else {
         Theme.icon(g, "music", x + 17, y + 17, 16, -1);
      }

      if (!key.equals(this.shownKey)) {
         if (!this.shownKey.isEmpty()) {
            this.changeNanos = System.nanoTime();
            this.changeDir = this.skipping() < 0 ? -1 : 1;
         }

         this.shownKey = key;
      }

      int skipping = this.skipping();
      int textX = x + 51;
      int textW = w - 170;
      g.enableScissor(textX, y + 4, textX + textW, y + 30);
      float ct = Theme.easeOutCubic(Math.min(1.0F, since(this.changeNanos) / 0.35F));
      if (empty) {
         g.text(f, f.plainSubstrByWidth(Component.translatable("valence.lyrics.status.nothing").getString(), textW), textX, y + 9, -724737, false);
         g.pose().pushMatrix();
         g.pose().translate(textX, y + 21);
         g.pose().scale(0.8F, 0.8F);
         g.text(f, Component.translatable("valence.lyrics.status.hint"), 0, 0, -8095322, false);
         g.pose().popMatrix();
      } else if (skipping != 0) {
         float k = Theme.easeOutCubic(Math.min(1.0F, since(this.skipStart()) / 0.25F));
         drawTitle(g, f, m.title, m.artist, textX - Math.round(k * 14.0F * skipping), y, textW, 1.0F - 0.7F * k);
         this.oldTitle = m.title;
         this.oldArtist = m.artist;
      } else if (ct < 1.0F) {
         drawTitle(g, f, this.oldTitle, this.oldArtist, textX - Math.round((0.3F + ct) * 18.0F * this.changeDir), y, textW, (1.0F - ct) * 0.3F);
         drawTitle(g, f, m.title, m.artist, textX + Math.round((1.0F - ct) * 18.0F * this.changeDir), y, textW, ct);
      } else {
         drawTitle(g, f, m.title, m.artist, textX, y, textW, 1.0F);
         this.oldTitle = m.title;
         this.oldArtist = m.artist;
      }

      g.disableScissor();
      String state = empty ? "none" : m.lyricsState.name().toLowerCase();
      Component st = Component.translatable("valence.lyrics.state." + state);
      int var10000;
      if (empty) {
         var10000 = -8095322;
      } else {
         switch (m.lyricsState) {
            case SYNCED:
               var10000 = -11870592;
               break;
            case LOADING:
            case NONE:
               var10000 = -8095322;
               break;
            case NOT_FOUND:
               var10000 = -278748;
               break;
            default:
               var10000 = -495247;
         }
      }

      int stColor = var10000;
      g.pose().pushMatrix();
      g.pose().translate(textX, y + 31);
      g.pose().scale(0.75F, 0.75F);
      int sw = f.width(st);
      Theme.pill(g, -2, -2, sw + 14, 9, Theme.withAlpha(stColor, 0.16F));
      Theme.pill(g, 2, 2, 5, 5, stColor);
      g.text(f, st, 9, 0, stColor, false);
      if (!empty) {
         long pos = m.progressNow(0);
         String time = ValenceHud.formatTime(pos) + (m.durationMs > 0L ? " / " + ValenceHud.formatTime(m.durationMs) : "");
         g.text(f, time, sw + 22, 0, -8095322, false);
      }

      g.pose().popMatrix();
      if (!empty && m.durationMs > 0L) {
         float k = skipping != 0 ? 0.0F : Math.min(1.0F, (float)m.progressNow(0) / (float)m.durationMs);
         float shown = Anim.approach(Anim.key(this.keyGroup, 91), k, skipping != 0 ? 8.0F : 12.0F);
         Theme.pill(g, textX, y + 43, x + w - 10, y + 46, 872415231);
         Theme.pill(g, textX, y + 43, Math.max(textX + 3, textX + Math.round((x + w - 10 - textX) * shown)), y + 46, -4745985);
      }

      for (int i = 0; i < 3; i++) {
         int size = buttonSize(i);
         int bx = buttonX(x, w, i);
         int by = y + 8 + (26 - size) / 2;
         boolean hovered = mx >= bx && mx < bx + size && my >= by && my < by + size;
         float hv = Anim.hover(Anim.key(this.keyGroup, 92 + i), hovered);
         float press = since(this.pressNanos[i]);
         float squeeze = press < 0.32F ? (float)(Math.sin(Math.min(1.0F, press / 0.32F) * Math.PI) * 0.12F) : 0.0F;
         float s = 1.0F + 0.05F * hv - squeeze;
         g.pose().pushMatrix();
         g.pose().translate(bx + size / 2.0F, by + size / 2.0F);
         g.pose().scale(s, s);
         int h2 = size / 2;
         if (i == 1) {
            Theme.glow(g, -h2, -h2, h2, h2, -7643914, 0.3F + 0.35F * hv);
            Theme.round(g, -h2, -h2, h2, h2, Theme.lerp(-7643914, -6259713, hv));
            Theme.sheen(g, -h2, -h2, h2, h2, 0.6F);
         } else {
            Theme.round(g, -h2, -h2, h2, h2, Theme.lerp(536870911, 956301311, hv));
            Theme.outline(g, -h2, -h2, h2, h2, Theme.lerp(654311423, 1439281407, hv));
         }

         int is = i == 1 ? 13 : 11;
         if (i == 1) {
            float pa = play;
            drawIcon(g, PAUSE, is, pa, 0.7F + 0.3F * pa);
            drawIcon(g, PLAY, is, 1.0F - pa, 0.7F + 0.3F * (1.0F - pa));
         } else {
            drawIcon(g, i == 0 ? PREV : NEXT, is, 0.85F + 0.15F * hv, 1.0F);
         }

         g.pose().popMatrix();
      }
   }

   private int skipping() {
      if (since(this.pressNanos[0]) < 0.6F) {
         return -1;
      } else {
         return since(this.pressNanos[2]) < 0.6F ? 1 : 0;
      }
   }

   private long skipStart() {
      return Math.max(this.pressNanos[0], this.pressNanos[2]);
   }

   private static void drawIcon(GuiGraphicsExtractor g, Identifier icon, int size, float alpha, float scale) {
      if (!(alpha <= 0.02F)) {
         g.pose().pushMatrix();
         g.pose().scale(scale, scale);
         Theme.image(g, icon, -size / 2, -size / 2, size, size, 64, 64, Theme.withAlpha(-1, alpha));
         g.pose().popMatrix();
      }
   }

   private static void drawTitle(GuiGraphicsExtractor g, Font f, String title, String artist, int x, int y, int w, float alpha) {
      if (!(alpha <= 0.02F)) {
         g.text(f, f.plainSubstrByWidth(title, w), x, y + 8, Theme.withAlpha(-724737, alpha), false);
         g.text(f, f.plainSubstrByWidth(artist, w), x, y + 19, Theme.withAlpha(-4608298, alpha), false);
      }
   }

   public boolean click(double mx, double my, int x, int y, int w) {
      for (int i = 0; i < 3; i++) {
         int size = buttonSize(i);
         int bx = buttonX(x, w, i);
         int by = y + 8 + (26 - size) / 2;
         if (mx >= bx && mx < bx + size && my >= by && my < by + size) {
            this.pressNanos[i] = System.nanoTime();
            switch (i) {
               case 0:
                  SpotifyClient.control(SpotifyClient.Cmd.PREVIOUS);
                  break;
               case 1:
                  SpotifyClient.control(SpotifyClient.Cmd.PLAYPAUSE);
                  break;
               default:
                  SpotifyClient.control(SpotifyClient.Cmd.NEXT);
            }

            UiSounds.click();
            return true;
         }
      }

      return false;
   }
}
