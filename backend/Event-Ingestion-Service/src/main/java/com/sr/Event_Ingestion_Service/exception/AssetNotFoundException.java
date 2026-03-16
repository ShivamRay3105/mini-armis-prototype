package com.sr.Event_Ingestion_Service.exception;

public class AssetNotFoundException extends RuntimeException {

    public AssetNotFoundException(String message) {
        super(message);
    }
}

