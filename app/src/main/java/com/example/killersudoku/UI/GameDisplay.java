package com.example.killersudoku.UI;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.killersudoku.Models.Board;
import com.example.killersudoku.Models.BoardTracker;
import com.example.killersudoku.Models.Cell;
import com.example.killersudoku.Models.SolvedCombination;
import com.example.killersudoku.Models.Zone;
import com.example.killersudoku.R;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameDisplay
{
    private final int CAGE_SUM_FONT_SIZE = 12;

    private final int CELL_VALUE_FONT_COLOR = Color.argb(255, 44, 142, 199);
    private final int CELL_NOTE_FONT_COLOR = Color.argb(255, 200, 200, 200);
    private final int CELL_HIGHLIGHTED_COLOR = Color.argb(255, 9, 61, 125);
    private final int SAME_VALUE_CELL_HIGHLIGHT_COLOR = Color.argb(255, 55, 55, 55);
    private final int CELL_INVALID_HIGHLIGHT_COLOR = Color.argb(255, 191, 4, 29);
    private final int CELL_VALUE_FONT_SIZE = 24;
    private final int CELL_NOTE_FONT_SIZE = 14;
    private final int DASHED_LINE_WIDTH = 5;
    private final int DASHED_LINE_MARGIN = 10;

    private static String sudokuSolutionsFile = "validSudokuGames.txt";

    private static final List<SolvedCombination> knownCombinations = new ArrayList<>(Arrays.asList(
        new SolvedCombination(3, 2, "12"),
        new SolvedCombination(4, 2, "13"),
        new SolvedCombination(16, 2, "79"),
        new SolvedCombination(17, 2, "89"),

        new SolvedCombination(6, 3, "123"),
        new SolvedCombination(7, 3, "124"),
        new SolvedCombination(23, 3, "689"),
        new SolvedCombination(24, 3, "789"),

        new SolvedCombination(10, 4, "1234"),
        new SolvedCombination(11, 4, "1235"),
        new SolvedCombination(29, 4, "5789"),
        new SolvedCombination(30, 4, "6789"),

        new SolvedCombination(15, 5, "12345"),
        new SolvedCombination(16, 5, "12346"),
        new SolvedCombination(34, 5, "46789"),
        new SolvedCombination(35, 5, "56789")
    ));

    private List<List<ConstraintLayout>> cellLayouts;
    private AppCompatActivity act;
    private String boardString;
    private Board board;
    private Board userBoard;
    // selectedCell is set to the SOLUTION BOARD cell
    private Cell selectedCell;
    private Drawable cellBorder;
    private boolean noteMode;
    private Random rand;
    private BoardTracker boardTracker;

    public GameDisplay(AppCompatActivity act)
    {
        this.act = act;
        this.rand = new Random();
        this.boardString = getNewGameString();
        this.board = new Board(boardString);
        this.userBoard = new Board("");

        // this.board.simplePrint();

        this.cellBorder = act.getDrawable(R.drawable.border);
        this.noteMode = false;

        board.setBoardZones();
        userBoard.cloneZones(board);
        initializeBoardGrid();

        this.boardTracker = new BoardTracker(userBoard);
    }

    public void initializeBoardGrid()
    {
        cellLayouts = new ArrayList<>();
        for (int i = 0; i < 9; ++i)
        {
            cellLayouts.add(new ArrayList<>());
            for (int j = 0; j < 9; ++j)
            {
                GridLayout gridLayout = getGridLayout(act, j, i);
                ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(act).inflate(R.layout.board_cell, null);

                // Set cell position within 3x3 block
                GridLayout.Spec rowSpec = GridLayout.spec(i, 1, 1);
                GridLayout.Spec columnSpec = GridLayout.spec(j, 1, 1);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams(rowSpec, columnSpec);
                // Keeps the column width equal
                param.width = 0;
                param.height = 0;

                // Setup constraint layout
                cl.setLayoutParams(param);
                cl.setTag(String.format("%s%s", j, i));
                cl.setOnClickListener(this::onCellClick);
                cellLayouts.get(i).add(cl);
                // cl.setOnHoverListener(view -> onCellClick(view));

                // Set cell value
                TextView t = cl.findViewById(R.id.cellValue);
                t.setTag(false);

                gridLayout.addView(cl);
            }
        }

        setAllCages();
        loadNotesForSolvedCombinations();
        markSingleCellZones();
        markRevealCells();
    }

    public void resetGame()
    {
        if (selectedCell != null)
        {
            ConstraintLayout cl =  cellLayouts.get(selectedCell.y).get(selectedCell.x);
            cl.setBackground(cellBorder);
        }
        selectedCell = null;
        noteMode = false;
        userBoard = new Board("");

        boardString = getNewGameString();
        board = new Board(boardString);

        // board.simplePrint();

        board.setBoardZones();
        userBoard.cloneZones(board);
        resetCellLayouts();
        setAllCages();
        loadNotesForSolvedCombinations();
        markSingleCellZones();
        markRevealCells();
        boardTracker = new BoardTracker(userBoard);
    }

    public void onNumClick(String num)
    {
        if (selectedCell == null)
            return;

        boardTracker.saveBoardState(userBoard);

        Cell userCell = userBoard.getCell(selectedCell);
        List<CellState> changedCells = new ArrayList<CellState>();

        if (noteMode)
        {
            if (userCell.valid && userCell.value > 0)
            {
                boardTracker.getLastBoardState();
                return;
            }
            
            setCellNote(selectedCell, num);
            userCell.value = 0;
        }
        else
        {
            int newValue = Integer.parseInt(num);

            if (userCell.valid && userCell.value > 0)
            {
                boardTracker.getLastBoardState();
                return;
            }

            changedCells.add(getStateOfCell(userCell));
            setCellValue(selectedCell, num);

            if (userCell.value != 0)
                highlightSameValueCells(userCell, false);

            userCell.value = newValue;
            highlightSameValueCells(userCell, true);

            boolean cellValidity = userBoard.isCellValid(userCell);
            boolean zoneValidity = zoneTotalIsValid(userCell);

            if (cellValidity && zoneValidity)
            {
                //  Clear the number from the notes of all relevant cells
                changedCells.addAll(clearNumberFromNotes(userCell));

                // Reset cell color to cyan if it was highlighted red before
                cellLayouts.get(userCell.y).get(userCell.x).setBackgroundColor(CELL_HIGHLIGHTED_COLOR);
                userCell.valid = true;

                // Highlight all cells green
                if (userBoard.isFull() && userBoard.checkSolution())
                {
                    for (int i = 0; i < 9; ++i)
                    {
                        for (int j = 0; j < 9; ++j)
                        {
                            cellLayouts.get(j).get(i).setBackgroundColor(Color.argb(255, 32, 153, 21));
                        }
                    }
                }
            }
            // Highlight cell in red
            else
            {
                ConstraintLayout cl = cellLayouts.get(selectedCell.y).get(selectedCell.x);
                cl.setBackgroundColor(CELL_INVALID_HIGHLIGHT_COLOR);
                userCell.valid = false;
            }

            boardTracker.saveCellChanges(changedCells);
        }
    }

    public boolean toggleNoteMode()
    {
        noteMode = !noteMode;
        return noteMode;
    }

    public void onEraseClick()
    {
        if (selectedCell == null)
            return;

        boardTracker.saveBoardState(userBoard);
        List<CellState> changedCells = new ArrayList<CellState>();

        setCellValue(selectedCell, "");
        Cell c = userBoard.getCell(selectedCell);

        changedCells.add(getStateOfCell(c));
        boardTracker.saveCellChanges(changedCells);

        if (c.value > 0)
            highlightSameValueCells(c, false);
        c.value = 0;
        if (!c.valid)
        {
            cellLayouts.get(c.y).get(c.x).setBackgroundColor(CELL_HIGHLIGHTED_COLOR);
            c.valid = true;
        }
    }

    public void onHintClick()
    {
        if (selectedCell == null)
            return;

        boardTracker.saveBoardState(userBoard);
        List<CellState> changedCells = new ArrayList<CellState>();

        Cell userCell = userBoard.getCell(selectedCell);
        changedCells.add(getStateOfCell(userCell));

        setCellValue(selectedCell, selectedCell.value);

        userCell.value = selectedCell.value;
        userCell.valid = true;
        cellLayouts.get(userCell.y).get(userCell.x).setBackgroundColor(CELL_HIGHLIGHTED_COLOR);

        changedCells.addAll(clearNumberFromNotes(selectedCell));
        boardTracker.saveCellChanges(changedCells);
    }

    public List<Integer> getNumCounts()
    {
        List<Integer> numCounts = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0));

        for(int y = 0; y < 9; ++y)
        {
            for (int x = 0; x < 9; ++x)
            {
                int val = userBoard.getCell(x, y).value;
                if (val > 0)
                    numCounts.set(val-1, numCounts.get(val-1)+1);
            }
        }

        return numCounts;
    }

    public void onUndoClick()
    {
        revertBoard();
        revertCells();
    }

    private void onCellClick(View v)
    {
        String coord = (String)v.getTag();

        int x = Character.getNumericValue(coord.charAt(0));
        int y = Character.getNumericValue(coord.charAt(1));

        handleCellClick(x, y);
    }

    private void handleCellClick(int x, int y)
    {
        // Reset background of previous selected cell (unless that cell is invalid
        if (selectedCell != null)
        {
            highlightSameValueCells(userBoard.getCell(selectedCell), false);
            if (userBoard.getCell(selectedCell).valid)
            {
                ConstraintLayout previous = cellLayouts.get(selectedCell.y).get(selectedCell.x);
                previous.setBackground(cellBorder);
            }
        }

        Cell newCell = userBoard.getCell(x, y);
        if (newCell.valid)
        {
            highlightSameValueCells(newCell, true);
            cellLayouts.get(y).get(x).setBackgroundColor(CELL_HIGHLIGHTED_COLOR);
        }
        selectedCell = board.getCell(x, y);
    }

    private static GridLayout getGridLayout(AppCompatActivity act, int x, int y)
    {
        int id = 0;

        if (x < 3)
        {
            if (y < 3)
                id = R.id.topLeftGrid;
            else if (y < 6)
                id = R.id.middleLeftGrid;
            else
                id = R.id.bottomLeftGrid;
        }
        else if (x < 6)
        {
            if (y < 3)
                id = R.id.topMiddleGrid;
            else if (y < 6)
                id = R.id.middleMiddleGrid;
            else
                id = R.id.bottomMiddleGrid;
        }
        else
        {
            if (y < 3)
                id = R.id.topRightGrid;
            else if (y < 6)
                id = R.id.middleRightGrid;
            else
                id = R.id.bottomRightGrid;
        }

        return act.findViewById(id);
    }

    private void hideCageWall(ConstraintSet cs, WallSide side)
    {
        int dashedLineId;

        switch(side)
        {
            case BOTTOM:
                dashedLineId = R.id.bottomLine;
                break;
            case TOP:
                dashedLineId = R.id.topLine;
                break;
            case RIGHT:
                dashedLineId = R.id.rightLine;
                break;
            case LEFT:
                dashedLineId = R.id.leftLine;
                break;
            default:
                return;
        }

        cs.setVisibility(dashedLineId, ConstraintSet.INVISIBLE);
    }

    // Resets a cell's cage wall views to all be visible with set margins
    private void resetCellCageWalls(ConstraintLayout cl)
    {
        View dashedLine;

        ConstraintSet cs = new ConstraintSet();
        cs.clone(cl);

        cs.setMargin(R.id.bottomLine, ConstraintSet.BOTTOM, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.bottomLine, ConstraintSet.START, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.bottomLine, ConstraintSet.END, DASHED_LINE_MARGIN);

        cs.setMargin(R.id.topLine, ConstraintSet.TOP, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.topLine, ConstraintSet.START, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.topLine, ConstraintSet.END, DASHED_LINE_MARGIN);

        cs.setMargin(R.id.rightLine, ConstraintSet.BOTTOM, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.rightLine, ConstraintSet.TOP, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.rightLine, ConstraintSet.END, DASHED_LINE_MARGIN);

        cs.setMargin(R.id.leftLine, ConstraintSet.BOTTOM, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.leftLine, ConstraintSet.START, DASHED_LINE_MARGIN);
        cs.setMargin(R.id.leftLine, ConstraintSet.TOP, DASHED_LINE_MARGIN);

        cs.applyTo(cl);



        dashedLine = cl.findViewById(R.id.bottomLine);
        dashedLine.setVisibility(View.VISIBLE);
        dashedLine = cl.findViewById(R.id.topLine);
        dashedLine.setVisibility(View.VISIBLE);
        dashedLine = cl.findViewById(R.id.rightLine);
        dashedLine.setVisibility(View.VISIBLE);
        dashedLine = cl.findViewById(R.id.leftLine);
        dashedLine.setVisibility(View.VISIBLE);
    }

    private void setCageSum(ConstraintLayout cl, int value)
    {
        TextView sum = cl.findViewById(R.id.cageSum);
        if (value > 0)
        {
            sum.setTextSize(CAGE_SUM_FONT_SIZE);
            sum.setText(Integer.toString(value));
        }
        else
        {
            sum.setTextSize(0);
            sum.setText("");
        }
    }

    // Draws cage sums and hides cage walls depending on the board zones
    private void setAllCages()
    {
        for (Zone z : board.zones)
        {
            Cell topLeftMost = z.getTopLeftMostCell();
            ConstraintLayout cl = cellLayouts.get(topLeftMost.y).get(topLeftMost.x);
            setCageSum(cl, z.getTotal());

            for (Cell c : z.cells)
            {
                ConstraintLayout conLay = cellLayouts.get(c.y).get(c.x);
                resetCellCageWalls(conLay);
                setCageWalls(z, c);
            }
        }
    }

    // Resets all cell layouts to the default state (no text, white background, etc)
    private void resetCellLayouts()
    {
        for (int i = 0; i < 9; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                ConstraintLayout cl = cellLayouts.get(j).get(i);
                TextView t = cl.findViewById(R.id.cellValue);
                t.setText("");
                t.setTag(false);
                cl.setBackground(cellBorder);
                setCageSum(cl, 0);
                resetCellCageWalls(cl);
            }
        }
    }

    // Hides cage walls for a cell based on its zone
    private void setCageWalls(Zone z, Cell c)
    {
        ConstraintLayout cl = cellLayouts.get(c.y).get(c.x);
        ConstraintSet cs = new ConstraintSet();
        cs.clone(cl);

        Cell neighbor = board.getCellLeft(c);
        if (z.contains(neighbor))
        {
            hideCageWall(cs, WallSide.LEFT);

            cs.setMargin(R.id.topLine, ConstraintSet.START, 0);
            cs.setMargin(R.id.bottomLine, ConstraintSet.START, 0);
        }

        neighbor = board.getCellAbove(c);
        if (z.contains(neighbor))
        {
            hideCageWall(cs, WallSide.TOP);

            cs.setMargin(R.id.leftLine, ConstraintSet.TOP, 0);
            cs.setMargin(R.id.rightLine, ConstraintSet.TOP, 0);
        }

        neighbor = board.getCellRight(c);
        if (z.contains(neighbor))
        {
            hideCageWall(cs, WallSide.RIGHT);

            cs.setMargin(R.id.topLine, ConstraintSet.END, 0);
            cs.setMargin(R.id.bottomLine, ConstraintSet.END, 0);
        }

        neighbor = board.getCellBelow(c);
        if (z.contains(neighbor))
        {
            hideCageWall(cs, WallSide.BOTTOM);

            cs.setMargin(R.id.leftLine, ConstraintSet.BOTTOM, 0);
            cs.setMargin(R.id.rightLine, ConstraintSet.BOTTOM, 0);
        }

        cs.applyTo(cl);
    }

    // Sets the text value of the cell, NOT the model's value field
    private void setCellValue(Cell c, String value)
    {
        String newValue = value.equals("0") ? "" : value;

        ConstraintLayout cl = cellLayouts.get(c.y).get(c.x);
        TextView t = cl.findViewById(R.id.cellValue);
        t.setText(newValue);
        t.setTextSize(CELL_VALUE_FONT_SIZE);
        t.setTextColor(CELL_VALUE_FONT_COLOR);
        t.setTag(false);
    }

    private void setCellValue(Cell c, int value)
    {
        setCellValue(c, String.valueOf(value));
    }

    // Passing a value with multiple numbers will just set the note text without doing any removal/sorting logic
    private void setCellNote(Cell c, String value)
    {
        ConstraintLayout cl = cellLayouts.get(c.y).get(c.x);
        TextView t = cl.findViewById(R.id.cellValue);

        String newNote;

        if ((boolean)t.getTag() && value.length() == 1)
        {
            String oldNote = (String)t.getText();
            newNote = getNoteString(oldNote, value);
        }
        else
            newNote = value;

        t.setText(newNote);
        t.setTextSize(CELL_NOTE_FONT_SIZE);
        t.setTextColor(CELL_NOTE_FONT_COLOR);
        t.setTag(true);
    }

    // Generates the new note string for a cell for a given number
    // e.g. getNoteString("1234", "3") returns "124"
    private static String getNoteString(String oldNote, String number)
    {
        int oldNoteSize = oldNote.length();

        if (oldNoteSize == 0)
            return number;
        else if (oldNote.contains(number))
            return oldNote.replace(number, "");
        else
        {
            char num = number.charAt(0);
            int i;
            for (i = 0; i < oldNoteSize; ++i)
            {
                if (oldNote.charAt(i) > num)
                    break;
            }
            if (i < oldNoteSize)
            {
                StringBuilder sb = new StringBuilder(oldNote);
                sb.insert(i, num);
                return sb.toString();
            }
            return oldNote + number;
        }
    }

    // Removes c.value from the notes of all other relevant cells
    private List<CellState> clearNumberFromNotes(Cell c)
    {
        List<CellState> cellStates = new ArrayList<CellState>();
        CellState cs;

        if (c.value == 0)
            return cellStates;

        for (int i = 0; i < 9; ++i)
        {
            if (i != c.x && userBoard.getCell(i, c.y).value == 0)
            {
                cs = removeNumFromCellNotes(i, c.y, c.value);
                if (cs != null)
                    cellStates.add(cs);
            }

            if (i != c.y && userBoard.getCell(c.x, i).value == 0)
            {
                cs = removeNumFromCellNotes(c.x, i, c.value);
                if (cs != null)
                    cellStates.add(cs);
            }
        }

        int lowerX = (c.x / 3) * 3;
        int lowerY = (c.y / 3) * 3;

        for (int y = lowerY; y < lowerY + 3; ++y)
        {
            for (int x = lowerX; x < lowerX + 3; ++x)
            {
                if ((x != c.x || y != c.y) && userBoard.getCell(x, y).value == 0)
                {
                    cs = removeNumFromCellNotes(x, y, c.value);
                    if (cs != null)
                        cellStates.add(cs);
                }
            }
        }

        Zone containingZone = userBoard.getZoneOfCell(c);
        for (Cell cell : containingZone.cells)
        {
            if (((cell.x != c.x) || (cell.y != c.y)) && cell.value == 0)
                {
                    cs = removeNumFromCellNotes(cell.x, cell.y, c.value);
                    if (cs != null)
                        cellStates.add(cs);
                }
        }

        return cellStates;
    }

    // Removes a single number from a single cell's notes
    private CellState removeNumFromCellNotes(int x, int y, int value)
    {
        CellState cs = getStateOfCell(x, y);
        ConstraintLayout cl = cellLayouts.get(y).get(x);
        TextView t = cl.findViewById(R.id.cellValue);

        // Cell is not in note mode
        if (!(boolean)t.getTag())
            return null;

        String oldNote = (String)t.getText();
        String newNote = oldNote.replace(String.valueOf(value), "");
        t.setText(newNote);

        if (newNote != oldNote)
            return cs;
        return null;
    }

    // Fills in notes for zones with one single combination of values
    private void loadNotesForSolvedCombinations()
    {
        for (Zone z : board.zones)
        {
            int zoneTotal = z.getTotal();
            int zoneSize = z.cells.size();
            SolvedCombination sc = knownCombinations.stream()
                    .filter(x -> x.zoneTotal == zoneTotal && x.zoneSize == zoneSize)
                    .findFirst()
                    .orElse(null);
            if (sc != null)
            {
                for (Cell c : z.cells)
                    setCellNote(c, sc.noteString);
            }
        }
    }

    // Sets the value of single cell zones, as these are free spaces
    private void markSingleCellZones()
    {
        for (Zone z :board.zones)
        {
            if (z.cells.size() == 1)
            {
                Cell solutionCell = z.cells.get(0);
                Cell userCell = userBoard.getCell(solutionCell);

                setCellValue(userCell, String.valueOf(solutionCell.value));
                userCell.value = solutionCell.value;
                userCell.valid = true;
                clearNumberFromNotes(userCell);
            }
        }
    }

    // Sets the value of any cells with the reveal flag set
    private void markRevealCells()
    {
        for (int y = 0; y < 9; ++y)
        {
            for (int x = 0; x < 9; ++x)
            {
                Cell c = board.getCell(x, y);
                if (c.reveal)
                {
                    Cell userCell = userBoard.getCell(x, y);
                    userCell.value = c.value;
                    userCell.valid = true;
                    setCellValue(c, c.value);
                    clearNumberFromNotes(userCell);
                }
            }
        }
    }

    private String getNewGameString()
    {
        try
        {
            AssetManager assets = act.getAssets();
            InputStream is = assets.open(sudokuSolutionsFile);

            int lineNumber = rand.nextInt(600000);
            long offset = lineNumber * 83;

            is.skip(offset);
            byte[] b = new byte[81];
            is.read(b, 0, 81);
            String output = new String(b, StandardCharsets.UTF_8);
            is.close();

            return output;
        }
        catch (Exception e)
        {
            System.out.printf("Exception: %s%n", e);
            return "";
        }
    }

    // Will not de-highlight the userCell itself
    private void highlightSameValueCells(Cell userCell, boolean highlight)
    {
        if (userCell.value == 0)
            return;

        for (int y = 0; y < 9; ++y)
        {
            for (int x = 0; x < 9; ++x)
            {
                Cell cell = userBoard.getCell(x, y);
                if (cell.valid && cell.value == userCell.value)
                {
                    ConstraintLayout cl = cellLayouts.get(y).get(x);

                    if (highlight)
                        cl.setBackgroundColor(SAME_VALUE_CELL_HIGHLIGHT_COLOR);
                    else if (x != userCell.x || y != userCell.y)
                        cl.setBackground(cellBorder);
                }
            }
        }
    }

    // Used to check if the user set values for all cells of a zone but the total doesn't add up to what the cage sum says it should.
    // Therefore, this returns true if not all cells have been assigned values for the user zone.
    private boolean zoneTotalIsValid(Cell userCell)
    {
        Zone userZone = userBoard.getZoneOfCell(userCell);
        Zone solutionZone = board.getZoneOfCell(userCell);

        int userTotal = 0;
        for (Cell c : userZone.cells)
        {
            if (c.value == 0)
                return true;
            userTotal += c.value;
        }
        return userTotal == solutionZone.getTotal();
    }

    private enum WallSide
    {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT
    }

    // Gets state of user cell
    private CellState getStateOfCell(Cell c)
    {
        CellState cs = new CellState(c);

        ConstraintLayout cl = cellLayouts.get(c.y).get(c.x);
        TextView t = cl.findViewById(R.id.cellValue);

        boolean noteMode = (boolean)t.getTag();

        if (noteMode)
        {
            cs.mode = CellState.Mode.NOTE;
            cs.noteValue = t.getText().toString();
        }
        else
            cs.mode = CellState.Mode.VALUE;

        return cs;
    }

    private CellState getStateOfCell(int x, int y)
    {
        Cell userCell = userBoard.getCell(x, y);
        return getStateOfCell(userCell);
    }

    // Sets userBoard to its previous state since last action
    private void revertBoard()
    {
        Cell currentCell;
        Cell previousCell;
        Board previousBoard = boardTracker.getLastBoardState();

        if (previousBoard == null)
            return;

        userBoard = new Board(previousBoard);
        userBoard.cloneZones(previousBoard);
    }

    // Sets the cell UIs to their previous state since last action
    private void revertCells()
    {
        List<CellState> cellStates = boardTracker.getLastCellChanges();

        for (CellState cs : cellStates)
        {
            // Only used as holder for x,y. Current value/board state doesn't matter
            Cell userCell = userBoard.getCell(cs.x, cs.y);
            ConstraintLayout cl = cellLayouts.get(cs.y).get(cs.x);

            if (cs.mode == CellState.Mode.NOTE)
                setCellNote(userCell, cs.noteValue);
            else
                setCellValue(userCell, cs.value);

            // Set highlighting
            if (!cs.valid)
                cl.setBackgroundColor(CELL_INVALID_HIGHLIGHT_COLOR);
            else if (selectedCell.x == cs.x && selectedCell.y == cs.y)
                cl.setBackgroundColor(CELL_HIGHLIGHTED_COLOR);
            else
                cl.setBackground(cellBorder);
        }

        if (cellStates.size() > 0)
        {
            CellState cs = cellStates.get(0);
            handleCellClick(cs.x, cs.y);
        }
    }
}
