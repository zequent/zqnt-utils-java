package com.zqnt.utils.notifications.domains;

import com.zqnt.utils.events.proto.NotificationEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO implements Serializable {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String modifiedFrom;

    private String assetSn;
    private String assetId;
    private NotificationEventType eventType;
    private String title;
    private String message;
    private Boolean read;
    private String capabilityExecutionId;
    private String capabilityNodeId;
    private String capabilityEventType;
    private String capabilityExecutionStatus;
    private String capabilityNodeStatus;
    private Float progress;
    private String externalExecutionId;
    private String commandId;
    private String commandExecutionStatus;
}
