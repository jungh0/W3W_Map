package com.jungh0.w3w_map.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jungh0.w3w_map.Collection;
import com.jungh0.w3w_map.R;
import com.pepperonas.materialdialog.MaterialDialog;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.aviran.cookiebar2.CookieBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.jungh0.w3w_map.Collection.ToastMD;

public class MapsActivity extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{
    private GoogleMap mGoogleMap = null;
    private static final String TAG = "w3w_";
    String w3w_apikey = "CD39RHMH";
    Handler mHandler = null;

    double last_long, last_lati;
    TextView word_3, country, country2, nearest, nearest2, longitude_t, latitude_t, share_code;
    ViewGroup rootView;

    RelativeLayout share_hide;

    static Boolean is_seting = false; // 위치 공유할 때 온오프
    static int is_get_auto = -1; //현재 위치 계속 전송 온오프 -1안함 0한번만 1계속

    //static Boolean is_geting = false; // 위치 추적할 때 온오프
    //static int get_location_first_move = 0; //위치 추적할 때 처음 한번은 카메라 이동하고 나중에 안함  //0은 한번만 1은 안함 2는 계속


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        rootView = (ViewGroup) inflater.inflate(R.layout.activity_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RelativeLayout card_up = (RelativeLayout) rootView.findViewById(R.id.card_up);

        LayoutInflater inflater2 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater2.inflate(R.layout.relative_card_up, card_up, true);
        word_3 = (TextView) card_up.findViewById(R.id.w3w_t);
        country = (TextView) card_up.findViewById(R.id.nearestPlace_t);
        country2 = (TextView) card_up.findViewById(R.id.nearestPlace_t2);
        nearest = (TextView) card_up.findViewById(R.id.country_t);
        nearest2 = (TextView) card_up.findViewById(R.id.country_t2);
        longitude_t = (TextView) card_up.findViewById(R.id.longitude_t);
        latitude_t = (TextView) card_up.findViewById(R.id.latitude_t);
        share_hide = (RelativeLayout) card_up.findViewById(R.id.share_view);
        share_code = (TextView) card_up.findViewById(R.id.share_code);

        //슬라이드 업 패널 리스너
        final FrameLayout willgone = (FrameLayout) card_up.findViewById(R.id.willgone);
        willgone.setVisibility(View.GONE);

        final SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        final Button upbtn = (Button) card_up.findViewById(R.id.upbtn);
        upbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLayout.getPanelState().toString().equals("EXPANDED")) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
                String tmp = newState.toString();
                String tmp2 = previousState.toString();
                if (tmp.equals("DRAGGING")) {
                    if (tmp2.equals("EXPANDED"))
                        willgone.setVisibility(View.GONE);
                } else if (tmp.equals("EXPANDED")) {
                    willgone.setVisibility(View.VISIBLE);
                } else if (tmp.equals("COLLAPSED")) {
                    willgone.setVisibility(View.GONE);
                }
            }
        });
        mLayout.setShadowHeight(0);
        mLayout.setOverlayed(true);

        FloatingActionButton copy_word = rootView.findViewById(R.id.fab2);
        copy_word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection.clip_copy(getActivity(), getContext(), word_3.getText().toString());
            }
        });

        Button share_code_sns = rootView.findViewById(R.id.share_sns);
        share_code_sns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection.share_sns(getActivity(), "http://thousand419.dothome.co.kr/deeplink.php?key=" + MainActivity.set__id.toString());
            }
        });

        fab_each(rootView, R.id.sms);
        fab_each(rootView, R.id.kakao);
        fab_each(rootView, R.id.hangout);

        keyboard_enter();

        //2초마다 쓰레드 돌면서 인터넷이 연결되어있는지 확인하고 끊기면 알림이 뜸
        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        if (!Collection.isNetworkAvailable(getContext())) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastMD(getContext(), "인터넷 연결에 실패 했습니다.", 3);
                                }
                            });
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        if (is_get_auto == 1) {
                            startLocationUpdates(true);
                        } else if (is_get_auto == 0) {
                            startLocationUpdates(true);
                            is_get_auto = -1;
                        } else {
                            if (share_hide.getVisibility() == View.VISIBLE && !is_seting) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        share_hide.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                        if (GetLocationService.is_geting) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Double get_lati2 =  GetLocationService.get_lati;
                                    Double get_long2 =  GetLocationService.get_long;
                                    if (GetLocationService.get_location_first_move == 0) {
                                        moveMap(mGoogleMap, get_lati2, get_long2, 15);
                                        GetLocationService.get_location_first_move = 1;
                                    } else if (GetLocationService.get_location_first_move == 2) {
                                        moveMap(mGoogleMap, get_lati2, get_long2, 15);
                                    }
                                    markerMap(mGoogleMap, get_lati2, get_long2);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        Collection.ToastMD(getContext(), "추적 오류", 3);
                    }
                }
            }
        }).start();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //CookieBar.dismiss(MainActivity.main_act);
        //is_seting = false;
    }

    private void keyboard_enter(){
        //검색 엔터키 눌렀을 때 반응하도록
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        switch (actionId) {
                            case EditorInfo.IME_ACTION_SEARCH:
                                keyboard_search();
                                break;
                            default:
                                return false;
                        }
                        return true;
                    }
                });
            }
        }, 1000);
    }
    //공유 fab 버튼 함수
    public void fab_each(ViewGroup aa, int find_id) {
        FloatingActionButton tmp = aa.findViewById(find_id);
        tmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection.share_sns(getActivity(), word_3.getText().toString());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.d(TAG, "onMapReady :");
        mGoogleMap = googleMap;

        //위치 권한
        int permission = Collection.location_permission(getContext(), getActivity());
        if (permission == 1) {
            startLocationUpdates(true);
        } else {
            setDefaultLocation();
        }

        //터치 했을 때 핀 생김
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (!GetLocationService.is_geting && is_get_auto != 1) {
                    markerMap(mGoogleMap, point.latitude, point.longitude);
                } else {
                    Collection.ToastMD(getContext(), "위치 공유 서비스중에는 핀을 수정 할 수 없습니다.", 4);
                }
            }
        });

        //현재 위치 버튼
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!GetLocationService.is_geting && is_get_auto != 1) {
                    startLocationUpdates(true);
                } else {
                    Collection.ToastMD(getContext(), "위치 공유 서비스중에는 핀을 수정 할 수 없습니다.", 4);
                }
            }
        });

        //확대
        FloatingActionButton up = rootView.findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_get_auto != 1) {
                    Float zoom = Float.parseFloat(mGoogleMap.getCameraPosition().toString().split("zoom=")[1].split(",")[0]) + 1;
                    moveMap(mGoogleMap, last_lati, last_long, zoom);
                } else {
                    Collection.ToastMD(getContext(), "위치 공유 서비스중에는 제한됩니다.", 4);
                }
            }
        });

        //축소
        FloatingActionButton down = rootView.findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (is_get_auto != 1) {
                    Float zoom = Float.parseFloat(mGoogleMap.getCameraPosition().toString().split("zoom=")[1].split(",")[0]) - 1;
                    moveMap(mGoogleMap, last_lati, last_long, zoom);
                } else {
                    Collection.ToastMD(getContext(), "위치 공유 서비스중에는 재한됩니다.", 4);
                }

            }
        });
    }

    //인터넷 연결 없을 시 기본 서울로 지정
    public void setDefaultLocation() {
        markerMap(mGoogleMap, 37.450989, 127.127156);
        moveMap(mGoogleMap, 37.450989, 127.127156, 15);
    }

    //사용자 현재 위치
    private void startLocationUpdates(final boolean move) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastMD(getContext(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", 3);
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Double latitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    markerMap(mGoogleMap, latitude, longitude);
                    if (move)
                        moveMap(mGoogleMap, latitude, longitude, 15);
                } else {
                    ToastMD(getContext(), "현재위치를 찾을 수 없습니다.", 3);
                    setDefaultLocation();
                }
            }
        });
    }

    //좌표로 카메라 이동
    public void moveMap(GoogleMap gMap, double latitude, double longitude, float size) {
        //Log.v(TAG, "mapMoved: " + gMap);
        LatLng latlng = new LatLng(latitude, longitude);
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latlng, size);
        try {
            gMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin2", 100, 100))));
        } catch (Exception e) {
            gMap.addMarker(new MarkerOptions().position(latlng));
        }
        gMap.moveCamera(cu);
    }

    private Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    //좌표에 핀 찍기
    public void markerMap(GoogleMap gMap, final double latitude, final double longitude) {
        last_long = longitude;
        last_lati = latitude;
        //Log.v(TAG, "mapMarked: " + gMap);
        MarkerOptions mOptions = new MarkerOptions();
        //mOptions.title("마커 좌표");//mOptions.snippet(latitude + ", " + longitude);
        try {
            mOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("pin2", 100, 100)));
        } catch (Exception e) {
        }
        mOptions.position(new LatLng(latitude, longitude));

        mGoogleMap.clear();
        mGoogleMap.addMarker(mOptions);

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String get_w3w = Collection.gethttp(getContext(),
                        "https://api.what3words.com/v3/convert-to-3wa?coordinates="
                                + latitude + "%2C" + longitude + "&key=" + w3w_apikey + "&language=ko");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Collection.ToastMD(getContext(), get_w3w, 1);
                            JSONObject jobj = new JSONObject(get_w3w);
                            JSONObject coordinates = jobj.getJSONObject("coordinates");
                            String words = jobj.getString("words");
                            String country_w = jobj.getString("country");
                            String nearestPlace = jobj.getString("nearestPlace");
                            String lng = coordinates.getString("lng");
                            String lat = coordinates.getString("lat");

                            word_3.setText(words);
                            country.setText(country_w);
                            country2.setText(country_w);
                            nearest.setText(nearestPlace);
                            nearest2.setText(nearestPlace);
                            longitude_t.setText(lng);
                            latitude_t.setText(lat);
                        } catch (JSONException e) {
                            word_3.setText("---.---.---");
                            e.printStackTrace();
                        }

                        if (is_seting) {
                            share_hide.setVisibility(View.VISIBLE);
                            share_code.setText("공유코드 : " + MainActivity.set__id);
                        }
                    }
                });
                if (is_seting) {
                    Collection.gethttp(getContext(), "http://thousand419.dothome.co.kr/InsertNew.php?id="
                            + MainActivity.set__id + "&x=" + longitude + "&y=" + latitude);
                }
            }
        }).start();
    }

    public void keyboard_search() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(MainActivity.search, 0);
        imm.hideSoftInputFromWindow(MainActivity.search.getWindowToken(), 0);

        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String get_w3w = Collection.gethttp(getContext(), "https://api.what3words.com/v3/autosuggest?key=" + w3w_apikey + "&input=" + MainActivity.search.getText().toString() + "&n-results=5");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jobj = new JSONObject(get_w3w);
                            JSONArray suggestions = jobj.getJSONArray("suggestions");
                            String[] get_w = new String[suggestions.length() ];
                            String[] get_d = new String[suggestions.length() ];
                            for (int i = 0; i < suggestions.length() ; i++) {
                                get_w[i] = suggestions.getJSONObject(i).getString("words");
                                get_d[i] = get_w[i] + " - " + suggestions.getJSONObject(i).getString("nearestPlace");
                            }
                            showMaterialDialogList(get_w, get_d);
                        } catch (Exception e) {
                            ToastMD(getContext(), "오류", 3);
                        }
                    }
                });
            }
        }).start();
    }

    private void showMaterialDialogList(final String[] list, final String[] list2) {
        new MaterialDialog.Builder(getActivity())
                .title("선택하세요")
                .negativeText("취소")
                .negativeColor(R.color.red)
                .listItems(true, list2)
                .itemSelectedListener(new MaterialDialog.ItemSelectedListener() {
                    @Override
                    public void onSelected(View view, int position, long id) {
                        super.onSelected(view, position, id);
                    }
                })
                .itemClickListener(new MaterialDialog.ItemClickListener() {
                    @Override
                    public void onClick(View v, final int position, long id) {
                        super.onClick(v, position, id);
                        //ToastMD(getApplicationContext(), "onClick (" + list[position] + ")",3);
                        mHandler = new Handler();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String get_w3w = Collection.gethttp(getContext(), "https://api.what3words.com/v3/convert-to-coordinates?key=" + w3w_apikey + "&words=" + list[position] + "&format=json");
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jobj = new JSONObject(get_w3w);
                                            JSONObject coordinates = jobj.getJSONObject("coordinates");
                                            Double lng = coordinates.getDouble("lng");
                                            Double lat = coordinates.getDouble("lat");
                                            if (lng != 0.0 && lat != 0.0) {
                                                markerMap(mGoogleMap, lat, lng);
                                                moveMap(mGoogleMap, lat, lng, 15);
                                            }
                                        } catch (JSONException e) {
                                            ToastMD(getContext(), "오류", 3);
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                })
                .buttonCallback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .show();
    }


}

