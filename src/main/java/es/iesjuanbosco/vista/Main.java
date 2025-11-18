package es.iesjuanbosco.vista;

import es.iesjuanbosco.dao.CategoriaDAO;
import es.iesjuanbosco.dao.ClienteDAO;
import es.iesjuanbosco.dao.ProductoDAO;
import es.iesjuanbosco.dao.VentaDAO;
import es.iesjuanbosco.modelo.*;
import es.iesjuanbosco.service.VentaService;
import es.iesjuanbosco.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Usamos el EntityManagerFactory del proyecto
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

        Long ventaId;

        // 1) Crear cliente, productos y una venta en estado PENDIENTE
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            ClienteDAO clienteDAO = new ClienteDAO(em);

            Cliente cliente = new Cliente();
            cliente.setNombre("María");
            cliente.setApellidos("Martínez");
            cliente.setDni("12345678A");
            clienteDAO.guardar(cliente);

            Producto p1 = new Producto();
            p1.setCodigo("P001");
            p1.setNombre("Ratón USB");
            p1.setPrecio(15.0);
            p1.setStock(10);
            p1.setStockMinimo(2);
            em.persist(p1);

            Producto p2 = new Producto();
            p2.setCodigo("P002");
            p2.setNombre("Teclado mecánico");
            p2.setPrecio(45.0);
            p2.setStock(5);
            p2.setStockMinimo(1);
            em.persist(p2);

            Venta venta = new Venta();
            venta.setFecha(LocalDate.now());
            venta.setEstado("PENDIENTE");
            venta.setCliente(cliente);

            DetalleVenta d1 = new DetalleVenta();
            d1.setProducto(p1);
            d1.setCantidad(2);
            d1.setPrecioVenta(p1.getPrecio());
            venta.addDetalle(d1);

            DetalleVenta d2 = new DetalleVenta();
            d2.setProducto(p2);
            d2.setCantidad(1);
            d2.setPrecioVenta(p2.getPrecio());
            venta.addDetalle(d2);

            // Calcular total de la venta
            double total = venta.getDetalleVentas().stream()
                    .mapToDouble(d -> d.getCantidad() * d.getPrecioVenta())
                    .sum();
            venta.setTotal(total);

            // Persistimos la venta (cascade guardará los detalles)
            em.persist(venta);

            tx.commit();

            ventaId = venta.getId();

            System.out.println("Cliente creado id=" + cliente.getId());
            System.out.println("Producto 1 creado id=" + p1.getId() + " stock=" + p1.getStock());
            System.out.println("Producto 2 creado id=" + p2.getId() + " stock=" + p2.getStock());
            System.out.println("Venta creada id=" + ventaId + " total=" + venta.getTotal());

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return;
        } finally {
            if (em.isOpen()) em.close();
        }

        // 2) Confirmar la venta usando el servicio (controla transacción y stock)
        VentaService ventaService = new VentaService(emf);
        try {
            System.out.println("\n== Intentando confirmar la venta id=" + ventaId + " ==");
            ventaService.confirmarVenta(ventaId);
        } catch (Exception e) {
            System.err.println("Error al confirmar la venta: " + e.getMessage());
        }

        // 3) Consultas posteriores: mostrar stock de productos y total ventas del día
        EntityManager em2 = emf.createEntityManager();
        try {
            ProductoDAO productoDAO = new ProductoDAO(em2);
            List<Producto> productosConStockBajo = productoDAO.obtenerConStockBajo();

            System.out.println("\nProductos con stock bajo:");
            for (Producto p : productosConStockBajo) {
                System.out.println(" - " + p.getNombre() + " (id=" + p.getId() + ") stock=" + p.getStock() + " stockMinimo=" + p.getStockMinimo());
            }

            VentaDAO ventaDAO = new VentaDAO(em2);
            Double totalHoy = ventaDAO.calcularTotalVentasDelDia(LocalDate.now());
            System.out.println("\nTotal ventas de hoy: " + totalHoy);

            System.out.println("\nVentas del cliente (si existen):");
            List<Venta> ventasCliente = ventaDAO.obtenerPorCliente(1L); // asumimos cliente id 1; si no existe, lista vacía
            for (Venta v : ventasCliente) {
                System.out.println("Venta id=" + v.getId() + " estado=" + v.getEstado() + " total=" + v.getTotal());
                v.getDetalleVentas().forEach(d -> System.out.println("   Detalle -> producto: " + d.getProducto().getNombre() + " cantidad=" + d.getCantidad()));
            }

        } finally {
            if (em2.isOpen()) em2.close();
        }

        // 4) Generar un reporte detallado de ventas
        System.out.println("\n=== REPORTE DE VENTAS ===");
        VentaDAO ventaDAO2 = new VentaDAO(em2);
        List<Object[]> reporte = ventaDAO2.obtenerReporteVentas();

        Long ventaActual = null;
        double totalVenta = 0.0;

        for (Object[] fila : reporte) {
            Long vId = (Long) fila[0];

            if (ventaActual == null || !ventaActual.equals(vId)) {
                if (ventaActual != null) {
                    System.out.println("  TOTAL VENTA: " + totalVenta + "€\n");
                }

                ventaActual = vId;
                totalVenta = 0.0;

                System.out.println("VENTA #" + fila[0] + " | Fecha: " + fila[1] + " | Estado: " + fila[2]);
                System.out.println("Cliente: " + fila[4] + " " + fila[5]);
                System.out.println("Líneas:");
            }

            Integer cantidad = (Integer) fila[6];
            Double precioVenta = (Double) fila[7];
            String nombreProducto = (String) fila[8];
            String nombreCategoria = (String) fila[9];

            double importe = cantidad * precioVenta;
            totalVenta += importe;

            System.out.println("  - " + nombreProducto + " (" + nombreCategoria + ") x" + cantidad + " = " + importe + "€");
        }

        if (ventaActual != null) {
            System.out.println("  TOTAL VENTA: " + totalVenta + "€");
        }

        // 5) Demostración de JOIN FETCH en CategoriaDAO
        System.out.println("\n=== DEMOSTRACIÓN JOIN FETCH EN CATEGORIADAO ===");
        EntityManager em3 = emf.createEntityManager();
        try {
            CategoriaDAO categoriaDAO = new CategoriaDAO(em3);

            System.out.println("Cargando categorías con sus productos:");
            List<Categoria> categorias = categoriaDAO.obtenerTodasConProductos();

            for (Categoria cat : categorias) {
                System.out.println("\nCategoría: " + cat.getNombre() + " (id=" + cat.getId() + ")");
                System.out.println("  Productos:");
                for (Producto prod : cat.getProductos()) {
                    System.out.println("    - " + prod.getNombre() + " | Stock: " + prod.getStock());
                }
            }
        } finally {
            if (em3.isOpen()) em3.close();
        }

        // 4) Limpiamos recursos
        JpaUtil.close();

        System.out.println("\nFin del ejemplo.");
    }

}
