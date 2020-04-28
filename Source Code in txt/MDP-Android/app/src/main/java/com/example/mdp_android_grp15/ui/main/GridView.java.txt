package com.example.mdp_android_grp15.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.mdp_android_grp15.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class GridView extends View {

    private static final String TAG = "GridView";
    private static final int COL = 15, ROW = 20;
    public static float cellSize;
    public static JSONObject mapJsonObject;
    public static Cell[][] cells;
    private ArrayList<String[]> arrowCoord = new ArrayList<>();
    Bitmap arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_error);

    private String exploredString = "";
    private String obstacleString = "";
    public boolean plotObstacle = false;
    private boolean mapDrawn = false;

    private Paint blackPaint = new Paint();
    private Paint whitePaint = new Paint();
    private Paint obstacleColor = new Paint();
    private Paint waypointColor = new Paint();
    private Paint unexploredColor = new Paint();
    private Paint exploredColor = new Paint();
    private Paint arrowColor = new Paint();
    private Paint fastestPathColor = new Paint();

    public GridView(Context context) {
        super(context);
        init(null);
    }

    public GridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        obstacleColor.setColor(Color.BLACK);
        waypointColor.setColor(Color.YELLOW);
        unexploredColor.setColor(Color.LTGRAY);
        exploredColor.setColor(Color.WHITE);
        arrowColor.setColor(Color.BLACK);
        fastestPathColor.setColor(Color.MAGENTA);
    }

    private void init(@Nullable AttributeSet attrs) {
        setWillNotDraw(false);
    }

    private int convertRow(int row) {
        return (20 - row);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        showLog("Entering onDraw");
        super.onDraw(canvas);
        showLog("Redrawing map");

        ArrayList<String[]> arrowCoord = this.getArrowCoord();

        if (!mapDrawn) {
            canvas.drawColor(Color.WHITE);
            String[] dummyArrowCoord = new String[]{};
            this.getArrowCoord().add(dummyArrowCoord);
            this.createCell();
            mapDrawn = true;
        }

        try {
            this.setMap(mapJsonObject);
            showLog("setMap try success");
        } catch (JSONException e) {
            e.printStackTrace();
            showLog("setMap try fail");
        }

        this.drawHorizontalLines(canvas);
        this.drawVerticalLines(canvas);
        this.drawGridNumber(canvas);

        if (plotObstacle)
            this.plotObstacle();

        this.drawIndividualCell(canvas);

        if (!plotObstacle)
            this.drawNumber(canvas);

        if (plotObstacle) {
            this.drawObstacle(canvas);
            this.drawArrow(canvas, arrowCoord);
        }

        showLog("Exiting onDraw");
    }

    public void createCell() {
        showLog("Entering cellCreate");
        cells = new Cell[COL + 1][ROW + 1];
        calculateDimension();

        for (int x = 0; x <= COL; x++)
            for (int y = 0; y <= ROW; y++)
                cells[x][y] = new Cell(x * cellSize + (cellSize / 30), y * cellSize + (cellSize / 30), (x + 1) * cellSize, (y + 1) * cellSize, unexploredColor, "unexplored");
        showLog("Exiting createCell");
    }

    private void setMap(JSONObject mapJsonObject) throws JSONException {
        showLog("Entering setMap");
        this.mapJsonObject = mapJsonObject;
        JSONArray infoJsonArray;
        JSONObject infoJsonObject;
        String hexStringExplored, hexStringObstacle;
        BigInteger hexBigIntegerExplored, hexBigIntegerObstacle;
        String message = "No message received";

        for (int i = 0; i < mapJsonObject.names().length(); i++) {
            switch (mapJsonObject.names().getString(i)) {
                case "map":
                    infoJsonArray = mapJsonObject.getJSONArray("map");
                    infoJsonObject = infoJsonArray.getJSONObject(0);
                    hexStringExplored = infoJsonObject.getString("explored");
                    hexBigIntegerExplored = new BigInteger(hexStringExplored, 16);
                    exploredString = hexBigIntegerExplored.toString(2);

                    int x, y;
                    for (int j=0; j<exploredString.length()-4; j++) {
                        y = 19 - (j/15);
                        x = 1 + j - ((19-y)*15);
                        if ((String.valueOf(exploredString.charAt(j+2))).equals("1"))
                            cells[x][y].setType("explored");
                        else
                            cells[x][y].setType("unexplored");
                    }

                    int length = infoJsonObject.getInt("length");

                    hexStringObstacle = infoJsonObject.getString("obstacle");
                    showLog("hexStringObstacle: " + hexStringObstacle);
                    hexBigIntegerObstacle = new BigInteger(hexStringObstacle, 16);
                    obstacleString = hexBigIntegerObstacle.toString(2);
                    while (obstacleString.length() < length) {
                        obstacleString = "0" + obstacleString;
                    }
                    message = "Explored map:  " + exploredString + "\n" + "Obstacle map:  " + obstacleString;
                    break;
                case "waypoint":
                    infoJsonArray = mapJsonObject.getJSONArray("waypoint");
                    infoJsonObject = infoJsonArray.getJSONObject(0);
                    cells[infoJsonObject.getInt("x")][20 - infoJsonObject.getInt("y")].setType("waypoint");
                    message = "Waypoint:  " + String.valueOf(infoJsonObject.getInt("x")) + String.valueOf(infoJsonObject.getInt("y"));
                    break;
                case "arrow":
                    infoJsonArray = mapJsonObject.getJSONArray("arrow");
                    for (int j = 0; j < infoJsonArray.length(); j++) {
                        infoJsonObject = infoJsonArray.getJSONObject(j);
                        if (!infoJsonObject.getString("face").equals("dummy")) {
                            this.setArrowCoordinate(infoJsonObject.getInt("x"), infoJsonObject.getInt("y"), infoJsonObject.getString("face"));
                            message = "Arrow:  " + String.valueOf(infoJsonObject.getInt("x")) + String.valueOf(infoJsonObject.getInt("y")) + infoJsonObject.getString("face");
                        }
                    }
                    break;
                default:
                    message = "default for JSONObject: " + mapJsonObject.names().getString(i);
                    break;
            }
            showLog(message);
        }
        showLog("Exiting setMap");
    }

    public void setArrowCoordinate(int col, int row, String arrowDirection) {
        showLog("Entering setArrowCoordinate");
        String[] arrowCoord = new String[3];
        arrowCoord[0] = String.valueOf(col);
        arrowCoord[1] = String.valueOf(row);
        arrowCoord[2] = arrowDirection;
        this.getArrowCoord().add(arrowCoord);

        showLog("Exiting setArrowCoordinate");
    }

    private ArrayList<String[]> getArrowCoord() {
        return this.arrowCoord;
    }

    private void drawIndividualCell(Canvas canvas) {
        showLog("Entering drawIndividualCell");
        for (int x = 1; x <= COL; x++)
            for (int y = 0; y < ROW; y++)
                for (int i = 0; i < this.getArrowCoord().size(); i++)
                    canvas.drawRect(cells[x][y].startX, cells[x][y].startY, cells[x][y].endX, cells[x][y].endY, cells[x][y].paint);

        showLog("Exiting drawIndividualCell");
    }

    private void drawHorizontalLines(Canvas canvas) {
        for (int y = 0; y <= ROW; y++)
            canvas.drawLine(cells[1][y].startX, cells[1][y].startY - (cellSize / 30), cells[15][y].endX, cells[15][y].startY - (cellSize / 30), blackPaint);
    }

    private void drawVerticalLines(Canvas canvas) {
        for (int x = 0; x <= COL; x++)
            canvas.drawLine(cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][0].startY - (cellSize / 30), cells[x][0].startX - (cellSize / 30) + cellSize, cells[x][19].endY + (cellSize / 30), blackPaint);
    }

    private void drawGridNumber(Canvas canvas) {
        showLog("Entering drawGridNumber");
        for (int x = 1; x <= COL; x++) {
            if (x > 9)
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 4.5f), cells[x][20].startY + (cellSize / 2), blackPaint);
            else
                canvas.drawText(Integer.toString(x-1), cells[x][20].startX + (cellSize / 3), cells[x][20].startY + (cellSize / 2), blackPaint);
        }
        for (int y = 0; y < ROW; y++) {
            if ((20 - y) > 9)
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 2), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
            else
                canvas.drawText(Integer.toString(19 - y), cells[0][y].startX + (cellSize / 1.5f), cells[0][y].startY + (cellSize / 1.5f), blackPaint);
        }
        showLog("Exiting drawGridNumber");
    }

    private void drawArrow(Canvas canvas, ArrayList<String[]> arrowCoord) {
        showLog("Entering drawArrow");
        RectF rect;

        if (arrowCoord.size() == 0) {
            return;
        }

        for (int i = 1; i < arrowCoord.size(); i++) {
            int col = Integer.parseInt(arrowCoord.get(i)[0]);
            int row = convertRow(Integer.parseInt(arrowCoord.get(i)[1]));
            rect = new RectF(col * cellSize, row * cellSize, (col + 1) * cellSize, (row + 1) * cellSize);
            switch (arrowCoord.get(i)[2]) {
                case "up":
                    arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_up);
                    break;
                case "right":
                    arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_right);
                    break;
                case "down":
                    arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_down);
                    break;
                case "left":
                    arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_left);
                    break;
                default:
                    break;
            }
            canvas.drawBitmap(arrowBitmap, null, rect, null);
        }
        showLog("Exiting drawArrow");
    }

    private void drawNumber(Canvas canvas) {
        showLog("Entering drawNumber");
        for(int x=1; x<=COL; x++)
            for(int y=0; y<ROW; y++)
                switch (cells[x][y].type) {
                    case "unexplored":
                        showLog("unexplored x: " + x + ", y: " + y);
                        canvas.drawText(Integer.toString(0), cells[x][y].startX + (cellSize / 1.3f), cells[x][y].startY + (cellSize / 1.2f), blackPaint);
                        break;
                    case "explored":
                        showLog("explored x: " + x + ", y: " + y);
                        canvas.drawText(Integer.toString(1), cells[x][y].startX + (cellSize / 1.3f), cells[x][y].startY + (cellSize / 1.2f), blackPaint);
                        break;
                    default:
                        showLog("Unexpected default for draw number: " + cells[x][y].type);
                        break;
                }
        showLog("Exiting drawNumber");
    }

    public class Cell {
        float startX, startY, endX, endY;
        String type;
        Paint paint;

        public Cell(float startX, float startY, float endX, float endY, Paint paint, String type) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.paint = paint;
            this.type = type;
        }

        public void setType(String type) {
            this.type = type;
            switch (type) {
                case "obstacle":
                    this.paint = obstacleColor;
                    break;
                case "waypoint":
                    this.paint = waypointColor;
                    break;
                case "unexplored":
                    this.paint = unexploredColor;
                    break;
                case "explored":
                    this.paint = exploredColor;
                    break;
                case "arrow":
                    this.paint = arrowColor;
                    break;
                case "fastestPath":
                    this.paint = fastestPathColor;
                    break;
                default:
                    showLog("setTtype default: " + type);
                    break;
            }
        }
    }

    private void drawObstacle(Canvas canvas) {
        showLog("Entering drawObstacle");
        for(int x=1; x<=COL; x++)
            for(int y=0; y<ROW; y++)
                switch (cells[x][y].type) {
                    case "explored":
                        canvas.drawText(Integer.toString(0), cells[x][y].startX + (cellSize / 1.3f), cells[x][y].startY + (cellSize / 1.2f), blackPaint);
                        break;
                    case "obstacle":
                    case "arrow":
                        canvas.drawText(Integer.toString(1), cells[x][y].startX + (cellSize / 1.3f), cells[x][y].startY + (cellSize / 1.2f), whitePaint);
                        break;
                    default:
                        showLog("Unexpected default for draw obstacle: " + cells[x][y].type);
                        break;
                }
        showLog("Exiting drawObstacle");
    }

    public void plotObstacle() {
        showLog("Entering plotObstacle");
        int k = 0;
        for (int row = ROW-1; row >= 0; row--)
            for (int col = 1; col <= COL; col++) {
                if (cells[col][row].type.equals("explored")) {
                    if ((String.valueOf(obstacleString.charAt(k))).equals("1"))
                        cells[col][row].setType("obstacle");
                    k++;
                }
            }
        showLog("Exiting plotObstacle");
    }

    private void calculateDimension() {
        this.setCellSize(getWidth() / (COL + 1));
    }

    private void setCellSize(float cellSize) {
        this.cellSize = cellSize;
    }

    private float getCellSize() {
        return this.cellSize;
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }
}