package com.sergiowebo;

import com.sergiowebo.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		// Iniciamos la animacion
		startAnimating();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// Deshabilitamos el menu en esta pantalla
		//getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}
	
	/**
     * Metodo que contiene la logica de la animacion de la splashscreen
     */
    private void startAnimating() {
    	// Instanciamos los elementos del layout
    	TextView titulo = (TextView) findViewById(R.id.app_title);
    	TextView descripcion = (TextView) findViewById(R.id.app_description);
    	ImageView logo =  (ImageView) findViewById(R.id.imageView1);
    	
    	// Creamos la animacion
        Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fade2 = AnimationUtils.loadAnimation(this, R.anim.fade_in2);

        // Asociamos las animaciones a los elementos
        titulo.startAnimation(fade1);
        logo.startAnimation(fade1);
        // Esta animacion lleva un pequeño delay y sera la ultima en finalizar
        descripcion.startAnimation(fade2);

        // La transicion a la MainActivity se produce al finalizar la ultima animacion
        fade2.setAnimationListener(new AnimationListener() {

            public void onAnimationEnd(Animation animation) {
                // The animation has ended, transition to the Main Menu screen
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                SplashActivity.this.finish();
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        

    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop the animation
    	// Instanciamos los elementos del layout
    	TextView titulo = (TextView) findViewById(R.id.app_title);
    	TextView descripcion = (TextView) findViewById(R.id.app_description);
    	ImageView logo =  (ImageView) findViewById(R.id.imageView1);
    	
    	// Reiniciamos la animacion
    	titulo.clearAnimation();
    	descripcion.clearAnimation();
    	logo.clearAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cuando vuelva el foco a la activity se recupera la animacion por completo
        // De esta manera no se vera "raro" si continua donde se quedo
        startAnimating();
    }

}
