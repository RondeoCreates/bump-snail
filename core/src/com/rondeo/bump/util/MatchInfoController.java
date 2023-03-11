package com.rondeo.bump.util;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.esotericsoftware.kryonet.Client;

public class MatchInfoController {
    public int ID;
    public int points = 0;
    public int overallPoints = 0;
    public String username = "";
    private Client client;
    private int opponentId;
    private Label myPointsLabel;

    public MatchInfoController( Client client, int opponentId ) {
        this.client = client;
        this.opponentId = opponentId;
    }

    public void setLabel( Label myPointsLabel  ) {
        this.myPointsLabel = myPointsLabel;
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
        myPointsLabel.setText( points + "POINTS" );
    }

}
