package com.rondeo.bump;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryonet.Client;
import com.github.tommyettinger.textra.TypingConfig;
import com.github.tommyettinger.textra.TypingLabel;
import com.github.tommyettinger.textra.effects.HeartbeatEffect;
import com.rondeo.bump.components.Cards;
import com.rondeo.bump.entity.Damage;
import com.rondeo.bump.entity.Entity;
import com.rondeo.bump.entity.Snail;
import com.rondeo.bump.entity.Spell;
import com.rondeo.bump.util.DatabaseController;
import com.rondeo.bump.util.MatchInfoController;
import com.rondeo.bump.util.Rumble;
import com.rondeo.bump.util.Network.Account;
import com.rondeo.bump.util.Network.Position;

public class MultiplayerScreen extends DatabaseController {
    World world;
    Box2DDebugRenderer debugRenderer;

    Stage stage;
    Skin skin;
    Group actorGroup, foreGroup;
    OrthographicCamera camera;
    int vWidth = 1000, vHeight = 500;
    InputMultiplexer inputMultiplexer;
    TextureAtlas assets;
    Texture terrainTexture, cloudTexture;

    BumpSnail game;
    Preferences preferences;

    public MultiplayerScreen( BumpSnail game, Client client, int opponentId, String opponentUsername, String myUsername ) throws IOException {
        super( client, opponentId, new MatchInfoController( client, opponentId ) );
        this.game = game;
        this.oppNameS = opponentUsername;
        this.myNameS = myUsername;

        preferences = Gdx.app.getPreferences( "bump-snail-prefs" );
        connectServer( game, preferences.getString( "username" ), preferences.getString( "password" ) );

        assets = new TextureAtlas( Gdx.files.internal( "assets.atlas" ) );
        cardTexture = new Texture( Gdx.files.internal( "cards.png" ) );

        // Box2d
        Box2D.init();
        world = new World( new Vector2( 0, 0 ), false );
        world.setContactListener( new ContactListener() {
            private Spell tempSpell;
            private Snail tempSnail;
            private Damage tempDamage;
            @Override
            public void beginContact(Contact contact) {
                if( ( contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor() ) || ( !contact.getFixtureA().isSensor() && contact.getFixtureB().isSensor() ) ) {
                    // TODO: apply spell
                    if( ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() ) instanceof Damage ) {
                        tempDamage = (Damage) ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        tempSnail = (Snail) ( contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        if( tempDamage.target == tempSnail.flip ) {
                            tempSnail.health += tempDamage.power;
                            tempSnail.checkDeath();
                            // Screen shake
                            Rumble.rumble( 5f, 1f );
                        }
                    } else {
                        tempSpell = (Spell) ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        tempSnail = (Snail) ( contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        if( tempSpell.target == tempSnail.flip ) {
                            tempSnail.power += tempSpell.power;
                            tempSnail.changeStat();
                        }
                    }
                }
            }
            @Override
            public void endContact(Contact contact) {
                if( ( contact.getFixtureA().isSensor() && !contact.getFixtureB().isSensor() ) || ( !contact.getFixtureA().isSensor() && contact.getFixtureB().isSensor() ) ) {
                    // TODO: apply spell
                    if( ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() ) instanceof Damage ) { } else {
                        tempSpell = (Spell) ( !contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        tempSnail = (Snail) ( contact.getFixtureA().isSensor() ? contact.getFixtureB().getBody().getUserData() : contact.getFixtureA().getBody().getUserData() );
                        if( tempSpell.target == tempSnail.flip ) {
                            tempSnail.power -= tempSpell.power;
                            tempSnail.changeStat();
                        }
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
        actorGroup = new Group();
        foreGroup = new Group();
        stage.addActor( actorGroup );
        stage.addActor( foreGroup );

        // TileMap
        terrainTexture = new Texture( Gdx.files.internal( "terrain.png" ) );
        Image terrain = new Image( terrainTexture );
        terrain.setBounds( -400, -300, 1800, 1100 );
        actorGroup.addActor( terrain );

        // Cloud
        cloudTexture = new Texture( Gdx.files.internal( "clouds.png" ) );
        Image cloudImageLeft = new Image( cloudTexture );
        cloudImageLeft.setBounds( -350, -300, 1800, 1100 );
        foreGroup.addActor( cloudImageLeft );

        Image cloudImageRight = new Image( cloudTexture );
        cloudImageRight.setBounds( 1350, -300, -1800, 1100 );
        foreGroup.addActor( cloudImageRight );
        
        cloudImageLeft.addAction( Actions.moveBy( -600, 0, 2f ) );
        cloudImageRight.addAction( Actions.moveBy( 600, 0, 2f ) );

        debugRenderer = new Box2DDebugRenderer();
    }

    public void connectServer( final BumpSnail game, String username, String password ) throws IOException {
        Account account = new Account();
        account.connectionId = client.getID();
        account.username = username;
        account.password = password;
        account.action = 1;
        client.sendTCP( account );
    }
    
    // HUD
    Stage hud;
    Table table, cardSlotA;
    Texture cardTexture, manaTexture;
    TextButton manaLabelA;
    ProgressBar manaProgressA;
    TypingLabel label, myPointsLabel, oppPointsLabel;
    Label timerLabel;
    Timer timer;
    Label myNameLabel, oppNameLabel;

    @Override
    public void show() {
        playTheme2();

        hud = new Stage( new ExtendViewport( vWidth, vHeight ) );
        skin = new Skin( Gdx.files.internal( "default/default.json" ) );

        // Setup table
        table = new Table( skin );
        table.setFillParent( true );
        hud.addActor( table );
        
        // Time
        timerLabel = new Label( "", skin.get( "small", LabelStyle.class ) );
        timerLabel.setAlignment( Align.center );
        table.row();
        table.add();
        table.add( timerLabel );
        table.add();
        
        // Other UI
        table.row();
        TypingConfig.registerEffect( "BEAT", "ENDBEAT", HeartbeatEffect.class );
        label = new TypingLabel( "", skin );
        table.add();
        table.add( label ).expandY();
        table.add();

        // Cards
        table.row();

        cardSlotA = new Table( skin );
        cardSlotA.setBackground( new NinePatchDrawable( skin.getPatch( "default_window" ) ) );
        cardSlotA.pad( 5 );
        VerticalGroup vGroup;
        vGroup = new VerticalGroup();
        vGroup.addActor( myPointsLabel = labelPoints( myPointsLabel ) );
        vGroup.addActor( myNameLabel = labelName( myNameLabel, myNameS ) );
        table.add( vGroup ).fill().minWidth( 200 );;
        table.add( cardSlotA );
        vGroup = new VerticalGroup();
        vGroup.addActor( oppPointsLabel = labelPoints( oppPointsLabel ) );
        vGroup.addActor( oppNameLabel = labelName( oppNameLabel, oppNameS ) );
        table.add( vGroup ).fill().minWidth( 200 );

        matchInfoController.setLabel( myPointsLabel );

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
                
                if( STARTTIME > 0 ) {
                    STARTTIME --;
                    switch( STARTTIME ) {
                        case 3:
                            label.setText( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}READY{ENDBEAT}[%]" );
                            playCountdownA();
                            break;
                        case 2:
                            label.setText( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}SET{ENDBEAT}[%]" );
                            playCountdownA();
                            break;
                        case 1:
                            label.setText( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}BUMP{ENDBEAT}[%]" );
                            playCountdownB();
                            break;
                        default:
                            label.setText( "" );
                    }
                }
            }
        }, 0, 1500 );

        // Setup label and fill bar for mana
        manaTexture = new Texture( Gdx.files.internal( "mana.png" ) );
        TextButtonStyle manaStyle = new TextButtonStyle( new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), new TextureRegionDrawable( manaTexture ), skin.getFont( "font-export" ) );

        manaLabelA = new TextButton( String.valueOf( manaA ), manaStyle );
        manaLabelA.pad( 2, 10, 0, 10 );
        manaProgressA = new ProgressBar( 0, 10, 1, false, skin );
        manaProgressA.setValue( 10 );

        table.row();

        Table manaTable;

        manaTable = new Table( skin );
        manaTable.setBackground( new NinePatchDrawable( skin.getPatch( "default_window" ) ) );
        manaTable.pad( 5 );
        manaTable.add( manaLabelA );
        manaTable.add( manaProgressA ).fill().expand();

        table.add();
        table.add( manaTable ).fill();
        table.add();

        //hud.setDebugAll( true );
        initForeground();
    }

    public TypingLabel labelPoints( TypingLabel label ) {
        label = new TypingLabel( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}" + 0 + " POINTS{ENDBEAT}[%]" , skin );
        label.setAlignment( Align.center );
        return label;
    }
    
    public Label labelName( Label label, String data ) {
        label = new Label( "@" + data , skin.get( "small", LabelStyle.class ) );
        label.setAlignment( Align.center );
        return label;
    }

    String myNameS = "", oppNameS = "";
    Integer myPointsI = 0, oppPointsI = 0;
    
    @Override
    public void updateOpponentInfo( int points ) {
        oppPointsI = points;
        oppPointsLabel.setText( "[%?BLACK OUTLINE]{BEAT=1.0;0.5;1.0}[FIREBRICK]" + points + " POINTS{CLEARCOLOR}{ENDBEAT}[%]");
        //oppPointsLabel.setText( points + " POINTS" );
    }

    Cards cards;
    Entity readyA, readyB;
    Position readyPosition;
    String refStringA, refStringB;
    Random random = new Random();
    Cell<Actor> tempCellA, tempCellB;
    int manaA = 10, manaB = 10;

    @Override
    public void placeOpponent( final float x, final float y, int index ) {
        switch( cards.getType( index ) ) {
            case Cards.SNAIL:
                readyB = new Snail( index, matchInfoController, world, vWidth -25, 0, 25, 25, true, cards.getAnimation( assets, 24, 24, index  ), assets.findRegions( "clock" ).toArray(), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
            break;
            case Cards.SPELL:
                readyB = new Spell( index, world, 0, 0, 150, 50, true, cards.getAnimation( assets, 150, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? false : true, skin, cards.getManaConsumption( index ) );
            break;
            case Cards.DMG:
                readyB = new Damage( index, world, 0, 0, 50, 50, true, cards.getAnimation( assets, 50, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? false : true, skin, cards.getManaConsumption( index ) );
            break;
        }
        Gdx.app.postRunnable( new Runnable() {
            @Override
            public void run() {
                readyB.setPosition( vWidth - x, y );
                readyB.pack();
                actorGroup.addActor( readyB );
            };
        } );
        
    }

    public void init() {

        cards = new Cards( 
            TextureRegion.split( cardTexture, 36, 36 )[0], 
            new String[] { "snailv1", "snailv2", "snailv3", "snailv4", "snailv5", "spellR", "thunder", "spellP" }, 
            new int[] { 1, 2, 3, 4, 5, 3, -5, -3 }, 
            new int[] { 2, 3, 4, 6, 8, 4, 4, 4 },
            new int[] { 0, 0, 0, 0, 0, 1, 2, 1 }
        );

        stage.addListener( new InputListener() {
            float touchX, touchY;
            Entity entity;
            Image indicator, redIndicator;
            {
                indicator = new Image( skin.getDrawable( "select-overlay" ) );
                indicator.setVisible( false );
                indicator.setSize( 100, 100 );
                stage.addActor( indicator );

                redIndicator = new Image( skin.getDrawable( "red_indicator" ) );
                redIndicator.setVisible( false );
                redIndicator.setBounds( vWidth/2, 0, vWidth/2, vHeight );
                redIndicator.addAction( Actions.forever( Actions.sequence( Actions.alpha( .3f, .15f ), Actions.alpha( 1f, .15f ) ) ) );
                stage.addActor( redIndicator );
            }

            @Override
            public boolean touchDown( InputEvent event, float x, float y, int pointer, int button ) {
                if( !started || end )
                    return false;
                // Show indicator
                if( x > 0 && x < vWidth && y > 0 && y < vHeight ) {
                    indicator.setVisible( true );
                    indicator.setPosition( x - x%100, y - y%100 );
                    indicator.setSize( 100, 100 );
                    if( readyA != null || readyB != null ) {
                        if( ((readyA == null ? readyB : readyA) instanceof Snail) )
                            redIndicator.setVisible( true );
                        if( !((readyA == null ? readyB : readyA) instanceof Damage) && (readyA == null ? readyB : readyA) instanceof Spell ) {
                            indicator.setPosition( (x - x%100) - 100, y - y%100 );
                            indicator.setSize( 300, 100 );
                        }
                    }
                    return true;
                } else {
                    indicator.setVisible( false );
                    redIndicator.setVisible( false );
                }
                return false;
            };

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if( !started || end )
                    return;
                if( x > 0 && x < vWidth && y > 0 && y < vHeight ) {
                    indicator.setVisible( true );
                    indicator.setPosition( x - x%100, y - y%100 );
                    indicator.setSize( 100, 100 );
                    if( readyA != null || readyB != null ) {
                        if( ((readyA == null ? readyB : readyA) instanceof Snail) )
                            redIndicator.setVisible( true );
                        if( !((readyA == null ? readyB : readyA) instanceof Damage) && (readyA == null ? readyB : readyA) instanceof Spell ) {
                            indicator.setPosition( (x - x%100) - 100, y - y%100 );
                            indicator.setSize( 300, 100 );
                        }
                    }
                } else {
                    indicator.setVisible( false );
                    redIndicator.setVisible( false );
                }
            };

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if( !started || end )
                    return; 
                // hide indicator
                if( x < 0 || x > vWidth || y < 0 || y > vHeight )
                    return;
                indicator.setVisible( false );
                redIndicator.setVisible( false );
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
                actorGroup.addActor( entity );
                manaA -= entity.manaConsumption;
                manaLabelA.setText( String.valueOf( manaA ) );
                manaProgressA.setValue( manaA );
                readyA = null;
                tempCellA = cardSlotA.getCell( cardSlotA.findActor( refStringA ) );
                cardSlotA.findActor( refStringA ).remove();
                refStringA = null;
                slotIndices.removeValue( entity.index, true );
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

    Array<Integer> slotIndices = new Array<>();

    public void addCardA() {

        int tempIndex2 = -1;
        while( true ) {
            int tempIndex1 = random.nextInt( 8 );
            if( !slotIndices.contains( tempIndex1, true ) ) {
                slotIndices.add( tempIndex1 );
                tempIndex2 = tempIndex1;
                break;
            }
        }
        final int index = tempIndex2;
        
        final Image cardImage = cards.getCard( index, null );
        cardImage.setName( String.valueOf( random.nextInt( 999999 ) ) );
        cardImage.addListener( new ClickListener() {
            public void clicked( InputEvent event, float x, float y ) {

                cardSlotA.invalidateHierarchy();
                cardImage.addAction( Actions.moveBy( 0, 15, .15f ) );
                refStringA = cardImage.getName();
                switch( cards.getType( index ) ) {
                    case Cards.SNAIL:
                        readyA = new Snail( index, matchInfoController, world, 25, 0, 25, 25, false, cards.getAnimation( assets, 24, 24, index  ), assets.findRegions( "clock" ).toArray(), cards.getPower( index ), false, skin, cards.getManaConsumption( index ) );
                        readyPosition = new Position( opponentId, 25, 0, index );
                    break;
                    case Cards.SPELL:
                        readyA = new Spell( index, world, 0, 0, 150, 50, false, cards.getAnimation( assets, 150, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? true : false, skin, cards.getManaConsumption( index ) );
                        readyPosition = new Position( opponentId, 0, 0, index );
                    break;
                    case Cards.DMG:
                        readyA = new Damage( index, world, 0, 0, 50, 50, false, cards.getAnimation( assets, 50, 50, index  ), cards.getPower( index ), cards.getPower( index ) < 0 ? true : false, skin, cards.getManaConsumption( index ) );
                        readyPosition = new Position( opponentId, 0, 0, index );
                    break;
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

    // Foregrounds
    Stage postStage;
    Table postTable;
    Label oppScoreEnd, oppNameEnd, myScoreEnd, myNameEnd;

    public void initForeground() {
        postStage = new Stage( new ExtendViewport( vWidth, vHeight ) );
        postTable = new Table( skin );
        postTable.setFillParent( true );
        postStage.addActor( postTable );

        oppScoreEnd = new Label( "0", skin.get( "big", LabelStyle.class ) );
        //oppPoints = new Label( "0", skin );
        oppNameEnd = new Label( "@default", skin.get( "small", LabelStyle.class ) );

        myScoreEnd = new Label( "0", skin.get( "big", LabelStyle.class ) );
        matchInfoController.setLabel( myScoreEnd );
        //myPoints = new Label( "0", skin );
        myNameEnd = new Label( "@default", skin.get( "small", LabelStyle.class ) );

        postTable.add( oppScoreEnd );
        //postTable.row();
        //postTable.add( oppPoints );
        postTable.row();
        postTable.add( oppNameEnd );

        Label vsLabel = new Label( "VS", skin );
        postTable.row();
        postTable.add( vsLabel ).pad( 20 );

        postTable.row();
        postTable.add( myScoreEnd );
        //postTable.row();
        //postTable.add( myPoints );
        postTable.row();
        postTable.add( myNameEnd );

        postTable.row();
        postTable.add().pad( 20 );

        TextButton homeButton = new TextButton( "HOME", skin.get( "secondary", TextButtonStyle.class ) );
        homeButton.addListener( new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; };
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen( new MenuScreen( game ) );
            };
        } );
        postTable.row();
        postTable.add( homeButton );

        postTable.setBackground( skin.getDrawable( "semi_trans_black" ) );

        inputMultiplexer.addProcessor( postStage );
    }

    // Timers
    int TIMELIMIT = 60 * 3;
    int STARTTIME = 4;
    boolean started = false;
    boolean end = false;

    Vector2 cameraPosition = new Vector2();

    @Override
    public void render(float delta) {
        if( !end )
            stage.act( delta );
        stage.draw();

        if( !end )
            hud.act( delta );
        hud.draw();

        world.step(1/60f, 6, 2);
        //debugRenderer.render( world, camera.combined );

        if( !started && STARTTIME <= 0 ) {
            TIMELIMIT += TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() );
            started = true;
        }
        if( started ) {
            if( TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) > TIMELIMIT ) {
                // End Match
                if( !end ) {
                    oppNameEnd.setText( "@" + oppNameS );
                    myNameEnd.setText( "@" + myNameS );

                    Account account = new Account();
                    account.ID = preferences.getInteger( "ID" );
                    account.points = preferences.getInteger( "points" ) + matchInfoController.points;
                    account.action = 2;
                    client.sendTCP( account );
                    
                    end = true;
                }
                postStage.act();
                postStage.draw();
                oppScoreEnd.setText( oppPointsI );
                myScoreEnd.setText( myPointsI );
            } else {
                timerLabel.setText( String.format( "Time\n%d:%02d", ( TIMELIMIT - TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) ) / 60, ( TIMELIMIT - TimeUnit.MILLISECONDS.toSeconds( System.currentTimeMillis() ) ) % 60 ) );
            }
        }

        // Screenshakes
        cameraPosition.set( MathUtils.lerp( camera.position.x, camera.viewportWidth/2, Gdx.graphics.getDeltaTime() * 2f ), MathUtils.lerp( camera.position.y, (camera.viewportHeight/2)-100, Gdx.graphics.getDeltaTime() * 2 ) );
        camera.position.set( cameraPosition.x, cameraPosition.y, 0 );
        if( Rumble.getRumbleTimeLeft() > 0 ) {
            Rumble.tick( Gdx.graphics.getDeltaTime() );
            camera.translate( Rumble.getPos() );
        }
    }

    @Override
    public void resize( int width, int height ) {
        stage.getViewport().update( width, height, true );
        hud.getViewport().update( width, height, true );
        postStage.getViewport().update( width, height, true );

        super.resize(width, height);
    }

    @Override
    public void dispose() {
        stage.dispose();
        hud.dispose();
        postStage.dispose();
        skin.dispose();
        world.dispose();
        client.close();
        
        assets.dispose();
        terrainTexture.dispose();
        cardTexture.dispose();
        manaTexture.dispose();
        timer.cancel();

        super.dispose();
    }

    @Override
    public void hide() {
        dispose();
    }
}
