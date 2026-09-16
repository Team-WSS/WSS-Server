CREATE TABLE novel_notification_subscription (
    novel_notification_subscription_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    novel_id BIGINT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME NOT NULL,
    modified_date DATETIME NOT NULL,
    PRIMARY KEY (novel_notification_subscription_id),
    CONSTRAINT uk_novel_notification_subscription_user_novel_type
        UNIQUE (user_id, novel_id, notification_type),
    CONSTRAINT fk_novel_notification_subscription_user
        FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_novel_notification_subscription_novel
        FOREIGN KEY (novel_id) REFERENCES novel (novel_id) ON DELETE CASCADE,
    INDEX idx_novel_notification_subscription_user_type_id
        (user_id, notification_type, novel_notification_subscription_id),
    INDEX idx_novel_notification_subscription_novel_type
        (novel_id, notification_type)
);

ALTER TABLE notification
    ADD COLUMN novel_id BIGINT NULL AFTER feed_id,
    ADD INDEX idx_notification_novel_id (novel_id),
    ADD CONSTRAINT fk_notification_novel
        FOREIGN KEY (novel_id) REFERENCES novel (novel_id) ON DELETE SET NULL;
