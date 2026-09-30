CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,

                             title VARCHAR(120) NOT NULL,
                             description VARCHAR(2000) NOT NULL,

                             price NUMERIC(15, 2) NOT NULL,
                             currency VARCHAR(3) NOT NULL,

                             square_meters NUMERIC(10, 2) NOT NULL,

                             address VARCHAR(255) NOT NULL,
                             latitude NUMERIC(9, 6),
                             longitude NUMERIC(9, 6),

                             available BOOLEAN NOT NULL DEFAULT TRUE,

                             created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             version BIGINT NOT NULL DEFAULT 0,

                             CONSTRAINT chk_department_price
                                 CHECK (price > 0),

                             CONSTRAINT chk_department_square_meters
                                 CHECK (square_meters > 0),

                             CONSTRAINT chk_department_currency
                                 CHECK (currency IN ('USD', 'ARS')),

                             CONSTRAINT chk_department_latitude
                                 CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),

                             CONSTRAINT chk_department_longitude
                                 CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);


CREATE TABLE images (
                        id BIGSERIAL PRIMARY KEY,

                        department_id BIGINT NOT NULL,

                        object_key VARCHAR(512) NOT NULL,
                        content_type VARCHAR(100) NOT NULL,

                        display_order INTEGER NOT NULL DEFAULT 0,
                        primary_image BOOLEAN NOT NULL DEFAULT FALSE,

                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_image_department
                            FOREIGN KEY (department_id)
                                REFERENCES departments(id)
                                ON DELETE CASCADE,

                        CONSTRAINT uq_image_object_key
                            UNIQUE (object_key),

                        CONSTRAINT chk_image_display_order
                            CHECK (display_order >= 0)
);


CREATE TABLE inquiries (
                           id BIGSERIAL PRIMARY KEY,

                           department_id BIGINT NOT NULL,

                           name VARCHAR(120) NOT NULL,
                           email VARCHAR(254) NOT NULL,
                           message VARCHAR(2000) NOT NULL,

                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_inquiry_department
                               FOREIGN KEY (department_id)
                                   REFERENCES departments(id)
                                   ON DELETE CASCADE
);


CREATE UNIQUE INDEX uq_primary_image_per_department
    ON images (department_id)
    WHERE primary_image = TRUE;


CREATE INDEX idx_department_available
    ON departments (available);

CREATE INDEX idx_department_price
    ON departments (price);

CREATE INDEX idx_department_square_meters
    ON departments (square_meters);

CREATE INDEX idx_image_department
    ON images (department_id);

CREATE INDEX idx_inquiry_department
    ON inquiries (department_id);