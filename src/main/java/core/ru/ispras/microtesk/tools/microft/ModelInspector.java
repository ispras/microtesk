package ru.ispras.microtesk.tools.metadata;

import ru.ispras.microft.service.json.JsonStorage;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.Model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.json.*;

public class ModelInspector {
  private final JsonStorage db = new JsonStorage();

  public static void store(final Set<String> names, final OutputStream os)
    throws IOException {
    final Map<String, Model> models = new LinkedHashMap<>();
    for (final String modelName : names) {
      final Model model = loadModel(modelName);
      if (model != null) {
        models.put(modelName, model);
      }
    }
    final ModelInspector inspector = new ModelInspector();
    inspector.inspectAll(models);
    inspector.store(os);
  }

  private void inspectAll(final Map<String, ? extends Model> models) {
    final JsonArrayBuilder archIndex = Json.createArrayBuilder();

    int index = 0;
    for (final Model model : models.values()) {
      archIndex.add(Json.createObjectBuilder()
        .add("id", index++)
        .add("name", model.getName()));
    }

    final Iterator<? extends Model> it = models.values().iterator();
    for (final JsonStorage.Ref ref : db.createEntry("arch").set(archIndex)) {
      inspectModel(it.next(), ref.getItem());
    }
  }

  private void inspectModel(final Model model, final JsonStorage.RefItem ref) {
  }

  private void store(final OutputStream os) throws IOException {
    db.write(os);
  }

  private static Model loadModel(final String modelName) {
    try {
      return SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error("Failed to load the %s model. Reason: %s.", modelName, e.getMessage());
      return null;
    }
  }
}
