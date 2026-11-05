package dev.valence.gui;

import dev.valence.config.ValenceConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class OptionList {
   private static int nextId = 1;
   private final List<OptionList.Row> rows = new ArrayList<>();
   private OptionList.Expander currentExpander;
   private int x;
   private int y;
   private int w;
   private int h;
   private float scroll;
   private float targetScroll;
   private OptionList.Row dragging;

   public void setBounds(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
   }

   public OptionList add(OptionList.Row row) {
      if (row instanceof OptionList.Expander e) {
         this.currentExpander = e;
      } else if (row instanceof OptionList.Header) {
         this.currentExpander = null;
      } else {
         row.parent = this.currentExpander;
      }

      this.rows.add(row);
      return this;
   }

   public void clear() {
      this.rows.clear();
      this.currentExpander = null;
   }

   private int contentHeight() {
      int total = 0;

      for (OptionList.Row r : this.rows) {
         if (r.shown()) {
            total += r.height();
         }
      }

      return total + 6;
   }

   private float maxScroll() {
      return Math.max(0, this.contentHeight() - this.h);
   }

   public void render(GuiGraphicsExtractor g, int mx, int my) {
      this.targetScroll = Math.max(0.0F, Math.min(this.maxScroll(), this.targetScroll));
      this.scroll = this.scroll + (this.targetScroll - this.scroll) * Math.min(1.0F, Anim.dt() * 18.0F);
      g.enableScissor(this.x - 14, this.y, this.x + this.w + 14, this.y + this.h);
      int cy = this.y - Math.round(this.scroll);
      boolean mouseIn = mx >= this.x && mx < this.x + this.w && my >= this.y && my < this.y + this.h;

      for (OptionList.Row r : this.rows) {
         if (r.shown()) {
            int rh = r.height();
            if (cy + rh >= this.y - 16 && cy <= this.y + this.h + 16) {
               r.render(g, this.x, cy, this.w - 7, mouseIn ? mx : -1000, mouseIn ? my : -1000);
            }

            cy += rh;
         }
      }

      g.disableScissor();
      float max = this.maxScroll();
      if (max > 0.0F) {
         int barH = Math.max(18, Math.round((float)(this.h * this.h) / this.contentHeight()));
         int barY = this.y + Math.round((this.h - barH) * (this.scroll / max));
         Theme.pill(g, this.x + this.w - 3, this.y, this.x + this.w, this.y + this.h, 419430399);
         Theme.pill(g, this.x + this.w - 3, barY, this.x + this.w, barY + barH, Theme.withAlpha(-4745985, 0.8F));
      }
   }

   public boolean mouseClicked(double mx, double my, int button) {
      if (this.ownsFocus()) {
         OptionList.TextField.focused = null;
         ValenceConfig.save();
      }

      if (!(mx < this.x) && !(mx >= this.x + this.w) && !(my < this.y) && !(my >= this.y + this.h)) {
         int cy = this.y - Math.round(this.scroll);

         for (OptionList.Row r : this.rows) {
            if (r.shown()) {
               int rh = r.height();
               if (my >= cy && my < cy + rh) {
                  if (r.click(mx, my, this.x, cy, this.w - 7, button)) {
                     this.dragging = r;
                     return true;
                  }

                  return false;
               }

               cy += rh;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(double mx, double my) {
      if (this.dragging != null) {
         int cy = this.y - Math.round(this.scroll);

         for (OptionList.Row r : this.rows) {
            if (r.shown()) {
               if (r == this.dragging) {
                  return r.drag(mx, my, this.x, cy, this.w - 7);
               }

               cy += r.height();
            }
         }
      }

      return false;
   }

   public boolean mouseReleased() {
      boolean was = this.dragging != null;
      if (this.dragging instanceof OptionList.Slider slider) {
         slider.active = false;
      }

      this.dragging = null;
      if (was) {
         ValenceConfig.save();
      }

      return was;
   }

   private boolean ownsFocus() {
      if (OptionList.TextField.focused == null) {
         return false;
      }

      for (OptionList.Row r : this.rows) {
         if (r.inner() == OptionList.TextField.focused) {
            return true;
         }
      }

      return false;
   }

   public boolean keyPressed(KeyEvent event) {
      return this.ownsFocus() && OptionList.TextField.focused.key(event);
   }

   public boolean charTyped(CharacterEvent event) {
      return this.ownsFocus() && OptionList.TextField.focused.type(event);
   }

   public static OptionList.Cycle choice(Component label, List<Component> names, IntSupplier get, IntConsumer set, boolean arrows) {
      List<String> values = new ArrayList<>();

      for (int i = 0; i < names.size(); i++) {
         values.add(String.valueOf(i));
      }

      OptionList.Cycle c = new OptionList.Cycle(label, values, names, () -> String.valueOf(get.getAsInt()), v -> set.accept(Integer.parseInt(v)));
      return arrows ? c.arrows() : c;
   }

   public boolean mouseScrolled(double mx, double my, double amount) {
      if (!(mx < this.x) && !(mx >= this.x + this.w) && !(my < this.y) && !(my >= this.y + this.h)) {
         this.targetScroll -= (float)amount * 28.0F;
         this.targetScroll = Math.max(0.0F, Math.min(this.maxScroll(), this.targetScroll));
         return true;
      } else {
         return false;
      }
   }

   public static void drawSwitch(GuiGraphicsExtractor g, int sx, int sy, float k, float hover) {
      int tw = 24;
      int th = 12;
      Theme.glow(g, sx, sy, sx + tw, sy + th, -7643914, 0.35F * k);
      Theme.pill(g, sx, sy, sx + tw, sy + th, Theme.lerp(Theme.lerp(822083583, 1174405119, hover), -7643914, k));
      Theme.pillLine(g, sx, sy, sx + tw, sy + th, Theme.lerp(654311423, 1725548543, k));
      int kx = sx + 2 + Math.round(k * (tw - th));
      Theme.pill(g, kx, sy + 2, kx + th - 4, sy + th - 2, -1);
   }

   public static float accentButton(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, Component text, int mx, int my, long key) {
      boolean hovered = mx >= x1 && mx < x2 && my >= y1 && my < y2;
      float hv = Anim.hover(key, hovered);
      Theme.glow(g, x1, y1, x2, y2, -7643914, 0.25F + 0.35F * hv);
      Theme.pill(g, x1, y1, x2, y2, Theme.lerp(-7643914, -6259713, hv));
      Theme.pillLine(g, x1, y1, x2, y2, Theme.withAlpha(-1, 0.25F + 0.25F * hv));
      g.centeredText(Minecraft.getInstance().font, text, (x1 + x2) / 2, (y1 + y2) / 2 - 4, -1);
      return hv;
   }

   public static float glassButton(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, Component text, int mx, int my, long key) {
      boolean hovered = mx >= x1 && mx < x2 && my >= y1 && my < y2;
      float hv = Anim.hover(key, hovered);
      Theme.pill(g, x1, y1, x2, y2, Theme.lerp(536870911, 1040187391, hv));
      Theme.pillLine(g, x1, y1, x2, y2, Theme.lerp(654311423, 1439281407, hv));
      g.centeredText(Minecraft.getInstance().font, text, (x1 + x2) / 2, (y1 + y2) / 2 - 4, Theme.lerp(-4608298, -1, hv));
      return hv;
   }

   public static class Button extends OptionList.Row {
      private final Component label;
      private final Supplier<Component> buttonText;
      private final Runnable action;

      public Button(Component label, Component buttonText, Runnable action) {
         this(label, () -> buttonText, action);
      }

      public Button(Component label, Supplier<Component> buttonText, Runnable action) {
         this.label = label;
         this.buttonText = buttonText;
         this.action = action;
      }

      private int bw() {
         return Math.max(72, font().width((FormattedText)this.buttonText.get()) + 20);
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.frame(g, x, y, w, this.height(), mx, my, this.label, null);
         int bx = x + w - this.bw() - 6;
         OptionList.accentButton(g, bx, y + 6, bx + this.bw(), y + 20, this.buttonText.get(), mx, my, this.key(1));
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         int bx = x + w - this.bw() - 6;
         if (mx >= bx && mx < bx + this.bw()) {
            sound();
            this.action.run();
         }

         return false;
      }
   }

   public static class Buttons extends OptionList.Row {
      private static final int BW = 34;
      private final Component label;
      private final List<Component> texts;
      private final List<Runnable> actions;
      private final Supplier<Component> value;

      public Buttons(Component label, Supplier<Component> value, List<Component> texts, List<Runnable> actions) {
         this.label = label;
         this.value = value;
         this.texts = texts;
         this.actions = actions;
      }

      private int bx(int x, int w, int i) {
         return x + w - 6 - (this.texts.size() - i) * 37;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.frame(g, x, y, w, this.height(), mx, my, this.label, null);
         Component v = this.value.get();
         if (v != null) {
            int vx = this.bx(x, w, 0) - 8 - font().width(v);
            g.text(font(), v, vx, y + 9, -4608298, false);
         }

         for (int i = 0; i < this.texts.size(); i++) {
            int bx = this.bx(x, w, i);
            OptionList.glassButton(g, bx, y + 6, bx + 34, y + 20, this.texts.get(i), mx, my, this.key(1 + i));
         }
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         for (int i = 0; i < this.texts.size(); i++) {
            int bx = this.bx(x, w, i);
            if (mx >= bx && mx < bx + 34) {
               sound();
               this.actions.get(i).run();
               save();
            }
         }

         return false;
      }
   }

   public static class Colors extends OptionList.Row {
      public static final int[] PALETTE = new int[]{
         -1,
         -1450497,
         -3695617,
         -7643914,
         -1004548,
         -1292135,
         -770210,
         -290244,
         -340971,
         -11870592,
         -13773633,
         -13058568,
         -12877066,
         -5917444,
         -7035976,
         -14804178
      };
      private static final int SIZE = 10;
      private static final int GAP = 3;
      private final Component label;
      private final IntSupplier get;
      private final IntConsumer set;
      private final int[] palette;
      private final boolean auto;

      public Colors(Component label, IntSupplier get, IntConsumer set) {
         this(label, PALETTE, false, get, set);
      }

      public Colors(Component label, int[] rgb, boolean auto, IntSupplier get, IntConsumer set) {
         this.label = label;
         this.auto = auto;
         int[] p = new int[rgb.length + (auto ? 1 : 0)];
         if (auto) {
            p[0] = -1;
         }

         for (int i = 0; i < rgb.length; i++) {
            p[i + (auto ? 1 : 0)] = rgb[i] | 0xFF000000;
         }

         this.palette = rgb == PALETTE ? PALETTE : p;
         this.get = get;
         this.set = rgb == PALETTE ? set : v -> set.accept(v == -1 ? -1 : v & 16777215);
      }

      @Override
      public int height() {
         return 28;
      }

      private int swatchX(int x, int w, int i) {
         int total = this.palette.length * 13 - 3;
         return x + w - total - 8 + i * 13;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.frame(g, x, y, w, this.height(), mx, my, this.label, null);
         int cur = this.get.getAsInt();

         for (int i = 0; i < this.palette.length; i++) {
            int sx = this.swatchX(x, w, i);
            int sy = y + 9;
            int col = this.palette[i];
            boolean isAuto = col == -1;
            boolean sel = isAuto ? cur == -1 : cur != -1 && (col & 16777215) == (cur & 16777215);
            boolean hovered = inside(mx, my, sx - 1, sy - 1, sx + 10 + 1, sy + 10 + 1);
            float hv = Anim.hover(this.key(1 + i), hovered, false);
            int grow = Math.round(hv * 1.5F);
            int shown = isAuto ? -14016192 : col;
            if (sel) {
               Theme.glow(g, sx, sy, sx + 10, sy + 10, isAuto ? -7643914 : shown | 0xFF000000, 0.5F);
               Theme.pillLine(g, sx - 2 - grow, sy - 2 - grow, sx + 10 + 2 + grow, sy + 10 + 2 + grow, -1);
            }

            Theme.pill(g, sx - grow, sy - grow, sx + 10 + grow, sy + 10 + grow, shown);
            if (isAuto) {
               g.pose().pushMatrix();
               g.pose().translate(sx + 5.0F - 1.6F, sy + 2.0F);
               g.pose().scale(0.75F, 0.75F);
               g.text(font(), "A", 0, 0, -1, false);
               g.pose().popMatrix();
            }

            Theme.pillLine(g, sx - grow, sy - grow, sx + 10 + grow, sy + 10 + grow, 872415231);
         }
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         for (int i = 0; i < this.palette.length; i++) {
            int sx = this.swatchX(x, w, i);
            if (mx >= sx - 1 && mx < sx + 10 + 1 && my >= y + 8 && my < y + 20) {
               this.set.accept(this.palette[i]);
               sound();
               save();
               return false;
            }
         }

         return false;
      }
   }

   public static class Cycle extends OptionList.Row {
      private final Component label;
      private final List<String> values;
      private final List<Component> names;
      private final Supplier<String> get;
      private final Consumer<String> set;
      private boolean arrows;
      private int shownIdx = -1;
      private int prevIdx;
      private int slideDir = 1;
      private long changeNanos;

      public Cycle(Component label, List<String> values, List<Component> names, Supplier<String> get, Consumer<String> set) {
         this.label = label;
         this.values = values;
         this.names = names;
         this.get = get;
         this.set = set;
      }

      public OptionList.Cycle arrows() {
         this.arrows = true;
         return this;
      }

      private int arrowWidth() {
         int max = 0;

         for (Component c : this.names) {
            max = Math.max(max, font().width(c));
         }

         return Math.max(128, max + 58);
      }

      private void renderArrows(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my, int idx) {
         Font f = font();
         int bw = this.arrowWidth();
         int bx = x + w - bw - 6;
         if (this.shownIdx != idx) {
            if (this.shownIdx >= 0) {
               this.prevIdx = this.shownIdx;
               this.changeNanos = System.nanoTime();
            }

            this.shownIdx = idx;
         }

         Theme.pill(g, bx, y + 4, bx + bw, y + 22, 603979776);
         Theme.pillLine(g, bx, y + 4, bx + bw, y + 22, 452984831);

         for (int side = 0; side < 2; side++) {
            int cx = side == 0 ? bx + 11 : bx + bw - 11;
            boolean hovered = inside(mx, my, cx - 10, y + 3, cx + 10, y + 23);
            float hv = Anim.hover(this.key(30 + side), hovered);
            float press = Math.max(0.0F, 1.0F - (float)(System.nanoTime() - this.changeNanos) / 1.8E8F);
            boolean pressedSide = side == 0 == this.slideDir < 0;
            float s = 1.0F + 0.08F * hv - (pressedSide ? 0.12F * (float)Math.sin(press * Math.PI) : 0.0F);
            g.pose().pushMatrix();
            g.pose().translate(cx, y + 13);
            g.pose().scale(s, s);
            Theme.pill(g, -8, -7, 8, 7, Theme.lerp(352321535, -7643914, hv));
            g.pose().rotate((float)Math.toRadians(side == 0 ? 180.0 : 0.0));
            Theme.icon(g, "arrow", -4, -4, 8, Theme.lerp(-8095322, -1, hv));
            g.pose().popMatrix();
         }

         int tx0 = bx + 22;
         int tx1 = bx + bw - 22;
         float k = Theme.easeOutCubic(Math.min(1.0F, (float)(System.nanoTime() - this.changeNanos) / 2.6E8F));
         g.enableScissor(tx0, y + 4, tx1, y + 22);
         int mid = (tx0 + tx1) / 2;
         if (k < 1.0F && this.prevIdx >= 0 && this.prevIdx < this.names.size()) {
            int off = Math.round(k * 26.0F) * this.slideDir;
            g.centeredText(f, this.names.get(this.prevIdx), mid - off, y + 9, Theme.withAlpha(-724737, 1.0F - k));
         }

         int off = Math.round((1.0F - k) * 26.0F) * this.slideDir;
         g.centeredText(f, this.names.get(idx), mid + off, y + 9, Theme.withAlpha(-1, Math.max(0.05F, k)));
         g.disableScissor();
         int n = this.values.size();
         int dx0 = mid - (n - 1) * 3;

         for (int i = 0; i < n; i++) {
            Theme.pill(g, dx0 + i * 6 - 1, y + 19, dx0 + i * 6 + 1, y + 21, i == idx ? -4745985 : 1090519039);
         }
      }

      private int segmentWidth() {
         int max = 0;

         for (Component c : this.names) {
            max = Math.max(max, font().width(c));
         }

         return max + 12;
      }

      private boolean segmented(int w) {
         return font().width(this.label) + 30 + this.segmentWidth() * this.values.size() < w;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.frame(g, x, y, w, this.height(), mx, my, this.label, null);
         int idx = Math.max(0, this.values.indexOf(this.get.get()));
         Font f = font();
         if (this.arrows) {
            this.renderArrows(g, x, y, w, mx, my, idx);
         } else if (this.segmented(w)) {
            int sw = this.segmentWidth();
            int total = sw * this.values.size();
            int bx = x + w - total - 6;
            Theme.pill(g, bx, y + 5, bx + total, y + 21, 603979776);
            Theme.pillLine(g, bx, y + 5, bx + total, y + 21, 452984831);
            float sel = Anim.approach(this.key(1), idx, 16.0F);
            int selX = bx + Math.round(sel * sw);
            Theme.glow(g, selX + 2, y + 7, selX + sw - 2, y + 19, -7643914, 0.3F);
            Theme.pill(g, selX + 2, y + 7, selX + sw - 2, y + 19, -7643914);

            for (int i = 0; i < this.values.size(); i++) {
               int sx = bx + i * sw;
               boolean hovered = inside(mx, my, sx, y + 5, sx + sw, y + 21);
               float hv = Anim.hover(this.key(10 + i), hovered && i != idx);
               int col = i == idx ? -1 : Theme.lerp(-8095322, -724737, hv);
               g.centeredText(f, this.names.get(i), sx + sw / 2, y + 9, col);
            }
         } else {
            Component name = this.names.get(idx);
            int bw = 116;
            int bx = x + w - bw - 6;
            boolean hovered = inside(mx, my, bx, y + 5, bx + bw, y + 21);
            float hv = Anim.hover(this.key(1), hovered);
            Theme.pill(g, bx, y + 5, bx + bw, y + 21, Theme.lerp(603979776, 1149983990, hv));
            Theme.pillLine(g, bx, y + 5, bx + bw, y + 21, Theme.lerp(452984831, 1439281407, hv));
            g.centeredText(f, name, bx + bw / 2, y + 9, -724737);
            g.text(f, "‹", bx + 6, y + 9, -8095322, false);
            g.text(f, "›", bx + bw - 10, y + 9, -8095322, false);
         }
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         int idx = Math.max(0, this.values.indexOf(this.get.get()));
         if (this.arrows) {
            int bw = this.arrowWidth();
            int bx = x + w - bw - 6;
            if (!(mx < bx) && !(my < y + 3) && !(my > y + 23)) {
               boolean back = button == 1 || mx < bx + bw / 2.0;
               this.slideDir = back ? -1 : 1;
               idx = (idx + (back ? this.values.size() - 1 : 1)) % this.values.size();
               this.set.accept(this.values.get(idx));
               sound();
               save();
               return false;
            } else {
               return false;
            }
         } else if (this.segmented(w)) {
            int sw = this.segmentWidth();
            int total = sw * this.values.size();
            int bx = x + w - total - 6;

            for (int i = 0; i < this.values.size(); i++) {
               int sx = bx + i * sw;
               if (mx >= sx && mx < sx + sw && my >= y + 5 && my < y + 21 && i != idx) {
                  this.set.accept(this.values.get(i));
                  sound();
                  save();
               }
            }

            return false;
         } else {
            int bw = 116;
            int bx = x + w - bw - 6;
            boolean back = button == 1 || mx < bx + bw / 4.0;
            idx = (idx + (back ? this.values.size() - 1 : 1)) % this.values.size();
            this.set.accept(this.values.get(idx));
            sound();
            save();
            return false;
         }
      }
   }

   public static class Expander extends OptionList.Row {
      private static final Set<String> OPEN = new HashSet<>();
      private final Component text;
      boolean open;

      public Expander(Component text) {
         this.text = text;
         this.open = OPEN.contains(text.getString());
      }

      @Override
      public int height() {
         return 28;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         boolean hovered = inside(mx, my, x, y + 4, x + w, y + 24);
         float hover = Anim.hover(this.key(0), hovered);
         float turn = Anim.approach(this.key(1), this.open ? 1.0F : 0.0F, 12.0F);
         Theme.card(g, x, y + 4, x + w, y + 24, hover, false);
         Font f = font();
         g.centeredText(f, this.text, x + w / 2, y + 10, Theme.lerp(-4608298, -724737, hover));
         int cx = x + w / 2 + f.width(this.text) / 2 + 9;
         int cy = y + 14;
         g.pose().pushMatrix();
         g.pose().translate(cx, cy);
         g.pose().rotate((float)Math.toRadians(90.0F * turn));
         Theme.icon(g, "arrow", -4, -4, 8, Theme.lerp(-8095322, -4745985, Math.max(hover, turn)));
         g.pose().popMatrix();
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         this.open = !this.open;
         if (this.open) {
            OPEN.add(this.text.getString());
         } else {
            OPEN.remove(this.text.getString());
         }

         UiSounds.toggle(this.open);
         return false;
      }
   }

   public static class Header extends OptionList.Row {
      private final Component text;

      public Header(Component text) {
         this.text = text;
      }

      @Override
      public int height() {
         return 20;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         Font f = font();
         Theme.pill(g, x + 2, y + 9, x + 5, y + 14, -4745985);
         g.text(f, this.text, x + 9, y + 8, -4745985, false);
         int tw = f.width(this.text);
         g.fill(x + tw + 15, y + 12, x + w, y + 13, 452984831);
      }
   }

   public static class Info extends OptionList.Row {
      private final Supplier<Component> text;
      private final int lines;
      private int lastW;

      public Info(Supplier<Component> text, int lines) {
         this.text = text;
         this.lines = lines;
      }

      @Override
      public int height() {
         if (this.lastW <= 0) {
            return 6 + this.lines * 10;
         }

         int n = font().split((FormattedText)this.text.get(), Math.round((this.lastW - 12) / 0.85F)).size();
         return 6 + Math.max((int)Math.ceil(n * 9 * 0.85F) + 2, Math.min(this.lines, n) * 9);
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.lastW = w;
         g.pose().pushMatrix();
         g.pose().translate(x + 6, y + 2);
         g.pose().scale(0.85F, 0.85F);
         g.textWithWordWrap(font(), (FormattedText)this.text.get(), 0, 0, Math.round((w - 12) / 0.85F), -8095322, false);
         g.pose().popMatrix();
      }
   }

   public abstract static class Row {
      protected final int id = OptionList.nextId++;
      OptionList.Expander parent;

      public int height() {
         return 27;
      }

      public boolean visible() {
         return true;
      }

      public OptionList.Row inner() {
         return this;
      }

      final boolean shown() {
         return this.visible() && (this.parent == null || this.parent.open);
      }

      public abstract void render(GuiGraphicsExtractor var1, int var2, int var3, int var4, int var5, int var6);

      public boolean click(double mx, double my, int x, int y, int w, int button) {
         return false;
      }

      public boolean drag(double mx, double my, int x, int y, int w) {
         return false;
      }

      protected long key(int part) {
         return Anim.key(100000 + this.id, part);
      }

      protected static Font font() {
         return Minecraft.getInstance().font;
      }

      protected static void save() {
         ValenceConfig.save();
      }

      protected static void sound() {
         UiSounds.click();
      }

      protected static boolean inside(int mx, int my, int x1, int y1, int x2, int y2) {
         return mx >= x1 && mx < x2 && my >= y1 && my < y2;
      }

      protected float frame(GuiGraphicsExtractor g, int x, int y, int w, int h, int mx, int my, Component label, Component desc) {
         boolean hovered = inside(mx, my, x, y + 1, x + w, y + h - 2);
         float hover = Anim.hover(this.key(0), hovered);
         Theme.card(g, x, y + 1, x + w, y + h - 2, hover, false);
         Font f = font();
         int tx = x + 8 + Math.round(hover * 1.5F);
         if (desc != null) {
            g.text(f, label, tx, y + 5, -724737, false);
            g.pose().pushMatrix();
            g.pose().translate(tx, y + 15);
            g.pose().scale(0.75F, 0.75F);
            int maxW = Math.round((w - (tx - x) - 46) / 0.75F);
            String d = desc.getString();
            if (f.width(d) > maxW) {
               d = f.plainSubstrByWidth(d, Math.max(10, maxW - f.width("…"))).stripTrailing() + "…";
            }

            g.text(f, d, 0, 0, Theme.lerp(-8095322, -4608298, hover), false);
            g.pose().popMatrix();
         } else {
            g.text(f, label, tx, y + (h - 3) / 2 - 3, -724737, false);
         }

         return hover;
      }
   }

   public static class Segmented extends OptionList.Row {
      private final Component label;
      private final Component desc;
      private final List<Component> names;
      private final IntSupplier get;
      private final IntConsumer set;

      public Segmented(Component label, Component desc, List<Component> names, IntSupplier get, IntConsumer set) {
         this.label = label;
         this.desc = desc;
         this.names = names;
         this.get = get;
         this.set = set;
      }

      private int segW() {
         int m = 0;

         for (Component c : this.names) {
            m = Math.max(m, font().width(c));
         }

         return Math.round(m * 0.85F) + 14;
      }

      private int left(int x, int w) {
         return x + w - 8 - this.segW() * this.names.size() - 4;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         this.frame(g, x, y, w, this.height(), mx, my, this.label, this.desc);
         int sw = this.segW();
         int sx = this.left(x, w);
         int sy = y + 6;
         int sh = 15;
         Theme.pill(g, sx, sy, sx + sw * this.names.size() + 4, sy + sh, 805306368);
         Theme.pillLine(g, sx, sy, sx + sw * this.names.size() + 4, sy + sh, 536870911);
         int sel = Math.max(0, Math.min(this.names.size() - 1, this.get.getAsInt()));
         float pos = Anim.approach(this.key(1), sel, 16.0F);
         int hx = sx + 2 + Math.round(pos * sw);
         Theme.glow(g, hx, sy + 2, hx + sw, sy + sh - 2, -7643914, 0.25F);
         Theme.pill(g, hx, sy + 2, hx + sw, sy + sh - 2, -7643914);
         Font f = font();

         for (int i = 0; i < this.names.size(); i++) {
            int cx = sx + 2 + i * sw + sw / 2;
            boolean hov = inside(mx, my, sx + 2 + i * sw, sy, sx + 2 + (i + 1) * sw, sy + sh);
            float hv = Anim.hover(this.key(10 + i), hov && i != sel);
            float near = 1.0F - Math.min(1.0F, Math.abs(pos - i));
            int col = Theme.lerp(Theme.lerp(-8095322, -724737, hv), -1, near);
            g.pose().pushMatrix();
            g.pose().translate(cx, sy + 4.5F);
            g.pose().scale(0.85F, 0.85F);
            g.centeredText(f, this.names.get(i), 0, 0, col);
            g.pose().popMatrix();
         }
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         int sw = this.segW();
         int sx = this.left(x, w) + 2;
         if (mx >= sx && mx < sx + sw * this.names.size()) {
            int i = (int)((mx - sx) / sw);
            i = Math.max(0, Math.min(this.names.size() - 1, i));
            if (i != this.get.getAsInt()) {
               this.set.accept(i);
               UiSounds.click();
               save();
            }

            return false;
         } else {
            int next = (this.get.getAsInt() + 1) % this.names.size();
            this.set.accept(next);
            UiSounds.click();
            save();
            return false;
         }
      }
   }

   public static class Slider extends OptionList.Row {
      private static final int TRACK_W = 90;
      private final Component label;
      private final double min;
      private final double max;
      private final double step;
      private final Supplier<Double> get;
      private final Consumer<Double> set;
      private final DoubleFunction<String> format;
      private boolean active;

      public Slider(Component label, double min, double max, double step, Supplier<Double> get, Consumer<Double> set, DoubleFunction<String> format) {
         this.label = label;
         this.min = min;
         this.max = max;
         this.step = step;
         this.get = get;
         this.set = set;
         this.format = format;
      }

      private int trackX(int x, int w) {
         return x + w - 90 - 44;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         float hover = this.frame(g, x, y, w, this.height(), mx, my, this.label, null);
         int tx = this.trackX(x, w);
         double v = this.get.get();
         float k = (float)((v - this.min) / (this.max - this.min));
         k = Math.max(0.0F, Math.min(1.0F, k));
         float shown = Anim.approach(this.key(1), k, 20.0F);
         int ty = y + 12;
         Theme.pill(g, tx, ty, tx + 90, ty + 4, 872415231);
         int fillX = tx + Math.round(90.0F * shown);
         Theme.pill(g, tx, ty, Math.max(tx + 4, fillX), ty + 4, -7643914);
         boolean knobHover = inside(mx, my, fillX - 6, ty - 6, fillX + 6, ty + 10) || this.active;
         float kh = Anim.hover(this.key(2), knobHover, false);
         Theme.glow(g, fillX - 5, ty - 3, fillX + 5, ty + 7, -7643914, 0.25F + 0.4F * kh);
         int r = 4 + Math.round(kh);
         Theme.pill(g, fillX - r, ty + 2 - r, fillX + r, ty + 2 + r, -1);
         String val = this.format.apply(v);
         g.text(font(), val, x + w - 8 - font().width(val), y + 9, Theme.lerp(-4608298, -724737, hover), false);
      }

      private void setFromMouse(double mx, int x, int w) {
         int tx = this.trackX(x, w);
         double k = (mx - tx) / 90.0;
         k = Math.max(0.0, Math.min(1.0, k));
         double v = this.min + (this.max - this.min) * k;
         v = Math.round(v / this.step) * this.step;
         v = Math.max(this.min, Math.min(this.max, v));
         this.set.accept(v);
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         int tx = this.trackX(x, w);
         if (mx >= tx - 8 && mx <= tx + 90 + 8) {
            this.setFromMouse(mx, x, w);
            this.active = true;
            UiSounds.click();
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean drag(double mx, double my, int x, int y, int w) {
         this.setFromMouse(mx, x, w);
         return true;
      }
   }

   public static class TextField extends OptionList.Row {
      static OptionList.TextField focused;
      private final Component label;
      private final Component hint;
      private final Supplier<String> get;
      private final Consumer<String> set;
      private final int maxLength;

      public TextField(Component label, Component hint, int maxLength, Supplier<String> get, Consumer<String> set) {
         this.label = label;
         this.hint = hint;
         this.maxLength = maxLength;
         this.get = get;
         this.set = set;
      }

      @Override
      public int height() {
         return 40;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         boolean hovered = inside(mx, my, x, y + 1, x + w, y + this.height() - 2);
         float hover = Anim.hover(this.key(0), hovered || focused == this);
         Theme.card(g, x, y + 1, x + w, y + this.height() - 2, hover, false);
         Font f = font();
         g.text(f, this.label, x + 8, y + 6, -724737, false);
         int bx1 = x + 6;
         int by1 = y + 18;
         int bx2 = x + w - 6;
         int by2 = y + 33;
         boolean on = focused == this;
         Theme.round(g, bx1, by1, bx2, by2, on ? 1140850688 : 704643072);
         Theme.outline(g, bx1, by1, bx2, by2, on ? 1439281407 : 587202559);
         String v = this.get.get();
         int max = bx2 - bx1 - 10;
         if (v.isEmpty() && !on) {
            g.text(f, f.plainSubstrByWidth(this.hint.getString(), max), bx1 + 5, by1 + 4, -8095322, false);
         } else {
            String shown = v;

            while (f.width(shown) > max && shown.length() > 1) {
               shown = shown.substring(1);
            }

            g.text(f, shown, bx1 + 5, by1 + 4, -724737, false);
            if (on && System.currentTimeMillis() / 500L % 2L == 0L) {
               int cx = bx1 + 5 + f.width(shown);
               g.fill(cx + 1, by1 + 3, cx + 2, by1 + 12, -1);
            }
         }
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         focused = this;
         sound();
         return false;
      }

      boolean key(KeyEvent e) {
         int k = e.key();
         if (k == 256 || k == 257 || k == 335) {
            focused = null;
            save();
            return true;
         }

         if (k == 259) {
            String v = this.get.get();
            if (!v.isEmpty()) {
               this.set.accept(v.substring(0, v.length() - 1));
            }

            return true;
         } else if (e.isPaste()) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard().trim().replace("\n", "");
            this.set.accept(this.cut(this.get.get() + clip));
            return true;
         } else {
            return false;
         }
      }

      boolean type(CharacterEvent e) {
         if (e.codepoint() < 32) {
            return false;
         }

         this.set.accept(this.cut(this.get.get() + e.codepointAsString()));
         return true;
      }

      private String cut(String s) {
         return s.length() > this.maxLength ? s.substring(0, this.maxLength) : s;
      }
   }

   public static class Toggle extends OptionList.Row {
      private final Component label;
      private final Component desc;
      private final Supplier<Boolean> get;
      private final Consumer<Boolean> set;

      public Toggle(Component label, Component desc, Supplier<Boolean> get, Consumer<Boolean> set) {
         this.label = label;
         this.desc = desc;
         this.get = get;
         this.set = set;
      }

      @Override
      public void render(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
         float hover = this.frame(g, x, y, w, this.height(), mx, my, this.label, this.desc);
         boolean on = this.get.get();
         float k = Theme.easeOutCubic(Anim.approach(this.key(1), on ? 1.0F : 0.0F, 14.0F));
         OptionList.drawSwitch(g, x + w - 32, y + 8, k, hover);
      }

      @Override
      public boolean click(double mx, double my, int x, int y, int w, int button) {
         boolean v = !this.get.get();
         this.set.accept(v);
         UiSounds.toggle(v);
         save();
         return false;
      }
   }
}
