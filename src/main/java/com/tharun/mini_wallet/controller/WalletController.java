
package com.tharun.mini_wallet.controller;

import com.tharun.mini_wallet.dto.CreateWalletRequest;
import com.tharun.mini_wallet.dto.CreateWalletResponse;
import com.tharun.mini_wallet.entity.Wallet;
import com.tharun.mini_wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
