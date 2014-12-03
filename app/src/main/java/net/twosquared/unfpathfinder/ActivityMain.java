package net.twosquared.unfpathfinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.twosquared.unfpathfinder.Search.Node;
import net.twosquared.unfpathfinder.Search.Path;
import net.twosquared.unfpathfinder.Search.SearchManager;
import net.twosquared.unfpathfinder.Settings.UserPrefs;

public class ActivityMain extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private SearchManager mSearchManager;
    private UserPrefs mUserPrefs;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activitiy_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case(R.id.search):
                mSearchManager.openSearchMenu();
                return true;
            case(R.id.clear):
                mSearchManager.clearMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

        //Center the map on UNF
        MapCenteringUtils.mapMoveAndZoomTo(mMap, new LatLng(30.268602, -81.507744), 16);
        mSearchManager = SearchManager.getInstance(mMap, getApplicationContext(), this);

        mUserPrefs = UserPrefs.getInstance();

        if(!mUserPrefs.getEulaPref()){
            openEULA();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }

    private void debug(){
        Path path = new Path();
        path.add(new LatLng(30.271483, -81.509146));
        path.add(new LatLng(30.271441, -81.509206));
        path.add(new LatLng(30.271413, -81.509300));
        path.add(new LatLng(30.271343, -81.509472));
        path.add(new LatLng(30.271298, -81.509602));
        path.add(new LatLng(30.271284, -81.509614));
        path.add(new LatLng(30.271270, -81.509663));
        path.add(new LatLng(30.271224, -81.509747));
        path.add(new LatLng(30.271165, -81.509745));
        path.add(new LatLng(30.271113, -81.509756));
        path.add(new LatLng(30.271075, -81.509766));
        path.add(new LatLng(30.271002, -81.509761));
        path.add(new LatLng(30.270968, -81.509761));
        path.add(new LatLng(30.270902, -81.509747));
        path.add(new LatLng(30.270857, -81.509715));
        path.add(new LatLng(30.270811, -81.509680));
        path.add(new LatLng(30.270751, -81.509633));
        path.add(new LatLng(30.270699, -81.509568));
        path.add(new LatLng(30.270662, -81.509523));
        path.add(new LatLng(30.270677, -81.509533));
        path.add(new LatLng(30.270683, -81.509540));
        path.add(new LatLng(30.270648, -81.509523));
        path.add(new LatLng(30.270614, -81.509463));
        path.add(new LatLng(30.270610, -81.509442));
        path.add(new LatLng(30.270534, -81.509370));
        path.add(new LatLng(30.270435, -81.509304));
        path.add(new LatLng(30.270349, -81.509274));
        path.add(new LatLng(30.270252, -81.509244));
        path.add(new LatLng(30.270186, -81.509260));
        path.add(new LatLng(30.270007, -81.509269));
        path.add(new LatLng(30.269871, -81.509288));
        path.add(new LatLng(30.269700, -81.509272));
        path.add(new LatLng(30.269694, -81.509272));
        path.add(new LatLng(30.269688, -81.509279));
        path.add(new LatLng(30.269549, -81.509244));
        path.add(new LatLng(30.269505, -81.509211));
        path.add(new LatLng(30.269459, -81.509104));
        path.add(new LatLng(30.269473, -81.509017));
        Node node = new Node(30.269475, -81.508936);
        node.addAlias("Thomas G Carpenter Library");
        path.add(node);

        mSearchManager.drawPath(path);
    }

    /**Opens End User Licensing Agreements that must be agreed to */
    private void openEULA(){

        if(flag) return;

        flag = true;

        new AlertDialog.Builder(this)
                .setTitle("EULA")
                .setMessage(R.string.eula_agreement)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(1);
                    }
                })
                .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserPrefs.saveEulaPref(true);
                    }
                }).show();
    }

    private void setupListeners(){

        //Get values from EditText fields
        final Button search = (Button) findViewById(R.id.search_button);

        //Opens menu on click
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchManager.openSearchMenu();
            }
        });

        //Changes button color on focus
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                search.setBackgroundResource(R.drawable.ic_search_icon_pressed);
            }
        });

    }
}
