package com.example.sortinggame;
public class GarbageItem {
    public enum Type {
        PLASTIC, PAPER, GLASS
    }
    private Type type;
    private int drawableId;

    public GarbageItem(Type type, int drawableId) {
        this.type = type;
        this.drawableId = drawableId;
    }

    public Type getType() {
        return type;
    }

    public int getDrawableId() {
        return drawableId;
    }
}
