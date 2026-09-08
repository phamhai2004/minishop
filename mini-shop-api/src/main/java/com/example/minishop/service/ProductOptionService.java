package com.example.minishop.service;

import com.example.minishop.dto.request.ProductOptionTypeRequest;
import com.example.minishop.dto.request.ProductOptionValueRequest;
import com.example.minishop.dto.response.ProductOptionTypeResponse;
import com.example.minishop.dto.response.ProductOptionValueResponse;
import com.example.minishop.entity.ProductOptionType;
import com.example.minishop.entity.ProductOptionValue;
import com.example.minishop.exception.BadRequestException;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.repository.ProductOptionTypeRepository;
import com.example.minishop.repository.ProductOptionValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductOptionService {

    private final ProductOptionTypeRepository optionTypeRepository;
    private final ProductOptionValueRepository optionValueRepository;

    public ProductOptionService(
            ProductOptionTypeRepository optionTypeRepository,
            ProductOptionValueRepository optionValueRepository
    ) {
        this.optionTypeRepository = optionTypeRepository;
        this.optionValueRepository = optionValueRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductOptionTypeResponse> getAllTypes() {

        return optionTypeRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toTypeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductOptionTypeResponse getTypeById(Long id) {

        ProductOptionType type = getTypeEntityById(id);

        return toTypeResponse(type);
    }

    @Transactional
    public ProductOptionTypeResponse createType(
            ProductOptionTypeRequest request
    ) {

        boolean exists =
                optionTypeRepository
                        .findAll()
                        .stream()
                        .anyMatch(type ->
                                type.getName()
                                        .equalsIgnoreCase(
                                                request.getName().trim()
                                        )
                        );

        if (exists) {
            throw new BadRequestException(
                    "Tên phân loại đã tồn tại"
            );
        }

        ProductOptionType type =
                new ProductOptionType();

        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());

        ProductOptionType saved =
                optionTypeRepository.save(type);

        return toTypeResponse(saved);
    }

    @Transactional
    public ProductOptionTypeResponse updateType(
            Long id,
            ProductOptionTypeRequest request
    ) {

        ProductOptionType type =
                getTypeEntityById(id);

        boolean exists =
                optionTypeRepository
                        .findAll()
                        .stream()
                        .anyMatch(other ->
                                !other.getId().equals(id)
                                        && other.getName()
                                        .equalsIgnoreCase(
                                                request.getName().trim()
                                        )
                        );

        if (exists) {
            throw new BadRequestException(
                    "Tên phân loại đã tồn tại"
            );
        }

        type.setName(request.getName().trim());
        type.setDescription(request.getDescription());

        ProductOptionType saved =
                optionTypeRepository.save(type);

        return toTypeResponse(saved);
    }

    @Transactional
    public void deleteType(Long id) {

        ProductOptionType type =
                getTypeEntityById(id);

        if (!type.getValues().isEmpty()) {
            throw new BadRequestException(
                    "Không thể xóa phân loại đang có giá trị"
            );
        }

        optionTypeRepository.delete(type);
    }

    @Transactional(readOnly = true)
    public List<ProductOptionValueResponse> getValuesByType(
            Long optionTypeId
    ) {

        getTypeEntityById(optionTypeId);

        return optionValueRepository
                .findByOptionType_IdOrderByNameAsc(optionTypeId)
                .stream()
                .map(this::toValueResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductOptionValueResponse getValueById(Long id) {

        ProductOptionValue value =
                getValueEntityById(id);

        return toValueResponse(value);
    }

    @Transactional
    public ProductOptionValueResponse createValue(
            ProductOptionValueRequest request
    ) {

        ProductOptionType type =
                getTypeEntityById(
                        request.getOptionTypeId()
                );

        List<ProductOptionValue> existingValues =
                optionValueRepository
                        .findByOptionType_IdOrderByNameAsc(
                                type.getId()
                        );

        boolean exists =
                existingValues
                        .stream()
                        .anyMatch(value ->
                                value.getName()
                                        .equalsIgnoreCase(
                                                request.getName().trim()
                                        )
                        );

        if (exists) {
            throw new BadRequestException(
                    "Giá trị phân loại đã tồn tại"
            );
        }

        ProductOptionValue value =
                new ProductOptionValue();

        value.setName(request.getName().trim());
        value.setOptionType(type);

        ProductOptionValue saved =
                optionValueRepository.save(value);

        return toValueResponse(saved);
    }

    @Transactional
    public ProductOptionValueResponse updateValue(
            Long id,
            ProductOptionValueRequest request
    ) {

        ProductOptionValue value =
                getValueEntityById(id);

        ProductOptionType type =
                getTypeEntityById(
                        request.getOptionTypeId()
                );

        List<ProductOptionValue> existingValues =
                optionValueRepository
                        .findByOptionType_IdOrderByNameAsc(
                                type.getId()
                        );

        boolean exists =
                existingValues
                        .stream()
                        .anyMatch(other ->
                                !other.getId().equals(id)
                                        && other.getName()
                                        .equalsIgnoreCase(
                                                request.getName().trim()
                                        )
                        );

        if (exists) {
            throw new BadRequestException(
                    "Giá trị phân loại đã tồn tại"
            );
        }

        value.setName(request.getName().trim());
        value.setOptionType(type);

        ProductOptionValue saved =
                optionValueRepository.save(value);

        return toValueResponse(saved);
    }

    @Transactional
    public void deleteValue(Long id) {

        ProductOptionValue value =
                getValueEntityById(id);

        optionValueRepository.delete(value);
    }

    private ProductOptionType getTypeEntityById(Long id) {

        return optionTypeRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phân loại với id: "
                                        + id
                        )
                );
    }

    private ProductOptionValue getValueEntityById(Long id) {

        return optionValueRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy giá trị phân loại với id: "
                                        + id
                        )
                );
    }

    private ProductOptionTypeResponse toTypeResponse(
            ProductOptionType type
    ) {

        return new ProductOptionTypeResponse(
                type.getId(),
                type.getName(),
                type.getDescription()
        );
    }

    private ProductOptionValueResponse toValueResponse(
            ProductOptionValue value
    ) {

        ProductOptionType type =
                value.getOptionType();

        return new ProductOptionValueResponse(
                value.getId(),
                value.getName(),
                type.getId(),
                type.getName()
        );
    }
}