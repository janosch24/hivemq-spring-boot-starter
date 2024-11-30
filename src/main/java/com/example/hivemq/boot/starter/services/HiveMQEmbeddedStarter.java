package com.example.hivemq.boot.starter.services;

/**
 * Interface to startup and shutdown embedded HiveMQ
 */
public interface HiveMQEmbeddedStarter {

    /**
     * Starts the embedded mqtt-broker
     * Blocks until startup finished.
     */
    void startup();

    /**
     * Stops the embedded mqtt-broker
     * Blocks until shutdown finished.
     */
    void shutdown();
}