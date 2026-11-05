package dev.valence.license;

import dev.valence.gui.Anim;
import dev.valence.gui.OptionList;
import dev.valence.gui.Theme;
import dev.valence.gui.UiSounds;
import dev.valence.gui.ValenceScreen;
import dev.valence.styles.StyleManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

public class LicenseScreen extends Screen {
   private static final int W = 280;
   private static final int H = 190;
   private static final int OK_COLOR = -9508952;
   private static final int BAD_COLOR = -495247;
   private final Screen parent;
   private final boolean openMenuAfter;
   private EditBox field;
   private long openedAt;
   private boolean busy;
   private long busySince;
   private String message = "";
   private int messageColor = -8095322;
   private long messageAt;
   private long shakeAt;
   private long successAt;
   private String lastText = "";

   public LicenseScreen(Screen parent, boolean openMenuAfter) {
      super(Component.translatable("valence.license.title"));
      this.parent = parent;
      this.openMenuAfter = openMenuAfter;
   }

   private int px() {
      return (this.width - 280) / 2;
   }

   private int py() {
      return (this.height - 190) / 2;
   }

   protected void init() {
      if (this.openedAt == 0L) {
         this.openedAt = System.currentTimeMillis();
      }

      String keep = this.field != null ? this.field.getValue() : "";
      this.field = new EditBox(this.font, this.px() + 58, this.py() + 100, 180, 12, Component.translatable("valence.license.field"));
      this.field.setBordered(false);
      this.field.setMaxLength(9);
      this.field.setTextColor(this.successAt != 0L ? -9508952 : -1);
      this.field.setValue(keep);
      this.field.setHint(Component.literal("XXXX-XXXX").withColor(-9542008));
      this.field.setResponder(this::format);
      this.addRenderableWidget(this.field);
      this.setInitialFocus(this.field);
   }

   private void format(String s) {
      String n = License.normalize(s);
      if (n.length() > 8) {
         n = n.substring(0, 8);
      }

      String f = n.length() > 4 ? n.substring(0, 4) + "-" + n.substring(4) : n;
      if (!f.equals(s)) {
         this.field.setValue(f);
         this.field.moveCursorToEnd(false);
      }

      if (!f.equals(this.lastText)) {
         this.lastText = f;
         if (!this.busy && this.successAt == 0L && this.messageColor == -495247) {
            this.message = "";
         }
      }
   }

   private void redeem() {
      if (!this.busy && this.successAt == 0L) {
         String code = this.field.getValue();
         if (License.normalize(code).length() != 8) {
            this.fail(Component.translatable("valence.license.short").getString());
         } else {
            this.busy = true;
            this.busySince = System.currentTimeMillis();
            UiSounds.click();
            License.redeem(code).thenAccept(r -> Minecraft.getInstance().execute(() -> this.done(r)));
         }
      }
   }

   private void done(License.Result r) {
      this.busy = false;
      switch (r) {
         case OK:
            this.successAt = System.currentTimeMillis();
            this.message = Component.translatable("valence.license.ok").getString();
            this.messageColor = -9508952;
            this.messageAt = this.successAt;
            this.field.setTextColor(-9508952);
            this.field.setTextColorUneditable(-9508952);
            this.field.setEditable(false);
            play(SoundEvents.NOTE_BLOCK_BELL, 1.5F, 0.9F);
            play(SoundEvents.NOTE_BLOCK_CHIME, 2.0F, 0.5F);

            try {
               StyleManager.markDirty();
               StyleManager.apply();
            } catch (Throwable var3) {
            }
            break;
         case USED:
            this.fail(Component.translatable("valence.license.used").getString());
            break;
         case OFFLINE:
            this.fail(Component.translatable("valence.license.offline").getString());
            break;
         case INVALID:
            this.fail(Component.translatable("valence.license.invalid").getString());
            break;
         default:
            this.fail(Component.translatable("valence.license.error").getString());
      }
   }

   private void fail(String text) {
      this.message = text;
      this.messageColor = -495247;
      this.messageAt = System.currentTimeMillis();
      this.shakeAt = this.messageAt;
      play(SoundEvents.NOTE_BLOCK_BASS, 0.75F, 1.0F);
      play(SoundEvents.NOTE_BLOCK_BASS, 0.6F, 0.7F);
   }

   private static void play(Holder<SoundEvent> sound, float pitch, float volume) {
      try {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)sound.value(), pitch, volume));
      } catch (Throwable var4) {
      }
   }

   private float closing(long now) {
      return this.successAt == 0L ? 0.0F : Theme.easeOutCubic(Math.max(0.0F, Math.min(1.0F, (float)(now - this.successAt - 700L) / 420.0F)));
   }

   public void tick() {
      super.tick();
      if (this.successAt != 0L && System.currentTimeMillis() - this.successAt > 1150L) {
         Minecraft mc = Minecraft.getInstance();
         mc.gui.setScreen(this.openMenuAfter ? new ValenceScreen(this.parent) : this.parent);
      }
   }

   public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
      if (this.minecraft != null && this.minecraft.level != null) {
         super.extractBackground(g, mouseX, mouseY, partialTick);
      }

      long now = System.currentTimeMillis();
      float e = Theme.easeOutCubic(Math.min(1.0F, (float)(now - this.openedAt) / 260.0F)) * (1.0F - this.closing(now));
      g.fillGradient(0, 0, this.width, this.height, Theme.withAlpha(-1945697265, e), Theme.withAlpha(-1291320302, e));
   }

   private void centered(GuiGraphicsExtractor g, Font f, Component text, int cx, int y, float scale, int maxW, int color) {
      List<FormattedCharSequence> lines = f.split(text, Math.round(maxW / scale));

      for (int i = 0; i < lines.size(); i++) {
         g.pose().pushMatrix();
         g.pose().translate(cx, y + i * 10 * scale);
         g.pose().scale(scale, scale);
         g.centeredText(f, lines.get(i), 0, 0, color);
         g.pose().popMatrix();
      }
   }

   public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
      long now = System.currentTimeMillis();
      float close = this.closing(now);
      float open = Theme.easeOutCubic(Math.min(1.0F, (float)(now - this.openedAt) / 360.0F)) * (1.0F - close);
      Font f = this.font;
      int x1 = this.px();
      int y1 = this.py();
      int lx = x1 + 140;
      float shake = this.shakeAt == 0L ? 0.0F : Math.max(0.0F, 1.0F - (float)(now - this.shakeAt) / 420.0F);
      float fsx = (float)Math.sin((now - this.shakeAt) / 22.0) * 6.0F * shake * shake;
      float good = this.successAt == 0L ? 0.0F : Math.min(1.0F, (float)(now - this.successAt) / 250.0F);
      g.pose().pushMatrix();
      float sc = 0.94F + 0.06F * open;
      g.pose().translate(this.width / 2.0F, this.height / 2.0F + (1.0F - open) * 10.0F);
      g.pose().scale(sc, sc);
      g.pose().translate(-this.width / 2.0F, -this.height / 2.0F);
      Theme.glass(g, x1, y1, x1 + 280, y1 + 190, Theme.withAlpha(-1207105001, 0.4F + 0.6F * open), Theme.withAlpha(1086959871, open), 0.55F * open);
      Theme.sheen(g, x1, y1, x1 + 280, y1 + 40, 0.35F * open);
      float pulse = 0.3F + 0.1F * (float)Math.sin(now / 600.0);
      Theme.glow(g, lx - 15, y1 + 14, lx + 15, y1 + 44, -7643914, pulse * open);
      Theme.image(g, Theme.LOGO, lx - 14, y1 + 15, 28, 28, 128, 128, Theme.withAlpha(-1, open));
      g.pose().pushMatrix();
      g.pose().translate(lx, y1 + 50);
      g.pose().scale(1.6F, 1.6F);
      g.centeredText(f, Component.literal("VALENCE").withStyle(s -> s.withBold(true)), 0, 0, Theme.withAlpha(-1, open));
      g.pose().popMatrix();
      this.centered(g, f, Component.translatable("valence.license.subtitle"), lx, y1 + 69, 0.8F, 250, Theme.withAlpha(-4608298, open));
      int fx1 = x1 + 34;
      int fy1 = y1 + 92;
      int fx2 = x1 + 280 - 34;
      int fy2 = fy1 + 24;
      float fh = Anim.approach(Anim.key(9100, 0), this.field.isFocused() ? 1.0F : 0.0F, 12.0F);
      int line = Theme.lerp(872415231, -1433707274, fh);
      line = Theme.lerp(line, -495247, shake);
      line = Theme.lerp(line, -9508952, good);
      g.pose().pushMatrix();
      g.pose().translate(fsx, 0.0F);
      if (fh > 0.01F && good == 0.0F && shake == 0.0F) {
         Theme.glow(g, fx1, fy1, fx2, fy2, -7643914, 0.25F * fh * open);
      }

      if (shake > 0.01F) {
         Theme.glow(g, fx1, fy1, fx2, fy2, -495247, 0.4F * shake);
      }

      if (good > 0.01F) {
         Theme.glow(g, fx1, fy1, fx2, fy2, -9508952, 0.4F * good * open);
      }

      Theme.pill(g, fx1, fy1, fx2, fy2, Theme.withAlpha(1073741824, open));
      Theme.pillLine(g, fx1, fy1, fx2, fy2, Theme.withAlpha(line, open));
      int iconCol = good > 0.0F ? -9508952 : Theme.lerp(Theme.lerp(-8095322, -4745985, fh), -495247, shake);
      Theme.icon(g, good > 0.0F ? "check" : "star", fx1 + 8, fy1 + 7, 10, Theme.withAlpha(iconCol, open));
      g.pose().popMatrix();
      this.field.setX(Math.round(fx1 + 24 + fsx));
      this.field.setY(fy1 + 8);
      this.field.setWidth(fx2 - fx1 - 32);
      this.field.setAlpha(open);
      super.extractRenderState(g, mx, my, pt);
      int bw = 124;
      int bx1 = lx - bw / 2;
      int by1 = y1 + 128;
      Component label;
      if (this.successAt != 0L) {
         label = Component.translatable("valence.license.done");
      } else if (this.busy) {
         int dots = (int)((now - this.busySince) / 300L % 4L);
         label = Component.literal(Component.translatable("valence.license.checking").getString() + ".".repeat(dots));
      } else {
         label = Component.translatable("valence.license.redeem");
      }

      if (open > 0.05F) {
         OptionList.accentButton(g, bx1, by1, bx1 + bw, by1 + 20, label, mx, my, Anim.key(9100, 1));
      }

      int ty = y1 + 158;
      if (!this.message.isEmpty()) {
         float ma = Math.min(1.0F, (float)(now - this.messageAt) / 200.0F) * open;
         g.pose().pushMatrix();
         g.pose().translate(fsx, (1.0F - ma) * 3.0F);
         this.centered(g, f, Component.literal(this.message), lx, ty, 0.8F, 250, Theme.withAlpha(this.messageColor, ma));
         g.pose().popMatrix();
      } else {
         this.centered(g, f, Component.translatable("valence.license.hint"), lx, ty, 0.7F, 250, Theme.withAlpha(-8095322, open));
      }

      g.pose().popMatrix();
   }

   private boolean overButton(double mx, double my) {
      int lx = this.px() + 140;
      int by1 = this.py() + 128;
      return mx >= lx - 62 && mx < lx + 62 && my >= by1 && my < by1 + 20;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (event.button() == 0 && this.overButton(event.x(), event.y())) {
         this.redeem();
         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }

   public boolean keyPressed(KeyEvent event) {
      if (event.key() != 40 && event.key() != 88) {
         return super.keyPressed(event);
      }

      this.redeem();
      return true;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      Minecraft.getInstance().gui.setScreen(this.parent);
   }
}
