package org.pt.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String userId;
    private String description;
}
