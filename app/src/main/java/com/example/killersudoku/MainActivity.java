package com.example.killersudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
//import androidx.gridlayout.widget.GridLayout;
import android.widget.Button;
import android.widget.GridLayout;
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
    }

    public void onNewGameClick(View v)
    {
        Button noteButton = findViewById(R.id.noteButton);
        noteButton.setBackgroundColor(defaultButtonColor);
        display.resetGame();
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
    }

    public void onHintClick(View v)
    {
        display.onHintClick();
    }
}