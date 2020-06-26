import java.io.IOException;

public class Application {

	public static void main(final String[] args) throws IOException {
		final int n = 3;
		final int m = 5;
		
		final String imagePath = "https://i2.res.24o.it/images2010/Editrice/ILSOLE24ORE/DOMENICA/2020/06/01/Domenica/ImmaginiWeb/Ritagli/AdobeStock_233945560%20(1)-U205598524LrD--1020x533@IlSole24Ore-Web.jpeg";

		final FrameController controller = new FrameController();
//		final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
//        puzzle.setVisible(true);
		controller.setSize(300,300);
		controller.setVisible(true);
	}
}
