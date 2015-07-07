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


    private boolean _delete;

    public SensorClickedEvent(Enum type, int _resultCode, int _elementId, boolean _delete) {
        super(type);
        this._resultCode = _resultCode;
        this._elementId = _elementId;
        this._delete = _delete;
    }

    public int getResultCode() {
        return _resultCode;
    }

    public int getElementId() {
        return _elementId;
    }

    public boolean isDelete() {
        return _delete;
    }

}
