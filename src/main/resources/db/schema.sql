/* ユーザマスタ */
CREATE TABLE user_m(
    user_id         CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    user_name       VARCHAR(50) NOT NULL ,
    user_address    VARCHAR(50) NOT NULL ,
    password        VARCHAR(255) NOT NULL ,
    user_role       VARCHAR(5) NOT NULL ,
    user_valid      BOOLEAN NOT NULL ,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    lasted_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* 面接テーブル */
CREATE TABLE interview_t(
    interview_id            CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                 CHAR(20) NOT NULL ,
    interview_expression    INT,
    interview_eyes          INT,
    interview_posture       INT,
    interview_voice         INT,
    interview_date          DATE,
    interview_score         INT,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* ESテーブル */
CREATE TABLE es_t(
    es_id                   CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                 CHAR(20) NOT NULL ,
    es_content_reason       VARCHAR(500),
    es_content_selfpr       VARCHAR(500),
    es_content_activities   VARCHAR(500),
    es_content_stwe         VARCHAR(500),
    es_occupation           VARCHAR(100),
    es_date                 DATE,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id)
);

/* ESテンプレテーブル */
CREATE TABLE es_template_t(
    es_template_id                  CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    es_template_content_reason      VARCHAR(500),
    es_template_content_selfpr      VARCHAR(500),
    es_template_content_activities  VARCHAR(500),
    es_template_content_stwe        VARCHAR(500),
    es_template_occupation          VARCHAR(100)
);

/* SPIテーブル */
CREATE TABLE spi_t(
    spi_id              CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    spi_content         VARCHAR(255),
    spi_answer1         VARCHAR(100),
    spi_answer2         VARCHAR(100),
    spi_answer3         VARCHAR(100),
    spi_answer4         VARCHAR(100),
    spi_correct_answer  INT,
    spi_category        VARCHAR(50)
);

/* SPI結果テーブル */
CREATE TABLE spi_result_t(
    spi_result_id               CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                     CHAR(20) NOT NULL ,
    spi_id                      CHAR(20) NOT NULL ,
    spi_user_answer             INT,
    spi_is_correct              BOOLEAN,
    spi_answered_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id),
    FOREIGN KEY (spi_id) REFERENCES spi_t(spi_id)
);

/* CAB/GABテーブル */
CREATE TABLE cabgab_t(
    cabgab_id               CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    cabgab_content          VARCHAR(255),
    cabgab_answer1          VARCHAR(100),
    cabgab_answer2          VARCHAR(100),
    cabgab_answer3          VARCHAR(100),
    cabgab_answer4          VARCHAR(100),
    cabgab_correct_answer   INT,
    cabgab_category         VARCHAR(50)
);

/* CAB/GAB結果テーブル */
CREATE TABLE cabgab_result_t(
    cabgab_result_id            CHAR(20) PRIMARY KEY NOT NULL UNIQUE ,
    user_id                     CHAR(20) NOT NULL ,
    cabgab_id                   CHAR(20) NOT NULL ,
    cabgab_user_answer          INT,
    cabgab_is_correct           BOOLEAN,
    cabgab_answered_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ,
    FOREIGN KEY (user_id) REFERENCES user_m(user_id),
    FOREIGN KEY (cabgab_id) REFERENCES cabgab_t(cabgab_id)
);