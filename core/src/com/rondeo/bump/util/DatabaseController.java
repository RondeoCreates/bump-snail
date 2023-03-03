package com.rondeo.bump.util;

import java.io.IOException;

import com.badlogic.gdx.ScreenAdapter;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network.Position;

public abstract class DatabaseController extends ScreenAdapter implements DBInterface {
    Kryo kryo;
    public Client client;
    public int opponentId = -1;

    public DatabaseController( Client client, int opponentId ) throws IOException {
        this.client = client;
        this.opponentId = opponentId;
        
        kryo = client.getKryo();
        Network.register( kryo );

        client.addListener( new Listener() {
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                if( object instanceof Position ) {
                    Position position = (Position) object;
                    placeOpponent( position.x, position.y, position.cardIndex );
                }
            };
        } );
    }

    public void sendPosition( float x, float y, int index ) {}
    
}
