package org.example.footgamers.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.footgamers.entities.enums.Rol;

@Entity(name = "usuario")
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uq_usuario_provider",
        columnNames = {"provider", "provider_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    // Los usuarios que entran por OAuth (Google/Discord) no tienen contrasena.
    @Column(nullable = true)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    // Ruta relativa o clave del objeto, nunca una ruta absoluta del sistema de
    // archivos. Cambiar de almacenamiento despues no debe romper la API.
    @Column(name = "imagen", length = 500)
    private String imagen;

    // Proveedor de identidad: "local" (registro normal), "google" o "discord".
    @Column(name = "provider", length = 20)
    private String provider = "local";

    // Identificador del usuario en el proveedor OAuth ("sub" de Google, "id" de
    // Discord). Es null para las cuentas locales.
    @Column(name = "provider_id")
    private String providerId;

    // URL remota del avatar que devuelve el proveedor OAuth.
    @Column(name = "avatar_url", columnDefinition = "text")
    private String avatarUrl;
}