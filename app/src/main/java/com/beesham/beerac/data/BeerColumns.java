package com.beesham.beerac.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Beesham on 1/17/2017.
 */

public class BeerColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String BEERID = "beer_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String NAME = "name";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String DESCRIPTION = "description";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String FOODPARINGS = "food_parings";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String ISORGANIC = "is_organic";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String LABELS = "labels";
    @DataType(DataType.Type.TEXT)
    public static final String IMAGEURLICON = "image_url_icon";
    @DataType(DataType.Type.TEXT)
    public static final String IMAGEURLMEDIUM = "image_url_medium";
    @DataType(DataType.Type.TEXT)
    public static final String IMAGEURLLARGE = "image_url_large";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String YEAR = "year";
    
}
