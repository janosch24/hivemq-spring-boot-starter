package com.example.boot.hivemq;

import com.hivemq.embedded.EmbeddedExtension;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
public class EmbeddedExtension2 implements EmbeddedExtension, ExtensionMain {

    @Override
    public @NotNull String getId() {
        return getClass().toString();
    }

    @Override
    public @NotNull String getName() {
        return "MyEmbeddedExtension2";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String getAuthor() {
        return "arajan";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getStartPriority() {
        return 2;
    }

    @Override
    public @NotNull ExtensionMain getExtensionMain() {
        return this;
    }

    @Override
    public void extensionStart(@NotNull ExtensionStartInput extensionStartInput, @NotNull ExtensionStartOutput extensionStartOutput) {
        log.info("{} starting ...", this.getName());
    }

    @Override
    public void extensionStop(@NotNull ExtensionStopInput extensionStopInput, @NotNull ExtensionStopOutput extensionStopOutput) {
        log.info("{} stopping ...", this.getName());
    }
}
