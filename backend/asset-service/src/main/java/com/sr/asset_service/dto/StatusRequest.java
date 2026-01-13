package com.sr.asset_service.dto;

import com.sr.asset_service.model.Status;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
                @NotNull Status status) {

}
