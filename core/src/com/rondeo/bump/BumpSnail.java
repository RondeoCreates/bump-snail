package com.rondeo.bump;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

public class BumpSnail extends Game {
	
	@Override
	public void create () {
		// Debug: Local 2 players
		// setScreen( new GameScreen() );

		// Multiplayer
		setScreen( new MenuScreen( this ) );
	}

	@Override
	public void render () {
        ScreenUtils.clear( Color.valueOf( "#6b99c9" ) );
		
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
