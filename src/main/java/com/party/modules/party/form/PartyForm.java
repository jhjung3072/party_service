package com.party.modules.party.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class PartyForm {

    public static final String VALID_PATH_PATTERN = "^(https:\\/\\/)(open\\.kakao\\.com)(\\/o\\/)[a-zA-Z0-9]{8}";

    @NotBlank
    @Pattern(regexp = VALID_PATH_PATTERN)
    private String kakaoLink;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @Min(1)
    @Max(4)
    private Integer limitOfEnrollments = 1;

}
