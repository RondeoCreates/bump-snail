package com.rondeo.bump.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class MusicController extends ScreenAdapter {
    public Music theme1, theme2;
    public Sound count_a, count_b;

    {
        theme1 = Gdx.audio.newMusic( Gdx.files.internal( "sounds/night_theme.wav" ) );
        theme1.setLooping( true );

        theme2 = Gdx.audio.newMusic( Gdx.files.internal( "sounds/cave_theme.wav" ) );
        theme2.setLooping( true );

        count_a = Gdx.audio.newSound( Gdx.files.internal( "sounds/countdown_a.ogg" ) );
        count_b = Gdx.audio.newSound( Gdx.files.internal( "sounds/countdown_b.ogg" ) );
    }

    public void playTheme2() {
        theme2.play();
    }

    public void playTheme1() {
        theme1.play();
    }

    public void playCountdownA() {
        count_a.play();
    }

    public void playCountdownB() {
        count_b.play();
    }

    @Override
    public void dispose() {
        theme1.dispose();
        theme2.dispose();
        count_a.dispose();
        count_b.dispose();

        super.dispose();
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
    public void hide() {
        super.hide();
    }

}
