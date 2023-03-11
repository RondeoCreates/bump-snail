package com.rondeo.bump.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.rondeo.bump.util.Network.Account;

public class SharedData extends MusicController {

    public Client client;
    public Kryo kryo;

    public void connectServer( String username, String password ) throws Exception {
        Account account = new Account();
        account.connectionId = client.getID();
        account.username = username;
        account.password = password;
        account.action = 1;
        client.sendTCP( account );
    }

    public void connectServer( String fullname, String username, String password ) throws Exception {
        Account account = new Account();
        account.connectionId = client.getID();
        account.fullname = fullname;
        account.username = username;
        account.password = hash( password );
        account.action = 0;
        client.sendTCP( account );
    }

    public String hash( String password ) {
        try {
            MessageDigest md = MessageDigest.getInstance( "MD5" );
            byte[] messageDigest = md.digest( password.getBytes() );
            BigInteger no = new BigInteger( 1, messageDigest );
            password = no.toString( 16 );
            while( password.length() < 32 ) {
                password = "0" + password;
            }
        } catch( NoSuchAlgorithmException e ) { e.printStackTrace(); }
        return password;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void hide() {
        super.hide();
    }

}
