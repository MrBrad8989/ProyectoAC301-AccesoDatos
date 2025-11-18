package es.iesjuanbosco.service;

import es.iesjuanbosco.dao.ProductoDAO;
import es.iesjuanbosco.dao.VentaDAO;
import es.iesjuanbosco.modelo.DetalleVenta;
import es.iesjuanbosco.modelo.Producto;
import es.iesjuanbosco.modelo.Venta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

// Excepción personalizada para problemas de stock
class StockException extends RuntimeException {
    public StockException(String message) {
        super(message);
    }
}

public class VentaService {

    // El Servicio utiliza los DAOs
    private VentaDAO ventaDAO;
    private ProductoDAO productoDAO;
    private EntityManagerFactory emf;

    public VentaService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void confirmarVenta(Long ventaId) {
        EntityManager em = emf.createEntityManager();

        this.ventaDAO = new VentaDAO(em);
        this.productoDAO = new ProductoDAO(em);

        // 1. Inicio de la Transacción
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        System.out.println("INICIO DE TRANSACCIÓN: Confirmando venta ID: " + ventaId);

        try {

            Venta venta = ventaDAO.obtenerPorId(ventaId)
                    .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

            if (!"PENDIENTE".equals(venta.getEstado())) {
                throw new RuntimeException("La venta ya no está pendiente");
            }

            for (DetalleVenta detalle : venta.getDetalleVentas()) {
                Producto producto = detalle.getProducto();
                int cantidadPedida = detalle.getCantidad();

                System.out.println("... Verificando stock para: " + producto.getNombre());

                if (producto.getStock() < cantidadPedida) {
                    throw new StockException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock: " + producto.getStock() +
                            ", Pedido: " + cantidadPedida);
                }

                producto.setStock(producto.getStock() - cantidadPedida);
            }

            venta.setEstado("CONFIRMADA");

            tx.commit();
            System.out.println("COMMIT REALIZADO: Venta " + ventaId + " confirmada.");

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("ROLLBACK REALIZADO: " + e.getMessage());
            throw new RuntimeException("Fallo en la transacción: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
    public void eliminarVenta(Long ventaId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {

            Venta venta = em.find(Venta.class, ventaId);
            if (venta != null) {
                em.remove(venta);
            }
            tx.commit();
            System.out.println("Venta " + ventaId + " y sus detalles eliminados.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Error al eliminar venta: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }
    public void eliminarLineaDetalleVenta(Long ventaId, Long detalleId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            Venta venta = em.find(Venta.class, ventaId);
            if (venta != null) {
                DetalleVenta detalleAEliminar = venta.getDetalleVentas().stream()
                        .filter(d -> d.getId().equals(detalleId))
                        .findFirst()
                        .orElse(null);

                if (detalleAEliminar != null) {
                    venta.removeDetalle(detalleAEliminar);
                }
            }
            tx.commit();
            System.out.println("Línea de detalle eliminada (orphanRemoval activo).");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
