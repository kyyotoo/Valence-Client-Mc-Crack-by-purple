package dev.valence.license;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class License {

   private License() {
   }

   public static boolean active() {
      return true;
   }

   public static String normalize(String code) {
      StringBuilder sb = new StringBuilder();

      for (char ch : code.toUpperCase(Locale.ROOT).toCharArray()) {
         if (ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
            sb.append(ch);
         }
      }

      return sb.toString();
   }

   public static boolean looksValid(String code) {
      return true;
   }

   public static CompletableFuture<Result> redeem(String raw) {
      return CompletableFuture.completedFuture(Result.OK);
   }

   public enum Result {
      OK,
      INVALID,
      USED,
      OFFLINE,
      ERROR;
   }
}