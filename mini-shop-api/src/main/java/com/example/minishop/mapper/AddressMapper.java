package com.example.minishop.mapper;

import com.example.minishop.dto.request.AddressRequest;
import com.example.minishop.dto.response.AddressResponse;
import com.example.minishop.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressRequest request) {

        Address address = new Address();

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setWard(request.getWard());
        address.setDetail(request.getDetail());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDefaultAddress(Boolean.TRUE.equals(request.getDefaultAddress()));

        return address;
    }

    public void updateEntity(
            Address address,
            AddressRequest request
    ) {

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setWard(request.getWard());
        address.setDetail(request.getDetail());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setDefaultAddress(Boolean.TRUE.equals(request.getDefaultAddress()));
    }

    public AddressResponse toResponse(
            Address address
    ) {

        AddressResponse response = new AddressResponse();

        response.setId(address.getId());
        response.setReceiverName(address.getReceiverName());
        response.setPhone(address.getPhone());
        response.setProvince(address.getProvince());
        response.setWard(address.getWard());
        response.setDetail(address.getDetail());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        response.setDefaultAddress(address.getDefaultAddress());

        return response;
    }
}