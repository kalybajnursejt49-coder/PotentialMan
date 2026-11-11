import java.util.Map;

public interface LoadObserver {
    void update(Map<String, Double> load);
}
