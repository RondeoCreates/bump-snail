package com.rondeo.bump;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class MenuScreen extends ScreenAdapter {
    Stage stage;
    Table table;
    Texture arenaTexture;
    Skin skin;
    OrthographicCamera camera;
    
    public MenuScreen( final BumpSnail game ) {
        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );

        stage = new Stage( new ExtendViewport( 1000, 500, camera = new OrthographicCamera( 1000, 500 ) ) );
        table = new Table( skin );
        table.setFillParent( true );
        stage.addActor( table );

        arenaTexture = new Texture( Gdx.files.internal( "arena.png" ) );
        Image image = new Image( arenaTexture );
        table.add( image );
        
        ImageTextButtonStyle style = new ImageTextButtonStyle( skin.getDrawable( "flat-button" ), skin.getDrawable( "flat-button" ), skin.getDrawable( "flat-button" ), skin.getFont( "font" ) );
        style.fontColor = Color.BLACK;
        style.imageUp = skin.getDrawable( "sword" );
        ImageTextButton playButton = new ImageTextButton(  "Find Match", style );
        playButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            };
            public void touchUp( InputEvent event, float x, float y, int pointer, int button ) {
                stage.addAction( new Action() {
                    @Override
                    public boolean act( float delta ) {
                        camera.zoom -= delta;
                        camera.translate( 0, 1 );
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

        Gdx.input.setInputProcessor( stage );
    }

    @Override
    public void show() {
        
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

        super.resize(width, height);
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
