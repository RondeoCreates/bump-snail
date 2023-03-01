package com.rondeo.bump;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
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
import com.rondeo.bump.entity.Entity;
import com.rondeo.bump.entity.Snail;
import com.rondeo.bump.entity.Spell;
import com.rondeo.bump.util.DatabaseController;
import com.rondeo.bump.util.Network.Position;

public class MultiplayerScreen extends DatabaseController {
    World world;
    Box2DDebugRenderer debugRenderer;

    Stage stage;
    Skin skin;
    OrthographicCamera camera;
    int vWidth = 1000, vHeight = 500;
    InputMultiplexer inputMultiplexer;

    TextureAtlas assets;
    Texture terrainTexture;

    public MultiplayerScreen() throws IOException {

        assets = new TextureAtlas( Gdx.files.internal( "assets.atlas" ) );
        cardTexture = new Texture( Gdx.files.internal( "cards.png" ) );

        // Box2d
        Box2D.init();
        world = new World( new Vector2( 0, 0 ), false );
        world.setContactListener( new ContactListener() {
            private Spell tempSpell;
            private Snail tempSnail;
            @Override
            public void beginContact(Contact contact) {
                if( ( contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor() ) || ( !contact.getFixtureA().isSensor() && contact.getFixtureB().isSensor() ) ) {
                    // TODO: apply spell
                    tempSpell = (Spell) ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                    tempSnail = (Snail) ( contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                    if( tempSpell.target == tempSnail.flip ) {
                        tempSnail.power += tempSpell.power;
                        tempSnail.changeStat();
                    }
                }
            }
            @Override
            public void endContact(Contact contact) {
                if( ( contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor() ) || ( !contact.getFixtureA().isSensor() && contact.getFixtureB().isSensor() ) ) {
                    // TODO: apply spell
                    tempSpell = (Spell) ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                    tempSnail = (Snail) ( contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                    if( tempSpell.target == tempSnail.flip ) {
                        tempSnail.power -= tempSpell.power;
                        tempSnail.changeStat();
                    }
                }
            }
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) { }
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) { }
        } );

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
    Table table, cardSlotA;
    Texture cardTexture, manaTexture;
    TextButton manaLabelA;
    ProgressBar manaProgressA;
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
            }
        }, 0, 2000 );

        // Setup label and fill bar for mana
        manaTexture = new Texture( Gdx.files.internal( "mana.png" ) );
        TextButtonStyle manaStyle = new TextButtonStyle( new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), skin.getFont( "font" ) );
        ProgressBarStyle progressBarStyle = new ProgressBarStyle( skin.getDrawable( "black-tint" ), skin.getDrawable( "purple-tint" ) );
        progressBarStyle.background.setMinHeight( 16 );
        progressBarStyle.knob.setMinHeight( 12 );
        progressBarStyle.knobBefore = progressBarStyle.knob;

        manaLabelA = new TextButton( String.valueOf( manaA ), manaStyle );
        manaLabelA.pad( 2, 10, 0, 10 );
        manaProgressA = new ProgressBar( 0, 10, 1, false, progressBarStyle );
        manaProgressA.setValue( 10 );

        table.row();

        Table manaTable;

        manaTable = new Table( skin );
        manaTable.setBackground( new NinePatchDrawable( skin.getPatch( "flat-slot" ) ) );
        manaTable.pad( 5 );
        manaTable.add( manaLabelA );
        manaTable.add( manaProgressA ).fill().expand();
        table.add( manaTable ).fill();
    }

    Cards cards;
    Entity readyA, readyB;
    Position readyPosition;
    String refStringA, refStringB;
    Random random = new Random();
    Cell<Actor> tempCellA, tempCellB;
    int manaA = 10, manaB = 10;

    @Override
    public void placeOpponent( float x, float y, int index ) {
        if( cards.getType( index ) == Cards.SNAIL )
            readyB = new Snail( world, vWidth -25, 0, 25, 25, true, cards.getAnimation( assets, 24, 24, index  ), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
        else
            readyB = new Spell( world, 0, 0, 150, 50, true, cards.getAnimation( assets, 150, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? false : true, skin, cards.getManaConsumption( index ) );
        readyB.setPosition( vWidth - x, y );
        readyB.pack();
        stage.addActor( readyB );
    }

    public void init() {

        cards = new Cards( 
            TextureRegion.split( cardTexture, 36, 36 )[0], 
            new String[] { "snailv1", "snailv2", "snailv3", "snailv4", "snailv5", "spellR", "spellP", "spellP" }, 
            new int[] { 1, 2, 3, 4, 5, 3, -1, -3 }, 
            new int[] { 2, 3, 4, 6, 8, 4, 4, 4 },
            new int[] { 0, 0, 0, 0, 0, 1, 1, 1 }
        );

        stage.addListener( new InputListener() {
            float touchX, touchY;
            Entity entity;
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
                    indicator.setSize( 100, 100 );
                    if( readyA != null || readyB != null ) {
                        if( (readyA == null ? readyB : readyA) instanceof Spell ) {
                            indicator.setPosition( (x - x%100) - 100, y - y%100 );
                            indicator.setSize( 300, 100 );
                        }
                    }
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
                    indicator.setSize( 100, 100 );
                    if( readyA != null || readyB != null ) {
                        if( (readyA == null ? readyB : readyA) instanceof Spell ) {
                            indicator.setPosition( (x - x%100) - 100, y - y%100 );
                            indicator.setSize( 300, 100 );
                        }
                    }
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
                
                if( readyA == null )
                    return;
                entity = readyA;
                if( entity instanceof Snail && x > vWidth/2 )
                    return;
                if( manaA < entity.manaConsumption )
                    return;
                entity.setPosition( touchX, touchY );
                entity.pack();
                stage.addActor( entity );
                manaA -= entity.manaConsumption;
                manaLabelA.setText( String.valueOf( manaA ) );
                manaProgressA.setValue( manaA );
                readyA = null;
                tempCellA = cardSlotA.getCell( cardSlotA.findActor( refStringA ) );
                cardSlotA.findActor( refStringA ).remove();
                refStringA = null;
                addCardA();

                // Send position to oponent
                readyPosition.x = touchX;
                readyPosition.y = touchY;
                client.sendTCP( readyPosition );
            };

        } );
        
        for( int i = 0; i < 4; i ++ ) {
            addCardA();
        }
    }

    public void addCardA() {
        final int index = random.nextInt( 8 );
        final Image cardImage = cards.getCard( index, null );
        cardImage.setName( String.valueOf( random.nextInt( 999999 ) ) );
        cardImage.addListener( new ClickListener() {
            public void clicked( InputEvent event, float x, float y ) {

                cardSlotA.invalidateHierarchy();
                cardImage.addAction( Actions.moveBy( 0, 15, .15f ) );
                refStringA = cardImage.getName();
                
                if( cards.getType( index ) == Cards.SNAIL ) {
                    readyA = new Snail( world, 25, 0, 25, 25, false, cards.getAnimation( assets, 24, 24, index  ), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
                    readyPosition = new Position( opponentId, 25, 0, index );
                } else {
                    readyA = new Spell( world, 0, 0, 150, 50, false, cards.getAnimation( assets, 150, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? true : false, skin, cards.getManaConsumption( index ) );
                    readyPosition = new Position( opponentId, 0, 0, index );
                }
            };
        } );
        if( tempCellA != null ) {
            cardImage.addAction( Actions.sequence( Actions.color( Color.DARK_GRAY ), Actions.delay( 3 ), Actions.color( Color.WHITE ) ) );
            tempCellA.setActor( cardImage );
            return;
        }
        //cardSlotA.row();
        cardSlotA.add( cardImage ).size( 80 ).pad( 1 );
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
        
        assets.dispose();
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
