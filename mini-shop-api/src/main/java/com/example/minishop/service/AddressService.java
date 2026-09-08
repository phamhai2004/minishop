package com.example.minishop.service;

import com.example.minishop.dto.request.AddressRequest;
import com.example.minishop.dto.request.CreateOrderRequest;
import com.example.minishop.dto.response.AddressResponse;
import com.example.minishop.entity.Address;
import com.example.minishop.entity.User;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.mapper.AddressMapper;
import com.example.minishop.repository.AddressRepository;
import com.example.minishop.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressService(
            AddressRepository addressRepository,
            AddressMapper addressMapper
    ) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        Long userId = SecurityUtils.getCurrentUserId();

        return addressRepository.findByUser_IdOrderByDefaultAddressDescIdDesc(userId)
                .stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User user = SecurityUtils.getCurrentUser().getUser();

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            clearDefaultAddress(user.getId());
        }

        Address savedAddress = addressRepository.save(address);

        Long userId = user.getId();

        boolean hasAddress = addressRepository.existsByUser_Id(userId);

        if (!hasAddress) {
            address.setDefaultAddress(true);
        }

        if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            clearDefaultAddress(userId);
        }

        return addressMapper.toResponse(savedAddress);
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Address address = getMyAddressEntity(id, userId);

        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            clearDefaultAddress(userId);
        }

        addressMapper.updateEntity(address, request);

        return addressMapper.toResponse(address);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Address address = getMyAddressEntity(id, userId);

        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefault(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        Address address = getMyAddressEntity(id, userId);

        clearDefaultAddress(userId);

        address.setDefaultAddress(true);

        return addressMapper.toResponse(address);
    }

    @Transactional(readOnly = true)
    public Address getMyDefaultAddressEntity() {
        Long userId = SecurityUtils.getCurrentUserId();

        return addressRepository.findByUser_IdAndDefaultAddressTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bạn chưa có địa chỉ mặc định"
                ));
    }

    private Address getMyAddressEntity(Long id, Long userId) {
        return addressRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy địa chỉ"
                ));
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUser_IdAndDefaultAddressTrue(userId)
                .ifPresent(address -> address.setDefaultAddress(false));
    }

    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress() {
        return addressMapper.toResponse(getMyDefaultAddressEntity());
    }

    @Transactional(readOnly = true)
    public Address getMyAddressEntity(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return getMyAddressEntity(id, userId);
    }
    
}