package com.tovi.devices.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author <a href='mailto:zhaotengfei9@gmail.com'>Tengfei Zhao</a>
 */

/**
 * 参考  https://github.com/AlexZhuo/AlxLocationManager
 * http://blog.csdn.net/lvshaorong/article/details/51635441
 */

public class LocationUtil {
    private static final String TAG = "LocationUtil";

    /**
     * 基站定位
     * <p>
     * 参考 http://blog.csdn.net/android_ls/article/details/8672856
     * http://www.jianshu.com/p/02c7508b2d1f
     * http://www.cnblogs.com/rayee/archive/2012/02/02/2336101.html#section-5
     *
     * @param context
     */
    public static void telLocation(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 返回值MCC + MNC
        String operator = mTelephonyManager.getNetworkOperator();
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));

        // 中国移动和中国联通获取LAC、CID的方式
        GsmCellLocation location = (GsmCellLocation) mTelephonyManager.getCellLocation();
        int lac = location.getLac();
        int cellId = location.getCid();

        Log.i(TAG, " MCC = " + mcc + "\t MNC = " + mnc + "\t LAC = " + lac + "\t CID = " + cellId);

        // 中国电信获取LAC、CID的方式
                /*CdmaCellLocation location1 = (CdmaCellLocation) mTelephonyManager.getCellLocation();
                lac = location1.getNetworkId();
                cellId = location1.getBaseStationId();
                cellId /= 16;*/


        // 另写法
//        if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
//            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)
//                    mTelephonyManager.getCellLocation();
//            int cid = cdmaCellLocation.getBaseStationId(); //获取cdma基站识别标号 BID
//            int lac = cdmaCellLocation.getNetworkId(); //获取cdma网络编号NID
//            int sid = cdmaCellLocation.getSystemId(); //用谷歌API的话cdma网络的mnc要用这个getSystemId()取得→SID
//        } else {
//            GsmCellLocation gsmCellLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
//            int cid = gsmCellLocation.getCid(); //获取gsm基站识别标号
//            int lac = gsmCellLocation.getLac(); //获取gsm网络编号
//        }

        // 获取邻区基站信息
        List<NeighboringCellInfo> infos = mTelephonyManager.getNeighboringCellInfo();
        StringBuffer sb = new StringBuffer("总数 : " + infos.size() + "\n");
        for (NeighboringCellInfo info1 : infos) { // 根据邻区总数进行循环
            sb.append(" LAC : " + info1.getLac()); // 取出当前邻区的LAC
            sb.append(" CID : " + info1.getCid()); // 取出当前邻区的CID
            sb.append(" BSSS : " + (-113 + 2 * info1.getRssi()) + "\n"); // 获取邻区基站信号强度
        }
        Log.i(TAG, " 获取邻区基站信息:" + sb.toString());

        // 获取全部基站信息
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> mInfos = mTelephonyManager.getAllCellInfo();
            StringBuffer sb2 = new StringBuffer("总数2 : " + mInfos.size() + "\n");
            for (CellInfo info : mInfos) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) info;
                CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                int strength = cellSignalStrengthCdma.getCdmaDbm();
                int cid = cellIdentityCdma.getBasestationId();
                // 处理 strength和id数据
                sb2.append(" strength : " + strength);
                sb2.append(" cid : " + cid);
                sb2.append(" longitude : " + cellIdentityCdma.getLongitude());
                sb2.append(" latitude : " + cellIdentityCdma.getLatitude());
                sb2.append(" toString : " + cellIdentityCdma.toString());
            }
            Log.i(TAG, " 获取全部基站信息:" + sb2.toString());
        }

        // 基站信息监听
//        mTelephonyManager.listen(new PhoneStateListener() {
//            @Override
//            public void onCellLocationChanged(CellLocation location) {
//            }
//
//            @Override
//            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//            }
//
//            @Override
//            public void onCellInfoChanged(List<CellInfo> cellInfo) {
//            }
//        }, PhoneStateListener.LISTEN_CELL_LOCATION); //注册监听器，设定不同的监听类型


        // TODO: 根据基站信息获取位置
    }

    /**
     * 获取JSON形式的基站信息
     *
     * @param mcc 移动国家代码（中国的为460）
     * @param mnc 移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
     * @param lac 位置区域码
     * @param cid 基站编号
     * @return json
     * @throws JSONException
     */
    private String getJsonCellPos(int mcc, int mnc, int lac, int cid) throws JSONException {
        JSONObject jsonCellPos = new JSONObject();
        jsonCellPos.put("version", "1.1.0");
        jsonCellPos.put("host", "maps.google.com");

        JSONArray array = new JSONArray();
        JSONObject json1 = new JSONObject();
        json1.put("location_area_code", "" + lac + "");
        json1.put("mobile_country_code", "" + mcc + "");
        json1.put("mobile_network_code", "" + mnc + "");
        json1.put("age", 0);
        json1.put("cell_id", "" + cid + "");
        array.put(json1);

        jsonCellPos.put("cell_towers", array);
        return jsonCellPos.toString();
    }


    private static LocationManager gpsLocationManager;
    private static LocationManager networkLocationManager;
    private static final int MINTIME = 2000;
    private static final int MININSTANCE = 2;
    private Location lastLocation = null;

    /**
     * GPS a-gps 定位
     * http://hack-zhang.iteye.com/blog/1872149
     * http://technicalsearch.iteye.com/blog/2111417
     *
     * @param context
     */
    public static void gpsLocation(Context context) {
        // Gps 定位
        gpsLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location gpsLocation = gpsLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            gpsLocationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
//                @Override
//                public void onStarted() {
//                    super.onStarted();
//                }
//
//                @Override
//                public void onSatelliteStatusChanged(GnssStatus status) {
//                    super.onSatelliteStatusChanged(status);
////                    status.getSatelliteCount(); // 获取卫星数量
//                }
//            });
//        }
        gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINTIME, MININSTANCE, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
        // 基站定位
        networkLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        Location networkLocation = gpsLocationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        networkLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, MINTIME, MININSTANCE,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
    }

    /**
     * wifi 定位，ip 辅助定位？
     * http://blog.csdn.net/lvshaorong/article/details/51635441
     */
    public static void wifiLocation() {

    }
}
