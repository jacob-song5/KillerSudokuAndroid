package com.example.killersudoku.Models;
import java.util.*;
import java.util.stream.Collectors;

public class Board
{
    private List<List<Cell>> board;
    public List<Zone> zones;
    private Random rand;

    public Board(String srcString)
    {
        this.board = new ArrayList<List<Cell>>();
        this.zones = new ArrayList<Zone>();
        this.rand = new Random();

        int size = srcString.length();

        for (int i = 0; i < 9; ++i)
        {
            this.board.add(new ArrayList<Cell>());

            for (int j = 0; j < 9; ++j)
            {
                if (size > 0)
                {
                    int srcIndex = (i * 9) + j;
                    Cell c = new Cell(j, i, Integer.parseInt(String.valueOf(srcString.charAt(srcIndex))));
                    this.board.get(i).add(c);
                }
                // Empty string passed in constructor will fill out an empty board with all zeroes for values
                else
                {
                    Cell c = new Cell(j, i, 0);
                    this.board.get(i).add(c);
                }
            }
        }
    }

    public Board(Board b)
    {
        this.board = new ArrayList<List<Cell>>();
        this.zones = new ArrayList<Zone>();
        this.rand = new Random();

        for (int i = 0; i < 9; ++i)
        {
            this.board.add(new ArrayList<Cell>());

            for (int j = 0; j < 9; ++j)
            {
                Cell c = new Cell(j, i, b.getCell(j, i).value);
                this.board.get(i).add(c);
            }
        }
    }

    public boolean isFull()
    {
        for (int i = 0; i < 9; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                if (this.board.get(j).get(i).value == 0)
                    return false;
            }
        }
        return true;
    }

    public void simplePrint()
    {
        System.out.println();
        for (int i = 0; i < 9; ++i)
        {
            System.out.println(this.board.get(i));
        }
        System.out.println();
    }

    public void printZones()
    {
        for (Zone z : this.zones)
        {
            System.out.println(z);
            System.out.println("\n\n");
        }
    }

    public Cell getCell(int x, int y)
    {
        return this.board.get(y).get(x);
    }

    public Cell getCell(Cell c)
    {
        return getCell(c.x, c.y);
    }

    public void addZone(Zone z)
    {
        this.zones.add(z);
    }

    public int getZoneTotal()
    {
        int total = 0;
        for (Zone z : this.zones)
        {
            total += z.getTotal();
        }
        return total;
    }

    public void markCellOwned(int x, int y)
    {
        this.board.get(y).get(x).owned = true;
    }

    public void markCellOwned(Cell c)
    {
        this.board.get(c.y).get(c.x).owned = true;
    }

    // Causes a zone to mark ownership of all of its cells
    public void zoneAttrition(Zone z)
    {
        for (Cell c : z.cells)
        {
            Cell boardCell = this.board.get(c.y).get(c.x);
            boardCell.owned = true;
            boardCell.parent = z.getId();
        }
    }

    public Cell getRandomUnownedCell()
    {
        List<Cell> unowned = new ArrayList<Cell>();

        for (List<Cell> row : this.board)
        {
            List<Cell> unownedRow = row.stream()
                    .filter(x -> !x.owned)
                    .collect(Collectors.toList());
            unowned.addAll(unownedRow);
        }

        int size = unowned.size();
        if (size > 1)
        {
            Random rand = new Random();
            Cell c = unowned.get(rand.nextInt(size));
            return this.board.get(c.y).get(c.x);
        }
        else if (size == 1)
        {
            Cell c = unowned.get(0);
            return this.board.get(c.y).get(c.x);
        }
        return null;
    }

    public Cell getRandomAdjacentCell(Zone z)
    {
        HashSet<Cell> availableCells = new HashSet<Cell>();

        int size = z.cells.size();
        for (int i = 0; i < size; ++i)
        {
            List<Cell> availableNeighbors = getAvailableCellNeighbors(z.cells.get(i));
            for (Cell c : availableNeighbors)
            {
                if (!z.containsValue(c.value))
                    availableCells.add(c);
            }
        }

        int avSize = availableCells.size();
        if (avSize > 0)
        {
            List<Cell> av = availableCells.stream().collect(Collectors.toList());
            Random rand = new Random();
            return av.get(rand.nextInt(avSize));
        }
        return null;
    }

    public List<Cell> getAvailableCellNeighbors(Cell homeCell)
    {
        List<Cell> output = new ArrayList<Cell>();
        Cell c;

        c = getCellAbove(homeCell);
        if (c != null && !c.owned)
            output.add(c);

        c = getCellRight(homeCell);
        if (c != null && !c.owned)
            output.add(c);

        c = getCellBelow(homeCell);
        if (c != null && !c.owned)
            output.add(c);

        c = getCellLeft(homeCell);
        if (c != null && !c.owned)
            output.add(c);

        return output;
    }

    public Cell getCellAbove(Cell c)
    {
        if (c == null || c.y == 0)
            return null;
        return this.board.get(c.y-1).get(c.x);
    }

    public Cell getCellBelow(Cell c)
    {
        if (c.y == 8)
            return null;
        return this.board.get(c.y+1).get(c.x);
    }

    public Cell getCellRight(Cell c)
    {
        if (c.x == 8)
            return null;
        return this.board.get(c.y).get(c.x+1);
    }

    public Cell getCellLeft(Cell c)
    {
        if (c.x == 0)
            return null;
        return this.board.get(c.y).get(c.x-1);
    }

    public boolean checkSolution()
    {
        for (int i = 0; i < 9; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                if (!isCellValid(this.board.get(j).get(i)))
                    return false;
            }
        }
        return true;
    }

    public boolean isCellValid(Cell c)
    {
        if (c == null || c.value == 0)
            return false;
        return !checkCollisionCells(c);
    }

    public Zone getZoneOfCell(Cell c)
    {
        for (Zone z : this.zones)
        {
            if (z.contains(c))
                return z;
        }
        return null;
    }

    // Divides all cells into randomly generated zones
    public void setBoardZones()
    {
        if (zones.size() > 0)
            zones = new ArrayList<Zone>();

        Cell c = getRandomUnownedCell();
        Zone z = new Zone();
        while (c != null)
        {
            int zoneSize = z.cells.size();
            if (zoneSize < 2)
            {
                z.add(c);
                zoneAttrition(z);
                c = getRandomAdjacentCell(z);
                if (c == null)
                {
                    addZone(z);
                    z = new Zone();
                    c = getRandomUnownedCell();
                }
            }
            else if (zoneSize < 5 && rand.nextBoolean())
            {
                z.add(c);
                zoneAttrition(z);
                c = getRandomAdjacentCell(z);
                if (c == null)
                {
                    addZone(z);
                    z = new Zone();
                    c = getRandomUnownedCell();
                }
            }
            else
            {
                addZone(z);
                z = new Zone();
                c = getRandomUnownedCell();
            }
        }

        int zoneSize = z.cells.size();
        if (zoneSize > 0)
        {
            for (Cell cell : z.cells)
                markCellOwned(cell);
            addZone(z);
        }

        // Alter zones to ensure the board has only one solution
        /*
        boolean containsLinkedZones = fixLinkedZones();
        while (containsLinkedZones)
            containsLinkedZones = fixLinkedZones();
         */
        fixLinkedCells();
    }

    // Returns how many of a given number are present on the board (e.g. there are seven 9's on the board)
    public int quantityOfNumber(int num)
    {
        int total = 0;
        for (int i = 0; i < 9; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                if (board.get(j).get(i).value == num)
                    total++;
            }
        }
        return total;
    }

    public int quantityOfNumber(String num)
    {
        return quantityOfNumber(Integer.valueOf(num));
    }

    // true if any cells collide
    private boolean checkCollisionCells(Cell c)
    {
        // Check rows and columns
        for (int i = 0; i < 9; ++i)
        {
            if (i != c.x && c.equals(this.board.get(c.y).get(i)))
            {
                // System.out.println(String.format("Cell (%s,%s) was found to have the same value %s as (%s,%s) from row", i+1, c.y+1, c.value, c.x+1, c.y+1));
                return true;
            }
            if (i != c.y && c.equals(this.board.get(i).get(c.x)))
            {
                // System.out.println(String.format("Cell (%s,%s) was found to have the same value %s as (%s,%s) from column", c.x+1, i+1, c.value, c.x+1, c.y+1));
                return true;
            }
        }

        int lowerXBound = (c.x / 3) * 3;
        int lowerYBound = (c.y / 3) * 3;


        // Check 3x3 section
        for (int y = lowerYBound; y < lowerYBound + 3; ++y)
        {
            for (int x = lowerXBound; x < lowerXBound + 3; ++x)
            {
                if ((x != c.x || y != c.y) && c.equals(this.board.get(y).get(x)))
                {
                    // System.out.println(String.format("Cell (%s,%s) was found to have the same value %s as (%s,%s) from section", x, y, c.value, c.x, c.y));
                    return true;
                }
            }
        }

        return false;
    }

    private void fixLinkedCells()
    {
        // HORIZONTAL
        // i < 8 because we are moving a 2-wide column (x) window
        for (int x = 0; x < 8; ++x)
        {
            // Compare each row (y) against every other row to find linked values
            // Left and right cells of pairs HAVE to come from the same zone, as separated zones would indicate a separate way to solve the loop
            for (int y = 0; y < 9; ++y)
            {
                Cell left = getCell(x, y);
                Cell right = getCell(x+1, y);

                checkCellPairForLoops(left, right, true);
            }
        }

        // VERTICAL
        // i < 8 because we are moving a 2-wide column (x) window
        for (int y = 0; y < 8; ++y)
        {
            // Compare each row (y) against every other row to find linked values
            // Top and bottom cells of pairs HAVE to come from the same zone, as separate zones would indicate a separate way to solve the loop
            for (int x = 0; x < 9; ++x)
            {
                Cell up = getCell(x, y);
                Cell down = getCell(x, y+1);

                checkCellPairForLoops(up, down, false);
            }
        }
    }

    // Two pairs of cells are linked they have only 2-3 unique values among them
    private boolean cellsAreLinked(Cell firstLeft, Cell firstRight, Cell secondLeft, Cell secondRight)
    {
        HashSet<Integer> uniqueValues = new HashSet<>();

        uniqueValues.add(firstLeft.value);
        uniqueValues.add(firstRight.value);
        uniqueValues.add(secondLeft.value);
        uniqueValues.add(secondRight.value);

        return uniqueValues.size() < 4;
    }

    // Call with null values for extra cells if you want to see if the loop is closed.
    // Assumes that cells are brought in as aligned pairs (2x1's)
    // NEVER call this with newCellOne being null and newCellTwo having a value or vice versa
    private static boolean cellsHaveLinkedValues(List<Cell> cells, Cell newCellOne, Cell newCellTwo)
    {
        HashSet<Integer> uniqueValues = new HashSet<>();
        int size = cells.size();

        for (Cell c : cells)
            uniqueValues.add(c.value);

        if (newCellOne != null)
        {
            size += 2;
            uniqueValues.add(newCellOne.value);
            uniqueValues.add(newCellTwo.value);
        }

        if (size < 4)
            return false;

        int checkValue = newCellOne == null ? (size / 2) : (size / 2 ) + 1;

        return uniqueValues.size() == checkValue;
    }

    private void checkCellPairForLoops(Cell first, Cell second, boolean horizontal)
    {
        if (!first.isNeighbor(second))
            return;

        if (first.reveal || second.reveal)
            return;

        List<Cell> linkedCells = new ArrayList<>();
        // [row/column index] = [first, second cells]
        HashMap<Integer, List<Cell>> uncheckedCells = new HashMap<>();
        linkedCells.add(first);
        linkedCells.add(second);

        // Different horizontal pairs have the same x value, but different y values
        int check = horizontal ? first.y : first.x;

        // Find the FIRST linked pair. This will determine if there exists a possible loop between the rows/columns
        for (int i = 0; i < 9; ++i)
        {
            if (i == check)
                continue;

            Cell otherFirst = horizontal ? getCell(first.x, i) : getCell(i, first.y);
            Cell otherSecond = horizontal ? getCell(second.x, i) : getCell(i, second.y);

            if (!otherFirst.isNeighbor(otherSecond))
                continue;

            if (cellsAreLinked(first, second, otherFirst, otherSecond))
            {
                linkedCells.add(otherFirst);
                linkedCells.add(otherSecond);
            }
            else
            {
                uncheckedCells.put(i, new ArrayList<Cell>());
                uncheckedCells.get(i).add(otherFirst);
                uncheckedCells.get(i).add(otherSecond);
            }
        }

        // If two pairs are linked in an open loop, keep checking rows until you close the loop or run out of options
        if (linkedCells.size() > 2)
        {
            // Shouldn't be possible to run out of unchecked pairs, but it's here for posterity
            while (uncheckedCells.size() > 0 && !cellsHaveLinkedValues(linkedCells, null, null))
            {
                int startSize = linkedCells.size();
                List<Integer> toRemove = new ArrayList<>();

                for (Integer i : uncheckedCells.keySet())
                {
                    Cell uncheckedFirst = uncheckedCells.get(i).get(0);
                    Cell uncheckedSecond = uncheckedCells.get(i).get(1);

                    // Do NOT remove the pair if it isn't linked, as it may be linked AFTER a different pair has been added to linkedCells
                    if (cellsHaveLinkedValues(linkedCells, uncheckedFirst, uncheckedSecond))
                    {
                        linkedCells.add(uncheckedFirst);
                        linkedCells.add(uncheckedSecond);
                        toRemove.add(i);
                    }
                }
                // If none of the unchecked pairs are linked, break out of the loop
                if (linkedCells.size() == startSize)
                    break;

                // Remove the newly discovered linked pairs from the unchecked pairs
                for (Integer i : toRemove)
                    uncheckedCells.remove(i);
            }
        }

        // Reveal a cell to converge the loop (if necessary)
        if (linkedCells.size() > 2 && cellsHaveLinkedValues(linkedCells, null, null) && !containsReveal(linkedCells))
        {
            Board newBoard = new Board(this);
            // If swapping the values of the cells of each pair still creates a valid solution, reveal the first linked cell's value to the user
            for (int i = 0; i < linkedCells.size(); i += 2)
            {
                Cell oldUp = linkedCells.get(i);
                Cell oldDown = linkedCells.get(i+1);
                Cell newUp = newBoard.getCell(oldUp.x, oldUp.y);
                Cell newDown = newBoard.getCell(oldDown.x, oldDown.y);

                int swap = newUp.value;
                newUp.value = newDown.value;
                newDown.value = swap;
            }

            if (newBoard.checkSolution())
            {
                Cell revealer = linkedCells.get(0);
                Cell boardAlter = getCell(revealer.x, revealer.y);
                boardAlter.reveal = true;
            }
        }
    }

    private static boolean containsReveal(List<Cell> cells)
    {
        for (Cell c : cells)
        {
            if (c.reveal)
                return true;
        }
        return false;
    }

}
