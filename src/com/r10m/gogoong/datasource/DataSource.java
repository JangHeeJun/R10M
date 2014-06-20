package com.r10m.gogoong.datasource;

import java.util.List;

import com.r10m.gogoong.component.Marker;

/** 모든 DataSource의 기초가 되는 abstract class */
public abstract class DataSource {
	/** 마커 가져오기 */
    public abstract List<Marker> getMarkers();
}
