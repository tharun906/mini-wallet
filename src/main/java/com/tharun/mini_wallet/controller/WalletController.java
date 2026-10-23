
package com.tharun.mini_wallet.controller;

import com.tharun.mini_wallet.dto.*;
import com.tharun.mini_wallet.entity.Wallet;
import com.tharun.mini_wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<CreateWalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {

        Wallet wallet = walletService.createWallet(
                request.name(),
                request.email(),
                request.initialBalance()
        );

        CreateWalletResponse response = new CreateWalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getEmail(),
                wallet.getBalance()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody DepositRequest request) {

        DepositResponse response = walletService.deposit(
                walletId,
                request.amount()
        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(
                walletService.getTransactionHistory(walletId)
        );
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = walletService.transfer(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(walletService.getWallet(walletId));
    }



}
