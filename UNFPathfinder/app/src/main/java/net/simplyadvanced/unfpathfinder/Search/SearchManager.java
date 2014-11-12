package net.simplyadvanced.unfpathfinder.Search;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by Spence on 11/12/2014.
 */
public class SearchManager {

    //Decs
    private static SearchManager mSearchManager;
    private static GoogleMap mMap;

    private SearchManager(){

    }

    public static SearchManager getInstance(){
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
        }
        return mSearchManager;
    }

    public static SearchManager getInstance(GoogleMap map){
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
            mMap = map;
        }
        return mSearchManager;
    }


}
