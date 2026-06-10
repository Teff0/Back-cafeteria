-- ============================================================
--  Cafeteria UTP - Esquema MySQL (JDBC puro, sin ORM)
--  Se ejecuta automaticamente al arrancar (spring.sql.init.mode=always).
--  La base de datos `cafeteria` debe existir previamente:
--      CREATE DATABASE cafeteria CHARACTER SET utf8mb4;
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id          CHAR(36)     PRIMARY KEY,
    codigo      VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nombre      VARCHAR(255) NOT NULL,
    rol         VARCHAR(20)  NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME,
    updated_at  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categorias (
    id          CHAR(36)     PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME,
    updated_at  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subcategorias (
    id           CHAR(36)     PRIMARY KEY,
    categoria_id CHAR(36)     NOT NULL,
    nombre       VARCHAR(255) NOT NULL,
    descripcion  TEXT,
    created_at   DATETIME,
    updated_at   DATETIME,
    CONSTRAINT fk_subcat_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS productos (
    id              CHAR(36)      PRIMARY KEY,
    nombre          VARCHAR(255)  NOT NULL,
    descripcion     TEXT,
    precio          DECIMAL(10,2) NOT NULL,
    categoria_id    CHAR(36)      NOT NULL,
    subcategoria_id CHAR(36),
    disponible      BOOLEAN       NOT NULL DEFAULT TRUE,
    imagen_url      VARCHAR(1000),
    stock           INT           NOT NULL DEFAULT 100,
    created_at      DATETIME,
    updated_at      DATETIME,
    CONSTRAINT fk_prod_categoria    FOREIGN KEY (categoria_id)    REFERENCES categorias(id),
    CONSTRAINT fk_prod_subcategoria FOREIGN KEY (subcategoria_id) REFERENCES subcategorias(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS menus (
    id          CHAR(36)    PRIMARY KEY,
    fecha       DATE        NOT NULL,
    horario     VARCHAR(20) NOT NULL,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  DATETIME,
    updated_at  DATETIME,
    UNIQUE KEY uk_menu_fecha_horario (fecha, horario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS menu_productos (
    menu_id     CHAR(36) NOT NULL,
    producto_id CHAR(36) NOT NULL,
    PRIMARY KEY (menu_id, producto_id),
    CONSTRAINT fk_mp_menu     FOREIGN KEY (menu_id)     REFERENCES menus(id)     ON DELETE CASCADE,
    CONSTRAINT fk_mp_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pedidos (
    id              CHAR(36)      PRIMARY KEY,
    usuario_id      CHAR(36)      NOT NULL,
    menu_id         CHAR(36),
    estado          VARCHAR(20)   NOT NULL,
    hora_programada TIME,
    total           DECIMAL(10,2) NOT NULL DEFAULT 0,
    observaciones   TEXT,
    voucher_url     TEXT,
    created_at      DATETIME,
    updated_at      DATETIME,
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES users(id),
    CONSTRAINT fk_pedido_menu    FOREIGN KEY (menu_id)    REFERENCES menus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS items_pedido (
    id              CHAR(36)      PRIMARY KEY,
    pedido_id       CHAR(36)      NOT NULL,
    producto_id     CHAR(36)      NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    created_at      DATETIME,
    CONSTRAINT fk_item_pedido   FOREIGN KEY (pedido_id)   REFERENCES pedidos(id)   ON DELETE CASCADE,
    CONSTRAINT fk_item_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pagos (
    id                 CHAR(36)      PRIMARY KEY,
    pedido_id          CHAR(36)      NOT NULL UNIQUE,
    monto              DECIMAL(10,2) NOT NULL,
    metodo_pago        VARCHAR(20)   NOT NULL,
    estado             VARCHAR(20)   NOT NULL,
    codigo_transaccion VARCHAR(100),
    fecha_pago         DATETIME,
    created_at         DATETIME,
    updated_at         DATETIME,
    CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS carrito_items (
    id          CHAR(36) PRIMARY KEY,
    usuario_id  CHAR(36) NOT NULL,
    producto_id CHAR(36) NOT NULL,
    cantidad    INT      NOT NULL,
    CONSTRAINT fk_cart_usuario  FOREIGN KEY (usuario_id)  REFERENCES users(id)     ON DELETE CASCADE,
    CONSTRAINT fk_cart_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    UNIQUE KEY uk_cart_usuario_producto (usuario_id, producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Categorias (idempotente) ──
INSERT IGNORE INTO categorias (id, nombre, descripcion, activo, created_at, updated_at) VALUES
 ('c1000000-0000-0000-0000-000000000001', 'Menú',    'Menú del día de la cafetería', TRUE, NOW(), NOW()),
 ('c1000000-0000-0000-0000-000000000002', 'Bebidas', 'Bebidas frías y calientes',    TRUE, NOW(), NOW()),
 ('c1000000-0000-0000-0000-000000000003', 'Frutas',  'Frutas y ensaladas frescas',   TRUE, NOW(), NOW()),
 ('c1000000-0000-0000-0000-000000000004', 'Snacks',  'Snacks y piqueos',             TRUE, NOW(), NOW());

-- ── Subcategorias del Menú (idempotente) ──
INSERT IGNORE INTO subcategorias (id, categoria_id, nombre, descripcion, created_at, updated_at) VALUES
 ('51000000-0000-0000-0000-000000000001', 'c1000000-0000-0000-0000-000000000001', 'Desayuno', '', NOW(), NOW()),
 ('51000000-0000-0000-0000-000000000002', 'c1000000-0000-0000-0000-000000000001', 'Almuerzo', '', NOW(), NOW()),
 ('51000000-0000-0000-0000-000000000003', 'c1000000-0000-0000-0000-000000000001', 'Cena',     '', NOW(), NOW());

-- ── Productos de ejemplo (idempotente): 3+ por categoria ──
INSERT IGNORE INTO productos (id, nombre, descripcion, precio, categoria_id, subcategoria_id, disponible, stock, created_at, updated_at) VALUES
 -- Bebidas
 ('b0000001-0000-0000-0000-000000000001', 'Café Americano',     'Café negro recién preparado',     4.50, 'c1000000-0000-0000-0000-000000000002', NULL, TRUE, 100, NOW(), NOW()),
 ('b0000002-0000-0000-0000-000000000002', 'Jugo de Naranja',    'Jugo natural recién exprimido',   5.00, 'c1000000-0000-0000-0000-000000000002', NULL, TRUE,  80, NOW(), NOW()),
 ('b0000003-0000-0000-0000-000000000003', 'Chocolate Caliente', 'Chocolate cremoso con leche',     6.00, 'c1000000-0000-0000-0000-000000000002', NULL, TRUE,  60, NOW(), NOW()),
 -- Frutas
 ('f0000001-0000-0000-0000-000000000001', 'Fruta Picada',       'Porción de fruta fresca',         4.00, 'c1000000-0000-0000-0000-000000000003', NULL, TRUE,  50, NOW(), NOW()),
 ('f0000002-0000-0000-0000-000000000002', 'Ensalada de Frutas', 'Mix de frutas de estación',       6.50, 'c1000000-0000-0000-0000-000000000003', NULL, TRUE,  40, NOW(), NOW()),
 ('f0000003-0000-0000-0000-000000000003', 'Manzana Verde',      'Manzana fresca',                  2.00, 'c1000000-0000-0000-0000-000000000003', NULL, TRUE,  90, NOW(), NOW()),
 -- Snacks
 ('50000001-0000-0000-0000-000000000001', 'Papas Fritas',       'Porción crocante de papas',       5.50, 'c1000000-0000-0000-0000-000000000004', NULL, TRUE,  90, NOW(), NOW()),
 ('50000002-0000-0000-0000-000000000002', 'Snack Mix',          'Mezcla de frutos secos',          4.50, 'c1000000-0000-0000-0000-000000000004', NULL, TRUE,  60, NOW(), NOW()),
 ('50000003-0000-0000-0000-000000000003', 'Galletas de Avena',  'Pack de 3 galletas artesanales',  4.00, 'c1000000-0000-0000-0000-000000000004', NULL, TRUE,  70, NOW(), NOW()),
 -- Menú / Desayuno
 ('40000001-0000-0000-0000-000000000001', 'Pan con Mantequilla','Pan tostado con mantequilla',     3.00, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000001', TRUE, 100, NOW(), NOW()),
 ('40000002-0000-0000-0000-000000000002', 'Sándwich Mixto',     'Jamón, queso y huevo',            8.50, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000001', TRUE,  50, NOW(), NOW()),
 -- Menú / Almuerzo
 ('40000003-0000-0000-0000-000000000003', 'Sándwich de Pollo',  'Pan artesanal con pollo',         9.90, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000002', TRUE,  50, NOW(), NOW()),
 ('40000004-0000-0000-0000-000000000004', 'Ensalada Fresca',    'Mix de vegetales de estación',    6.00, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000002', TRUE,  40, NOW(), NOW()),
 -- Menú / Cena
 ('40000005-0000-0000-0000-000000000005', 'Sándwich Veggie',    'Vegetales frescos y palta',       8.00, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000003', TRUE,  40, NOW(), NOW()),
 ('40000006-0000-0000-0000-000000000006', 'Sopa del Día',       'Sopa casera de la casa',          7.50, 'c1000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000003', TRUE,  30, NOW(), NOW());
