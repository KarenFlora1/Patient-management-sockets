package common.protocol;

import java.io.*;
import java.time.LocalDate;

import com.google.gson.*;

public class LineJson {
  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, t, ctx) ->
          src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString()))
      .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, t, ctx) ->
          (json == null || json.isJsonNull()) ? null : LocalDate.parse(json.getAsString()))
      .create();

  public static void send(Object obj, Writer w) throws IOException {
    w.write(gson.toJson(obj));
    w.write("\n");
    w.flush();
  }

  public static <T> T recv(BufferedReader r, Class<T> type) throws IOException {
    String line = r.readLine();
    if (line == null) throw new EOFException("ligação fechada");
    return gson.fromJson(line, type);
  }
}
