package com.user.server.product.service;

import com.common.config.api.exception.GeneralException;
import com.user.server.product.dto.RequestProductDto;
import com.user.server.product.dto.ResponseProductDto;
import com.user.server.product.entity.Product;
import com.user.server.product.respository.ProductRepository;
import com.user.server.user.entity.SellerProfile;
import com.user.server.user.entity.User;
import com.user.server.user.repository.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerProfileRepository;

    public List<ResponseProductDto> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue()
                .stream().map(ResponseProductDto::from).collect(Collectors.toList());
    }

    public List<ResponseProductDto> getLatestProducts() {
        return productRepository.findTop10ByOrderByCreatedAtDesc()
                .stream().map(ResponseProductDto::from).collect(Collectors.toList());
    }

    public List<ResponseProductDto> getDiscountedProducts() {
        return productRepository.findDiscountedProducts()
                .stream().map(ResponseProductDto::from).collect(Collectors.toList());
    }

    public List<ResponseProductDto> getProductsByTag(String tag) {
        return productRepository.findByTag(tag)
                .stream().map(ResponseProductDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ResponseProductDto> getAllProducts(User user, Pageable pageable) {
        Page<Product> products = productRepository.findAllByUserUid(user.getUid(), pageable);

        return products.map(ResponseProductDto::from);
    }

    @Transactional(readOnly = true)
    public Product getProduct(String productUid) {

        return productRepository.findByUid(productUid);
    }

    public List<ResponseProductDto> getProductsByBrand(Long brandId) {
        return productRepository.findByBrandId(brandId).stream()
                .map(ResponseProductDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public String registerProduct(RequestProductDto requestProductDto, User user) {
        try {
            String uid = RandomStringUtils.randomAlphanumeric(16);

            SellerProfile seller = sellerProfileRepository.findByUserUid(user.getUid())
                    .orElseThrow(() -> new GeneralException("판매자 정보가 없습니다."));


            Product product = productRepository.save(requestProductDto.toEntity(user.getId(), uid, seller));
            productRepository.save(product);

            return uid;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }


}
