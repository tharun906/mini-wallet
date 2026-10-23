
package com.tharun.mini_wallet.service;

import com.tharun.mini_wallet.dto.DepositResponse;
import com.tharun.mini_wallet.entity.Wallet;
import com.tharun.mini_wallet.entity.WalletTransaction;
import com.tharun.mini_wallet.repository.WalletRepository;
import com.tharun.mini_wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
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



    @Transactional
    public DepositResponse deposit(Long walletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        BigDecimal previousBalance = wallet.getBalance();
        BigDecimal currentBalance = previousBalance.add(amount);

        wallet.setBalance(currentBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setStatus("SUCCESS");

        walletTransactionRepository.save(transaction);

        return new DepositResponse(
                wallet.getId(),
                previousBalance,
                amount,
                currentBalance
        );
    }


}
