package com.javarush.task.task35.task3513;

import java.util.*;
import java.util.stream.IntStream;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    public int score;
    public int maxTile;
    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;
    private boolean isSaveNeeded = true;

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public Model() {
        resetGameTiles();
        this.previousStates = new Stack<>();
        this.previousScores = new Stack<>();
        this.score = 0;
        this.maxTile = 2;

    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() > 0) {
            emptyTiles.get(((int) (emptyTiles.size() * Math.random()))).value = (Math.random() < 0.9 ? 2 : 4);
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTilesList = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].isEmpty()) {
                    emptyTilesList.add(gameTiles[i][j]);
                }
            }
        }
        return emptyTilesList;
    }

    public void resetGameTiles() {
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                gameTiles[j][i] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean wasChanged = false;
        Tile[] original = Arrays.copyOf(tiles, 4);

        for (int i = 0; i < tiles.length; i++) {
            IntStream.range(0, tiles.length - 1).filter(x -> tiles[x].isEmpty()).forEach(x -> {
                Tile tile = tiles[x + 1];
                tiles[x + 1] = tiles[x];
                tiles[x] = tile;
            });
        }

        for (int i = 0; i < original.length; i++) {
            if (original[i].value != tiles[i].value) {
                wasChanged = true;
            }
        }
        return wasChanged;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean sumCell = false;
        int emptyNumber = 0;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i + 1].value && tiles[i].value != 0) {
                tiles[i].value = tiles[i].value + tiles[i + 1].value;
                tiles[i + 1].value = emptyNumber;
                maxTile = Math.max(tiles[i].value, maxTile);
                score = score + tiles[i].value + tiles[i + 1].value;
                compressTiles(tiles);
                sumCell = true;
            }
        }
        return sumCell;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean wasChanged = false;

        for (Tile[] tiles : gameTiles) {
            if (compressTiles(tiles) | mergeTiles(tiles)) {
                wasChanged = true;
            }
        }

        if (wasChanged) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
    }

    public void up() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
    }

    public void down() {
        saveState(gameTiles);
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
    }

    private void rotateClockwise() {
        Tile[][] rotatedMatrix = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0, a = 0; i < FIELD_WIDTH; i++, a++) {
            for (int j = FIELD_WIDTH - 1, b = 0; j >= 0; j--, b++) {
                rotatedMatrix[a][b] = gameTiles[j][i];
            }
        }
        gameTiles = rotatedMatrix;
    }

    public boolean canMove() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0) {
                    return true;
                }
                if (i < FIELD_WIDTH - 1) {
                    if (gameTiles[i][j].value == gameTiles[i + 1][j].value) {
                        return true;
                    }
                }
                if (j < FIELD_WIDTH - 1) {
                    if (gameTiles[i][j].value == gameTiles[i][j + 1].value) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] tempTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles.length; j++) {
                tempTiles[i][j] = new Tile(tiles[i][j].value);
            }
        }
        int tempScore = score;
        previousStates.push(tempTiles);
        previousScores.push(tempScore);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        Tile[][] lastState = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (lastState[i][j].value != gameTiles[i][j].value) {
                    return true;
                }
            }
        }
        return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        } else
            rollback();
        return new MoveEfficiency(getEmptyTiles().size(), score, move);

    }

    public void autoMove(){
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4,Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.peek().getMove().move();
    }
}
