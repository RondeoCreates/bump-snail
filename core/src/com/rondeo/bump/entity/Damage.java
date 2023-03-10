package com.rondeo.bump.entity;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Damage extends Spell {

    public Damage(World world, float x, float y, float width, float height, boolean flip, TextureRegion[] animation, int power, boolean target, Skin skin, int manaConsumption) {
        super(world, x, y, width, height, flip, animation, power, target, skin, manaConsumption);
    }

    @Override
    public void act(float delta) {
        if( delta == 0 )
            return;

        deltaTime += delta;

        if( timer == null ) {
            timer = new Timer();
            timer.scheduleAtFixedRate( new TimerTask() {
                @Override
                public void run() {
                    isDead = true;
                    timer.cancel();
                }
            }, 2000, 1 );
        }

        if( isDead ) {
            Gdx.app.postRunnable( new Runnable() {
                @Override
                public void run() {
                    world.destroyBody( body );
                    remove();
                }
            } );
        }
        superAct( delta );
    }

    @Override
    public void draw( Batch batch, float parentAlpha ) {
        batch.draw( spellAnimation.getKeyFrame( deltaTime ), getX() - getWidth(), getY() - getHeight(), getWidth() * 2, getHeight() * 8 );
        //label.draw( batch, parentAlpha );
    }
    
}
