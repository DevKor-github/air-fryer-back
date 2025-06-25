package com.airfryer.repicka.util;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.repository.ItemRepository;
import com.airfryer.repicka.domain.item_image.entity.ItemImage;
import com.airfryer.repicka.domain.item_image.repository.ItemImageRepository;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.post.repository.PostRepository;
import com.airfryer.repicka.domain.post_like.entity.PostLike;
import com.airfryer.repicka.domain.post_like.repository.PostLikeRepository;
import com.airfryer.repicka.domain.user.entity.Gender;
import com.airfryer.repicka.domain.user.entity.LoginMethod;
import com.airfryer.repicka.domain.user.entity.Role;
import com.airfryer.repicka.domain.user.entity.User;
import com.airfryer.repicka.domain.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateEntityUtil
{
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final AppointmentRepository appointmentRepository;

    public User createUser()
    {
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@naver.com")
                .nickname("Test")
                .loginMethod(LoginMethod.GOOGLE)
                .oauthId("0000000000")
                .role(Role.USER)
                .profileImageUrl("/프로필-이미지-기본경로")
                .isKoreaUnivVerified(false)
                .gender(Gender.MALE)
                .height(180)
                .weight(70)
                .fcmToken("fcmToken")
                .todayPostCount(0)
                .lastAccessDate(LocalDate.now())
                .build();

        user = userRepository.save(user);

        return user;
    }

    public Item createItem()
    {
        Item item = Item.builder()
                .productTypes(new ProductType[]{ProductType.HOCKEY, ProductType.ACCESSORY})
                .size(ItemSize.XL)
                .title("title")
                .description("description")
                .color(ItemColor.BLACK)
                .quality(ItemQuality.LOW)
                .location("location")
                .tradeMethod(TradeMethod.DIRECT)
                .canDeal(true)
                .repostDate(LocalDateTime.now())
                .build();

        item = itemRepository.save(item);

        return item;
    }

    public ItemImage createItemImage()
    {
        ItemImage itemImage = ItemImage.builder()
                .item(createItem())
                .imageUrl("/이미지-경로")
                .build();

        itemImage = itemImageRepository.save(itemImage);

        return itemImage;
    }

    public Post createPost()
    {
        Post post = Post.builder()
                .writer(createUser())
                .item(createItem())
                .postType(PostType.RENTAL)
                .price(10000)
                .build();

        post = postRepository.save(post);

        return post;
    }

    public PostLike createPostLike()
    {
        PostLike postLike = PostLike.builder()
                .liker(createUser())
                .post(createPost())
                .build();

        postLike = postLikeRepository.save(postLike);

        return postLike;
    }

    public Appointment createAppointment()
    {
        User owner = createUser();
        User requester = createUser();

        Appointment appointment = Appointment.builder()
                .post(createPost())
                .creator(owner)
                .owner(owner)
                .requester(requester)
                .rentalLocation("rentalLocation")
                .returnLocation("returnLocation")
                .rentalDate(LocalDateTime.now())
                .returnDate(LocalDateTime.now().plusDays(1))
                .price(10000)
                .state(AppointmentState.SUCCESS)
                .build();

        appointment = appointmentRepository.save(appointment);

        return appointment;
    }
}
