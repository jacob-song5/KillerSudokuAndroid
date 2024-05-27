package com.example.killersudoku.Models;

import com.example.killersudoku.UI.CellState;

import java.util.*;

public class BoardTracker
{
    private Stack<Board> states;
    private Stack<List<CellState>> cellChanges;

    public BoardTracker(Board b)
    {
        this.states = new Stack<>();
        this.cellChanges = new Stack<>();
    }

    public Board getLastBoardState()
    {
        if (states.size() > 0)
            return states.pop();
        return null;
    }

    public void saveBoardState(Board b)
    {
        Board x = new Board(b);
        x.cloneZones(b);
        states.push(x);
    }

    public List<CellState> getLastCellChanges()
    {
        if (cellChanges.size() > 0)
            return cellChanges.pop();
        return new ArrayList<CellState>();
    }

    public void saveCellChanges(List<CellState> changes)
    {
        cellChanges.push(changes);
    }
}
