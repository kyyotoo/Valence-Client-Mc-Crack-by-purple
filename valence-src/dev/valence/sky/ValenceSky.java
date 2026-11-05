package dev.valence.sky;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.valence.ValenceClient;
import dev.valence.config.ValenceConfig;
import dev.valence.license.License;
import dev.valence.render.MultiBufferSource;
import dev.valence.sky.classic.G;
import dev.valence.sky.classic.SkyCanvas;
import dev.valence.sky.classic.SkyCtx;
import dev.valence.sky.classic.SkyEngine;
import dev.valence.sky.classic.SkyTex;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class ValenceSky implements SkyCanvas {
   public static final SkyEngine ENGINE = new SkyEngine();
   private static final ValenceSky INSTANCE = new ValenceSky();
   private static final Map<SkyTex, Identifier> IDS = new EnumMap<>(SkyTex.class);
   private static final long[] REBUILD_NANOS = new long[]{50000000L, 33000000L, 16000000L};
   private static int autoQuality = 2;
   private static long qualityCheckAt;
   private static int lowFrames;
   private static int highFrames;
   private static SkyDefinition previewSky;
   private static float night = 1.0F;
   private static boolean failed;
   private static boolean suspended;
   private static long lastShaderCheck;
   private static Method irisInUse;
   private static Object irisApi;
   private static boolean irisLookedUp;
   private static long lastBuildNanos;
   private static int lastRevision = -1;
   private MultiBufferSource.BufferSource buffers;
   private VertexConsumer vc;
   private int recCount;
   private float[] rx = new float[8192];
   private float[] ry = new float[8192];
   private float[] rz = new float[8192];
   private float[] ru = new float[8192];
   private float[] rv = new float[8192];
   private int[] rc = new int[8192];
   private int layerCount;
   private SkyTex[] layerTex = new SkyTex[64];
   private int[] layerStart = new int[64];

   private ValenceSky() {
   }

   public static SkyDefinition current() {
      if (!License.active()) {
         return null;
      } else {
         return previewSky != null ? previewSky : SkyRegistry.active();
      }
   }

   public static void setPreview(SkyDefinition sky) {
      previewSky = sky;
   }

   public static SkyDefinition preview() {
      return previewSky;
   }

   public static boolean replacesOverworld() {
      return !failed && !suspended && ENGINE.visibility() >= 0.999F;
   }

   public static boolean visible() {
      return !failed && !suspended && ENGINE.visibility() > 0.5F;
   }

   public static boolean replacesEnd() {
      return !failed && !suspended && current() != null && (previewSky != null || ValenceConfig.get().skyInEnd);
   }

   private static void applySettings() {
      ValenceConfig cfg = ValenceConfig.get();
      SkyDefinition def = current();
      ENGINE.select(def == null ? null : def.sky);
      ENGINE.enabled = def != null;
      ENGINE.speed = cfg.skyAnimations ? cfg.skyAnimationSpeed : 0.0F;
      ENGINE.transitionSeconds = previewSky != null ? 0.35F : 0.45F;
      ENGINE.opacity = 1.0F;
      SkyCtx c = ENGINE.settings();
      c.quality = quality(cfg);
      c.brightness = cfg.skyBrightness;
      c.saturation = 1.0F;
      c.hue = 0.0F;
      c.stars = cfg.skyStars ? 1.0F : 0.0F;
      c.particles = cfg.skyParticles ? 1.0F : 0.0F;
      c.shooting = cfg.skyShootingStars;
      c.lightning = cfg.skyLightning;
      c.constellations = cfg.skyConstellations;
      ENGINE.nightOnly = false;
      ENGINE.night = night;
   }

   public static void render(float rainBrightness, float starBrightness) {
      if (!failed) {
         try {
            suspended = shaderPackActive();
            if (suspended) {
               return;
            }

            float rb = Math.max(0.1F, rainBrightness);
            night = G.clamp(starBrightness / rb / 0.4F, 0.0F, 1.0F);
            applySettings();
            ENGINE.settings().rain = ValenceConfig.get().skyDimInRain ? 1.0F - G.clamp(rainBrightness, 0.0F, 1.0F) : 0.0F;
            ENGINE.update();
            if (!ENGINE.active()) {
               return;
            }

            draw();
         } catch (Throwable e) {
            failed = true;
            ValenceClient.LOGGER.error("Valence sky failed, using the normal sky", e);
         }
      }
   }

   public static void renderEnd() {
      if (!failed) {
         try {
            suspended = shaderPackActive();
            if (suspended) {
               return;
            }

            night = 1.0F;
            applySettings();
            ENGINE.settings().rain = 0.0F;
            ENGINE.update();
            if (ENGINE.active()) {
               draw();
            }
         } catch (Throwable e) {
            failed = true;
            ValenceClient.LOGGER.error("Valence sky failed, using the normal sky", e);
         }
      }
   }

   private static void draw() {
      if (INSTANCE.buffers == null) {
         INSTANCE.buffers = MultiBufferSource.immediate(new ByteBufferBuilder(262144));
      }

      long now = System.nanoTime();
      int q = ENGINE.settings().quality;
      if (now - lastBuildNanos >= REBUILD_NANOS[q] || lastBuildNanos == 0L || ValenceConfig.revision() != lastRevision) {
         lastBuildNanos = now;
         lastRevision = ValenceConfig.revision();
         INSTANCE.recCount = 0;
         INSTANCE.layerCount = 0;
         ENGINE.render(INSTANCE);
      }

      INSTANCE.replay();
   }

   private static int quality(ValenceConfig cfg) {
      switch (cfg.skyQuality == null ? "auto" : cfg.skyQuality) {
         case "high":
            return 2;
         case "balanced":
            return 1;
         case "performance":
            return 0;
         default:
            long now = System.currentTimeMillis();
            if (now - qualityCheckAt >= 500L) {
               qualityCheckAt = now;
               int fps = Minecraft.getInstance().getFps();
               int limit = (Integer)Minecraft.getInstance().options.framerateLimit().get();
               int target = Math.max(30, Math.min(60, limit >= 260 ? 60 : limit - 5));
               if (fps < target - 8) {
                  highFrames = 0;
                  if (++lowFrames >= 2 && autoQuality > 0) {
                     autoQuality--;
                     lowFrames = 0;
                  }
               } else if (fps > target + 25) {
                  lowFrames = 0;
                  if (++highFrames >= 6 && autoQuality < 2) {
                     autoQuality++;
                     highFrames = 0;
                  }
               } else {
                  lowFrames = 0;
                  highFrames = 0;
               }
            }

            return autoQuality;
      }
   }

   public static float visibility() {
      return !failed && !suspended ? ENGINE.visibility() : 0.0F;
   }

   public static int fogColor(int vanilla) {
      return !failed && !suspended && ENGINE.active() ? ENGINE.fogColor(vanilla, ValenceConfig.get().skyFogTint) : vanilla;
   }

   private static boolean shaderPackActive() {
      long now = System.nanoTime();
      if (now - lastShaderCheck < 1000000000L) {
         return suspended;
      }

      lastShaderCheck = now;

      try {
         if (!irisLookedUp) {
            irisLookedUp = true;
            if (FabricLoader.getInstance().isModLoaded("iris")) {
               Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
               irisApi = api.getMethod("getInstance").invoke(null);
               irisInUse = api.getMethod("isShaderPackInUse");
            }
         }

         return irisInUse != null && Boolean.TRUE.equals(irisInUse.invoke(irisApi));
      } catch (Throwable e) {
         irisInUse = null;
         return false;
      }
   }

   @Override
   public void layer(SkyTex t) {
      if (this.layerCount == this.layerTex.length) {
         this.layerTex = Arrays.copyOf(this.layerTex, this.layerCount * 2);
         this.layerStart = Arrays.copyOf(this.layerStart, this.layerCount * 2);
      }

      this.layerTex[this.layerCount] = t;
      this.layerStart[this.layerCount] = this.recCount;
      this.layerCount++;
   }

   @Override
   public void vertex(float x, float y, float z, float u, float v, int argb) {
      if (this.recCount == this.rx.length) {
         int n = this.recCount * 2;
         this.rx = Arrays.copyOf(this.rx, n);
         this.ry = Arrays.copyOf(this.ry, n);
         this.rz = Arrays.copyOf(this.rz, n);
         this.ru = Arrays.copyOf(this.ru, n);
         this.rv = Arrays.copyOf(this.rv, n);
         this.rc = Arrays.copyOf(this.rc, n);
      }

      this.rx[this.recCount] = x;
      this.ry[this.recCount] = y;
      this.rz[this.recCount] = z;
      this.ru[this.recCount] = u;
      this.rv[this.recCount] = v;
      this.rc[this.recCount] = argb;
      this.recCount++;
   }

   @Override
   public void finish() {
   }

   private void replay() {
      for (int l = 0; l < this.layerCount; l++) {
         int start = this.layerStart[l];
         int end = l + 1 < this.layerCount ? this.layerStart[l + 1] : this.recCount;
         if (end - start >= 4) {
            if (this.vc == null) {
               this.vc = this.buffers.getBuffer(RenderTypes.beaconBeam(IDS.get(this.layerTex[l]), true));
            }

            for (int i = start; i < end; i++) {
               this.vc
                  .addVertex(this.rx[i], this.ry[i], this.rz[i])
                  .setColor(this.rc[i])
                  .setUv(this.ru[i], this.rv[i])
                  .setOverlay(655360)
                  .setLight(15728880)
                  .setNormal(0.0F, 1.0F, 0.0F);
            }

            int next = l + 1;

            while (next < this.layerCount && (next + 1 < this.layerCount ? this.layerStart[next + 1] : this.recCount) - this.layerStart[next] < 4) {
               next++;
            }

            if (next >= this.layerCount || this.layerTex[next] != this.layerTex[l]) {
               this.buffers.endBatch();
               this.vc = null;
            }
         }
      }

      if (this.vc != null) {
         this.buffers.endBatch();
      }

      this.vc = null;
   }

   static {
      for (SkyTex t : SkyTex.values()) {
         IDS.put(t, Identifier.fromNamespaceAndPath("valence", t.path()));
      }
   }
}
