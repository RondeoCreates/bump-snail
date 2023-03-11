package com.rondeo.bump.util;

import com.esotericsoftware.kryo.Kryo;

public class Network {

    public static void register( Kryo kryo ) {
        kryo.register( Position.class );
        kryo.register( MatchDefinition.class );
        kryo.register( FindMatch.class );
        kryo.register( Account.class );
        kryo.register( MatchInfo.class );
        kryo.register( Message.class );
    }
    
    public static class Position {
        public int connectionId = -1;
        public float x, y;
        public int cardIndex;

        public Position(){}

        public Position( int id, float x, float y, int cardIndex ) {
            connectionId = id;
            this.x = x;
            this.y = y;
            this.cardIndex = cardIndex;
        }
    }

    public static class FindMatch {
        public int connectionId;
    }

    public static class Account {
        public int connectionId;
        
        public int ID;
        public String username;
        public String password;
        public String fullname;
        public int points;

        public int action;

        @Override
        public String toString() {
            return String.format( "%s, %s, %s, %s, %s, %s, %s", connectionId, ID, username, password, fullname, points, action );
        }
    }

    public static class Message {
        public String msg;
        public Message() {}
        public Message( String msg ) {
            this.msg = msg;
        }
    }

    public static class MatchInfo {
        public int points = 0;
        public int opponentId = -1;
        public String username = "";
        public int overallPoints = 0;
    }

    public static class MatchDefinition {
        public int connectionIdA = -1;
        public int connectionIdB = -1;

        public MatchDefinition(){}

        public MatchDefinition( int id ) {
            connectionIdA = id;
        }
    }

}