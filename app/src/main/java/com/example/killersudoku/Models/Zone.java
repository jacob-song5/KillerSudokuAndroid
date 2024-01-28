package com.example.killersudoku.Models;

import java.util.*;

public class Zone
{
    public List<Cell> cells;
    private String id;

    public Zone()
    {
        cells = new ArrayList<Cell>();
        id = "";
    }

    @Override public String toString()
    {
        String output = String.format("%s\n", this.id);
        int sumTotal = 0;
        for (Cell c : this.cells)
        {
            sumTotal += c.value;
            output += String.format("(%s,%s)\n", c.x+1, c.y+1);
        }
        output += String.format("Total value=%s", sumTotal);
        return output;
    }

    public boolean equals(Zone z)
    {
        return this.id.equals(z.id);
    }

    public String getId()
    {
        return id;
    }

    public int getTotal()
    {
        int output = 0;

        for (Cell c : this.cells)
        {
            output += c.value;
        }

        return output;
    }

    public void add(Cell c)
    {
        this.cells.add(c);
        this.updateId();
    }

    public void remove(Cell c)
    {
        for (Cell x : cells)
        {
            if (x.value == c.value)
            {
                x.setUnowned();
                cells.remove(x);
                updateId();
                return;
            }
        }
    }

    public boolean contains(Cell j)
    {
        if (j == null)
            return false;

        for (Cell c : this.cells)
        {
            if (c.x == j.x && c.y == j.y)
                return true;
        }
        return false;
    }

    public boolean containsValue(int val)
    {
        for (Cell c : this.cells)
        {
            if (c.value == val)
                return true;
        }
        return false;
    }

    public Cell getTopLeftMostCell()
    {
        Cell output = new Cell(9, 9, 0);
        for (Cell c : this.cells)
        {
            if (c.y < output.y)
                output = c;
            else if (c.y == output.y && c.x < output.x)
                output = c;
        }
        return output;
    }

    public boolean isAlignedWith(Zone z)
    {
        HashSet<Integer> columns = new HashSet<Integer>();
        HashSet<Integer> rows = new HashSet<Integer>();
        HashSet<Integer> zColumns = new HashSet<Integer>();
        HashSet<Integer> zRows = new HashSet<Integer>();

        for (Cell c: this.cells)
        {
            columns.add(c.x);
            rows.add(c.y);
        }

        for (Cell c: z.cells)
        {
            zColumns.add(c.x);
            zRows.add(c.y);
        }

        if (columns.size() > 1 && zColumns.size() > 1)
            return columns.equals(zColumns);

        else if (rows.size() > 1 && zRows.size() > 1)
            return rows.equals(zRows);

        return false;
    }

    public boolean sameValuesWith(Zone z)
    {
        HashSet<Integer> val = new HashSet<Integer>();
        HashSet<Integer> zVal = new HashSet<Integer>();

        for (Cell c : this.cells)
            val.add(c.value);

        for (Cell c : z.cells)
            zVal.add(c.value);

        return val.equals(zVal);
    }

    private void updateId()
    {
        String name = "";
        for (Cell c : this.cells)
        {
            name += Integer.toString(c.x) + Integer.toString(c.y);
        }
        this.id = name;
    }


}