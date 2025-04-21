import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Converts an image to ASCII art.", name = "img2ascii", version = "img2ascii 1.0")
public class ImageConverter implements Callable<Void> {
    private static final double WEIGHT_RED = 0.299;
    private static final double WEIGHT_GREEN = 0.587;
    private static final double WEIGHT_BLUE = 0.114;

    @Parameters(paramLabel = "<img-file>", description = "Path to image file")
    private File imgFile;

    @Option(names = {"-o", "--out"}, paramLabel = "<ascii-file>", description = "Path to output file")
    private String asciiFile = "out.txt";

    @Option(names = {"-w", "--width"}, paramLabel = "<int>", description = "Max width of ASCII output in chars", type = Integer.class)
    private int maxWidth = 240;

    @Option(names = {"-h", "--height"}, paramLabel = "<int>", description = "Max height of ASCII output in chars", type = Integer.class)
    private int maxHeight = 65;

    @Option(names = {"-f", "--font-size"}, paramLabel = "<int>", description = "Font size to optimise size for", type = Integer.class)
    private int fontSize;

    @Option(names = {"-r", "--ramp"}, paramLabel = "<gray-ramp>", description = "Set default ASCII char ramp for conversion", type=String.class)
    private String ramp = "@%#*+=-:. ";
    // private String ramp = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";

    @Option(names = {"--help"}, usageHelp = true, description = "Display this help message")
    private boolean help = false;

    private BufferedImage img;
    private int width;
    private int height;

    private static int[] fontSizeToPixels(int fontSize) {
        switch(fontSize) {
            case 1:  return new int[]{1910, 975};
            case 2:  return new int[]{950, 240};
            case 3:  return new int[]{950, 195};
            case 4:  return new int[]{610, 160};
            case 5:  return new int[]{470, 105};
            case 6:  return new int[]{380, 105};
            case 7:  return new int[]{380, 95};
            case 8:  return new int[]{270, 75};
            case 9:  return new int[]{270, 65};
            case 10: return new int[]{240, 65};
            case 11: return new int[]{210, 50};
            case 12: return new int[]{190, 50};
            default: return new int[]{240, 65};
        }
    }

    private static int[] scaleImage(int width, int height, int maxWidth, int maxHeight) {
        if (height > maxHeight) {
            width = (int) Math.floor(width * (float) maxHeight / height);
            height = maxHeight;
        }

        if (width > maxWidth) {
            height = (int) Math.floor(height * (float) maxWidth / width);
            width = maxWidth;
        }

        return new int[] {width, height};
    }

    private static long rgbToGrayscale(int rgb) {
        int alpha = (rgb >> 24) & 0xff;
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;

        double gray = (red * WEIGHT_RED +
                green * WEIGHT_GREEN +
                blue * WEIGHT_BLUE);

        if (alpha == 0)
            gray = 255;

        return Math.round(gray);
    }

    private int getAvgGrayscale(int rowStart, int rowEnd, int colStart, int colEnd) {
        int grayscale = 0;
        int pixelRGB;
        for (int row = rowStart; row < rowEnd; row++) {
            for (int col = colStart; col < colEnd; col++) {
                pixelRGB = img.getRGB(col, row);
                grayscale += rgbToGrayscale(pixelRGB);
            }
        }
        grayscale /= (rowEnd - rowStart) * (colEnd - colStart);
        return grayscale;
    }

    public void convertToASCII() {
        int[] dimensions = scaleImage(width, height, maxWidth, maxHeight);
        int chunkWidth = (int) Math.ceil(width / (float) dimensions[0]);
        int chunkHeight = (int) Math.ceil(height / (float) dimensions[1]);

        long grayscale;
        List<String> lines = new ArrayList<>();
        String line;

        int chars = ramp.length();

        Path file = Paths.get(asciiFile);
        for (int row = 0; row < height; row += chunkHeight) {
            line = "";
            for (int col = 0; col < width; col += chunkWidth) {
                // Get average grayscale of chunk
                grayscale = getAvgGrayscale(row, Math.min(height, row + chunkHeight), col, Math.min(width, col + chunkWidth));
                line += ramp.charAt((int) Math.ceil((chars - 1) * grayscale / 255.0));
            }
            lines.add(line);
        }

        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) { }
    }

    public static void main(String[] args) {
        CommandLine.call(new ImageConverter(), args);
    }

    @Override
    public Void call() throws IOException {
        img = ImageIO.read(imgFile);

        width = img.getWidth();
        height = img.getHeight();

        if (fontSize != 0) {
            int[] dimensions = fontSizeToPixels(fontSize);
            maxWidth = dimensions[0];
            maxHeight = dimensions[1];
        }

        convertToASCII();

        return null;
    }
}
