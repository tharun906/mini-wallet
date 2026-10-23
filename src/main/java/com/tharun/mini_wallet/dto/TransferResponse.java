
package com.tharun.mini_wallet.dto;

import java.math.BigDecimal;

public record TransferResponse(
        Long transactionId,
        Long fromWalletId,
        Long toWalletId,
        BigDecimal amount,
        String status
) {}
