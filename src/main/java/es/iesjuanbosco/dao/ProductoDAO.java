package es.iesjuanbosco.dao;


import es.iesjuanbosco.modelo.Producto;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ProductoDAO {
    private EntityManager em;

    public ProductoDAO(EntityManager em) {
        this.em = em;
    }

    public List<Producto> obtenerConStockBajo() {

        String jpql = "SELECT p FROM Producto p WHERE p.stock < p.stockMinimo";

        return em.createQuery(jpql, Producto.class)
                .getResultList();
    }
}
