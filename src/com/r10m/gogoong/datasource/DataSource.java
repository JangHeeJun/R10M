package com.r10m.gogoong.datasource;

import java.util.List;

import com.r10m.gogoong.component.Marker;

/** ��� DataSource�� ���ʰ� �Ǵ� abstract class */
public abstract class DataSource {
	/** ��Ŀ �������� */
    public abstract List<Marker> getMarkers();
}
