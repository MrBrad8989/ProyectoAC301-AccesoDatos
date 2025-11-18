package es.iesjuanbosco.utils;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utilidad para obtener un EntityManagerFactory compartido.
 */
public class JpaUtil {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("VentasUnidadPersistencia");

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
