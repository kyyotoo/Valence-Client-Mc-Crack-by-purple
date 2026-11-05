package dev.valence.perf;

import dev.valence.config.ValenceConfig;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class Occlusion {
   private static final long HIDE_AFTER_NS = 200000000L;
   private static final long FRESH_NS = 300000000L;
   private static final long FORGET_NS = 3000000000L;
   private static final double MAX_DIST = 128.0;
   private static final Map<Integer, Occlusion.State> ENTITIES = new ConcurrentHashMap<>();
   private static final Map<Long, Occlusion.State> BLOCKS = new ConcurrentHashMap<>();
   private static volatile double camX;
   private static volatile double camY;
   private static volatile double camZ;
   private static volatile boolean running;

   private Occlusion() {
   }

   private static boolean enabled() {
      return ValenceConfig.get().perfCulling;
   }

   private static void start() {
      if (!running) {
         synchronized (Occlusion.class) {
            if (!running) {
               running = true;
               Thread t = new Thread(Occlusion::loop, "Valence Culling");
               t.setDaemon(true);
               t.setPriority(2);
               t.start();
            }
         }
      }
   }

   public static boolean hideEntity(Entity e, double cx, double cy, double cz) {
      if (!enabled()) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      Entity camEntity = mc.getCameraEntity();
      if (e != camEntity
         && !(e instanceof Player)
         && !e.isCurrentlyGlowing()
         && !e.shouldShowName()
         && (camEntity == null || !camEntity.isSpectator() && !e.hasIndirectPassenger(camEntity) && !camEntity.hasIndirectPassenger(e))) {
         AABB b = e.getBoundingBox();
         if (!(b.getXsize() > 8.0) && !(b.getYsize() > 8.0) && !(b.getZsize() > 8.0)) {
            camX = cx;
            camY = cy;
            camZ = cz;
            start();
            Occlusion.State s = ENTITIES.computeIfAbsent(e.getId(), k -> new Occlusion.State());
            s.x0 = b.minX;
            s.y0 = b.minY;
            s.z0 = b.minZ;
            s.x1 = b.maxX;
            s.y1 = b.maxY;
            s.z1 = b.maxZ;
            s.blockTarget = false;
            return decide(s);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean hideBlock(BlockPos pos, double cx, double cy, double cz) {
      if (!enabled()) {
         return false;
      }

      Entity camEntity = Minecraft.getInstance().getCameraEntity();
      if (camEntity != null && camEntity.isSpectator()) {
         return false;
      }

      camX = cx;
      camY = cy;
      camZ = cz;
      start();
      Occlusion.State s = BLOCKS.computeIfAbsent(pos.asLong(), k -> new Occlusion.State());
      s.x0 = pos.getX();
      s.y0 = pos.getY();
      s.z0 = pos.getZ();
      s.x1 = pos.getX() + 1;
      s.y1 = pos.getY() + 1;
      s.z1 = pos.getZ() + 1;
      s.targetCell = pos.asLong();
      s.blockTarget = true;
      return decide(s);
   }

   private static boolean decide(Occlusion.State s) {
      long now = System.nanoTime();
      s.lastQuery = now;
      long check = s.lastCheck;
      return check != 0L && now - check <= 300000000L ? now - s.lastVisible > 200000000L : false;
   }

   public static void clear() {
      ENTITIES.clear();
      BLOCKS.clear();
   }

   private static void loop() {
      while (true) {
         try {
            long start = System.nanoTime();
            if (enabled()) {
               ClientLevel level = Minecraft.getInstance().level;
               if (level == null) {
                  clear();
               } else {
                  sweep(ENTITIES, level, start);
                  sweep(BLOCKS, level, start);
               }
            }

            long took = (System.nanoTime() - start) / 1000000L;
            Thread.sleep(Math.max(4L, 10L - took));
         } catch (InterruptedException ie) {
            return;
         } catch (Throwable var5) {
         }
      }
   }

   private static <K> void sweep(Map<K, Occlusion.State> map, ClientLevel level, long now) {
      double cx = camX;
      double cy = camY;
      double cz = camZ;
      Iterator<Entry<K, Occlusion.State>> it = map.entrySet().iterator();

      while (it.hasNext()) {
         Occlusion.State s = it.next().getValue();
         if (now - s.lastQuery > 3000000000L) {
            it.remove();
         } else {
            boolean visible;
            try {
               visible = visible(level, s, cx, cy, cz);
            } catch (Throwable t) {
               visible = true;
            }

            long t = System.nanoTime();
            if (visible) {
               s.lastVisible = t;
            }

            s.lastCheck = t;
         }
      }
   }

   private static boolean visible(ClientLevel level, Occlusion.State s, double cx, double cy, double cz) {
      double x0 = s.x0;
      double y0 = s.y0;
      double z0 = s.z0;
      double x1 = s.x1;
      double y1 = s.y1;
      double z1 = s.z1;
      if (cx > x0 - 2.0 && cx < x1 + 2.0 && cy > y0 - 2.0 && cy < y1 + 2.0 && cz > z0 - 2.0 && cz < z1 + 2.0) {
         return true;
      }

      double mx = (x0 + x1) * 0.5;
      double my = (y0 + y1) * 0.5;
      double mz = (z0 + z1) * 0.5;
      double dx = mx - cx;
      double dy = my - cy;
      double dz = mz - cz;
      if (dx * dx + dy * dy + dz * dz > 16384.0) {
         return true;
      }

      MutableBlockPos mp = new MutableBlockPos();
      double ix = Math.min(0.08, (x1 - x0) * 0.25);
      double iy = Math.min(0.08, (y1 - y0) * 0.25);
      double iz = Math.min(0.08, (z1 - z0) * 0.25);
      if (clearRay(level, mp, cx, cy, cz, mx, my, mz, s)) {
         return true;
      }

      for (int i = 0; i < 8; i++) {
         double px = (i & 1) == 0 ? x0 + ix : x1 - ix;
         double py = (i & 2) == 0 ? y0 + iy : y1 - iy;
         double pz = (i & 4) == 0 ? z0 + iz : z1 - iz;
         if (clearRay(level, mp, cx, cy, cz, px, py, pz, s)) {
            return true;
         }
      }

      return false;
   }

   private static boolean clearRay(ClientLevel level, MutableBlockPos mp, double ax, double ay, double az, double bx, double by, double bz, Occlusion.State s) {
      int x = floor(ax);
      int y = floor(ay);
      int z = floor(az);
      int tx = floor(bx);
      int ty = floor(by);
      int tz = floor(bz);
      double dx = bx - ax;
      double dy = by - ay;
      double dz = bz - az;
      int sx = dx > 0.0 ? 1 : (dx < 0.0 ? -1 : 0);
      int sy = dy > 0.0 ? 1 : (dy < 0.0 ? -1 : 0);
      int sz = dz > 0.0 ? 1 : (dz < 0.0 ? -1 : 0);
      double tdx = sx != 0 ? Math.abs(1.0 / dx) : Double.MAX_VALUE;
      double tdy = sy != 0 ? Math.abs(1.0 / dy) : Double.MAX_VALUE;
      double tdz = sz != 0 ? Math.abs(1.0 / dz) : Double.MAX_VALUE;
      double tmx = sx > 0 ? (x + 1 - ax) * tdx : (sx < 0 ? (ax - x) * tdx : Double.MAX_VALUE);
      double tmy = sy > 0 ? (y + 1 - ay) * tdy : (sy < 0 ? (ay - y) * tdy : Double.MAX_VALUE);
      double tmz = sz > 0 ? (z + 1 - az) * tdz : (sz < 0 ? (az - z) * tdz : Double.MAX_VALUE);
      int steps = Math.abs(tx - x) + Math.abs(ty - y) + Math.abs(tz - z);

      for (int i = 0; i < steps; i++) {
         if (tmx < tmy && tmx < tmz) {
            x += sx;
            tmx += tdx;
         } else if (tmy < tmz) {
            y += sy;
            tmy += tdy;
         } else {
            z += sz;
            tmz += tdz;
         }

         if (x == tx && y == ty && z == tz) {
            return true;
         }

         if (s.blockTarget && BlockPos.asLong(x, y, z) == s.targetCell) {
            return true;
         }

         if (y >= level.getMinY() && y <= level.getMaxY()) {
            BlockState st = level.getBlockState(mp.set(x, y, z));
            if (st.canOcclude() && st.isSolidRender()) {
               return false;
            }
         }
      }

      return true;
   }

   private static int floor(double v) {
      int i = (int)v;
      return v < i ? i - 1 : i;
   }

   private static final class State {
      volatile double x0;
      volatile double y0;
      volatile double z0;
      volatile double x1;
      volatile double y1;
      volatile double z1;
      volatile long lastQuery;
      volatile long lastVisible;
      volatile long lastCheck;
      volatile long targetCell;
      volatile boolean blockTarget;
   }
}
