package dev.valence.sky.classic;

public final class SkyEngine {
   private final G g = new G();
   private final SkyCtx ctx = new SkyCtx();
   private final SkyCtx prevCtx = new SkyCtx();
   private Sky current;
   private Sky previous;
   private float shown;
   private float previousShown;
   private float switchT = 1.0F;
   private double clock;
   private long lastNanos;
   public boolean enabled;
   public float speed = 1.0F;
   public float transitionSeconds = 1.6F;
   public boolean nightOnly;
   public float night = 1.0F;
   private float nightSmooth = 1.0F;
   public int lastQuads;
   public float opacity = 1.0F;

   public SkyCtx settings() {
      return this.ctx;
   }

   public Sky current() {
      return this.current;
   }

   public void select(Sky sky) {
      if (sky != this.current) {
         if (this.current != null && this.shown > 0.01F) {
            this.previous = this.current;
            this.previousShown = this.shown * ease(this.switchT);
            this.switchT = 0.0F;
         } else {
            this.previous = null;
            this.switchT = 1.0F;
         }

         this.current = sky;
      }
   }

   private static float ease(float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      return t * t * (3.0F - 2.0F * t);
   }

   public boolean active() {
      return (this.current != null && (this.enabled || this.shown > 0.002F) || this.previous != null) && this.nightSmooth > 0.004F;
   }

   public void update() {
      long now = System.nanoTime();
      float dt = this.lastNanos == 0L ? 0.016F : Math.min(0.25F, (float)(now - this.lastNanos) / 1.0E9F);
      this.lastNanos = now;
      this.step(dt);
   }

   public void step(float dt) {
      this.ctx.dt = dt;
      this.clock = this.clock + dt * this.speed;
      this.ctx.time = this.clock;
      float fadeSpeed = dt / Math.max(0.05F, this.transitionSeconds);
      float target = this.enabled && this.current != null ? 1.0F : 0.0F;
      if (this.shown < target) {
         this.shown = Math.min(target, this.shown + fadeSpeed);
      } else if (this.shown > target) {
         this.shown = Math.max(target, this.shown - fadeSpeed);
      }

      float nightTarget = this.nightOnly ? this.night : 1.0F;
      this.nightSmooth = this.nightSmooth + (nightTarget - this.nightSmooth) * Math.min(1.0F, dt * 1.5F);
      if (Math.abs(nightTarget - this.nightSmooth) < 0.004F) {
         this.nightSmooth = nightTarget;
      }

      if (this.switchT < 1.0F) {
         this.switchT = Math.min(1.0F, this.switchT + fadeSpeed);
         if (this.switchT >= 1.0F) {
            this.previous = null;
         }
      }
   }

   public float visibility() {
      float cur = ease(this.shown) * (this.previous != null ? ease(this.switchT) : 1.0F);
      float prev = this.previous != null ? this.previousShown * (1.0F - ease(this.switchT)) : 0.0F;
      return Math.min(1.0F, cur + prev) * this.nightSmooth * this.opacity;
   }

   public int fogColor(int vanilla, float strength) {
      strength *= this.nightSmooth * this.opacity;
      float b = this.ctx.brightness;
      int c = vanilla;
      if (this.previous != null) {
         c = G.mix(vanilla, G.bright(this.previous.fogColor, b), this.previousAlpha() * strength);
      }

      if (this.current != null) {
         float k = ease(this.shown) * (this.previous != null ? ease(this.switchT) : 1.0F);
         c = G.mix(c, G.bright(this.current.fogColor, b), k * strength);
      }

      return c;
   }

   private float previousAlpha() {
      return this.current == null ? this.previousShown * (1.0F - ease(this.switchT)) : this.previousShown;
   }

   public void render(SkyCanvas canvas) {
      this.g.quads = 0;
      if (this.previous != null) {
         copy(this.ctx, this.prevCtx);
         this.prevCtx.alpha = this.previousAlpha() * this.nightSmooth * this.opacity;
         this.g.begin(canvas, this.prevCtx);
         this.previous.render(this.g, this.prevCtx);
         this.stars(this.previous, this.prevCtx);
         this.previous.horizon(this.g);
      }

      if (this.current != null && this.shown > 0.002F && this.nightSmooth > 0.002F) {
         float k = ease(this.shown) * (this.previous != null ? ease(this.switchT) : 1.0F);
         this.ctx.alpha = k * this.nightSmooth * this.opacity;
         this.g.begin(canvas, this.ctx);
         this.current.render(this.g, this.ctx);
         this.stars(this.current, this.ctx);
         this.current.horizon(this.g);
      }

      canvas.finish();
      this.lastQuads = this.g.quads;
   }

   private void stars(Sky sky, SkyCtx c) {
      if (sky.starry && c.constellations && c.stars > 0.0F) {
         Constellations.draw(this.g, c, 0.9F * (1.0F - 0.8F * c.rain));
      }
   }

   public static int renderOnce(Sky sky, SkyCanvas canvas, SkyCtx c) {
      G g = new G();
      g.begin(canvas, c);
      sky.render(g, c);
      if (sky.starry && c.constellations && c.stars > 0.0F) {
         Constellations.draw(g, c, 0.9F);
      }

      sky.horizon(g);
      canvas.finish();
      return g.quads;
   }

   private static void copy(SkyCtx a, SkyCtx b) {
      b.time = a.time;
      b.dt = a.dt;
      b.quality = a.quality;
      b.brightness = a.brightness;
      b.saturation = a.saturation;
      b.hue = a.hue;
      b.stars = a.stars;
      b.particles = a.particles;
      b.rain = a.rain;
      b.shooting = a.shooting;
      b.lightning = a.lightning;
      b.constellations = a.constellations;
   }
}
