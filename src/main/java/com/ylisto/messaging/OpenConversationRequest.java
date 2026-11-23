package com.ylisto.messaging;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OpenConversationRequest(@NotNull UUID recipientId) {
}
