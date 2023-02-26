package com.rondeo.bump;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.rondeo.bump.entity.Snail;

public class GameScreen extends ScreenAdapter {
    World world;
    Box2DDebugRenderer debugRenderer;

    Stage stage;
    Stage hud;
    Table table;
    Skin skin;
    OrthographicCamera camera;
    int vWidth = 1000, vHeight = 500;

    Texture snailTexture;
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    public GameScreen() {
        // Box2d
        Box2D.init();
        world = new World( new Vector2( 0, 0 ), false );

        // Stages and Skins
        stage = new Stage( new ExtendViewport( vWidth, vHeight, camera = new OrthographicCamera( vWidth, vHeight ) ) );
        hud = new Stage( new ExtendViewport( vWidth, vHeight ) );
        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );
        table = new Table( skin );

        // TileMap
        tiledMap = new TmxMapLoader().load( "terrain.tmx" );
        tiledMapRenderer = new OrthogonalTiledMapRenderer( tiledMap );

        // Snail
        snailTexture = new Texture( Gdx.files.internal( "snail.png" ) );

        stage.addListener( new InputListener() {

            int width = 25;
            int height = 25;
            boolean flip = false;
            boolean lastFlip = true;
            int power = 0;
            Random random = new Random();

            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                flip = x > vWidth/2;
                if( lastFlip != flip ) {
                    power = random.nextInt( 4 ) + 1;
                    stage.addActor( 
                        new Snail( world, flip ? vWidth - width : width, (y - y%100) + 50, width, height, flip, TextureRegion.split( snailTexture, 24, 24 )[0], power, button == 1, skin ) 
                    );
                    lastFlip = flip;
                }
                return false;
            }

        } );

        Gdx.input.setInputProcessor( stage );
        //stage.setDebugAll( true );
        camera.zoom = 1.1f;

        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void show() {
        
    }

    @Override
    public void render(float delta) {

        tiledMapRenderer.setView( camera );
        tiledMapRenderer.render();
        
        stage.act( delta );
        stage.draw();

        hud.act( delta );
        hud.draw();

        world.step(1/60f, 6, 2);
        //debugRenderer.render( world, camera.combined );
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
        
        snailTexture.dispose();
    }

    @Override
    public void hide() {
        dispose();
    }
    
}
