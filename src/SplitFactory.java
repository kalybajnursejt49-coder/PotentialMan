public abstract class SplitFactory {
    public abstract SplitStrategy create();

    public static SplitFactory forId(String id) {
        if (id.equals("full")) return new FullBodyFactory();
        if (id.equals("upper")) return new UpperLowerFactory();
        if (id.equals("classic")) return new ClassicFactory();
        if (id.equals("ppl")) return new PushPullLegsFactory();
        if (id.equals("weightloss")) return new WeightLossFactory();
        if (id.equals("running")) return new RunningFactory();
        if (id.equals("mixed")) return new MixedFactory();
        if (id.equals("home")) return new HomeFactory();
        if (id.equals("recovery")) return new RecoveryFactory();
        throw new IllegalArgumentException("Неизвестная программа");
    }

    public static SplitStrategy recommend(int days) {
        return recommend(days, "strength");
    }

    public static SplitStrategy recommend(int days, String goal) {
        if (days < 1 || days > 7) throw new IllegalArgumentException("Введите число дней от 1 до 7");
        if (goal.equals("lose")) return forId("weightloss").create();
        if (goal.equals("run")) return forId("running").create();
        if (goal.equals("mixed")) return forId("mixed").create();
        if (goal.equals("home")) return forId("home").create();
        if (goal.equals("recovery")) return forId("recovery").create();
        if (!goal.equals("strength")) throw new IllegalArgumentException("Неизвестная цель программы");
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

class WeightLossFactory extends SplitFactory {
    public SplitStrategy create() { return new WeightLossSplit(); }
}

class RunningFactory extends SplitFactory {
    public SplitStrategy create() { return new RunningSplit(); }
}

class MixedFactory extends SplitFactory {
    public SplitStrategy create() { return new MixedSplit(); }
}

class HomeFactory extends SplitFactory {
    public SplitStrategy create() { return new HomeSplit(); }
}

class RecoveryFactory extends SplitFactory {
    public SplitStrategy create() { return new RecoverySplit(); }
}
