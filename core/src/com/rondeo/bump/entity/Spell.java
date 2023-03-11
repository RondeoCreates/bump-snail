package com.rondeo.bump.entity;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Spell extends Entity {
    World world;
    Body body;
    public int power;
    public boolean flip;
    public boolean target;
    boolean isDead;

    Animation<TextureRegion> spellAnimation;

    public String referenceName;
    float width, height;

    public Spell( int index, World world, float x, float y, float width, float height, boolean flip, TextureRegion[] animation, int power, boolean target, Skin skin, int manaConsumption ) {
        this.index = index;
        this.world = world;
        this.flip = flip;
        this.power = power;
        this.manaConsumption = manaConsumption;
        this.width = width;
        this.height = height;
        this.target = target;

        setBounds( x, y, width, height );
        spellAnimation = new Animation<TextureRegion>( 0.14f, animation );
        spellAnimation.setPlayMode( PlayMode.LOOP );
    }

    @Override
    public void pack() {
        if( body == null ) {
            // create body
            body = createBody( getX(), getY(), getWidth(), getHeight() );
        }
    }

    private Body createBody( float cx, float cy, float hw, float hh ) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        bodyDef.position.set( cx, cy );

        Body body = world.createBody( bodyDef );
        body.setFixedRotation( true );

        PolygonShape box = new PolygonShape();
        box.setAsBox( hw, hh, new Vector2( 0,0 ), 0 );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.filter.groupIndex = -1;
        fixtureDef.isSensor = true;

        body.createFixture( fixtureDef );
        body.setUserData( this );

        box.dispose();

        return body;
    }

    Timer timer;
    float deltaTime;

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
            }, 6000, 1 );
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

        super.act( delta );
    }

    public void superAct( float delta ) {
        super.act( delta );
    }
    
    @Override
    public void draw( Batch batch, float parentAlpha ) {
        batch.draw( spellAnimation.getKeyFrame( deltaTime ), getX() - getWidth(), getY() - getHeight(), getWidth() * 2, getHeight() * 2 );
        //label.draw( batch, parentAlpha );
    }

}
