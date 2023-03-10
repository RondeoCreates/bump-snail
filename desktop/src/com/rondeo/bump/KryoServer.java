package com.rondeo.bump;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.Network.Account;
import com.rondeo.bump.util.Network.FindMatch;
import com.rondeo.bump.util.Network.MatchDefinition;
import com.rondeo.bump.util.Network.MatchInfo;
import com.rondeo.bump.util.Network.Message;
import com.rondeo.bump.util.Network.Position;

public class KryoServer extends DatabaseController {
    Server server;
    Kryo kryo;
    ArrayList<MatchDefinition> matches = new ArrayList<>();

	public KryoServer () throws IOException, SQLException {
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

                // Account
                if( object instanceof Account ) {
                    Account account = (Account) object;
                    int connectionId = account.connectionId;
                    try {
                        switch( account.action ) {
                            case 0:
                                Message result = registerUser( account.username, account.password, account.fullname );
                                server.sendToTCP( connectionId, result );
                            break;
                            case 1:
                                account = loginUser( account.username, account.password );
                                account.connectionId = connectionId;
                                server.sendToTCP( connectionId, account );
                            break;
                            case 2:
                                account = updatePoints( account.ID, account.points );
                                account.connectionId = connectionId;
                                server.sendToTCP( connectionId, account );
                            break;
                        }
                    } catch( Exception e ) {
                        e.printStackTrace();
                    }
                }
                
            };
        } );
    }

	public static void main (String[] args) throws IOException, SQLException {
		new KryoServer();
	}
}
