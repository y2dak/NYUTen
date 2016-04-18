package com.nyuten.nyuten.Model;

import android.os.Parcel;
import android.os.Parcelable;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

public class mLocation implements Parcelable {

    private String Location;
    double Lat;
    double Lng;

    /**
     * Parcelable creator. Do not modify this function.
     */
    public static final Parcelable.Creator<mLocation> CREATOR = new Parcelable.Creator<mLocation>() {
        public mLocation createFromParcel(Parcel p) {
            return new mLocation(p);
        }

        public mLocation[] newArray(int size) {
            return new mLocation[size];
        }
    };

    /**
     * Create a Trip model object from a Parcel. This
     * function is called via the Parcelable creator.
     *
     * @param p The Parcel used to populate the
     * Model fields.
     */
    public mLocation(Parcel p) {

        Location =p.readString();

        Lat = p.readDouble();
        Lng =p. readDouble();
    }

    /**
     * Create a Trip model object from arguments
     *
     * @param name  Add arbitrary number of arguments to
     * instantiate Trip class based on member variables.
     */
    public mLocation(String Location,double Lat, double Lng)
    {
        this.Location = Location;

        this.Lat= Lat;
        this.Lng = Lng;
    }
    public mLocation()
    {
        this.Location = "";
        this.Lng =0;
        this.Lat =0;
    }
    public void setmLocation(mLocation mLoc)
    {
        this.Location= mLoc.getLocation();
        this.Lat =mLoc.getLat();
        this.Lng=mLoc.getLng();

    }

    public String getLocation()
    {
        return Location;
    }
    public double getLat(){return Lat;}
    public double getLng() {return Lng;}
    /**
     * Serialize Trip object by using writeToParcel. 
     * This function is automatically called by the
     * system when the object is serialized.
     *
     * @param dest Parcel object that gets written on 
     * serialization. Use functions to write out the
     * object stored via your member variables. 
     *
     * @param flags Additional flags about how the object 
     * should be written. May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
     * In our case, you should be just passing 0.
     */

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO - fill in here
        dest.writeString(Location);

        dest.writeDouble(Lat);
        dest.writeDouble(Lng);

    }
    /**
     * Feel free to add additional functions as necessary below.
     */

    /**
     * Do not implement
     */
    @Override
    public int describeContents() {
        // Do not implement!
        return 0;
    }

    @Override
    public String toString()
    {
        return "Location: " + this.Location +
                "\nlat: " +this.Lat
                +"\nlng: " + this.Lng
                ;

    }

    public void setmLocation(String Location, double Lat,double Lng)
    {
        this.Location = Location;
        this.Lat= Lat;
        this.Lng = Lng;

    }

}
