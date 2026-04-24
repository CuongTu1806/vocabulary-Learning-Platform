package com.example.learningVocabularyPlatform.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "assignment_attachment")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentAttachmentEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private AssignmentEntity assignment;

    /** Đường dẫn tương đối so với app.storage.root */
    @Column(name = "relative_path", nullable = false, length = 1024)
    private String relativePath;

    @Column(name = "original_filename", nullable = false, length = 512)
    private String originalFilename;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
}
