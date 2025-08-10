package com.airfryer.repicka.domain.appointment.service;

import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.dto.AppointmentRes;
import com.airfryer.repicka.domain.appointment.dto.OfferToUpdateInProgressAppointmentReq;
import com.airfryer.repicka.domain.appointment.dto.UpdateInProgressAppointmentRes;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.repository.UpdateInProgressAppointmentRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateInProgressAppointmentService
{
    private final AppointmentRepository appointmentRepository;
    private final UpdateInProgressAppointmentRepository updateInProgressAppointmentRepository;

    private final AppointmentService appointmentService;

    /// 서비스

    // 대여 중인 약속 변경 제시
    @Transactional
    public AppointmentRes offerToUpdateInProgressAppointment(User user, OfferToUpdateInProgressAppointmentReq dto)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, dto.getAppointmentId()));

        // 대여 중인 약속인지 체크
        if(appointment.getState() != AppointmentState.IN_PROGRESS) {
            throw new CustomException(CustomExceptionCode.NOT_IN_PROGRESS_APPOINTMENT, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        // 기존 반납 일시부터 새로운 반납 일시까지의 대여 구간 가능 여부 체크
        if(appointment.getReturnDate().isBefore(dto.getReturnDate()))
        {
            appointmentService.checkRentalPeriodPossibility(
                    appointment.getReturnDate(),
                    dto.getReturnDate(),
                    appointment.getItem()
            );
        }

        /// 대여 중인 약속 변경 요청 데이터 생성

        // 대여 중인 약속 변경 요청 데이터 생성
        UpdateInProgressAppointment updateInProgressAppointment;

        // 대여 중인 약속 변경 요청 데이터 조회
        Optional<UpdateInProgressAppointment> updateInProgressAppointmentOptional = updateInProgressAppointmentRepository.findByAppointmentIdAndCreatorId(
                appointment.getId(),
                user.getId()
        );

        // 기존의 대여중 변경 요청이 존재하는 경우
        if(updateInProgressAppointmentOptional.isPresent())
        {
            // 기존 데이터를 변경
            updateInProgressAppointment = updateInProgressAppointmentOptional.get();
            updateInProgressAppointment.update(dto.getReturnDate(), dto.getReturnLocation());
        }
        // 기존의 대여중 변경 요청이 존재하지 않는 경우
        else
        {
            // 새로운 데이터 생성
            updateInProgressAppointment = UpdateInProgressAppointment.builder()
                    .appointment(appointment)
                    .creator(user)
                    .returnDate(dto.getReturnDate())
                    .returnLocation(dto.getReturnLocation())
                    .build();

            // 데이터 저장
            updateInProgressAppointmentRepository.save(updateInProgressAppointment);
        }

        /// 약속 데이터 반환

        return AppointmentRes.from(updateInProgressAppointment);
    }

    // 대여 중인 약속 변경 제시 데이터 조회
    @Transactional(readOnly = true)
    public UpdateInProgressAppointmentRes findOfferToUpdateInProgressAppointment(User user, Long appointmentId, Boolean isMine)
    {
        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.APPOINTMENT_NOT_FOUND, appointmentId));

        // 대여 중인 약속인지 체크
        if(appointment.getState() != AppointmentState.IN_PROGRESS) {
            throw new CustomException(CustomExceptionCode.NOT_IN_PROGRESS_APPOINTMENT, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 대여 중인 약속 변경 제시 데이터 조회

        // isMine == true : 내가 제시한 정보
        // isMine == false : 상대방이 제시한 정보
        UpdateInProgressAppointment updateInProgressAppointment = isMine ?
                updateInProgressAppointmentRepository.findByAppointmentIdAndCreatorId(appointmentId, user.getId())
                        .orElseThrow(() -> new CustomException(CustomExceptionCode.UPDATE_IN_PROGRESS_APPOINTMENT_NOT_FOUND, null)) :
                updateInProgressAppointmentRepository.findByAppointmentIdAndCreatorId(appointmentId, Objects.equals(appointment.getRequester().getId(), user.getId()) ? appointment.getOwner().getId() : appointment.getRequester().getId())
                        .orElseThrow(() -> new CustomException(CustomExceptionCode.UPDATE_IN_PROGRESS_APPOINTMENT_NOT_FOUND, null));

        /// 데이터 반환

        return UpdateInProgressAppointmentRes.from(appointment, updateInProgressAppointment);
    }

    // 대여 중인 약속 변경 제시 수락 및 거절
    @Transactional
    public void responseOfferToUpdateInProgressAppointment(User user, Long updateInProgressAppointmentId, Boolean isAccepted)
    {
        /// 대여 중인 약속 변경 제시 데이터 조회

        // 대여 중인 약속 변경 제시 데이터 조회
        UpdateInProgressAppointment updateInProgressAppointment = updateInProgressAppointmentRepository.findById(updateInProgressAppointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.UPDATE_IN_PROGRESS_APPOINTMENT_NOT_FOUND, updateInProgressAppointmentId));

        // 본인이 본인의 데이터를 수락 또는 거절하는 경우, 예외 처리
        if(Objects.equals(user.getId(), updateInProgressAppointment.getCreator().getId())) {
            throw new CustomException(CustomExceptionCode.CANNOT_RESPONSE_UPDATE_IN_PROGRESS_APPOINTMENT_MYSELF, null);
        }

        /// 약속 데이터 조회

        // 약속 데이터 조회
        Appointment appointment = updateInProgressAppointment.getAppointment();

        // 대여 중인 약속인지 체크
        if(appointment.getState() != AppointmentState.IN_PROGRESS) {
            throw new CustomException(CustomExceptionCode.NOT_IN_PROGRESS_APPOINTMENT, appointment.getState());
        }

        // 요청자가 약속 관계자인지 체크
        if(!Objects.equals(user.getId(), appointment.getOwner().getId()) && !Objects.equals(user.getId(), appointment.getRequester().getId())) {
            throw new CustomException(CustomExceptionCode.NOT_APPOINTMENT_PARTICIPANT, null);
        }

        /// 수락하는 경우, 반납 일시가 가능한 시간인지 확인하고 약속 데이터 수정

        if(isAccepted)
        {
            // 기존 반납 일시부터 새로운 반납 일시까지의 대여 구간 가능 여부 체크
            if(appointment.getReturnDate().isBefore(updateInProgressAppointment.getReturnDate()))
            {
                appointmentService.checkRentalPeriodPossibility(
                        appointment.getReturnDate(),
                        updateInProgressAppointment.getReturnDate(),
                        appointment.getItem()
                );
            }

            // 약속 데이터 변경
            appointment.update(updateInProgressAppointment.getReturnDate(), updateInProgressAppointment.getReturnLocation());
        }

        /// 대여 중인 약속 변경 제시 데이터 삭제

        updateInProgressAppointmentRepository.delete(updateInProgressAppointment);
    }

    // 대여 중인 약속 변경 취소
    @Transactional
    public void deleteOfferToUpdateInProgressAppointment(User user, Long updateInProgressAppointmentId)
    {
        /// 대여 중인 약속 변경 제시 데이터 조회

        // 대여 중인 약속 변경 제시 데이터 조회
        UpdateInProgressAppointment updateInProgressAppointment = updateInProgressAppointmentRepository.findById(updateInProgressAppointmentId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.UPDATE_IN_PROGRESS_APPOINTMENT_NOT_FOUND, updateInProgressAppointmentId));

        // 본인이 생성한 데이터인지 체크
        if(!Objects.equals(user.getId(), updateInProgressAppointment.getCreator().getId())) {
            throw new CustomException(CustomExceptionCode.CANNOT_DELETE_OTHERS_UPDATE_IN_PROGRESS_APPOINTMENT_MYSELF, null);
        }

        /// 대여 중인 약속 변경 제시 데이터 삭제

        updateInProgressAppointmentRepository.delete(updateInProgressAppointment);
    }
}
