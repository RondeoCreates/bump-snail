package com.rondeo.bump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.Network.Account;

public class LoginScreen extends ScreenAdapter {
    Stage stage;
    Table table;
    Skin skin;
    TextField userField, passField;
    Label message;
    
    public LoginScreen( final BumpSnail game, @Null String text ) {
        client = new Client();
        client.start();
        //try {
        //    client.connect( 5000, client.discoverHost( 80, 5000 ), 80, 80 );
        //} catch( Exception e ) {
            try {
                client.connect( 5 * 1000, "97.74.80.16", 80, 80 );
            } catch( Exception ex ) {
                // Show message or something
            }
        //}
        kryo = client.getKryo();
        Network.register( kryo );

        client.addListener( new Listener() {
            public void received( Connection connection, Object object) {
                if( object instanceof Account ) {
                    final Account account = (Account) object;
                    //System.out.println( account.ID );
                    if( account.ID != 0 ) {
                        Gdx.app.postRunnable( new Runnable() {
                            @Override
                            public void run() {
                                Preferences preferences = Gdx.app.getPreferences( "bump-snail-prefs" );
                                preferences.putInteger( "ID", account.ID );
                                preferences.putString( "username", account.username );
                                preferences.putString( "fullname", account.fullname );
                                preferences.putInteger( "points", account.points );
                                preferences.flush();
                                game.setScreen( new MenuScreen( game ) );
                            }
                        } );
                    } else {
                        // Show the error message
                        passField.setText( "" );
                        message.addAction( Actions.alpha( 1 ) );
                        message.setText( "Invalid credentials" );
                        message.addAction( Actions.sequence( Actions.delay( 3 ), Actions.alpha( 0 ) ) );
                    }
                }
            };
        } );

        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );

        stage = new Stage( new ExtendViewport( 1000, 500 ) );
        table = new Table( skin );
        table.setFillParent( true );
        stage.addActor( table );

        message = new Label( text, skin );

        userField = new TextField( "", skin );
        userField.setMessageText( "Username" );
        passField = new TextField( "", skin );
        passField.setPasswordMode( true );
        passField.setMessageText( "Password" );
        passField.setPasswordCharacter( '•' );
        TextButton loginButton = new TextButton( "LOG IN", skin );
        loginButton.pad( 15 );
        loginButton.addListener( new InputListener() {
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                String username = userField.getText();
                String password = passField.getText();
                try {
                    connectServer( game, username, password );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                return true;
            };
        } );

        TextButton regButton = new TextButton( "REGISTER", skin );
        regButton.pad( 15 );
        regButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen( new RegisterScreen( game ) );
                return true;
            };
        } );

        table.add().width( 300 );
        table.row();
        table.add( message ).fill();
        table.row();
        table.add( userField ).fill();
        table.row();
        table.add( passField ).fill();
        table.row();
        table.add( loginButton ).fill();
        table.row();
        table.add( new Label( "or", skin ) );
        table.row();
        table.add( regButton ).fill();

        Gdx.input.setInputProcessor( stage );
    }

    Client client;
    Kryo kryo;
    public void connectServer( final BumpSnail game, String username, String password ) throws Exception {
        
        Account account = new Account();
        account.connectionId = client.getID();
        account.username = username;
        account.password = password;
        account.action = 1;
        client.sendTCP( account );
        
    }

    @Override
    public void render(float delta) {
        stage.act( delta );
        stage.draw();

        super.render(delta);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();

        super.dispose();
    }

    @Override
    public void hide() {
        dispose();

        super.hide();
    }

}
