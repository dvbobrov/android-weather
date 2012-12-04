package com.dbobrov.android.weather.models;

/**
 * Created with IntelliJ IDEA.
 * User: blackhawk
 * Date: 05.12.12
 * Time: 0:35
 */
public class City {
    public long id;
    public String name, country;

    public City(long id, String name, String country) {
        this.country = country;
        this.id = id;
        this.name = name;
    }
}
