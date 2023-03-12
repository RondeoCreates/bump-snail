package com.rondeo.bump.util;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.esotericsoftware.kryonet.Client;
import com.github.tommyettinger.textra.TypingLabel;

public class MatchInfoController {
    public int ID;
    public int points = 0;
    public int overallPoints = 0;
    public String username = "";
    private Client client;
    private int opponentId;

    // Outside source
    private TypingLabel myPointsLabel;
    private Label myScoreEnd;

    public MatchInfoController( Client client, int opponentId ) {
        this.client = client;
        this.opponentId = opponentId;
    }

    public void setLabel( TypingLabel myPointsLabel ) {
        this.myPointsLabel = myPointsLabel;
    }

    public void setLabel( Label myScoreEnd ) {
        this.myScoreEnd = myScoreEnd;
    }

    public void update( int additionalPoints ) {
        points += additionalPoints;
        Network.MatchInfo matchInfo = new Network.MatchInfo();
        matchInfo.opponentId = opponentId;
        matchInfo.points = points;
        matchInfo.overallPoints = overallPoints;
        matchInfo.username = username;

        client.sendTCP( matchInfo );
        if( myPointsLabel == null )
            return;
        myPointsLabel.setText( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}" + points + " POINTS{ENDBEAT}[%]");

        // ulusbonon ni
        if( myScoreEnd == null )
            return;
        myScoreEnd.setText( points );
    }

}
