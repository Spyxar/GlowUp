package com.spyxar.glowup;

public final class ColorHelper {
    private ColorHelper() {}
    public static int getRed(int color) { return (color >> 16) & 0xFF; }
    public static int getGreen(int color) { return (color >> 8) & 0xFF; }
    public static int getBlue(int color) { return color & 0xFF; }
}