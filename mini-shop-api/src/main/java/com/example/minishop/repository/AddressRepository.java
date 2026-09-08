package com.example.minishop.repository;

import com.example.minishop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_IdOrderByDefaultAddressDescIdDesc(Long userId);

    Optional<Address> findByIdAndUser_Id(Long id, Long userId);

    Optional<Address> findByUser_IdAndDefaultAddressTrue(Long userId);

    boolean existsByUser_Id(Long userId);
}