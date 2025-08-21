package com.airfryer.repicka.common.batch.scheduler;

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
public class AppointmentScheduler
{
    private final JobLauncher jobLauncher;
    private final Job expireAppointmentJob;
    private final Job successAppointmentJob;

    // 매일 오전 4시에 실행 - 만료 배치
    @Scheduled(cron = "0 0 4 * * *")
    public void runExpireBatch()
    {
        try {
            jobLauncher.run(expireAppointmentJob, new JobParametersBuilder()
                    .addString("now", LocalDateTime.now().toString())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("ExpireAppointmentJob 실행 중 오류 발생: {}", e.getMessage());
        }
    }

    // 매일 12시에 실행 - 성공 처리 배치
    @Scheduled(cron = "0 0 0 * * *")
    public void runSuccessBatch()
    {
        try {
            jobLauncher.run(successAppointmentJob, new JobParametersBuilder()
                    .addString("now", LocalDateTime.now().toString())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("SuccessAppointmentJob 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}
