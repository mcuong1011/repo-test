package caro;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;


public class Computer {

    private int height;
    private int width;
    public int optimalX;
    public int optimalY;
    
    public Computer(int height, int width) {
        this.height = height;
        this.width = width;

       
        myEvalBoard = new EvalBoard(height, width);
    }


    private EvalBoard myEvalBoard;
    
        private String generateAsciiBoard(int[][] status) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        for (int x = 0; x < width; x++) {
            sb.append(x).append(" ");
        }
        sb.append("\n");
        for (int y = 0; y < height; y++) {
            sb.append(y).append(" ");
            for (int x = 0; x < width; x++) {
                char c = '.';
                if (status[y][x] == 1) c = 'X';
                else if (status[y][x] == 2) c = 'O';
                sb.append(c).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void FindMoveUsingGemini(int[][] status) {
        String asciiBoard = generateAsciiBoard(status);

        String message = "As an AI playing Gomoku, analyze the following board and provide the optimal next move for the computer (O). You are assigned to ePC, and the board is represented as a grid with coordinates, utilize the algorithm i provide to make the best move agaisnt the player as much as possible.\n\n"
                + asciiBoard + "\n\nPlease provide your move in the format: \"My move is at x=column, y=row\"";

        String apiKey = "AIzaSyAqw5WCxocPbS87kxGeFK_iSZpV3M-p0ow"; // Replace with your actual API key

        // Call the Gemini API (Note: This is a placeholder and may need adjustments based on the actual API)
        try {
            // Set up the connection and request
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateText?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Construct JSON payload
            JSONObject prompt = new JSONObject();
            prompt.put("prompt", message);

            JSONObject payload = new JSONObject();
            payload.put("prompt", prompt);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response
            InputStream is = conn.getInputStream();
            Scanner scanner = new Scanner(is, "UTF-8");
            String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();

            // Parse response to extract move
            parseResponse(response, status);

        } catch (IOException e) {
            System.err.println("API call error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private boolean isCellEmpty(int[][] board, int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        return board[y][x] == 0;
    }
    private void parseResponse(String response, int[][] status) {
        try {
            // Use regex to find x and y values in the format x=VALUE and y=VALUE
            Pattern pattern = Pattern.compile("x=(\\d+),\\s*y=(\\d+)");
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                int apiX = Integer.parseInt(matcher.group(1));
                int apiY = Integer.parseInt(matcher.group(2));
                System.out.println("x=" + apiX + " y=" + apiY);

                // Check if the selected move is empty
                if (isCellEmpty(status, apiX, apiY)) {
                    optimalX = apiX;
                    optimalY = apiY;
                    // Optionally, update the board or game state here
                } else {
                    System.out.println("Position taken; using Minimax algorithm instead.");
                    FindMove(status);
                }
            } else {
                System.err.println("Could not find coordinates in the response: " + response);
                System.out.println("Using Minimax algorithm instead.");
                FindMove(status);
            }
        } catch (NumberFormatException e) {
            System.err.println("Number format error when processing coordinates: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Using Minimax algorithm instead.");
            FindMove(status);
        }
    }
    
    
    

    public void calculateEvalBoard(int player, int[][] status) {
        int[] DScore = new int[]{0, 1, 9, 81, 729};
        int[] AScore = new int[]{0, 2, 18, 162, 1458};

        int row, col, ePC, eHuman;

        myEvalBoard.ResetBoard();
        //Đánh giá theo hàng ngang
        for (row = 0; row < height; row++) {
            for (col = 0; col < width - 4; col++) {
                ePC = 0;
                eHuman = 0;
                for (int i = 0; i < 5; i++) {
                    if (status[row][col + i] == 1) {
                        eHuman++;
                    }
                    if (status[row][ col + i] == 2) {
                        ePC++;
                    }
                }

                if (eHuman * ePC == 0 && eHuman != ePC) // một trong 2 bằng 0 và không đồng thời bằng 0
                {
                    for (int i = 0; i < 5; i++) {
                        if (status[row][col + i] == 0) // Nếu ô chưa đánh
                        {
                            if (eHuman == 0) {
                                if (player == 1) {
                                    myEvalBoard.EBoard[row][ col + i] += DScore[ePC];
                                } else {
                                    myEvalBoard.EBoard[row][ col + i] += AScore[ePC];
                                }
                            }
                            else if (ePC == 0) {
                                if (player == 2) {
                                    myEvalBoard.EBoard[row][col + i] += DScore[eHuman];
                                } else {
                                    myEvalBoard.EBoard[row][col + i] += AScore[eHuman];
                                }
                            }
                            if (eHuman == 4 || ePC == 4) {
                                myEvalBoard.EBoard[row][col + i] *= 2;
                            }
                        }
                    }

                }
            }
        }
        //Đánh giá theo cột
        for (col = 0; col < width; col++) {
            for (row = 0; row < height - 4; row++) {
                ePC = 0;
                eHuman = 0;
                for (int i = 0; i < 5; i++) {
                    if (status[row + i][col] == 1) {
                        eHuman++;
                    }
                    if (status[row + i][col] == 2) {
                        ePC++;
                    }
                }

                if (eHuman * ePC == 0 && eHuman != ePC) {
                    for (int i = 0; i < 5; i++) {
                        if (status[row + i][col] == 0) // cộng điểm cho các ô chưa đánh
                        {
                            if (eHuman == 0) {
                                if (player == 1) {
                                    myEvalBoard.EBoard[row + i][col] += DScore[ePC];
                                } else {
                                    myEvalBoard.EBoard[row + i][col] += AScore[ePC];
                                }
                            }
                            if (ePC == 0) {
                                if (player == 2) {
                                    myEvalBoard.EBoard[row + i][ col] += DScore[eHuman];
                                } else {
                                    myEvalBoard.EBoard[row + i][col] += AScore[eHuman];
                                }
                            }
                            if (eHuman == 4 || ePC == 4) {
                                myEvalBoard.EBoard[row + i][col] *= 2;
                            }
                        }
                    }

                }
            }
        }

        //Đánh giá theo đường chéo chính
        for (col = 0; col < width - 4; col++) {
            for (row = 0; row < height - 4; row++) {
                ePC = 0;
                eHuman = 0;
                for (int i = 0; i < 5; i++) {
                    if (status[row + i][col + i] == 1) {
                        eHuman++;
                    }
                    if (status[row + i][col + i] == 2) {
                        ePC++;
                    }
                }

                if (eHuman * ePC == 0 && eHuman != ePC) {
                    for (int i = 0; i < 5; i++) {
                        if (status[row + i][col + i] == 0) // Neu o chua duoc danh
                        {
                            if (eHuman == 0) {
                                if (player == 1) {
                                    myEvalBoard.EBoard[row + i][col + i] += DScore[ePC];
                                } else {
                                    myEvalBoard.EBoard[row + i][col + i] += AScore[ePC];
                                }
                            }
                            if (ePC == 0) {
                                if (player == 2) {
                                    myEvalBoard.EBoard[row + i][col + i] += DScore[eHuman];
                                } else {
                                    myEvalBoard.EBoard[row + i][col + i] += AScore[eHuman];
                                }
                            }
                            if (eHuman == 4 || ePC == 4) {
                                myEvalBoard.EBoard[row + i][col + i] *= 2;
                            }
                        }
                    }

                }
            }
        }

            //Đánh giá theo đường chéo phụ
        for (row = 4; row < width; row++) {
            for (col = 0; col < height - 4; col++) {
                ePC = 0;
                eHuman = 0;
                for (int i = 0; i < 5; i++) {
                    if (status[row - i][col + i] == 1) {
                        eHuman++;
                    }
                    if (status[row - i][col + i] == 2) {
                        ePC++;
                    }
                }

                if (eHuman * ePC == 0 && eHuman != ePC) {
                    for (int i = 0; i < 5; i++) {
                        if (status[row - i][col + i] == 0) // Neu o chua duoc danh
                        {
                            if (eHuman == 0) {
                                if (player == 1) {
                                    myEvalBoard.EBoard[row - i][col + i] += DScore[ePC];
                                } else {
                                    myEvalBoard.EBoard[row - i][col + i] += AScore[ePC];
                                }
                            }
                            if (ePC == 0) {
                                if (player == 2) {
                                    myEvalBoard.EBoard[row - i][col + i] += DScore[eHuman];
                                } else {
                                    myEvalBoard.EBoard[row - i][ col + i] += AScore[eHuman];
                                }
                            }
                            if (eHuman == 4 || ePC == 4) {
                                myEvalBoard.EBoard[row - i][col + i] *= 2;
                            }
                        }
                    }

                }
            }
        }

    }


    public void FindMove(int[][] status) {


        calculateEvalBoard(2, status);

        Point temp =  myEvalBoard.MaxPos();
       
        optimalX = temp.x;
        optimalY = temp.y;
        System.out.println("op: " + temp.x + " " + temp.y);

    }

}
