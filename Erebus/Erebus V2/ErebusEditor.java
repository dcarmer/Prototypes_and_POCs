/* Window Imports */
import java.awt.Frame; //For Drawing Surface
import java.awt.Container;
import java.awt.Component;


/* Geometry Imports */
import java.awt.Polygon; //NavMeshes
import java.awt.geom.AffineTransform; //For Transformations
import java.awt.Point; //For Dual int Encapsulation
import java.awt.Rectangle; //For Point and Dimension Encapusulation

/* Thread Safe Imports */
import java.util.concurrent.atomic.AtomicBoolean; //Booleans
import java.util.concurrent.atomic.AtomicInteger; //Integers
import java.util.Random; //For Psuedo-Random Decisions
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/* File Imports */
import javax.imageio.ImageIO; //For Loading Images From Files
import java.io.File; //For Loading Files From Paths
import java.io.IOException; //For Catching Unreadable Files
import java.io.FileReader; //Read Files
import java.io.FileWriter; //Write Files
import java.io.BufferedReader; //Buffered Wrapper
import java.io.BufferedWriter; //Buffered Wrapper
import java.io.PrintWriter; //Text Writer

/* Input Handleing Imports */
import java.awt.event.KeyEvent; //For Key Index(s)
import java.awt.event.KeyListener; //For Keyboard Input
import java.awt.event.MouseEvent; //For Mouse_Button Index(s) and Location
import java.awt.event.MouseWheelEvent; //For Scrolling
import java.awt.event.MouseListener; //For Mouse Button Input
import java.awt.event.MouseMotionListener; //For Mouse Motion Input
import java.awt.event.MouseWheelListener; //For Mouse Scroll Input
import java.awt.event.FocusEvent; //For pausing
import java.awt.event.FocusListener; //For Minimizeing

/* Collection Imports */
import java.util.List;//Interface For Generic Collections
import java.util.ArrayList;//For List Implementation
import java.util.Map; //Interface For a Collection Mapped to unique keys
import java.util.EnumMap; //For Enum Combinations
import java.util.concurrent.ConcurrentHashMap; //For ThreadSafe HashMap
import java.util.Deque;//Interface for Queue(FIFO) and Stack(FILO) Collections
import java.util.ArrayDeque; //For Queue/Stack Implimentation

/* Font Imports */
import java.awt.Font; //For Font Control
import java.awt.FontFormatException; //For Catching Font Read Error
import java.awt.FontMetrics; //For Font Positioning

/* Visual Imports */
import java.awt.image.BufferStrategy; //For Double Buffering Screen
import java.awt.Image; //For Drawing Images
import java.awt.image.BufferedImage; //For Manipulating Image Data
import java.awt.Graphics; //For Drawing to Buffer_Strategy
import java.awt.Graphics2D; //For Using Affine_Transform Draws
import java.awt.GraphicsConfiguration; //For Buffered_Image Creation
import java.awt.GraphicsDevice; //For Full Screen Exclusive Mode
import java.awt.GraphicsEnvironment; //For Graphics_Device
import java.awt.Transparency; //For Setting Image Transparancy Type
import java.awt.BasicStroke; //For Drawing lines
import java.awt.image.RescaleOp; //For Image editing
import java.awt.image.ColorConvertOp; //For Image editing
import java.awt.image.BufferedImageOp; //For Image editing
import java.awt.Color; //For Color Manipulation
import java.awt.color.ColorSpace; //For Color Manipulation

/* Sound Imports */
import javax.sound.sampled.AudioSystem; //Get Sound
import javax.sound.sampled.AudioInputStream; //Get Sound
import javax.sound.sampled.LineUnavailableException; //Get Sound Error
import javax.sound.sampled.UnsupportedAudioFileException; //Get Sound Error
import javax.sound.sampled.Clip; //Hold sound 
import javax.sound.sampled.BooleanControl; //Mute
import javax.sound.sampled.FloatControl;  //Volume

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;


public class ErebusEditor
{
   public static final GraphicsConfiguration GC;
   private static final BufferStrategy BS;
   public static final int XRES,YRES;
      public static final AtomicInteger SCROLL = new AtomicInteger(0);
   public static final Frame f;
      public static final AtomicInteger MOUSE_X = new AtomicInteger(0), MOUSE_Y = new AtomicInteger(0);
      public static final AtomicInteger MOVE_X = new AtomicInteger(0), MOVE_Y = new AtomicInteger(0);
     public static final AtomicInteger OVER_X = new AtomicInteger(0), OVER_Y = new AtomicInteger(0);
     public static final AtomicInteger FLOOR_X = new AtomicInteger(0), FLOOR_Y = new AtomicInteger(0);
   static
   {
      /* As Far As I Know (.getLocalGraphicsEnvironment()) Is The Only Way To Get The GraphicsEnvironment */
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      /* Usually Only Have 1 Device, Though Many May Be Present(.getScreenDevices()), Do Not Yet Know Of A Way To Determine Best */
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      /* Usually Only Have 1 Config, Though Many May Be Present(.getConfigurations()), Do Not Yet Know Of A Way To Determine Best */
      GC = gd.getDefaultConfiguration();
   
      Rectangle screenBounds = GC.getBounds();
      XRES = screenBounds.width;
      YRES = screenBounds.height;
   
      /* Frame Setup */
      f = new Frame("Erebus",GC);
      f.setLayout(null);
      EventControllerEditor EC = new EventControllerEditor();
      f.setCursor(f.getToolkit().createCustomCursor(ImageManager.getBuffImg("BattleAxeCursor"),new Point(0,0),"BattleAxe Cursor"));
      f.setIconImage(ImageManager.getBuffImg("ApplicationIcon"));
      f.addKeyListener(EC);
      f.addMouseListener(EC);
      f.addMouseMotionListener(EC);
      f.addMouseWheelListener(EC);
      f.addFocusListener(EC);
      f.setResizable(false);
      f.setUndecorated(true);
      f.setIgnoreRepaint(true);
      /* Sets Font for Graphics based off Frame */
      try
      {
         Font gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("erebusfont.ttf"));
         gameFont = gameFont.deriveFont(12f);
         f.setFont(gameFont);
      }
      catch(FontFormatException|IOException e)
      {
         System.out.println("Error - Font File"); 
      }
      /* Sets Size To FullScreen */
      f.setExtendedState(Frame.MAXIMIZED_BOTH);
      /* Enters into FullScreen Exclusive mode if possible */
      gd.setFullScreenWindow(f);   
   /* Will Create Best Possible Stratagy For Frame */
      f.createBufferStrategy(2);
      BS = f.getBufferStrategy();
   }
   public static Floor floor;
   private ErebusEditor(){}
   public static void main(String[] args)
   {
      do
      {
         try
         {
            FLOOR_X.set(Integer.parseInt(JOptionPane.showInputDialog(null, "Floor Width? (Should be Odd)")));
            FLOOR_Y.set(Integer.parseInt(JOptionPane.showInputDialog(null, "Floor Height? (Should be Odd)")));
            break;
         }
         catch(Exception e)
         {
         
         }
      }while(true);
      floor = new Floor(FLOOR_X.get(),FLOOR_Y.get());
      while(true)
      {
         System.gc();
         visual();
      }
   }
   public static void visual()
   {
      do
      {
         do
         {
            Graphics g = BS.getDrawGraphics();
            /* Insert All Draw Methods Here */
                  g.setColor(Color.GRAY);
      g.fillRect(0,0,XRES,YRES);
      double scrollRatio = SCROLL.get();
      double zoom = (0>scrollRatio?1/Math.abs(scrollRatio):1+scrollRatio);
      AffineTransform at = AffineTransform.getTranslateInstance(XRES/2,YRES/2);
      at.scale(zoom,zoom);
      at.translate(-MOVE_X.get(),-MOVE_Y.get());
      
      ((Graphics2D)g).setTransform(at);
            floor.drawMaze(g);
            floor.drawGrid(g);
            ((Graphics2D)g).setTransform(new AffineTransform());
            g.setColor(Color.WHITE);
            g.drawString("x"+String.valueOf(zoom),100,100);
            g.drawString("x@"+String.valueOf(OVER_X.get())+" y@"+String.valueOf(OVER_Y.get()),100,200);
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }

}
final class Floor
{
   public static final int CELL_SIZE = Cell.minAllowedRes;
   private final Point endPos;
   private final Cell[][] layout;
   private double lastZoom;
   private static final ThreadLocal<Random> rand = new ThreadLocal<Random>();
   private final BufferedImage lastDraw = ImageManager.getBuffImg(ErebusEditor.XRES,ErebusEditor.YRES,Transparency.BITMASK);
   public Floor(int x, int y)
   {
      rand.set(new Random(0));
      layout = new Cell[x][y];
      //layout = new Cell[1+2*ThreadLocalRandom.current().nextInt(40)+12][1+2*ThreadLocalRandom.current().nextInt(40)+12];
      endPos = new Point(1,1);
      floorGeneration();
   }
   /*
   * Assumes layout is filled with null.
   * Starts at the endPos and connects all reachable Tiles.
   */
   private void floorGeneration()
   {
      final Deque<Point> stack = new ArrayDeque<Point>();
      Point current = endPos;
      layout[current.x][current.y] = Cell.getCell(TileType.PATH);
      while(true)
      {
         final List<Direction> unvisited = getNullNodeNeighbors(current);
         if(!unvisited.isEmpty())//not at dead end
         {
            if(unvisited.size()>1)//branching tile
            {
               stack.push(new Point(current));
            }
            final Direction selection = unvisited.get(rand.get().nextInt(unvisited.size()));
            layout[current.x][current.y] = layout[current.x][current.y].changeCellLink(selection,true);
            final Point s = selection.getVector();
            final int csX = current.x+s.x;
            final int csY = current.y+s.y;
            layout[csX][csY] = Cell.getCell(TileType.PATH).changeCellLink(selection,true).changeCellLink(selection.getOpposite(),true);
            current.setLocation(csX+s.x,csY+s.y);
            layout[current.x][current.y] = Cell.getCell(TileType.PATH).changeCellLink(selection.getOpposite(),true);
         }
         else if(!stack.isEmpty())//backtrack
         {
            current = stack.pop();
         }
         else //no more accesable nodes
         {
            break;
         }         
      }
      for(int i=0;i<layout.length;i++)
      {
         for(int j=0;j<layout[0].length;j++)
         {
            if(layout[i][j]==null)
            {
               List<Direction> adjWalls = getWallNeighbors(new Point(i,j));
               layout[i][j] = Cell.getCell(TileType.WALL);
               for(Direction dir: adjWalls)
               {
                  layout[i][j] = layout[i][j].changeCellLink(dir,true);
               }
            }
         }
      }
   }
   /*
   * Takes in a Point within layout bounds.
   * Detects cardinally adjacent Node Cells that are null.
   * Returns a list of Directions from p to Cell
   */
   private List<Direction> getNullNodeNeighbors(final Point p)
   {
      final List<Direction> dirs = new ArrayList<Direction>(4);
      for(Direction d: Direction.values())
      {
         final Point n = d.getVector();
         final int pnX = p.x+n.x*2;
         final int pnY = p.y+n.y*2;
         if((0 <= pnY && pnY < layout[0].length) && (0 <= pnX && pnX < layout.length) && (layout[pnX][pnY]==null))
         {
            dirs.add(d);
         }
      }
      return dirs;
   }
   /*
   * Takes in a Point within layout bounds.
   * Detects cardinally adjacent Cells that are null or Walls.
   * Returns a list Directions from p to Cell
   */
   private List<Direction> getWallNeighbors(final Point p)
   {
      final List<Direction> dirs = new ArrayList<Direction>(4);
      for(Direction d: Direction.values())
      {
         final Point n = d.getVector();
         final int pnX = p.x+n.x;
         final int pnY = p.y+n.y;
         if((0 <= pnY && pnY < layout[0].length) && (0 <= pnX && pnX < layout.length) && (layout[pnX][pnY]==null || layout[pnX][pnY].getTT()==TileType.WALL))
         {
            dirs.add(d);
         }
      }
      return dirs;
   }
   /*
   * Draws onto MAZE each Cell's Image int the appropriate location
   */
   public void drawMaze(final Graphics g)
   {
      for(int i=0;i<layout.length;i++)
      {
         for(int j=0;j<layout[0].length;j++)
         {
            g.drawImage(layout[i][j].getVisual(),i*CELL_SIZE,j*CELL_SIZE,null);
         }
      }
   }
   public void toggleTile(int x,int y)
   {
      if(x<0||y<0)
      {
         return;
      }
      if(layout[x][y].tileType==TileType.WALL)
      {
         layout[x][y] = Cell.getCell(TileType.PATH,layout[x][y].linkType,layout[x][y].direction);
      }
      else
      {
         layout[x][y] = Cell.getCell(TileType.WALL,layout[x][y].linkType,layout[x][y].direction);
      }
   }
   public void drawGrid(final Graphics g)
   {
      g.setColor(Color.WHITE);
      ((Graphics2D)g).setStroke(new BasicStroke((float)(1/((Graphics2D)g).getTransform().getScaleX()),BasicStroke.JOIN_MITER,BasicStroke.CAP_BUTT));
      for(int i=0;i<layout.length;i++)
      {
         for(int j=0;j<layout[0].length;j++)
         {
            g.drawRect(i*CELL_SIZE,j*CELL_SIZE,CELL_SIZE,CELL_SIZE);
         }
      }
   }
   /*
   * Enum Containing The Type of Cells That can be created.
   * Represents data on core functionality of Cell
   */
   private static enum TileType
   {
      WALL,PATH;
   }
   /*
   * Object Representing Tesalating Pieces of Full Floor
   * Represents all possible combinations of TileType, LinkType, and Direction Enums(Excluding Trivial Directions)
   */
   private static final class Cell
   {
      /*
      * Enum Containing the Type of Linkage a Cell May Have
      * Represents all Possible Combinations of Links.(Excluding Order Shifting)
      */
      private static enum LinkType
      {
         NONE(false,false,false,false),
         END(true,false,false,false),
         STRAIGHT(true,false,true,false),
         CORNER(true,true,false,false),
         TEE(true,true,false,true),
         INTERSECTION(true,true,true,true);
         
         private final boolean [] links;
         private LinkType(final boolean n,final boolean e,final boolean s,final boolean w)
         {
            links=new boolean[]{n,e,s,w};
         }
         private boolean[] getLinkArray(final Direction d)
         {
            boolean [] rotated = new boolean[4];
            int shift = d.ordinal();
            for(int i=0;i<4;i++)
            {
               rotated[(i+shift)%4]=links[i];
            }
            return rotated;
         }
      }
      /* The largest resolution of the cell images, to which all other cell images are scaled */
      private final static int minAllowedRes;
      /* A EnumMap of EnumMaps, Holding All permutations with the corresponding new Cell at the bottom*/
      private final static Map<TileType,Map<LinkType,Map<Direction,Cell>>> EnumTree;
      static
      {
         int tempAllowedRes = 0;
         EnumTree = new EnumMap<TileType,Map<LinkType,Map<Direction,Cell>>>(TileType.class);
         Map<TileType,Map<LinkType,BufferedImage>> CellImgs = new EnumMap<TileType,Map<LinkType,BufferedImage>>(TileType.class);
         for(TileType tt: TileType.values())
         {  
            Map<LinkType,Map<Direction,Cell>> LinkMap = new EnumMap<LinkType,Map<Direction,Cell>>(LinkType.class);
            Map<LinkType,BufferedImage> ImgMap = new EnumMap<LinkType,BufferedImage>(LinkType.class);
            for(LinkType lt: LinkType.values())
            {
               final BufferedImage bi = ImageManager.getBuffImg(tt.name()+"_"+lt.name(),Transparency.OPAQUE);
               tempAllowedRes=Math.max(tempAllowedRes,bi.getWidth());tempAllowedRes=Math.max(tempAllowedRes,bi.getHeight());
               
               Map<Direction,Cell> DirectionMap = new EnumMap<Direction,Cell>(Direction.class);
               for(Direction d: Direction.values())
               {
                  DirectionMap.put(d,null);
               }
               ImgMap.put(lt,bi);
               LinkMap.put(lt,DirectionMap);
            }
            CellImgs.put(tt,ImgMap);
            EnumTree.put(tt,LinkMap);
         }
         minAllowedRes = tempAllowedRes;
         
         for(TileType tt: TileType.values())
         {  
            for(LinkType lt: LinkType.values())
            {
               final BufferedImage bi = ImageManager.getResizedBuffImg(CellImgs.get(tt).get(lt),minAllowedRes,minAllowedRes);
               if(lt==LinkType.NONE || lt==LinkType.INTERSECTION)
               {
                  final Cell trivialRotation = new Cell(tt,lt,Direction.NORTH,bi);
                  for(Direction d: Direction.values())
                  {
                     EnumTree.get(tt).get(lt).put(d,trivialRotation);
                  }
               }
               else if(lt==LinkType.STRAIGHT)
               {
                  final Cell vert =  new Cell(tt,lt,Direction.NORTH,bi);
                  final Cell horz =  new Cell(tt,lt,Direction.EAST,ImageManager.getRotatedBuffImg(bi,Direction.EAST.getRadians()));
                  EnumTree.get(tt).get(lt).put(Direction.NORTH,vert);EnumTree.get(tt).get(lt).put(Direction.SOUTH,vert);
                  EnumTree.get(tt).get(lt).put(Direction.EAST,horz);EnumTree.get(tt).get(lt).put(Direction.WEST,horz);
               }
               else
               {
                  for(Direction d: Direction.values())
                  {
                     EnumTree.get(tt).get(lt).put(d,new Cell(tt,lt,d,d==Direction.NORTH?bi:ImageManager.getRotatedBuffImg(bi,d.getRadians())));
                  }
               }
            }
         }
         
      }
      
      private final TileType tileType;
      private final LinkType linkType;
      private final Direction direction;
      /* Used to determine the current Links without resorting to a switch(---SUBJECT TO CHANGE---)*/
      private final boolean[] linkMarkers;
      /* The Unique Image Associated with each Cell */
      private final BufferedImage visual;
      private Cell(final TileType tt,final LinkType lt,final Direction dir,final BufferedImage bi)
      {
         /* 
         * The Cell Class uses A Factory Like Pattern and generates all unique instances upon load.
         * However even though its constructor is private, because it is an inner static class, it is possible to externally create new Cells.
         * In order to detect this, the constructor checks to see if this combination has already been created.
         * If it is determined that the call was external than an Exception will be generated and printed.
         * Note however that the exception will not be thrown and a Cell will be successfully instantiated. (---SUBJECT TO CHANGE---)
         */
         if(Cell.getCell(tt,lt,dir)!=null)
         {
            Exception e = new Exception("Illegal Creation of Cell");
            System.err.print("Exception in thread \""+Thread.currentThread().getName()+"\" ");
            e.printStackTrace();
         }
         tileType=tt; linkType=lt; direction=dir;visual=bi;
         linkMarkers = lt.getLinkArray(dir);
      }
      private TileType getTT()
      {
         return tileType;
      }
      private BufferedImage getVisual()
      {
         return visual;
      }
      private static Cell getCell(final TileType tt)
      {
         return getCell(tt,LinkType.NONE,Direction.NORTH);
      }
      private static Cell getCell(final TileType tt,final LinkType lt,final Direction dir)
      {
         return EnumTree.get(tt).get(lt).get(dir);
      }
      /*
      * Given the supplied Direction and boolean,
      * A refrence to the Cell object representing this one with the alterd values is returned.
      */
      private Cell changeCellLink(final Direction d,final boolean val)
      {
         if(linkMarkers[d.ordinal()]!=val)
         {
            if(val)
            {
               switch(linkType)
               {
                  case NONE:
                     return getCell(tileType,LinkType.END,d);
                  case END:
                     if(direction.isOpposite(d))
                     {
                        return getCell(tileType,LinkType.STRAIGHT,d);
                     }
                     else
                     {
                        if(direction.isPrev(d))
                        {
                           return getCell(tileType,LinkType.CORNER,d);
                        }
                        return getCell(tileType,LinkType.CORNER,direction);
                     }
                  case STRAIGHT:
                     return getCell(tileType,LinkType.TEE,d);
                  case CORNER:
                     if(direction.isOpposite(d))
                     {
                        return getCell(tileType,LinkType.TEE,direction.getNext());
                     }
                     return getCell(tileType,LinkType.TEE,direction);
                  case TEE:
                     return getCell(tileType,LinkType.INTERSECTION,d);
               }
            }
            else
            {
               switch(linkType)
               {
                  case END:
                     return getCell(tileType,LinkType.NONE,d);
                  case STRAIGHT:
                     return getCell(tileType,LinkType.END,d.getOpposite());
                  case CORNER:
                     if(direction == d)
                     {
                        return getCell(tileType,LinkType.END,direction.getNext());
                     }
                     return getCell(tileType,LinkType.END,direction);
                  case TEE:
                     if(direction == d)
                     {
                        return getCell(tileType,LinkType.STRAIGHT,direction.getNext());
                     }
                     else
                     {
                        final Direction prev = direction.getPrev();
                        if(prev != d)
                        {
                           return getCell(tileType,LinkType.CORNER,prev);
                        }
                        return getCell(tileType,LinkType.CORNER,direction);
                     }
                  case INTERSECTION:
                     return getCell(tileType,LinkType.TEE,d.getOpposite());
               }
            }
         }
         return this;
      }
   }
}
enum Direction
{
   NORTH,EAST,SOUTH,WEST;
   private static final Direction[] CARDINAL = Direction.values();
   private final double radian;
   private final int vx,vy;
   private Direction()
   {
      final int pos = this.ordinal();
      final double halfPI = Math.PI/2;
      radian = halfPI*pos;
      final double theta = (pos-1)*halfPI;
      vx = (int)Math.cos(theta);
      vy = (int)Math.sin(theta);
   }
   public boolean isOpposite(final Direction other)
   {
      return (this.ordinal()+2)%4==other.ordinal();
   }
   public Direction getOpposite()
   {
      return CARDINAL[(this.ordinal()+2)%4];
   }
   public boolean isNext(final Direction other)
   {
      return (this.ordinal()+1)%4==other.ordinal();
   }
   public Direction getNext()
   {
      return CARDINAL[(this.ordinal()+1)%4];
   }
   public boolean isPrev(final Direction other)
   {
      return (this.ordinal()+3)%4==other.ordinal();
   }
   public Direction getPrev()
   {
      return CARDINAL[(this.ordinal()+3)%4];
   }
   public Point getVector()
   {
      return new Point(vx,vy);
   }
   public double getRadians()
   {
      return radian;
   }
}

final class ImageManager
{
   private final static String[] extensions = new String[]{".jpg",".png"};
   private final static GraphicsConfiguration GC = ErebusEditor.GC;
   private ImageManager(){}
   public static Color getInvertColor(final Color oldColor)
   {
      return new Color(oldColor.getRed()*-1+255,oldColor.getGreen()*-1+255,oldColor.getBlue()*-1+255);
   }
   public static BufferedImage getBuffImg(int w,int h,int t)
   {
      return GC.createCompatibleImage(w,h,t);
   }
   public static BufferedImage getBuffImg(final String path)
   {
      for(String ext: extensions)
      {
         final File file = new File(path+ext);
         try
         {
            final BufferedImage img  = ImageIO.read(file);
            final BufferedImage buffImg = GC.createCompatibleImage(img.getWidth(null),img.getHeight(null),img.getTransparency());
            final Graphics2D g2D = buffImg.createGraphics();
            g2D.drawImage(img,0,0,null);
            g2D.dispose();
            return buffImg;
         }
         catch(IOException e){}
      }
      System.out.println("Error Loading Image: "+path);
      return GC.createCompatibleImage(1,1,Transparency.OPAQUE);
   }
   public static BufferedImage getBuffImg(final String path,final int t)
   {
      for(String ext: extensions)
      {
         final File file = new File(path+ext);
         try
         {
            final BufferedImage img  = ImageIO.read(file);
            final BufferedImage buffImg = GC.createCompatibleImage(img.getWidth(null),img.getHeight(null),t);
            final Graphics2D g2D = buffImg.createGraphics();
            g2D.drawImage(img,0,0,null);
            g2D.dispose();
            return buffImg;
         }
         catch(IOException e){}
      }
      System.out.println("Error Loading Image: "+path);
      return GC.createCompatibleImage(1,1,t);
   }
   public static BufferedImage getResizedBuffImg(final BufferedImage oldImg,final int w,final int h)
   {
      final int width = oldImg.getWidth();
      final int height = oldImg.getHeight();
      if(width == w && height == h)
      {
         return oldImg;
      }
      final BufferedImage buffImg = GC.createCompatibleImage(w,h,oldImg.getTransparency());
      final Image img = oldImg.getScaledInstance(w,h,Image.SCALE_DEFAULT);
      Graphics2D g2D = buffImg.createGraphics();
      g2D.drawImage(img,0,0,null);
      g2D.dispose();
      return buffImg;
   }
   public static BufferedImage getRotatedBuffImg(final BufferedImage oldImg,final double theta)
   {
      return getBuffImg(oldImg,1,1,theta);
   }
   public static BufferedImage getScaledBuffImg(final BufferedImage oldImg,final double sCX,final double sCY)
   {
      return getBuffImg(oldImg,sCX,sCY,0);
   }
   public static BufferedImage getBuffImg(final BufferedImage oldImg,final double sCX,final double sCY,final double theta)
   {
      final double scaleX = sCX*Math.cos(theta), scaleY = sCY*Math.cos(theta);
      final double shearX = -sCX*Math.sin(theta), shearY = sCY*Math.sin(theta);
      final double width = oldImg.getWidth(), height = oldImg.getHeight();
      
      final double scaledW = width*scaleX, scaledH = height*scaleY;
      final double shearedW = width*shearY, shearedH = height*shearX;
      
      final double possW1 = Math.abs(scaledW-shearedH), possW2 = Math.abs(scaledW+shearedH);
      final double possH1 = Math.abs(shearedW-scaledH), possH2 = Math.abs(shearedW+scaledH);
      
      final double newW = Math.max(possW1,possW2), newH = Math.max(possH1,possH2);
      
      final double minX = Math.min(Math.min(scaledW,shearedH),Math.min(0,scaledW+shearedH));
      final double minY = Math.min(Math.min(shearedW,scaledH),Math.min(0,shearedW+scaledH));
      
      final AffineTransform at = new AffineTransform(scaleX,shearY,shearX,scaleY,-minX,-minY);
      final BufferedImage buffImg = GC.createCompatibleImage((int)newW,(int)newH,oldImg.getTransparency());
      Graphics2D g2D = buffImg.createGraphics();
      g2D.drawImage(oldImg,at,null);
      g2D.dispose();
      return buffImg;
   }
   public static BufferedImage getInvertBuffImg(final BufferedImage oldImg)
   {
      final BufferedImageOp invertOp = new RescaleOp(new float[]{-1f,-1f,-1f,1f},new float[]{255f,255f,255f,0f}, null);
      return invertOp.filter(oldImg,null);
   }
   public static BufferedImage getGrayScaleBuffImg(final BufferedImage oldImg)
   {
      final BufferedImageOp grayScaleOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
      return grayScaleOp.filter(oldImg,null);
   }
}


/* NOTE: All Listeners are run in the same thread, different from the Main Thread */
final class EventControllerEditor implements KeyListener,MouseListener,MouseWheelListener,MouseMotionListener,FocusListener
{
   public void keyPressed(final KeyEvent EVENT) 
   {
      int keyCode = EVENT.getKeyCode();
      if(keyCode==KeyEvent.VK_ESCAPE)
      {
         System.exit(0);
      }
      if(keyCode==KeyEvent.VK_W)
      {
         ErebusEditor.MOVE_Y.getAndAdd(-5);
      }
      if(keyCode==KeyEvent.VK_S)
      {
         ErebusEditor.MOVE_Y.getAndAdd(5);
      }
      if(keyCode==KeyEvent.VK_A)
      {
         ErebusEditor.MOVE_X.getAndAdd(-5);
      }
      if(keyCode==KeyEvent.VK_D)
      {
         ErebusEditor.MOVE_X.getAndAdd(5);
      }
   }
   public void keyReleased(final KeyEvent EVENT) 
   { 
   }
   public void mousePressed(final MouseEvent EVENT) 
   {
   }
   public void mouseReleased(final MouseEvent EVENT) 
   { 
   }
   public void mouseWheelMoved(final MouseWheelEvent EVENT)
   {
      ErebusEditor.SCROLL.getAndAdd(EVENT.getWheelRotation());
   }
   public void focusLost(final FocusEvent EVENT)
   {
   }
   public void mouseMoved(final MouseEvent EVENT)
   {
      ErebusEditor.MOUSE_X.set(EVENT.getX());
      ErebusEditor.MOUSE_Y.set(EVENT.getY());
      double scrollRatio = ErebusEditor.SCROLL.get();
      double zoom = (0>scrollRatio?1/Math.abs(scrollRatio):1+scrollRatio);
      double x = ((ErebusEditor.MOUSE_X.get()-ErebusEditor.XRES/2+zoom*ErebusEditor.MOVE_X.get())/zoom/Floor.CELL_SIZE);
      double y = ((ErebusEditor.MOUSE_Y.get()-ErebusEditor.YRES/2+zoom*ErebusEditor.MOVE_Y.get())/zoom/Floor.CELL_SIZE);
      if(x<0 || x>ErebusEditor.FLOOR_X.get() || y<0 || y>ErebusEditor.FLOOR_Y.get())
      {
         x=y=-1;
      }
      ErebusEditor.OVER_X.set((int)x);
      ErebusEditor.OVER_Y.set((int)y);
   }
   public void mouseDragged(final MouseEvent EVENT)
   {
      ErebusEditor.MOUSE_X.set(EVENT.getX());
      ErebusEditor.MOUSE_Y.set(EVENT.getY());
   }
   public void focusGained(final FocusEvent e){}
   public void keyTyped(final KeyEvent e){}
   public void mouseClicked(final MouseEvent e)
   {
      ErebusEditor.floor.toggleTile(ErebusEditor.OVER_X.get(),ErebusEditor.OVER_Y.get());
   }
   public void mouseEntered(final MouseEvent e){}
   public void mouseExited(final MouseEvent e){}
}