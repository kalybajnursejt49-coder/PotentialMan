public interface SplitStrategy {
    String getId();
    String getName();
    String[] getSchedule();
}

class FullBodySplit implements SplitStrategy {
    public String getId() { return "full"; }
    public String getName() { return "Фулбади"; }
    public String[] getSchedule() { return new String[]{"Пн · Всё тело", "Ср · Всё тело", "Пт · Всё тело"}; }
}

class UpperLowerSplit implements SplitStrategy {
    public String getId() { return "upper"; }
    public String getName() { return "Верх / Низ"; }
    public String[] getSchedule() { return new String[]{"Пн · Верх", "Вт · Низ", "Чт · Верх", "Пт · Низ"}; }
}

class ClassicSplit implements SplitStrategy {
    public String getId() { return "classic"; }
    public String getName() { return "Классический сплит"; }
    public String[] getSchedule() { return new String[]{"Пн · Грудь", "Вт · Спина", "Ср · Ноги", "Пт · Плечи", "Сб · Руки"}; }
}

class PushPullLegsSplit implements SplitStrategy {
    public String getId() { return "ppl"; }
    public String getName() { return "Тяни / Толкай / Ноги"; }
    public String[] getSchedule() { return new String[]{"Пн · Толкай", "Вт · Тяни", "Ср · Ноги", "Чт · Толкай", "Пт · Тяни", "Сб · Ноги"}; }
}

class WeightLossSplit implements SplitStrategy {
    public String getId() { return "weightloss"; }
    public String getName() { return "Снижение веса"; }
    public String[] getSchedule() { return new String[]{"Пн · Силовая", "Вт · Бег", "Чт · Силовая", "Сб · Ходьба / кардио"}; }
}

class RunningSplit implements SplitStrategy {
    public String getId() { return "running"; }
    public String getName() { return "Бег по утрам"; }
    public String[] getSchedule() { return new String[]{"Пн · Лёгкий бег", "Ср · Интервалы", "Пт · Лёгкий бег"}; }
}

class MixedSplit implements SplitStrategy {
    public String getId() { return "mixed"; }
    public String getName() { return "Силовые + кардио"; }
    public String[] getSchedule() { return new String[]{"Пн · Верх", "Вт · Бег", "Чт · Низ", "Сб · Кардио"}; }
}

class HomeSplit implements SplitStrategy {
    public String getId() { return "home"; }
    public String getName() { return "Тренировки дома"; }
    public String[] getSchedule() { return new String[]{"Пн · Всё тело", "Ср · Ноги и пресс", "Пт · Всё тело"}; }
}

class RecoverySplit implements SplitStrategy {
    public String getId() { return "recovery"; }
    public String getName() { return "Лёгкая активность"; }
    public String[] getSchedule() { return new String[]{"Вт · Ходьба", "Чт · Мобильность", "Сб · Лёгкое кардио"}; }
}
