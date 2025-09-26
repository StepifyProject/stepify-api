package br.com.stepify.mongo.entity;

import br.com.stepify.enums.ETaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicroTask {
    private String id;
    private String title;
    private String description;
    private ETaskStatus status;
    private Integer order;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
