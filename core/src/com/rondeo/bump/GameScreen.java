package com.rondeo.bump;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.rondeo.bump.components.Cards;
import com.rondeo.bump.entity.Snail;

public class GameScreen extends ScreenAdapter {
    World world;
    Box2DDebugRenderer debugRenderer;

    Stage stage;
    Skin skin;
    OrthographicCamera camera;
    int vWidth = 1000, vHeight = 500;
    InputMultiplexer inputMultiplexer;

    Texture snailTexture;
    Texture terrainTexture;

    public GameScreen() {
        snailTexture = new Texture( Gdx.files.internal( "snail.png" ) );
        cardTexture = new Texture( Gdx.files.internal( "cards.png" ) );

        // Box2d
        Box2D.init();
        world = new World( new Vector2( 0, 0 ), false );

        // Stages and Skins
        stage = new Stage( new ExtendViewport( vWidth, vHeight, camera = new OrthographicCamera( vWidth, vHeight ) ) );

        // TileMap
        terrainTexture = new Texture( Gdx.files.internal( "terrain.png" ) );
        Image terrain = new Image( new TextureRegion( terrainTexture ) );
        terrain.setBounds( -400, -300, 1800, 1100 );
        stage.addActor( terrain );

        debugRenderer = new Box2DDebugRenderer();
    }
    
    // HUD
    Stage hud;
    Table table, cardSlotA, cardSlotB;
    Texture cardTexture, manaTexture;
    TextButton manaLabelA, manaLabelB;
    ProgressBar manaProgressA, manaProgressB;
    Timer timer;

    @Override
    public void show() {
        hud = new Stage( new ExtendViewport( vWidth, vHeight ) );
        skin = new Skin( Gdx.files.internal( "ui/terra-mother-ui.json" ) );

        // Setup table
        table = new Table( skin );
        table.setFillParent( true );
        hud.addActor( table );
        
        // Health
        table.row();
        table.add().colspan( 3 );
        
        table.row();
        table.add().colspan( 3 ).expandY();

        // Cards
        table.row();

        cardSlotA = new Table( skin );
        cardSlotA.setBackground( new NinePatchDrawable( skin.getPatch( "flat-slot" ) ) );
        cardSlotA.pad( 5 );
        table.add( cardSlotA );

        table.add().expandX();

        cardSlotB = new Table( skin );
        cardSlotB.setBackground( new NinePatchDrawable( skin.getPatch( "flat-slot" ) ) );
        cardSlotB.pad( 5 );
        table.add( cardSlotB );

        init();

        inputMultiplexer = new InputMultiplexer( stage, hud );
        Gdx.input.setInputProcessor( inputMultiplexer );
        //hud.setDebugAll( true );
        camera.zoom = 1.5f;
        camera.translate( 0, -100 );

        // Setup a timer for Mana
        timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                if( manaA < 10 ) {
                    manaA ++;
                    manaLabelA.setText( String.valueOf( manaA ) );
                    manaProgressA.setValue( manaA );
                }
                if( manaB < 10 ) {
                    manaB ++;
                    manaLabelB.setText( String.valueOf( manaB ) );
                    manaProgressB.setValue( manaB );
                }
            }
        }, 0, 2000 );

        // Setup label and fill bar for mana
        manaTexture = new Texture( Gdx.files.internal( "mana.png" ) );
        TextButtonStyle manaStyle = new TextButtonStyle( new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), skin.getFont( "font" ) );
        ProgressBarStyle progressBarStyle = new ProgressBarStyle( skin.getDrawable( "black-tint" ), skin.getDrawable( "purple-tint" ) );
        progressBarStyle.background.setMinHeight( 16 );
        progressBarStyle.knob.setMinHeight( 12 );
        progressBarStyle.knobBefore = progressBarStyle.knob;

        manaLabelA = new TextButton( "10", manaStyle );
        manaLabelA.pad( 2, 10, 0, 10 );
        manaLabelB = new TextButton( "10", manaStyle );
        manaLabelB.pad( 2, 10, 0, 10 );
        manaProgressA = new ProgressBar( 0, 10, 1, false, progressBarStyle );
        manaProgressA.setValue( 10 );
        manaProgressB = new ProgressBar( 0, 10, 1, false, progressBarStyle );
        manaProgressB.setValue( 10 );

        HorizontalGroup horizontalGroup;
        cardSlotA.row();
        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor( manaLabelA );
        horizontalGroup.addActor( manaProgressA );
        cardSlotA.add( horizontalGroup ).colspan( 4 );

        cardSlotB.row();
        horizontalGroup = new HorizontalGroup();
        horizontalGroup.addActor( manaLabelB );
        horizontalGroup.addActor( manaProgressB );
        cardSlotB.add( horizontalGroup ).colspan( 4 );
        /*cardSlotA.add( manaLabelA );
        cardSlotA.add( manaProgressA ).colspan( 3 );
        cardSlotB.row();
        cardSlotB.add( manaLabelB );
        cardSlotB.add( manaProgressB ).colspan( 3 );*/
    }

    Cards cards;
    Snail readyA, readyB;
    String refStringA, refStringB;
    Random random = new Random();
    Cell<Actor> tempCellA, tempCellB;
    int manaA = 10, manaB = 10;

    public void init() {

        cards = new Cards( 
            TextureRegion.split( cardTexture, 36, 36 )[0], 
            TextureRegion.split( snailTexture, 24, 24 ), 
            new int[] { 1, 2, 3, 4, 5 }, 
            new int[] { 2, 3, 4, 6, 8 }
        );

        stage.addListener( new InputListener() {
            float touchX, touchY;
            Snail snail;
            Image indicator;
            {
                indicator = new Image( skin.getDrawable( "select-overlay" ) );
                indicator.setVisible( false );
                stage.addActor( indicator );
                indicator.setSize( 100, 100 );
            }

            @Override
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                // Show indicator
                if( x > 0 && x < vWidth && y > 0 && y < vHeight ) {
                    indicator.setVisible( true );
                    indicator.setPosition( x - x%100, y - y%100 );
                    return true;
                } else {
                    indicator.setVisible( false );
                }
                return false;
            };

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if( x > 0 && x < vWidth && y > 0 && y < vHeight ) {
                    indicator.setVisible( true );
                    indicator.setPosition( x - x%100, y - y%100 );
                } else {
                    indicator.setVisible( false );
                }
            };

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // hide indicator
                if( x < 0 || x > vWidth || y < 0 || y > vHeight )
                    return;
                indicator.setVisible( false );
                touchY = y;
                touchY = Math.max( 0, touchY );
                touchY = Math.min( vHeight - 1, touchY );
                touchY = (touchY - touchY%100) + 50;
                touchX = x;
                touchX = Math.max( 0, touchX );
                touchX = Math.min( vWidth - 1, touchX );
                touchX = ( touchX - touchX%100 ) + 50;
                if( x > vWidth/2 ) {
                    if( readyB == null )
                        return;
                    snail = readyB;
                    snail.setPosition( touchX, touchY );
                    snail.pack();
                    stage.addActor( snail );
                    manaB -= snail.manaConsumption;
                    manaLabelB.setText( String.valueOf( manaB ) );
                    manaProgressB.setValue( manaB );
                    readyB = null;
                    tempCellB = cardSlotB.getCell( cardSlotB.findActor( refStringB ) );
                    cardSlotB.findActor( refStringB ).remove();
                    refStringB = null;
                    addCardB();
                } else {
                    if( readyA == null )
                        return;
                    snail = readyA;
                    snail.setPosition( touchX, touchY );
                    snail.pack();
                    stage.addActor( snail );
                    manaA -= snail.manaConsumption;
                    manaLabelA.setText( String.valueOf( manaA ) );
                    manaProgressA.setValue( manaA );
                    readyA = null;
                    tempCellA = cardSlotA.getCell( cardSlotA.findActor( refStringA ) );
                    cardSlotA.findActor( refStringA ).remove();
                    refStringA = null;
                    addCardA();
                }

            };

        } );

        
        for( int i = 0; i < 4; i ++ ) {
            addCardA();
        }

        for( int i = 0; i < 4; i ++ ) {
            addCardB();
        }
    }

    public void addCardA() {
        final int index = random.nextInt( 5 );
        final Image cardImage = cards.getCard( index, null );
        cardImage.setName( String.valueOf( random.nextInt( 999999 ) ) );
        cardImage.addListener( new ClickListener() {
            public void clicked( InputEvent event, float x, float y ) {
                if( manaA < cards.getManaConsumption( index ) )
                    return;
                if( refStringA != null )
                    cardSlotA.findActor( refStringA ).addAction( Actions.alpha( 1 ) );
                readyA = new Snail( world, 25, 0, 25, 25, false, cards.getSnail( index ), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
                cardImage.addAction( Actions.alpha( .5f ) );
                refStringA = cardImage.getName();
            };
        } );
        if( tempCellA != null ) {
            cardImage.addAction( Actions.sequence( Actions.visible( false ), Actions.delay( 3 ), Actions.visible( true ) ) );
            tempCellA.setActor( cardImage );
            return;
        }
        //cardSlotA.row();
        cardSlotA.add( cardImage ).size( 80 ).pad( 1 );
    }

    public void addCardB() {
        final int index = random.nextInt( 5 );
        final Image cardImage = cards.getCard( index, null );
        cardImage.setName( String.valueOf( random.nextInt( 999999 ) ) );
        cardImage.addListener( new ClickListener() {
            public void clicked( InputEvent event, float x, float y ) {
                if( manaB < cards.getManaConsumption( index ) )
                    return;
                if( refStringB != null )
                    cardSlotB.findActor( refStringB ).addAction( Actions.alpha( 1 ) );
                readyB = new Snail( world, vWidth -25, 0, 25, 25, true, cards.getSnail( index ), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
                cardImage.addAction( Actions.alpha( .5f ) );
                refStringB = cardImage.getName();
            };
        } );
        if( tempCellB != null ) {
            cardImage.addAction( Actions.sequence( Actions.visible( false ), Actions.delay( 3 ), Actions.visible( true ) ) );
            tempCellB.setActor( cardImage );
            return;
        }
        //cardSlotB.row();
        cardSlotB.add( cardImage ).size( 80 ).pad( 1 );
    }

    @Override
    public void render(float delta) {
        
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
        hud.getViewport().update( width, height, true );
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        hud.dispose();
        skin.dispose();
        
        snailTexture.dispose();
        terrainTexture.dispose();
        cardTexture.dispose();
        manaTexture.dispose();
        timer.cancel();
    }

    @Override
    public void hide() {
        dispose();
    }
    
}
