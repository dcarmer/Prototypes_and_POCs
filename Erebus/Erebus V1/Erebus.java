/* 
NOTE 
.swing is supposedly preferable to .awt (Think I prefer inverse)
volitile should be used for a field with one write thread and one or more read-only threads
atomic's should be used for a feild with more than one write thread
*/
import java.awt.Color; //For Color Manipulation
import java.awt.Frame; //For Drawing Surface
import java.awt.Graphics; //For Drawing to Buffer_Strategy
import java.awt.Graphics2D; //For Using Affine_Transform Draws
import java.awt.GraphicsConfiguration; //For Buffered_Image Creation
import java.awt.GraphicsDevice; //For Full Screen Exclusive Mode
import java.awt.GraphicsEnvironment; //For Graphics_Device
import java.awt.Point; //For Dual int Encapsulation
import java.awt.Transparency; //For Setting Image Transparancy Type
import java.awt.geom.AffineTransform; //For Scale/Translate of Images
import java.awt.image.BufferStrategy; //For Double Buffering Screen
import java.awt.Image; //For Drawing Images
import java.awt.image.BufferedImage; //For Manipulating Image Data
import java.util.Random; //For Psuedo-Random Decisions
import javax.imageio.ImageIO; //For Loading Images From Files
import java.io.File; //For Loading Files From Paths
import java.io.IOException; //For Catching Unreadable Files

/* Input Handleing Imports */
import java.awt.event.KeyEvent; //For Key Index(s)
import java.awt.event.KeyListener; //For Keyboard Input
import java.awt.event.MouseEvent; //For Mouse_Button Index(s)
import java.awt.event.MouseListener; //For Mouse Input
import java.awt.MouseInfo; //For Pointer Location

/* Collection Imports */
import java.util.Arrays; //For Deep Copy of []s and checking [] equality 
import java.util.List;//Interface For Generic Collections
import java.util.ArrayList;//For List Implementation
import java.util.Map; //Interface For a Collection Mapped to unique keys
import java.util.HashMap; //For Mapping Keyboard Keys' States
import java.util.Set;//Interface For a Collection with Unigue Elements
import java.util.HashSet;//For .contains() Optimization
import java.util.Deque;//Interface for Queue(FIFO) and Stack(FILO) Collections
import java.util.ArrayDeque; //For Queue/Stack Implimentation

/* Font Imports */
import java.awt.Font; //For Font Control
import java.awt.FontFormatException; //For Catching Font Read Error
import java.awt.FontMetrics; //For Font Positioning

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Scanner;



public final class Erebus
{
/*** For Generic Use ***/
   private static final Random RAND = new Random();

/*** For Graphical Hardware Properties ***/
   public static final GraphicsConfiguration GC;
   public static final int XRES, YRES;
   private static final BufferStrategy BS;
   static
   {
   /* As Far As I Know (.getLocalGraphicsEnvironment()) Is The Only Way To Get The GraphicsEnvironment */
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
   /* Usually Only Have 1 Device, Though Many May Be Present(.getScreenDevices()), Do Not Yet Know Of A Way To Determine Best */
      GraphicsDevice gd = ge.getDefaultScreenDevice();
   /* Usually Only Have 1 Config, Though Many May Be Present(.getConfigurations()), Do Not Yet Know Of A Way To Determine Best */
      GC = gd.getDefaultConfiguration();
   
   /* Resolution Setup */
      Rectangle screenBounds = GC.getBounds();
      XRES = screenBounds.width;
      YRES = screenBounds.height;
   
   /* Font Setup */
      Font gameFont = null;
      try
      {
         gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("erebusfont.ttf"));
      }
      catch(FontFormatException|IOException e)
      {
         System.out.println("Font Error"); 
         System.exit(0);
      }
      gameFont = gameFont.deriveFont(12f);
   
   /* Frame Setup */
      Frame f = new Frame("Erebus",GC);
      f.addKeyListener(new KeyController());
      f.addMouseListener(new MouseController());
      f.setResizable(false);
      f.setUndecorated(true);
      f.setIgnoreRepaint(true);
      f.setFont(gameFont);
      if(gd.isFullScreenSupported())
      {
         gd.setFullScreenWindow(f);
      /* A WHOLE BUNCH OF DISPLAY MODE SETTINGS CAN BE APPLIED HERE */
         System.out.println("Exclusive FullScreen Mode");
      }
      else
      {
         f.setExtendedState(Frame.MAXIMIZED_BOTH);
         System.out.println("Windowed FullScreen Mode");
         f.setVisible(true);
      }
   /* Will Create Best Possible Stratagy For Frame */
      f.createBufferStrategy(2);
      BS = f.getBufferStrategy();
   }

/*** For Input Handleing ***/
   public static final Map<Integer,Boolean> MOUSE_STAT = new HashMap<Integer,Boolean>();
   public static final Map<Integer,Boolean> KEY_STAT = new HashMap<Integer,Boolean>();
   public static final int 
   MOVE_UP = KeyEvent.VK_W, /* Move Player Up */
   MOVE_DOWN = KeyEvent.VK_S, /* Move Player Down */
   MOVE_LEFT = KeyEvent.VK_A, /* Move Player Left */
   MOVE_RIGHT = KeyEvent.VK_D, /* Move Player Right */
   ESCAPE = KeyEvent.VK_ESCAPE, /* Open Menu */
   LEFT_CLICK = MouseEvent.BUTTON1; /* A Left Click */
   static
   {
   /* Set all keys to false */
      KEY_STAT.put(MOVE_UP,false);
      KEY_STAT.put(MOVE_DOWN,false);
      KEY_STAT.put(MOVE_LEFT,false);
      KEY_STAT.put(MOVE_RIGHT,false);
      KEY_STAT.put(ESCAPE,false);
   /* Set all buttons to false */
      MOUSE_STAT.put(LEFT_CLICK,false);
   }

/* For Output Handleing */
   private static final boolean GRID = true, NUMBERS = true, HUD=true, optiLine = true, fogOn = true;
   public static final int PIXEL_WIDTH = 16, PIXEL_HEIGHT = 16, TILE_WIDTH = PIXEL_WIDTH*4, TILE_HEIGHT = PIXEL_HEIGHT*4;

/* For BufferedImages */
   private static BufferedImage floorImage;
   private static final BufferedImage GOLD_BUTTON = GC.createCompatibleImage(256,32,Transparency.OPAQUE);
   private static final BufferedImage SILVER_BUTTON = GC.createCompatibleImage(256,32,Transparency.OPAQUE);
   private static final BufferedImage DARK_BUTTON = GC.createCompatibleImage(256,32,Transparency.OPAQUE);
   
   private static final BufferedImage BACKGROUND = GC.createCompatibleImage(XRES,YRES,Transparency.OPAQUE);
   private static final BufferedImage MENUBACKGROUND = GC.createCompatibleImage(XRES,YRES,Transparency.BITMASK);
   private static final BufferedImage OPTI_LINE_IMAGE = GC.createCompatibleImage(TILE_WIDTH,TILE_HEIGHT,Transparency.OPAQUE);
   private static final BufferedImage FRAMEIMAGE = GC.createCompatibleImage(XRES,YRES,Transparency.BITMASK);
   private static final BufferedImage MINIMAPFRAME = GC.createCompatibleImage(XRES/4,YRES/4,Transparency.BITMASK);
   private static final BufferedImage PLAYERSTATFRAME = GC.createCompatibleImage(XRES/8,YRES/24,Transparency.BITMASK);
   static
   {
      try
      {
         Image pngBar  = ImageIO.read(new File("Frame_Bar_Up.png"   )).getScaledInstance(16,16,Image.SCALE_DEFAULT);
         Image pngCorn = ImageIO.read(new File("Frame_Corner_NW.png")).getScaledInstance(16,16,Image.SCALE_DEFAULT);
      
         Image goldB   = ImageIO.read(new File("Gold_Button.png"  )).getScaledInstance(GOLD_BUTTON.getWidth(),GOLD_BUTTON.getHeight(),Image.SCALE_DEFAULT);
         Image silverB = ImageIO.read(new File("Silver_Button.png")).getScaledInstance(SILVER_BUTTON.getWidth(),SILVER_BUTTON.getHeight(),Image.SCALE_DEFAULT);
         Image darkB   = ImageIO.read(new File("Dark_Button.png"  )).getScaledInstance(DARK_BUTTON.getWidth(),DARK_BUTTON.getHeight(),Image.SCALE_DEFAULT);
      
         Image back = ImageIO.read(new File("BackGround.png"  )).getScaledInstance(128,128,Image.SCALE_DEFAULT);
      
         Graphics2D g;
         AffineTransform at = new AffineTransform();
         int barW = pngBar.getWidth(null);
         int barH = pngBar.getHeight(null);
         int cornW = pngCorn.getWidth(null);
         int cornH = pngCorn.getHeight(null);
         int frameW,frameH;
         for(int i=0;i<3;i++)
         {
            switch(i)
            {
               case 0:
                  g = FRAMEIMAGE.createGraphics();
                  frameW = FRAMEIMAGE.getWidth();
                  frameH = FRAMEIMAGE.getHeight();
                  break;
               case 1:
                  g = MINIMAPFRAME.createGraphics();
                  frameW = MINIMAPFRAME.getWidth();
                  frameH = MINIMAPFRAME.getHeight();
                  break;
               default:
                  g = PLAYERSTATFRAME.createGraphics();
                  frameW = PLAYERSTATFRAME.getWidth();
                  frameH = PLAYERSTATFRAME.getHeight();
            }
            at.setToScale(frameW/barW,1);
            g.drawImage(pngBar,at,null);
            at.translate(0,frameH-barH);
            at.quadrantRotate(2,barW/2,barH/2);
            g.drawImage(pngBar,at,null);
            at.setToScale(1,frameH/barH);
            at.quadrantRotate(3,barW/2,barH/2);
            g.drawImage(pngBar,at,null);
            at.setToScale(1,frameH/barH);
            at.translate(frameW-barW,0);
            at.quadrantRotate(1,barW/2,barH/2);
            g.drawImage(pngBar,at,null);
            g.drawImage(pngCorn,0,0,null);
            at.setToTranslation(frameW-cornW,0);
            at.quadrantRotate(1,cornW/2,cornH/2);
            g.drawImage(pngCorn,at,null);
            at.setToTranslation(frameW-cornW,frameH-cornH);
            at.quadrantRotate(2,cornW/2,cornH/2);
            g.drawImage(pngCorn,at,null);
            at.setToTranslation(0,frameH-cornH);
            at.quadrantRotate(3,cornW/2,cornH/2);
            g.drawImage(pngCorn,at,null);
         }
         
         g = OPTI_LINE_IMAGE.createGraphics();
         g.setColor(new Color(0,100,0));
         g.fillRect(0,0,OPTI_LINE_IMAGE.getWidth(),OPTI_LINE_IMAGE.getHeight());
      
         g = GOLD_BUTTON.createGraphics();
         g.drawImage(goldB,0,0,null);
         g = SILVER_BUTTON.createGraphics();
         g.drawImage(silverB,0,0,null);
         g = DARK_BUTTON.createGraphics();
         g.drawImage(darkB,0,0,null);
         
         g = BACKGROUND.createGraphics();
         for(int i=0;i<XRES;i+=back.getWidth(null)-1)/***********************/
         {
            for(int j=0;j<YRES;j+=back.getHeight(null))
            {
               g.drawImage(back,i,j,null);
            }
         }
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Frame Image File");
         System.exit(0);
      }
   }

/* For Game Variables */
   private static final List<daemon> GUPH = new ArrayList<daemon>();
   private static final vardoger PLAYER = new vardoger();
   private static final int floorWidth=XRES/64,floorHeight=YRES/64;
   public static tile floor[][];
   public static int distToPlayer[][];
   public static int fog[][];
   private static ThreeNum exitPoint;
   private static int floorNum = 1;
   private static int nullFloorSpacesRemaining; //NEED TO MAKE LOCAL VAR INSTEAD
   private static boolean floorClear; //IF CURRENT FLOOR CLEARED

/* For Multi-Thread Synching */   
   private static final Object PLAYER_LOCK = new Object();
   private static final Object PATH_LOCK = new Object();
   private static final Object MENU_LOCK = new Object();
   private static volatile boolean menuUp = false;

/* For Menu Tree */
   private static boolean configMenu = false;
   private static boolean volumeMenu = false;

   public static final Set<Point> WALLS = new HashSet<Point>();
   public static final int CENTER_X = (XRES-PLAYER.width)/2, CENTER_Y = (YRES-PLAYER.height)/2;
   private static final Color MENU_MASK = new Color(0,0,0,150);

   private Erebus(){}
/* NOTES - TO WORK ON:
-Put assumptions in assert/try statements
-Implement Combat Mechanic
-Implement Fog of War Mechanic
-Implement Boss Floors
-Implement Shop Keep Floors
-Implement Sound
-Implement Speed Control
-Implement Enemy/Player Spawn + Floor Size/Room Number Algorithms
-Implement Zoom via Scroll
-Implement Custom Key Bindings
-Implement Items

?-Thread Synch

(1)Rework Anima Parolling AI
(4)Improve Mouse Handling
(5)Improve Room generation
-Improve Binary Tree
(6)ReWork How Movement Works
-Allow diagonal moves
-ReWork all daemon movement
(10)ReWork Maze Generation for dead ends and multi-paths
(12)Flesh Out HUD
(13)Move Logic out of Graphics thread
(14)Flesh Out Load Screen
(15)Flseh Out Menu Screen
(16)Implement Buffer Capabilities
(17)Fix how menu background is created

make minimap fog show up
make minimap aspect ratio stay
fix fog algorithm for rooms/updates
fix file floor creation
*/
   public static Point getCenterOffset()/**/
   {
      return new Point(CENTER_X-PLAYER.getX(),CENTER_Y-PLAYER.getY());
   }
   public static void updateDistToPlayer()/**/
   {/* Assumes every tile can be reached */
      int capacity = floor.length*floor[0].length;
   /* HashSet 
   - Used to verify if tile has been assigned already(.contains())
   - To prevent costly rehashing, capacity is set to maxTileNumber*defaultLoadFactor(0.75) */
      Set<Point> checkedSet = new HashSet<Point>((int)Math.ceil(capacity*0.75));
   /* ArrayDeque
   -Used as queue to store potentially assigned tiles
   -To Prevent costly resizeing, capacity is set to maxTileNumber */ 
      Deque<Point> queue = new ArrayDeque<Point>(capacity);
      Point playerPt = PLAYER.getTile();
      queue.add(playerPt);
      checkedSet.add(playerPt);
      distToPlayer[playerPt.x][playerPt.y] = 0;
      while(0<queue.size())
      {
      /* Returns and Removes Head of Queue - .poll() */
         Point qPt = queue.poll();
         List<Point> potentials = getPathableTiles(qPt);
         for(Point maybe: potentials)
         {
         /* If Set .contains() Obj Return false Else Add Obj to Set - .add() */
            if(checkedSet.add(maybe))
            {
            /* Adds Obj to Tail of Queue - .offer() */
               queue.offer(maybe);
               distToPlayer[maybe.x][maybe.y] = distToPlayer[qPt.x][qPt.y]+1;
            }
         }
      }
   }
   public static List<Point> getPathableTiles(Point p)/**/
   {/* Assumes Point p Does Not Path Outside Floor*/
   /* To prevent costly resizeing, capacity is set to the maximum possible outcome */
      List<Point> adjTiles = new ArrayList<Point>(4);
      boolean [] tileBools = floor[p.x][p.y].getOps();
      if(tileBools[0])
      {/* Up Direction */
         adjTiles.add(new Point(p.x,p.y-1));
      }
      if(tileBools[1])
      {/* Down Direction */
         adjTiles.add(new Point(p.x,p.y+1));
      }
      if(tileBools[2])
      {/* Left Direction */
         adjTiles.add(new Point(p.x-1,p.y));
      }
      if(tileBools[3])
      {/* Right Direction */
         adjTiles.add(new Point(p.x+1,p.y));
      }
      return adjTiles;
   }
   private static void runPathfinding()/**/
   {
   /* Assumes that -1,-1 is an invalid point - to run at least once */
      Point playerPt = new Point(-1,-1);
      while(true)
      {
         synchronized(PATH_LOCK)
         {
            try
            {
            /* Releases PATH_LOCK Monitor and waits untill PATH_LOCK is Notified */
               PATH_LOCK.wait();
            }
            catch(InterruptedException e)
            {
            /* Interupt While Waiting - Skip Rest Of Loop - Go Back To Waiting */
               continue;
            }
         }
      /* Runs Process Untill Interupted - Clears Interrupt Flag */
         while(!Thread.interrupted())
         {
         /* Only Recalculate Pathfinding If Player Has Changed Locations */
            if(PLAYER.getTile() != playerPt)
            {
               playerPt = PLAYER.getTile();
               updateDistToPlayer();
               moveDeamons();
               updateFog(playerPt); 
            }
         }
      }
   }
   private static void runGraphics()/***/
   {
      while(true)
      {
         synchronized(MENU_LOCK)
         {
         /* Draws Loading Screen If Waiting */
            drawLoad();
            try
            {
            /* Releases MENU_LOCK Monitor and waits untill MENU_LOCK is Notified */
               MENU_LOCK.wait();
            }
            catch(InterruptedException e)
            {
            /* Interupt While Waiting - Skip Rest Of Loop - Go Back To Waiting */
               continue;
            }
         }
      /* Runs Process Untill Interupted - Clears Interrupt Flag */
         while(!Thread.interrupted())
         {
            if(menuUp)
            {
               drawMenu();
            }
            else
            {
               drawPlay();
            }
         }
      }
   }
   private static void setupFloorImage()/**/
   {
      BufferedImage tempImage = GC.createCompatibleImage(floor.length*4,floor[0].length*4,Transparency.BITMASK);
      WALLS.clear();
      Point finish = new Point();
      Graphics g = tempImage.createGraphics();
      for(int i=0;i<floor.length;i++)
      {
         for(int j=0;j<floor[0].length;j++)
         {
            if(floor[i][j].isExit())
            {/* If Exit Tile Display additional Image */
               finish = new Point(i,j);
            }
            BufferedImage tileImg = floor[i][j].getImg();
            g.drawImage(tileImg,i*4-2,j*4-2,null);
         }
      }
      for(int i=0;i<tempImage.getWidth();i++)
      {
         for(int j=0;j<tempImage.getHeight();j++)
         {
            if(tempImage.getRGB(i,j) != 0)
            {
               WALLS.add(new Point(i,j));
            }
         }
      }
      floorImage = GC.createCompatibleImage(tempImage.getWidth()*PIXEL_WIDTH,tempImage.getHeight()*PIXEL_HEIGHT,Transparency.BITMASK);
      g = floorImage.createGraphics();
      g.drawImage(tempImage.getScaledInstance(tempImage.getWidth()*PIXEL_WIDTH,tempImage.getHeight()*PIXEL_HEIGHT,Image.SCALE_DEFAULT),0,0,null);
      g.dispose();
   }
   private static void drawPlay()/***(17)***/
   {
      do
      {
         do
         {
            Graphics g = MENUBACKGROUND.createGraphics();
            refresh(g);
            drawFloor(g);
            drawDaemons(g);
            drawPlayer(g);
            drawFog(g);
            drawHUD(g);
         
            g = BS.getDrawGraphics();
            g.drawImage(MENUBACKGROUND,0,0,null);
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }
   private static void updateFog(Point p)
   {
      if(!fogOn)
      {
         return;
      }
      for(int i=0;i<fog.length;i++)
      {
         for(int j=0;j<fog[0].length;j++)
         {
            if(fog[i][j]==2)
            {
               fog[i][j]=1;
            }
         }
      }
      fog[p.x][p.y] = 2;
      boolean[] dirs = floor[p.x][p.y].getOps();
      boolean up = dirs[0];
      boolean down = dirs[1];
      boolean left = dirs[2];
      boolean right = dirs[3];
      int counter = 1;
      while(up)
      {
         List<Point> branch = getPathableTiles(new Point(p.x,p.y-counter));
         for(Point b: branch)
         {
            fog[b.x][b.y] = 2;
         }
         up = floor[p.x][p.y-counter].getOps()[0];
         counter++;
      }
      counter = 1;
      while(down)
      {
         List<Point> branch = getPathableTiles(new Point(p.x,p.y+counter));
         for(Point b: branch)
         {
            fog[b.x][b.y] = 2;
         }
         down = floor[p.x][p.y+counter].getOps()[1];
         counter++;
      }
      counter = 1;
      while(left)
      {
         List<Point> branch = getPathableTiles(new Point(p.x-counter,p.y));
         for(Point b: branch)
         {
            fog[b.x][b.y] = 2;
         }
         left = floor[p.x-counter][p.y].getOps()[2];
         counter++;
      }
      counter = 1;
      while(right)
      {
         List<Point> branch = getPathableTiles(new Point(p.x+counter,p.y));
         for(Point b: branch)
         {
            fog[b.x][b.y] = 2;
         }
         right = floor[p.x+counter][p.y].getOps()[3];
         counter++;
      }
   }
   private static void drawFog(Graphics g)
   {
      if(!fogOn)
      {
         return;
      }
      Color visited = new Color(0,0,0,100);
      Point offset = getCenterOffset();
      for(int i=0;i<fog.length;i++)
      {
         for(int j=0;j<fog[0].length;j++)
         {
            switch(fog[i][j])
            {
               case 0:
                  g.setColor(Color.BLACK);
                  g.fillRect(offset.x+i*TILE_WIDTH,offset.y+j*TILE_HEIGHT,TILE_WIDTH,TILE_HEIGHT);
                  break;
               case 1:
                  g.setColor(visited);
                  g.fillRect(offset.x+i*TILE_WIDTH,offset.y+j*TILE_HEIGHT,TILE_WIDTH,TILE_HEIGHT);
                  break;
               default:
            }
         }
      }
   }
   private static void drawLoad()/***(14)***/
   {
      do
      {
         do
         {
            Graphics g = BS.getDrawGraphics();
            refresh(g);
            drawLoadIcon(g);
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }
   private static void drawMenu()/***(15)***/
   {
      if(configMenu)
      {
         drawConfigMenu();
         return;
      }
      else if(volumeMenu)
      {
         drawVolumeMenu();
         return;
      }
      if(KEY_STAT.get(ESCAPE))
      {
         KEY_STAT.put(ESCAPE,false);
         menuUp=false;
         synchronized(PATH_LOCK){PATH_LOCK.notify();}
         synchronized(PLAYER_LOCK){PLAYER_LOCK.notify();}
      }
      Point mouseP = MouseInfo.getPointerInfo().getLocation();
      do
      {
         do
         {
            Graphics g = BS.getDrawGraphics();
            g.drawImage(MENUBACKGROUND,0,0,null);
            g.setColor(MENU_MASK);
            g.fillRect(0,0,XRES,YRES);
            g.setColor(Color.WHITE);
            String txt = "Menu";
            FontMetrics fm = g.getFontMetrics();
            int txtW = fm.stringWidth(txt);
            int txtH = fm.getHeight();
            g.drawString(txt,XRES/2-txtW/2,YRES/2-55);
            Font norm = g.getFont();
            Font big = norm.deriveFont(13f);
         
            Image rollover = GOLD_BUTTON.getScaledInstance((int)(GOLD_BUTTON.getWidth()*1.1),(int)(GOLD_BUTTON.getHeight()*1.1),Image.SCALE_DEFAULT);
         
            Rectangle buttonBounds = new Rectangle((XRES-GOLD_BUTTON.getWidth())/2,(YRES-GOLD_BUTTON.getHeight())/2,GOLD_BUTTON.getWidth(),GOLD_BUTTON.getHeight());
            int rollButtonY = (YRES-rollover.getHeight(null))/2;
            int rollButtonX = (XRES-rollover.getWidth(null))/2;
            g.setColor(Color.BLACK);
            for(int i=0;i<3;i++)
            {
               int offset = GOLD_BUTTON.getHeight()*2*i;
               if(buttonBounds.contains(mouseP))
               {
                  if(MOUSE_STAT.get(LEFT_CLICK))
                  {
                     MOUSE_STAT.put(LEFT_CLICK,false);
                     switch(i)
                     {
                        case 0:
                           configMenu = true;
                           break;
                        case 1:
                           volumeMenu = true;
                           break;
                        default:
                           System.exit(0);
                     }
                     return;
                  }
                  g.drawImage(rollover,rollButtonX,rollButtonY+offset,null);
                  g.setFont(big);
               }
               else
               {
                  g.setFont(norm);
                  g.drawImage(GOLD_BUTTON,buttonBounds.x,buttonBounds.y,null);
               }
               String buttonTxt;
               switch(i)
               {
                  case 0:
                     buttonTxt = "Input Configuration";
                     break;
                  case 1:
                     buttonTxt = "Volume";
                     break;
                  default:
                     buttonTxt = "Quit";
               }
               fm = g.getFontMetrics();
               int buttonTxtWidth = fm.stringWidth(buttonTxt);
               int buttonTxtHeight = fm.getHeight();
               g.drawString(buttonTxt,(XRES-buttonTxtWidth)/2,(YRES+buttonTxtHeight)/2+offset);
            
               buttonBounds.translate(0,GOLD_BUTTON.getHeight()*2);
            }
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }
   private static void drawConfigMenu()/***(15)***/
   {
      if(KEY_STAT.get(ESCAPE))
      {
         KEY_STAT.put(ESCAPE,false);
         configMenu=false;
         return;
      }
      do
      {
         do
         {
            Graphics g = BS.getDrawGraphics();
            g.drawImage(MENUBACKGROUND,0,0,null);
            g.setColor(MENU_MASK);
            g.fillRect(0,0,XRES,YRES);
         
            FontMetrics fm = g.getFontMetrics();
            int txtH = fm.getHeight();
            int columPad = 2;
            for(int i=-2;i<2;i++)
            {
               int offset = 40*i;
               g.drawImage(SILVER_BUTTON,XRES/2-columPad-SILVER_BUTTON.getWidth(),YRES/2+offset,null);
               g.drawImage(DARK_BUTTON,XRES/2+columPad,YRES/2+offset,null);
               String inputTxt,slotTxt;
               switch(i)
               {
                  case -2:
                     inputTxt = KeyEvent.getKeyText(MOVE_UP);
                     slotTxt = "Move Up";
                     break;
                  case -1:
                     inputTxt = KeyEvent.getKeyText(MOVE_DOWN);
                     slotTxt = "Move Down";
                     break;
                  case 0:
                     inputTxt = KeyEvent.getKeyText(MOVE_LEFT);
                     slotTxt = "Move Left";
                     break;
                  default:
                     inputTxt = KeyEvent.getKeyText(MOVE_RIGHT);
                     slotTxt = "Move Right";  
               }
               g.setColor(Color.WHITE);
               int txtW = fm.stringWidth(inputTxt);
               g.drawString(inputTxt,(XRES+DARK_BUTTON.getWidth()-txtW)/2+columPad,(YRES+DARK_BUTTON.getHeight()+txtH)/2+offset);
               g.setColor(Color.BLACK);
               txtW = fm.stringWidth(slotTxt);
               g.drawString(slotTxt,(XRES-SILVER_BUTTON.getWidth()-txtW)/2-columPad,(YRES+SILVER_BUTTON.getHeight()+txtH)/2+offset);
            }   
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }
   private static void drawVolumeMenu()/***(15)***/
   {
      if(KEY_STAT.get(ESCAPE))
      {
         KEY_STAT.put(ESCAPE,false);
         volumeMenu=false;
         return;
      }
      do
      {
         do
         {
            Graphics g = BS.getDrawGraphics();
            g.drawImage(MENUBACKGROUND,0,0,null);
            g.setColor(MENU_MASK);
            g.fillRect(0,0,XRES,YRES);
            g.setColor(Color.WHITE);
            String txt = "Volume";
            int txtW = g.getFontMetrics().stringWidth(txt);
            g.drawString(txt,XRES/2-txtW/2,YRES/2-55);
         
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());
   }
   private static void refresh(Graphics g)/**/
   {
      g.drawImage(BACKGROUND,0,0,null);
   }
   private static void drawLoadIcon(Graphics g)/***(14)***/
   {
      g.setColor(Color.WHITE);
      g.drawString("Loading...",XRES/2,YRES/2);
   }
   private static void drawFloor(Graphics g)/***(13)***/
   {
      g.setColor(Color.DARK_GRAY);
      Point offset = getCenterOffset();
      g.fillRect(offset.x,offset.y,floor.length*TILE_WIDTH,floor[0].length*TILE_HEIGHT);
      if(optiLine)
      {
         Point currTile = new Point(exitPoint.x,exitPoint.y);
         int min = distToPlayer[currTile.x][currTile.y];
         List<Point> nextMoves;
         while(min > 0)
         {
            g.drawImage(OPTI_LINE_IMAGE,offset.x+(currTile.x)*TILE_WIDTH,offset.y+(currTile.y)*TILE_HEIGHT,null);
            nextMoves = getPathableTiles(currTile);
            for(Point next: nextMoves)
            {
               if(min > distToPlayer[next.x][next.y])
               {
                  min = distToPlayer[next.x][next.y];
                  currTile = next;
               }
            }
            min = distToPlayer[currTile.x][currTile.y];
         }
      }
      g.drawImage(floorImage,offset.x,offset.y,null);
      g.drawImage(tile.FINPIC.getScaledInstance((TILE_WIDTH*2),(TILE_HEIGHT*2),Image.SCALE_DEFAULT),offset.x+exitPoint.x*TILE_WIDTH-2*PIXEL_WIDTH,offset.y+exitPoint.y*TILE_HEIGHT-2*PIXEL_HEIGHT,null);
      if(GRID || NUMBERS)
      {
         g.setColor(Color.WHITE);
         for(int i=0;i<floor.length;i++)
         {
            for(int j=0;j<floor[0].length;j++)
            {
               if(GRID)
               {
                  g.drawRect(offset.x+i*TILE_WIDTH,offset.y+j*TILE_HEIGHT,TILE_WIDTH,TILE_HEIGHT);
               }
               if(NUMBERS)
               {
                  g.drawString(new Integer(distToPlayer[i][j]).toString(),offset.x+i*TILE_WIDTH,offset.y+(j+1)*TILE_HEIGHT);
               }
            }  
         }  
      }
   }
   private static void drawDaemons(Graphics g)/**/
   {
      g.setColor(Color.PINK);
      Point offset = getCenterOffset();
      for(daemon d: GUPH)
      {
         g.fillRect(offset.x+d.getX()-d.width/2,offset.y+d.getY()-d.height/2,d.width,d.height);
      }
   }
   private static void drawPlayer(Graphics g)/**/
   {
      AffineTransform at = new AffineTransform();
      at.translate(CENTER_X,CENTER_Y);
      at.rotate(PLAYER.getFacing(),PLAYER.width/2,PLAYER.height/2);
      ((Graphics2D)g).drawImage(PLAYER.getImg(),at,null);
      //g.setColor(Color.WHITE);
      //g.fillRect(CENTER_X,CENTER_Y,PLAYER.width+1,PLAYER.height+1);
   }
   private static void drawHUD(Graphics g)/***(12)***/
   {
      if(!HUD)
      {/* If HUD Visibility is Turned Off, Skip Drawing */
         return;
      }
      g.drawString(PLAYER.getX()+","+PLAYER.getY(),100,100);
   /* Draws Window Frame */
      g.drawImage(FRAMEIMAGE,0,0,null);
   /* Draws MiniMap and MiniMap Frame */
      g.setColor(Color.DARK_GRAY);
      g.fillRect(XRES-50-MINIMAPFRAME.getWidth(),YRES-50-MINIMAPFRAME.getHeight(),MINIMAPFRAME.getWidth(),MINIMAPFRAME.getHeight());
      Graphics floorImageG = floorImage.createGraphics();
      //drawFog(floorImageG);
      floorImageG.dispose();   
      g.drawImage(floorImage.getScaledInstance(MINIMAPFRAME.getWidth()-32,MINIMAPFRAME.getHeight()-32,Image.SCALE_DEFAULT),XRES-50-MINIMAPFRAME.getWidth()+16,YRES-50-MINIMAPFRAME.getHeight()+16,null);
      g.drawImage(MINIMAPFRAME,XRES-50-MINIMAPFRAME.getWidth(),YRES-50-MINIMAPFRAME.getHeight(),null);
   /* Draws Armor, Health, and Mana Bars,Fills and Frames */
      g.setColor(Color.BLACK);
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight(),PLAYERSTATFRAME.getWidth(),PLAYERSTATFRAME.getHeight());
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight()*2,PLAYERSTATFRAME.getWidth(),PLAYERSTATFRAME.getHeight());
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight()*3,PLAYERSTATFRAME.getWidth(),PLAYERSTATFRAME.getHeight());
      g.setColor(Color.BLUE);
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight(),(int)(PLAYERSTATFRAME.getWidth()*PLAYER.getManaRatio()),PLAYERSTATFRAME.getHeight());
      g.setColor(Color.RED);
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight()*2,(int)(PLAYERSTATFRAME.getWidth()*PLAYER.getHealthRatio()),PLAYERSTATFRAME.getHeight());
      g.setColor(Color.GRAY);
      g.fillRect(50,YRES-50-PLAYERSTATFRAME.getHeight()*3,(int)(PLAYERSTATFRAME.getWidth()*PLAYER.getArmorRatio()),PLAYERSTATFRAME.getHeight());
      g.drawImage(PLAYERSTATFRAME,50,YRES-50-PLAYERSTATFRAME.getHeight(),null);
      g.drawImage(PLAYERSTATFRAME,50,YRES-50-PLAYERSTATFRAME.getHeight()*2,null);
      g.drawImage(PLAYERSTATFRAME,50,YRES-50-PLAYERSTATFRAME.getHeight()*3,null);
      g.dispose();
   }
   public static void main(String[] args)/**/
   {
   /* Graphics Thread - Responsible for Rendering All Visuals */
      Thread graph = new Thread(()->runGraphics(),"Graphics");
      graph.start();
   /* Pathfinding Thread - Responsible for Calculating All Pathfinding */
      Thread path = new Thread(()->runPathfinding(),"Pathfinding");
      path.start();
   /* Player Thread - Responsible for Handleing Player Actions */
      Thread play = new Thread(()->movePlayer(),"Player");
      play.start();
   /* This Thread - Responsible for Everything Else */
      Thread.currentThread().setName("Everything Else");
   
   /* While Still Running Something */
      while(true)
      {
      /* Intturupt all Threads - Set Them To Wait */
         graph.interrupt();
         path.interrupt();
         play.interrupt();
      /* Now Wait Untill Threads area actually Waiting */
         while(!(play.getState() == Thread.State.WAITING && path.getState() == Thread.State.WAITING && graph.getState() == Thread.State.WAITING)){} 
      /* Setup Next Maze */
         //setupNewLevel("BossFloor.txt");
         setupNewLevel();
      /* Start All Threads Again */
         synchronized(PATH_LOCK){PATH_LOCK.notify();}
         synchronized(MENU_LOCK){MENU_LOCK.notify();}
         synchronized(PLAYER_LOCK){PLAYER_LOCK.notify();}
         while(!floorClear)
         {/* While Current Floor Hasn't Been Cleared */
            if(!menuUp && KEY_STAT.get(ESCAPE))
            {/* If Escape then Pull Up Menu */
               KEY_STAT.put(ESCAPE,false);
               menuUp = true;
               path.interrupt();
               play.interrupt();
            }
         }
         floorNum++;
      }
   }
   private static void setupNewLevel()/**/
   {
      floorClear = false;
      createFloor(floorWidth,floorHeight);
      setupFloorImage();
      setDaemons();
   }
   private static void setupNewLevel(String p)/**/
   {
      floorClear = false;
      createFloor(p);
      setupFloorImage();
      setDaemons();
   }
   private static void createFloor(int x, int y)/***(10)***/
   {
      floor = new tile[x][y];
      distToPlayer = new int[x][y];
      fog = new int[x][y];
      for(int i=0;i<floor.length;i++)
      {
         for(int j=0;j<floor[0].length;j++)
         {
            floor[i][j] = new tile(tile.FULL_WALL);
         }
      }
      nullFloorSpacesRemaining = x*y; // sets number of walls left to fill;
      exitPoint = setExit(new FourNum(0,0,floor.length,floor[0].length));
      floor[exitPoint.x][exitPoint.y].setExit();
      nullFloorSpacesRemaining--;
      List<FourNum> rooms = createRooms(8);
      createPath(exitPoint.toPoint());
      connectRooms(rooms);
   }
   private static void createFloor(String p)
   {
      Scanner s = null;
      try
      {
         s = new Scanner(new File(p));
      }
      catch(IOException e)
      {
         System.out.println("FloorFileError");
         System.exit(0);
      }
      int xLength=0,yLength=0;
      List<FourNum> rooms = new ArrayList<FourNum>();
      while(s.hasNextLine())
      {
         String line = s.nextLine().replaceAll(" ","");
         String label = line.substring(0,line.indexOf("="));
         if(label.equalsIgnoreCase("FloorSize"))
         {
            int colon = line.indexOf(":");
            xLength = Integer.valueOf(line.substring(line.indexOf("(")+1,colon));
            yLength = Integer.valueOf(line.substring(colon+1,line.indexOf(")")));
         }
         else if(label.equalsIgnoreCase("Rooms"))
         {
            int index = 0;
            while(line.indexOf("(",index)>=0)
            {
               index = line.indexOf("(",index)+1;
               String nextRoom = line.substring(index,line.indexOf(")",index));
               String [] roomSize = nextRoom.split(":");
               rooms.add(new FourNum(Integer.valueOf(roomSize[0]),Integer.valueOf(roomSize[1]),Integer.valueOf(roomSize[2]),Integer.valueOf(roomSize[3])));
            }
         }
      }
      
      floor = new tile[xLength][yLength];
      distToPlayer = new int[xLength][yLength];
      fog = new int[xLength][yLength];
      for(int i=0;i<floor.length;i++)
      {
         for(int j=0;j<floor[0].length;j++)
         {
            floor[i][j] = new tile(tile.FULL_WALL);
         }
      }
      nullFloorSpacesRemaining = xLength*yLength; // sets number of walls left to fill;
      exitPoint = setExit(new FourNum(0,0,floor.length,floor[0].length));
      floor[exitPoint.x][exitPoint.y].setExit();
      nullFloorSpacesRemaining--;
      for(FourNum rm: rooms)
      {
         int tileType;
         for(int i=0;i<rm.w;i++)
         {
            for(int j=0;j<rm.h;j++)
            {
               if(i==0)
               {
                  if(j==0)
                  {tileType = tile.WALL_NW;}
                  else if(j==rm.h-1)
                  {tileType = tile.WALL_SW;}
                  else
                  {tileType = tile.LEFT_WALL;}
               }
               else if(i==rm.w-1)
               {
                  if(j==0)
                  {tileType = tile.WALL_NE;}
                  else if(j==rm.h-1)
                  {tileType = tile.WALL_SE;}
                  else
                  {tileType = tile.RIGHT_WALL;}
               }
               else if(j==0)
               {
                  tileType = tile.UP_WALL;
               }
               else if(j==rm.h-1)
               {
                  tileType = tile.DOWN_WALL;
               }
               else
               {
                  tileType = tile.EMPTY;
               }
               floor[i+rm.x][j+rm.y].setType(tileType);
               nullFloorSpacesRemaining--;
            }
         }
      }
      createPath(exitPoint.toPoint());
      connectRooms(rooms);
   }
   private static ThreeNum setExit(FourNum rect)/**/
   {/* Assumes FourNum rect 's Included Area is Completely Valid */
      int x,y,dir;
      if(RAND.nextBoolean())
      {
         x = RAND.nextInt(rect.w)+rect.x;
         if(RAND.nextBoolean())
         {/* Up Direction */
            dir = 0; y = rect.y;
         }
         else
         {/* Down Direction */
            dir = 1; y = rect.y+rect.h-1;
         }
      }
      else
      {
         y = RAND.nextInt(rect.h)+rect.y;
         if(RAND.nextBoolean())
         {/* Left Direction */
            dir = 2; x = rect.x;
         }
         else
         {/* Right Direction */
            dir = 3; x = rect.x+rect.w-1;
         }
      }
      return new ThreeNum(x,y,dir);
   }
   private static List<FourNum> createRooms(int num)/***(5)***/
   {
      List<FourNum> rooms = new ArrayList<FourNum>();
      int numberOfDivisions = num-1;// aka ...0-1][2][3][4][5...
      BinaryTree splitter = new BinaryTree(0,new FourNum(0,0,floor.length,floor[0].length),RAND.nextBoolean());
      for(int i=0;i<numberOfDivisions;i++)
      {
         splitter.split();
      }
      List<FourNum> bounds = splitter.getLowestBounds();
      for(FourNum rect: bounds)//needs to be more uniform
      {
         Point one = new Point(RAND.nextInt(rect.w-3)+rect.x+1,RAND.nextInt(rect.h-3)+rect.y+1);
         Point two = new Point(RAND.nextInt(rect.w-(one.x-rect.x)-2)+2,RAND.nextInt(rect.h-(one.y-rect.y)-2)+2);
         rooms.add(new FourNum(one.x,one.y,two.x,two.y));
      }
      for(FourNum rm: rooms)
      {
         int tileType;
         for(int i=0;i<rm.w;i++)
         {
            for(int j=0;j<rm.h;j++)
            {
               if(i==0)
               {
                  if(j==0)
                  {tileType = tile.WALL_NW;}
                  else if(j==rm.h-1)
                  {tileType = tile.WALL_SW;}
                  else
                  {tileType = tile.LEFT_WALL;}
               }
               else if(i==rm.w-1)
               {
                  if(j==0)
                  {tileType = tile.WALL_NE;}
                  else if(j==rm.h-1)
                  {tileType = tile.WALL_SE;}
                  else
                  {tileType = tile.RIGHT_WALL;}
               }
               else if(j==0)
               {
                  tileType = tile.UP_WALL;
               }
               else if(j==rm.h-1)
               {
                  tileType = tile.DOWN_WALL;
               }
               else
               {
                  tileType = tile.EMPTY;
               }
               floor[i+rm.x][j+rm.y].setType(tileType);
               nullFloorSpacesRemaining--;
            }
         }
      }
      return rooms;
   
   }
   private static void connectRooms(List<FourNum> rooms)/**/
   {
      for(FourNum rm: rooms)
      {       
         connectFloor(setExit(rm));
      }
   }
   private static void createPath(Point startAt) //
   {/*Assumes Point startAt is Valid && Wall Tiles are Unvisited*/
      List<Point> stack = new ArrayList<Point>();
      Point current = startAt;
      while(0<nullFloorSpacesRemaining)
      {
         List<ThreeNum> unvisited = getWallNeighbors(current);
         if(!unvisited.isEmpty())
         {
            ThreeNum selection = unvisited.get(RAND.nextInt(unvisited.size()));
            stack.add(current);
            connectFloor(selection);
            nullFloorSpacesRemaining--;
            current = selection.toPoint();
         }
         else if(!stack.isEmpty())
         {
            current = stack.get(stack.size()-1);
            stack.remove(stack.size()-1);
         }
         else{System.out.println("Error: Could not connect path to all spaces - Remaining: "+ nullFloorSpacesRemaining); nullFloorSpacesRemaining=0;}         
      }
   }
   private static List<ThreeNum> getWallNeighbors(Point p)//
   {/* Assumes given point p is a valid tile location */
      List<ThreeNum> adjWalls = new ArrayList<ThreeNum>();
      if(0<=p.y-1 && floor[p.x][p.y-1].getType()==tile.FULL_WALL)
      {
         adjWalls.add(new ThreeNum(p.x,p.y-1,1));//up 
      }
      if(p.y+1<floor[0].length && floor[p.x][p.y+1].getType()==tile.FULL_WALL)
      {
         adjWalls.add(new ThreeNum(p.x,p.y+1,0));//down 
      }
      if(0<=p.x-1 && floor[p.x-1][p.y].getType()==tile.FULL_WALL)
      {
         adjWalls.add(new ThreeNum(p.x-1,p.y,3));//left 
      }
      if(p.x+1<floor.length && floor[p.x+1][p.y].getType()==tile.FULL_WALL)
      {
         adjWalls.add(new ThreeNum(p.x+1,p.y,2));//right 
      }
      return adjWalls;
   }
   private static void connectFloor(ThreeNum p)/**/
   {/*Assumes ThreeNum is Valid && Pointing in Valid Direction */
      ThreeNum adj;
      switch(p.z)
      {
         case 0: /* Up Direction */
            adj = new ThreeNum(p.x,p.y-1,1);
            break;
         case 1: /* Down Direction */
            adj = new ThreeNum(p.x,p.y+1,0);
            break;
         case 2: /* Left Direction */
            adj = new ThreeNum(p.x-1,p.y,3);
            break;
         default: /* Right Direction */
            adj = new ThreeNum(p.x+1,p.y,2);
      }
      floor[p.x][p.y].changeType(p.z,true);
      floor[adj.x][adj.y].changeType(adj.z,true);
   }
   private static void setDaemons()/**/
   {
      GUPH.clear();
      PLAYER.setLocation(new Point(PIXEL_WIDTH,PIXEL_HEIGHT));
      createAnima(1);
      createShade(1);
      createJinn(1);
   }
   private static void createAnima(int num)/**/
   {
      for(int i=0;i<num;i++)
      {
         GUPH.add(new anima(RAND.nextInt(floor.length)*TILE_WIDTH+TILE_WIDTH/2,RAND.nextInt(floor[0].length)*TILE_HEIGHT+TILE_HEIGHT/2));
      }
   }
   private static void createShade(int num)/**/
   {
      for(int i=0;i<num;i++)
      {
         GUPH.add(new shade(RAND.nextInt(floor.length)*TILE_WIDTH+TILE_WIDTH/2,RAND.nextInt(floor[0].length)*TILE_HEIGHT+TILE_HEIGHT/2));
      }
   }
   private static void createJinn(int num)/**/
   {
      for(int i=0;i<num;i++)
      {
         GUPH.add(new jinn(RAND.nextInt(floor.length)*TILE_WIDTH+TILE_WIDTH/2,RAND.nextInt(floor[0].length)*TILE_HEIGHT+TILE_HEIGHT/2));
      }
   }
   private static void moveDeamons()/**/
   {      
      for(daemon d: GUPH)
      {
         d.move();
      }
   }
   private static void movePlayer()/**/
   {
      while(true)
      {
         synchronized(PLAYER_LOCK)
         {
            try
            {
            /* Releases PLAYER_LOCK Monitor and waits untill PLAYER_LOCK is Notified */
               PLAYER_LOCK.wait();
            }
            catch(InterruptedException e)
            {
            /* Interupt While Waiting - Skip Rest Of Loop - Go Back To Waiting */
               continue;
            }
         }
      /* Runs Process Untill Interupted - Clears Interrupt Flag */
         while(!Thread.interrupted())
         {
            PLAYER.move();
            if(PLAYER.isOut())
            {
               floorClear = true;
            }
         }
      }
   }
}

abstract class daemon/***(6)***/
{
   private final Random RAND = new Random();
/* The Maimum Value of Attribute Allowed */
   protected int healthCap,armorCap,manaCap;
/* The Current Value of Attribute */
   protected int health,armor,mana;
/* Current Location */
   protected double xPos,yPos;
   public final int width, height;
   protected daemon(int xo, int yo,int hc,int ac,int mc)
   {
      xPos=xo; yPos=yo;
      healthCap = health = hc;
      armorCap = armor = ac;
      manaCap = mana = mc;
      width = 16; height=16;
   }
   public final int getX()/**/
   {
      return (int)xPos;
   }
   public final int getY()/**/
   {
      return (int)yPos;
   }
   public final Point getTile()/**/
   {
      return new Point(getX()/Erebus.TILE_WIDTH,getY()/Erebus.TILE_HEIGHT); 
   }
   public final void setLocation(Point p)/**/
   {
      xPos=p.x; yPos=p.y;
   }
   protected final <E> E getRandomElement(List<E> list)/**/
   {/* Selects Random Element From List */
      return list.get(RAND.nextInt(list.size()));
   }
   public final double getHealthRatio()/**/
   {/* The Percentage of Current Health */
      return (double)health/healthCap;
   }
   public final double getArmorRatio()/**/
   {/* The Percentage of Current Armor */
      return (double)armor/armorCap;
   }
   public final double getManaRatio()/**/
   {/* The Percentage of Current Mana */
      return (double)mana/manaCap;
   }
   public abstract void move();
   public abstract BufferedImage getImg();
}
final class vardoger extends daemon/***(6)***/
{
/* If Located On an Exit Tile */
   private static boolean out = false;
   private static double dirFacing = 0;
   private final BufferedImage PIC;
   public vardoger()
   {
      this(0,0,100,100,100);
   }
   public vardoger(int xo,int yo,int hc,int ac,int mc)
   {
      super(xo,yo,hc,ac,mc);
      PIC = Erebus.GC.createCompatibleImage(width,height,Transparency.BITMASK);
      try
      {/* Initialises Image For Class */
         Image png = ImageIO.read(new File("vardoger.png")).getScaledInstance(PIC.getWidth(),PIC.getHeight(),Image.SCALE_DEFAULT);
         Graphics g = PIC.getGraphics();
         g.drawImage(png,0,0,null);
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Vardoger Image File");
         System.exit(0);
      }
   }
   public void move()/***********Hella lota fix need */
   {
      if(Erebus.KEY_STAT.get(Erebus.MOVE_UP))
      {
         if(Erebus.KEY_STAT.get(Erebus.MOVE_LEFT))
         {
            dirFacing = (Math.PI*7)/4;
         }
         else if(Erebus.KEY_STAT.get(Erebus.MOVE_RIGHT))
         {
            dirFacing = Math.PI/4;
         }
         else
         {
            dirFacing = 0;
         }
      }
      else if(Erebus.KEY_STAT.get(Erebus.MOVE_DOWN))
      {
         if(Erebus.KEY_STAT.get(Erebus.MOVE_LEFT))
         {
            dirFacing = (Math.PI*5)/4;
         }
         else if(Erebus.KEY_STAT.get(Erebus.MOVE_RIGHT))
         {
            dirFacing = (Math.PI*3)/4;
         }
         else
         {
            dirFacing = Math.PI;
         }
      }
      else if(Erebus.KEY_STAT.get(Erebus.MOVE_LEFT))
      {
         dirFacing = (Math.PI*3)/2;
      }
      else if(Erebus.KEY_STAT.get(Erebus.MOVE_RIGHT))
      {
         dirFacing = Math.PI/2;
      }
      boolean upBlocked = Erebus.WALLS.contains(new Point(getX()/Erebus.PIXEL_WIDTH,(getY()-1)/Erebus.PIXEL_HEIGHT)) || Erebus.WALLS.contains(new Point((getX()+width)/Erebus.PIXEL_WIDTH,(getY()-1)/Erebus.PIXEL_HEIGHT));
      boolean downBlocked = Erebus.WALLS.contains(new Point(getX()/Erebus.PIXEL_WIDTH,(getY()+height+1)/Erebus.PIXEL_HEIGHT)) || Erebus.WALLS.contains(new Point((getX()+width)/Erebus.PIXEL_WIDTH,(getY()+height+1)/Erebus.PIXEL_HEIGHT));
      boolean leftBlocked =  Erebus.WALLS.contains(new Point((getX()-1)/Erebus.PIXEL_WIDTH,getY()/Erebus.PIXEL_HEIGHT)) || Erebus.WALLS.contains(new Point((getX()-1)/Erebus.PIXEL_WIDTH,(getY()+height)/Erebus.PIXEL_HEIGHT));
      boolean rightBlocked = Erebus.WALLS.contains(new Point((getX()+width+1)/Erebus.PIXEL_WIDTH,getY()/Erebus.PIXEL_HEIGHT)) || Erebus.WALLS.contains(new Point((getX()+width+1)/Erebus.PIXEL_WIDTH,(getY()+height)/Erebus.PIXEL_HEIGHT));
      if(Erebus.KEY_STAT.get(Erebus.MOVE_UP) && !Erebus.KEY_STAT.get(Erebus.MOVE_DOWN) && !upBlocked)
      {
         yPos-=.0001;//up
      }
      else if(!Erebus.KEY_STAT.get(Erebus.MOVE_UP) && Erebus.KEY_STAT.get(Erebus.MOVE_DOWN) && !downBlocked)
      {
         yPos+=.0001;//down
      }
      if(Erebus.KEY_STAT.get(Erebus.MOVE_LEFT) && !Erebus.KEY_STAT.get(Erebus.MOVE_RIGHT) && !leftBlocked)
      {
         xPos-=.0001;//left
      }
      else if(!Erebus.KEY_STAT.get(Erebus.MOVE_LEFT) && Erebus.KEY_STAT.get(Erebus.MOVE_RIGHT) && !rightBlocked)
      {
         xPos+=.0001;//right
      }
      if(Erebus.floor[getX()/Erebus.TILE_WIDTH][getY()/Erebus.TILE_HEIGHT].isExit())
      {
         out=true;
      }
      else
      {
         out=false;
      }
   }
   public double getFacing()
   {
      return dirFacing;
   }
   public BufferedImage getImg()/**/
   {
      return PIC;
   }
   public boolean isOut()/**/
   {
      return out;
   }
}
final class anima extends daemon/***(1)***(6)***/
{
   private static final BufferedImage PIC = Erebus.GC.createCompatibleImage(16,16,Transparency.BITMASK);
   static
   {/* Initialises Image For Class */
      try
      {
         Image png = ImageIO.read(new File("Anima.png"));
         Graphics g = PIC.getGraphics();
         g.drawImage(png,0,0,null);
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Anima Image File");
         System.exit(0);
      }
   
   }
   public anima(int xo,int yo)
   {
      super(xo,yo,100,100,100);
   }
   public void move()/**/
   {/* Sets Location To Random Possible Move Location */
      setLocation(getRandomElement(Erebus.getPathableTiles(getTile())));
   }
   public BufferedImage getImg()/**/
   {
      return PIC;
   }
}
final class shade extends daemon/***(6)***/
{
/* Step distance in which to attempt to move */
   private static int sensitivity;
   private static final BufferedImage PIC = Erebus.GC.createCompatibleImage(16,16,Transparency.BITMASK);
   static
   {/* Initialises Image For Class */
      try
      {
         Image png = ImageIO.read(new File("Shade.png"));
         Graphics g = PIC.getGraphics();
         g.drawImage(png,0,0,null);
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Shade Image File");
         System.exit(0);
      }
   }
   public shade(int xo,int yo)
   {
      super(xo,yo,100,100,100);
      sensitivity=10;
   }
   public void move()/**/
   {/* Moves In A Random Direction Toward The Player */
      Point here = getTile();
      int min = Erebus.distToPlayer[here.x][here.y];
   /* Only Try To Move if Player is Within sensitivity Steps */
      if(min<sensitivity)
      {
      /* Gets List of All Possible Move Locations */
         List<Point> nextMoves = Erebus.getPathableTiles(getTile());
      /* The List Of Move Locations That Are Closer To The Player */
         List<Point> minMoves = new ArrayList<Point>(4);
         minMoves.add(new Point(getX(),getY()));
         for(Point next: nextMoves)
         {
            if(min > Erebus.distToPlayer[next.x][next.y])
            {
               min = Erebus.distToPlayer[next.x][next.y];
               minMoves.clear();
               minMoves.add(next);
            }
            else if(min == Erebus.distToPlayer[next.x][next.y])
            {
               minMoves.add(next);
            }
         }
      /* Move To Random Closer Location */
         setLocation(getRandomElement(minMoves));
      }
   }
   public BufferedImage getImg()/**/
   {
      return PIC;
   }
}
final class jinn extends daemon/***(6)***/
{
/* Step distance in which to attempt to move */
   private int sensitivity=0;
   private static final BufferedImage PIC = Erebus.GC.createCompatibleImage(16,16,Transparency.BITMASK);
   static
   {/* Initialises Image For Class */
      try
      {
         Image png = ImageIO.read(new File("Jinn.png"));
         Graphics g = PIC.getGraphics();
         g.drawImage(png,0,0,null);
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Jinn Image File");
         System.exit(0);
      }
   
   }
   public jinn(int xo,int yo)
   {
      super(xo,yo,100,100,100);
      sensitivity = 10;
   }
   public void move()/**/
   {/* Moves In A Random Direction Away From Player */
      Point here = getTile();
      int max = Erebus.distToPlayer[here.x][here.y];
   /* Only Try To Move if Player is Within sensitivity Steps */
      if(max<sensitivity)
      {
      /* Gets List of All Possible Move Locations */
         List<Point> nextMoves = Erebus.getPathableTiles(getTile());
      /* The List Of Move Locations That Are Father Away From The Player */
         List<Point> maxMoves = new ArrayList<Point>(4);
         maxMoves.add(new Point(getX(),getY()));
         for(Point next: nextMoves)
         {
            if(max < Erebus.distToPlayer[next.x][next.y])
            {/* If Move Location is Further From The Player */
               max = Erebus.distToPlayer[next.x][next.y];
               maxMoves.clear();
               maxMoves.add(next);
            }
            else if(max == Erebus.distToPlayer[next.x][next.y])
            {/* If Move Loacation is Equaly Far From The Player*/
               maxMoves.add(next);
            }
         }
      /* Move to Random Farther Location */
         setLocation(getRandomElement(maxMoves));
      }
   }
   public BufferedImage getImg()/**/
   {
      return PIC;
   }
}

final class tile/**/
{
   public static final int
   EMPTY=0, FULL_WALL=1,
   DOWN_WALL=2,UP_WALL=3,RIGHT_WALL=4,LEFT_WALL=5,
   WALL_SE=6,WALL_NE=7,WALL_NW=8,WALL_SW=9,
   PATH_H=10,PATH_V=11,
   END_UP=12,END_DOWN=13,END_LEFT=14,END_RIGHT=15;   
   private static final boolean[][] CHOICE = new boolean[16][4];
   static
   {/* Represents Ability to Move: UP, DOWN, LEFT, RIGHT : From This Tile */
      CHOICE[EMPTY]    = new boolean[]{ true, true, true, true};
      CHOICE[FULL_WALL]= new boolean[]{false,false,false,false};
   
      CHOICE[DOWN_WALL] = new boolean[]{ true,false, true, true};
      CHOICE[UP_WALL]   = new boolean[]{false, true, true, true};
      CHOICE[RIGHT_WALL]= new boolean[]{ true, true, true,false};
      CHOICE[LEFT_WALL] = new boolean[]{ true, true,false, true};
   
      CHOICE[WALL_SE]= new boolean[]{ true,false, true,false};
      CHOICE[WALL_NE]= new boolean[]{false, true, true,false};
      CHOICE[WALL_NW]= new boolean[]{false, true,false, true};
      CHOICE[WALL_SW]= new boolean[]{ true,false,false, true};
   
      CHOICE[PATH_H]= new boolean[]{false,false, true, true};
      CHOICE[PATH_V]= new boolean[]{ true, true,false,false};
   
      CHOICE[END_UP]   = new boolean[]{ true,false,false,false};
      CHOICE[END_DOWN] = new boolean[]{false, true,false,false};
      CHOICE[END_LEFT] = new boolean[]{false,false, true,false};
      CHOICE[END_RIGHT]= new boolean[]{false,false,false, true};
   }
   private static final BufferedImage[] PICS = new BufferedImage[CHOICE.length];
   public static final BufferedImage FINPIC = Erebus.GC.createCompatibleImage(8,8,Transparency.BITMASK);
   static
   {
      try
      {
      /* Initialises The Overlay Image For Exit Tiles */
         Image png = ImageIO.read(new File("Tile_Fin.png")).getScaledInstance(FINPIC.getWidth(),FINPIC.getHeight(),Image.SCALE_DEFAULT);
         Graphics g = FINPIC.createGraphics();
         g.drawImage(png,0,0,null);
      /* Initialises The Images For Each Tile Type */
         String[] fileNames = new String[PICS.length];
         fileNames[EMPTY]     = "Tile_Wall_Empty.png";
         fileNames[FULL_WALL] = "Tile_Wall_Full.png";
         fileNames[DOWN_WALL] = "Tile_Wall_Down.png";
         fileNames[UP_WALL]   = "Tile_Wall_Up.png";
         fileNames[RIGHT_WALL]= "Tile_Wall_Right.png";
         fileNames[LEFT_WALL] = "Tile_Wall_Left.png";
         fileNames[WALL_SE]   = "Tile_Wall_SE.png";
         fileNames[WALL_NE]   = "Tile_Wall_NE.png";
         fileNames[WALL_NW]   = "Tile_Wall_NW.png";
         fileNames[WALL_SW]   = "Tile_Wall_SW.png";
         fileNames[PATH_H]    = "Tile_Path_H.png";
         fileNames[PATH_V]    = "Tile_Path_V.png";
         fileNames[END_UP]    = "Tile_End_Up.png";
         fileNames[END_DOWN]  = "Tile_End_Down.png";
         fileNames[END_LEFT]  = "Tile_End_Left.png";
         fileNames[END_RIGHT] = "Tile_End_Right.png";
         for(int i=0;i<PICS.length;i++)
         {
            PICS[i] = Erebus.GC.createCompatibleImage(8,8,Transparency.BITMASK);
            png = ImageIO.read(new File(fileNames[i])).getScaledInstance(PICS[i].getWidth(),PICS[i].getHeight(),Image.SCALE_DEFAULT);
            g = PICS[i].createGraphics();
            g.drawImage(png,0,0,null);
         }
         g.dispose();
      }
      catch(IOException e)
      {
         System.out.println("Error reading Tile Image File");
         System.exit(0);
      }
   }
   private int type;
   private boolean exitTile=false;
   public tile(int t)/**/
   {type = t;}
   public void setType(int t)/**/
   {type = t;}
   public boolean isExit()/**/
   {
      return exitTile;
   }
   public void setExit()/**/
   {
      exitTile = true;
   }
   public BufferedImage getImg()/**/
   {
      return PICS[type];
   }
   public void changeType(int index,boolean value)/**/
   {/* Assumes index is Within Bounds[0-3] */
   /* Arrays.copyOf() - Returns New Array With Copied Elements */
      boolean [] newOps = Arrays.copyOf(CHOICE[type],4);
      newOps[index] = value;
      for(int i=0;i<CHOICE.length;i++)
      {
      /* Arrays.equals() - Returns true if 1 Depth Elements Are == */
         if(Arrays.equals(CHOICE[i],newOps))
         {
            setType(i);
            return;
         }
      }
   }
   public int getType()/**/
   {
      return type;
   }
   public boolean[] getOps()/**/
   {
      return CHOICE[type];
   }
}

final class ThreeNum/**/
{/* Typically Used As A Wrapper Class For A Point and Additional int */ 
   public int x,y,z;
   public ThreeNum(int xPos,int yPos, int zPos)
   {
      x=xPos; y=yPos; z=zPos;
   }
   public Point toPoint()
   {
      return new Point(x,y);
   }
   public String toString()
   {/* For De-Bugging Purposes */
      System.out.println(x+", "+y+" ,"+z);
      return null;
   }
}
final class FourNum/**/
{/* Typically Used As A Rectangle */
   public int x,y,w,h;
   public FourNum(int xPos,int yPos,int width, int height)
   {
      x=xPos; y=yPos; w=width; h=height;
   }
   public String toString()
   {/* For De-Bugging Purposes */
      System.out.println(x+", "+y+" ,"+w+" ,"+h);
      return null;
   }
}

final class BinaryTree/***(5)***/
{
   private static final Random RAND = new Random();
   private final int level;
   private boolean horzSplit;
   private FourNum bounds;
   private final BinaryTree[] nodes = new BinaryTree[2];
   public BinaryTree(int lvl,FourNum rect, boolean hSplit)
   {
      level  = lvl;
      bounds = rect;
      horzSplit = hSplit;
   }
   public List<FourNum> getLowestBounds()
   {
      List<FourNum> subBounds = new ArrayList<FourNum>();
      if(nodes[0]==null)
      {
         subBounds.add(bounds);
         return subBounds;
      }
      subBounds.addAll(nodes[0].getLowestBounds());
      subBounds.addAll(nodes[1].getLowestBounds());
      return subBounds;
   }
   public void split()//need fail if not enough room to garuntee space
   {
      if(nodes[0]==null)//aka bottom lvl
      {
         splitThis();
      }
      else
      {
         int maxLvlOne = nodes[0].getMaxLvl();
         int maxLvlTwo = nodes[1].getMaxLvl();
         if(maxLvlOne>maxLvlTwo)
         {
            nodes[1].split();
         }
         else if(maxLvlOne<maxLvlTwo)
         {
            nodes[0].split();
         }
         else
         {
            nodes[RAND.nextInt(2)].split();
         }
      }
   }
   private int getMaxLvl()
   {
      if(nodes[0] == null)
      {
         return level;
      }
      int maxLvlOne = nodes[0].getMaxLvl();
      int maxLvlTwo = nodes[1].getMaxLvl();
      return Math.max(maxLvlOne,maxLvlTwo);
   }
   private void splitThis()//need fix for int to dec div
   {
      FourNum one,two;
      if(horzSplit)//aka splitline was horz prev, now do vertSplit
      {
         one = new FourNum(bounds.x,bounds.y,bounds.w/2,bounds.h);
         two = new FourNum(bounds.x+bounds.w/2,bounds.y,bounds.w/2,bounds.h);
      }
      else
      {
         one = new FourNum(bounds.x,bounds.y,bounds.w,bounds.h/2);
         two = new FourNum(bounds.x,bounds.y+bounds.h/2,bounds.w,bounds.h/2);
      }
      nodes[0] = new BinaryTree(level+1,one,!horzSplit);
      nodes[1] = new BinaryTree(level+1,two,!horzSplit);
   }
}


final class KeyController implements KeyListener/**/
{
   public KeyController(){}
   public void keyPressed(KeyEvent key) 
   {
      int keyCode = key.getKeyCode();
      if(Erebus.KEY_STAT.containsKey(keyCode) && !Erebus.KEY_STAT.get(keyCode))
      {/* .put() - Sets This Key's Value(true) */
         Erebus.KEY_STAT.put(keyCode,true);
      }
   }
   public void keyReleased(KeyEvent key) 
   { 
      int keyCode = key.getKeyCode();
      if(Erebus.KEY_STAT.containsKey(keyCode))
      {/* .put() - Sets This Key's Value(false) */
         Erebus.KEY_STAT.put(keyCode,false);
      }
   }
   public void keyTyped(KeyEvent key){}
}
final class MouseController implements MouseListener/***(4)***/
{
   public MouseController(){}
   public void mousePressed(MouseEvent button) 
   { 
      int buttonNum = button.getButton();
      if(Erebus.MOUSE_STAT.containsKey(buttonNum) && !Erebus.MOUSE_STAT.get(buttonNum))
      {/* .put() - Sets This Button's Value(true) */
         Erebus.MOUSE_STAT.put(buttonNum,true);
      }
   }
   public void mouseReleased(MouseEvent button) 
   { 
      int buttonNum = button.getButton();
      if(Erebus.MOUSE_STAT.containsKey(buttonNum))
      {/* .put() - Sets This Button's Value(false) */
         Erebus.MOUSE_STAT.put(buttonNum,false);
      }
   }
   public void mouseExited(MouseEvent e){}
   public void mouseClicked(MouseEvent e){}
   public void mouseEntered(MouseEvent e){}
}