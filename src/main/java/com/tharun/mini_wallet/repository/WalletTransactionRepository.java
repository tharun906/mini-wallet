package com.tharun.mini_wallet.repository;

import com.tharun.mini_wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {
}
