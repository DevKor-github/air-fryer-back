package com.airfryer.repicka.common.batch.configuration;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.repository.UpdateInProgressAppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 일주일 지난 PENDING 상태의 약속을 만료시키는 Spring Batch 설정

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ExpireAppointmentConfig
{
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AppointmentRepository appointmentRepository;
    private final UpdateInProgressAppointmentRepository updateInProgressAppointmentRepository;

    // 약속 만료 Job
    @Bean
    public Job expireAppointmentJob(Step expireAppointmentStep)
    {
        return new JobBuilder("expireAppointmentJob", jobRepository)
                .start(expireAppointmentStep)
                .build();
    }

    // 약속 만료 Step
    @Bean
    public Step expireAppointmentStep(ItemReader<Appointment> expireAppointmentReader,
                                      ItemWriter<Appointment> expireAppointmentWriter)
    {
        return new StepBuilder("expireAppointmentStep", jobRepository)
                .<Appointment, Appointment>chunk(100, transactionManager)
                .reader(expireAppointmentReader)
                .writer(expireAppointmentWriter)
                .build();
    }

    // 약속 만료 reader
    @Bean
    @StepScope
    public ItemReader<Appointment> expireAppointmentReader(@Value("#{jobParameters['now']}") String now)
    {
        // updatedAt 일시가 일주일 이전인 Appointment 조회
        return new RepositoryItemReaderBuilder<Appointment>()
                .repository(appointmentRepository)
                .methodName("findByStateAndUpdatedAtBefore")
                .arguments(List.of(AppointmentState.PENDING, LocalDateTime.parse(now).minusWeeks(1)))
                .pageSize(100)
                .sorts(Map.of("updatedAt", Sort.Direction.ASC))
                .name("expireAppointmentReader")
                .build();
    }

    // PENDING 약속 만료 writer
    @Bean
    public ItemWriter<Appointment> expireAppointmentWriter() {
        return appointments -> {

            /// 1. 약속 상태를 EXPIRED로 변경
            /// 2. 연관된 UpdateInProgressAppointment 데이터 삭제

            // 삭제할 UpdateInProgressAppointment 데이터
            List<UpdateInProgressAppointment> deleteList = new ArrayList<>();

            // 각 약속 데이터 순회
            for(Appointment appointment : appointments)
            {
                // 약속 만료
                appointment.expire();

                // 해당 약속에 대한 UpdateInProgressAppointment 데이터 삭제
                List<UpdateInProgressAppointment> data = updateInProgressAppointmentRepository.findByAppointmentId(appointment.getId());
                deleteList.addAll(data);
            }

            updateInProgressAppointmentRepository.deleteAll(deleteList);
            appointmentRepository.saveAll(appointments);

        };
    }
}
