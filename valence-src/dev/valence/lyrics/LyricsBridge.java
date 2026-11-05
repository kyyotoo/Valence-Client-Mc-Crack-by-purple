package dev.valence.lyrics;

import dev.valence.config.ValenceConfig;
import dev.valence.cord.NeonLyrics;
import dev.valence.cord.lyrics.LyricTiming;
import dev.valence.cord.lyrics.NowPlaying;
import dev.valence.cord.lyrics.SpotifyClient;

public final class LyricsBridge {
   private static String track = "";
   private static int line = -1;
   private static long lastPos;
   private static boolean paused;

   private LyricsBridge() {
   }

   public static long positionMs() {
      NowPlaying np = SpotifyClient.now();
      return np.hasTrack() ? np.progressNow(NeonLyrics.cfg().lyricsOffsetMs) : 0L;
   }

   public static void tick() {
      SkyLyrics.tickCombat();
      ValenceConfig cfg = ValenceConfig.get();
      if (!cfg.lyricsGlowDefaultOff) {
         cfg.lyricsGlowDefaultOff = true;
         cfg.lyricsGlow = 0.0F;
         ValenceConfig.save();
      }

      if (!cfg.hudLogoDefaultOff) {
         cfg.hudLogoDefaultOff = true;
         cfg.hudLogo = false;
         ValenceConfig.save();
      }

      NowPlaying np = SpotifyClient.now();
      if (cfg.lyricsEnabled && np != null && np.hasTrack() && (!"demo".equals(np.trackId) || NeonLyrics.cfg().demoLyrics)) {
         String id = String.valueOf(np.trackId);
         if (!id.equals(track)) {
            track = id;
            line = -1;
            SkyLyrics.clear();
            if (cfg.lyricsShowTitleWithoutLyrics && np.title != null && !np.title.isBlank() && !"last".equals(id)) {
               SkyLyrics.showTitle(np.title, np.artist);
            }
         }

         if (!np.playing) {
            if (!paused) {
               paused = true;
               line = -1;
               SkyLyrics.clear();
            }
         } else {
            paused = false;
            if (np.lyricsState == NowPlaying.LyricsState.SYNCED && !np.lines.isEmpty()) {
               long pos = positionMs();
               int idx = np.lineIndex(pos + 160L);
               boolean seek = Math.abs(pos - lastPos) > 3000L;
               lastPos = pos;
               if (idx != line) {
                  if (seek || idx < line || idx > line + 2) {
                     SkyLyrics.clear();
                  }

                  line = idx;
                  if (idx >= 0 && idx < np.lines.size()) {
                     NowPlaying.Line l = np.lines.get(idx);
                     String[] words = l.words();
                     if (words != null && words.length != 0 && !l.text().isBlank()) {
                        long start = l.timeMs();
                        long end = Math.max(start + 300L, np.lineEnd(idx));
                        long[] starts = null;
                        LyricTiming t = LyricTiming.of(np.lines);
                        if (t != null && idx < t.wordStart.length && t.wordStart[idx].length == words.length) {
                           starts = (long[])t.wordStart[idx].clone();
                        }

                        if (starts == null) {
                           starts = new long[words.length];
                           long span = Math.max(300L, Math.min(end - start, 6000L));

                           for (int i = 0; i < words.length; i++) {
                              starts[i] = start + span * i / Math.max(1, words.length);
                           }
                        }

                        SkyLyrics.addLine(words, starts, start, end);
                     }
                  }
               }
            }
         }
      } else {
         if (!track.isEmpty()) {
            track = "";
            line = -1;
            SkyLyrics.clear();
         }
      }
   }
}
