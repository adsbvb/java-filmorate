DROP TABLE IF EXISTS film_directors;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS mpa_ratings CASCADE;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS review_likes_dislikes;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS mpa_ratings (
    mpa_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT,
    mpa_id INT,
    FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(mpa_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

CREATE TABLE IF NOT EXISTS directors (
    director_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    director_name   VARCHAR(225) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film_directors (
    film_id     BIGINT NOT NULL,
    director_id BIGINT NOT NULL,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (director_id) REFERENCES directors (director_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS film_likes (
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, film_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (film_id) REFERENCES films(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    useful INT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (film_id) REFERENCES films(id)
);

CREATE TABLE IF NOT EXISTS review_likes_dislikes (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
);