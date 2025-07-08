package com.airfryer.repicka.domain.item;

import com.airfryer.repicka.common.aws.s3.S3Service;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlReq;
import com.airfryer.repicka.common.aws.s3.dto.PresignedUrlRes;
import com.airfryer.repicka.common.exception.CustomException;
import com.airfryer.repicka.common.exception.CustomExceptionCode;
import com.airfryer.repicka.domain.appointment.dto.GetItemAvailabilityRes;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.entity.AppointmentType;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.service.AppointmentService;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item.dto.CreateItemReq;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item_image.ItemImageService;
import com.airfryer.repicka.domain.item.dto.ItemDetailRes;
import com.airfryer.repicka.domain.item.dto.ItemPreviewRes;
import com.airfryer.repicka.domain.item.dto.SearchItemReq;
import com.airfryer.repicka.domain.item.entity.PostType;
import com.airfryer.repicka.domain.item.repository.ItemCustomRepository;
import com.airfryer.repicka.domain.post_like.repository.PostLikeRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemService
{
    private final ItemRepository itemRepository;
    private final ItemCustomRepository itemCustomRepository;
    private final ItemImageService itemImageService;
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    private final PostLikeRepository postLikeRepository;

    private final S3Service s3Service;

    /// 서비스

    // 제품 이미지 업로드 위해 presigned url 발급
    public PresignedUrlRes getPresignedUrl(PresignedUrlReq req) {
        return s3Service.generatePresignedUrl(req, "post");
    }

    // 제품 생성
    @Transactional
    public ItemDetailRes createItemAndImages(CreateItemReq dto, User user)
    {
        // 제품 생성
        Item item = Item.builder()
                .owner(user)
                .productTypes(dto.getProductTypes())
                .postTypes(dto.getPostTypes())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .color(dto.getColor())
                .size(dto.getSize())
                .quality(dto.getQuality())
                .rentalFee(dto.getRentalFee())
                .salePrice(dto.getSalePrice())
                .deposit(dto.getDeposit())
                .location(dto.getLocation())
                .tradeMethods(dto.getTradeMethods())
                .canDeal(dto.getCanDeal())
                .likeCount(0)
                .chatRoomCount(0)
                .saleDate(null)
                .repostDate(LocalDateTime.now())
                .build();

        // 제품 저장
        itemRepository.save(item);

        // 제품 이미지 생성
        itemImageService.createItemImage(dto.getImages(), item);

        return ItemDetailRes.from(item, itemImageService.getItemImages(item), user, false);
    }

    // 제품 수정
    @Transactional
    public ItemDetailRes updateItem(Long itemId, CreateItemReq dto, User user)
    {
        // 제품 조회 및 권한 확인
        Item item = validatePostOwnership(itemId, user);

        // 제품 수정
        item.updateItem(dto);

        // TODO: 제품 이미지 수정

        return ItemDetailRes.from(item, itemImageService.getItemImages(item), user, false);
    }

    // 제품 삭제
    @Transactional
    public void deleteItem(Long itemId, User user)
    {
        // 제품 조회 및 권한 확인
        Item item = validatePostOwnership(itemId, user);

        // 확정되었거나, 약속이 존재하는 경우, 삭제 불가능
        if(!appointmentService.isItemAvailableOnInterval(itemId, LocalDateTime.now())) {
            throw new CustomException(CustomExceptionCode.ALREADY_RESERVED_ITEM, null);
        }

        // 제품 삭제
        itemRepository.delete(item);
    }

    // 제품 상세 조회
    @Transactional(readOnly = true)
    public ItemDetailRes getItemDetail(Long itemId, User user)
    {
        // 제품 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 제품 이미지 조회
        List<String> imageUrls = itemImageService.getItemImages(item);

        // 좋아요 여부 조회
        boolean isLiked = (user != null) && (postLikeRepository.findByItemIdAndLikerId(itemId, user.getId()).isPresent());

        return ItemDetailRes.from(item, imageUrls, user, isLiked);
    }

    // 제품 목록 검색
    @Transactional(readOnly = true)
    public List<ItemPreviewRes> searchItemList(SearchItemReq condition)
    {
        // 태그로 제품 리스트 찾기
        List<Item> items = itemCustomRepository.findItemsByCondition(condition);

        // 제품 각각의 썸네일 조회
        Map<Long, String> thumbnailMap = itemImageService.getThumbnailsForItems(items);

        // 제품 정보를 정제하여 반환
        return items.stream()
            .map(item -> {
                boolean isAvailable = appointmentService.isItemAvailableOnDate(item.getId(), condition.getDate());  // 원하는 날짜에 대여나 구매 가능 여부
                String thumbnailUrl = thumbnailMap.get(item.getId()); // 대표 사진
                return ItemPreviewRes.from(item, thumbnailUrl, isAvailable);
            })
            .toList();
    }
    
    // 제품 끌올
    @Transactional
    public LocalDateTime repostItem(Long itemId, User user)
    {
        // 제품 조회 및 권한 확인
        Item item = validatePostOwnership(itemId, user);

        // 제품 끌올
        item.repostItem();

        return item.getRepostDate();
    }

    // 월 단위로 날짜별 제품 대여 가능 여부 조회
    @Transactional(readOnly = true)
    public GetItemAvailabilityRes getItemRentalAvailability(Long itemId, int year, int month)
    {
        // 해당 월의 길이
        int lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth();

        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 대여가 가능한 제품인지 확인
        if(!Arrays.asList(item.getPostTypes()).contains(PostType.RENTAL)) {
            throw new CustomException(CustomExceptionCode.CANNOT_RENTAL_ITEM, null);
        }

        /// 반환할 날짜별 제품 대여 가능 여부 해시맵 생성 및 초기화

        // 반환할 날짜별 제품 대여 가능 여부 해시맵
        Map<LocalDate, Boolean> map = new LinkedHashMap<>();

        // 일단, 모든 날짜에 대여가 가능한 것으로 초기화
        for (int i = 1; i <= lastDayOfMonth; i++) {
            map.put(LocalDate.of(year, month, i), true);
        }

        /// 불가능 처리
        /// 1. 현재 이전의 날짜들은 전부 불가능 처리
        /// 2. 제품 판매 날짜부터는 전부 대여 불가능 처리
        /// 3. 해당 월 동안 예정된 모든 대여 약속들에 대해, 각 구간마다 대여 불가능 처리

        // 현재 이전의 날짜들은 전부 불가능 처리
        for(int i = 1; i <= lastDayOfMonth && LocalDate.of(year, month, i).isBefore(LocalDate.now()); i++) {
            map.put(LocalDate.of(year, month, i), false);
        }

        // 제품이 판매 예정 혹은 판매된 경우, 이후의 날짜들은 전부 대여 불가능 처리
        if(Arrays.asList(item.getPostTypes()).contains(PostType.SALE) && item.getSaleDate() != null)
        {
            LocalDate saleDate = item.getSaleDate().toLocalDate();  // 제품 판매 예정 날짜

            // 해당 월 이전에 이미 판매된 경우
            if(saleDate.isBefore(LocalDate.of(year, month, 1))) {
                for(int i = 1; i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 해당 월 동안 판매되는 경우
            else if(
                    !saleDate.isBefore(LocalDate.of(year, month, 1)) &&
                            !saleDate.isAfter(LocalDate.of(year, month, lastDayOfMonth))
            ) {
                for(int i = saleDate.getDayOfMonth(); i <= lastDayOfMonth; i++) {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        // 해당 월 동안 존재하는 모든 대여 약속 조회
        List<Appointment> appointmentList = appointmentRepository.findListOverlappingWithPeriod(
                itemId,
                List.of(AppointmentState.CONFIRMED, AppointmentState.IN_PROGRESS),
                AppointmentType.RENTAL,
                LocalDateTime.of(year, month, 1, 0, 0, 0),
                LocalDateTime.of(year, month, YearMonth.of(year, month).lengthOfMonth(), 23, 59, 59, 0)
        );

        // 모든 대여 약속 구간에 대해, 대여 불가능 처리
        for(Appointment appointment : appointmentList)
        {
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월 이후인 경우
            if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() > month)
            {
                map.replaceAll((key, value) -> false);
                break;
            }
            // 대여 시작 날짜가 해당 월에 속하고, 대여 종료 날짜가 해당 월 이후인 경우
            else if(appointment.getRentalDate().getMonthValue() == month && appointment.getReturnDate().getMonthValue() > month)
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= lastDayOfMonth; i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜가 해당 월 이전이고, 대여 종료 날짜가 해당 월에 속하는 경우
            else if(appointment.getRentalDate().getMonthValue() < month && appointment.getReturnDate().getMonthValue() == month)
            {
                for(int i = 1; i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
            // 대여 시작 날짜 및 대여 종료 날짜가 둘 다 해당 월에 속하는 경우
            else
            {
                for(int i = appointment.getRentalDate().getDayOfMonth(); i <= appointment.getReturnDate().getDayOfMonth(); i++)
                {
                    map.put(LocalDate.of(year, month, i), false);
                }
            }
        }

        return GetItemAvailabilityRes.builder()
                .itemId(item.getId())
                .year(year)
                .month(month)
                .availability(map)
                .build();
    }

    // 제품 구매가 가능한 첫 날짜 조회
    @Transactional(readOnly = true)
    public LocalDate getItemSaleAvailability(Long itemId)
    {
        /// 제품 데이터 조회

        // 제품 데이터 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 구매가 가능한 제품인지 확인
        if(!Arrays.asList(item.getPostTypes()).contains(PostType.SALE)) {
            throw new CustomException(CustomExceptionCode.CANNOT_SALE_ITEM, null);
        }

        // 판매 예정이거나 판매된 제품이 아닌지 체크
        if(item.getSaleDate() != null) {
            throw new CustomException(CustomExceptionCode.ALREADY_SALE_RESERVED, item.getId());
        }

        /// 제품 구매가 가능한 첫 날짜 반환

        return appointmentService.getFirstSaleAvailableDate(item.getId());
    }

    /// 공통 로직

    // 제품 조회 및 권한 확인
    private Item validatePostOwnership(Long itemId, User user)
    {
        // 제품 조회
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new CustomException(CustomExceptionCode.ITEM_NOT_FOUND, itemId));

        // 권한 확인
        if (!item.getOwner().getId().equals(user.getId())) {
            throw new CustomException(CustomExceptionCode.ITEM_ACCESS_FORBIDDEN, null);
        }

        return item;
    }
}
