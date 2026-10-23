
package com.tharun.mini_wallet.service;

import com.tharun.mini_wallet.dto.*;
import com.tharun.mini_wallet.entity.TransactionStatus;
import com.tharun.mini_wallet.entity.TransactionType;
import com.tharun.mini_wallet.entity.Wallet;
import com.tharun.mini_wallet.entity.WalletTransaction;
import com.tharun.mini_wallet.exception.BadRequestException;
import com.tharun.mini_wallet.exception.ResourceNotFoundException;
import com.tharun.mini_wallet.repository.WalletRepository;
import com.tharun.mini_wallet.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    @Transactional
    public Wallet createWallet(String name, String email, BigDecimal initialBalance) {

        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name is required");
        }

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (initialBalance == null
                || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(
                    "Initial balance cannot be negative");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (walletRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered");
        }

        Wallet wallet = new Wallet();
        wallet.setName(name.trim());
        wallet.setEmail(normalizedEmail);
        wallet.setBalance(initialBalance);

        return walletRepository.save(wallet);
    }

    @Transactional
    public DepositResponse deposit(Long walletId, BigDecimal amount) {

        if (walletId == null || walletId <= 0) {
            throw new BadRequestException("Valid wallet ID is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Deposit amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new BadRequestException(
                    "Deposit amount must fit DECIMAL(19,2)");
        }

        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        BigDecimal previousBalance = wallet.getBalance();
        BigDecimal currentBalance = previousBalance.add(amount);

        if (currentBalance.precision() - currentBalance.scale() > 17) {
            throw new BadRequestException(
                    "Resulting balance exceeds the supported limit");
        }

        wallet.setBalance(currentBalance);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);

        walletTransactionRepository.save(transaction);

        return new DepositResponse(
                wallet.getId(),
                previousBalance,
                amount,
                currentBalance
        );
    }


    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getTransactionHistory(Long walletId) {

        if (walletId == null || walletId <= 0) {
            throw new BadRequestException("Valid wallet ID is required");
        }

        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet not found");
        }

        return walletTransactionRepository
                .findByWalletIdOrderByCreatedAtDescIdDesc(walletId)
                .stream()
                .map(transaction -> new TransactionHistoryResponse(
                        transaction.getId(),
                        transaction.getType().name(),
                        transaction.getAmount(),
                        transaction.getStatus().name(),
                        transaction.getCreatedAt()
                ))
                .toList();
    }


    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        Long fromWalletId = request.fromWalletId();
        Long toWalletId = request.toWalletId();
        BigDecimal amount = request.amount();

        if (fromWalletId.equals(toWalletId)) {
            throw new BadRequestException(
                    "Sender and receiver wallets must be different");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Transfer amount must be greater than zero");
        }

        // Lock wallets in a consistent order to reduce deadlock risk.
        Long firstId = Math.min(fromWalletId, toWalletId);
        Long secondId = Math.max(fromWalletId, toWalletId);

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet " + firstId + " not found"));

        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet " + secondId + " not found"));

        Wallet sender = fromWalletId.equals(firstId) ? firstWallet : secondWallet;
        Wallet receiver = toWalletId.equals(firstId) ? firstWallet : secondWallet;

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException(
                    "Insufficient balance to complete the transfer");
        }

        BigDecimal senderBalance = sender.getBalance().subtract(amount);
        BigDecimal receiverBalance = receiver.getBalance().add(amount);

        sender.setBalance(senderBalance);
        receiver.setBalance(receiverBalance);

        String referenceId = UUID.randomUUID().toString();

        WalletTransaction outgoing = new WalletTransaction();
        outgoing.setWallet(sender);
        outgoing.setType(TransactionType.TRANSFER_OUT);
        outgoing.setAmount(amount);
        outgoing.setStatus(TransactionStatus.SUCCESS);
        outgoing.setReferenceId(referenceId);

        WalletTransaction incoming = new WalletTransaction();
        incoming.setWallet(receiver);
        incoming.setType(TransactionType.TRANSFER_IN);
        incoming.setAmount(amount);
        incoming.setStatus(TransactionStatus.SUCCESS);
        incoming.setReferenceId(referenceId);

        walletTransactionRepository.save(outgoing);
        walletTransactionRepository.save(incoming);

        //ROLLBACK TESTING
//        walletTransactionRepository.saveAndFlush(outgoing);
//        throw new RuntimeException("TEST_ROLLBACK");

        return new TransferResponse(
                outgoing.getId(),
                sender.getId(),
                receiver.getId(),
                amount,
                TransactionStatus.SUCCESS.name()
        );
    }


    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId) {

        if (walletId == null || walletId <= 0) {
            throw new BadRequestException("Valid wallet ID is required");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        return new WalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getEmail(),
                wallet.getBalance()
        );
    }


}
