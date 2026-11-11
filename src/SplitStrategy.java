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
