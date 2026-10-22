package com.tharun.mini_wallet.repository;

import com.tharun.mini_wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
