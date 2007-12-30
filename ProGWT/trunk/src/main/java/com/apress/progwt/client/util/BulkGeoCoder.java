package com.apress.progwt.client.util;

import java.util.List;

import com.apress.progwt.client.domain.HasAddress;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Timer;

public class BulkGeoCoder extends Timer {

    private List<HasAddress> schools;
    private Geocoder geocoder;
    private String tablename;

    public BulkGeoCoder(List<HasAddress> schools, String tablename) {
        this.schools = schools;
        this.tablename = tablename;
        geocoder = new Geocoder();
    }

    @Override
    public void run() {

        final HasAddress school = schools.remove(0);
        if (school == null) {
            System.out.println("Finished");
            cancel();
            return;
        }
        final String full = school.getFullAddress();
        Geocoder geocoder = new Geocoder();
        geocoder.getLatLng("1600 pennsylvania avenue, washington dc",
                new LatLngCallback() {
                    public void onFailure() {
                    }

                    public void onSuccess(LatLng point) {
                    }
                });

        geocoder.getLatLng(full, new LatLngCallback() {
            public void onFailure() {
                System.out
                        .println("UPDATE "
                                + tablename
                                + " SET latitude = '-1',longitude  = '-1' WHERE id ="
                                + school.getId() + " LIMIT 1 ;");
                run();
            }

            public void onSuccess(LatLng point) {

                System.out.println("UPDATE " + tablename
                        + " SET latitude = '" + point.getLatitude()
                        + "',longitude  = '" + point.getLongitude()
                        + "' WHERE id =" + school.getId() + " LIMIT 1 ;");
                run();
            }
        });
    }

}
