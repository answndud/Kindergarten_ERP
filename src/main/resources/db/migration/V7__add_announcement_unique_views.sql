CREATE TABLE announcement_view (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    viewer_id BIGINT NOT NULL,
    read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_announcement_view_announcement
        FOREIGN KEY (announcement_id) REFERENCES announcement(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_view_viewer
        FOREIGN KEY (viewer_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT uk_announcement_view_announcement_viewer
        UNIQUE (announcement_id, viewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_announcement_view_announcement ON announcement_view(announcement_id);
CREATE INDEX idx_announcement_view_viewer ON announcement_view(viewer_id);
