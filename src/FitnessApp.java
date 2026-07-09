import java.util.Scanner;

public class FitnessApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Health health = new Health();

        System.out.println("Введите данные пользователя 1");
        User user1 = readUser(scanner);
        readWorkouts(scanner, user1);

        System.out.println("Введите данные пользователя 2");
        User user2 = readUser(scanner);
        readWorkouts(scanner, user2);

        System.out.println();
        System.out.println(user1);
        System.out.println(user2);

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1 - Пользователь 1");
            System.out.println("2 - Пользователь 2");
            System.out.println("3 - Сравнить пользователей");
            System.out.println("0 - Выход");
            System.out.print("Ваш выбор: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                running = false;
            } else if (choice == 1) {
                showMenu(scanner, health, user1);
            } else if (choice == 2) {
                showMenu(scanner, health, user2);
            } else if (choice == 3) {
                compareUsers(health, user1, user2);
            } else {
                System.out.println("Некорректный выбор");
            }
        }

        System.out.println("Программа завершена");
        scanner.close();
    }

    private static User readUser(Scanner scanner) {
        System.out.print("Возраст: ");
        int age = scanner.nextInt();
        System.out.print("Вес (кг): ");
        double weight = scanner.nextDouble();
        System.out.print("Рост (см): ");
        double height = scanner.nextDouble();
        scanner.nextLine();
        System.out.print("Пол (M/F): ");
        String gender = scanner.nextLine();
        return new User(age, weight, height, gender);
    }

    private static void readWorkouts(Scanner scanner, User user) {
        System.out.print("Сколько тренировок добавить: ");
        int count = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < count; i++) {
            System.out.print("Тип тренировки: ");
            String type = scanner.nextLine();
            System.out.print("Длительность (мин): ");
            int duration = scanner.nextInt();
            System.out.print("Сожжено калорий: ");
            int calories = scanner.nextInt();
            scanner.nextLine();
            user.addWorkout(new WorkoutPlan(type, duration, calories));
        }
    }

    private static void showMenu(Scanner scanner, Health health, User user) {
        System.out.println();
        System.out.println("1 - BMI");
        System.out.println("2 - BMR и дневная норма");
        System.out.println("3 - Список тренировок");
        System.out.print("Ваш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.println("BMI: " + health.getBMICategory(user));
        } else if (choice == 2) {
            double bmr = health.calculateBMR(user);
            System.out.printf("BMR: %.2f kcal%n", bmr);
            String goal = readGoal(scanner);
            double dailyNorm = health.calculateDailyNorm(user, goal);
            System.out.printf("Дневная норма (%s): %.2f kcal%n", goal, dailyNorm);
        } else if (choice == 3) {
            for (WorkoutPlan plan : user.getWorkouts()) {
                System.out.println(plan);
            }
        } else {
            System.out.println("Некорректный выбор");
        }
    }

    private static String readGoal(Scanner scanner) {
        System.out.println("1 - Похудение (lose)");
        System.out.println("2 - Набор массы (gain)");
        System.out.println("3 - Поддержание (maintain)");
        System.out.print("Ваш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) return "lose";
        if (choice == 2) return "gain";
        return "maintain";
    }

    private static void compareUsers(Health health, User user1, User user2) {
        double bmr1 = health.calculateBMR(user1);
        double bmr2 = health.calculateBMR(user2);
        int calories1 = user1.getTotalCalories();
        int calories2 = user2.getTotalCalories();

        System.out.println();
        if (bmr1 > bmr2) {
            System.out.println("У пользователя 1 BMR выше");
        } else if (bmr2 > bmr1) {
            System.out.println("У пользователя 2 BMR выше");
        } else {
            System.out.println("BMR одинаковый");
        }

        if (calories1 > calories2) {
            System.out.println("Пользователь 1 сжёг больше калорий на тренировках");
        } else if (calories2 > calories1) {
            System.out.println("Пользователь 2 сжёг больше калорий на тренировках");
        } else {
            System.out.println("Пользователи сожгли одинаковое количество калорий");
        }
    }
}