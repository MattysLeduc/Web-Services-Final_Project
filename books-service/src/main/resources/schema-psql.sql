DROP TABLE IF EXISTS books;

CREATE TABLE IF NOT EXISTS books(
    id SERIAL,
    book_id VARCHAR(36) UNIQUE,
    isbn VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    author_first_name VARCHAR(255),
    author_last_name VARCHAR(255),
    author_biography VARCHAR(255),
    genre_name VARCHAR(50),
    publication_date DATE,
    book_type VARCHAR(50),
    age_group VARCHAR(50),
    copies_available INT,
    PRIMARY KEY(id)
    );