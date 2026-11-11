import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FitnessApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FitnessFacade facade = new FitnessFacade();

        System.out.println("Введите данные пользователя 1");
        User user1 = readUser(scanner, facade);
        readWorkouts(scanner, facade, user1);

        System.out.println("Введите данные пользователя 2");
        User user2 = readUser(scanner, facade);
        readWorkouts(scanner, facade, user2);

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
            System.out.println("5 - Работа с базой данных");
            System.out.println("6 - Пример конструктора тренировки");
            System.out.println("0 - Выход");
            int choice = readInt(scanner, "Ваш выбор: ");

            if (choice == 0) {
                running = false;
            } else if (choice == 1) {
                showMenu(scanner, facade, user1);
            } else if (choice == 2) {
                showMenu(scanner, facade, user2);
            } else if (choice == 3) {
                compareUsers(facade, user1, user2);
            } else if (choice == 4) {
                showDataPoolMenu(scanner, facade, user1, user2);
            } else if (choice == 5) {
                showDatabaseMenu(scanner, facade, user1, user2);
            } else if (choice == 6) {
                showPatternDemo(scanner, facade);
            } else {
                System.out.println("Некорректный выбор");
            }
        }

        System.out.println("Программа завершена");
        scanner.close();
    }

    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } else {
                System.out.println("Ошибка: введите целое число.");
                scanner.nextLine();
            }
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } else {
                System.out.println("Ошибка: введите число.");
                scanner.nextLine();
            }
        }
    }

    private static User readUser(Scanner scanner, FitnessFacade facade) {
        int age = readInt(scanner, "Возраст: ");
        double weight = readDouble(scanner, "Вес (кг): ");
        double height = readDouble(scanner, "Рост (см): ");
        System.out.print("Пол (M/F): ");
        String gender = scanner.nextLine();
        return facade.createUser(age, weight, height, gender);
    }

    private static void readWorkouts(Scanner scanner, FitnessFacade facade, User user) {
        int count = readInt(scanner, "Сколько тренировок добавить: ");

        for (int i = 0; i < count; i++) {
            System.out.println("1 - Cardio");
            System.out.println("2 - Strength");
            int typeChoice = readInt(scanner, "Тип тренировки: ");

            int duration = readInt(scanner, "Длительность (мин): ");
            int calories = readInt(scanner, "Сожжено калорий: ");

            if (typeChoice == 1) {
                double distance = readDouble(scanner, "Дистанция (км): ");
                facade.addWorkout(user, facade.createCardioWorkout(duration, calories, distance));
            } else {
                int sets = readInt(scanner, "Количество подходов: ");
                int reps = readInt(scanner, "Количество повторений: ");
                facade.addWorkout(user, facade.createStrengthWorkout(duration, calories, sets, reps));
            }
        }
    }

    private static void showMenu(Scanner scanner, FitnessFacade facade, User user) {
        System.out.println();
        System.out.println("1 - BMI");
        System.out.println("2 - BMR и дневная норма");
        System.out.println("3 - Список тренировок");
        int choice = readInt(scanner, "Ваш выбор: ");

        if (choice == 1) {
            System.out.println("BMI: " + facade.bmiCategory(user));
        } else if (choice == 2) {
            double bmr = facade.bmr(user);
            System.out.printf("BMR: %.2f kcal%n", bmr);
            String goal = readGoal(scanner);
            double dailyNorm = facade.dailyNorm(user, goal);
            System.out.printf("Дневная норма (%s): %.2f kcal%n", goal, dailyNorm);
        } else if (choice == 3) {
            for (WorkoutPlan plan : facade.userWorkouts(user)) {
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
        int choice = readInt(scanner, "Ваш выбор: ");

        if (choice == 1) return "lose";
        if (choice == 2) return "gain";
        return "maintain";
    }

    private static void compareUsers(FitnessFacade facade, User user1, User user2) {
        double bmr1 = facade.bmr(user1);
        double bmr2 = facade.bmr(user2);
        int calories1 = facade.totalCalories(user1);
        int calories2 = facade.totalCalories(user2);

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

    private static void showDataPoolMenu(Scanner scanner, FitnessFacade facade, User user1, User user2) {
        List<WorkoutPlan> pool = new ArrayList<>();
        pool.addAll(facade.userWorkouts(user1));
        pool.addAll(facade.userWorkouts(user2));

        System.out.println();
        System.out.println("1 - Показать все тренировки");
        System.out.println("2 - Фильтр по минимальным калориям");
        System.out.println("3 - Поиск по типу тренировки");
        System.out.println("4 - Сортировка по калориям");
        int choice = readInt(scanner, "Ваш выбор: ");

        if (choice == 1) {
            printPool(pool);
        } else if (choice == 2) {
            int min = readInt(scanner, "Минимум калорий: ");
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

    private static void showDatabaseMenu(Scanner scanner, FitnessFacade facade, User user1, User user2) {
        System.out.println();
        System.out.println("1 - Сохранить пользователя 1 и его тренировки в базу");
        System.out.println("2 - Сохранить пользователя 2 и его тренировки в базу");
        System.out.println("3 - Показать всех пользователей из базы");
        System.out.println("4 - Показать тренировки пользователя из базы по id");
        System.out.println("5 - Обновить вес пользователя в базе");
        System.out.println("6 - Удалить тренировку из базы по id");
        int choice = readInt(scanner, "Ваш выбор: ");

        if (choice == 1) {
            saveUserToDatabase(facade, user1);
        } else if (choice == 2) {
            saveUserToDatabase(facade, user2);
        } else if (choice == 3) {
            List<User> users = facade.legacyUsers();
            for (User user : users) {
                System.out.println(user);
            }
        } else if (choice == 4) {
            int userId = readInt(scanner, "Id пользователя: ");
            List<WorkoutPlan> plans = facade.legacyWorkouts(userId);
            printPool(plans);
        } else if (choice == 5) {
            int userId = readInt(scanner, "Id пользователя: ");
            double newWeight = readDouble(scanner, "Новый вес: ");
            facade.updateLegacyWeight(userId, newWeight);
        } else if (choice == 6) {
            int workoutId = readInt(scanner, "Id тренировки: ");
            facade.deleteLegacyWorkout(workoutId);
        } else {
            System.out.println("Некорректный выбор");
        }
    }

    private static void saveUserToDatabase(FitnessFacade facade, User user) {
        facade.saveLegacyUser(user);
        for (WorkoutPlan plan : facade.userWorkouts(user)) {
            plan.setUserId(user.getId());
            facade.saveLegacyWorkout(plan);
        }
    }

    private static void showPatternDemo(Scanner scanner, FitnessFacade facade) {
        int days = readInt(scanner, "Тренировок в неделю: ");
        System.out.println("Рекомендуемая программа: " + facade.recommend(days).get("recommendedId"));
        List<Object> exercises = new ArrayList<>();
        boolean adding = true;
        while (adding) {
            System.out.println("1 - Жим лёжа, 2 - Подтягивания, 0 - Готово");
            int choice = readInt(scanner, "Упражнение: ");
            if (choice == 0) {
                adding = false;
            } else if (choice == 1 || choice == 2) {
                int sets = readInt(scanner, "Подходов: ");
                int reps = readInt(scanner, "Повторений: ");
                int modifier = readInt(scanner, "Модификатор: 0 - нет, 1 - дропсет, 2 - суперсет, 3 - тяжёлый вес: ");
                java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("id", choice == 1 ? "bench" : "pullups");
                item.put("sets", sets);
                item.put("reps", reps);
                item.put("dropSet", modifier == 1);
                item.put("superSet", modifier == 2);
                item.put("heavyWeight", modifier == 3);
                exercises.add(item);
                java.util.Map<String, Object> request = new java.util.LinkedHashMap<>();
                request.put("exercises", exercises);
                try {
                    java.util.Map<String, Object> built = facade.build(request);
                    System.out.println("Нагрузка: " + built.get("load"));
                    System.out.println("Примерно сожжено: " + built.get("calories") + " ккал");
                } catch (IllegalArgumentException error) {
                    exercises.remove(exercises.size() - 1);
                    System.out.println(error.getMessage());
                }
            }
        }
    }
}
