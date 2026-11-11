import java.util.LinkedHashMap;
import java.util.Map;

public class LoadPanel implements LoadObserver {
    private final Map<String, Object> levels = new LinkedHashMap<>();

    public void update(Map<String, Double> load) {
        levels.clear();
        for (String muscle : load.keySet()) {
            int level = Math.min(10, Math.round(load.get(muscle).floatValue()));
            levels.put(muscle, level);
        }
    }

    public Map<String, Object> getLevels() { return new LinkedHashMap<>(levels); }
}
