package com.aicode.reviewer.model.entity;

import com.aicode.reviewer.enums.FindingCategory;
import com.aicode.reviewer.enums.FindingSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review_findings")
public class ReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private PullRequestReview review;

    @Enumerated(EnumType.STRING)
    private FindingCategory category;

    @Enumerated(EnumType.STRING)
    private FindingSeverity severity;

    private String filePath;
    private Integer lineNumber;

    @Column(length = 2000)
    private String message;

    @Column(length = 2000)
    private String suggestion;
}
