package com.rondeo.bump;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.rondeo.bump.util.Network;
import com.rondeo.bump.util.Network.Account;
import com.rondeo.bump.util.Network.Message;

public class RegisterScreen extends ScreenAdapter {
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
                client.connect( 5 * 1000, "97.74.80.16", 80, 80 );
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
                                game.setScreen( new LoginScreen( game, "Complete Registration by logging in" ) );
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

        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );

        stage = new Stage( new ExtendViewport( 1000, 500 ) );
        table = new Table( skin );
        table.setFillParent( true );
        stage.addActor( table );

        message = new Label( "", skin );

        nameField = new TextField( "", skin );
        nameField.setMessageText( "Fullname" );
        userField = new TextField( "", skin );
        userField.setMessageText( "Username" );
        passField = new TextField( "", skin );
        passField.setMessageText( "Password" );
        passField.setPasswordMode( true );
        passField.setPasswordCharacter( '•' );
        TextButton regButton = new TextButton( "REGISTER", skin );
        regButton.pad( 15 );
        regButton.addListener( new InputListener() {
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                String fullname = nameField.getText();
                String username = userField.getText();
                String password = passField.getText();
                try {
                    connectServer( game, fullname, username, password );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                return true;
            };
        } );

        TextButton loginButton = new TextButton( "LOG IN", skin );
        loginButton.pad( 15 );
        loginButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen( new LoginScreen( game, "" ) );
                return true;
            };
        } );

        table.add().width( 300 );
        table.row();
        table.add( message ).fill();
        table.row();
        table.add( nameField ).fill();
        table.row();
        table.add( userField ).fill();
        table.row();
        table.add( passField ).fill();
        table.row();
        table.add( regButton ).fill();
        table.row();
        table.add( new Label( "or", skin ) );
        table.row();
        table.add( loginButton ).fill();

        Gdx.input.setInputProcessor( stage );
    }

    Client client;
    Kryo kryo;
    public void connectServer( final BumpSnail game, String fullname, String username, String password ) throws Exception {
        
        Account account = new Account();
        account.connectionId = client.getID();
        account.fullname = fullname;
        account.username = username;
        account.password = password;
        account.action = 0;
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