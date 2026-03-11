package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "server_leaderboard")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

public class ServerLeaderboardEntity extends BaseEntity {
    @Column(name = "rating")
    private int rating;

    @Column(name = "rank_position")
    private int rankPosition;

    @Column(name = "contest_count")
    private int contestCount;

    // user 1 - 1 leaderboard
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
