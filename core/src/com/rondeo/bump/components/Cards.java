package com.rondeo.bump.components;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Null;

public class Cards {
    TextureRegion[] cards;
    String[] regions;
    int[] power;
    int[] mana;
    int[] type;

    public static int SNAIL = 0, SPELL = 1;
    
    public Cards( TextureRegion[] cards, String[] regions, int[] power, int[] mana, int[] type ) {
        this.cards = cards;
        this.regions = regions;
        this.power = power;
        this.mana = mana;
        this.type = type;
    }

    public Image getCard( int index, @Null Image image ) {
        if( image == null ) {
            image = new Image();
        }
        image.setDrawable( new TextureRegionDrawable( cards[index] ) );
        
        return image;
    }

    public TextureRegion[] getAnimation( TextureAtlas atlas, int tileWidth, int tileHeight, int index ) {
        return atlas.findRegion( regions[index] ).split( tileWidth, tileHeight )[0];
    }

    public int getPower( int index ) {
        return power[index];
    }

    public int getManaConsumption( int index ) {
        return mana[index];
    }

    public int getType( int index ) {
        return type[index];
    }

    public int size() {
        return cards.length;
    }

}
