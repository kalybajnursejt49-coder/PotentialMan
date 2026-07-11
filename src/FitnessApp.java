import java.util.ArrayList;
import java.util.List;
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
            System.out.println("4 - Работа с пулом тренировок");
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
            } else if (choice == 4) {
                showDataPoolMenu(scanner, user1, user2);
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
            System.out.println("1 - Cardio");
            System.out.println("2 - Strength");
            System.out.print("Тип тренировки: ");
            int typeChoice = scanner.nextInt();

            System.out.print("Длительность (мин): ");
            int duration = scanner.nextInt();
            System.out.print("Сожжено калорий: ");
            int calories = scanner.nextInt();

            if (typeChoice == 1) {
                System.out.print("Дистанция (км): ");
                double distance = scanner.nextDouble();
                scanner.nextLine();
                user.addWorkout(new CardioWorkout(duration, calories, distance));
            } else {
                System.out.print("Количество подходов: ");
                int sets = scanner.nextInt();
                System.out.print("Количество повторений: ");
                int reps = scanner.nextInt();
                scanner.nextLine();
                user.addWorkout(new StrengthWorkout(duration, calories, sets, reps));
            }
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

    private static void showDataPoolMenu(Scanner scanner, User user1, User user2) {
        List<WorkoutPlan> pool = new ArrayList<>();
        pool.addAll(user1.getWorkouts());
        pool.addAll(user2.getWorkouts());

        System.out.println();
        System.out.println("1 - Показать все тренировки");
        System.out.println("2 - Фильтр по минимальным калориям");
        System.out.println("3 - Поиск по типу тренировки");
        System.out.println("4 - Сортировка по калориям");
        System.out.print("Ваш выбор: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            printPool(pool);
        } else if (choice == 2) {
            System.out.print("Минимум калорий: ");
            int min = scanner.nextInt();
            scanner.nextLine();
            printPool(filterByMinCalories(pool, min));
        } else if (choice == 3) {
            System.out.print("Тип (Cardio/Strength): ");
            String type = scanner.nextLine();
            printPool(searchByType(pool, type));
        } else if (choice == 4) {
            sortByCalories(pool);
            printPool(pool);
        } else {
            System.out.println("Некорректный выбор");
        }
    }

    private static void printPool(List<WorkoutPlan> pool) {
        if (pool.isEmpty()) {
            System.out.println("Ничего не найдено");
            return;
        }
        for (WorkoutPlan plan : pool) {
            System.out.println(plan);
        }
    }

    private static List<WorkoutPlan> filterByMinCalories(List<WorkoutPlan> pool, int min) {
        List<WorkoutPlan> result = new ArrayList<>();
        for (WorkoutPlan plan : pool) {
            if (plan.getCalories() >= min) {
                result.add(plan);
            }
        }
        return result;
    }

    private static List<WorkoutPlan> searchByType(List<WorkoutPlan> pool, String type) {
        List<WorkoutPlan> result = new ArrayList<>();
        for (WorkoutPlan plan : pool) {
            if (plan.getType().equalsIgnoreCase(type)) {
                result.add(plan);
            }
        }
        return result;
    }

    private static void sortByCalories(List<WorkoutPlan> pool) {
        for (int i = 0; i < pool.size() - 1; i++) {
            for (int j = 0; j < pool.size() - 1 - i; j++) {
                if (pool.get(j).getCalories() > pool.get(j + 1).getCalories()) {
                    WorkoutPlan temp = pool.get(j);
                    pool.set(j, pool.get(j + 1));
                    pool.set(j + 1, temp);
                }
            }
        }
    }
}