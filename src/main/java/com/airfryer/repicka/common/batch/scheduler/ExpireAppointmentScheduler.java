package com.airfryer.repicka.common.batch.scheduler;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpireAppointmentScheduler
{
    private final JobLauncher jobLauncher;
    private final Job expireAppointmentJob;

    // 매일 오전 4시에 실행
    @Scheduled(cron = "0 0 4 * * *")
    public void run()
    {
        try {
            jobLauncher.run(expireAppointmentJob, new JobParametersBuilder()
                    .addString("now", LocalDateTime.now().toString())
                    .toJobParameters());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
