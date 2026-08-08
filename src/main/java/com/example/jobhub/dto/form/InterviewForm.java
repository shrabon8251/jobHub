package com.example.jobhub.dto.form;

import com.example.jobhub.validation.ValidInterviewSchedule;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidInterviewSchedule
public class InterviewForm {

    @NotNull(message = "Interview date is required.")
    @FutureOrPresent(message = "Interview date must be today or later.")
    private LocalDate interviewDate;

    @NotNull(message = "Interview time is required.")
    private LocalTime interviewTime;

    @Pattern(regexp = "^$|https?://[^\\s]+$", message = "Enter a valid http(s) meeting link.")
    @Size(max = 255, message = "Meeting link must be 255 characters or fewer.")
    private String meetingLink;

    @Size(max = 4000, message = "Interview notes must be 4,000 characters or fewer.")
    private String notes;
}
