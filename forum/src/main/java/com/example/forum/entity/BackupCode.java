package com.example.forum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;

@Entity
@Table(name = "backup_codes")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackupCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codeHash;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name ="user_id", nullable = false)
    private UserEntity userEntity;
}
