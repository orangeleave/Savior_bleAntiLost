package main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;


/**
 * Created by orange on 4/29/17.
 */

// override the baseactivity so that we can have the immersive mode of the sidebar
public class BaseActivity extends Activity{
	protected final static Intent intent = new Intent(); 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImmersiveMode.setImmersiveMode(this);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
        	return onKeyBack(keyCode, event);
    	}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public boolean onKeyBack(int keyCode, KeyEvent event){
		return super.onKeyDown(keyCode, event);
	}
	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
		return super.onOptionsItemSelected(item);

    }
    
    public void startActivity(Class<?> c) {
		intent.setClass(this, c);
		startActivity(intent);
	};

}
