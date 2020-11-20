/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package circledetector;

import static circledetector.GUIManager.resize;
import java.awt.Color;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import net.jafama.FastMath;

/**
 *
 * @author seanjhardy
 */
public class CircleDetector {
  
  private static BufferedImage image, greyImage, filteredImage, circleImage;
  private static GUIManager frame;
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      String name = (new Scanner(System.in)).next();
      image = ImageIO.read(new File(name));
      double scale = FastMath.min(200.0/image.getWidth(), 200.0/image.getHeight());
      image = resize(image,image.getWidth()*scale, image.getHeight()*scale);
      greyImage = new BufferedImage(image.getWidth(), image.getHeight(),  
      BufferedImage.TYPE_BYTE_GRAY);  
      Graphics g = greyImage.getGraphics();  
      g.drawImage(image, 0, 0, null);  
      g.dispose(); 
      int[][] filteredMatrix = filterImage(greyImage);
      ArrayList<int[]> circles = detectCircles(filteredMatrix);
      g = filteredImage.getGraphics();  
      g.setColor(RED);
      for(int[] circle : circles){
        g.drawOval(circle[0] - circle[2], circle[1] - circle[2], 
                circle[2]*2, circle[2]*2);  
      }
      g.dispose(); 
      
    } catch (IOException e) {
      System.out.println(e);
    }
    frame = new GUIManager();
  }
  
  public static ArrayList<int[]> detectCircles(int[][] img){
    circleImage = new BufferedImage(image.getWidth(), image.getHeight(),  
      BufferedImage.TYPE_4BYTE_ABGR);  
    Graphics g = circleImage.getGraphics();  
    int[][][] circleMatrix = new int[200][img.length][img[0].length];
    Random rand = new Random();
    int maxCount = 0;
    
    for(int r = 10; r < 200; r++){
      for(int x = 0; x < img.length; x++){
        for(int y = 0; y < img[0].length; y++){
          //if(rand.nextInt(100) < 50) continue;
          int value = img[x][y];
          //value = (value>>8)&0xFF;
          if(value == 0) continue;
          circleMatrix[r][x][y] += 1;
          for(double t = 0; t < Math.PI*2; t += 0.2){
            int newX = (int) ((double)x + FastMath.cos(t)*(double)r);
            int newY = (int) ((double)y + FastMath.sin(t)*(double)r);
            if(newX < 0 || newX >= img.length || newY < 0 || newY >= img[0].length) continue;
            circleMatrix[r][newX][newY] += 1;
            int count = circleMatrix[r][newX][newY];
            maxCount = FastMath.max(count, maxCount);
          }
        }
      }
    }
    ArrayList<int[]> circles = new ArrayList<>();
    for(int r = 5; r < 50; r++){
      for(int x = 0; x < circleMatrix[0].length; x++){
        for(int y = 0; y < circleMatrix[0][0].length; y++){
          double count = circleMatrix[r][x][y];
          if(count >= maxCount*0.9){
            circles.add(new int[]{x, y, r});
          }
        }
      }
    }
    double[][] colours = new double[circleMatrix[0].length][circleMatrix[0][0].length];
    for(int[] circle : circles){
      int r = circle[2];
      for(int x = 0; x < circleMatrix[0].length; x++){
        for(int y = 0; y < circleMatrix[0][0].length; y++){
          double count = circleMatrix[r][x][y];
          int colourVal = (int) ((count*255.0)/maxCount);
          colours[x][y] = FastMath.max(colours[x][y], colourVal);
        }
      }
    }
    for(int x = 0; x < circleMatrix[0].length; x++){
      for(int y = 0; y < circleMatrix[0][0].length; y++){
        g.setColor(new Color((int) colours[x][y], 0, 0));
        g.drawRect(x,y,1,1);
      }
    }
    g.dispose();
    return circles;
  }
  
  public static int[][] filterImage(BufferedImage img){
    int[][] imageMatrix = new int[img.getWidth()][img.getHeight()];
    filteredImage = new BufferedImage(img.getWidth(), img.getHeight(),  
    BufferedImage.TYPE_INT_RGB);
    int[] GxFilter = {-1, 0, 1,
                      -2, 0, 2,
                      -1, 0, 1};
    int[] GyFilter = {-1, -2, -1,
                      0, 0, 0,
                      1, 2, 1};
    for(int x = 0; x < img.getWidth(); x++){
      for(int y = 0; y < img.getHeight(); y++){
        int Gx = applyFilter(GxFilter, img, x, y);
        int Gy = applyFilter(GyFilter, img, x, y);
        int value = (int) FastMath.sqrt(FastMath.pow(Gx, 2) + FastMath.pow(Gy, 2));
        if(value < 150) value = 0;
        int rgb = 0;
        rgb = (rgb << 8) + value;
        rgb = (rgb << 8) + 0;
        imageMatrix[x][y] = value;
        filteredImage.setRGB(x, y, rgb);
      }
    }
    return imageMatrix;
  }
  
  public static int applyFilter(int[] filter, BufferedImage img, int x, int y){
    int val = 0;
   val += (x > 0 && y > 0) ? 
            filter[0]*(new Color(img.getRGB(x-1, y-1)).getRed()) : 0;
    val += (y > 0) ? 
            filter[1]*(new Color(img.getRGB(x, y-1)).getRed()) : 0;
    val += (x < img.getWidth()-1 && y > 0) ?
            filter[2]*(new Color(img.getRGB(x+1, y-1)).getRed()) : 0;

    val += (x > 0) ?
            filter[3]*(new Color(img.getRGB(x-1, y)).getRed()) : 0;
    val += (x > 0) ? 
            filter[4]*(new Color(img.getRGB(x, y)).getRed()) : 0;
    val += (x < img.getWidth()-1) ?  
            filter[5]*(new Color(img.getRGB(x+1, y)).getRed()) : 0;

    val += (x > 0 && y < img.getHeight()-1) ? 
            filter[6]*(new Color(img.getRGB(x-1, y+1)).getRed()) : 0;
    val += (x > 0 && y < img.getHeight()-1) ? 
            filter[7]*(new Color(img.getRGB(x, y+1)).getRed()): 0;
    val += (x < img.getWidth()-1 && y < img.getHeight()-1) ?
            filter[8]*(new Color(img.getRGB(x+1, y+1)).getRed()) : 0;
    return val;
  }
  public static int applyFilter(int[] filter, int[][] matrix, int x, int y){
    int val = 0;
   val += (x > 0 && y > 0) ? 
            filter[0]*matrix[x-1][y-1] : 0;
    val += (y > 0) ? 
            filter[1]*matrix[x][y-1] : 0;
    val += (x < matrix.length-1 && y > 0) ?
            filter[2]*matrix[x+1][y-1] : 0;

    val += (x > 0) ?
            filter[3]*matrix[x-1][y] : 0;
    val += (x > 0) ? 
            filter[4]*matrix[x][y] : 0;
    val += (x < matrix.length-1) ?  
            filter[5]*matrix[x+1][y] : 0;

    val += (x > 0 && y < matrix[0].length-1) ? 
            filter[6]*matrix[x-1][y+1] : 0;
    val += (x > 0 && y < matrix[0].length-1) ? 
            filter[7]*matrix[x][y+1] : 0;
    val += (x < matrix.length-1 && y < matrix[0].length-1) ?
            filter[8]*matrix[x+1][y+1] : 0;
    return val;
  }
  
  
  public static BufferedImage getImage(){
    return image;
  }
  public static BufferedImage getFilteredImage(){
    return filteredImage;
  }
  public static BufferedImage getCircleImage(){
    return circleImage;
  }
  public static BufferedImage getGreyscaleImage(){
    return greyImage;
  }
}
