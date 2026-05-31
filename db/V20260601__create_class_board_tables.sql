CREATE TABLE IF NOT EXISTS class_board_post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NULL,
    updated_at DATETIME NOT NULL,
    content TEXT NOT NULL,
    classroom_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_class_board_post_classroom FOREIGN KEY (classroom_id) REFERENCES classroom (id) ON DELETE CASCADE,
    CONSTRAINT fk_class_board_post_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS class_board_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME NULL,
    updated_at DATETIME NOT NULL,
    content TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_class_board_comment_post FOREIGN KEY (post_id) REFERENCES class_board_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_class_board_comment_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);