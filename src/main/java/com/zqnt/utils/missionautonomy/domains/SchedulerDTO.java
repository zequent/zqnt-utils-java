package com.zqnt.utils.missionautonomy.domains;

import com.zqnt.utils.mission.proto.SchedulerType;
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
    private String cronExpression;
    private Boolean active;
    private SchedulerType type;
    private String clientTimeZone;
    private String assetSn;
    private String commandId;
    private String capabilityPackageId;
    private String capabilityId;
    private String executionParametersJson;
    private Boolean autoStart;

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

        boolean simpleExecution = assetSn != null && !assetSn.isBlank()
                && commandId != null && !commandId.isBlank();
        boolean packageExecution = assetSn != null && !assetSn.isBlank()
                && capabilityPackageId != null && !capabilityPackageId.isBlank()
                && capabilityId != null && !capabilityId.isBlank();
        if (!simpleExecution && !packageExecution) {
            throw new IllegalArgumentException("Capability execution target must be specified");
        }
        if (simpleExecution && packageExecution) {
            throw new IllegalArgumentException("Scheduler cannot define command and package execution together");
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
