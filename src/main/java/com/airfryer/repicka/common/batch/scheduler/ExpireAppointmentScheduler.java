package com.airfryer.repicka.common.batch.scheduler;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ExpireAppointmentScheduler
{
    private final JobLauncher jobLauncher;
    private final Job expireAppointmentJob;

    // 매일 오전 4시에 실행
    @Scheduled(cron = "* * 4 * * *")
    public void run()
    {
        try {
            jobLauncher.run(expireAppointmentJob, new JobParametersBuilder()
                    .addString("now", LocalDateTime.now().toString())
                    .toJobParameters());
        } catch (Exception e) {
            throw new CustomException(CustomExceptionCode.SPRING_SCHEDULER_ERROR, e.getMessage());
        }
    }
}
