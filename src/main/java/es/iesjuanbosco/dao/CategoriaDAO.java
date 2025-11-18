package es.iesjuanbosco.dao;

import es.iesjuanbosco.modelo.Categoria;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    private EntityManager em;

    public CategoriaDAO(EntityManager em) {
        this.em = em;
    }

    public Optional<Categoria> obtenerPorId(Long id) {
        return Optional.ofNullable(em.find(Categoria.class, id));
    }

    public List<Categoria> obtenerTodasConProductos() {
        String jpql = "SELECT DISTINCT c FROM Categoria c " +
                "LEFT JOIN FETCH c.productos " +
                "ORDER BY c.nombre";

        return em.createQuery(jpql, Categoria.class)
                .getResultList();
    }

    public Optional<Categoria> obtenerPorIdConProductos(Long id) {
        String jpql = "SELECT c FROM Categoria c " +
                "LEFT JOIN FETCH c.productos " +
                "WHERE c.id = :id";

        List<Categoria> resultados = em.createQuery(jpql, Categoria.class)
                .setParameter("id", id)
                .getResultList();

        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }
}
