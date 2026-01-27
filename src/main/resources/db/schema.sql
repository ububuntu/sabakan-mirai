/* ユーザマスタ */
CREATE TABLE user_m(
    user_id         CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    user_name       VARCHAR(50) NOT NULL ,
    user_address    VARCHAR(100) NOT NULL ,
    password        VARCHAR(255) NOT NULL ,
    user_role       VARCHAR(5) NOT NULL ,
    user_valid      BOOLEAN NOT NULL ,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    lasted_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* 目標テーブル */
CREATE TABLE goal_m(
    goal_id         CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    user_id         CHAR(37) NOT NULL ,
    goal_content    VARCHAR(255) NOT NULL ,
    goal_date       DATE NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* 面接テーブル */
CREATE TABLE interview_t(
    interview_id            CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                 CHAR(37) NOT NULL ,
    interview_expression    INT,
    interview_eyes          INT,
    interview_posture       INT,
    interview_voice         INT,
    interview_date          DATE,
    interview_score         INT,
    interviewComment        VARCHAR(500) NOT NULL ,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* ESテーブル */
CREATE TABLE es_t(
    es_id                   CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                 CHAR(37) NOT NULL ,
    es_content_reason       VARCHAR(500),
    es_content_selfpr       VARCHAR(500),
    es_content_activities   VARCHAR(500),
    es_content_stwe         VARCHAR(500),
    es_occupation           VARCHAR(100),
    es_date                 DATE,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* SPIテーブル */
CREATE TABLE spi_m(
    spi_id              CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    spi_content         VARCHAR(255),
    spi_answer1         VARCHAR(100),
    spi_answer2         VARCHAR(100),
    spi_answer3         VARCHAR(100),
    spi_answer4         VARCHAR(100),
    spi_correct_answer  INT,
    spi_category        VARCHAR(50)
);

/* SPI結果テーブル */
CREATE TABLE spi_history_t(
    spi_hs_id             CHAR(37) PRIMARY KEY NOT NULL UNIQUE,
    user_id             CHAR(37) NOT NULL,
    total_questions     INT NOT NULL,
    correct_count       INT NOT NULL,
    accuracy_rate       DECIMAL(5, 2),
    spi_hs_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_finished         BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* SPI回答テーブル */
CREATE TABLE spi_detail_t(
    spi_dl_id      CHAR(37) PRIMARY KEY NOT NULL UNIQUE,
    spi_hs_id             CHAR(37) NOT NULL,
    spi_id              CHAR(37) NOT NULL,
    user_answer         INT,
    is_correct          BOOLEAN NOT NULL,
    FOREIGN KEY (spi_hs_id) REFERENCES spi_history_t(spi_hs_id),
    FOREIGN KEY (spi_id) REFERENCES spi_m(spi_id)
);

/* CAB/GABテーブル */
CREATE TABLE cabgab_m(
    cabgab_id               CHAR(37) PRIMARY KEY NOT NULL UNIQUE ,
    cabgab_content          VARCHAR(255),
    cabgab_answer1          VARCHAR(100),
    cabgab_answer2          VARCHAR(100),
    cabgab_answer3          VARCHAR(100),
    cabgab_answer4          VARCHAR(100),
    cabgab_correct_answer   CHAR(1) DEFAULT NULL ,
    cabgab_category         VARCHAR(50)
);

/* CAB/GAB結果テーブル */
CREATE TABLE cabgab_history_t(
     cabgab_hs_id             CHAR(37) PRIMARY KEY NOT NULL UNIQUE,
     user_id             CHAR(37) NOT NULL,
     total_questions     INT NOT NULL,
     correct_count       INT NOT NULL,
     accuracy_rate       DECIMAL(5, 2),
     cabgab_hs_date           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     is_finished         BOOLEAN DEFAULT FALSE,
     FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* CAB/GAB回答テーブル */
CREATE TABLE cabgab_detail_t(
    cabgab_dl_id      CHAR(37) PRIMARY KEY NOT NULL UNIQUE,
    cabgab_hs_id             CHAR(37) NOT NULL,
    cabgab_id              CHAR(37) NOT NULL,
    user_answer         INT,
    is_correct          BOOLEAN NOT NULL,
    FOREIGN KEY (cabgab_hs_id) REFERENCES cabgab_history_t(cabgab_hs_id),
    FOREIGN KEY (cabgab_id) REFERENCES cabgab_m(cabgab_id)
);