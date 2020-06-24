import com.google.gson.Gson;
import com.google.gson.JsonObject;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

public class FrameController extends JFrame {

    private JButton newPuzzleButton;
    private JButton joinPuzzleButton;
    private NewPuzzleManager newPuzzleManager = new NewPuzzleManager();

    public FrameController() {
        setTitle("Collaborative Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel newPuzzle = new JPanel();
        final JPanel joinPuzzle = new JPanel();
        JLabel imageUrlLabel = new JLabel("Image URL: ");
        JTextField imageUrlText = new JTextField(15);
        JLabel rowLabel = new JLabel("Puzzle row: ");
        JTextField rowText = new JTextField(15);
        JLabel colLabel = new JLabel("Puzzle colums: ");
        JTextField colText = new JTextField(15);
        newPuzzle.setLayout(new GridLayout(5, 1));
        newPuzzle.add(imageUrlLabel);
        newPuzzle.add(imageUrlText);
        newPuzzle.add(rowLabel);
        newPuzzle.add(rowText);
        newPuzzle.add(colLabel);
        newPuzzle.add(colText);
        newPuzzleButton = new JButton("New Puzzle");
        newPuzzleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String urlImage = imageUrlText.getText();
                String rowImage = rowText.getText();
                String colImage = colText.getText();
                try {
                    JsonObject result =  newPuzzleManager.create(urlImage, rowImage, colImage);
                    System.out.println(result);
                    final PuzzleBoard puzzle = new PuzzleBoard(
                         Integer.parseInt(rowImage), Integer.parseInt(colImage), urlImage,
                            result.get("playerid").getAsString(),
                            result.get("puzzleid").getAsString()
                    );
                    puzzle.setVisible(true);
                    dispose();
                } catch (IOException | InterruptedException  ioException) {
                    ioException.printStackTrace();
                }
            }
        });


        newPuzzle.add(newPuzzleButton);

        JLabel puzzleIdLabel = new JLabel("Puzzle ID: ");
        JTextField puzzleIdText = new JTextField(15);
        joinPuzzleButton = new JButton("Join Puzzle");
        joinPuzzleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String puzzleId = puzzleIdText.getText();
                System.out.println(puzzleId);
                try {
                    JsonObject result =  newPuzzleManager.join(puzzleId);
                    System.out.println(result);
                    final PuzzleBoard puzzle = new PuzzleBoard(
                            result.get("rows").getAsInt(),
                            result.get("cols").getAsInt(),
                            result.get("imageurl").getAsString(),
                            result.get("playerid").getAsString(),
                            result.get("_id").getAsString()
                    );
                    puzzle.setVisible(true);
                    dispose();
                } catch (IOException | InterruptedException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        joinPuzzle.setLayout(new GridLayout(5, 2));
        joinPuzzle.add(puzzleIdLabel);
        joinPuzzle.add(puzzleIdText);
        joinPuzzle.add(joinPuzzleButton);
        joinPuzzle.setBorder(BorderFactory.createLineBorder(Color.gray));
        getContentPane().add(newPuzzle, BorderLayout.NORTH);
        getContentPane().add(joinPuzzle, BorderLayout.SOUTH);

    }

}


