package com.r10m.gogoong.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.r10m.gogoong.R;
import com.r10m.gogoong.component.IconMarker;
import com.r10m.gogoong.component.Marker;

public class LocalDataSource extends DataSource {
	private List<Marker> cachedMarkers = new ArrayList<Marker>();
    private static Bitmap icon = null;
    
    public LocalDataSource(Resources res) {
        if (res==null) throw new NullPointerException();
        
        createIcon(res);
    }
    /** 앱의 기본 아이콘을 할당 */
    protected void createIcon(Resources res) {
        if (res==null) throw new NullPointerException();
        
        icon=BitmapFactory.decodeResource(res, R.drawable.ic_launcher);
    }
    
	@Override
	public List<Marker> getMarkers() {
		Marker atl = new IconMarker("ATL", 39.931269, -75.051261, 0, Color.DKGRAY, icon);
        cachedMarkers.add(atl);

        Marker home = new Marker("Mt Laurel", 39.95, -74.9, 0, Color.YELLOW);
        cachedMarkers.add(home);

        return cachedMarkers;
	}

}
