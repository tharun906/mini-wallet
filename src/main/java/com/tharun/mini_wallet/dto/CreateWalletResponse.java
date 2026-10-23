
package com.tharun.mini_wallet.dto;

import java.math.BigDecimal;

public record CreateWalletResponse(
        Long walletId,
        String name,
        String email,
        BigDecimal balance
) {}
