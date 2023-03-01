package com.rondeo.bump.util;

import java.io.IOException;

import com.badlogic.gdx.ScreenAdapter;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network.FindMatch;
import com.rondeo.bump.util.Network.Position;

public abstract class DatabaseController extends ScreenAdapter implements DBInterface {
    Kryo kryo;
    public Client client;
    public int opponentId = -1;

    public DatabaseController() throws IOException {
        client = new Client();
        client.start();
        //client.discoverHost( 54777, 5000 );
        //client.connect( 5000, "bump-snail-server.rosenietalibo.repl.co", 54555 );
        client.connect( 5000, client.discoverHost( 80, 5000 ), 80, 80 );

        kryo = client.getKryo();
        Network.register( kryo );

        FindMatch findMatch = new FindMatch();
        findMatch.connectionId = client.getID();
        System.out.println( client.getID() );
        client.sendTCP( findMatch );

        client.addListener( new Listener() {
            public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                if( object instanceof FindMatch ) {
                    FindMatch findMatch = (FindMatch) object;
                    opponentId = findMatch.connectionId;
                    System.out.println( findMatch.connectionId );
                }
                if( object instanceof Position ) {
                    Position position = (Position) object;
                    placeOpponent( position.x, position.y, position.cardIndex );
                }
            };
        } );
    }

    public void sendPosition( float x, float y, int index ) {}
    
}
