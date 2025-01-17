package com.example.suvasam;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.suvasam.database.DonateFirebase;
import com.example.suvasam.model.Donate;
import com.example.suvasam.model.Events;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DonateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DonateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DonateFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
     GoogleMap map;
    HashMap<String, Donate> mAreaList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FloatingActionButton donateBtn;
    FloatingActionButton resetBtn;

    private OnFragmentInteractionListener mListener;

    public DonateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DonateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DonateFragment newInstance(String param1, String param2) {
        DonateFragment fragment = new DonateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(savedInstanceState == null) {
            mAreaList = fetchDataFromFirebase();
        } else {
            Log.e("SCreen Rotated", "SvaedInstance not null");
            mAreaList = (HashMap<String, Donate>) savedInstanceState.getSerializable("areaList");
        }
        View view =  inflater.inflate(R.layout.fragment_donate, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        donateBtn = view.findViewById(R.id.donateBtn);
        resetBtn = view.findViewById(R.id.resetBtn);
        //if(mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mapFragment).commit();

        //}
        mapFragment.getMapAsync(this);
        updateMap(mAreaList);
        donateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mAreaList = fetchDataFromFirebase();
                Log.e("Donate Dial Frag", String.valueOf(mAreaList.size()));
                displayDonateFrag();

            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Area Reset", Toast.LENGTH_LONG).show();
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference();
                for(HashMap.Entry<String, Donate> area : mAreaList.entrySet()) {
                    ref.child("area").child(String.valueOf(area.getValue().getId())).child("donated").setValue("no");
                }
            }
        });

        return view;
    }

    public void displayDonateFrag(){
        Log.e("Display Donate Frag", "Displayed");
        if(checkDonatePlantPresent(mAreaList)) {
            DonateDialogFragment dialogFragment = new DonateDialogFragment();
            Bundle bundle = new Bundle();
            Log.e("Donate Dial Frag", String.valueOf(mAreaList.size()));
            bundle.putSerializable("areaList", mAreaList);
            dialogFragment.setArguments(bundle);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.addToBackStack(null).replace(R.id.donateFrameLayout, dialogFragment);
            ft.commit();
        } else {
            Toast.makeText(getContext(), "Madurai Made Green, All Plants have been donated",
                    Toast.LENGTH_LONG).show();
        }
    }

    public boolean checkDonatePlantPresent(HashMap<String, Donate> areaList) {
        Log.e("CheckDonate", String.valueOf(areaList.size()));
        boolean donationRequired = false;
        for(Map.Entry<String, Donate> area : areaList.entrySet()) {
            Log.e("CheckDonate", area.getValue().getDonated());
            if(area.getValue().getDonated().contains("no")){
                Log.e("Check area Donate", area.getValue().getName());
                return true;
            }
        }
        return donationRequired;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
//            mAreaList = savedInstanceState.getParcelable("areaList");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("DONATE FRAG", "SAVE INSTANCE area list size"+mAreaList.size());
        outState.putSerializable("areaList", mAreaList);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            LatLng madurai = new LatLng(9.922939, 78.114941);

            LatLng maduraiLeft = new LatLng(9.924006, 78.064849);
            LatLng maduraiRight = new LatLng(9.929358, 78.219317);


            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(maduraiLeft);
            builder.include(maduraiRight);

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            int padding = (int) (width * 0.20);

            map.setLatLngBoundsForCameraTarget(bounds);
        Log.e("Donate Fragment LAT", "Inside LAT LNG");


            map.addMarker(new MarkerOptions().position(madurai).title("Madurai"));
            map.addMarker(new MarkerOptions().position(maduraiLeft).title("MaduraiLeft"));
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));

            map.setMinZoomPreference(map.getCameraPosition().zoom);

            if(!mAreaList.isEmpty()) {
                Log.e("Inside MapOnReady", String.valueOf(mAreaList.size()));
                updateMap(mAreaList);
            }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public HashMap<String, Donate> fetchDataFromFirebase() {
        mAreaList = new HashMap<>();
        //final ArrayList<Donate> areaList = new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        ref.child("area").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("Count", ""+dataSnapshot.getChildrenCount());
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Donate areaDetails = postSnapshot.getValue(Donate.class);
                    mAreaList.put(String.valueOf(areaDetails.getId()), areaDetails);
                    Log.e("Get Data", areaDetails.getName());
                }
                updateMap(mAreaList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        return mAreaList;
    }

    private void updateMap(HashMap<String, Donate> areaList) {
        for(Map.Entry<String, Donate> area : areaList.entrySet()) {
            Log.e("Donate Fragment LAT", String.valueOf(area.getValue().getLat()));
            Log.e("Donate Fragment LNG", String.valueOf(area.getValue().getLng()));
            LatLng areaPosition = new LatLng(area.getValue().getLat(), area.getValue().getLng());
            if(map != null) {
                if (area.getValue().getDonated().equals("yes")) {
                    map.addMarker(new MarkerOptions().position(areaPosition).title(area.getValue().getName()).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                } else {
                    map.addMarker(new MarkerOptions().position(areaPosition).title(area.getValue().getName()).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }
            }
        }
    }


}
