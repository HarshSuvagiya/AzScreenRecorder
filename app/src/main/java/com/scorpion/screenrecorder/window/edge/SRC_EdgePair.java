package com.scorpion.screenrecorder.window.edge;

public class SRC_EdgePair {
    public SRC_Edge primary;
    public SRC_Edge secondary;

    public SRC_EdgePair(SRC_Edge edge1, SRC_Edge edge2) {
        this.primary = edge1;
        this.secondary = edge2;
    }
}
