package com.r10m.gogoong.filter;

import org.opencv.core.Mat;

public interface Filter {
    public abstract boolean apply(final Mat src, final Mat dst);
}
