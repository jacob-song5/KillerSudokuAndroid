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

    // Are two cells from the same zone?
    public boolean sameZone(Cell i, Cell j)
    {
        for (Zone z : this.zones)
        {
            if (z.contains(i))
                return z.contains(j);
        }
        return false;
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

    // Fixes the first occurrence of linked zones found.
    // Currently only works for 2x1 aligned zones.
    // BUG: if three zones are aligned but only two are linked, this will not fix any zones
    private boolean fixLinkedZones()
    {
        for (int i = 0; i < zones.size(); ++i)
        {
            Zone z = zones.get(i);
            List<Zone> alignedZones = new ArrayList<>();
            alignedZones.add(z);

            for (Zone j : zones)
            {
                if (z.equals(j))
                    continue;

                if (twoZonesAreAligned(z, j))
                    alignedZones.add(j);
            }

            if (alignedZones.size() > 1 && zonesHaveLinkedValues(alignedZones))
            {
                zones.remove(z);
                System.out.printf("Old zone: %s%n", z);
                for (Cell c : z.cells)
                {
                    Zone newZone = new Zone();
                    newZone.add(c);
                    zones.add(newZone);
                    zoneAttrition(newZone);
                    System.out.printf("New zone: %s%n", newZone);
                }
                return true;
            }
        }
        return false;
    }

    // Currently only works on 2x1 zones
    private static boolean twoZonesAreAligned(Zone one, Zone two)
    {
        if (one.cells.size() != 2 || two.cells.size() != 2)
            return false;

        // one is vertical
        if (one.cells.get(0).x == one.cells.get(1).x)
        {
            // two is on the same rows as one
            HashSet<Integer> oneCells = new HashSet<>();
            HashSet<Integer> twoCells = new HashSet<>();
            for (Cell c : one.cells)
                oneCells.add(c.y);
            for (Cell c : two.cells)
                twoCells.add(c.y);

            return oneCells.equals(twoCells);
        }

        // one is horizontal
        else
        {
            // two is on the same columns as one
            HashSet<Integer> oneCells = new HashSet<>();
            HashSet<Integer> twoCells = new HashSet<>();
            for (Cell c : one.cells)
                oneCells.add(c.x);
            for (Cell c : two.cells)
                twoCells.add(c.x);

            return oneCells.equals(twoCells);
        }
    }

    // If there are more unique values in the zones than zones themselves, there is no loop and there should only be one solution for those zones.
    private static boolean zonesHaveLinkedValues(List<Zone> zones)
    {
        HashSet<Integer> uniqueValues = new HashSet<>();

        for (Zone z : zones)
        {
            for (Cell c : z.cells)
            {
                uniqueValues.add(c.value);
            }
        }

        return uniqueValues.size() == zones.size();
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

                if (!left.isNeighbor(right))
                    continue;

                List<Cell> linkedCells = new ArrayList<>();
                linkedCells.add(left);
                linkedCells.add(right);

                for (int i = 0; i < 9; ++i)
                {
                    if (i == y)
                        continue;

                    Cell otherLeft = getCell(x, i);
                    Cell otherRight = getCell(x+1, i);

                    if (!otherLeft.isNeighbor(otherRight))
                        continue;

                    // If no new linked pair has been found yet, check to see if this pair is linked with the original pair
                    // Otherwise, check if the new pair is linked with ALL of the found linked pairs
                    boolean check = linkedCells.size() == 2
                            ? cellsAreLinked(left, right, otherLeft, otherRight)
                            : cellsHaveLinkedValues(linkedCells, otherLeft, otherRight);

                    if (check)
                    {
                        linkedCells.add(otherLeft);
                        linkedCells.add(otherRight);
                    }
                }

                if (linkedCells.size() > 2 && cellsHaveLinkedValues(linkedCells, null, null) && !containsReveal(linkedCells))
                {
                    /*
                    System.out.println("Linked cells found.");
                    for (Cell c : linkedCells)
                    {
                        c.fullPrint();
                    }
                    System.out.println("end");
                    */

                    Board newBoard = new Board(this);
                    // Iterate over the pairs
                    for (int i = 0; i < linkedCells.size(); i += 2)
                    {
                        Cell oldLeft = linkedCells.get(i);
                        Cell oldRight = linkedCells.get(i+1);
                        Cell newLeft = newBoard.getCell(oldLeft.x, oldLeft.y);
                        Cell newRight = newBoard.getCell(oldRight.x, oldRight.y);

                        int swap = newLeft.value;
                        newLeft.value = newRight.value;
                        newRight.value = swap;
                    }

                    if (newBoard.checkSolution())
                    {
                        // System.out.println("Linked pair has multiple solutions.");
                        Cell revealer = linkedCells.get(0);
                        Cell boardAlter = getCell(revealer.x, revealer.y);
                        boardAlter.reveal = true;
                    }
                }
            }
        }

        // VERTICAL
        // i < 8 because we are moving a 2-wide column (x) window
        for (int y = 0; y < 8; ++y)
        {
            // Compare each row (y) against every other row to find linked values
            // Left and right cells of pairs HAVE to come from the same zone, as separated zones would indicate a separate way to solve the loop
            for (int x = 0; x < 9; ++x)
            {
                Cell up = getCell(x, y);
                Cell down = getCell(x, y+1);

                if (!up.isNeighbor(down))
                    continue;

                List<Cell> linkedCells = new ArrayList<>();
                linkedCells.add(up);
                linkedCells.add(down);

                for (int i = 0; i < 9; ++i)
                {
                    if (i == x)
                        continue;

                    Cell otherUp = getCell(i, y);
                    Cell otherDown = getCell(i, y+1);

                    if (!otherUp.isNeighbor(otherDown))
                        continue;

                    // If no new linked pair has been found yet, check to see if this pair is linked with the original pair
                    // Otherwise, check if the new pair is linked with ALL of the found linked pairs
                    boolean check = linkedCells.size() == 2
                            ? cellsAreLinked(up, down, otherUp, otherDown)
                            : cellsHaveLinkedValues(linkedCells, otherUp, otherDown);

                    if (check)
                    {
                        linkedCells.add(otherUp);
                        linkedCells.add(otherDown);
                    }
                }

                if (linkedCells.size() > 2 && cellsHaveLinkedValues(linkedCells, null, null) && !containsReveal(linkedCells))
                {
                    /*
                    System.out.println("Linked cells found.");
                    for (Cell c : linkedCells)
                    {
                        c.fullPrint();
                    }
                    System.out.println("end");
                    */

                    Board newBoard = new Board(this);
                    // Iterate over the pairs
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
                        // System.out.println("Linked pair has multiple solutions.");
                        Cell revealer = linkedCells.get(0);
                        Cell boardAlter = getCell(revealer.x, revealer.y);
                        boardAlter.reveal = true;
                    }
                }
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

    // Assumes that cells are brought in aligned pairs (2x1's)
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

        return uniqueValues.size() == (size / 2);
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
