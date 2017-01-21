package com.beesham.beerac.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Beesham on 1/17/2017.
 */

@Database(version = BeerDatabase.VERSION)
public class BeerDatabase {
    public static final int VERSION = 1;

    @Table(BeerColumns.class) public static final String BEERS = "beers";
}
