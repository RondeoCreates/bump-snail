package com.rondeo.bump.util;

import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network.Account;
import com.rondeo.bump.util.Network.MatchInfo;
import com.rondeo.bump.util.Network.Position;

public abstract class DatabaseController extends MusicController implements DBInterface {
    Kryo kryo;
    public Client client;
    public int opponentId = -1;
    public MatchInfoController matchInfoController;

    public DatabaseController( Client client, int opponentId, final MatchInfoController matchInfoController ) throws IOException {
        this.client = client;
        this.opponentId = opponentId;
        this.matchInfoController = matchInfoController;
        
        kryo = client.getKryo();
        Network.register( kryo );

        client.addListener( new Listener() {
            public void received( Connection connection, Object object) {
                if( object instanceof Position ) {
                    Position position = (Position) object;
                    placeOpponent( position.x, position.y, position.cardIndex );
                }
                if( object instanceof MatchInfo ) {
                    MatchInfo info = (MatchInfo) object;
                    updateOpponentInfo( info.points );
                    //setOpponentInfo( info.username, info.overallPoints );
                }
                if( object instanceof Account ) {
                    Account account = (Account) object;
                    if( account.ID != 0 ) {
                        matchInfoController.overallPoints = account.points;
                        matchInfoController.username = account.username;
                        //setMyInfo( account.username, account.points );
                    } else { 
                        // force log out
                    }
                }
            };
        } );
    }

    public void sendPosition( float x, float y, int index ) {};

    public void updateOpponentInfo( int points ) {};
    
}
