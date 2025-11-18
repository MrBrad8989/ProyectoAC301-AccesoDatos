package es.iesjuanbosco.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @NotNull(message = "El precio no puede ser nulo")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Column(name = "precio")
    private Double precio;

    @NotNull(message = "El stock no puede ser nulo")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock")
    private Integer stock;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    @ToString.Exclude
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalleVentas = new ArrayList<>();
}
