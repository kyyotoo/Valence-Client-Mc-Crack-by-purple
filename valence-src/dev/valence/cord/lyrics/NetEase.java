package dev.valence.cord.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.valence.ValenceClient;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NetEase {
   private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).followRedirects(Redirect.NORMAL).build();
   private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";
   private static final Pattern LINE = Pattern.compile("^\\[(\\d+),(\\d+)]");
   private static final Pattern WORD = Pattern.compile("\\((\\d+),(\\d+),[-\\d]+\\)");

   private NetEase() {
   }

   public static List<NowPlaying.Line> fetch(String title, String artist, long durationMs) {
      try {
         long id = find(title, artist, durationMs);
         if (id <= 0L) {
            return null;
         } else {
            HttpResponse<String> res = request("https://music.163.com/api/song/lyric/v1?id=" + id + "&lv=-1&kv=-1&tv=-1&rv=-1&yv=-1&ytv=-1&yrv=-1");
            if (res.statusCode() != 200) {
               return null;
            } else {
               JsonElement el = JsonParser.parseString(res.body());
               if (!el.isJsonObject()) {
                  return null;
               } else {
                  JsonObject root = el.getAsJsonObject();
                  JsonObject yrc = root.has("yrc") && root.get("yrc").isJsonObject() ? root.getAsJsonObject("yrc") : null;
                  if (yrc != null && yrc.has("lyric") && !yrc.get("lyric").isJsonNull()) {
                     List<NowPlaying.Line> lines = parse(yrc.get("lyric").getAsString());
                     return lines.size() >= 3 ? lines : null;
                  } else {
                     return null;
                  }
               }
            }
         }
      } catch (Exception e) {
         ValenceClient.LOGGER.debug("Valence: NetEase lyrics not available: {}", e.toString());
         return null;
      }
   }

   private static long find(String title, String artist, long durationMs) throws Exception {
      String cleanTitle = cleanTitle(title);
      String firstArtist = firstArtist(artist);
      HttpResponse<String> res = request(
         "https://music.163.com/api/search/get?type=1&limit=12&offset=0&s=" + URLEncoder.encode(cleanTitle + " " + firstArtist, StandardCharsets.UTF_8)
      );
      if (res.statusCode() != 200) {
         return -1L;
      }

      JsonElement el = JsonParser.parseString(res.body());
      if (el.isJsonObject() && el.getAsJsonObject().has("result")) {
         JsonObject result = el.getAsJsonObject().getAsJsonObject("result");
         if (result != null && result.has("songs")) {
            JsonArray songs = result.getAsJsonArray("songs");
            String wantTitle = norm(cleanTitle);
            String wantArtist = norm(firstArtist);
            long best = -1L;
            double bestScore = Double.MAX_VALUE;

            for (JsonElement e : songs) {
               if (e.isJsonObject()) {
                  JsonObject s = e.getAsJsonObject();
                  String name = norm(cleanTitle(s.has("name") ? s.get("name").getAsString() : ""));
                  if ((name.equals(wantTitle) || name.startsWith(wantTitle) || wantTitle.startsWith(name)) && !name.isEmpty()) {
                     boolean artistOk = false;
                     if (s.has("artists") && s.get("artists").isJsonArray()) {
                        for (JsonElement a : s.getAsJsonArray("artists")) {
                           String an = a.isJsonObject() && a.getAsJsonObject().has("name") ? norm(a.getAsJsonObject().get("name").getAsString()) : "";
                           if (!an.isEmpty() && (an.contains(wantArtist) || wantArtist.contains(an))) {
                              artistOk = true;
                           }
                        }
                     }

                     if (artistOk) {
                        double score = 0.0;
                        if (durationMs > 0L && s.has("duration")) {
                           score = Math.abs(s.get("duration").getAsLong() - durationMs) / 1000.0;
                           if (score > 4.0) {
                              continue;
                           }
                        }

                        if (score < bestScore) {
                           bestScore = score;
                           best = s.get("id").getAsLong();
                        }
                     }
                  }
               }
            }

            return best;
         } else {
            return -1L;
         }
      } else {
         return -1L;
      }
   }

   private static String norm(String s) {
      return s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", "");
   }

   static List<NowPlaying.Line> parse(String yrc) {
      List<long[]> starts = new ArrayList<>();
      List<String[]> wordLists = new ArrayList<>();
      List<long[]> timeLists = new ArrayList<>();

      for (String raw : yrc.split("\\r?\\n")) {
         String line = raw.trim();
         if (!line.isEmpty() && !line.startsWith("{")) {
            Matcher lm = LINE.matcher(line);
            if (lm.find()) {
               long ls = Long.parseLong(lm.group(1));
               long ld = Long.parseLong(lm.group(2));
               String body = line.substring(lm.end());
               Matcher wm = WORD.matcher(body);
               List<String> words = new ArrayList<>();
               List<Long> times = new ArrayList<>();
               int prevEnd = -1;
               long prevTime = 0L;
               StringBuilder cur = new StringBuilder();
               long curTime = -1L;
               List<String> parts = new ArrayList<>();
               List<Long> partTimes = new ArrayList<>();

               while (wm.find()) {
                  if (prevEnd >= 0) {
                     parts.add(body.substring(prevEnd, wm.start()));
                     partTimes.add(prevTime);
                  }

                  prevTime = Long.parseLong(wm.group(1));
                  prevEnd = wm.end();
               }

               if (prevEnd >= 0) {
                  parts.add(body.substring(prevEnd));
                  partTimes.add(prevTime);
               }

               for (int i = 0; i < parts.size(); i++) {
                  String p = parts.get(i);
                  if (curTime < 0L) {
                     curTime = partTimes.get(i);
                  }

                  cur.append(p);
                  boolean wordEnds = p.endsWith(" ") || i == parts.size() - 1 || parts.get(i + 1).startsWith(" ");
                  if (wordEnds) {
                     String w = cur.toString().trim();
                     if (!w.isEmpty()) {
                        for (String piece : w.split("\\s+")) {
                           words.add(piece);
                           times.add(curTime);
                        }
                     }

                     cur.setLength(0);
                     curTime = -1L;
                  }
               }

               if (!words.isEmpty()) {
                  long[] t = new long[times.size()];

                  for (int i = 0; i < t.length; i++) {
                     t[i] = times.get(i);
                  }

                  starts.add(new long[]{ls, ld});
                  wordLists.add(words.toArray(new String[0]));
                  timeLists.add(t);
               }
            }
         }
      }

      List<NowPlaying.Line> out = new ArrayList<>();

      for (int i = 0; i < starts.size(); i++) {
         String[] ws = wordLists.get(i);
         out.add(new NowPlaying.Line(starts.get(i)[0], String.join(" ", ws), ws, timeLists.get(i)));
         long end = starts.get(i)[0] + starts.get(i)[1];
         if (i + 1 < starts.size() && starts.get(i + 1)[0] - end > 2500L) {
            out.add(new NowPlaying.Line(end + 400L, "", new String[0], null));
         }
      }

      return out;
   }

   static String cleanTitle(String title) {
      String t = title == null ? "" : title;
      t = t.replaceAll("(?i)\\s*[\\(\\[](feat\\.?|ft\\.?|with)[^\\)\\]]*[\\)\\]]", "");
      t = t.replaceAll("(?i)\\s+-\\s+(remaster(ed)?|\\d{4} remaster(ed)?|live|radio edit|single version|mono|stereo|sped up|slowed).*$", "");
      return t.trim();
   }

   static String firstArtist(String artist) {
      String a = artist == null ? "" : artist;

      for (String sep : new String[]{",", " & ", " feat", " x ", ";", " ft."}) {
         int idx = a.toLowerCase(Locale.ROOT).indexOf(sep);
         if (idx > 0) {
            a = a.substring(0, idx);
         }
      }

      return a.trim();
   }

   private static HttpResponse<String> request(String url) throws Exception {
      HttpRequest req = HttpRequest.newBuilder(URI.create(url))
         .timeout(Duration.ofSeconds(6L))
         .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
         .header("Referer", "https://music.163.com/")
         .GET()
         .build();
      return HTTP.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8));
   }
}
