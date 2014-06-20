package com.r10m.gogoong.datasource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.r10m.gogoong.component.Marker;

/** URL로 부터 데이터를 얻어오기 위한 기본설정을 포함한 확장을 위한 클래스 */
public abstract class NetworkDataSource extends DataSource {
	/** 사용자에게 보여질 결과물의 최대개수 */
    protected static final int MAX = 1000;
    /** 연결할 때 타임아웃을 정하는 밀리초 단위의 값 */
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;
    
    protected List<Marker> markersCache = null;
    
    public abstract String createRequestURL(double lat, double lon, double alt,
                                            float radius, String locale);

    public abstract List<Marker> parse(JSONObject root);

    public List<Marker> getMarkers() {
        return markersCache;
    }
    /** String으로 전달된 특정 URL로 부터 InputStream을 얻어오는데 사용 */
    protected static InputStream getHttpGETInputStream(String urlStr) {
        if (urlStr == null)
            throw new NullPointerException();

        InputStream is = null;
        URLConnection conn = null;

        try {
            if (urlStr.startsWith("file://"))
                return new FileInputStream(urlStr.replace("file://", ""));

            URL url = new URL(urlStr);
            conn = url.openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);

            is = conn.getInputStream();

            return is;
        } catch (Exception ex) {
            try {
                is.close();
            } catch (Exception e) {
                // Ignore
            }
            try {
                if (conn instanceof HttpURLConnection)
                    ((HttpURLConnection) conn).disconnect();
            } catch (Exception e) {
                // Ignore
            }
            ex.printStackTrace();
        }

        return null;
    }
    /** InputStream의 내용을 얻어와서  String에 입력 */
    protected String getHttpInputString(InputStream is) {
        if (is == null)
            throw new NullPointerException();

        BufferedReader reader = new BufferedReader(new InputStreamReader(is),
                8 * 1024);
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    /** 데이터소스로부터 JSON오브젝트를 얻어서 다른 parse()메소드를 호출 */
    public List<Marker> parse(String url) {
        if (url == null)
            throw new NullPointerException();

        InputStream stream = null;
        stream = getHttpGETInputStream(url);
        if (stream == null)
            throw new NullPointerException();

        String string = null;
        string = getHttpInputString(stream);
        if (string == null)
            throw new NullPointerException();

        JSONObject json = null;
        try {
            json = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null)
            throw new NullPointerException();
        
        return parse(json);
    }
}
