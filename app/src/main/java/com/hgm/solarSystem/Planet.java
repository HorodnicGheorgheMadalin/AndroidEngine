package com.hgm.solarSystem;

import android.content.Context;

public class Planet extends Body{
    Planet(Context context, int objectID, String name)
    {
        //  TODO(Madalin): pass context
        super(context, objectID, name);

        position = new V3( -5.0d, -5.0d, -5.0d );
    }

    public void getPosition()
    {
        super.getPosition();
    }
}
