package com.ocr.termocel.events;

/**
 * SensorClickedEvent
 */
public class SensorClickedEvent extends AbstractEvent {
    public enum Type {
        COMPLETED,
        STARTED
    }

    private int _resultCode;

    private int _elementId;

    public SensorClickedEvent(Enum type, int _resultCode, int _elementId) {
        super(type);
        this._resultCode = _resultCode;
        this._elementId = _elementId;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public int getElementId() {
        return _elementId;
    }
}
