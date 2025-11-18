package es.iesjuanbosco.dao;

import es.iesjuanbosco.modelo.Cliente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

public class ClienteDAO {

    private EntityManager em;

    public ClienteDAO(EntityManager em) {
        this.em = em;
    }

    public void guardar(Cliente cliente) {
        em.persist(cliente);
    }

    public Cliente actualizar(Cliente cliente) {
        return em.merge(cliente);
    }

    public void eliminar(Cliente cliente) {
        em.remove(cliente);
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return Optional.ofNullable(em.find(Cliente.class, id));
    }

    public Optional<Cliente> obtenerPorDni(String dni) {
        try {
            String jpql = "SELECT c FROM Cliente c WHERE c.dni = :dni";

            Cliente cliente = em.createQuery(jpql, Cliente.class)
                    .setParameter("dni", dni)
                    .getSingleResult();

            return Optional.of(cliente);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        String jpql = "SELECT c FROM Cliente c WHERE c.nombre LIKE CONCAT('%', :nombre, '%')";

        return em.createQuery(jpql, Cliente.class)
                .setParameter("nombre", nombre)
                .getResultList();
    }
}