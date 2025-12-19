package com.upiiz.ligas_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "entrenadores")
public class Entrenador {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String nombre;

    @Column(length = 120)
    private String email;

    @Column(length = 30)
    private String telefono;
}
