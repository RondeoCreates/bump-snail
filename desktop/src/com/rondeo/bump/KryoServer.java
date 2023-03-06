package com.rondeo.bump;

import java.io.IOException;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.Network.FindMatch;
import com.rondeo.bump.util.Network.MatchDefinition;
import com.rondeo.bump.util.Network.MatchInfo;
import com.rondeo.bump.util.Network.Position;

public class KryoServer {
    Server server;
    Kryo kryo;
    ArrayList<MatchDefinition> matches = new ArrayList<>();

	public KryoServer () throws IOException {
        server = new Server();
        server.start();
        server.bind( 80, 80 );

        kryo = server.getKryo();
        Network.register( kryo );
        
        server.addListener( new Listener() {
            public void received( Connection connection, Object object ) {
                // find match
                if( object instanceof FindMatch ) {
                    boolean connected = false;
                    FindMatch tempMatch;
                    // Iterate through pending matches to find connection
                    for ( MatchDefinition matchDefinition : matches ) {
                        if( matchDefinition.connectionIdA == -1 && matchDefinition.connectionIdB != -1 && matchDefinition.connectionIdB != connection.getID() ) {
                            matchDefinition.connectionIdA = connection.getID();

                            tempMatch = new FindMatch();
                            tempMatch.connectionId = connection.getID();
                            server.sendToTCP( matchDefinition.connectionIdB, tempMatch );

                            tempMatch = new FindMatch();
                            tempMatch.connectionId = matchDefinition.connectionIdB;
                            connection.sendTCP( tempMatch );

                            connected = true;
                            System.out.println( matchDefinition.connectionIdA+"<>"+matchDefinition.connectionIdB );
                            matches.remove( matchDefinition );
                            break;
                        }
                        if( matchDefinition.connectionIdB == -1 && matchDefinition.connectionIdA != -1 && matchDefinition.connectionIdA != connection.getID() ) {
                            matchDefinition.connectionIdB = connection.getID();
                            
                            tempMatch = new FindMatch();
                            tempMatch.connectionId = connection.getID();
                            server.sendToTCP( matchDefinition.connectionIdA, tempMatch );

                            tempMatch = new FindMatch();
                            tempMatch.connectionId = matchDefinition.connectionIdA;
                            connection.sendTCP( tempMatch );

                            connected = true;
                            System.out.println( matchDefinition.connectionIdA+"<>"+matchDefinition.connectionIdB );
                            matches.remove( matchDefinition );
                            break;
                        }
                    }
                    if( !connected ) {
                        matches.add( new MatchDefinition( connection.getID() ) );
                    }
                }

                // game play
                if( object instanceof Position ) {
                    Position position = (Position) object;
                    server.sendToTCP( position.connectionId, position );
                }
                if( object instanceof MatchInfo ) {
                    MatchInfo info = (MatchInfo) object;
                    server.sendToTCP( info.opponentId, object );
                }
            };
        } );
    }

	public static void main (String[] args) throws IOException {
		new KryoServer();
	}
}
