package com.rondeo.bump;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.SharedData;
import com.rondeo.bump.util.Network.Account;

public class MenuScreen extends SharedData {
    Stage stage;
    Table table;
    Table infoTable;
    Texture arenaTexture;
    Skin skin;
    OrthographicCamera camera;
    Image arenaImage;
    BumpSnail game;

    Label username, points;
    
    public MenuScreen( final BumpSnail game ) {
        this.game = game;

        skin = new Skin( Gdx.files.internal( "default/default.json" ) );

        stage = new Stage( new ExtendViewport( 1000, 500, camera = new OrthographicCamera( 1000, 500 ) ) );
        table = new Table( skin );
        //table.setFillParent( true );
        table.setBounds( 300, 0, 700, 500 );
        stage.addActor( table );

        arenaTexture = new Texture( Gdx.files.internal( "arena.png" ) );
        arenaImage = new Image( arenaTexture );
        table.add( arenaImage );
        
        ImageTextButton playButton = new ImageTextButton(  "FIND MATCH", skin );
        playButton.padLeft( 20 );
        playButton.padRight( 20 );
        playButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            };
            public void touchUp( InputEvent event, float x, float y, int pointer, int button ) {
                stage.addAction( new Action() {
                    @Override
                    public boolean act( float delta ) {
                        camera.zoom -= delta;
                        camera.translate( 3.2f, 1 );
                        if( camera.zoom < .2f ) {
                            try {
                                game.setScreen( new MatchFinderScreen( game ) );
                            } catch ( IOException e) {
                                e.printStackTrace();
                                Gdx.app.exit();
                            }
                            return true;
                        }
                        return false;
                    }
                } );
            };
        } );
        table.row();
        table.add( playButton );

        infoTable = new Table( skin );
        infoTable.setBounds( -300, 0, 300, 500 );
        infoTable.setBackground( skin.getDrawable( "default_window" ) );
        infoTable.addAction( Actions.moveBy( 300, 0, .3f ) );
        stage.addActor( infoTable );

        points = new Label( "0", skin );
        username = new Label( "@default", skin.get( "small", LabelStyle.class ) );
        username.setColor( Color.WHITE );
        TextButton logoutButton = new TextButton( "LOGOUT", skin.get( "secondary", TextButtonStyle.class ) );
        logoutButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; };
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                preferences.clear();
                preferences.flush();
                game.setScreen( new LoginScreen( game, "Welcome Back! " ) );
            };
        } );

        infoTable.add( points ).padBottom( 5 );
        infoTable.row();
        infoTable.add( username );
        infoTable.row();
        infoTable.add( logoutButton ).padTop( 50 );

        Gdx.input.setInputProcessor( stage );
    }

    Preferences preferences;

    @Override
    public void show() {
        playTheme1();

        preferences = Gdx.app.getPreferences( "bump-snail-prefs" );

        client = new Client();
        client.start();
        try {
            client.connect( 5 * 1000, "97.74.80.16", 54555 , 54777 );
        } catch( Exception ex ) {
            // Show message or something
        }
        kryo = client.getKryo();
        Network.register( kryo );

        client.addListener( new Listener() {
            public void received( Connection connection, Object object) {
                if( object instanceof Account ) {
                    final Account account = (Account) object;
                    //System.out.println( account.ID );
                    if( account.ID != 0 ) {
                        preferences.putInteger( "ID", account.ID );
                        preferences.putString( "username", account.username );
                        preferences.putString( "fullname", account.fullname );
                        preferences.putString( "password", account.password );
                        preferences.putInteger( "points", account.points );
                        preferences.flush();
                        username.setText( "@" + account.username );
                        points.setText( account.points + " POINTS" );
                    } else {
                        // Show the error message
                        game.setScreen( new LoginScreen( game, "Please log in" ) );
                    }
                }
            };
        } );

        if( preferences.contains( "username" ) && preferences.contains( "password" ) ) {
            try {
                connectServer( preferences.getString( "username" ), preferences.getString( "password" ) );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        super.show();
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

        super.resize( width, height );
    }

    @Override
    public void dispose() {
        stage.dispose();
        arenaTexture.dispose();
        skin.dispose();

        super.dispose();
    }

    @Override
    public void hide() {
        dispose();
        super.hide();
    }

}
