package com.rondeo.bump;

import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;

public class BumpSnail extends Game {
	
	@Override
	public void create () {
		// Debug: Local 2 players
		// setScreen( new GameScreen() );

		// Multiplayer
		try {
			setScreen( new MultiplayerScreen() );
		} catch ( IOException e) {
			e.printStackTrace();
			Gdx.app.exit();
		}
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
