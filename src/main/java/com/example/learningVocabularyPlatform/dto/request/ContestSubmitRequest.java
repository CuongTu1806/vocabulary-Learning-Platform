package com.example.learningVocabularyPlatform.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestSubmitRequest {

    @NotEmpty(message = "Danh sách đáp án không được rỗng")
    @Valid
    private List<ContestAnswerItem> answers;
}
