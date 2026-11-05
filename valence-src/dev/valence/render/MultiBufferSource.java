package dev.valence.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.StagedVertexBuffer.Draw;
import net.minecraft.client.renderer.StagedVertexBuffer.ExecuteInfo;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderType;

public interface MultiBufferSource {
   VertexConsumer getBuffer(RenderType var1);

   static MultiBufferSource.BufferSource immediate(Object ignored) {
      return new MultiBufferSource.BufferSource();
   }

   final class BufferSource implements MultiBufferSource {
      private static final StagedVertexBuffer STAGED = new StagedVertexBuffer(() -> "Valence", 262144);
      private static long lastFrame = -1L;
      private static boolean deferring;
      private static final List<MultiBufferSource.BufferSource.Part> PENDING = new ArrayList<>();
      private static final List<MultiBufferSource.BufferSource.Ready> READY = new ArrayList<>();
      private final List<MultiBufferSource.BufferSource.Part> parts = new ArrayList<>();
      private RenderType lastType;
      private VertexConsumer last;

      @Override
      public VertexConsumer getBuffer(RenderType type) {
         if (type == this.lastType && this.last != null) {
            return this.last;
         }

         Draw d = STAGED.appendDraw(type.format(), type.primitiveTopology());
         this.parts.add(new MultiBufferSource.BufferSource.Part(type, d));
         this.lastType = type;
         this.last = STAGED.getVertexBuilder(d);
         return this.last;
      }

      private static List<MultiBufferSource.BufferSource.Ready> upload(List<MultiBufferSource.BufferSource.Part> list) {
         List<MultiBufferSource.BufferSource.Ready> out = new ArrayList<>();
         STAGED.upload();

         for (MultiBufferSource.BufferSource.Part p : list) {
            ExecuteInfo info = STAGED.getExecuteInfo(p.draw());
            if (info != null) {
               out.add(new MultiBufferSource.BufferSource.Ready(p.type().prepare(), info));
            }
         }

         return out;
      }

      public void endBatch() {
         if (!this.parts.isEmpty()) {
            List<MultiBufferSource.BufferSource.Part> list = new ArrayList<>(this.parts);
            this.parts.clear();
            this.lastType = null;
            this.last = null;
            if (deferring) {
               PENDING.addAll(list);
            } else {
               try {
                  List<MultiBufferSource.BufferSource.Ready> ready = upload(list);
                  if (!ready.isEmpty()) {
                     RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
                     RenderPass pass = RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(() -> "Valence", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty());

                     try {
                        for (MultiBufferSource.BufferSource.Ready r : ready) {
                           r.type().drawFromBuffer(r.info(), pass);
                        }
                     } catch (Throwable var12) {
                        if (pass != null) {
                           try {
                              pass.close();
                           } catch (Throwable var11) {
                              var12.addSuppressed(var11);
                           }
                        }

                        throw var12;
                     }

                     if (pass != null) {
                        pass.close();
                     }
                  }
               } finally {
                  STAGED.endDraw();
               }
            }
         }
      }

      public static void beginDefer() {
         if (!READY.isEmpty()) {
            READY.clear();
            STAGED.endDraw();
         }

         PENDING.clear();
         deferring = true;
      }

      public static void endDefer() {
         deferring = false;
         if (!PENDING.isEmpty()) {
            READY.addAll(upload(PENDING));
            PENDING.clear();
         }
      }

      public static void flushDeferred(RenderPass pass) {
         if (!READY.isEmpty()) {
            try {
               for (MultiBufferSource.BufferSource.Ready r : READY) {
                  r.type().drawFromBuffer(r.info(), pass);
               }
            } finally {
               READY.clear();
               STAGED.endDraw();
            }
         }
      }

      public static void endFrame(long frame) {
         if (frame != lastFrame && READY.isEmpty() && !deferring) {
            lastFrame = frame;
            STAGED.endFrame();
         }
      }

      private record Part(RenderType type, Draw draw) {
      }

      private record Ready(PreparedRenderType type, ExecuteInfo info) {
      }
   }
}
