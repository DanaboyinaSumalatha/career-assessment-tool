package com.careerpath.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private String text;
    private String type;        // PERSONALITY | SKILLS | INTEREST
    private String category;
    private List<String> options;
    private Integer orderIndex;
    private boolean active;
}
