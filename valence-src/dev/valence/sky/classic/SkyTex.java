package dev.valence.sky.classic;

public enum SkyTex {
   WHITE("white"),
   GLOW("glow"),
   DOT("dot"),
   STAR("star"),
   DISC("disc"),
   LINE("line"),
   STREAK("streak"),
   BEAM("beam"),
   PETAL("petal"),
   FLAKE("flake"),
   BUBBLE("bubble"),
   NEBULA1("nebula1"),
   NEBULA2("nebula2"),
   CLOUDS("clouds"),
   AURORA("aurora"),
   AURORA_BRIGHT("aurora_bright"),
   MILKYWAY("milkyway"),
   RAYS("rays"),
   CAUSTICS("caustics"),
   CRYSTAL("crystal"),
   TRAILS("trails"),
   SATURN("saturn"),
   RING("ring"),
   MOON("moon"),
   RAINBOW("rainbow"),
   SYNTHSUN("synthsun");

   public final String file;

   SkyTex(String file) {
      this.file = file;
   }

   public String path() {
      return "textures/sky/classic/" + this.file + ".png";
   }
}
