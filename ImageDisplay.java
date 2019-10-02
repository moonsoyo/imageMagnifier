import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class ImageDisplay extends JPanel
{
    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne;
    BufferedImage lowContrast;
    BufferedImage imgTwo;
    int width = 1920;
    int height = 1080;

    public static BufferedImage resizeImage(float scaleRatio, BufferedImage imgOriginal, int widthOriginal, int heightOriginal, int widthNew, int heightNew)
    {
        BufferedImage resizedImage = new BufferedImage(widthNew, heightNew, BufferedImage.TYPE_INT_RGB);
    	int[] pixelArray = new int[widthOriginal * heightOriginal];
    	for (int x = 0; x < widthOriginal; x++)
	{
	    for (int y = 0; y < heightOriginal; y++)
	    {
	        pixelArray[y * widthOriginal + x] = imgOriginal.getRGB(x, y);
      	    }
	}

        int[] resizedPixelArray = new int[widthNew * heightNew];
        double newX, newY;
        for (int x = 0; x < widthNew; x++)
	{
	    for (int y = 0; y < heightNew; y++)
	    {
	        newX = Math.floor(x / scaleRatio);
        	newY = Math.floor((y / scaleRatio));
        	resizedPixelArray[y * widthNew + x] = pixelArray[(int)(newY * widthOriginal + newX)];
      	    }
    	}

	for (int x = 0; x < widthNew; x++)
	{
	    for (int y = 0; y < heightNew; y++)
	    {
	        resizedImage.setRGB(x, y, resizedPixelArray[y * widthNew + x]);
      	    }
    	}
    	return resizedImage;
    }

    // Function that creates a circular area in a bufferedimage and turns the rest of the area transparent.
    public static BufferedImage clip(BufferedImage originalImg, float ratioScale, int width, int height, int p, int q)
    {
        BufferedImage clippedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2=clippedImg.createGraphics();
	Shape circle = new java.awt.geom.Ellipse2D.Double(p * ratioScale, q * ratioScale, 200, 200);
	g2.setClip(circle);
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	int screenHeight = screenSize.height;
	int screenWidth = screenSize.width;

	float imgX = 100;
	float imgY = 65;
	g2.drawImage(originalImg, (int)imgX, (int)imgY, null);

	return clippedImg;
    }

    public static BufferedImage antiAliasingFilter(BufferedImage originalImg)
    {
        int imgWidth = originalImg.getWidth();
	int imgHeight = originalImg.getHeight();

	float r = 0;
	float g = 0;
	float b = 0;

	int pixel;

	BufferedImage filteredImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
	for(int y=0; y < imgHeight; y++)
	{
	    for(int x=0; x < imgWidth; x++)
	    {
	        pixel = originalImg.getRGB(x,y);
		r = 4 * ((pixel & 0x00ff0000) >> 16);
		g = 4 * ((pixel & 0x0000ff00) >> 8);
		b = 4 * (pixel & 0x000000ff);

		if((y + 1) < imgHeight)
		{
		    pixel = originalImg.getRGB(x,y+1);
		    r += 2 * ((pixel & 0x00ff0000) >> 16);
		    g += 2 * ((pixel & 0x0000ff00) >> 8);
		    b += 2 * (pixel & 0x000000ff);
		}

		if((x + 1) < imgWidth)
		{
		    pixel = originalImg.getRGB(x+1, y);
		    r += 2 * ((pixel & 0x00ff0000) >> 16);
		    g += 2 * ((pixel & 0x0000ff00) >> 8);
		    b += 2 * (pixel & 0x000000ff);
		}

		if((y - 1) >= 0)
		{
		    pixel = originalImg.getRGB(x, y-1);
		    r += 2 * ((pixel & 0x00ff0000) >> 16);
		    g += 2 * ((pixel & 0x0000ff00) >> 8);
		    b += 2 * (pixel & 0x000000ff);
		}

		if((x - 1) >= 0)
		{
		    pixel = originalImg.getRGB(x-1, y);
		    r += 2 * ((pixel & 0x00ff0000) >> 16);
		    g += 2 * ((pixel & 0x0000ff00) >> 8);
		    b += 2 * (pixel & 0x000000ff);
		}

		if(((x - 1) >= 0) && ((y - 1) >= 0))
		{
		    pixel = originalImg.getRGB(x-1, y-1);
		    r += (pixel & 0x00ff0000) >> 16;
		    g += (pixel & 0x0000ff00) >> 8;
		    b += pixel & 0x000000ff;
		}

		if(((x + 1) < imgWidth) && ((y - 1) >= 0))
		{
		    pixel = originalImg.getRGB(x+1, y-1);
		    r += (pixel & 0x00ff0000) >> 16;
		    g += (pixel & 0x0000ff00) >> 8;
		    b += pixel & 0x000000ff;
		}

		if(((x - 1) >= 0) && ((y + 1) < imgHeight))
		{
		    pixel = originalImg.getRGB(x-1, y+1);
		    r += (pixel & 0x00ff0000) >> 16;
		    g += (pixel & 0x0000ff00) >> 8;
		    b += pixel & 0x000000ff;
		}

		if(((x + 1) < imgWidth) && ((y + 1) < imgHeight))
		{
		    pixel = originalImg.getRGB(x+1, y+1);
		    r += (pixel & 0x00ff0000) >> 16;
		    g += (pixel & 0x0000ff00) >> 8;
		    b += pixel & 0x000000ff;
		}

		pixel = (Math.round(r/16) << 16) | (Math.round(g/16) << 8) | (Math.round(b/16));
		filteredImg.setRGB(x,y,pixel);
	    }
	}
	return filteredImg;
    }

    public static BufferedImage lowerContrast(BufferedImage img)
    {
        int imgWidth = img.getWidth();
	int imgHeight = img.getHeight();

	float r = 0;
	float g = 0;
	float b = 0;

	int newPixel;
	int pixel;

	BufferedImage lowContrastImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
	for(int j=0; j < imgHeight; j++)
	{
	    for(int i=0; i < imgWidth; i++)
	    {
	        pixel = img.getRGB(i,j);
	        r = (pixel & 0x00ff0000) >> 16;
		g = (pixel & 0x0000ff00) >> 8;
		b = pixel & 0x000000ff;

		newPixel = (Math.round(r/3) << 16) | (Math.round(g/3) << 8) | (Math.round(b/3));
		lowContrastImg.setRGB(i,j,newPixel);
	    }
	}
	return lowContrastImg;
    }

    /** Read Image RGB
     *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
    {
        try
	{
	    int frameLength = width*height*3;

	    File file = new File(imgPath);
	    RandomAccessFile raf = new RandomAccessFile(file, "r");
	    raf.seek(0);

	    long len = frameLength;
	    byte[] bytes = new byte[(int) len];

	    raf.read(bytes);

	    int ind = 0;
	    for(int y = 0; y < height; y++)
	    {
	        for(int x = 0; x < width; x++)
	        {
		    byte a = 0;
		    byte r = bytes[ind];
		    byte g = bytes[ind+height*width];
		    byte b = bytes[ind+height*width*2];

		    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    //int pix = ((a << 24) + (r << 16) + (g << 8) + b);
		    img.setRGB(x,y,pix);
		    ind++;
		}
	    }
	}
	catch (FileNotFoundException e)
	{
	    e.printStackTrace();
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
    }

    public void showIms(String[] args)
    {
        final MouseCoordDetector m = new MouseCoordDetector();

        // Read a parameter from command line
        String modeNum = args[1];
        float scaleRatio = Float.valueOf(args[2].trim()).floatValue();
        int scaledImageWidth = Math.round((float)width*scaleRatio);
        int scaledImageHeight = Math.round((float)height*scaleRatio);
        String antiAliasingMode = args[3];

        // Read in the specified image
        imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        readImageRGB(width, height, args[0], imgOne);

        lowContrast = lowerContrast(imgOne);

        BufferedImage filteredImage = null;
        filteredImage = antiAliasingFilter(imgOne);

        // Use label to display the image
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        if (modeNum.equals("1"))
        {
            if(antiAliasingMode.equals("0"))
	    {
	        imgTwo = resizeImage(scaleRatio, imgOne, width, height, scaledImageWidth, scaledImageHeight);
	        lbIm1 = new JLabel(new ImageIcon(imgTwo));
	    }
	    if(antiAliasingMode.equals("1"))
	    {
	        imgTwo = resizeImage(scaleRatio, filteredImage, width, height, scaledImageWidth, scaledImageHeight);
	        lbIm1 = new JLabel(new ImageIcon(imgTwo));
	    }
        }

        if (modeNum.equals("2"))
        {
            if(antiAliasingMode.equals("0"))
            {
                imgTwo = resizeImage(scaleRatio, imgOne, width, height, scaledImageWidth, scaledImageHeight);
	        lbIm1 = new JLabel(new ImageIcon(lowContrast));
            }
            if(antiAliasingMode.equals("1"))
            {
                imgTwo = resizeImage(scaleRatio, filteredImage, width, height, scaledImageWidth, scaledImageHeight);
	        lbIm1 = new JLabel(new ImageIcon(lowContrast));
            }
        }

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);
        frame.pack();

        if (modeNum.equals("2"))
        {
            JLayeredPane layer = frame.getRootPane().getLayeredPane();
            layer.add(m, JLayeredPane.DRAG_LAYER);
            m.setBounds(-100, -100, frame.getWidth() + 100, frame.getHeight() + 100);

            lbIm1.addMouseMotionListener(new MouseMotionAdapter()
            {
                public void mouseMoved(MouseEvent e)
                {
	            m.p = e.getX();
	            m.q = e.getY();
	            m.scaleRatio = scaleRatio;
	            m.clipImage = clip(imgTwo, scaleRatio, scaledImageWidth, scaledImageHeight, m.p, m.q);
	            m.repaint();
                }
            });
        }
        frame.setVisible(true);
    }

    public static void main(String[] args) 
    {
        ImageDisplay ren = new ImageDisplay();
	ren.showIms(args);
    }
}

class MouseCoordDetector extends JComponent
{
    public int p;
    public int q;
    public BufferedImage clipImage;
    public float scaleRatio;

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int scaledImageLocationX = (int)(Math.floor(p * (1 - scaleRatio)));
        int scaledImageLocationY = (int)(Math.floor(q * (1 - scaleRatio)));

        g.drawImage(clipImage, scaledImageLocationX, scaledImageLocationY, null);
    }
}
