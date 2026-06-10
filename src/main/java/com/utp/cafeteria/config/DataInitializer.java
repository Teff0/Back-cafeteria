package com.utp.cafeteria.config;

import com.utp.cafeteria.entity.Categoria;
import com.utp.cafeteria.entity.ConfiguracionCafeteria;
import com.utp.cafeteria.entity.Producto;
import com.utp.cafeteria.entity.Usuario;
import com.utp.cafeteria.repository.CategoriaRepository;
import com.utp.cafeteria.repository.ConfiguracionCafeteriaRepository;
import com.utp.cafeteria.repository.ProductoRepository;
import com.utp.cafeteria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ConfiguracionCafeteriaRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearAdminSiNoExiste();
        crearCajeraSiNoExiste();
        crearClienteSiNoExiste();
        crearConfiguracionSiNoExiste();
        log.info("DataInitializer completado.");
    }

    private void crearAdminSiNoExiste() {
        if (usuarioRepository.findByEmail("admin@utp.edu.pe").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .codigo("ADMIN001")
                    .email("admin@utp.edu.pe")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Administrador")
                    .apellidos("UTP Cafetería")
                    .cargo("Administrador del sistema")
                    .rol(Usuario.Rol.ADMIN)
                    .build());
            log.info("Usuario ADMIN creado: admin@utp.edu.pe / admin123");
        }
    }

    private void crearCajeraSiNoExiste() {
        if (usuarioRepository.findByEmail("caja@utp.edu.pe").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .codigo("CAJA001")
                    .email("caja@utp.edu.pe")
                    .password(passwordEncoder.encode("caja123"))
                    .nombre("Cajero")
                    .apellidos("UTP Cafetería")
                    .cargo("Cajero")
                    .rol(Usuario.Rol.CAJA)
                    .build());
            log.info("Usuario CAJA creado: caja@utp.edu.pe / caja123");
        }
    }

    private void crearClienteSiNoExiste() {
        if (usuarioRepository.findByEmail("cliente@utp.edu.pe").isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .codigo("CLI001")
                    .email("cliente@utp.edu.pe")
                    .password(passwordEncoder.encode("cliente123"))
                    .nombre("Cliente")
                    .apellidos("Demo")
                    .rol(Usuario.Rol.USUARIO)
                    .build());
            log.info("Usuario CLIENTE creado: cliente@utp.edu.pe / cliente123");
        }
    }

    private void crearConfiguracionSiNoExiste() {
        if (configuracionRepository.findAll().isEmpty()) {
            configuracionRepository.save(ConfiguracionCafeteria.builder()
                    .nombreCafeteria("Cafetería UTP")
                    .mensajeDelDia("¡Bienvenido a la Cafetería UTP!")
                    .horaApertura(LocalTime.of(7, 0))
                    .horaCierre(LocalTime.of(20, 0))
                    .telefono("01-123-4567")
                    .direccion("Campus UTP")
                    .build());
        }
    }

    private void crearCategoríasYProductosSiNoExisten() {
        if (categoriaRepository.count() > 0) return;

        // ── MENÚ ──────────────────────────────────────────────────────────
        Categoria menu = categoriaRepository.save(Categoria.builder()
                .nombre("Menú")
                .descripcion("Platos del día, guisos y segundos")
                .build());

        crearProducto("Arroz a la jardinera",
                "Arroz graneado acompañado de vegetales frescos. Una opción nutritiva y balanceada.",
                10.00, "img/Arroz con pollo - copia.png", menu, 15);

        crearProducto("Lomo saltado",
                "Lomo salteado al wok con cebolla y tomate, servido con papas fritas y arroz.",
                11.50, "img/Lomo Saltado.jpg", menu, 20);

        crearProducto("Estofado de pollo",
                "Pollo guisado con papas, zanahorias y arvejas. Entrada: huancaína.",
                10.00, "img/pollo-arvejado.jpg", menu, 15);

        crearProducto("Arroz con pollo",
                "Arroz al cilantro con presas de pollo, arvejas y zanahoria.",
                10.00, "img/Arroz con pollo2.jpg", menu, 15);

        crearProducto("Tallarines verdes",
                "Fideos en salsa de albahaca y espinaca, acompañados con milanesa de pollo.",
                11.00, "img/Tallarines verdes.jpg", menu, 20);

        crearProducto("Ají de gallina",
                "Pollo deshilachado en ají amarillo con pan y leche. Servido con arroz, papa y huevo.",
                10.00, "img/Ají de Gallina.jpg", menu, 15);

        // ── BEBIDAS ───────────────────────────────────────────────────────
        Categoria bebidas = categoriaRepository.save(Categoria.builder()
                .nombre("Bebidas")
                .descripcion("Jugos, gaseosas e infusiones")
                .build());

        crearProducto("Jugo de naranja",
                "Néctar natural de naranjas recién exprimidas, fresco y rico en vitamina C.",
                3.00, "img/Gemini_Generated_Image_nqy9lrnqy9lrnqy9.png", bebidas, 5);

        crearProducto("Inca Kola",
                "Bebida gaseosa de sabor original y refrescante.",
                2.50, "img/Inca kola.jpg", bebidas, 5);

        crearProducto("Chicha morada",
                "Bebida de maíz morado con piña, canela y limón. Dulce y refrescante.",
                3.00, "img/Chicha morada.jpg", bebidas, 5);

        crearProducto("Café",
                "Esencia de granos tostados con aroma revitalizante.",
                3.50, "img/descarga (2).jpg", bebidas, 5);

        crearProducto("Té",
                "Infusión aromática y reconfortante.",
                1.00, "img/té.jpg", bebidas, 5);

        crearProducto("Coca Cola",
                "La clásica bebida gaseosa, perfecta para acompañar tus comidas.",
                2.50, "img/descarga (3).jpg", bebidas, 5);

        // ── SNACKS ────────────────────────────────────────────────────────
        Categoria snacks = categoriaRepository.save(Categoria.builder()
                .nombre("Snacks")
                .descripcion("Aperitivos, galletas y bocaditos")
                .build());

        crearProducto("Choco soda",
                "Galleta de soda bañada en chocolate.",
                1.50, "img/choco soda - copia.jpg", snacks, 5);

        crearProducto("Galleta Ritz",
                "Galletas saladas y crocantes con toque de mantequilla.",
                1.80, "img/Galleta rits - copia.jpg", snacks, 5);

        crearProducto("Empanadas",
                "Masa horneada con rellenos de carne, pollo o queso.",
                2.50, "img/Empanada - copia.jpg", snacks, 10);

        crearProducto("Hamburguesa de carne",
                "Pieza de carne, lechuga y tomate en pan artesanal.",
                6.00, "img/Hamburguesa de carne - copia.jpg", snacks, 15);

        crearProducto("Hamburguesa de pollo",
                "Pieza de pollo, lechuga y tomate en pan artesanal.",
                5.00, "img/Hamburguesa de pollo - copia.jpg", snacks, 15);

        crearProducto("Alfajores",
                "Galletas de maicena rellenas con dulce de leche.",
                2.00, "img/Alfajores.jpg", snacks, 5);

        // ── FRUTAS ────────────────────────────────────────────────────────
        Categoria frutas = categoriaRepository.save(Categoria.builder()
                .nombre("Frutas")
                .descripcion("Frutas frescas de temporada")
                .build());

        crearProducto("Uvas",
                "Protegen el corazón, retrasan el envejecimiento celular y mejoran la digestión.",
                4.00, "img/fruite-item-5.jpg", frutas, 5);

        crearProducto("Frambuesas",
                "Fortalecen las defensas, aceleran el metabolismo y regulan el azúcar en sangre.",
                4.50, "img/fruite-item-2.jpg", frutas, 5);

        crearProducto("Duraznos",
                "Protegen la piel, mejoran la digestión y fortalecen el sistema inmunológico.",
                3.00, "img/fruite-item-4.jpg", frutas, 5);

        crearProducto("Plátano",
                "Aporta energía rápida, evita calambres y regula la presión arterial.",
                4.99, "img/fruite-item-3.jpg", frutas, 5);

        crearProducto("Naranjas",
                "Refuerzan el sistema inmunológico y mejoran la absorción de hierro.",
                3.50, "img/fruite-item-1.jpg", frutas, 5);

        crearProducto("Mandarinas",
                "Defienden al cuerpo contra virus, hidratan y mejoran la piel.",
                4.99, "img/Mandarina.jpg", frutas, 5);

        log.info("Categorías y productos iniciales creados.");
    }

    private void crearProducto(String nombre, String descripcion, double precio,
                                String imagenUrl, Categoria categoria, int tiempoPreparacion) {
        if (productoRepository.findByNombre(nombre).isEmpty()) {
            productoRepository.save(Producto.builder()
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .precio(BigDecimal.valueOf(precio))
                    .imagenUrl(imagenUrl)
                    .categoria(categoria)
                    .tiempoPreparacion(tiempoPreparacion)
                    .stock(50)
                    .build());
        }
    }
}
