package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.common.proto.SchedulerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link Scheduler}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchedulerDTO implements Serializable {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private String modifiedFrom;
    private String name;
    private UUID missionId;
    private UUID taskId;
    private String cronExpression;
    private Boolean active;
    private SchedulerType type;
    private String clientTimeZone;

    /**
     * Validates this scheduler configuration
     * @throws IllegalArgumentException if scheduler is invalid
     */
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("Scheduler type must be specified");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheduler name must be provided");
        }

        if (missionId == null && taskId == null) {
            throw new IllegalArgumentException("Mission or task ID must be specified");
        }

        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression must be provided");
        }

        // Validate cron expression format (basic check)
        String[] parts = cronExpression.trim().split("\\s+");
        if (parts.length < 5 || parts.length > 7) {
            throw new IllegalArgumentException("Invalid cron expression format: " + cronExpression);
        }

        if (active == null) {
            throw new IllegalArgumentException("Active status must be specified");
        }

        if (clientTimeZone == null || clientTimeZone.trim().isEmpty()) {
            throw new IllegalArgumentException("Client timezone must be provided");
        }
    }
}
