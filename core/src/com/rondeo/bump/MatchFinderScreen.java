package com.rondeo.bump;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.Network.FindMatch;

public class MatchFinderScreen extends ScreenAdapter {
    private AsyncExecutor executor = new AsyncExecutor(4);

    Texture search, clouds, terrainTexture;
    Animation<TextureRegion> searcAnimation;

    Stage stage;
    OrthographicCamera camera;
    
    public MatchFinderScreen( final BumpSnail game ) throws IOException {

        stage = new Stage( new ExtendViewport( 1000, 500, camera = new OrthographicCamera( 1000, 500 ) ) );

        terrainTexture = new Texture( Gdx.files.internal( "terrain.png" ) );
        Image terrain = new Image( new TextureRegion( terrainTexture ) );
        terrain.setBounds( -400, -300, 1800, 1100 );
        stage.addActor( terrain );

        clouds = new Texture( Gdx.files.internal( "clouds.png" ) );
        search = new Texture( Gdx.files.internal( "searching.png" ) );
        searcAnimation = new Animation<>( .1f, TextureRegion.split( search, 48, 48 )[0] );
        searcAnimation.setPlayMode( PlayMode.LOOP );

        Image cloudImageLeft = new Image( clouds );
        cloudImageLeft.setBounds( -350, -300, 1800, 1100 );
        stage.addActor( cloudImageLeft );

        Image cloudImageRight = new Image( clouds );
        cloudImageRight.setBounds( 1350, -300, -1800, 1100 );
        stage.addActor( cloudImageRight );

        Table table = new Table();
        table.setFillParent( true );

        Actor animatedActor = new Actor() {
            float time;
            
            @Override
            public void act( float delta ) {
                time += delta;
                super.act(delta);
            }

            @Override
            public void draw( Batch batch, float parentAlpha ) {
                batch.draw( searcAnimation.getKeyFrame( time ), getX(), getY(), getWidth(), getHeight() );
            }
        };
        animatedActor.setBounds( -100, -100, 200, 200 );
        table.add( animatedActor );
        stage.addActor( table );
        
        executor.submit( new AsyncTask<Void>() {
            @Override
            public Void call() throws Exception {
                final Client client = new Client();
                client.start();
                
                //try {
                //    client.connect( 5000, client.discoverHost( 80, 5000 ), 80, 80 );
                //} catch( Exception e ) {
                    client.connect( 5 * 1000, "97.74.80.16", 80, 80 );
                //}

                Kryo kryo = client.getKryo();
                Network.register( kryo );

                FindMatch findMatch = new FindMatch();
                findMatch.connectionId = client.getID();
                client.sendTCP( findMatch );

                client.addListener( new Listener() {
                    public void received( Connection connection, Object object) {
                        if( object instanceof FindMatch ) {
                            FindMatch findMatch = (FindMatch) object;
                            final int opponentId = findMatch.connectionId;
                            
                            Gdx.app.postRunnable( new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        game.setScreen( new MultiplayerScreen( client, opponentId ) );
                                    } catch( IOException e ) {
                                        e.printStackTrace();
                                    }
                                }
                            } );
                        }
                    };
                } );

                return null;
            }
        } );

        camera.zoom = 1.5f;
        camera.translate( 0, -100 );
    }

    @Override
    public void render(float delta) {

        stage.act();
        stage.draw();
        
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update( width, height );
        super.resize(width, height);
    }
    
    @Override
    public void hide() {
        dispose();
        super.hide();
    }

    @Override
    public void dispose() {
        executor.dispose();
        stage.dispose();
        clouds.dispose();
        search.dispose();

        super.dispose();
    }

}
