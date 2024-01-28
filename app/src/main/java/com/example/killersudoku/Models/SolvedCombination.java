package com.example.killersudoku.Models;

public class SolvedCombination
{
    public int zoneTotal;
    public int zoneSize;
    public String noteString;

    public SolvedCombination(int zoneTotal, int zoneSize, String noteString)
    {
        this.zoneTotal = zoneTotal;
        this.zoneSize = zoneSize;
        this.noteString = noteString;
    }
}
