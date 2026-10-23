
package com.tharun.mini_wallet.dto;

import java.math.BigDecimal;

public record DepositResponse(
        Long walletId,
        BigDecimal previousBalance,
        BigDecimal amount,
        BigDecimal currentBalance
) {}
