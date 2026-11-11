public abstract class SplitFactory {
    public abstract SplitStrategy create();

    public static SplitFactory forId(String id) {
        if (id.equals("full")) return new FullBodyFactory();
        if (id.equals("upper")) return new UpperLowerFactory();
        if (id.equals("classic")) return new ClassicFactory();
        if (id.equals("ppl")) return new PushPullLegsFactory();
        throw new IllegalArgumentException("Неизвестная программа");
    }

    public static SplitStrategy recommend(int days) {
        if (days < 1 || days > 7) throw new IllegalArgumentException("Введите число дней от 1 до 7");
        if (days <= 3) return forId("full").create();
        if (days == 4) return forId("upper").create();
        if (days == 5) return forId("classic").create();
        return forId("ppl").create();
    }
}

class FullBodyFactory extends SplitFactory {
    public SplitStrategy create() { return new FullBodySplit(); }
}

class UpperLowerFactory extends SplitFactory {
    public SplitStrategy create() { return new UpperLowerSplit(); }
}

class ClassicFactory extends SplitFactory {
    public SplitStrategy create() { return new ClassicSplit(); }
}

class PushPullLegsFactory extends SplitFactory {
    public SplitStrategy create() { return new PushPullLegsSplit(); }
}
