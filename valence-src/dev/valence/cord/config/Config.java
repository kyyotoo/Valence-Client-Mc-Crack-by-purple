package dev.valence.cord.config;

import dev.valence.cord.hud.Gfx;
import dev.valence.cord.sky.SkyPreset;
import dev.valence.cord.sky.WorldSky;
import java.util.Locale;

public class Config {
   public int accent = 0;
   public boolean rgbAccent = false;
   public boolean blur = true;
   public boolean particles = false;
   public boolean ripples = false;
   public boolean sounds = true;
   public int uiFont = 0;
   public int lyricsFont = 0;
   public boolean fxMenuAnim = true;
   public int sky = 0;
   public int lastSky = 0;
   public float skyStrength = 0.8F;
   public float weatherAmount = 1.0F;
   public boolean realWeather = true;
   public boolean hideClouds = true;
   public String cloudsBefore = "";
   public boolean skyDay = true;
   public boolean fxDayNight = false;
   public boolean fxConstellations = true;
   public boolean fxSkyMusic = true;
   public float skySpeed = 1.0F;
   public int skyPalette = -1;
   public int hat = 0;
   public boolean hatGlow = true;
   public int crosshair = 0;
   public int trail = 0;
   public boolean aura = false;
   public int deathFx = 1;
   public boolean deathOthers = false;
   public boolean beatGlow = false;
   public boolean skyShuffle = false;
   public float skyPattern = 1.0F;
   public float skyLively = 0.6F;
   public float skyBreath = 1.0F;
   public int customTop = 656920;
   public int customHorizon = 2758216;
   public int customAurora1 = 10719743;
   public int customAurora2 = 16777215;
   public int customWeather = 2;
   public boolean customAurora = true;
   public boolean customStars = true;
   public boolean customMoon = false;
   public boolean customShooting = true;
   public boolean lyricsSky = true;
   public boolean lyricsIsland = false;
   public boolean lyricsFollow = true;
   public int lyricsStyle = 0;
   public boolean lyricsViewLock = false;
   public float lyricsSize = 1.6F;
   public float lyricsOpacity = 0.9F;
   public boolean lyricsGlow = true;
   public float lyricsGlowStrength = 1.0F;
   public int lyricsGlowColor = -1;
   public int lyricsColor = -1;
   public boolean lyricsGradient = false;
   public int lyricsGradA = 16747100;
   public int lyricsGradB = 16735400;
   public int lyricsAnim = 0;
   public int lyricsReveal = 0;
   public String lyricsPreset = "";
   public boolean lyricsUnderground = false;
   public int lyricsOffsetMs = 0;
   public boolean wordSync = true;
   public boolean fxBeatPulse = true;
   public boolean fxSongIntro = true;
   public boolean translate = false;
   public String translateTo = "de";
   public boolean islandEnabled = true;
   public boolean showClock = false;
   public boolean showPing = true;
   public boolean showFps = true;
   public boolean albumCover = true;
   public boolean glassHotbar = false;
   public int hotbarStyle = 0;
   public boolean fxStartup = true;
   public int quality = 1;
   public boolean fpsBoost = false;
   public String boostBefore = "";
   public boolean autoPerf = true;
   public int targetFps = 0;
   public boolean fxWeather = true;
   public boolean fxAurora = true;
   public boolean fxStars = true;
   public boolean fxBeams = true;
   public boolean fxSkyColor = true;
   public int profile = 0;
   public int pvpLyricsStyle = 1;
   public int pvpLyricsFont = 21;
   public int pvpLyricsColor = 8;
   public boolean pvpLyricsGlow = false;
   public float pvpLyricsSize = 1.6F;
   public int chillLyricsStyle = 0;
   public int chillLyricsFont = 0;
   public int chillLyricsColor = -1;
   public boolean chillLyricsGlow = true;
   public float chillLyricsSize = 1.6F;
   public boolean demoLyrics = false;
   public String lastTitle = "";
   public String lastArtist = "";
   public String spotifyClientId = "";
   public int spotifyMode = 0;
   public boolean spotifyTutorialSeen = false;
   public int styleVersion = 0;

   public void clamp() {
      this.deathFx = Math.max(0, Math.min(3, this.deathFx));
      if (this.styleVersion < 2 && this.spotifyClientId != null && !this.spotifyClientId.isBlank()) {
         this.spotifyMode = 1;
      }

      if (this.styleVersion < 3 && System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         this.spotifyMode = 0;
      }

      if (this.styleVersion < 7) {
         this.fxDayNight = false;
         this.skyDay = true;
      }

      if (this.styleVersion < 8) {
         this.styleVersion = 8;
      }

      if (this.styleVersion < 9) {
         this.autoPerf = true;
         this.targetFps = 0;
         this.styleVersion = 9;
      }

      if (this.styleVersion < 10) {
         this.pvpLyricsStyle = 1;
         this.pvpLyricsFont = 21;
         this.pvpLyricsColor = 8;
         this.pvpLyricsGlow = false;
         this.pvpLyricsSize = 1.6F;
         if (this.profile == 1) {
            this.chillLyricsStyle = this.lyricsStyle;
            this.chillLyricsFont = this.lyricsFont;
            this.chillLyricsColor = this.lyricsColor;
            this.chillLyricsGlow = this.lyricsGlow;
            this.chillLyricsSize = this.lyricsSize;
            this.lyricsStyle = this.pvpLyricsStyle;
            this.lyricsFont = this.pvpLyricsFont;
            this.lyricsColor = this.pvpLyricsColor;
            this.lyricsGlow = this.pvpLyricsGlow;
            this.lyricsSize = this.pvpLyricsSize;
         }

         this.styleVersion = 10;
      }

      this.spotifyMode = Math.max(0, Math.min(1, this.spotifyMode));
      this.rgbAccent = false;
      int fonts = Gfx.FONT_IDS.length;
      this.uiFont = Math.max(0, Math.min(fonts, this.uiFont));
      this.lyricsFont = Math.max(0, Math.min(fonts - 1, this.lyricsFont));
      this.sky = Math.max(-1, Math.min(SkyPreset.values().length - 1, this.sky));
      this.skyStrength = Math.max(0.1F, Math.min(1.0F, this.skyStrength));
      this.weatherAmount = Math.max(0.0F, Math.min(2.0F, this.weatherAmount));
      this.lyricsSize = Math.max(0.6F, Math.min(4.0F, this.lyricsSize));
      this.lyricsOpacity = Math.max(0.2F, Math.min(1.0F, this.lyricsOpacity));
      this.lyricsGlowStrength = Math.max(0.2F, Math.min(2.5F, this.lyricsGlowStrength));
      this.lyricsAnim = Math.floorMod(this.lyricsAnim, 3);
      this.quality = Math.max(0, Math.min(2, this.quality));
      if (this.targetFps != 0) {
         this.targetFps = Math.max(30, Math.min(240, this.targetFps));
      }

      this.skySpeed = Math.max(0.25F, Math.min(2.0F, this.skySpeed));
      this.skyPattern = Math.max(0.2F, Math.min(1.5F, this.skyPattern));
      this.skyLively = Math.max(0.0F, Math.min(1.0F, this.skyLively));
      this.skyBreath = Math.max(0.0F, Math.min(2.0F, this.skyBreath));
      this.skyPalette = Math.max(-1, Math.min(WorldSky.PALETTES.length - 1, this.skyPalette));
      this.lyricsOffsetMs = Math.max(-5000, Math.min(5000, this.lyricsOffsetMs));
      this.lyricsStyle = Math.floorMod(this.lyricsStyle, 3);
      this.lyricsReveal = Math.floorMod(this.lyricsReveal, 3);
      if (this.lyricsPreset == null) {
         this.lyricsPreset = "";
      }

      this.hotbarStyle = Math.floorMod(this.hotbarStyle, 5);
      if (this.spotifyClientId == null) {
         this.spotifyClientId = "";
      }

      if (this.cloudsBefore == null) {
         this.cloudsBefore = "";
      }

      if (this.boostBefore == null) {
         this.boostBefore = "";
      }

      if (this.lastTitle == null) {
         this.lastTitle = "";
      }

      if (this.lastArtist == null) {
         this.lastArtist = "";
      }

      if (this.translateTo == null) {
         this.translateTo = "de";
      }

      if (this.sky >= 0) {
         this.lastSky = this.sky;
      }

      if (this.styleVersion < 11) {
         this.showClock = false;
         this.showFps = true;
         this.showPing = true;
         this.hotbarStyle = 0;
         this.styleVersion = 11;
      }

      if (this.styleVersion < 12) {
         this.showClock = false;
         this.showFps = true;
         this.showPing = true;
         this.lyricsIsland = false;
         this.lyricsOffsetMs = 0;
         this.styleVersion = 12;
      }

      this.hat = 0;
      this.trail = 0;
      this.aura = false;
      this.crosshair = 0;
      this.beatGlow = false;
      this.deathFx = 0;
      this.deathOthers = false;
      this.sky = -1;
   }
}
