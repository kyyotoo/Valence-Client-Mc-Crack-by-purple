package dev.valence.gui;

import dev.valence.styles.StyleManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Preview3D {
   private static final float SIN_45 = (float)Math.sin(Math.PI / 4);
   private static final int FRONT = 0;
   private static final int BOTH = 1;
   private static final int BACK = 2;
   private static final Vector3f LIGHT = new Vector3f(0.35F, 0.85F, 0.45F).normalize();

   private Preview3D() {
   }

   private static Matrix4f view(float yawDeg, float pitchDeg) {
      return new Matrix4f().rotateX((float)Math.toRadians(pitchDeg)).rotateY((float)Math.toRadians(yawDeg));
   }

   private static void face(
      List<Preview3D.Face> out,
      Matrix4f m,
      Identifier tex,
      int texW,
      int texH,
      float u,
      float v,
      float uw,
      float vh,
      Vector3f c,
      Vector3f r,
      Vector3f up,
      boolean bright,
      int sides
   ) {
      face(out, m, tex, null, texW, texH, u, v, uw, vh, c, r, up, bright, sides);
   }

   private static void face(
      List<Preview3D.Face> out,
      Matrix4f m,
      Identifier tex,
      Identifier mirror,
      int texW,
      int texH,
      float u,
      float v,
      float uw,
      float vh,
      Vector3f c,
      Vector3f r,
      Vector3f up,
      boolean bright,
      int sides
   ) {
      Vector3f tl = m.transformPosition(new Vector3f(c).sub(r).add(up));
      Vector3f tr = m.transformPosition(new Vector3f(c).add(r).add(up));
      Vector3f bl = m.transformPosition(new Vector3f(c).sub(r).sub(up));
      out.add(new Preview3D.Face(tex, mirror, texW, texH, u, v, uw, vh, tl, tr, bl, -1, bright, sides));
   }

   private static void colorFace(List<Preview3D.Face> out, Matrix4f m, int color, Vector3f c, Vector3f r, Vector3f up) {
      Vector3f tl = m.transformPosition(new Vector3f(c).sub(r).add(up));
      Vector3f tr = m.transformPosition(new Vector3f(c).add(r).add(up));
      Vector3f bl = m.transformPosition(new Vector3f(c).sub(r).sub(up));
      out.add(new Preview3D.Face(null, null, 0, 0, 0.0F, 0.0F, 0.0F, 0.0F, tl, tr, bl, color, false, 0));
   }

   private static void box(
      List<Preview3D.Face> out, Matrix4f m, Identifier tex, int texW, int texH, int u, int v, int w, int h, int d, float inflate, boolean bright
   ) {
      float hw = w / 2.0F + inflate;
      float hh = h / 2.0F + inflate;
      float hd = d / 2.0F + inflate;
      face(out, m, tex, texW, texH, u + d, v + d, w, h, new Vector3f(0.0F, 0.0F, -hd), new Vector3f(-hw, 0.0F, 0.0F), new Vector3f(0.0F, hh, 0.0F), bright, 0);
      face(
         out,
         m,
         tex,
         texW,
         texH,
         u + 2 * d + w,
         v + d,
         w,
         h,
         new Vector3f(0.0F, 0.0F, hd),
         new Vector3f(hw, 0.0F, 0.0F),
         new Vector3f(0.0F, hh, 0.0F),
         bright,
         0
      );
      face(
         out, m, tex, texW, texH, u + d + w, v + d, d, h, new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -hd), new Vector3f(0.0F, hh, 0.0F), bright, 0
      );
      face(out, m, tex, texW, texH, u, v + d, d, h, new Vector3f(-hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, hd), new Vector3f(0.0F, hh, 0.0F), bright, 0);
      face(out, m, tex, texW, texH, u + d + w, v, w, d, new Vector3f(0.0F, hh, 0.0F), new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -hd), bright, 0);
      face(out, m, tex, texW, texH, u + d, v, w, d, new Vector3f(0.0F, -hh, 0.0F), new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, hd), bright, 0);
   }

   private static void colorBox(List<Preview3D.Face> out, Matrix4f m, int color, float w, float h, float d) {
      float hw = w / 2.0F;
      float hh = h / 2.0F;
      float hd = d / 2.0F;
      colorFace(out, m, color, new Vector3f(0.0F, 0.0F, -hd), new Vector3f(-hw, 0.0F, 0.0F), new Vector3f(0.0F, hh, 0.0F));
      colorFace(out, m, color, new Vector3f(0.0F, 0.0F, hd), new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, hh, 0.0F));
      colorFace(out, m, color, new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -hd), new Vector3f(0.0F, hh, 0.0F));
      colorFace(out, m, color, new Vector3f(-hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, hd), new Vector3f(0.0F, hh, 0.0F));
      colorFace(out, m, color, new Vector3f(0.0F, hh, 0.0F), new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -hd));
      colorFace(out, m, color, new Vector3f(0.0F, -hh, 0.0F), new Vector3f(hw, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, hd));
   }

   private static void block(List<Preview3D.Face> out, Matrix4f m, StyleManager.Tex top, StyleManager.Tex side) {
      float h = 8.0F;
      if (side != null) {
         int sw = side.width();
         int sh = side.frameHeight();
         face(
            out,
            m,
            side.id(),
            side.width(),
            side.height(),
            0.0F,
            0.0F,
            sw,
            sh,
            new Vector3f(0.0F, 0.0F, -h),
            new Vector3f(-h, 0.0F, 0.0F),
            new Vector3f(0.0F, h, 0.0F),
            false,
            0
         );
         face(
            out,
            m,
            side.id(),
            side.width(),
            side.height(),
            0.0F,
            0.0F,
            sw,
            sh,
            new Vector3f(0.0F, 0.0F, h),
            new Vector3f(h, 0.0F, 0.0F),
            new Vector3f(0.0F, h, 0.0F),
            false,
            0
         );
         face(
            out,
            m,
            side.id(),
            side.width(),
            side.height(),
            0.0F,
            0.0F,
            sw,
            sh,
            new Vector3f(h, 0.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, -h),
            new Vector3f(0.0F, h, 0.0F),
            false,
            0
         );
         face(
            out,
            m,
            side.id(),
            side.width(),
            side.height(),
            0.0F,
            0.0F,
            sw,
            sh,
            new Vector3f(-h, 0.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, h),
            new Vector3f(0.0F, h, 0.0F),
            false,
            0
         );
      }

      if (top != null) {
         int tw = top.width();
         int th = top.frameHeight();
         face(
            out,
            m,
            top.id(),
            top.width(),
            top.height(),
            0.0F,
            0.0F,
            tw,
            th,
            new Vector3f(0.0F, h, 0.0F),
            new Vector3f(h, 0.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, -h),
            false,
            0
         );
         face(
            out,
            m,
            top.id(),
            top.width(),
            top.height(),
            0.0F,
            0.0F,
            tw,
            th,
            new Vector3f(0.0F, -h, 0.0F),
            new Vector3f(h, 0.0F, 0.0F),
            new Vector3f(0.0F, 0.0F, h),
            false,
            0
         );
      }
   }

   private static void extruded(List<Preview3D.Face> out, Matrix4f m, StyleManager.Tex t, int frame, boolean edges) {
      int w = t.width();
      int fh = t.frameHeight();
      float s = 16.0F / w;
      float half = 8.0F;
      float halfH = fh * s / 2.0F;
      float th = s * 0.5F;
      float v0 = frame * fh;
      face(
         out,
         m,
         t.id(),
         t.width(),
         t.height(),
         0.0F,
         v0,
         w,
         fh,
         new Vector3f(0.0F, 0.0F, th),
         new Vector3f(half, 0.0F, 0.0F),
         new Vector3f(0.0F, halfH, 0.0F),
         false,
         0
      );
      face(
         out,
         m,
         t.id(),
         t.mirrorId(),
         t.width(),
         t.height(),
         0.0F,
         v0,
         w,
         fh,
         new Vector3f(0.0F, 0.0F, -th),
         new Vector3f(half, 0.0F, 0.0F),
         new Vector3f(0.0F, halfH, 0.0F),
         false,
         2
      );
      if (edges) {
         int base = frame * fh;

         for (int y = 0; y < fh; y++) {
            for (int x = 0; x < w; x++) {
               int c = t.pixel(x, base + y);
               if (c >>> 24 >= 128) {
                  float px = -half + (x + 0.5F) * s;
                  float py = halfH - (y + 0.5F) * s;
                  int dark = shade(c, 0.72F) | 0xFF000000;
                  if (x == 0 || t.pixel(x - 1, base + y) >>> 24 < 128) {
                     colorFace(out, m, dark, new Vector3f(px - s / 2.0F, py, 0.0F), new Vector3f(0.0F, 0.0F, th), new Vector3f(0.0F, s / 2.0F, 0.0F));
                  }

                  if (x == w - 1 || t.pixel(x + 1, base + y) >>> 24 < 128) {
                     colorFace(out, m, dark, new Vector3f(px + s / 2.0F, py, 0.0F), new Vector3f(0.0F, 0.0F, -th), new Vector3f(0.0F, s / 2.0F, 0.0F));
                  }

                  if (y == 0 || t.pixel(x, base + y - 1) >>> 24 < 128) {
                     colorFace(
                        out,
                        m,
                        shade(c, 0.9F) | 0xFF000000,
                        new Vector3f(px, py + s / 2.0F, 0.0F),
                        new Vector3f(s / 2.0F, 0.0F, 0.0F),
                        new Vector3f(0.0F, 0.0F, -th)
                     );
                  }

                  if (y == fh - 1 || t.pixel(x, base + y + 1) >>> 24 < 128) {
                     colorFace(
                        out,
                        m,
                        shade(c, 0.55F) | 0xFF000000,
                        new Vector3f(px, py - s / 2.0F, 0.0F),
                        new Vector3f(s / 2.0F, 0.0F, 0.0F),
                        new Vector3f(0.0F, 0.0F, th)
                     );
                  }
               }
            }
         }
      }
   }

   private static void cross(List<Preview3D.Face> out, Matrix4f m, StyleManager.Tex t, int frame, float height) {
      if (t != null) {
         int w = t.width();
         int fh = t.frameHeight();
         float v0 = frame * fh;
         float h = height / 2.0F;
         float d = 8.0F * SIN_45;
         Vector3f c = new Vector3f(0.0F, 0.0F, 0.0F);
         face(out, m, t.id(), t.mirrorId(), t.width(), t.height(), 0.0F, v0, w, fh, c, new Vector3f(d, 0.0F, d), new Vector3f(0.0F, h, 0.0F), true, 1);
         face(out, m, t.id(), t.mirrorId(), t.width(), t.height(), 0.0F, v0, w, fh, c, new Vector3f(d, 0.0F, -d), new Vector3f(0.0F, h, 0.0F), true, 1);
      }
   }

   private static int shade(int argb, float k) {
      int r = (int)((argb >> 16 & 0xFF) * k);
      int g = (int)((argb >> 8 & 0xFF) * k);
      int b = (int)((argb & 0xFF) * k);
      return argb & 0xFF000000 | r << 16 | g << 8 | b;
   }

   private static void draw(GuiGraphicsExtractor g, List<Preview3D.Face> faces, float cx, float cy, float scale) {
      faces.sort(Comparator.comparingDouble(Preview3D.Face::depth));
      Matrix3x2f tmp = new Matrix3x2f();

      for (Preview3D.Face face : faces) {
         Preview3D.Face f = face;
         float pe1x = f.tr().x - f.tl().x;
         float pe1y = -(f.tr().y - f.tl().y);
         float pe2x = f.bl().x - f.tl().x;
         float pe2y = -(f.bl().y - f.tl().y);
         if (pe1x * pe2y - pe1y * pe2x < 0.0F && f.sides() != 0 && f.mirror() != null) {
            Vector3f br = new Vector3f(f.bl()).add(f.tr()).sub(f.tl());
            f = new Preview3D.Face(
               f.mirror(), null, f.texW(), f.texH(), f.texW() - (f.u() + f.uw()), f.v(), f.uw(), f.vh(), f.tr(), f.tl(), br, f.color(), f.fullBright(), 1
            );
         }

         float x0 = cx + f.tl().x * scale;
         float y0 = cy - f.tl().y * scale;
         float e1x = (f.tr().x - f.tl().x) * scale;
         float e1y = -(f.tr().y - f.tl().y) * scale;
         float e2x = (f.bl().x - f.tl().x) * scale;
         float e2y = -(f.bl().y - f.tl().y) * scale;
         float cross = e1x * e2y - e1y * e2x;
         if (!(Math.abs(cross) <= 0.01F) && !(cross < 0.0F) && (!(cross > 0.0F) || face.sides() != 2 || f != face)) {
            float nx = (f.tr().y - f.tl().y) * (f.bl().z - f.tl().z) - (f.tr().z - f.tl().z) * (f.bl().y - f.tl().y);
            float ny = (f.tr().z - f.tl().z) * (f.bl().x - f.tl().x) - (f.tr().x - f.tl().x) * (f.bl().z - f.tl().z);
            float nz = (f.tr().x - f.tl().x) * (f.bl().y - f.tl().y) - (f.tr().y - f.tl().y) * (f.bl().x - f.tl().x);
            float nl = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            float lambert = nl > 0.0F ? Math.abs(-(nx * LIGHT.x + ny * LIGHT.y + nz * LIGHT.z) / nl) : 1.0F;
            float sh = f.fullBright() ? 1.0F : Math.max(0.5F, Math.min(1.0F, 0.55F + 0.45F * lambert));
            g.pose().pushMatrix();
            g.pose().mul(tmp.set(e1x, e1y, e2x, e2y, x0, y0));
            if (f.tex() == null) {
               g.fill(0, 0, 1, 1, shade(f.color(), sh));
            } else {
               int c = (int)(255.0F * sh);
               int color = 0xFF000000 | c << 16 | c << 8 | c;
               g.pose().scale(1.0F / f.uw(), 1.0F / f.vh());
               g.blit(
                  RenderPipelines.GUI_TEXTURED,
                  f.tex(),
                  0,
                  0,
                  f.u(),
                  f.v(),
                  Math.round(f.uw()),
                  Math.round(f.vh()),
                  Math.round(f.uw()),
                  Math.round(f.vh()),
                  f.texW(),
                  f.texH(),
                  color
               );
            }

            g.pose().popMatrix();
         }
      }
   }

   public static void crystal(
      GuiGraphicsExtractor g, StyleManager.Tex crystal, StyleManager.Tex obsidian, float cx, float cy, float size, float time, float yaw
   ) {
      float scale = size / 52.0F;
      Matrix4f v = view(yaw, 24.0F);
      List<Preview3D.Face> faces = new ArrayList<>();
      if (obsidian != null) {
         block(faces, new Matrix4f(v).translate(0.0F, -14.0F, 0.0F), obsidian, obsidian);
      }

      if (crystal != null) {
         float age = time * 20.0F;
         float spin = age * 3.0F;
         float bob = (float)Math.sin(age * 0.2F) * 2.5F;
         Quaternionf axis = new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45);
         Quaternionf outer = new Quaternionf().rotateY((float)Math.toRadians(spin)).mul(axis);
         Quaternionf inner = new Quaternionf(outer).mul(axis).rotateY((float)Math.toRadians(spin));
         Quaternionf core = new Quaternionf(inner).mul(axis).rotateY((float)Math.toRadians(spin));
         Matrix4f base = new Matrix4f(v).translate(0.0F, 12.0F + bob, 0.0F);
         Identifier id = crystal.id();
         int tw = crystal.width();
         int th = crystal.height();
         float k = tw / 64.0F;
         boxScaled(faces, new Matrix4f(base).rotate(core).scale(1.3398438F), id, tw, th, 32, 0, 8, k);
         boxScaled(faces, new Matrix4f(base).rotate(inner).scale(1.75F), id, tw, th, 0, 0, 8, k);
         boxScaled(faces, new Matrix4f(base).rotate(outer).scale(2.0F), id, tw, th, 0, 0, 8, k);
      }

      draw(g, faces, cx, cy + 2.0F * scale, scale);
   }

   private static void boxScaled(List<Preview3D.Face> out, Matrix4f m, Identifier tex, int texW, int texH, int u, int v, int s, float k) {
      float h = s / 2.0F;
      float U = u * k;
      float V = v * k;
      float S = s * k;
      face(out, m, tex, texW, texH, U + S, V + S, S, S, new Vector3f(0.0F, 0.0F, -h), new Vector3f(-h, 0.0F, 0.0F), new Vector3f(0.0F, h, 0.0F), true, 0);
      face(out, m, tex, texW, texH, U + 3.0F * S, V + S, S, S, new Vector3f(0.0F, 0.0F, h), new Vector3f(h, 0.0F, 0.0F), new Vector3f(0.0F, h, 0.0F), true, 0);
      face(out, m, tex, texW, texH, U + 2.0F * S, V + S, S, S, new Vector3f(h, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -h), new Vector3f(0.0F, h, 0.0F), true, 0);
      face(out, m, tex, texW, texH, U, V + S, S, S, new Vector3f(-h, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, h), new Vector3f(0.0F, h, 0.0F), true, 0);
      face(out, m, tex, texW, texH, U + 2.0F * S, V, S, S, new Vector3f(0.0F, h, 0.0F), new Vector3f(h, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -h), true, 0);
      face(out, m, tex, texW, texH, U + S, V, S, S, new Vector3f(0.0F, -h, 0.0F), new Vector3f(h, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, h), true, 0);
   }

   public static void block(GuiGraphicsExtractor g, StyleManager.Tex top, StyleManager.Tex side, float cx, float cy, float size, float yaw) {
      float scale = size / 28.0F;
      List<Preview3D.Face> faces = new ArrayList<>();
      block(faces, view(yaw, 30.0F), top, side);
      draw(g, faces, cx, cy, scale);
   }

   public static void item(GuiGraphicsExtractor g, StyleManager.Tex tex, float cx, float cy, float size, float yaw, float time, boolean detailed) {
      if (tex != null) {
         float scale = size / 20.0F;
         float bob = (float)Math.sin(time * 2.0F) * 0.6F;
         Matrix4f m = view(yaw, 12.0F).translate(0.0F, bob, 0.0F);
         List<Preview3D.Face> faces = new ArrayList<>();
         int frame = tex.frames() > 1 ? (int)(time * 20.0F / 2.0F) % tex.frames() : 0;
         extruded(faces, m, tex, frame, detailed && tex.width() <= 64);
         draw(g, faces, cx, cy, scale);
      }
   }

   public static void shield(GuiGraphicsExtractor g, StyleManager.Tex tex, float cx, float cy, float size, float yaw) {
      if (tex != null) {
         float scale = size / 30.0F;
         float k = tex.width() / 64.0F;
         Matrix4f v = view(yaw, 14.0F);
         List<Preview3D.Face> faces = new ArrayList<>();
         boxK(faces, new Matrix4f(v).translate(0.0F, 0.0F, -1.5F), tex, 0, 0, 12, 22, 1, k);
         boxK(faces, new Matrix4f(v).translate(0.0F, 0.0F, 2.0F), tex, 26, 0, 2, 6, 6, k);
         draw(g, faces, cx, cy, scale);
      }
   }

   private static void boxK(List<Preview3D.Face> out, Matrix4f m, StyleManager.Tex t, int u, int v, int w, int h, int d, float k) {
      float hw = w / 2.0F;
      float hh = h / 2.0F;
      float hd = d / 2.0F;
      Identifier id = t.id();
      int tw = t.width();
      int th = t.height();
      face(
         out,
         m,
         id,
         tw,
         th,
         (u + d) * k,
         (v + d) * k,
         w * k,
         h * k,
         new Vector3f(0.0F, 0.0F, -hd),
         new Vector3f(-hw, 0.0F, 0.0F),
         new Vector3f(0.0F, hh, 0.0F),
         false,
         0
      );
      face(
         out,
         m,
         id,
         tw,
         th,
         (u + 2 * d + w) * k,
         (v + d) * k,
         w * k,
         h * k,
         new Vector3f(0.0F, 0.0F, hd),
         new Vector3f(hw, 0.0F, 0.0F),
         new Vector3f(0.0F, hh, 0.0F),
         false,
         0
      );
      face(
         out,
         m,
         id,
         tw,
         th,
         (u + d + w) * k,
         (v + d) * k,
         d * k,
         h * k,
         new Vector3f(hw, 0.0F, 0.0F),
         new Vector3f(0.0F, 0.0F, -hd),
         new Vector3f(0.0F, hh, 0.0F),
         false,
         0
      );
      face(
         out,
         m,
         id,
         tw,
         th,
         u * k,
         (v + d) * k,
         d * k,
         h * k,
         new Vector3f(-hw, 0.0F, 0.0F),
         new Vector3f(0.0F, 0.0F, hd),
         new Vector3f(0.0F, hh, 0.0F),
         false,
         0
      );
      face(
         out,
         m,
         id,
         tw,
         th,
         (u + d + w) * k,
         v * k,
         w * k,
         d * k,
         new Vector3f(0.0F, hh, 0.0F),
         new Vector3f(hw, 0.0F, 0.0F),
         new Vector3f(0.0F, 0.0F, -hd),
         false,
         0
      );
      face(
         out,
         m,
         id,
         tw,
         th,
         (u + d) * k,
         v * k,
         w * k,
         d * k,
         new Vector3f(0.0F, -hh, 0.0F),
         new Vector3f(hw, 0.0F, 0.0F),
         new Vector3f(0.0F, 0.0F, hd),
         false,
         0
      );
   }

   public static void armor(GuiGraphicsExtractor g, StyleManager.Tex body, StyleManager.Tex legs, float cx, float cy, float size, float yaw) {
      float scale = size / 45.0F;
      Matrix4f v = view(yaw, 10.0F).translate(0.0F, -16.5F, 0.0F);
      List<Preview3D.Face> faces = new ArrayList<>();
      int skin = -13949632;
      colorBox(faces, new Matrix4f(v).translate(0.0F, 28.0F, 0.0F), skin, 8.0F, 8.0F, 8.0F);
      colorBox(faces, new Matrix4f(v).translate(0.0F, 18.0F, 0.0F), skin, 8.0F, 12.0F, 4.0F);
      colorBox(faces, new Matrix4f(v).translate(-6.0F, 18.0F, 0.0F), skin, 4.0F, 12.0F, 4.0F);
      colorBox(faces, new Matrix4f(v).translate(6.0F, 18.0F, 0.0F), skin, 4.0F, 12.0F, 4.0F);
      colorBox(faces, new Matrix4f(v).translate(-2.0F, 6.0F, 0.0F), skin, 4.0F, 12.0F, 4.0F);
      colorBox(faces, new Matrix4f(v).translate(2.0F, 6.0F, 0.0F), skin, 4.0F, 12.0F, 4.0F);
      if (body != null) {
         float k = body.width() / 64.0F;
         armorBox(faces, new Matrix4f(v).translate(0.0F, 28.0F, 0.0F), body, 0, 0, 8, 8, 8, 1.0F, k);
         armorBox(faces, new Matrix4f(v).translate(0.0F, 28.0F, 0.0F), body, 32, 0, 8, 8, 8, 1.5F, k);
         armorBox(faces, new Matrix4f(v).translate(0.0F, 18.0F, 0.0F), body, 16, 16, 8, 12, 4, 1.0F, k);
         armorBox(faces, new Matrix4f(v).translate(-6.0F, 18.0F, 0.0F), body, 40, 16, 4, 12, 4, 1.0F, k);
         armorBox(faces, new Matrix4f(v).translate(6.0F, 18.0F, 0.0F), body, 40, 16, 4, 12, 4, 1.0F, k);
         armorBox(faces, new Matrix4f(v).translate(-2.0F, 6.0F, 0.0F), body, 0, 16, 4, 12, 4, 1.0F, k);
         armorBox(faces, new Matrix4f(v).translate(2.0F, 6.0F, 0.0F), body, 0, 16, 4, 12, 4, 1.0F, k);
      }

      if (legs != null) {
         float k = legs.width() / 64.0F;
         armorBox(faces, new Matrix4f(v).translate(0.0F, 18.0F, 0.0F), legs, 16, 16, 8, 12, 4, 0.5F, k);
         armorBox(faces, new Matrix4f(v).translate(-2.0F, 6.0F, 0.0F), legs, 0, 16, 4, 12, 4, 0.5F, k);
         armorBox(faces, new Matrix4f(v).translate(2.0F, 6.0F, 0.0F), legs, 0, 16, 4, 12, 4, 0.5F, k);
      }

      draw(g, faces, cx, cy, scale);
   }

   private static void armorBox(List<Preview3D.Face> out, Matrix4f m, StyleManager.Tex t, int u, int v, int w, int h, int d, float inflate, float k) {
      float sx = (w + 2.0F * inflate) / w;
      float sy = (h + 2.0F * inflate) / h;
      float sz = (d + 2.0F * inflate) / d;
      boxK(out, new Matrix4f(m).scale(sx, sy, sz), t, u, v, w, h, d, k);
   }

   public static void fire(
      GuiGraphicsExtractor g,
      StyleManager.Tex fire0,
      StyleManager.Tex fire1,
      StyleManager.Tex netherrack,
      float cx,
      float cy,
      float size,
      float yaw,
      float time
   ) {
      float scale = size / 34.0F;
      Matrix4f v = view(yaw, 22.0F);
      List<Preview3D.Face> faces = new ArrayList<>();
      if (netherrack != null) {
         block(faces, new Matrix4f(v).translate(0.0F, -8.0F, 0.0F), netherrack, netherrack);
      }

      StyleManager.Tex a = fire0 != null ? fire0 : fire1;
      StyleManager.Tex b = fire1 != null ? fire1 : fire0;
      if (a != null) {
         int fa = (int)(time * 20.0F) % Math.max(1, a.frames());
         int fb = (int)(time * 20.0F + 7.0F) % Math.max(1, b.frames());
         cross(faces, new Matrix4f(v).translate(0.0F, 8.5F, 0.0F), a, fa, 17.0F);
         cross(faces, new Matrix4f(v).translate(0.0F, 8.5F, 0.0F).rotateY((float) (Math.PI / 4)), b, fb, 17.0F);
      }

      draw(g, faces, cx, cy + 2.0F * scale, scale);
   }

   public static void crossed(GuiGraphicsExtractor g, StyleManager.Tex tex, float cx, float cy, float size, float yaw) {
      float scale = size / 24.0F;
      List<Preview3D.Face> faces = new ArrayList<>();
      cross(faces, view(yaw, 18.0F), tex, 0, 16.0F);
      draw(g, faces, cx, cy, scale);
   }

   private record Face(
      Identifier tex,
      Identifier mirror,
      int texW,
      int texH,
      float u,
      float v,
      float uw,
      float vh,
      Vector3f tl,
      Vector3f tr,
      Vector3f bl,
      int color,
      boolean fullBright,
      int sides
   ) {
      float depth() {
         return (this.tr.z + this.bl.z) / 2.0F;
      }
   }
}
