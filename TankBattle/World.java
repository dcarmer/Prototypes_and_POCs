import java.awt.Toolkit;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Point;
import java.awt.Rectangle;

public final class World
{
   public static final int XRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()); 
   public static final int YRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
   public static final boolean [] keyStat = new boolean [600];
   public static final boolean [] mouse = new boolean [4];
   public static final ArrayList<Shell> shells = new ArrayList<Shell>();
   public static final ArrayList<Wall> walls = new ArrayList<Wall>();
   public static final Tank[] tanks = new Tank[2];
   public static final Random r = new Random();
   public static final int tSpeed = 1, sSpeed = 3;
   public static final int tWidth = 50, tHeight = 50, LIFE = 10;
   public static final int sWidth = 10, sHeight = 10, DAMAGE = 1;
   public static final int wallThick = 10;
   public static final Area Maze = new Area();
   public static Point [][] nodes = new Point[XRES/tWidth][YRES/tHeight];
   public World()
   {
      /*for(int i=0;i<=World.XRES;i+=100)
      {
         walls.add(new Wall(i-2,0,4,r.nextInt(World.YRES/5)*5));
      }
      for(int i=0;i<=World.YRES;i+=100)
      {
         walls.add(new Wall(0,i-2,r.nextInt(World.XRES/5)*5,4));
      }*/
      try
      {
         BufferedImage image = ImageIO.read(new File("Maze.png"));
         for (int r = 0; r < image.getHeight(); r ++) 
         {
            for (int c = 0; c < image.getWidth(); c ++) 
            {
               int color = (image.getRGB(c,r)>>24&0xff);
               int start = 0;
               int end = image.getWidth();
               if(color != 0)
               {
                  start = c;
                  while(c<image.getWidth() && color != 0)
                  {
                     end =c;
                     color = (image.getRGB(c,r)>>24&0xff);
                     c++;
                  }
                  Maze.add(new Area(new Rectangle(start,r,end-start,1)));
               }
            }
         }
      }
      catch(IOException e)
      {
         System.out.println("No Maze");
         System.exit(0);
      }
      for(int i =0;i<nodes.length;i++)
      {
         for(int j=0;j<nodes[i].length;j++)
         {
            Point p = new Point(i*tWidth+tWidth/2,j*tHeight+tHeight/2);
            if(Maze.contains(p))
            {
               nodes[i][j] = null;
            }
            else
            {
               nodes[i][j] = p;
            }
         }
      }
      tanks[0] = new Tank(World.XRES,World.YRES,Color.BLUE,"PLAYER 1",new int[]{KeyEvent.VK_UP,KeyEvent.VK_DOWN,KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_SPACE});
      tanks[1] = new Tank(100,100,Color.RED,"PLAYER 2", new int[]{KeyEvent.VK_W,KeyEvent.VK_S,KeyEvent.VK_A,KeyEvent.VK_D,KeyEvent.VK_X});
      
   }
   public static ArrayList<Node> getPath(Node loc, Node end, int step,ArrayList<Node> taken)
   {
      if(loc.iPos == end.iPos && loc.jPos == end.jPos)
      {
         return taken;
      }
      ArrayList<Node> newTaken = taken;
      ArrayList<Node> potentials = getViableAdjacent(loc, step+1, newTaken);
      if(potentials.size()==0)
      {
         System.out.println("No Moves");
         return null;
      }
      for(Node n: potentials)
      {
         newTaken.add(n);
         ArrayList<Node> path = getPath(n,end,n.step+1,newTaken);
         if(path!=null)
         return path;
         newTaken.remove(n);
      }
      System.out.println("No end");
      return null;
   }
   public static ArrayList<Node> getViableAdjacent(Node loc, int step, ArrayList<Node> taken)
   {
      ArrayList<Node> neighbors = new ArrayList<Node>();
      int nX = loc.iPos;
      int nY = loc.jPos;
      for(int i = nX-1;i<nX+2;i++)
      {
         if(i>-1 && i<nodes.length)
         {
            Point p = nodes[i][nY];
            if(p != null && i!=nX)
            {
               Node n = new Node(i,nY,step);
               if(taken.indexOf(n)==-1)
               {
                  neighbors.add(n);
               }
            }
         }
      }
      for(int i = nY-1;i<nY+2;i++)
      {
         if(i>-1 && i<nodes[0].length)
         {
            Point p = nodes[nX][i];
            if(p != null && i!=nY)
            {
               Node n = new Node(nX,i,step);
               if(taken.indexOf(n)==-1)
               {
                  neighbors.add(n);
               }
            }
         }
      }
      return neighbors;
   }
   public static Node getNearestNode(int x, int y)
   {
      int radius = 100;
      int iN = 0;
      int jN = 0;
      for(int i =0;i<nodes.length;i++)
      {
         for(int j=0;j<nodes[i].length;j++)
         {
            Point p = nodes[i][j];
            if(p!=null)
            {
               double rad = Math.sqrt(p.getX()*p.getX()+p.getY()*p.getY());
               if(rad<radius)
               {
                  radius =(int)rad;
                  iN = i;
                  jN = j;
               }
            }
         }
      }
      return new Node(iN,jN,0);
   }
   public static int[] getBounds(int x, int y)
   {
      int lb = 0;
      int rb = World.XRES;
      int tb = 0;
      int bb = World.YRES;
      for(Wall w: walls)
      {
         if(w.yPos <=y && w.yPos+w.height>=y)
         {
            if(w.xPos>x)
            {
               if(w.xPos<rb)
               {
                  rb = w.xPos;
               }
            }
            else
            {
               if(w.xPos>lb)
               {
                  lb = w.xPos;
               }
            }
         }
         if(w.xPos<=x && w.xPos+w.width>=x)
         {
            if(w.yPos>y)
            {
               if(w.yPos<bb)
               {
                  bb = w.yPos;
               }
            }
            else
            {
               if(w.yPos>tb)
               {
                  tb = w.yPos;
               }
            }
         }
      }
      return new int[]{tb,bb,lb,rb};
   }
   public static void paws(int time)
   {
      if(time > 0)
      {
         try 
         {
            Thread.sleep(time);
         } 
         catch(InterruptedException ex)
         {
            Thread.currentThread().interrupt();
         }
      }
   }
}
final class Node
{
   public final int step,iPos,jPos;
   public Node(int s, int i, int j)
   {
      step = s;
      iPos = i;
      jPos = j;
   }
}
final class Wall
{
   public final int xPos, yPos, width, height;
   public Wall(int x, int y, int w, int h)
   {
      xPos = x;
      yPos = y;
      width = w;
      height = h;
   }
}
final class Tank
{
   public int xPos,yPos, angle, life;
   public final Color hue;
   public final String name;
   public final int up, down, left, right, shoot;
   private int prevX,prevY;
   private boolean gotIn;
   public Tank(int x, int y, Color c, String n, int[] keys)
   {
      xPos = x;
      yPos = y;
      prevX=x;
      prevY=y;
      hue = c;
      angle = 0;
      name = n;
      life = World.LIFE;
      up=keys[0];
      down=keys[1];
      left=keys[2];
      right=keys[3];
      shoot=keys[4];
      gotIn = false;
   }
   public double getLifeRatio()
   {
      return (double)life/World.LIFE;
   }
   public void autoMoveTo(int x, int y)
   {
      Node me = World.getNearestNode(xPos,yPos);
      Node end = World.getNearestNode(x,y);
      ArrayList<Node> path = World.getPath(me,end,0,null);
      if(path==null)
      {
         System.out.println("No Path");
      }
      else
      {
         prevX = xPos;
         prevY = yPos;
         Node n = path.get(0);
         Point p = World.nodes[n.iPos][n.jPos];
         if(p.getX()<xPos)
         {
            xPos -= World.tSpeed;
            angle = 180;
         }
         else
         {
            xPos += World.tSpeed;
            angle = 0;
         }
         if(p.getY()<yPos)
         {
            yPos -= World.tSpeed;
            angle = 90;
         }
         else
         {
            yPos += World.tSpeed;
            angle = 270;
         }
         if(gotIn)
         {
            if(World.Maze.intersects(xPos,yPos,World.tWidth,World.tHeight))
            {
               xPos = prevX;
               yPos = prevY;
            }
         }
         else
         {
            if(!World.Maze.intersects(xPos,yPos,World.tWidth,World.tHeight))
            {
               gotIn=true;
            }
         }
      }
   }
   public void move()
   {
      //int[] bounds = World.getBounds(xPos,yPos);
      prevX = xPos;
      prevY = yPos;
      if(World.keyStat[up])
      {
         if(!World.keyStat[down])
         {
            yPos -= World.tSpeed;
            angle = 90;
         }
      }
      else
      {
         if(World.keyStat[down])
         {
            yPos += World.tSpeed;
            angle = 270;
         }
         else
         {
            if(World.keyStat[right])
            {
               if(!World.keyStat[left])
               {
                  xPos += World.tSpeed;
                  angle = 0;
               }
            }
            else
            {
               if(World.keyStat[left])
               {
                  xPos -= World.tSpeed;
                  angle = 180;
               }
            }
         }
      }
      if(gotIn)
      {
         if(World.Maze.intersects(xPos,yPos,World.tWidth,World.tHeight))
         {
            xPos = prevX;
            yPos = prevY;
         }
      }
      else
      {
         if(!World.Maze.intersects(xPos,yPos,World.tWidth,World.tHeight))
         {
            gotIn=true;
         }
      }
      /*if(xPos+World.tWidth > bounds[3])
      {
         xPos = bounds[3]-World.tWidth;
      }
      else if(xPos<bounds[2])
      {
         xPos = bounds[2];
      }
      if(yPos+World.tHeight > bounds[1])
      {
         yPos = bounds[1]-World.tHeight;
      }
      else if(yPos<bounds[0])
      {
         yPos = bounds[0];
      }*/
   }
   public void shoot()
   {
      if(World.keyStat[shoot])
      {
         World.keyStat[shoot] = false;
         World.shells.add(new Shell(xPos+World.tWidth/2,yPos+World.tHeight/2,angle,name, hue));
      }
   }
}
final class Shell
{
   public int xPos,yPos;
   public final int angle;
   public final String shooter;
   public final Color hue;
   public Shell(int x, int y, int dir, String firer, Color c)
   {
      xPos = x;
      yPos = y;
      angle = dir;
      shooter = firer;
      hue = c;
   }
   public void move()
   {
      int[] bounds = World.getBounds(xPos,yPos);
      if(angle == 0 || angle == 360)
      {
         xPos += World.sSpeed;
      }
      else if(angle == 180)
      {
         xPos -= World.sSpeed;
      }
      else if(angle == 90)
      {
         yPos -= World.sSpeed;
      }
      else
      {
         yPos += World.sSpeed;
      }
      if(xPos+World.sWidth > bounds[3] || xPos<bounds[2])
      {
         World.shells.remove(this);
      }
      else if(yPos+World.sHeight > bounds[1] || yPos<bounds[0])
      {
         World.shells.remove(this);
      }
   }
}
final class KeyController implements KeyListener
{
   public KeyController(){/* Nothing */}
   public void keyPressed(KeyEvent key) 
   {
      World.keyStat[key.getKeyCode()] = true;
   }
   public void keyReleased(KeyEvent key) 
   { 
      World.keyStat[key.getKeyCode()] = false;
   }
   public void keyTyped(KeyEvent e){/* Nothing */}
}
final class MouseController implements MouseListener
{
   public MouseController(){/* Nothing */}
   public void mousePressed(MouseEvent button) 
   { 
      World.mouse[button.getButton()] = true;
   }
   public void mouseReleased(MouseEvent button) 
   { 
      World.mouse[button.getButton()] = false; 
   }
   public void mouseExited(MouseEvent e){/* Nothing */}
   public void mouseClicked(MouseEvent e){/* Nothing */}
   public void mouseEntered(MouseEvent e){/* Nothing */}
}   
