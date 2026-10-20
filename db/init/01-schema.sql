-- =====================================================================
-- LogiTrack - shipment-service
-- Script inicial de base de datos (MySQL 8)
-- Se ejecuta automáticamente la PRIMERA vez que se crea el contenedor
-- de MySQL (volumen vacío). Para re-ejecutarlo: docker compose down -v
-- =====================================================================

CREATE DATABASE IF NOT EXISTS logitrack_shipments
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE logitrack_shipments;

-- ---------------------------------------------------------------------
-- Clientes
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id          CHAR(36)     NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    telefono    VARCHAR(30),
    version     BIGINT,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6),
    CONSTRAINT pk_clientes PRIMARY KEY (id),
    CONSTRAINT uk_clientes_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- Repartidores
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS repartidores (
    id             CHAR(36)     NOT NULL,
    nombre         VARCHAR(120) NOT NULL,
    tipo_vehiculo  VARCHAR(20)  NOT NULL,
    estado         VARCHAR(20)  NOT NULL,
    version        BIGINT,
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6),
    CONSTRAINT pk_repartidores PRIMARY KEY (id)
);

-- ---------------------------------------------------------------------
-- Envíos (origen y destino son Value Objects embebidos)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS envios (
    id                    CHAR(36)      NOT NULL,
    codigo_seguimiento    VARCHAR(20)   NOT NULL,
    cliente_id            CHAR(36)      NOT NULL,
    repartidor_id         CHAR(36),
    origen_calle          VARCHAR(120)  NOT NULL,
    origen_numero         VARCHAR(10)   NOT NULL,
    origen_ciudad         VARCHAR(80)   NOT NULL,
    origen_codigo_postal  VARCHAR(10)   NOT NULL,
    destino_calle         VARCHAR(120)  NOT NULL,
    destino_numero        VARCHAR(10)   NOT NULL,
    destino_ciudad        VARCHAR(80)   NOT NULL,
    destino_codigo_postal VARCHAR(10)   NOT NULL,
    peso_kg               DECIMAL(10,3) NOT NULL,
    alto_cm               DECIMAL(10,2) NOT NULL,
    ancho_cm              DECIMAL(10,2) NOT NULL,
    largo_cm              DECIMAL(10,2) NOT NULL,
    tarifa                DECIMAL(12,2) NOT NULL,
    estado                VARCHAR(20)   NOT NULL,
    version               BIGINT,
    created_at            DATETIME(6)   NOT NULL,
    updated_at            DATETIME(6),
    CONSTRAINT pk_envios PRIMARY KEY (id),
    CONSTRAINT uk_envios_codigo UNIQUE (codigo_seguimiento),
    CONSTRAINT fk_envios_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_envios_repartidor FOREIGN KEY (repartidor_id) REFERENCES repartidores (id)
);

CREATE INDEX idx_envios_estado ON envios (estado);

-- ---------------------------------------------------------------------
-- Historial de auditoría de cambios de estado
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historial_estados (
    id               CHAR(36)    NOT NULL,
    envio_id         CHAR(36)    NOT NULL,
    estado_anterior  VARCHAR(20),
    estado_nuevo     VARCHAR(20) NOT NULL,
    fecha_hora       DATETIME(6) NOT NULL,
    version          BIGINT,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6),
    CONSTRAINT pk_historial_estados PRIMARY KEY (id),
    CONSTRAINT fk_historial_envio FOREIGN KEY (envio_id) REFERENCES envios (id)
);

CREATE INDEX idx_historial_envio ON historial_estados (envio_id, fecha_hora);

-- ---------------------------------------------------------------------
-- Datos de prueba
-- ---------------------------------------------------------------------
INSERT INTO repartidores (id, nombre, tipo_vehiculo, estado, version, created_at, updated_at) VALUES
    ('3f6c1a52-8e0b-4d7a-9c11-0a1b2c3d4e01', 'Lucía Fernández', 'MOTO',       'ACTIVO',   0, NOW(6), NOW(6)),
    ('3f6c1a52-8e0b-4d7a-9c11-0a1b2c3d4e02', 'Martín Quispe',   'CAMIONETA',  'ACTIVO',   0, NOW(6), NOW(6)),
    ('3f6c1a52-8e0b-4d7a-9c11-0a1b2c3d4e03', 'Sofía Mamaní',    'BICICLETA',  'INACTIVO', 0, NOW(6), NOW(6));

INSERT INTO clientes (id, nombre, email, telefono, version, created_at, updated_at) VALUES
    ('7a2d9b10-1c3e-4f5a-8b6c-0d1e2f3a4b01', 'Tienda Norte SRL', 'ventas@tiendanorte.com', '+54 388 400-1000', 0, NOW(6), NOW(6));
