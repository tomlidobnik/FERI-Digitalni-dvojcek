package si.um.feri.copycats.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Dotenv {

  private static final Map<String, String> VALUES = new HashMap<>();
  private static volatile boolean loaded = false;

  private Dotenv() { }

  /**
   * Ensure .env values are loaded. This method is safe to call multiple times.
   */
  public static void load() {
    if (loaded) return;
    synchronized (Dotenv.class) {
      if (loaded) return;
      List<InputStream> candidates = new ArrayList<>();

      // 1) classpath root /.env
      try {
        InputStream in = Dotenv.class.getResourceAsStream("/.env");
        if (in != null) candidates.add(in);
      } catch (Exception ignored) { }

      // 2) same package resource
      try {
        String path = Dotenv.class.getPackage().getName().replace('.', '/') + "/.env";
        InputStream in = Dotenv.class.getClassLoader().getResourceAsStream(path);
        if (in != null) candidates.add(in);
      } catch (Exception ignored) { }

      // 3) working directory .env
      try {
        Path wd = Paths.get(".env");
        if (Files.exists(wd) && Files.isRegularFile(wd)) {
          candidates.add(Files.newInputStream(wd));
        }
      } catch (Exception ignored) { }

      // 4) fallback to repository path useful during development
      try {
        Path repo = Paths.get("core/src/main/java/si/um/feri/copycats/utils/.env");
        if (Files.exists(repo) && Files.isRegularFile(repo)) {
          candidates.add(Files.newInputStream(repo));
        }
      } catch (Exception ignored) { }

      for (InputStream in : candidates) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
          String line;
          while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int idx = line.indexOf('=');
            if (idx <= 0) continue;
            String key = line.substring(0, idx).trim();
            String val = line.substring(idx + 1).trim();
            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
              val = val.substring(1, val.length() - 1);
            }

            // prefer actual environment variables if present
            String env = System.getenv(key);
            if (env != null) {
              VALUES.put(key, env);
            } else {
              VALUES.put(key, val);
              try {
                System.setProperty(key, val);
              } catch (SecurityException ignored) { }
            }
          }
        } catch (IOException ignored) { }
      }

      loaded = true;
    }
  }

  /**
   * Get the value for a key. Checks (in order):
   *  - actual environment variables (System.getenv)
   *  - System properties (System.getProperty)
   *  - values loaded from .env
   */
  public static String get(String key) {
    if (!loaded) load();
    String v = System.getenv(key);
    if (v != null) return v;
    v = System.getProperty(key);
    if (v != null) return v;
    return VALUES.get(key);
  }

}
