package com.rondeo.bump;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.ScreenUtils;

public class BumpSnail extends Game {
	
	@Override
	public void create () {
		// Debug: Local 2 players
		// setScreen( new GameScreen() );

		// Multiplayer
		setScreen( new MultiplayerScreen() );
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		
		super.render();
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
}
