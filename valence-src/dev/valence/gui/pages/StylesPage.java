package dev.valence.gui.pages;

import com.mojang.blaze3d.Blaze3D;
import dev.valence.config.ValenceConfig;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.hud.GlassHotbar;
import dev.valence.gui.Anim;
import dev.valence.gui.OptionList;
import dev.valence.gui.Page;
import dev.valence.gui.Preview3D;
import dev.valence.gui.Theme;
import dev.valence.gui.UiSounds;
import dev.valence.gui.ValenceScreen;
import dev.valence.styles.StyleCategory;
import dev.valence.styles.StyleManager;
import dev.valence.styles.StylePacks;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StylesPage extends Page {
   private static final int LIST_W = 112;
   private static final int ROW_H = 19;
   private static final int TILE = 62;
   private static final int BOTTOM = 24;
   private static final List<String> TOTEM_SIZES = List.of("normal", "small", "tiny");
   private int category = 0;
   private boolean hotbar = false;
   private static final ItemStack HOTBAR_ICON = new ItemStack(Items.CHEST);
   private float listScroll;
   private float gridScroll;
   private float gridTarget;
   private float listTarget;
   private List<StyleManager.Style> styles;
   private String hovered;
   private ItemStack[] icons;
   private static final String[] HOTBAR_KEYS = new String[]{
      "valence.styles.hotbar.vanilla",
      "valence.styles.hotbar.glass",
      "valence.styles.hotbar.minimal",
      "valence.styles.hotbar.dark",
      "valence.styles.hotbar.neon"
   };

   @Override
   public String nameKey() {
      return "valence.page.styles";
   }

   @Override
   public String subtitleKey() {
      return "valence.page.styles.subtitle";
   }

   @Override
   public String icon() {
      return "sword";
   }

   @Override
   public void init(ValenceScreen screen, int x, int y, int w, int h) {
      super.init(screen, x, y, w, h);
      this.styles = StyleManager.stylesFor(this.current());
      this.icons = new ItemStack[StyleCategory.ALL.size()];

      for (int i = 0; i < this.icons.length; i++) {
         this.icons[i] = new ItemStack(StyleCategory.ALL.get(i).icon());
      }
   }

   private StyleCategory current() {
      return StyleCategory.ALL.get(this.category);
   }

   private int inspectorW() {
      return this.w >= 400 ? 128 : 0;
   }

   private static float time() {
      return (float)(System.nanoTime() / 1000000L % 3600000L) / 1000.0F;
   }

   private void drawPreview(GuiGraphicsExtractor g, StyleCategory cat, String styleId, int bx, int by, int size, boolean turning) {
      float t = time();
      float yaw = turning ? t * 40.0F : 35.0F;
      float cx = bx + size / 2.0F;
      float cy = by + size / 2.0F;
      switch (cat.preview()) {
         case "crystal":
            StyleManager.Tex crystal = StyleManager.texture(cat, styleId, cat.textures().get(0));
            StyleCategory obsCat = StyleCategory.byId("obsidian");
            StyleManager.Tex obsidian = StyleManager.texture(obsCat, StyleManager.selected(obsCat), "textures/block/obsidian.png");
            Preview3D.crystal(g, crystal, obsidian, cx, by + size * 0.5F, size, turning ? t : 0.6F, turning ? t * 30.0F : 35.0F);
            break;
         case "cube": {
            StyleManager.Tex tex = StyleManager.texture(cat, styleId, StyleManager.mainPath(cat, styleId));
            Preview3D.block(g, tex, tex, cx, cy, size * 0.8F, turning ? yaw : 45.0F);
            break;
         }
         case "anchor":
            StyleManager.Tex top = StyleManager.texture(cat, styleId, "textures/block/respawn_anchor_top.png");
            StyleManager.Tex side = StyleManager.texture(cat, styleId, "textures/block/respawn_anchor_side4.png");
            Preview3D.block(g, top, side, cx, cy, size * 0.8F, turning ? yaw : 45.0F);
            break;
         case "shield": {
            StyleManager.Tex tex = StyleManager.texture(cat, styleId, cat.textures().get(0));
            Preview3D.shield(g, tex, cx, cy, size, turning ? yaw + 180.0F : 205.0F);
            break;
         }
         case "armor":
            String mat = StylePacks.byId(styleId) != null
                  && !StylePacks.byId(styleId).has("textures/entity/equipment/humanoid/netherite.png")
                  && StylePacks.byId(styleId).has("textures/entity/equipment/humanoid/diamond.png")
               ? "diamond"
               : "netherite";
            StyleManager.Tex body = StyleManager.texture(cat, styleId, "textures/entity/equipment/humanoid/" + mat + ".png");
            StyleManager.Tex legs = StyleManager.texture(cat, styleId, "textures/entity/equipment/humanoid_leggings/" + mat + ".png");
            Preview3D.armor(g, body, legs, cx, cy, size, turning ? yaw + 180.0F : 200.0F);
            break;
         case "fire":
            StyleManager.Tex f0 = StyleManager.texture(cat, styleId, "textures/block/fire_0.png");
            StyleManager.Tex f1 = StyleManager.texture(cat, styleId, "textures/block/fire_1.png");
            StyleManager.Tex rack = StyleManager.texture(null, "vanilla", "textures/block/netherrack.png");
            Preview3D.fire(g, f0, f1, rack, cx, cy, size, turning ? yaw : 30.0F, t);
            break;
         case "cross": {
            StyleManager.Tex tex = StyleManager.texture(cat, styleId, cat.textures().get(0));
            Preview3D.crossed(g, tex, cx, cy, size, turning ? yaw : 30.0F);
            break;
         }
         default: {
            StyleManager.Tex tex = StyleManager.texture(cat, styleId, StyleManager.mainPath(cat, styleId));
            Preview3D.item(g, tex, cx, cy, size, turning ? yaw : 28.0F, turning ? t : 0.0F, turning);
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor g, int mx, int my, float pt) {
      Font f = this.font();
      int bodyH = this.h - 24 - 4;
      this.hovered = null;
      Theme.card(g, this.x, this.y, this.x + 112, this.y + bodyH, 234881023, 352321535);
      int listH = (StyleCategory.ALL.size() + 1) * 19 + 6;
      float listMax = Math.max(0, listH - bodyH);
      this.listTarget = Math.max(0.0F, Math.min(listMax, this.listTarget));
      this.listScroll = this.listScroll + (this.listTarget - this.listScroll) * Math.min(1.0F, Anim.dt() * 16.0F);
      float hl = Anim.approach(Anim.key(54, 0), this.hotbar ? StyleCategory.ALL.size() : this.category, 16.0F);
      g.enableScissor(this.x + 1, this.y + 1, this.x + 112 - 1, this.y + bodyH - 1);
      int hy = Math.round(this.y + 3 + hl * 19.0F - this.listScroll);
      Theme.round(g, this.x + 3, hy, this.x + 112 - 3, hy + 19 - 1, 1384865014);
      Theme.outline(g, this.x + 3, hy, this.x + 112 - 3, hy + 19 - 1, -1718919946);
      Theme.pill(g, this.x + 1, hy + 5, this.x + 3, hy + 19 - 6, -4745985);
      int ry = this.y + 3 + StyleCategory.ALL.size() * 19 - Math.round(this.listScroll);
      boolean hover = this.inside(mx, my, this.x + 2, ry, this.x + 112 - 2, ry + 19) && my >= this.y && my < this.y + bodyH;
      float hv = Anim.hover(Anim.key(50, 999), hover && !this.hotbar);
      if (hv > 0.01F) {
         Theme.round(g, this.x + 3, ry, this.x + 112 - 3, ry + 19 - 1, Theme.withAlpha(486539263, hv));
      }

      g.pose().pushMatrix();
      g.pose().translate(this.x + 6, ry + 1);
      g.pose().scale(0.95F, 0.95F);
      g.item(HOTBAR_ICON, 0, 0);
      g.pose().popMatrix();
      g.text(
         f,
         Component.translatable("valence.styles.hotbar"),
         this.x + 25 + Math.round(hv * 2.0F),
         ry + 6,
         this.hotbar ? -1 : Theme.lerp(-4608298, -724737, hv),
         false
      );
      String count = String.valueOf(GlassHotbar.STYLES.length);
      boolean changed = NeonLyrics.cfg().hotbarStyle > 0;
      g.text(f, count, this.x + 112 - 8 - f.width(count), ry + 6, changed ? -4745985 : -8095322, false);

      for (int i = 0; i < StyleCategory.ALL.size(); i++) {
         StyleCategory c = StyleCategory.ALL.get(i);
         int ryx = this.y + 3 + i * 19 - Math.round(this.listScroll);
         boolean sel = !this.hotbar && i == this.category;
         changed = this.inside(mx, my, this.x + 2, ryx, this.x + 112 - 2, ryx + 19) && my >= this.y && my < this.y + bodyH;
         float hvx = Anim.hover(Anim.key(50, i), changed && !sel);
         if (hvx > 0.01F) {
            Theme.round(g, this.x + 3, ryx, this.x + 112 - 3, ryx + 19 - 1, Theme.withAlpha(486539263, hvx));
         }

         g.pose().pushMatrix();
         g.pose().translate(this.x + 6, ryx + 1);
         g.pose().scale(0.95F, 0.95F);
         g.item(this.icons[i], 0, 0);
         g.pose().popMatrix();
         Component name = Component.translatable(c.nameKey());
         g.text(
            f, f.plainSubstrByWidth(name.getString(), 66), this.x + 25 + Math.round(hvx * 2.0F), ryx + 6, sel ? -1 : Theme.lerp(-4608298, -724737, hvx), false
         );
         String countx = String.valueOf(StyleManager.count(c));
         boolean changedx = !"vanilla".equals(StyleManager.selected(c));
         g.text(f, countx, this.x + 112 - 8 - f.width(countx), ryx + 6, changedx ? -4745985 : -8095322, false);
      }

      g.disableScissor();
      if (this.hotbar) {
         this.renderHotbars(g, f, mx, my, bodyH);
         this.renderBottom(g, f, mx, my);
      } else {
         ry = this.inspectorW();
         hover = (boolean)(this.x + 112 + 7);
         int gw = this.x + this.w - hover - (ry > 0 ? ry + 7 : 0);
         StyleCategory cat = this.current();
         String selected = StyleManager.selected(cat);
         g.text(f, Component.translatable(cat.nameKey()), hover, this.y + 2, -724737, false);
         Component countText = Component.translatable("valence.styles.count", new Object[]{this.styles.size()});
         g.text(f, countText, hover + gw - f.width(countText) - 2, this.y + 2, -8095322, false);
         int top = this.y + 14;
         int gridH = bodyH - 14;
         int gap = 5;
         int cols = Math.max(1, (gw + gap) / (62 + gap));
         int tileW = (gw - gap * (cols - 1)) / cols;
         int tileH = 70;
         int rows = (this.styles.size() + cols - 1) / cols;
         boolean noPacks = this.styles.stream().noneMatch(st -> st.source() != null);
         int contentH = rows * (tileH + gap) + (noPacks ? 72 : 0);
         float max = Math.max(0, contentH - gridH);
         this.gridTarget = Math.max(0.0F, Math.min(max, this.gridTarget));
         this.gridScroll = this.gridScroll + (this.gridTarget - this.gridScroll) * Math.min(1.0F, Anim.dt() * 16.0F);
         g.enableScissor(hover - 14, top, hover + gw + 14, top + gridH);

         for (int i = 0; i < this.styles.size(); i++) {
            StyleManager.Style st = this.styles.get(i);
            int tx = hover + i % cols * (tileW + gap);
            int ty = top + 2 + i / cols * (tileH + gap) - Math.round(this.gridScroll);
            if (ty + tileH >= top && ty <= top + gridH) {
               boolean sel = st.id().equals(selected);
               boolean hoverx = this.inside(mx, my, tx, ty, tx + tileW, ty + tileH) && my >= top && my < top + gridH;
               if (hoverx) {
                  this.hovered = st.id();
               }

               float hvx = Anim.hover(Anim.key(51, i), hoverx);
               ty -= Math.round(hvx * 1.5F);
               Theme.card(g, tx, ty, tx + tileW, ty + tileH, hvx, sel);
               int img = 42;
               this.drawPreview(g, cat, st.id(), tx + (tileW - img) / 2, ty + 4, img, hoverx);
               String txt = f.plainSubstrByWidth(st.name(), (int)((tileW - 6) / 0.8F));
               g.pose().pushMatrix();
               g.pose().translate(tx + tileW / 2.0F, ty + tileH - 11);
               g.pose().scale(0.8F, 0.8F);
               g.centeredText(f, txt, 0, 0, sel ? -1 : Theme.lerp(-4608298, -724737, hvx));
               g.pose().popMatrix();
               if (st.source() != null) {
                  g.pose().pushMatrix();
                  g.pose().translate(tx + 4, ty + 4);
                  g.pose().scale(0.55F, 0.55F);
                  Theme.pill(g, -3, -2, 30, 9, -1718919946);
                  g.text(f, "PACK", 1, 0, -1, false);
                  g.pose().popMatrix();
               } else if (st.builtIn()) {
                  Theme.pill(g, tx + 5, ty + 5, tx + 10, ty + 10, st.swatch());
               }

               if (sel) {
                  Theme.pill(g, tx + tileW - 13, ty + 3, tx + tileW - 3, ty + 13, -7643914);
                  Theme.icon(g, "check", tx + tileW - 11, ty + 4, 7, -1);
               }
            }
         }

         if (noPacks) {
            int ey = top + 2 + rows * (tileH + gap) - Math.round(this.gridScroll);
            Theme.card(g, hover, ey, hover + gw, ey + 64, 285212671, 536870911);
            Theme.icon(g, "folder", hover + 8, ey + 8, 12, -4745985);
            g.text(f, Component.translatable("valence.styles.empty.title"), hover + 26, ey + 10, -724737, false);
            g.pose().pushMatrix();
            g.pose().translate(hover + 8, ey + 26);
            g.pose().scale(0.8F, 0.8F);
            g.textWithWordWrap(f, Component.translatable("valence.styles.empty.text"), 0, 0, Math.round((gw - 16) / 0.8F), -8095322, false);
            g.pose().popMatrix();
         }

         g.disableScissor();
         if (ry > 0) {
            int ix = this.x + this.w - ry;
            Theme.card(g, ix, this.y, ix + ry, this.y + bodyH, 234881023, 352321535);
            Theme.sheen(g, ix, this.y, ix + ry, this.y + bodyH, 0.5F);
            String show = this.hovered != null ? this.hovered : selected;
            StyleManager.Style showStyle = null;

            for (StyleManager.Style s : this.styles) {
               if (s.id().equals(show)) {
                  showStyle = s;
               }
            }

            g.pose().pushMatrix();
            g.pose().translate(ix + 7, this.y + 6);
            g.pose().scale(0.75F, 0.75F);
            g.text(f, Component.translatable(this.hovered != null ? "valence.styles.preview" : "valence.styles.active"), 0, 0, -8095322, false);
            g.pose().popMatrix();
            int nameX = ix + 7;
            Identifier packIcon = showStyle == null ? null : StyleManager.icon(showStyle.id());
            if (packIcon != null) {
               Theme.image(g, packIcon, ix + 7, this.y + 14, 12, 12, 64, 64, -1);
               nameX += 15;
            }

            if (showStyle != null) {
               g.text(f, f.plainSubstrByWidth(showStyle.name(), ix + ry - 6 - nameX), nameX, this.y + 16, -724737, false);
            }

            int box = Math.min(ry - 14, bodyH - 80);
            int by = this.y + 32;
            Theme.glow(g, ix + ry / 2 - box / 3, by + box / 4, ix + ry / 2 + box / 3, by + box * 3 / 4, -7643914, 0.35F);
            this.drawPreview(g, cat, show, ix + (ry - box) / 2, by, box, true);
            int ty = by + box + 6;
            if (showStyle != null && showStyle.source() != null) {
               Component info = Component.translatable("valence.styles.pack.info", new Object[]{showStyle.source().count(cat), cat.textures().size()});
               g.pose().pushMatrix();
               g.pose().translate(ix + 7, ty);
               g.pose().scale(0.75F, 0.75F);
               g.textWithWordWrap(f, info, 0, 0, Math.round((ry - 14) / 0.75F), -8095322, false);
               g.pose().popMatrix();
            } else {
               String key = showStyle != null && showStyle.builtIn() ? "valence.styles.desc.builtin" : "valence.styles.desc.vanilla";
               g.pose().pushMatrix();
               g.pose().translate(ix + 7, ty);
               g.pose().scale(0.75F, 0.75F);
               g.textWithWordWrap(f, Component.translatable(key), 0, 0, Math.round((ry - 14) / 0.75F), -8095322, false);
               g.pose().popMatrix();
            }

            if ("totem".equals(cat.id())) {
               int sy = this.y + bodyH - 66;
               g.pose().pushMatrix();
               g.pose().translate(ix + 7, sy);
               g.pose().scale(0.75F, 0.75F);
               g.text(f, Component.translatable("valence.styles.totem.pop"), 0, 0, -4608298, false);
               g.pose().popMatrix();
               int segW = (ry - 14) / 3;
               int idx = Math.max(0, Math.min(2, ValenceConfig.get().totemPop));
               Theme.pill(g, ix + 7, sy + 10, ix + 7 + segW * 3, sy + 24, 603979776);
               float pos = Anim.approach(Anim.key(55, 1), idx, 16.0F);
               int px = ix + 7 + Math.round(pos * segW);
               Theme.pill(g, px + 1, sy + 11, px + segW - 1, sy + 23, -7643914);

               for (int i = 0; i < 3; i++) {
                  Component lbl = Component.translatable("valence.styles.totem.pop." + i);
                  g.pose().pushMatrix();
                  g.pose().translate(ix + 7 + i * segW + segW / 2.0F, sy + 14);
                  g.pose().scale(0.75F, 0.75F);
                  g.centeredText(f, lbl, 0, 0, i == idx ? -1 : -8095322);
                  g.pose().popMatrix();
               }

               sy = this.y + bodyH - 34;
               g.pose().pushMatrix();
               g.pose().translate(ix + 7, sy);
               g.pose().scale(0.75F, 0.75F);
               g.text(f, Component.translatable("valence.styles.totem.size"), 0, 0, -4608298, false);
               g.pose().popMatrix();
               segW = (ry - 14) / 3;
               idx = Math.max(0, TOTEM_SIZES.indexOf(ValenceConfig.get().styleTotemSize));
               Theme.pill(g, ix + 7, sy + 10, ix + 7 + segW * 3, sy + 24, 603979776);
               pos = Anim.approach(Anim.key(55, 0), idx, 16.0F);
               px = ix + 7 + Math.round(pos * segW);
               Theme.pill(g, px + 1, sy + 11, px + segW - 1, sy + 23, -7643914);

               for (int i = 0; i < 3; i++) {
                  Component lbl = Component.translatable("valence.styles.totem." + TOTEM_SIZES.get(i));
                  g.pose().pushMatrix();
                  g.pose().translate(ix + 7 + i * segW + segW / 2.0F, sy + 14);
                  g.pose().scale(0.75F, 0.75F);
                  g.centeredText(f, lbl, 0, 0, i == idx ? -1 : -8095322);
                  g.pose().popMatrix();
               }
            }
         }

         this.renderBottom(g, f, mx, my);
      }
   }

   private void renderHotbars(GuiGraphicsExtractor g, Font f, int mx, int my, int bodyH) {
      int gx = this.x + 112 + 7;
      int gw = this.x + this.w - gx;
      g.text(f, Component.translatable("valence.styles.hotbar"), gx, this.y + 2, -724737, false);
      int top = this.y + 14;
      int tileH = 40;
      int gap = 5;
      int cur = NeonLyrics.cfg().hotbarStyle;

      for (int i = 0; i < HOTBAR_KEYS.length; i++) {
         int ty = top + 2 + i * (tileH + gap);
         if (ty + tileH > top + bodyH - 14) {
            break;
         }

         boolean sel = i == cur;
         boolean hover = this.inside(mx, my, gx, ty, gx + gw, ty + tileH);
         float hv = Anim.hover(Anim.key(56, i), hover);
         Theme.card(g, gx, ty, gx + gw, ty + tileH, hv, sel);
         g.text(f, Component.translatable(HOTBAR_KEYS[i]), gx + 8, ty + 6, sel ? -1 : Theme.lerp(-4608298, -724737, hv), false);
         float scale = Math.min(1.0F, (gw - 16) / 182.0F) * 0.9F;
         float px = gx + 8;
         float py = ty + 17;
         GlassHotbar.preview(g, i, px, py, Math.min(scale, 0.85F));
         if (sel) {
            Theme.pill(g, gx + gw - 15, ty + 5, gx + gw - 5, ty + 15, -7643914);
            Theme.icon(g, "check", gx + gw - 13, ty + 6, 7, -1);
         }
      }
   }

   private void renderBottom(GuiGraphicsExtractor g, Font f, int mx, int my) {
      int by = this.y + this.h - 24 + 2;
      boolean dirty = StyleManager.isDirty();
      Component apply = Component.translatable("valence.styles.apply");
      int aw = f.width(apply) + 22;
      int ax = this.x + this.w - aw;
      if (dirty) {
         float pulse = 0.5F + 0.5F * (float)Math.sin(System.currentTimeMillis() / 300.0);
         Theme.glow(g, ax, by, ax + aw, by + 18, -7643914, 0.35F * pulse);
      }

      OptionList.accentButton(g, ax, by, ax + aw, by + 18, apply, mx, my, Anim.key(52, 0));
      int bx = this.x;
      bx = this.smallButton(g, f, mx, my, bx, by, "folder", Component.translatable("valence.styles.folder"), 1);
      bx = this.smallButton(g, f, mx, my, bx + 4, by, "refresh", Component.translatable("valence.styles.rescan"), 2);
      bx = this.smallButton(g, f, mx, my, bx + 4, by, "close", Component.translatable("valence.styles.reset"), 3);
      if (dirty) {
         g.pose().pushMatrix();
         g.pose().translate(bx + 8, by + 6);
         g.pose().scale(0.75F, 0.75F);
         g.text(f, Component.translatable("valence.styles.unsaved"), 0, 0, -278748, false);
         g.pose().popMatrix();
      }
   }

   private int smallButton(GuiGraphicsExtractor g, Font f, int mx, int my, int bx, int by, String icon, Component text, int key) {
      int bw = f.width(text) + 26;
      float hv = Anim.hover(Anim.key(53, key), this.inside(mx, my, bx, by, bx + bw, by + 18));
      Theme.pill(g, bx, by, bx + bw, by + 18, Theme.lerp(536870911, 1040187391, hv));
      Theme.pillLine(g, bx, by, bx + bw, by + 18, Theme.lerp(654311423, 1439281407, hv));
      Theme.icon(g, icon, bx + 7, by + 4, 10, Theme.lerp(-4608298, -4745985, hv));
      g.text(f, text, bx + 21, by + 5, Theme.lerp(-4608298, -724737, hv), false);
      return bx + bw;
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      Font f = this.font();
      int bodyH = this.h - 24 - 4;
      if (this.inside(mx, my, this.x, this.y, this.x + 112, this.y + bodyH)) {
         int hy0 = this.y + 3 + StyleCategory.ALL.size() * 19 - Math.round(this.listScroll);
         if (my >= hy0 && my < hy0 + 19) {
            if (!this.hotbar) {
               this.hotbar = true;
               click();
            }

            return true;
         } else {
            for (int i = 0; i < StyleCategory.ALL.size(); i++) {
               int ry = this.y + 3 + i * 19 - Math.round(this.listScroll);
               if (my >= ry && my < ry + 19) {
                  if (i != this.category || this.hotbar) {
                     this.hotbar = false;
                     this.category = i;
                     this.styles = StyleManager.stylesFor(this.current());
                     this.gridTarget = 0.0F;
                     this.gridScroll = 0.0F;
                     click();
                  }

                  return true;
               }
            }

            return true;
         }
      } else {
         if (this.hotbar) {
            int gx = this.x + 112 + 7;
            int gw = this.x + this.w - gx;
            int top = this.y + 14;

            for (int i = 0; i < HOTBAR_KEYS.length; i++) {
               int ty = top + 2 + i * 45;
               if (this.inside(mx, my, gx, ty, gx + gw, ty + 40)) {
                  NeonLyrics.cfg().hotbarStyle = i;
                  NeonLyrics.saveConfig();
                  UiSounds.toggle(true);
                  return true;
               }
            }
         }

         int insW = this.inspectorW();
         int gx = this.x + 112 + 7;
         int gw = this.x + this.w - gx - (insW > 0 ? insW + 7 : 0);
         int top = this.y + 14;
         int gridH = bodyH - 14;
         int gap = 5;
         int cols = Math.max(1, (gw + gap) / (62 + gap));
         int tileW = (gw - gap * (cols - 1)) / cols;
         int tileH = 70;
         if (!this.hotbar && my >= top && my < top + gridH && mx >= gx && mx < gx + gw) {
            for (int i = 0; i < this.styles.size(); i++) {
               int tx = gx + i % cols * (tileW + gap);
               int ty = top + 2 + i / cols * (tileH + gap) - Math.round(this.gridScroll);
               if (this.inside(mx, my, tx, ty, tx + tileW, ty + tileH)) {
                  StyleManager.select(this.current(), this.styles.get(i).id());
                  UiSounds.toggle(true);
                  return true;
               }
            }
         }

         if (!this.hotbar && insW > 0 && "totem".equals(this.current().id())) {
            int ix = this.x + this.w - insW;
            int sy = this.y + bodyH - 66;
            int segW = (insW - 14) / 3;

            for (int i = 0; i < 3; i++) {
               int sx = ix + 7 + i * segW;
               if (this.inside(mx, my, sx, sy + 10, sx + segW, sy + 24)) {
                  if (ValenceConfig.get().totemPop != i) {
                     ValenceConfig.get().totemPop = i;
                     ValenceConfig.save();
                     click();
                  }

                  return true;
               }
            }
         }

         if (!this.hotbar && insW > 0 && "totem".equals(this.current().id())) {
            int ix = this.x + this.w - insW;
            int sy = this.y + bodyH - 34;
            int segW = (insW - 14) / 3;

            for (int i = 0; i < 3; i++) {
               int sx = ix + 7 + i * segW;
               if (this.inside(mx, my, sx, sy + 10, sx + segW, sy + 24)) {
                  if (!TOTEM_SIZES.get(i).equals(ValenceConfig.get().styleTotemSize)) {
                     ValenceConfig.get().styleTotemSize = TOTEM_SIZES.get(i);
                     ValenceConfig.save();
                     StyleManager.markDirty();
                     click();
                  }

                  return true;
               }
            }
         }

         int by = this.y + this.h - 24 + 2;
         Component apply = Component.translatable("valence.styles.apply");
         int aw = f.width(apply) + 22;
         int ax = this.x + this.w - aw;
         if (this.inside(mx, my, ax, by, ax + aw, by + 18)) {
            click();
            StyleManager.apply();
            return true;
         } else {
            int bw1 = f.width(Component.translatable("valence.styles.folder")) + 26;
            if (this.inside(mx, my, this.x, by, this.x + bw1, by + 18)) {
               click();
               Blaze3D.openPath(StylePacks.resourcePackFolder());
               return true;
            } else {
               int bx2 = this.x + bw1 + 4;
               int bw2 = f.width(Component.translatable("valence.styles.rescan")) + 26;
               if (this.inside(mx, my, bx2, by, bx2 + bw2, by + 18)) {
                  click();
                  StyleManager.rescan();
                  this.styles = StyleManager.stylesFor(this.current());
                  return true;
               } else {
                  int bx3 = bx2 + bw2 + 4;
                  int bw3 = f.width(Component.translatable("valence.styles.reset")) + 26;
                  if (this.inside(mx, my, bx3, by, bx3 + bw3, by + 18)) {
                     click();
                     StyleManager.resetAll();
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      if (mx < this.x + 112) {
         this.listTarget -= (float)amount * 20.0F;
      } else {
         this.gridTarget -= (float)amount * 30.0F;
      }

      return true;
   }
}
