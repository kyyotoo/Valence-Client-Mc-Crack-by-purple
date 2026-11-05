package dev.valence.sky.classic;

public abstract class Sky {
   public final String id;
   public final int fogColor;
   public final int accent;
   protected float fadeFull = 0.5F;
   protected float fadeTop = 13.0F;
   boolean starry;

   Sky starry() {
      this.starry = true;
      return this;
   }

   public boolean isStarry() {
      return this.starry;
   }

   public Sky(String id, int fogColor, int accent) {
      this.id = id;
      this.fogColor = fogColor;
      this.accent = accent;
   }

   public abstract void render(G var1, SkyCtx var2);

   void horizon(G g) {
      g.horizonFade(this.fogColor, this.fadeFull, this.fadeTop);
   }
}
