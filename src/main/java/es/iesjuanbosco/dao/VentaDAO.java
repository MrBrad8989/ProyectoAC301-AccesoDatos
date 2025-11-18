package es.iesjuanbosco.dao;

// En una nueva clase VentaDAO

import es.iesjuanbosco.modelo.Venta;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class VentaDAO {

    private EntityManager em;

    public VentaDAO(EntityManager em) {
        this.em = em;
    }
    public Double calcularTotalVentasDelDia(LocalDate fecha) {
        String jpql = "SELECT COALESCE(SUM(v.total), 0.0) FROM Venta v WHERE v.fecha = :fecha";

        return em.createQuery(jpql, Double.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
    }

    public List<Venta> obtenerPorCliente(Long clienteId) {
        String jpql = "SELECT v FROM Venta v " +
                "JOIN FETCH v.detalleVentas " +
                "WHERE v.cliente.id = :clienteId";

        return em.createQuery(jpql, Venta.class)
                .setParameter("clienteId", clienteId)
                .getResultList();
    }

    // Obtener venta por ID
    public java.util.Optional<Venta> obtenerPorId(Long id) {
        Venta venta = em.find(Venta.class, id);
        return venta != null ? java.util.Optional.of(venta) : java.util.Optional.empty();
    }
    public List<Object[]> obtenerReporteVentas() {
        String jpql = "SELECT v.id, v.fecha, v.estado, v.total, " +
                "c.nombre, c.apellidos, " +
                "dv.cantidad, dv.precioVenta, " +
                "p.nombre, cat.nombre " +
                "FROM Venta v " +
                "JOIN v.cliente c " +
                "JOIN v.detalleVentas dv " +
                "JOIN dv.producto p " +
                "JOIN p.categoria cat " +
                "ORDER BY v.id, dv.id";

        return em.createQuery(jpql, Object[].class).getResultList();
    }
}
