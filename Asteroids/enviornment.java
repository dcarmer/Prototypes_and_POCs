import java.awt.Toolkit;
import java.util.Random;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Polygon;
import java.util.Arrays;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.RadialGradientPaint;
import java.awt.Point;

import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class enviornment
{
   public static final int XRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth());
   public static final int YRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
   
   public static final boolean[] keyStat = new boolean[600];
   public static final boolean[] mouseStat = new boolean[4];
   public static final ArrayList<asteroid> feild = new ArrayList();
   public static BufferStrategy bs;
   
   public static final rocketShip[] SHIPS = new rocketShip[]
   {
      new rocketShip(Color.RED, "Red", KeyEvent.VK_W,KeyEvent.VK_A,KeyEvent.VK_S,KeyEvent.VK_D),
      new rocketShip(Color.BLUE, "Blue", KeyEvent.VK_UP,KeyEvent.VK_LEFT,KeyEvent.VK_DOWN,KeyEvent.VK_RIGHT)
      //new rocketShip(Color.PINK, "Pink", KeyEvent.VK_I,KeyEvent.VK_J,KeyEvent.VK_K,KeyEvent.VK_L)
   }; 
   public static final QuadTree quad = new QuadTree(0, new Rectangle(0,0,XRES,YRES));
   //public static final int[][] STARS = new int[3][100];
   public static final Planet terra = new Planet();
   public static final Random r = new Random();
   public static void main(String[] args)
   {
      /* Frame Setup */
      JFrame f = new JFrame("RocketShipShooter");
      f.setSize(XRES,YRES);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.addKeyListener(new KeyController());
      f.addMouseListener(new MouseController());
      f.setResizable(false);
      f.setUndecorated(false);
      f.setVisible(true);
      /* Graphics Setup */
      f.createBufferStrategy(2);
      bs = f.getBufferStrategy();
      
      /*for(int i=0;i<100;i++)
      {
         STARS[0][i] = r.nextInt(XRES);
         STARS[1][i] = r.nextInt(YRES);
         STARS[2][i] = r.nextInt(3)*2;
      }*/
      while(!keyStat[KeyEvent.VK_ESCAPE])
      {
         addAsteriod();
         drawObjects();
         moveObjects();
         quadObjects();
      }
      System.exit(0);
   }
   private static void addAsteriod()
   {
      if(feild.size()>=100)
      {
         return;
      }
      if(r.nextInt(400)==0)
      {
         feild.add(new asteroid());
      }
   }
   private static void drawObjects()
   {
      do
      {
         drawBackground();
         for(rocketShip rs: SHIPS)
         {
            for(rocket s: rs.getShots())
            {
               s.drawSelf(bs);
            }
            rs.drawSelf(bs);
         }
         for(asteroid a: feild)
         {
            a.drawSelf(bs);
         }
         bs.show();
      }while(bs.contentsLost());
   }
   private static void moveObjects()
   {
      for(int i=feild.size()-1;i>-1;i--)
      {
         feild.get(i).applyGravity(terra);
         feild.get(i).move();
         if(feild.get(i).outOfBounds)
         {
            feild.remove(i);
         }
      }
      for(rocketShip rs: SHIPS)
      {
         rs.move();
         for(int i=rs.getShots().size()-1;i>-1;i--)
         {
            rs.getShots().get(i).applyGravity(terra);
            rs.getShots().get(i).move();
            if(rs.getShots().get(i).outOfBounds)
            {
               rs.getShots().remove(i);
            }
         }
      }
   }
   public static void quadObjects()
   {
      quad.clear();
      for(int i=0;i<feild.size();i++)
      {
         quad.insert(feild.get(i));
      }
      ArrayList<asteroid> returnObjs = new ArrayList<asteroid>();
      for(rocketShip rs: SHIPS)
      {
         for(int i=rs.getShots().size()-1;i>-1;i--)
         {
            returnObjs.clear();
            quad.retrieveAsteroids(returnObjs,new Rectangle((int)rs.getShots().get(i).getX()-5,(int)rs.getShots().get(i).getY()-5,10,10));
            for(asteroid a: returnObjs)
            {
               if(a.getPoly().contains(rs.getShots().get(i).getX(),rs.getShots().get(i).getY()))
               {
                  a.crackOpen();
                  feild.remove(a);
                  rs.getShots().remove(i);
                  break;
               }
            }
         }
      }
      for(rocketShip rs: SHIPS)
      {
         if(rs.isDead())
         {
            continue;
         }
         returnObjs.clear();
         quad.retrieveAsteroids(returnObjs,new Rectangle((int)rs.getX()-5,(int)rs.getY()-5,10,10));
         for(asteroid a: returnObjs)
         {
            if(a.getPoly().intersects(rs.getPoly().getBounds()))
            {
               rs.kill(a);
               feild.remove(a);
               break;
            }
         }
         for(rocketShip rs2: SHIPS)
         {
            if(rs2 == rs)
            {
               continue;
            }
            for(rocket s: rs2.getShots())
            {
               if(rs.getPoly().contains(s.getX(),s.getY()))
               {
                  rs2.getShots().remove(s);
                  rs.kill(rs2);
                  break;
               }
            }
         }
      }      
   }
   private static void drawBackground()
   {
      do
      {
         Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
         g2D.fillRect(0,0,XRES,YRES);
         g2D.setColor(Color.WHITE);
         /*for(int i=0;i<100;i++)
         {
            if(r.nextInt(1000)==0)
            {
               STARS[2][i] = r.nextInt(3)*2;
            }
            g2D.fillRect(STARS[0][i]-STARS[2][i]/2,STARS[1][i]-STARS[2][i]/2,STARS[2][i],STARS[2][i]);
         }*/
         g2D.dispose();
      }while(bs.contentsRestored());
      terra.drawSelf(bs);
   }
}


















class asteroid
{
   double xPos; 
   double yPos;
   double speed, xSpeed, ySpeed, xAccel,yAccel;
   double direction;
   boolean outOfBounds;
   private final int level;
   private final Random r = new Random();
   int[] radi = new int[8];
   int[] angle = new int[8];
   public asteroid()
   {
      if(r.nextInt(2)==0)
      {
         xPos = r.nextInt(enviornment.XRES+1);
         if(r.nextInt(2)==0)
         {
            yPos = 0;
            direction = r.nextInt(180)+180;
         }
         else
         {
            yPos = enviornment.YRES;
            direction = r.nextInt(180);
         }
         
      }
      else
      {
         if(r.nextInt(2)==0)
         {
            xPos = 0;
            direction = r.nextInt(180)-90;
         }
         else
         {
            xPos = enviornment.XRES;
            direction = r.nextInt(180)+90;
         }
         
         yPos = r.nextInt(enviornment.YRES+1);
      }
      speed = .3;
      xSpeed = speed*Math.cos(Math.toRadians(direction));
      ySpeed = speed*Math.sin(Math.toRadians(direction));
      xAccel=0;
      yAccel=0;
      outOfBounds = false;
      level = r.nextInt(5)+1;
      for(int i=0;i<8;i++)
      {
         radi[i] = r.nextInt(13*level)+5*level;
         angle[i] = r.nextInt(45)+45*i;
      }
      
   }
   public asteroid(double x, double y, double degree, int l)
   {
      xPos = x;
      yPos = y;
      direction = degree;
      speed = .1;
      xSpeed = speed*Math.cos(Math.toRadians(direction));
      ySpeed = speed*Math.sin(Math.toRadians(direction));
      xAccel=0;
      yAccel=0;
      outOfBounds = false;
      for(int i=0;i<8;i++)
      {
         radi[i] = r.nextInt(13*l)+l*5;
         angle[i] = r.nextInt(45)+45*i;
      }
      level = l;
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   public Polygon getPoly()
   {
      return new Polygon(
      new int[]{
         (int)(xPos+radi[0]*Math.cos(Math.toRadians(angle[0]))),
         (int)(xPos+radi[1]*Math.cos(Math.toRadians(angle[1]))),
         (int)(xPos+radi[2]*Math.cos(Math.toRadians(angle[2]))),
         (int)(xPos+radi[3]*Math.cos(Math.toRadians(angle[3]))),
         (int)(xPos+radi[4]*Math.cos(Math.toRadians(angle[4]))),
         (int)(xPos+radi[5]*Math.cos(Math.toRadians(angle[5]))),
         (int)(xPos+radi[6]*Math.cos(Math.toRadians(angle[6]))),
         (int)(xPos+radi[7]*Math.cos(Math.toRadians(angle[7])))},
      new int[]{
         (int)(yPos+radi[0]*Math.sin(Math.toRadians(angle[0]))),
         (int)(yPos+radi[1]*Math.sin(Math.toRadians(angle[1]))),
         (int)(yPos+radi[2]*Math.sin(Math.toRadians(angle[2]))),
         (int)(yPos+radi[3]*Math.sin(Math.toRadians(angle[3]))),
         (int)(yPos+radi[4]*Math.sin(Math.toRadians(angle[4]))),
         (int)(yPos+radi[5]*Math.sin(Math.toRadians(angle[5]))),
         (int)(yPos+radi[6]*Math.sin(Math.toRadians(angle[6]))),
         (int)(yPos+radi[7]*Math.sin(Math.toRadians(angle[7])))},
      8);
   }
   public void applyGravity(Planet p)
   {
      double xDiff = p.getX()-xPos;
      double yDiff = p.getY()-yPos;
      double dist2 = Math.pow(xDiff,2)+Math.pow(yDiff,2);
      double radian;
      if(xDiff>=0)
      {
         if(yDiff>=0)
         {
            radian = Math.atan(yDiff/xDiff);
         }
         else
         {
            radian = Math.PI*2-Math.atan(Math.abs(yDiff/xDiff));
         }
      }
      else
      {
         if(yDiff>=0)
         {
            radian = Math.PI-Math.atan(Math.abs(yDiff/xDiff));
         }
         else
         {
            radian = Math.PI+Math.atan(yDiff/xDiff);
         }
      }
      
      double accel = p.getDiameter()/(16*dist2);
      xAccel = accel*Math.cos(radian);
      yAccel = accel*Math.sin(radian);
   }
   public void move()
   {
      xPos += xSpeed+xAccel/2;
      yPos -= ySpeed+yAccel/2;
      xSpeed += xAccel;
      ySpeed -= yAccel;
      if(xPos > enviornment.XRES || xPos < 0 || yPos > enviornment.YRES || yPos < 0 || enviornment.terra.getEllipse(0).contains(xPos,yPos))
      {
         outOfBounds = true;
      }
   }
   public void crackOpen()
   {
      if(level <= 1)
      {
         return;
      }
      enviornment.feild.add(new asteroid(xPos,yPos,r.nextInt(360),level-1));
      enviornment.feild.add(new asteroid(xPos,yPos,r.nextInt(360),level-1));
   }
   public void drawSelf(BufferStrategy b)
   {
      do
      {
         Graphics2D g2D = (Graphics2D)b.getDrawGraphics();
         g2D.setColor(Color.GRAY);
         g2D.fill(getPoly());
         g2D.dispose();
      }while(b.contentsRestored());
   }
}









class Planet
{
   private static final Random r = new Random();
   private final int diameter,xPos,yPos;
   private final int[] radi = new int[8], angle = new int[8];
   public Planet()
   {
      diameter = r.nextInt(300)+200;
      xPos = r.nextInt(enviornment.XRES);
      yPos = r.nextInt(enviornment.YRES);
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   public double getDiameter()
   {
      return diameter;
   }
   public Ellipse2D getEllipse(int var)
   {
      return new Ellipse2D.Double(xPos-diameter/2-var,yPos-diameter/2-var,diameter+var*2,diameter+var*2);
   }
   public void drawSelf(BufferStrategy b)
   {
      do
      {
         Graphics2D g2D = (Graphics2D)b.getDrawGraphics();
         g2D.setPaint(new RadialGradientPaint(new Point(xPos,yPos),diameter,new Point(xPos,yPos), new float[]{0.0f,0.7f},new Color[]{new Color(255,255,255,255),new Color(0,0,0,0)},RadialGradientPaint.CycleMethod.NO_CYCLE));
         g2D.fill(getEllipse(diameter));
         g2D.setColor(Color.BLUE);
         g2D.fill(getEllipse(0));
         g2D.dispose();
      }while(b.contentsRestored());
   }
}























final class QuadTree
{
   private final int MAX_OBJECTS = 10;
   private final int MAX_LEVELS = 30;
   
   private int level;
   private ArrayList<asteroid> objects;
   private Rectangle bounds;
   private QuadTree[] nodes;
   
   public QuadTree(int pLevel, Rectangle pBounds)
   {
      level = pLevel;
      objects = new ArrayList();
      bounds = pBounds;
      nodes = new QuadTree[4];
   }
   public void clear()
   {
      objects.clear();
      for(int i=0;i<nodes.length;i++)
      {
         if(nodes[i] != null)
         {
            nodes[i].clear();
            nodes[i]= null;
         }
      }
   }
   private void split()
   {
      int subWidth = (int)(bounds.getWidth()/2);
      int subHeight = (int)(bounds.getHeight()/2);
      int x = (int)bounds.getX();
      int y = (int)bounds.getY();
      nodes[0] = new QuadTree(level+1,new Rectangle(x+subWidth, y, subWidth, subHeight));
      nodes[1] = new QuadTree(level+1,new Rectangle(x, y, subWidth, subHeight));
      nodes[2] = new QuadTree(level+1,new Rectangle(x, y+subHeight, subWidth, subHeight));
      nodes[3] = new QuadTree(level+1,new Rectangle(x+subWidth, y+subHeight, subWidth, subHeight));
   }
   private int getIndex(Rectangle pRect)
   {
      int index = -1;
      double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
      double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

      boolean topQuadrant = (pRect.getY() < horizontalMidpoint && pRect.getY() + pRect.getHeight() < horizontalMidpoint);
      boolean bottomQuadrant = (pRect.getY() > horizontalMidpoint);
 
      if (pRect.getX() < verticalMidpoint && pRect.getX() + pRect.getWidth() < verticalMidpoint)
      {
         if (topQuadrant)
         {
            index = 1;
         }
         else if (bottomQuadrant)
         {
            index = 2;
         }
      }
      else if (pRect.getX() > verticalMidpoint)
      {
         if (topQuadrant)
         {
            index = 0;
         }
         else if (bottomQuadrant)
         {
               index = 3;
         }
      }
      return index;
   }
   public void insert(asteroid aster)
   {
      if (nodes[0] != null)
      {
         int index = getIndex(new Rectangle((int)aster.getX()-25,(int)aster.getY()-25,50,50));
         if (index != -1)
         {
            nodes[index].insert(aster);
            return;
         }
      }
      objects.add(aster);
      if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS)
      {
         if (nodes[0] == null)
         { 
            split(); 
         }
         int i = 0;
         while (i < objects.size())
         {
            int index = getIndex(new Rectangle((int)objects.get(i).getX()-25,(int)objects.get(i).getY()-25,50,50));
            if (index != -1)
            {
               nodes[index].insert(objects.remove(i));
            }
            else 
            {
               i++;
            }
         }
      }
   }
   public ArrayList<asteroid> retrieveAsteroids(ArrayList<asteroid> returnObjects, Rectangle pRect)
   {
      int index = getIndex(pRect);
      if (index != -1 && nodes[0] != null)
      {
         nodes[index].retrieveAsteroids(returnObjects, pRect);
      }
      returnObjects.addAll(objects);
      return returnObjects;
   }
}















// Handles keys
final class KeyController implements KeyListener
{
   public KeyController(){/* Nothing */}
   public void keyPressed(KeyEvent key) 
   {
      enviornment.keyStat[key.getKeyCode()] = true;
   }
   public void keyReleased(KeyEvent key) 
   { 
      enviornment.keyStat[key.getKeyCode()] = false;
   }
   public void keyTyped(KeyEvent e){/* Nothing */}
}





// handles mouse
final class MouseController implements MouseListener
{
   public MouseController(){/* Nothing */}
   public void mousePressed(MouseEvent button) 
   { 
      enviornment.mouseStat[button.getButton()] = true;
   }
   public void mouseReleased(MouseEvent button) 
   { 
      enviornment.mouseStat[button.getButton()] = false; 
   }
   public void mouseExited(MouseEvent e){/* Nothing */}
   public void mouseClicked(MouseEvent e){/* Nothing */}
   public void mouseEntered(MouseEvent e){/* Nothing */}
} 