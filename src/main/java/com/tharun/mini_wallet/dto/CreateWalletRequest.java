
package com.tharun.mini_wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateWalletRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(
                value = "0.00",
                message = "Initial balance cannot be negative"
        )
        @Digits(
                integer = 17,
                fraction = 2,
                message = "Initial balance must have at most 2 decimal places"
        )
        BigDecimal initialBalance

) {}
