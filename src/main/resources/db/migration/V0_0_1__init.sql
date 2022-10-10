CREATE TABLE IF NOT EXISTS project
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY,
    color
    VARCHAR
(
    255
),
    index INTEGER NOT NULL,
    is_default BOOLEAN NOT NULL,
    is_enabled BOOLEAN NOT NULL,
    is_work BOOLEAN NOT NULL,
    name VARCHAR
(
    255
),
    PRIMARY KEY
(
    id
)
    );

CREATE TABLE IF NOT EXISTS settings
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY,
    default_background_color
    VARCHAR
(
    255
),
    default_font_color VARCHAR
(
    255
),
    display_projects_right BOOLEAN NOT NULL,
    hover_background_color VARCHAR
(
    255
),
    hover_font_color VARCHAR
(
    255
),
    task_bar_color VARCHAR
(
    255
),
    use_hotkey BOOLEAN NOT NULL,
    PRIMARY KEY
(
    id
)
    );

CREATE TABLE IF NOT EXISTS work
(
    id
    BIGINT
    GENERATED
    BY
    DEFAULT AS
    IDENTITY,
    creation_date
    DATE,
    end_time
    TIMESTAMP,
    notes
    CLOB,
    start_time
    TIMESTAMP,
    project_id
    BIGINT,
    PRIMARY
    KEY
(
    id
)
    );

ALTER TABLE work
    ADD CONSTRAINT IF NOT EXISTS fkf4jf1abybj7fc5w1nt686svxt FOREIGN KEY (project_id) REFERENCES
    project;