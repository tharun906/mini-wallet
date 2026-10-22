
package com.tharun.mini_wallet.service;

import com.tharun.mini_wallet.entity.Wallet;
import com.tharun.mini_wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet createWallet(String name, String email, BigDecimal initialBalance) {
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        Wallet wallet = new Wallet();
        wallet.setName(name);
        wallet.setEmail(email);
        wallet.setBalance(initialBalance);

        return walletRepository.save(wallet);
    }
}
