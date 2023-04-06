package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class JobDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,mappedBy = "jobDetailEntity")
    @JoinColumn(name="job_id")
    private DownloadJobEntity downloadJobEntity;


    private String alias;
    private String note;

    @Enumerated(EnumType.STRING)
    private JobStatus jobStatus;

    @Enumerated(EnumType.STRING)
    private ClientType type;

    public JobDetailEntity(String alias, String note, ClientType type) {
        this.alias = alias;
        this.note = note;
        this.type = type;
    }
}
