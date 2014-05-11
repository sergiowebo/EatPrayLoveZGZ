package com.sergiowebo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sergiowebo.R;
import com.sergiowebo.data.menuAdapter;
import com.sergiowebo.data.menuItem;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Instanciamos el objeto ListView
		ListView menuList = (ListView)findViewById(R.id.ListView_Menu);
		
		// Creamos un array con los elementos del menu
		final menuItem mItem[] = new menuItem[] {
			new menuItem(R.drawable.monumentos, R.string.menu_item_monumentos),
			new menuItem(R.drawable.restaurantes, R.string.menu_item_restaurantes),
			new menuItem(R.drawable.alojamiento, R.string.menu_item_alojamientos),
			new menuItem(R.drawable.favoritos, R.string.menu_item_favoritos)
		};
		
		// Rellanamos el adaptaador
		menuAdapter adapter = new menuAdapter(this, R.layout.menu_item, mItem);
		menuList.setAdapter(adapter);
		
		// Creamos los listener para cada objeto del menu
		menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	if (position == 0) {
						// Pantalla de Monumentos Activity
						startActivity(new Intent(MainActivity.this, SeeingActivity.class));
					} else if (position == 1) {
						// Pantalla de Restaurantes Activity
						startActivity(new Intent(MainActivity.this, EatingActivity.class));
					} else if (position == 2) {
						// Pantalla de Alojamientos Activity
						startActivity(new Intent(MainActivity.this, SleepingActivity.class));
					} else if (position == 3) {
						// Pantalla de Favoritos Activity
						startActivity(new Intent(MainActivity.this, BookmarkActivity.class));
					}
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.action_about:
	        	// Lanzamos la activity de About
				startActivity(new Intent(MainActivity.this, AboutActivity.class));
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
}

