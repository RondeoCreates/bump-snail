package com.rondeo.bump.util;

import com.badlogic.gdx.ScreenAdapter;

public class FirebaseController extends ScreenAdapter {
    
    public FirebaseController() {
        GdxFIRApp.inst().configure();
    }
    
}
