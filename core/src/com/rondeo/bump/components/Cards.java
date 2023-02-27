package com.rondeo.bump.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Null;

public class Cards {
    TextureRegion[] cards;
    TextureRegion[][] snails;
    int[] power;
    int[] mana;
    
    public Cards( TextureRegion[] cards, TextureRegion[][] snails, int[] power, int[] mana ) {
        this.cards = cards;
        this.snails = snails;
        this.power = power;
        this.mana = mana;
    }

    public Image getCard( int index, @Null Image image ) {
        if( image == null ) {
            image = new Image();
        }
        image.setDrawable( new TextureRegionDrawable( cards[index] ) );
        
        return image;
    }

    public TextureRegion[] getSnail( int index ) {
        return snails[ index ];
    }

    public int getPower( int index ) {
        return power[index];
    }

    public int getManaConsumption( int index ) {
        return mana[index];
    }

    public int size() {
        return cards.length;
    }

}
