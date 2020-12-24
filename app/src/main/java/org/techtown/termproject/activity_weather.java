package org.techtown.termproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.DialogAction;
import com.androdocs.httprequest.HttpRequest;
import com.bumptech.glide.Glide;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.termproject.data.GpsTracker;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import static android.content.Context.LOCATION_SERVICE;

public class activity_weather extends Fragment implements View.OnClickListener {

    //날씨 관련 변수
    TextView tvUpdated, tvStatus, tvTemp, tvTempMin, tvTempMax; //업데이트 시간, 날씨상태, 기온, 최저온도, 최고온도 변수
    String API = "df0d3dcbd140d2f8b42bd412b48467df"; //openweatherMap에서 받은 api이다.
    TextView tvDay; //달, 일, 요일 변수
    static double LON, LAT; //위도 경도를 담을 변수
    int getTimes; //18시 이후일 때 배경색/이미지 변경을 위한 변수
    ImageView weatherImage; //OpenWeatherMap에서 가져오는 날씨 이미지 담을 변수
    ImageButton imgBtnRefresh; //업데이트 새로고침 버튼 변수

    //Gps
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2000;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    static TextView tvGPS; //gps로 받은 주소값 변수

    //네트워크
    private boolean isConnected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //현재 시간 체크
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        String getTime = simpleDateFormat.format(mDate);
        getTimes = Integer.parseInt(getTime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.weather, container, false);

        //gps
        tvGPS = v.findViewById(R.id.tvGPS);

        //날씨
        tvUpdated = v.findViewById(R.id.tvUpdated);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvTemp = v.findViewById(R.id.tvTemp);
        tvTempMin = v.findViewById(R.id.tvTempMin);
        tvTempMax = v.findViewById(R.id.tvTempMax);
        tvDay = v.findViewById(R.id.tvDay);
        imgBtnRefresh = v.findViewById(R.id.imgBtnRefresh);

        //달,월,일 가져오는 함수
        currentMonth();

        //Gps
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        imgBtnRefresh.setOnClickListener(this);
        refreshGPSWeather();
        return v;
    }

    //새로고침 버튼 클릭 시 현재 위치값을 받아 현재 위치에 대한 날씨를 알려준다.
    @Override
    public void onClick(View view) {
        refreshGPSWeather();
    }

    private void refreshGPSWeather() {
        gpsTracker = new GpsTracker(getActivity());

        double longitude = gpsTracker.getLongitude();
        double latitude = gpsTracker.getLatitude();

        if (longitude == 0.0 && latitude == 0.0) {
            Toast.makeText(getActivity(), "날씨를 불러올 수 없습니다\n" + "위치 설정과 네트워크 연결을 확인해주세요" +
                    "", Toast.LENGTH_LONG).show();
        } else {
            String address = getCurrentAddress(latitude, longitude);
            tvGPS.setText(address);
            LAT = latitude;
            LON = longitude;
            new weatherTask().execute();
        }
    }

    //날씨
    public class weatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LON + "&units=metric&appid=" + API);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            //네트워크 연결 오류 시 앱 꺼짐 현상 막음
            if (result == null) {
                isConnected = true;
                if (isConnected) {
                    Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                isConnected = false;
            }

            try {
                JSONObject jsonObj = new JSONObject(result);
                // main에 온도와, 기압, 습도, 최저기온, 최고기온
                JSONObject main = jsonObj.getJSONObject("main");
                // sys에 국가와 일출시간, 일몰시간
                JSONObject sys = jsonObj.getJSONObject("sys");
                // wind에 풍속
                JSONObject wind = jsonObj.getJSONObject("wind");
                // weather에 날씨정보(ex.구름 많음 등등)
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                // coord에 위도와 경도 값이 들어있다.
                JSONObject coord = jsonObj.getJSONObject("coord");

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "업데이트 : " + new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.KOREA).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "최저 기온: " + main.getString("temp_min") + "°C";
                String tempMax = "최고 기온: " + main.getString("temp_max") + "°C";

                //날씨 코드 값 가져오기
                int id = weather.getInt("id");

                tvUpdated.setText(updatedAtText);
                tvTemp.setText(temp);
                tvTempMin.setText(tempMin);
                tvTempMax.setText(tempMax);

                //날씨 아이디 값에 맞는 MaterialStyledDialog 출력 및 배경 색
                switch (id) {
                    //뇌우
                    case 200: case 201: case 202: case 210: case 211: case 212: case 221: case 230: case 231: case 232:
                        tvStatus.setText("뇌우");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("뇌우").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("뇌우").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;

                    //비
                    case 500: case 501: case 502: case 503: case 504: case 511: case 520: case 521: case 522: case 531: case 300: case 301: case 302: case 310: case 311: case 312: case 313: case 314: case 321:
                        tvStatus.setText("비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;

                    //눈
                    case 600: case 601: case 602: case 611: case 612: case 615: case 616: case 620: case 621: case 622:
                        tvStatus.setText("눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈").setDescription("창문을 꼭 닫고, 우산 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 701:
                        tvStatus.setText("옅은 안개");
                        break;
                    case 711:
                        tvStatus.setText("연기");
                        break;
                    case 721:
                        tvStatus.setText("실안개   ");
                        break;
                    case 731: case 741: case 751: case 761: case 762:
                        tvStatus.setText("먼지");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("먼지").setDescription("마스크를 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("먼지").setDescription("마스크를 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 771:
                        tvStatus.setText("돌풍");
                        break;
                    case 781:
                        tvStatus.setText("토네이도");
                        break;
                    case 800:
                        tvStatus.setText("맑은 하늘");
                        break;
                    case 801: case 802: case 803: case 804:
                        tvStatus.setText("구름 낀 하늘");
                        break;
                    case 900:
                        tvStatus.setText("토네이도");
                        break;
                    case 902:
                        tvStatus.setText("   허리케인   ");
                        break;
                    case 903:
                        tvStatus.setText("한랭");
                        break;
                    case 904:
                        tvStatus.setText("고온");
                        break;
                    case 905:
                        tvStatus.setText("많은 바람");
                        break;
                    case 906:
                        tvStatus.setText("우박");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("우박").setDescription("창문을 꼭 닫고 우산을 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_hail).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("우박").setDescription("창문을 꼭 닫고 우산을 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_hail).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 951:
                        tvStatus.setText("바람 없는 하늘");
                        break;
                    case 952: case 953: case 954: case 955: case 956: case 957: case 958: case 959:
                        tvStatus.setText("바람 부는 하늘");
                    case 960: case 961:
                        tvStatus.setText("폭풍");
                        break;
                    case 962:
                        tvStatus.setText("   허리케인   ");
                        break;
                    default:
                        tvStatus.setText("날씨를 가져올 수 없습니다.");
                }
            } catch (JSONException e) {
            }
        }
    }

    /**GPS************************
     */

    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 받는다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있다면
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
//                    finish();
                } else {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    void checkRunTimePermission() {

        // 위치 퍼미션을 가지고 있는지 체크.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 이미 퍼미션을 가지고 있다면 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식한다.
            // 위치 값을 가져올 수 있음

        } else {  //퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다.

            // 사용자가 퍼미션 거부를 한 적이 있는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {
        //GPS를 주소로 변환한다.
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses;
        try {
            //현재 위치 주소를 가져온다.
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제 발생 시
            Toast.makeText(getActivity(), "위치 서비스 사용불가", Toast.LENGTH_SHORT).show();
            return "네트워크 문제로 주소를 불러올 수 없습니다.";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_SHORT).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return "주소를 찾을 수 없습니다.";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0) + "\n";
    }

    /**
     * gps가 비활성화 되어있을 경우 gps활성화 할 수 있는 설정창으로 이동한다.
     */
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스가 비활성화되어있습니다.");
        builder.setIcon(R.drawable.location);
        builder.setMessage("TODO 어플을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 활성화하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("활성화하러 가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사한다.
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    /**
     * GPS >> checkLocationServicesStatus()
     * <p>
     * 현재 위치 값을 가지고 오기 위한 객체 선언한다.
     * LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     * <p>
     * GPS로부터 현재 위치 값을 가져온다.
     * LocationManager.GPS_PROVIDER
     * * <p>
     * 기지국으로부터 현재 위치 값을 가져온다.
     * LocationManager.NETWORK_PROVIDER
     */
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //달,월,일 가져오는 함수
    //SlideUpPanel Layout에 XXXX년 XX월 XX일이 표시된다.
    private void currentMonth() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sDay = new SimpleDateFormat("EE", Locale.getDefault());
        SimpleDateFormat sDate = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sMonth = new SimpleDateFormat("MM", Locale.getDefault());

        String month = sMonth.format(currentTime);
        String date = sDate.format(currentTime);
        String day = sDay.format(currentTime);

        tvDay.setText(month + "월 " + date + "일 " + day + "요일");
    }
}