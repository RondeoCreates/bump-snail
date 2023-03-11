package com.rondeo.bump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.SharedData;
import com.rondeo.bump.util.Network.Message;

public class RegisterScreen extends SharedData {
    Stage stage;
    Table table;
    Skin skin;
    TextField nameField, userField, passField;
    Label message;
    
    public RegisterScreen( final BumpSnail game ) {
        client = new Client();
        client.start();
        //try {
        //    client.connect( 5000, client.discoverHost( 80, 5000 ), 80, 80 );
        //} catch( Exception e ) {
            try {
                client.connect( 5 * 1000, "97.74.80.16", 54555 , 54777 );
            } catch( Exception ex ) {
                // Show message or something
            }
        //}
        kryo = client.getKryo();
        Network.register( kryo );

        client.addListener( new Listener() {
            public void received( Connection connection, Object object) {
                if( object instanceof Message ) {
                    Message objMsg = (Message) object;
                    //System.out.println( objMsg.msg );
                    if( objMsg.msg == null ) {
                        Gdx.app.postRunnable( new Runnable() {
                            @Override
                            public void run() {
                                table.addAction( Actions.sequence( Actions.moveBy( 500, 0, .3f ), new Action() {
                                    @Override
                                    public boolean act(float delta) {
                                        game.setScreen( new LoginScreen( game, "Complete Registration by logging in" ) );
                                        return true;
                                    }
                                } ) );
                            }
                        } );
                    } else {
                        // Show the error message
                        passField.setText( "" );
                        message.addAction( Actions.alpha( 1 ) );
                        message.setText( objMsg.msg );
                        message.addAction( Actions.sequence( Actions.delay( 3 ), Actions.alpha( 0 ) ) );
                    }
                }
            };
        } );

        skin = new Skin( Gdx.files.internal( "default/default.json" ) );

        stage = new Stage( new ExtendViewport( 1000, 500 ) );
        table = new Table( skin );
        table.setPosition( 1000, 0 );
        table.addAction( Actions.moveBy( -500, 0, .3f ) );
        table.setSize( 500, 500 );
        stage.addActor( table );

        message = new Label( "", skin.get( "small", LabelStyle.class ) );
        message.setColor( Color.BLACK );
        message.setAlignment( Align.center );

        nameField = new TextField( "", skin );
        nameField.setMessageText( "Fullname" );
        userField = new TextField( "", skin );
        userField.setMessageText( "Username" );
        passField = new TextField( "", skin );
        passField.setMessageText( "Password" );
        passField.setPasswordMode( true );
        passField.setPasswordCharacter( '•' );
        TextButton regButton = new TextButton( "REGISTER", skin );
        //regButton.pad( 15 );
        regButton.addListener( new InputListener() {
            public void touchUp( InputEvent event, float x, float y, int pointer, int button ) {
                String fullname = nameField.getText();
                String username = userField.getText();
                String password = passField.getText();
                try {
                    connectServer( fullname, username, password );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            };
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) { return true; };
        } );

        TextButton loginButton = new TextButton( "LOG IN", skin.get( "secondary", TextButtonStyle.class ) );
        //loginButton.pad( 15 );
        loginButton.addListener( new InputListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                table.addAction( Actions.sequence( Actions.moveBy( 500, 0, .3f ), new Action() {
                    @Override
                    public boolean act(float delta) {
                        game.setScreen( new LoginScreen( game, "" ) );
                        return true;
                    }
                } ) );
            };
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; };
        } );

        table.add().width( 300 );
        table.row();
        table.add( message ).fill().pad( 10 );
        table.row();
        table.add( nameField ).fill().pad( 10 ).height( 50 );
        table.row();
        table.add( userField ).fill().pad( 10 ).height( 50 );
        table.row();
        table.add( passField ).fill().pad( 10 ).height( 50 );
        table.row();
        table.add( regButton ).fill().pad( 10 );
        table.row();
        Label orLabel = new Label( "or", skin.get( "small", LabelStyle.class ) );
        orLabel.setColor( Color.BLACK );
        table.add( orLabel );
        table.row();
        table.add( loginButton ).fill().pad( 10 );

        table.setBackground( skin.getDrawable( "secondary_window" ) );

        Gdx.input.setInputProcessor( stage );
    }

    @Override
    public void render(float delta) {
        stage.act( delta );
        stage.draw();

        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update( width, height );

        super.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        client.close();

        super.dispose();
    }

    @Override
    public void hide() {
        dispose();

        super.hide();
    }
}