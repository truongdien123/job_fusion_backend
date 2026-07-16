package com.tma.job_fusion_backend.models;

import com.tma.job_fusion_backend.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private String description;

    @Column(name = "ip_address")
    private String ipAddress;
}
