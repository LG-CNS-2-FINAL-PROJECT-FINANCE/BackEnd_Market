package com.ddiring.backend_market.investment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_seq", nullable = false)
    private Integer userSeq;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 255)
    private String nickname;

    @Column(name = "role", nullable = false)
    private Byte role; // 0: ADMIN, 1: USER, 2: Creator

    @Column(name = "age", nullable = false)
    private Byte age;

    @Column(name = "gender")
    private Byte gender; // 0: Male, 1: Female

    @Column(name = "user_status")
    private Byte userStatus; // 0: ACTIVE, 1: DISABLED, 2: DELETED

    @Column(name = "lastest_at", nullable = false)
    private LocalDate lastestAt;

    @Column(name = "created_id", nullable = false)
    private Integer createdId;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_id", nullable = false)
    private Integer updatedId;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;
}
