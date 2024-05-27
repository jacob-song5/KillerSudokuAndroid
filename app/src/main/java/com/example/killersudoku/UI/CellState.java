package com.example.killersudoku.UI;

import com.example.killersudoku.Models.Cell;

public class CellState
{
    public Mode mode;
    public int x;
    public int y;
    public int value;
    public boolean valid;
    public String noteValue;

    public CellState(Cell c)
    {
        this.x = c.x;
        this.y = c.y;
        this.value = c.value;
        this.valid = c.valid;
    }

    public enum Mode
    {
        NOTE,
        VALUE
    }
}
