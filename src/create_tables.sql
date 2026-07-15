DROP TABLE IF EXISTS workout_plans;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       age INT NOT NULL,
                       weight DOUBLE PRECISION NOT NULL,
                       height DOUBLE PRECISION NOT NULL,
                       gender VARCHAR(10) NOT NULL
);

CREATE TABLE workout_plans (
                               id SERIAL PRIMARY KEY,
                               user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               type VARCHAR(20) NOT NULL,
                               duration INT NOT NULL,
                               calories INT NOT NULL,
                               distance DOUBLE PRECISION,
                               sets INT,
                               reps INT
);