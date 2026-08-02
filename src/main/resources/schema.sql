CREATE DATABASE IF NOT EXISTS echovault_db;
USE echovault_db;

-- 1. USERS TABLE (Includes Admin Role & Security Question Fields)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role ENUM('ROLE_ADMIN', 'ROLE_ACCOUNT_OWNER', 'ROLE_FAMILY_MEMBER') NOT NULL DEFAULT 'ROLE_ACCOUNT_OWNER',
    
    -- Inactivity Tracking
    last_login_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    inactivity_threshold_days INT DEFAULT 30,
    inactivity_alert_sent BOOLEAN DEFAULT FALSE,
    
    -- Emergency Unlock Security
    security_question VARCHAR(255),
    security_answer_hash VARCHAR(255),
    failed_unlock_attempts INT DEFAULT 0,
    locked_until DATETIME NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. FAMILY MEMBERS TABLE (Tiered Permissions)
CREATE TABLE family_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    member_user_id BIGINT NULL, -- Can link to registered user or stay guest email
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    permission_level ENUM('READ_ONLY', 'FULL_MEMORIAL_ACCESS', 'EMERGENCY_CONTACT') NOT NULL DEFAULT 'READ_ONLY',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (member_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 3. PERSONALITY PROFILE TABLE (Context for Gemini Ghost Engine)
CREATE TABLE personality_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    biography TEXT,
    core_values TEXT,
    favorite_phrases TEXT,
    special_instructions TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. VOICE NOTES TABLE (Cloudinary Audio + AssemblyAI Transcriptions)
CREATE TABLE voice_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    cloudinary_url VARCHAR(255) NOT NULL,
    cloudinary_public_id VARCHAR(100) NOT NULL,
    transcription LONGTEXT,
    transcription_status ENUM('PENDING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    tag ENUM('MOTIVATIONAL', 'LOVE', 'STORY', 'WARNING', 'FAITH', 'CELEBRATION') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. LETTERS TABLE (Time-Locked Email Delivery via SendGrid)
CREATE TABLE letters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_email VARCHAR(120) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body_content LONGTEXT NOT NULL,
    tag ENUM('MOTIVATIONAL', 'LOVE', 'STORY', 'WARNING', 'FAITH', 'CELEBRATION') NOT NULL,
    scheduled_delivery_at DATETIME NOT NULL,
    is_delivered BOOLEAN DEFAULT FALSE,
    delivered_at DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_scheduled_delivery (scheduled_delivery_at, is_delivered)
);

-- 6. PHOTOGRAPHS TABLE (Cloudinary Image Storage)
CREATE TABLE photographs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    cloudinary_public_id VARCHAR(100) NOT NULL,
    caption TEXT,
    tag ENUM('MOTIVATIONAL', 'LOVE', 'STORY', 'WARNING', 'FAITH', 'CELEBRATION') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. GHOST QUERIES TABLE (Auditing Gemini Ghost Engine Queries)
CREATE TABLE ghost_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vault_owner_id BIGINT NOT NULL,
    queried_by_id BIGINT NOT NULL,
    query_text TEXT NOT NULL,
    response_text LONGTEXT NOT NULL,
    sources_used TEXT, -- Stores JSON array of memory IDs used as ground truth
    queried_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vault_owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (queried_by_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 8. DELIVERY LOGS TABLE (Admin Monitoring & Audit Trail)
CREATE TABLE delivery_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    delivery_type ENUM('SENDGRID_EMAIL', 'TWILIO_SMS') NOT NULL,
    recipient VARCHAR(120) NOT NULL,
    trigger_reason VARCHAR(100) NOT NULL, -- e.g., "TIME_LOCKED_LETTER", "INACTIVITY_ALERT", "ANNIVERSARY"
    status ENUM('SUCCESS', 'FAILED', 'DEMO_TRIGGERED') NOT NULL,
    error_message TEXT NULL,
    delivered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. GHOST TRIGGERS TABLE (Recurring Anniversary/Birthday Triggers)
CREATE TABLE ghost_triggers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    trigger_type ENUM('ANNIVERSARY', 'BIRTHDAY', 'INACTIVITY_CHECK', 'SCHEDULED_LETTER') NOT NULL,
    payload_json TEXT, -- Contains custom SMS text or target emails
    next_run_at DATETIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_next_run (next_run_at, is_active)
);
