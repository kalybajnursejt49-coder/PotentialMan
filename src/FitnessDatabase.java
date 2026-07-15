import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FitnessDatabase {

    public static void saveUser(User user) {
        String sql = "INSERT INTO users (age, weight, height, gender) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, user.getAge());
            statement.setDouble(2, user.getWeight());
            statement.setDouble(3, user.getHeight());
            statement.setString(4, user.getGender());
            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            System.out.println("Пользователь сохранён в базу данных с id " + user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveWorkout(WorkoutPlan plan) {
        String sql = "INSERT INTO workout_plans (user_id, type, duration, calories, distance, sets, reps) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, plan.getUserId());
            statement.setString(2, plan.getType());
            statement.setInt(3, plan.getDuration());
            statement.setInt(4, plan.getCalories());

            if (plan instanceof CardioWorkout) {
                CardioWorkout cardio = (CardioWorkout) plan;
                statement.setDouble(5, cardio.getDistance());
                statement.setNull(6, java.sql.Types.INTEGER);
                statement.setNull(7, java.sql.Types.INTEGER);
            } else if (plan instanceof StrengthWorkout) {
                StrengthWorkout strength = (StrengthWorkout) plan;
                statement.setNull(5, java.sql.Types.DOUBLE);
                statement.setInt(6, strength.getSets());
                statement.setInt(7, strength.getReps());
            }

            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                plan.setId(keys.getInt(1));
            }
            System.out.println("Тренировка сохранена в базу данных с id " + plan.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getInt("age"),
                        resultSet.getDouble("weight"),
                        resultSet.getDouble("height"),
                        resultSet.getString("gender")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static List<WorkoutPlan> getWorkoutsByUserId(int userId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM workout_plans WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                int duration = resultSet.getInt("duration");
                int calories = resultSet.getInt("calories");
                WorkoutPlan plan;

                if (type.equalsIgnoreCase("Cardio")) {
                    double distance = resultSet.getDouble("distance");
                    plan = new CardioWorkout(duration, calories, distance);
                } else {
                    int sets = resultSet.getInt("sets");
                    int reps = resultSet.getInt("reps");
                    plan = new StrengthWorkout(duration, calories, sets, reps);
                }

                plan.setId(resultSet.getInt("id"));
                plan.setUserId(resultSet.getInt("user_id"));
                plans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return plans;
    }

    public static void updateUserWeight(int userId, double newWeight) {
        String sql = "UPDATE users SET weight = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newWeight);
            statement.setInt(2, userId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Вес пользователя с id " + userId + " обновлён на " + newWeight);
            } else {
                System.out.println("Пользователь с id " + userId + " не найден");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteWorkout(int workoutId) {
        String sql = "DELETE FROM workout_plans WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, workoutId);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("Тренировка с id " + workoutId + " удалена");
            } else {
                System.out.println("Тренировка с id " + workoutId + " не найдена");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}