
package com.tharun.mini_wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        Long transactionId,
        String type,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt
) {}
