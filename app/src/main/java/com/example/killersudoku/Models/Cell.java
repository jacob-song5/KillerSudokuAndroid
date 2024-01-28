package com.example.killersudoku.Models;

public class Cell
{
    public int x;
    public int y;
    public int value;
    public boolean owned;
    public String parent;
    public boolean valid;
    // Cheater field for telling the UI that this cell should be revealed before the game starts
    public boolean reveal;

    public Cell(int x, int y, int value)
    {
        this.x = x;
        this.y = y;
        this.value = value;
        this.owned = false;
        this.parent = "";
        this.valid = true;
        this.reveal = false;
    }

    @Override public String toString()
    {
        return Integer.toString(this.value);
    }

    public void fullPrint()
    {
        String output = String.format("(%s,%s)=%s", this.x+1, this.y+1, this.value);
        System.out.println(output);
    }

    // Checks if two cells are from the same zone
    public boolean isNeighbor(Cell neighbor)
    {
        return (!neighbor.owned) || (neighbor.parent.equals(this.parent));
    }

    public void setUnowned()
    {
        this.owned = false;
        this.parent = "";
    }

    public boolean equals(Cell c)
    {
        return c != null && this.value == c.value;
    }
}