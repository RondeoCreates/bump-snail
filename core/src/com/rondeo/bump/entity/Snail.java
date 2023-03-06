package com.rondeo.bump.entity;

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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.rondeo.bump.util.MatchInfoController;

public class Snail extends Entity {
    World world;
    Body body;

    Vector2 lastPosition = new Vector2();
    Vector2 targetPoint = new Vector2();
    public int power = 1;
    public int health = 5;
    public boolean flip;
    boolean log;
    boolean animate = true;
    boolean isDead;
    float deltaTime;

    Animation<TextureRegion> walkAnimation;

    Label label;
    public String referenceName;
    float width, height;

    MatchInfoController matchInfoController;

    public Snail( MatchInfoController matchInfoController, World world, float x, float y, float width, float height, boolean flip, TextureRegion[] animation, int power, boolean log, Skin skin, int manaConsumption ) {
        this.matchInfoController = matchInfoController;
        this.world = world;
        this.flip = flip;
        this.log = log;
        this.power = power;
        this.manaConsumption = manaConsumption;
        this.width = width;
        this.height = height;

        health += power;

        setBounds( x, y, width, height );
        
        targetPoint.set( flip ? -5*power : 5*power, 0 );

        walkAnimation = new Animation<TextureRegion>( 0.14f, animation );
        walkAnimation.setPlayMode( PlayMode.LOOP );
        label = new Label( String.valueOf( power ), skin );
    }

    @Override
    public void pack() {
        if( body == null )
            body = createBody( getX(), getY(), getWidth(), getHeight() );
    }

    private Body createBody( float cx, float cy, float hw, float hh ) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set( cx, cy );

        Body body = world.createBody( bodyDef );
        body.setFixedRotation( true );

        PolygonShape box = new PolygonShape();
        box.setAsBox( hw, hh, new Vector2( 0,0 ), 0 );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1;
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 0;

        body.createFixture( fixtureDef );
        body.setUserData( this );

        box.dispose();

        return body;
    }

    public void changeStat() {
        targetPoint.set( flip ? -5*Math.max( 0, power) : 5*Math.max( 0, power), 0 );
    }

    @Override
    public void act(float delta) {
        if( delta == 0 )
            return;

        lastPosition.set( getX(), getY() );

        //body.applyForceToCenter( targetPoint.x * 5000, 0, true );
        body.setLinearVelocity( targetPoint.x * 5, 0 );
        //body.applyLinearImpulse( targetPoint, body.getWorldCenter(), true );
        setBounds( body.getPosition().x - width, body.getPosition().y -height, width*2, height*2 );
        
        animate = false;
        if( flip ) {
            if( getX() < lastPosition.x )
                animate = true;
        } else {
            if( getX() > lastPosition.x )
                animate = true;
        }

        if( animate )
            deltaTime += delta;

        lastPosition.set( getX(), getY() );

        label.setPosition( getX(), getY() );
        label.setText( String.valueOf( power ) );
        
        super.act( delta );

        // remove snail if out of bounds
        if( ( body.getPosition().x < -100 || body.getPosition().x > 1000 + 100 ) && !isDead ) {
            isDead = true;
            if( body.getPosition().x > 1000 + 100 && !flip ) {
                matchInfoController.update( power * 10 );
            }
            Gdx.app.postRunnable( new Runnable() {
                @Override
                public void run() {
                    world.destroyBody( body );
                    remove();
                }
            } );
        }
    }

    final int scale = 5;

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw( 
            walkAnimation.getKeyFrame( deltaTime ), 
            flip ? getX() + getWidth() + ( 10 + scale*power) : getX() - ( 10 + scale*power), 
            getY(), 
            flip ? - getWidth() - ( 20 + scale*power) : getWidth() + ( 20 + scale*power), 
            getHeight() + ( 20 + scale*power) );
        //label.draw( batch, parentAlpha );
    }

}
