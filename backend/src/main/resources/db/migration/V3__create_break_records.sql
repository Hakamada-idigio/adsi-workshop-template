CREATE TABLE break_records (
    id BIGSERIAL PRIMARY KEY,
    attendance_record_id BIGINT NOT NULL,
    break_start TIMESTAMP NOT NULL,
    break_end TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_break_attendance FOREIGN KEY (attendance_record_id) REFERENCES attendance_records(id)
);

CREATE INDEX idx_break_attendance_id ON break_records(attendance_record_id);
