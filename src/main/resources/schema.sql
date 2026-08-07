DROP TABLE IF EXISTS ghost_queries CASCADE;
DROP TABLE IF EXISTS delivery_logs CASCADE;
DROP TABLE IF EXISTS voice_notes CASCADE;
DROP TABLE IF EXISTS letters CASCADE;
DROP TABLE IF EXISTS family_members CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone_number VARCHAR(50),
    inactivity_threshold_days INT DEFAULT 30,
    last_login_at TIMESTAMP,
    inactivity_alert_sent BOOLEAN DEFAULT FALSE,
    failed_unlock_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    security_answer_hash VARCHAR(255)
);

CREATE TABLE family_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    relationship VARCHAR(100),
    permission_level VARCHAR(50) DEFAULT 'VIEW',
    CONSTRAINT fk_family_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE letters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(255),
    recipient_email VARCHAR(255),
    subject VARCHAR(255),
    body_content TEXT,
    tag VARCHAR(100),
    is_delivered BOOLEAN DEFAULT FALSE,
    scheduled_delivery_at TIMESTAMP,
    delivered_at TIMESTAMP,
    CONSTRAINT fk_letters_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE voice_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    tag VARCHAR(100),
    file_path VARCHAR(500),
    transcription TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_voice_notes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE delivery_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    delivery_type VARCHAR(50),
    recipient VARCHAR(255),
    trigger_reason VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE ghost_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_owner_id BIGINT NOT NULL,
    queried_by_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ghost_queries_owner FOREIGN KEY (vault_owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ghost_queries_queried_by FOREIGN KEY (queried_by_id) REFERENCES users(id) ON DELETE CASCADE
);
