package dev.valence.gui.pages;

import dev.valence.config.ValenceConfig;
import dev.valence.gui.Anim;
import dev.valence.gui.OptionList;
import dev.valence.gui.RoundedImage;
import dev.valence.gui.Theme;
import dev.valence.gui.UiScale;
import dev.valence.gui.UiSounds;
import dev.valence.sky.SkyDefinition;
import dev.valence.sky.SkyRegistry;
import dev.valence.sky.ValenceSky;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.dimension.DimensionType.Skybox;

public class SkyPreviewScreen extends Screen {
   private final Screen parent;
   private SkyDefinition sky;
   private float startYaw;
   private float startPitch;
   private boolean savedHideGui;
   private boolean saved;
   private float yaw;
   private float pitch;
   private float targetYaw;
   private float targetPitch;
   private long lastDrag;
   private float fade;
   private boolean leaving;
   private boolean apply;
   private boolean turning;
   private float flyFromYaw;
   private float flyFromPitch;
   private float flyT = 1.0F;
   private float leaveYaw;
   private float leavePitch;
   private final OptionList options = new OptionList();
   private boolean draggingOption;
   private boolean dragging;
   private int panelX1;
   private int panelY1;
   private int panelX2;
   private int panelY2;
   private float ui = 1.0F;
   private int W = 1;
   private int H = 1;

   public SkyPreviewScreen(Screen parent, SkyDefinition sky) {
      super(Component.translatable("valence.sky.preview"));
      this.parent = parent;
      this.sky = sky;
   }

   protected void init() {
      this.ui = UiScale.factor();
      this.W = Math.max(1, Math.round(this.width / this.ui));
      this.H = Math.max(1, Math.round(this.height / this.ui));
      LocalPlayer p = this.minecraft.player;
      if (!this.saved) {
         this.saved = true;
         this.savedHideGui = this.minecraft.gui.hud.isHidden();
         if (p != null) {
            this.startYaw = p.getYRot();
            this.startPitch = p.getXRot();
            this.yaw = this.startYaw;
            this.pitch = this.startPitch;
         }

         this.show(this.sky);
      }

      if (!this.minecraft.gui.hud.isHidden()) {
         this.minecraft.gui.hud.toggle();
      }

      int pw = Math.min(236, this.W / 3);
      this.panelX2 = this.W - 14;
      this.panelX1 = this.panelX2 - pw;
      this.panelY1 = 12;
      this.panelY2 = Math.min(this.H - 80, this.panelY1 + 26 + 216 + 6);
      this.options.setBounds(this.panelX1 + 6, this.panelY1 + 22, pw - 12, this.panelY2 - this.panelY1 - 26);
      this.options.clear();
      ValenceConfig c = ValenceConfig.get();
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
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.animations"), null, () -> c.skyAnimations, v -> c.skyAnimations = v));
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.stars"), null, () -> c.skyStars, v -> c.skyStars = v));
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.shooting"), null, () -> c.skyShootingStars, v -> c.skyShootingStars = v));
      this.options
         .add(new OptionList.Toggle(Component.translatable("valence.sky.constellations"), null, () -> c.skyConstellations, v -> c.skyConstellations = v));
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.particles"), null, () -> c.skyParticles, v -> c.skyParticles = v));
      this.options.add(new OptionList.Toggle(Component.translatable("valence.sky.lightning"), null, () -> c.skyLightning, v -> c.skyLightning = v));
   }

   private void show(SkyDefinition s) {
      this.sky = s;
      ValenceSky.setPreview(s);
      float az = s.view != null && s.view.length > 0 ? s.view[0] : 0.0F;
      float el = s.view != null && s.view.length > 1 ? s.view[1] : 20.0F;
      this.targetYaw = closestAngle(this.yaw, az - 180.0F);
      this.targetPitch = -Math.max(5.0F, Math.min(60.0F, el + 2.0F));
      this.lastDrag = 0L;
      this.turning = true;
      this.flyFromYaw = this.yaw;
      this.flyFromPitch = this.pitch;
      this.flyT = 0.0F;
   }

   private static float closestAngle(float from, float to) {
      float d = (to - from) % 360.0F;
      if (d > 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return from + d;
   }

   private void step(int dir) {
      List<SkyDefinition> all = SkyRegistry.all();
      int i = Math.max(0, all.indexOf(this.sky));
      i = (i + dir + all.size()) % all.size();
      this.show(all.get(i));
      UiSounds.click();
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
   }

   public void extractRenderState(GuiGraphicsExtractor g, int rawMouseX, int rawMouseY, float partialTick) {
      int mouseX = Math.round(rawMouseX / this.ui);
      int mouseY = Math.round(rawMouseY / this.ui);
      Anim.frame();
      float dt = Anim.dt();
      this.fade = this.leaving ? Math.max(0.0F, this.fade - dt * 2.4F) : Math.min(1.0F, this.fade + dt * 4.0F);
      if (this.leaving && this.fade <= 0.0F) {
         this.finish();
      } else {
         LocalPlayer p = this.minecraft.player;
         if (p != null) {
            float camYaw;
            float camPitch;
            if (this.leaving) {
               float k = Theme.easeInOutCubic(1.0F - this.fade);
               camYaw = this.leaveYaw + (this.startYaw - this.leaveYaw) * k;
               camPitch = this.leavePitch + (this.startPitch - this.leavePitch) * k;
            } else {
               this.flyT = Math.min(1.0F, this.flyT + dt / 1.3F);
               float k = Theme.easeInOutCubic(this.flyT);
               float baseYaw = this.flyFromYaw + (this.targetYaw - this.flyFromYaw) * k;
               float basePitch = this.flyFromPitch + (this.targetPitch - this.flyFromPitch) * k;
               this.yaw = baseYaw;
               this.pitch = basePitch;
               camYaw = baseYaw;
               camPitch = Math.max(-89.0F, Math.min(89.0F, basePitch));
               this.leaveYaw = camYaw;
               this.leavePitch = camPitch;
            }

            p.setYRot(camYaw);
            p.setXRot(camPitch);
            p.yRotO = camYaw;
            p.xRotO = camPitch;
            p.yHeadRot = camYaw;
            p.yHeadRotO = camYaw;
         }

         Font f = this.font;
         float e = Theme.easeOutCubic(this.fade);
         g.pose().pushMatrix();
         g.pose().scale(this.ui, this.ui);
         RoundedImage.menuScale = this.ui;
         Component label = Component.translatable("valence.sky.preview.title");
         int lw = f.width(label) + 21;
         int ly = Math.round(12.0F - (1.0F - e) * 8.0F);
         Theme.glass(g, 12, ly, 12 + lw, ly + 16, Theme.withAlpha(-1207105001, e), Theme.withAlpha(1439281407, e), 0.4F * e);
         Theme.glow(g, 18, ly + 6, 22, ly + 10, -7643914, 0.6F * e);
         Theme.pill(g, 18, ly + 6, 22, ly + 10, Theme.withAlpha(-4745985, e));
         g.text(f, label, 27, ly + 4, Theme.withAlpha(-724737, e), false);
         float slide = (1.0F - e) * 24.0F;
         g.pose().pushMatrix();
         g.pose().translate(slide, 0.0F);
         Theme.glass(g, this.panelX1, this.panelY1, this.panelX2, this.panelY2, Theme.withAlpha(-1005778409, e), Theme.withAlpha(1439281407, e), 0.6F * e);
         Theme.icon(g, "gear", this.panelX1 + 9, this.panelY1 + 7, 9, Theme.withAlpha(-4745985, e));
         g.text(f, Component.translatable("valence.sky.preview.settings"), this.panelX1 + 22, this.panelY1 + 8, Theme.withAlpha(-724737, e), false);
         if (e > 0.6F) {
            this.options.render(g, Math.round(mouseX - slide), mouseY);
         }

         g.pose().popMatrix();
         int bw = Math.min(460, this.W - 32);
         int bh = 48;
         int bx = (this.W - bw) / 2;
         int by = Math.round(this.H - bh - 14 + (1.0F - e) * 20.0F);
         Theme.glass(g, bx, by, bx + bw, by + bh, Theme.withAlpha(-1005778409, e), Theme.withAlpha(1439281407, e), 0.6F * e);
         float h1 = Anim.hover(Anim.key(40, 0), inside(mouseX, mouseY, bx + 8, by + 15, bx + 26, by + 33));
         float h2 = Anim.hover(Anim.key(40, 1), inside(mouseX, mouseY, bx + 30, by + 15, bx + 48, by + 33));
         this.arrowButton(g, bx + 8, by + 15, h1, true, e);
         this.arrowButton(g, bx + 30, by + 15, h2, false, e);
         List<SkyDefinition> all = SkyRegistry.all();
         int idx = all.indexOf(this.sky) + 1;
         Component name = Component.translatable("valence.sky.name." + this.sky.id);
         g.pose().pushMatrix();
         g.pose().translate(bx + 58, by + 11);
         g.pose().scale(1.3F, 1.3F);
         g.text(f, name, 0, 0, Theme.withAlpha(-1, e), false);
         g.pose().popMatrix();
         g.pose().pushMatrix();
         g.pose().translate(bx + 58, by + 28);
         g.pose().scale(0.75F, 0.75F);
         boolean noSky = this.minecraft.level != null && this.minecraft.level.dimensionType().skybox() == Skybox.NONE;
         Component hint = noSky
            ? Component.translatable("valence.sky.preview.nosky")
            : Component.translatable("valence.sky.preview.hint", new Object[]{idx, all.size()});
         g.text(f, hint, 0, 0, Theme.withAlpha(noSky ? -278748 : -8095322, e), false);
         g.pose().popMatrix();
         boolean current = ValenceConfig.get().skyEnabled && this.sky.id.equals(ValenceConfig.get().skyId);
         Component applyText = Component.translatable(current ? "valence.sky.preview.active" : "valence.sky.preview.apply");
         Component backText = Component.translatable("valence.sky.preview.back");
         int aw = f.width(applyText) + 24;
         int bkw = f.width(backText) + 20;
         int ax = bx + bw - aw - 10;
         int bkx = ax - bkw - 6;
         if (e > 0.5F) {
            OptionList.glassButton(g, bkx, by + 16, bkx + bkw, by + 32, backText, mouseX, mouseY, Anim.key(40, 2));
            OptionList.accentButton(g, ax, by + 16, ax + aw, by + 32, applyText, mouseX, mouseY, Anim.key(40, 3));
         }

         g.pose().popMatrix();
         RoundedImage.menuScale = 1.0F;
      }
   }

   private void arrowButton(GuiGraphicsExtractor g, int x, int y, float hover, boolean left, float e) {
      Theme.pill(g, x, y, x + 18, y + 18, Theme.withAlpha(Theme.lerp(536870911, 1720409334, hover), e));
      Theme.pillLine(g, x, y, x + 18, y + 18, Theme.withAlpha(Theme.lerp(654311423, 1439281407, hover), e));
      g.pose().pushMatrix();
      g.pose().translate(x + 9, y + 9);
      if (left) {
         g.pose().rotate((float) Math.PI);
      }

      Theme.icon(g, "arrow", -4, -4, 8, Theme.withAlpha(-1, e));
      g.pose().popMatrix();
   }

   private static boolean inside(double mx, double my, int x1, int y1, int x2, int y2) {
      return mx >= x1 && mx < x2 && my >= y1 && my < y2;
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      double mx = event.x() / this.ui;
      double my = event.y() / this.ui;
      if (inside(mx, my, this.panelX1, this.panelY1, this.panelX2, this.panelY2)) {
         this.draggingOption = this.options.mouseClicked(mx, my, event.button());
         return true;
      } else {
         int bw = Math.min(460, this.W - 32);
         int bh = 48;
         int bx = (this.W - bw) / 2;
         int by = this.H - bh - 14;
         if (inside(mx, my, bx + 8, by + 15, bx + 26, by + 33)) {
            this.step(-1);
            return true;
         } else if (inside(mx, my, bx + 30, by + 15, bx + 48, by + 33)) {
            this.step(1);
            return true;
         } else {
            Font f = this.font;
            boolean current = ValenceConfig.get().skyEnabled && this.sky.id.equals(ValenceConfig.get().skyId);
            int aw = f.width(Component.translatable(current ? "valence.sky.preview.active" : "valence.sky.preview.apply")) + 24;
            int bkw = f.width(Component.translatable("valence.sky.preview.back")) + 20;
            int ax = bx + bw - aw - 10;
            int bkx = ax - bkw - 6;
            if (inside(mx, my, ax, by + 16, ax + aw, by + 32)) {
               this.applySky();
               return true;
            } else if (inside(mx, my, bkx, by + 16, bkx + bkw, by + 32)) {
               UiSounds.click();
               this.onClose();
               return true;
            } else {
               return true;
            }
         }
      }
   }

   private void applySky() {
      ValenceConfig cfg = ValenceConfig.get();
      cfg.skyId = this.sky.id;
      cfg.skyEnabled = true;
      ValenceConfig.save();
      UiSounds.toggle(true);
      this.apply = true;
      this.onClose();
   }

   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.draggingOption) {
         return this.options.mouseDragged(event.x() / this.ui, event.y() / this.ui);
      }

      if (event.button() != 0) {
         return false;
      }

      this.dragging = true;
      this.targetYaw += (float)dx * 0.35F;
      this.targetPitch = Math.max(-89.0F, Math.min(89.0F, this.targetPitch + (float)dy * 0.35F));
      if (this.flyT < 1.0F) {
         this.flyFromYaw += (float)dx * 0.35F;
         this.flyFromPitch = Math.max(-89.0F, Math.min(89.0F, this.flyFromPitch + (float)dy * 0.35F));
      }

      this.turning = false;
      this.lastDrag = System.currentTimeMillis();
      return true;
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.draggingOption) {
         this.draggingOption = false;
         this.options.mouseReleased();
         return true;
      } else {
         this.dragging = false;
         return super.mouseReleased(event);
      }
   }

   public boolean mouseScrolled(double rmx, double rmy, double scrollX, double scrollY) {
      double mx = rmx / this.ui;
      double my = rmy / this.ui;
      return inside(mx, my, this.panelX1, this.panelY1, this.panelX2, this.panelY2)
         ? this.options.mouseScrolled(mx, my, scrollY)
         : super.mouseScrolled(rmx, rmy, scrollX, scrollY);
   }

   public boolean keyPressed(KeyEvent event) {
      int key = event.key();
      if (key == 263) {
         this.step(-1);
         return true;
      }

      if (key == 262) {
         this.step(1);
         return true;
      }

      if (key != 257 && key != 335) {
         return super.keyPressed(event);
      }

      this.applySky();
      return true;
   }

   public void onClose() {
      if (!this.leaving) {
         this.leaving = true;
         if (!ValenceConfig.get().menuAnimations) {
            this.finish();
         }
      }
   }

   public void removed() {
      this.restore();
   }

   private void restore() {
      ValenceSky.setPreview(null);
      if (this.minecraft.gui.hud.isHidden() != this.savedHideGui) {
         this.minecraft.gui.hud.toggle();
      }

      LocalPlayer p = this.minecraft.player;
      if (p != null && this.saved) {
         p.setYRot(this.startYaw);
         p.setXRot(this.startPitch);
         p.yRotO = this.startYaw;
         p.xRotO = this.startPitch;
         p.yHeadRot = this.startYaw;
         p.yHeadRotO = this.startYaw;
      }
   }

   private void finish() {
      this.minecraft.gui.setScreen(this.parent);
   }
}
