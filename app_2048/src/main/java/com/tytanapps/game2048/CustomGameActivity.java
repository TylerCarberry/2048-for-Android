package com.tytanapps.game2048;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CustomGameActivity extends Activity {

    private enum Mode {XMODE, CORNER, ARCADE, SURVIVAL, SPEED, RUSH, GHOST}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_game);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_custom_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {

        Log.d("a", "Entering onclick");

        switch(view.getId()) {
            case R.id.create_game_button:
                createGame();
                break;
            case R.id.preview_game_button:
                updateGamePreview();
        }
    }

    private void createGame() {

        Log.d("a", "Entering creategame");

        int width = ((NumberPicker)findViewById(R.id.width_number_picker)).getValue();
        int height = ((NumberPicker)findViewById(R.id.height_number_picker)).getValue();

        List<Mode> gameModes = getSelectedGameModes();
        if(! isCustomGameValid(width, height, gameModes))
            return;

        Game game = new Game(width, height);

        if(gameModes.contains(Mode.XMODE))
            game.enableXMode();
        if(gameModes.contains(Mode.CORNER))
            game.enableCornerMode();
        if(gameModes.contains(Mode.SURVIVAL))
            game.enableSurvivalMode();
        game.setSpeedMode(gameModes.contains(Mode.SPEED));
        game.setDynamicTileSpawning(gameModes.contains(Mode.RUSH));
        game.setGhostMode(gameModes.contains(Mode.GHOST));
        game.finishedCreatingGame();

        File currentGameFile = new File(getFilesDir(), getString(R.string.file_current_game));
        try {
            Save.save(game, currentGameFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Switch to the game activity
        startActivity(new Intent(this, GameActivity.class));
    }

    private List<Mode> getSelectedGameModes() {
        boolean xmode = ((CheckBox)findViewById(R.id.xmode_checkbox)).isChecked();
        boolean cornerMode = ((CheckBox)findViewById(R.id.corner_mode_checkbox)).isChecked();
        boolean speedMode = ((CheckBox)findViewById(R.id.speed_mode_checkbox)).isChecked();
        boolean surivalMode = ((CheckBox)findViewById(R.id.survival_mode_checkbox)).isChecked();
        boolean rushMode = ((CheckBox)findViewById(R.id.rush_mode_checkbox)).isChecked();
        boolean ghostMode = ((CheckBox)findViewById(R.id.ghost_mode_checkbox)).isChecked();

        List<Mode> gameModes = new ArrayList<Mode>();

        if(xmode)
            gameModes.add(Mode.XMODE);
        if(cornerMode)
            gameModes.add(Mode.CORNER);
        if(speedMode)
            gameModes.add(Mode.SPEED);
        if(surivalMode)
            gameModes.add(Mode.SURVIVAL);
        if(rushMode)
            gameModes.add(Mode.RUSH);
        if(ghostMode)
            gameModes.add(Mode.GHOST);

        return gameModes;
    }

    private void updateGamePreview() {
        int width = ((NumberPicker)findViewById(R.id.width_number_picker)).getValue();
        int height = ((NumberPicker)findViewById(R.id.height_number_picker)).getValue();

        List<Mode> gameModes = getSelectedGameModes();
        View gamePreview =  generateGamePreview(width, height, gameModes);

        FrameLayout gamePreviewFrame = (FrameLayout) findViewById(R.id.game_preview_game_layout);
        gamePreviewFrame.removeAllViews();
        gamePreviewFrame.addView(gamePreview);

    }

    private View generateGamePreview(int width, int height, List<Mode> modes) {
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(width);
        gridLayout.setRowCount(height);
        gridLayout.setUseDefaultMargins(true);


        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                ImageView tileBackground = new ImageView(this);
                tileBackground.setImageResource(R.drawable.tile_blank);
                gridLayout.addView(tileBackground);

            }
        }

        int cornerTileResource = (modes.contains(Mode.GHOST)) ? R.drawable.tile_question : R.drawable.tile_corner;
        int xTileResource = (modes.contains(Mode.GHOST)) ? R.drawable.tile_question : R.drawable.tile_x;

        if(modes.contains(Mode.XMODE)) {
            ImageView XTile = new ImageView(this);
            XTile.setImageResource(xTileResource);
            GridLayout.Spec specRow, specCol;

            if (height >= 2 && width >= 2) {
                specCol = GridLayout.spec(1, 1);
                specRow = GridLayout.spec(1, 1);
            }
            else {
                if (modes.contains(Mode.CORNER) && (height >= 3 || width >= 3)) {
                    if (width > 2) {
                        specCol = GridLayout.spec(1, 1);
                        specRow = GridLayout.spec(0, 1);
                    } else {
                        specCol = GridLayout.spec(0, 1);
                        specRow = GridLayout.spec(1, 1);
                    }
                }
                else {
                    specCol = GridLayout.spec(0, 1);
                    specRow = GridLayout.spec(0, 1);
                }
            }
            GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(specRow, specCol);
            gridLayout.addView(XTile, gridLayoutParam);
        }

        if(modes.contains(Mode.CORNER)) {
            GridLayout.Spec specRow = GridLayout.spec(0, 1);
            GridLayout.Spec specCol = GridLayout.spec(0, 1);
            GridLayout.LayoutParams gridLayoutParam = new GridLayout.LayoutParams(specRow, specCol);

            // Add a blank tile to that spot on the grid
            ImageView cornerTile = new ImageView(this);
            cornerTile.setImageResource(cornerTileResource);
            gridLayout.addView(cornerTile, gridLayoutParam);

            cornerTile = new ImageView(this);
            cornerTile.setImageResource(cornerTileResource);
            specRow = GridLayout.spec(height - 1, 1);
            specCol = GridLayout.spec(0, 1);
            gridLayoutParam = new GridLayout.LayoutParams(specRow, specCol);
            gridLayout.addView(cornerTile, gridLayoutParam);

            cornerTile = new ImageView(this);
            cornerTile.setImageResource(cornerTileResource);
            specRow = GridLayout.spec(height - 1, 1);
            specCol = GridLayout.spec(width - 1, 1);
            gridLayoutParam = new GridLayout.LayoutParams(specRow, specCol);
            gridLayout.addView(cornerTile, gridLayoutParam);

            cornerTile = new ImageView(this);
            cornerTile.setImageResource(cornerTileResource);
            specRow = GridLayout.spec(0, 1);
            specCol = GridLayout.spec(width - 1, 1);
            gridLayoutParam = new GridLayout.LayoutParams(specRow, specCol);
            gridLayout.addView(cornerTile, gridLayoutParam);
        }

        return gridLayout;
    }

    private boolean isCustomGameValid(int width, int height, List<Mode> modes) {

        if(width * height < 1) {
            Toast.makeText(this, getString(R.string.error_grid_small), Toast.LENGTH_LONG).show();
            return false;
        }

        // If corner mode is enabled the width and height must be >2 and cannot both be 2
        if(modes.contains(Mode.CORNER) && ((width == 2 && height == 2) || (width < 2 || height < 2))) {
            Toast.makeText(this, "Corner Mode will not fit on that grid size", Toast.LENGTH_LONG).show();
            return false;
        }

        if(modes.contains(Mode.XMODE) && width * height <= 2) {
            Toast.makeText(this, "XMode cannot fit on that grid size", Toast.LENGTH_LONG).show();
            return false;
        }

        if(modes.contains(Mode.XMODE) && modes.contains(Mode.CORNER) && width * height <= 7) {
            Toast.makeText(this, "XMode and Corner Mode cannot fit on that grid size", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_custom_game, container, false);

            NumberPicker widthNumberPicker = (NumberPicker) rootView.findViewById(R.id.width_number_picker);
            NumberPicker heightNumberPicker = (NumberPicker) rootView.findViewById(R.id.height_number_picker);

            String[] values=new String[5];
            for(int i = 0; i < values.length; i++){
                values[i] = ""+(i+1);
            }

            for(String s : values)
                Log.d("a", s);

            widthNumberPicker.setMaxValue(5);
            widthNumberPicker.setMinValue(1);
            widthNumberPicker.setDisplayedValues(values);

            heightNumberPicker.setMaxValue(5);
            heightNumberPicker.setMinValue(1);
            heightNumberPicker.setDisplayedValues(values);


            widthNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    ((CustomGameActivity)getActivity()).updateGamePreview();
                }
            });

            heightNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    ((CustomGameActivity) getActivity()).updateGamePreview();
                }
            });

            CheckBox xModeCheckbox = (CheckBox) rootView.findViewById(R.id.xmode_checkbox);
            CheckBox cornerCheckbox = (CheckBox) rootView.findViewById(R.id.corner_mode_checkbox);
            CheckBox arcadeCheckbox = (CheckBox) rootView.findViewById(R.id.arcade_mode_checkbox);
            CheckBox speedCheckbox = (CheckBox) rootView.findViewById(R.id.speed_mode_checkbox);
            CheckBox survivalCheckbox = (CheckBox) rootView.findViewById(R.id.survival_mode_checkbox);
            CheckBox rushCheckbox = (CheckBox) rootView.findViewById(R.id.rush_mode_checkbox);
            CheckBox ghostCheckbox = (CheckBox) rootView.findViewById(R.id.ghost_mode_checkbox);


            CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((CustomGameActivity)getActivity()).updateGamePreview();
                }
            };

            xModeCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            cornerCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            arcadeCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            speedCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            survivalCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            rushCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            ghostCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);



            return rootView;
        }
    }
}
