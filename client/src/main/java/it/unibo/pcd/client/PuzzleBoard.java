package it.unibo.pcd.client;

import com.google.gson.JsonArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("serial")
public class PuzzleBoard extends JFrame {
	
	final int rows, columns;
	private JsonArray currentState;
	private List<Tile> tiles = new ArrayList<>();
    final JLabel playerId = new JLabel();
    final JLabel puzzleId = new JLabel();
	
	private SelectionManager selectionManager = new SelectionManager();
	
    public PuzzleBoard(final int rows,
                       final int columns,
                       final String imagePath,
                       final String playerid,
                       final String puzzleid,
                       final JsonArray state) throws IOException {
    	this.rows = rows;
		this.columns = columns;
		this.currentState = state;

    	setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final JPanel board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        final JPanel info = new JPanel();
        info.setLayout(new GridLayout(2,2));
        final JLabel playerIdLabel = new JLabel("PlayerID:");
        playerId.setText(playerid);
        final JLabel puzzleIdLabel = new JLabel("PuzzleID:");

        puzzleId.setText(puzzleid);
        info.add(playerIdLabel);
        info.add(playerId);
        info.add(puzzleIdLabel);
        info.add(puzzleId);
        getContentPane().add(info, BorderLayout.SOUTH);
        
        createTiles(imagePath);
        paintPuzzle(board);
    }

    
    private void createTiles(final String imagePath) throws IOException {

        URL url = new URL(imagePath);

        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        conn.connect();
        InputStream urlStream = conn.getInputStream();
        final BufferedImage image ;

        try {
            image = ImageIO.read(url);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = IntStream.range(0,currentState.size())
                .mapToObj(i->currentState.get(i)
                        .getAsInt()).collect(Collectors.toList());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
            	final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns,
                        					i * imageHeight / rows, 
                        					(imageWidth / columns), 
                        					imageHeight / rows)));

                tiles.add(new Tile(imagePortion, position, randomPositions.get(position)));
                position++;
            }
        }
	}
    
    private void paintPuzzle(final JPanel board) {
    	board.removeAll();
    	
    	Collections.sort(tiles);
    	
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> {
                try {
                    selectionManager.selectTile(tile, puzzleId.getText(), playerId.getText(), () -> {
                        paintPuzzle(board);
                        checkSolution();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
    	});
    	
    	pack();
        setLocationRelativeTo(null);
    }

    private void checkSolution() {
    	if(tiles.stream().allMatch(Tile::isInRightPlace)) {
    		JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    	}
    }
}
