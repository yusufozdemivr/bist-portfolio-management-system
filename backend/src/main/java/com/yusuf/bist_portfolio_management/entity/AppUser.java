package com.yusuf.bist_portfolio_management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Getter
@Setter
@ToString(exclude = {"tradingAccount"})
@EqualsAndHashCode(exclude = {"tradingAccount"})

public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name ="first_name", nullable = false, length= 50)
    private String firstName;

    @Column(name="last_name", nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique= true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToOne (mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TradingAccount tradingAccount;

}
