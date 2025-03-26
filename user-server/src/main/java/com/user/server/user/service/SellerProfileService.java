package com.user.server.user.service;

import com.common.config.api.exception.GeneralException;
import com.user.server.redis.RedisUserInfoRepository;
import com.user.server.user.dto.RequestSellerProfile;
import com.user.server.user.dto.ResponseUser;
import com.user.server.user.entity.Role;
import com.user.server.user.entity.SellerProfile;
import com.user.server.user.entity.User;
import com.user.server.user.repository.SellerProfileRepository;
import com.user.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProfileService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final RedisUserInfoRepository redisUserInfoRepository;

    @Transactional
    public void applySeller(String uid, RequestSellerProfile dto) {
        User user = userRepository.findByUid(uid)
                .orElseThrow(() -> new GeneralException("사용자를 찾을 수 없습니다."));

        if (user.getRole().name().equals("SELLER")) {
            throw new GeneralException("이미 판매자로 등록되어 있습니다.");
        }

        SellerProfile profile = SellerProfile.builder()
                .shopName(dto.getShopName())
                .contact(dto.getContact())
                .description(dto.getDescription())
                .user(user)
                .build();

        sellerProfileRepository.save(profile);
        user.setRole(Role.SELLER);
        user.setSellerProfile(profile);

        ResponseUser responseUser = ResponseUser.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .role(user.getRole())
                .build();

        redisUserInfoRepository.deleteUser(uid);
        redisUserInfoRepository.saveUser(uid, responseUser);
    }
}
