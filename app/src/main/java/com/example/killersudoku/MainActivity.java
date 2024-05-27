package com.example.killersudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
//import androidx.gridlayout.widget.GridLayout;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.killersudoku.Models.*;
import com.example.killersudoku.UI.GameDisplay;

import java.util.*;

public class MainActivity extends AppCompatActivity
{
    private GameDisplay display;

    private int defaultButtonColor;
    private int noteModeColor;
    private Random rand;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.defaultButtonColor = ContextCompat.getColor(this, R.color.purple);
        this.noteModeColor = ContextCompat.getColor(this, R.color.gold);
        this.rand = new Random();

        this.display = new GameDisplay(this);
    }

    public void onNumClick(View v)
    {
        Button b = (Button)v;
        String num = (String)b.getText();
        display.onNumClick(num);
        updateNumButtons();
    }

    // Spawns a confirmation popup window for starting a new game
    public void onNewGameClick(View v)
    {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.new_game_confirmation, null);

        int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 300);

        Button yes = popupView.findViewById(R.id.yesOption);
        yes.setOnClickListener((view) -> {
            startNewGame(view);
            popupWindow.dismiss();
        });

        Button no = popupView.findViewById(R.id.noOption);
        no.setOnClickListener((view) -> popupWindow.dismiss());
    }

    // Resets button colors/visibilities and starts a new game in the model
    public void startNewGame(View v)
    {
        Button noteButton = findViewById(R.id.noteButton);
        noteButton.setBackgroundColor(defaultButtonColor);
        display.resetGame();

        Button b;
        b = findViewById(R.id.button1);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button2);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button3);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button4);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button5);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button6);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button7);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button8);
        b.setVisibility(Button.VISIBLE);
        b = findViewById(R.id.button9);
        b.setVisibility(Button.VISIBLE);
    }

    public void toggleNoteMode(View v)
    {
        Button b = (Button)v;
        boolean noteMode = display.toggleNoteMode();

        if (noteMode)
            b.setBackgroundColor(noteModeColor);
        else
            b.setBackgroundColor(defaultButtonColor);
    }

    public void onEraseClick(View v)
    {
        display.onEraseClick();
        updateNumButtons();
    }

    public void onHintClick(View v)
    {
        display.onHintClick();
        updateNumButtons();
    }

    public void onUndoClick(View v)
    {
        display.onUndoClick();
        updateNumButtons();
    }

    private void updateNumButtons()
    {
        List<Integer> quantities = display.getNumCounts();
        List<Integer> buttonIds = new ArrayList<>(Arrays.asList(
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9
        ));
        for (int i = 0; i < 9; ++i)
        {
            Button b = findViewById(buttonIds.get(i));

            if (quantities.get(i) == 9)
                b.setVisibility(Button.INVISIBLE);

            else
                b.setVisibility(Button.VISIBLE);
        }
    }
}