package com.rondeo.bump.util;

import com.esotericsoftware.kryo.Kryo;

public class Network {

    public static void register( Kryo kryo ) {
        kryo.register( Position.class );
        kryo.register( MatchDefinition.class );
        kryo.register( FindMatch.class );
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

    public static class MatchDefinition {
        public int connectionIdA = -1;
        public int connectionIdB = -1;

        public MatchDefinition(){}

        public MatchDefinition( int id ) {
            connectionIdA = id;
        }
    }

    public static class FindMatch {
        public int connectionId;
    }

}