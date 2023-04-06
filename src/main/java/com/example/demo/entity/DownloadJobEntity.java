package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class DownloadJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String jobKey;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "jobdetail_id", referencedColumnName = "id")
    private JobDetailEntity jobDetailEntity;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credention_id", referencedColumnName = "id")
    private Credention credention;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "telegramCredention_id", referencedColumnName = "id")
    private TelegramCredention telegramCredention;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hiWatchSettings_id", referencedColumnName = "id")
    private HiWatchSettings hiWatchSettings;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ftpSettings_id", referencedColumnName = "id")
    private FtpSettings ftpSettings;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "downloadSettings_id", referencedColumnName = "id")
    private DownloadSettings downloadSettings;


}
