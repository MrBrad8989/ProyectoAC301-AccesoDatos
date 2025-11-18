-- Script para MariaDB / MySQL compatible
-- Crea la base de datos y tablas necesarias para el proyecto
-- Usuario/contraseña usados en persistence.xml: user=root, password=alumno

CREATE DATABASE IF NOT EXISTS `ventas_db`
  DEFAULT CHARACTER SET = `utf8mb4`
  DEFAULT COLLATE = `utf8mb4_unicode_ci`;

USE `ventas_db`;

-- Tabla de categorías
CREATE TABLE IF NOT EXISTS `categorias` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categoria_nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de clientes
CREATE TABLE IF NOT EXISTS `clientes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `apellidos` VARCHAR(150) NOT NULL,
  `dni` VARCHAR(9) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cliente_dni` (`dni`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de productos
CREATE TABLE IF NOT EXISTS `productos` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(100) NOT NULL,
  `nombre` VARCHAR(150) NOT NULL,
  `precio` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL,
  `stock_minimo` INT DEFAULT NULL,
  `categoria_id` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_producto_codigo` (`codigo`),
  KEY `idx_producto_categoria` (`categoria_id`),
  CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla de ventas
CREATE TABLE IF NOT EXISTS `ventas` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `fecha` DATE NOT NULL,
  `estado` VARCHAR(50) NOT NULL,
  `total` DECIMAL(10,2) DEFAULT NULL,
  `cliente_id` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_venta_cliente` (`cliente_id`),
  CONSTRAINT `fk_venta_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla detalle_ventas
CREATE TABLE IF NOT EXISTS `detalle_ventas` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `cantidad` INT DEFAULT NULL,
  `precio_venta` DECIMAL(10,2) DEFAULT NULL,
  `venta_id` BIGINT DEFAULT NULL,
  `producto_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_detalle_venta` (`venta_id`),
  KEY `idx_detalle_producto` (`producto_id`),
  CONSTRAINT `fk_detalle_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos de ejemplo mínimos
INSERT INTO `categorias` (`nombre`) VALUES ('Electrónica'), ('Hogar')
  ON DUPLICATE KEY UPDATE `nombre` = `nombre`;

INSERT INTO `clientes` (`nombre`, `apellidos`, `dni`) VALUES
  ('Juan', 'Pérez', '12345678A')
  ON DUPLICATE KEY UPDATE `dni` = `dni`;

INSERT INTO `productos` (`codigo`, `nombre`, `precio`, `stock`, `stock_minimo`, `categoria_id`) VALUES
  ('P001', 'Televisor 40"', 499.99, 10, 2, (SELECT id FROM `categorias` WHERE nombre='Electrónica' LIMIT 1)),
  ('P002', 'Aspiradora', 99.50, 20, 5, (SELECT id FROM `categorias` WHERE nombre='Hogar' LIMIT 1))
  ON DUPLICATE KEY UPDATE `codigo` = `codigo`;

-- Una venta de ejemplo con sus detalles
INSERT INTO `ventas` (`fecha`, `estado`, `total`, `cliente_id`) VALUES
  (CURRENT_DATE(), 'PENDIENTE', 599.49, (SELECT id FROM `clientes` WHERE dni='12345678A' LIMIT 1));

-- Obtener el id de la venta recién insertada (asumimos id=1 si la BD estaba vacía)
-- Insertar detalles relacionados
INSERT INTO `detalle_ventas` (`cantidad`, `precio_venta`, `venta_id`, `producto_id`) VALUES
  (1, 499.99, (SELECT id FROM `ventas` ORDER BY id DESC LIMIT 1), (SELECT id FROM `productos` WHERE codigo='P001' LIMIT 1)),
  (1, 99.50, (SELECT id FROM `ventas` ORDER BY id DESC LIMIT 1), (SELECT id FROM `productos` WHERE codigo='P002' LIMIT 1));

-- Actualizar el total por si no coincide
UPDATE `ventas` v
SET v.total = (
  SELECT IFNULL(SUM(d.cantidad * d.precio_venta), 0) FROM `detalle_ventas` d WHERE d.venta_id = v.id
)
WHERE v.id = (SELECT id FROM `ventas` ORDER BY id DESC LIMIT 1);

-- Opcional: crear usuario específico (descomentar y ajustar si quieres crear un usuario local)
-- CREATE USER 'alumno'@'localhost' IDENTIFIED BY 'alumno';
-- GRANT ALL PRIVILEGES ON `ventas_db`.* TO 'alumno'@'localhost';
-- FLUSH PRIVILEGES;

-- Fin del script

