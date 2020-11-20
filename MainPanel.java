/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circledetector;
import static circledetector.CircleDetector.getCircleImage;
import static circledetector.CircleDetector.getFilteredImage;
import static circledetector.CircleDetector.getGreyscaleImage;
import static circledetector.CircleDetector.getImage;
import static circledetector.GUIManager.getColour;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public final class MainPanel extends JPanel{

  private Rectangle frameBounds;
  private BufferedImage image;

  public MainPanel(){
    //set the background colour
    setBackground(getColour("background"));
  }
  
  @Override
  public void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    
    //draw background
    g.setColor(getColour("background"));
    g.fillRect(0,0,1920,1080);
    BufferedImage image = getImage();
    BufferedImage filteredImage = getFilteredImage();
    BufferedImage circleImage = getCircleImage();
    BufferedImage greyscaleImage = getGreyscaleImage();
    if(image != null){
      double size = FastMath.min(500.0/image.getWidth(), 500.0/image.getHeight());
      image = resize(image, image.getWidth()*size, image.getHeight()*size);
      filteredImage = resize(filteredImage,filteredImage.getWidth()*size, filteredImage.getHeight()*size);
      circleImage = resize(circleImage,circleImage.getWidth()*size, circleImage.getHeight()*size);
      greyscaleImage = resize(greyscaleImage,greyscaleImage.getWidth()*size, greyscaleImage.getHeight()*size);
      g.drawImage(image, 0, 0, null);
      g.drawImage(greyscaleImage, image.getWidth(), 0, null);
      g.drawImage(filteredImage, 0, image.getHeight(), null);
      g.drawImage(circleImage, image.getWidth(), image.getHeight(), null);
    }
    
  }
  
  public static BufferedImage resize(BufferedImage img, double newW, double newH) {
        BufferedImage after = new BufferedImage((int)newW, (int)newH, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(newW/img.getWidth(), newH/img.getHeight());
        AffineTransformOp scaleOp = 
           new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        after = scaleOp.filter(img, after);
        return after;
    }
  

}
