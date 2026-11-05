package dev.valence.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.Font.GlyphVisitor;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.TextRenderable.Styled;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class Txt {
   private Txt() {
   }

   public static void drawInBatch(
      Font font,
      FormattedCharSequence text,
      float x,
      float y,
      int color,
      boolean shadow,
      final Matrix4f pose,
      final MultiBufferSource buffers,
      final DisplayMode mode,
      int background,
      final int light
   ) {
      font.prepareText(text, x, y, color, shadow, false, background).visit(new GlyphVisitor() {
         public void acceptGlyph(Styled glyph) {
            this.draw(glyph);
         }

         public void acceptEffect(TextRenderable effect) {
            this.draw(effect);
         }

         private void draw(TextRenderable r) {
            r.render(pose, buffers.getBuffer(r.renderType(mode)), light, false);
         }
      });
   }

   public static void drawInBatch(
      Font font,
      Component text,
      float x,
      float y,
      int color,
      boolean shadow,
      Matrix4f pose,
      MultiBufferSource buffers,
      DisplayMode mode,
      int background,
      int light
   ) {
      drawInBatch(font, text.getVisualOrderText(), x, y, color, shadow, pose, buffers, mode, background, light);
   }

   public static void drawInBatch(
      Font font,
      String text,
      float x,
      float y,
      int color,
      boolean shadow,
      Matrix4f pose,
      MultiBufferSource buffers,
      DisplayMode mode,
      int background,
      int light
   ) {
      drawInBatch(font, Component.literal(text).getVisualOrderText(), x, y, color, shadow, pose, buffers, mode, background, light);
   }
}
