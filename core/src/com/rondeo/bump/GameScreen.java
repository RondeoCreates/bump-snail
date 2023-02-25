package com.rondeo.bump;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.dongbat.jbump.World;
import com.rondeo.bump.entity.Entity;
import com.rondeo.bump.entity.Snail;

public class GameScreen extends ScreenAdapter {
    
    Stage stage;
    Stage hud;
    Table table;
    Skin skin;

    World<Entity> world;
    int vWidth = 1000, vHeight = 500;

    Texture lawnTexture;

    public GameScreen() {
        stage = new Stage( new ExtendViewport( vWidth, vHeight ) );
        hud = new Stage( new ExtendViewport( vWidth, vHeight ) );
        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );
        table = new Table( skin );

        lawnTexture = new Texture( Gdx.files.internal( "lawn.png" ) );
        Image image = new Image( lawnTexture );
        image.setBounds( 0, 0, vWidth, vHeight );
        stage.addActor( image );

        stage.addListener( new InputListener() {
            int width = 80;
            int height = 80;
            boolean left = true;
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                left = x < vWidth/2;
                stage.addActor( new Snail( world, left  ? 0 : vWidth, y - height/2, width, height, left ? 1 : -1, 0 ) );
                //stage.addActor( new Snail( world, vWidth - width/2, y - height/2, width, height, -1, 0 ) );
                return false;
            }
        } );

        Gdx.input.setInputProcessor( stage );
        stage.setDebugAll( true );

        world = new World<Entity>( 50 );
    }

    @Override
    public void show() {
        
    }

    @Override
    public void render(float delta) {
        
        stage.act( delta );
        stage.draw();

        hud.act( delta );
        hud.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update( width, height );
        hud.getViewport().update( width, height );
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        hud.dispose();

        lawnTexture.dispose();
    }

    @Override
    public void hide() {
        dispose();
    }
    
}
