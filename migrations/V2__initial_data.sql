-- Migration: V2__initial_data.sql
-- Initial data for Cafeteria UTP

-- Insert default products
INSERT INTO productos (nombre, descripcion, precio, categoria, disponible, stock) VALUES
('Café Americano', 'Café negro tradicional', 5.00, 'BEBIDA', TRUE, 100),
('Café Latte', 'Café con leche espumosa', 6.50, 'BEBIDA', TRUE, 100),
('Cappuccino', 'Café italiano', 7.00, 'BEBIDA', TRUE, 80),
('Jugo de Naranja', 'Jugo natural', 5.50, 'BEBIDA', TRUE, 50),
('Sandwich de Pollo', 'Pan con pollo', 12.00, 'SANDWICH', TRUE, 40),
('Sandwich de Jamón', 'Pan con jamón', 10.00, 'SANDWICH', TRUE, 40),
('Burguer', 'Hamburguesa', 14.00, 'SANDWICH', TRUE, 35),
('Pie de Manzana', 'Pie con helado', 8.00, 'POSTRE', TRUE, 25),
('Brownie', 'Brownie de chocolate', 7.00, 'POSTRE', TRUE, 30),
('Papas Fritas', 'Papas crocantes', 6.00, 'ACOMPAÑAMIENTO', TRUE, 60)
ON CONFLICT DO NOTHING;

-- Insert default menu for today
INSERT INTO menus (fecha, horario, activo) 
VALUES (CURRENT_DATE, 'ALMUERZO', TRUE)
ON CONFLICT DO NOTHING;