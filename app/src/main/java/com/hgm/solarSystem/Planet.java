package com.hgm.solarSystem;

public class Planet extends Body{
    Planet( int objectID, String name)
    {
        //  TODO(Madalin): pass context
        super(null, objectID, name);

        position = new V3( 0.0d, 0.0d, 0.0d );
    }

    public void getPosition()
    {
        super.getPosition();
    }
}
