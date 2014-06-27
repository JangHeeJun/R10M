package com.r10m.gogoong.filter;

import org.opencv.core.Mat;

public class NoneFilter implements Filter {

	@Override
	public boolean apply(final Mat src, final Mat dst) {
		// Do nothing.
		return false;
	}
}
