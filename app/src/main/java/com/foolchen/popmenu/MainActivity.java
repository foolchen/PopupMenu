package com.foolchen.popmenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.foolchen.popmenu.widget.PopupMenu;

public class MainActivity extends AppCompatActivity {

    private PopupMenu mPopupMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPopupMenu = (PopupMenu) findViewById(R.id.popup_menu);
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupMenu.isChildDisplaying()) {
                    mPopupMenu.dismiss();
                } else {
                    mPopupMenu.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_left:
                mPopupMenu.setGravity(PopupMenu.LEFT);
                break;
            case R.id.action_top:
                mPopupMenu.setGravity(PopupMenu.TOP);
                break;
            case R.id.action_right:
                mPopupMenu.setGravity(PopupMenu.RIGHT);
                break;
            case R.id.action_bottom:
                mPopupMenu.setGravity(PopupMenu.BOTTOM);
                break;
        }
        findViewById(R.id.popup_menu).postInvalidate();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final int gravity = mPopupMenu.getGravity();
        menu.findItem(R.id.action_left).setChecked(gravity == PopupMenu.LEFT);
        menu.findItem(R.id.action_top).setChecked(gravity == PopupMenu.TOP);
        menu.findItem(R.id.action_right).setChecked(gravity == PopupMenu.RIGHT);
        menu.findItem(R.id.action_bottom).setChecked(gravity == PopupMenu.BOTTOM);
        return super.onPrepareOptionsMenu(menu);
    }
}
