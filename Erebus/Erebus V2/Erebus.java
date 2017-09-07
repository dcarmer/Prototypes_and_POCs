/*    
   NOTE: Volitile should be used for a concurrent field with at most one write thread
   
   atomic's should be used for a feild with more than one write thread
   
   NOTE: VolatileImages are not always accelerated, 
   and the BufferStrategy may be using them internally anyway, 
   so no need to complicate code by using them
   
*/

/* 
CHECKLIST SYSTEM INFRASTRUCTURE CAPABILITIES
 * Load Saved Control Configuration
 * Change Control Configuration
 * Detect Keys Being Pressed
 * Detect Scrolling
 * Detect Mouse Position
 * Detect Mouse Buttons Being Pressed
 * Save Control Configuration
 * Seperate Controls For Menu and Play
 * Seperate Threads for Graphics, Player, Enemies
 * Maintain Constant Execution Speed
 * Control Sounds as Groups
 * Load Saved Player Data
 * Pause all Play when in Menu
 * Automatically Pause when focus lost
 * Support Multiple Saved Games


*/

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

public final class Erebus
{  
   /*** For Thread Communication ***/
   private static final PipedInputStream PIPE_IN = new PipedInputStream();
   public static final AtomicBoolean pipeNextInput = new AtomicBoolean(false);

   public static final GraphicsConfiguration GC;
   private static final BufferStrategy BS;
   public static final int XRES,YRES;
   public static final Frame f;
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
      EventController EC = new EventController(Erebus.PIPE_IN);
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
   /*** For Input Handleing ***/
   //contains only play keys - Left click has seperate handeling - Mouse keys are negative
   public static final Map<Integer,Boolean> INPUT_STAT = new ConcurrentHashMap<Integer,Boolean>(5,1f,2);
   public static final AtomicBoolean CLICKED = new AtomicBoolean(false);
   public static final AtomicInteger SCROLL = new AtomicInteger(0);
   public static final AtomicInteger MOUSE_X = new AtomicInteger(0), MOUSE_Y = new AtomicInteger(0);
   public static int MOVE_UP,MOVE_DOWN,MOVE_LEFT,MOVE_RIGHT,OPEN_MENU;
   static
   {
      resetBindings();
   }
   /*** For Exit Handling ***/
   static
   {
      Runtime runtime = Runtime.getRuntime();
      runtime.addShutdownHook(new Thread(()->saveControlBindings()));
   }
   
   private static final boolean grid = false, mesh = false;
   private static boolean menuUp=false,quit=false;
   public static final Player PLAYER = Player.getPlayer();
   private static final List<Floor> LEVELS = new ArrayList<Floor>();
   private static int LevelNum = 0;
   private static final Menu pauseMenu;
   static
   {/*HATE ALL OF MY MENU SYSTEM NEED TO FIX*/
      final BufferedImage normalImg = ImageManager.getBuffImg("Gold_Button");
      final BufferedImage invertImg = ImageManager.getScaledBuffImg(ImageManager.getInvertBuffImg(normalImg),1.09375,1.09375);
      final BufferedImage grayScaleImg = ImageManager.getGrayScaleBuffImg(normalImg);
      pauseMenu = new Menu(normalImg,invertImg,grayScaleImg);
      pauseMenu.addButton("Resume",XRES/2,YRES/2,()->pause(false));
      pauseMenu.addButton("Options",XRES/2,YRES/2+32,()->{});
      pauseMenu.addButton("Quit",XRES/2,YRES/2+64,()->System.exit(0));
      pauseMenu.add(new CustomLabel("Paused",XRES/2,YRES/2-16));
   }
   
   
   
   
   
   
   
   
   
   
   //ThreadLocal gives thread private variables
   private Erebus(){}
   //FIX FEILD CONCURANCY, ADD FPS Controller, ADD FOGofWAR, ADD Enemies, ADD Pathfinding, ADD Sound Handleing
   public static void main(String[] args)
   {
      loadControlBindings();
      long time = System.currentTimeMillis();
      LEVELS.add(new Floor());
      time = System.currentTimeMillis()-time;
      System.out.println(time);
      PLAYER.setLocation(Floor.CELL_SIZE*3/2,Floor.CELL_SIZE*3/2);
      
      final int mb = 1024*1024;
      int totalMB=0;
      int iterations=0;
      //Getting the runtime reference from system
      final Runtime runtime = Runtime.getRuntime();
      while(!quit)
      {
         System.gc();
         visual();
         move();
         if(INPUT_STAT.get(OPEN_MENU))
         {
            INPUT_STAT.put(OPEN_MENU,false);
            pause(!menuUp);
         }
         
         iterations++;
         totalMB +=  ((runtime.totalMemory() - runtime.freeMemory()) / mb);
         //System.out.println("Average Used Memory:"+ totalMB/iterations+"  "+ (runtime.totalMemory() - runtime.freeMemory())/ mb);
      }
      System.exit(0);
   }
   
   
   public static void pause(boolean set)
   {
      menuUp = set;
      if(set)
      {
         f.add(pauseMenu);
      }
      else
      {
         f.remove(pauseMenu);
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
            if(menuUp)
            {
               drawMenu(g);
            }
            else
            {
               drawPlay(g);
            }   
            g.dispose();
         }while(BS.contentsRestored());
         BS.show();
      }while(BS.contentsLost());   
      
   }
   public static void drawPlay(final Graphics g)
   {
      g.setColor(Color.GRAY);
      g.fillRect(0,0,XRES,YRES);
      double scrollRatio = SCROLL.get();
      double zoom = (0>scrollRatio?1/Math.abs(scrollRatio):1+scrollRatio);
      AffineTransform at = AffineTransform.getTranslateInstance(XRES/2,YRES/2);
      at.scale(zoom,zoom);
      at.translate(-PLAYER.getLocation().x,-PLAYER.getLocation().y);
            
      ((Graphics2D)g).setTransform(at);
      getCurrFloor().drawMaze(g);
      if(mesh)
      {
         getCurrFloor().drawMesh(g);
      } 
      PLAYER.drawSelf(g);
      if(grid)
      {
         getCurrFloor().drawGrid(g);
      }
      ((Graphics2D)g).setTransform(new AffineTransform());
      g.setColor(Color.WHITE);
      g.drawString("x"+String.valueOf(zoom),100,100);
   }
   public static void drawMenu(final Graphics g)
   {
      g.setColor(Color.GRAY);
      g.fillRect(0,0,XRES,YRES);
      pauseMenu.drawMenu(g);
      
   }
  /*
   * Allows Player to Move
   */
   public static void move()
   {
      PLAYER.move();
   }
  /*
   * Returns the floor object associated with the current floorNum
   */
   public static Floor getCurrFloor()
   {
      return LEVELS.get(LevelNum);
   }

   /* 
   * Flags the InputListeners to pipe all input.
   * After the next Key/Button pressed, the Flag is reset.
   * The Pipe is cleared of all other data.
   * The key/Button pressed from the pipe data is returned.
   */
   private static int getNextInput()
   {
      pipeNextInput.set(true);
      int input=0;
      try
      {
         input = PIPE_IN.read();
         int extraData = PIPE_IN.available();
         if(extraData > 0)
         {
            PIPE_IN.skip(extraData);
         }
      }
      catch(IOException e)
      {
         System.out.println("Error Pipe Read");
         System.exit(0);
      }
      return input;
   }
   /* 
   * Assumes Parameters curr, change are valid KeyCode values.
   * Removes curr from KEY_STAT and replaces with change.
   * Sets change to value of curr.
   * Returns False if change is already assigned, curr is nonExistent, or if an unchangable value is given.
   */
   private static boolean changeBinding(int curr, int change)
   {
      if(INPUT_STAT.containsKey(change))
      {
         System.out.println("Error This input is already being used");
      }
      else if(INPUT_STAT.containsKey(curr))
      {
         if(curr==MOVE_UP)
         {
            INPUT_STAT.remove(MOVE_UP);
            MOVE_UP=change;
            INPUT_STAT.put(MOVE_UP,false);
         }
         else if(curr==MOVE_DOWN)
         {
            INPUT_STAT.remove(MOVE_DOWN);
            MOVE_DOWN=change;
            INPUT_STAT.put(MOVE_DOWN,false);
         }
         else if(curr==MOVE_LEFT)
         {
            INPUT_STAT.remove(MOVE_LEFT);
            MOVE_LEFT=change;
            INPUT_STAT.put(MOVE_LEFT,false);
         }
         else if(curr==MOVE_RIGHT)
         {
            INPUT_STAT.remove(MOVE_RIGHT);
            MOVE_RIGHT=change;
            INPUT_STAT.put(MOVE_RIGHT,false);
         }
         else
         {
            System.out.println("Error Key is Unchangable");
            return false;
         }
         return true;
      }
      else
      {
         System.out.println("Error Key not Recognized");
      }
      return false;
   }
   /*
   * Clears Input Map.
   * Sets Movement Controls To Default(WSAD).
   * Re-adds All Mappings.
   */
   private static void resetBindings()
   {
      INPUT_STAT.clear();
      INPUT_STAT.put(OPEN_MENU = KeyEvent.VK_ESCAPE,false);
      INPUT_STAT.put(MOVE_UP = KeyEvent.VK_W,false);
      INPUT_STAT.put(MOVE_DOWN = KeyEvent.VK_S,false);
      INPUT_STAT.put(MOVE_LEFT = KeyEvent.VK_A,false);
      INPUT_STAT.put(MOVE_RIGHT = KeyEvent.VK_D,false);
   }
   private static void loadControlBindings()//Maybe Make Binary File???????????????????
   {
      try(final BufferedReader reader = new BufferedReader(new FileReader("controlBindings.txt")))
      {
         INPUT_STAT.clear();
         String line;
         while((line=reader.readLine()) != null)
         {
            line = line.replaceAll(" ","");
            int seperator = line.indexOf(":");
            String lineLabel = line.substring(0,seperator);
            int lineValue = Integer.valueOf(line.substring(seperator+1));
            if((lineValue < -MouseEvent.BUTTON3 || lineValue > -MouseEvent.BUTTON1) && KeyEvent.getKeyText(lineValue).contains("Unknown"))
            {
               throw new Exception("Unknown Value");
            }
            if(INPUT_STAT.containsKey(lineLabel))
            {
               throw new Exception("Duplicate Value");
            }
            switch(lineLabel)
            {
               case "Move_Up":
                  MOVE_UP = lineValue;
                  INPUT_STAT.put(MOVE_UP,false);
                  break;
               case "Move_Down":
                  MOVE_DOWN = lineValue;
                  INPUT_STAT.put(MOVE_DOWN,false);
                  break;
               case "Move_Left":
                  MOVE_LEFT = lineValue;
                  INPUT_STAT.put(MOVE_LEFT,false);
                  break;
               case "Move_Right":
                  MOVE_RIGHT = lineValue;
                  INPUT_STAT.put(MOVE_RIGHT,false);
                  break;
               case "Open_Menu":
                  OPEN_MENU = lineValue;
                  INPUT_STAT.put(OPEN_MENU,false);
                  break;
            }
         }
         if(!(INPUT_STAT.containsKey(MOVE_UP) && INPUT_STAT.containsKey(MOVE_DOWN) && INPUT_STAT.containsKey(MOVE_LEFT) && INPUT_STAT.containsKey(MOVE_RIGHT) && INPUT_STAT.containsKey(OPEN_MENU)))
         {
            throw new Exception("Missing Value");
         }
      }
      catch(Exception e)
      {
         System.out.println(e);
         System.out.println("Error Reading Control Bindings File - Will Use Defaults");
         /*Here to make sure partialy incorrectly read files don't screw up controls */
         resetBindings();
      }
   }
   /*
   * Writes Current Control Values to File
   */
   private static void saveControlBindings()
   {
      try(PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("controlBindings.txt"))))
      {
         writer.println("Open_Menu:"+OPEN_MENU);
         writer.println("Move_Up:"+MOVE_UP);
         writer.println("Move_Down:"+MOVE_DOWN);
         writer.println("Move_Left:"+MOVE_LEFT);
         writer.println("Move_Right:"+MOVE_RIGHT);
         writer.close();
      }
      catch(IOException e)
      {
         System.out.println("Error - Couldn't Save Control Bindings File");
      }
   }
}
/*
* An Object Describing the controllable Character.
* Uses Singleton Pattern for single Instantiation.
*/
final class Player 
{
   private static final Player INSTANCE = new Player();
   private int xPos,yPos;
   private double facing;
   private final BufferedImage SKIN = ImageManager.getBuffImg("Player",Transparency.BITMASK);
   private final Clip STEP = SoundManager.getSound(SoundManager.EFFECTS,"step.wav");
   private Player()
   {
      facing = xPos = yPos = 0;
   }
   public static Player getPlayer()
   {
      return INSTANCE;
   }
   public Point getLocation()
   {
      return new Point(xPos,yPos);
   }
   public synchronized void setLocation(int x,int y)
   {
      xPos = x;
      yPos = y;
   }
   public synchronized void move()
   {
      final boolean up = Erebus.INPUT_STAT.get(Erebus.MOVE_UP);
      final boolean down = Erebus.INPUT_STAT.get(Erebus.MOVE_DOWN);
      final boolean left = Erebus.INPUT_STAT.get(Erebus.MOVE_LEFT);
      final boolean right = Erebus.INPUT_STAT.get(Erebus.MOVE_RIGHT);
      final int vert = (down?1:0)-(up?1:0);
      final int horz = (right?1:0)-(left?1:0);
      if(horz!=0 || vert!=0)
      {
         if(!STEP.isActive())
         {
            STEP.setFramePosition(0);
            STEP.start();
         }
         //final boolean horzBlocked = Erebus.getCurrFloor().getCollision().contains(new Point((xPos+horz)/Floor.CELL_SIZE,(yPos)/Floor.CELL_SIZE)); 
         //final boolean vertBlocked = Erebus.getCurrFloor().getCollision().contains(new Point((xPos)/Floor.CELL_SIZE,(yPos+vert)/Floor.CELL_SIZE));
         //if(!horzBlocked)
         {
            xPos += 10*horz;
         }
         //if(!vertBlocked)
         {
            yPos += 10*vert;
         }
         
      }
   }
   public void drawSelf(final Graphics g)
   {
      g.drawImage(SKIN,xPos-SKIN.getWidth()/2,yPos-SKIN.getHeight()/2,null);
   }
}
/*
* An Object That holds data regarding pathable areas.
* This is stored as a collection of convex Polygons, and Portals joining linked Polygons
*/
final class NavMesh
{
   private final List<Sector> sects = new ArrayList<>();
   private final List<Portal> ports = new ArrayList<>();
   
   public void NavMesh(){}
   public void addSector(final int[] xs,final int[]ys)
   {
      int vertNum = Math.min(xs.length,ys.length);
      for(int i=2;i<vertNum;i++)
      {
         int[] newXs = new int[]{xs[0],xs[i-1],xs[i]};
         int[] newYs = new int[]{ys[0],ys[i-1],ys[i]};
         sects.add(new Sector(newXs,newYs));
         if(i>2)// aka poly was split and last two sectors should be connected
         {
            Portal p = new Portal(sects.get(sects.size()-1),new int[]{xs[0]+5,xs[i-1]+5,xs[i-1]-5,xs[0]-5},new int[]{ys[0]-5,ys[i-1]-5,ys[i-1]+5,ys[0]+5});
            p.addLink(sects.get(sects.size()-2));
            sects.get(sects.size()-1).addPortal(p);
            sects.get(sects.size()-2).addPortal(p);
            ports.add(p);
         }
      }
   }
   public void drawSelf(final Graphics2D g)
   {
      g.setColor(new Color(255,0,0,150));
      sects.parallelStream().forEach(s -> g.fill(s));
      g.setColor(Color.GREEN);
      ((Graphics2D)g).setStroke(new BasicStroke((float)(1/((Graphics2D)g).getTransform().getScaleX()),BasicStroke.JOIN_MITER,BasicStroke.CAP_BUTT));
      sects.stream().forEach(s -> g.draw(s));
      g.setColor(new Color(255,150,0));
      ports.parallelStream().forEach(p -> g.draw(p));
   }
   
   /*
   * An Object representing a convex Polygon, with links to pertinant portals
   */
   private static final class Sector extends Polygon
   {
      private final List<Portal> links = new ArrayList<>();
      private Sector(final int[] xs,final int[]ys)
      {
         super(xs,ys,Math.min(xs.length,ys.length));
      }
      private void addPortal(Portal p)
      {
         links.add(p);
      }
   }
   /*
   * An object containing links to accessable Sectors, and optional action
   */
   private static final class Portal extends Polygon
   {
      private final List<Sector> links = new ArrayList<>();
      private Portal(Sector s, int []xs,int[]ys)
      {
         super(xs,ys,Math.min(xs.length,ys.length));
         links.add(s);
      }
      private void addLink(Sector s)
      {
         links.add(s);
      }
   }
}
/*
* An Object Describing the map.
* Instantiation automatically generates layout.
*/
final class Floor
{
   public static final int CELL_SIZE = Cell.minAllowedRes;
   private final Point endPos;
   private final Cell[][] layout;
   private final NavMesh mesh = new NavMesh();
   private double lastZoom;
   private static final ThreadLocal<Random> rand = new ThreadLocal<Random>();
   private final BufferedImage lastDraw = ImageManager.getBuffImg(Erebus.XRES,Erebus.YRES,Transparency.BITMASK);
   public Floor()
   {
      rand.set(new Random(0));
      layout = new Cell[1+80][1+80];
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
      mesh.addSector(new int[]{current.x*CELL_SIZE,(current.x+1)*CELL_SIZE,(current.x+1)*CELL_SIZE,current.x*CELL_SIZE},new int[]{current.y*CELL_SIZE,current.y*CELL_SIZE,(current.y+1)*CELL_SIZE,(current.y+1)*CELL_SIZE});
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
            mesh.addSector(new int[]{csX*CELL_SIZE,(csX+1)*CELL_SIZE,(csX+1)*CELL_SIZE,csX*CELL_SIZE},new int[]{csY*CELL_SIZE,csY*CELL_SIZE,(csY+1)*CELL_SIZE,(csY+1)*CELL_SIZE});
            current.setLocation(csX+s.x,csY+s.y);
            layout[current.x][current.y] = Cell.getCell(TileType.PATH).changeCellLink(selection.getOpposite(),true);
            mesh.addSector(new int[]{current.x*CELL_SIZE,(current.x+1)*CELL_SIZE,(current.x+1)*CELL_SIZE,current.x*CELL_SIZE},new int[]{current.y*CELL_SIZE,current.y*CELL_SIZE,(current.y+1)*CELL_SIZE,(current.y+1)*CELL_SIZE});
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
   public void drawMesh(final Graphics g)
   {
      mesh.drawSelf((Graphics2D)g);
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


abstract class MenuComponent extends Component 
{
   protected MenuComponent()
   {
      super.setIgnoreRepaint(true);
   }
   protected abstract void drawSelf(Graphics g);
}
final class Menu extends Container
{
   public final List<MenuComponent> components = new ArrayList<>();
   public final BufferedImage normalImg,rolloverImg,pressedImg;
   public final int buttonWidth,buttonHeight;
   final int xOffset,yOffset;
   public Menu(BufferedImage n,BufferedImage r,BufferedImage p)
   {
      normalImg = n;
      rolloverImg = r;
      pressedImg = p;
      buttonWidth = normalImg.getWidth();
      buttonHeight = normalImg.getHeight();
      xOffset = rolloverImg.getWidth()- buttonWidth;
      yOffset = rolloverImg.getHeight()- buttonHeight;
      this.setBounds(0,0,Erebus.XRES,Erebus.YRES);
   }
   public void addButton(String label,int xLoc,int yLoc,Runnable action)
   {
      CustomButton b = 
         new CustomButton(this,label,xLoc,yLoc,action)
         {
            @Override
               protected void drawPressed(final Graphics g){g.drawImage(pressedImg,getX(),getY(),null);}
            @Override
               protected void drawRollover(final Graphics g){g.drawImage(rolloverImg,getX()-xOffset/2,getY()-yOffset/2,null);}
         };
      b.setBounds(xLoc-buttonWidth/2,yLoc-buttonHeight/2,buttonWidth,buttonHeight);
      components.add(b);
      super.add(b);
   }
   public void add(final CustomLabel l)
   {
      components.add(l);
      super.add(l);
   }
   public void drawMenu(Graphics g)
   {
      components.stream().forEach(c -> c.drawSelf(g));
   }
}
final class CustomLabel extends MenuComponent
{
   private final String text;
   public CustomLabel(final String s,final int x, final int y)
   {
      text = s;
      final FontMetrics fm = Erebus.f.getFontMetrics(Erebus.f.getFont());
      final int txtW = fm.stringWidth(text);
      final int txtH = fm.getHeight();
      super.setBounds(x-txtW/2,y-txtH/2,txtW,txtH);
   }
   @Override
   public void drawSelf(final Graphics g)
   {
      g.setColor(Color.WHITE);
      g.drawString(text,getX(),getY());
   }
}
class CustomButton extends MenuComponent
{
   protected final Menu parent;
   protected final String label;
   private boolean rollover = false,pressed = false;
   private final Runnable action;
   /* For now always assume Centered */
   public CustomButton(final Menu menu, final String txt,final int x,final int y,Runnable r)
   {
      parent = menu;
      label = txt;
      action = r;
      super.addMouseListener(
            new MouseListener()
            {
               @Override
               public void mouseEntered(final MouseEvent EVENT)
               {
                  CustomButton.this.rollover = true;
               }
               @Override
               public void mouseExited(final MouseEvent EVENT)
               {
                  CustomButton.this.rollover = false;
                  CustomButton.this.pressed = false;
               }
               @Override
               public void mousePressed(final MouseEvent EVENT)
               {
                  CustomButton.this.pressed = true;
               }
               @Override
               public void mouseClicked(final MouseEvent EVENT)
               {
                  CustomButton.this.clickAction();
               }
               @Override
               public void mouseReleased(final MouseEvent EVENT)
               {
                  CustomButton.this.pressed = false;
               }
            });
   }
   private void clickAction()
   {
      action.run();
   }
   protected void drawPressed(final Graphics g)
   {
      drawNormal(g);
   }
   protected void drawRollover(final Graphics g)
   {
      drawNormal(g);
   }
   protected void drawNormal(final Graphics g)
   {
      g.drawImage(parent.normalImg,getX(),getY(),null);
      g.setColor(Color.BLACK);
      g.drawString(label,getX(),getY()+getHeight());
   }
   @Override
   public final void drawSelf(final Graphics g)
   {
      if(pressed)
      {
         drawPressed(g);
      }
      else if(rollover)
      {
         drawRollover(g);
      }
      else
      {
         drawNormal(g);
      }
   }
}
enum SoundManager
{
   MUSIC,EFFECTS;
   private static final Map<SoundManager,Map<String,Clip>> groupMap = new EnumMap<>(SoundManager.class);
   static
   {
      for(SoundManager SM: SoundManager.values())
      {
         groupMap.put(SM,new ConcurrentHashMap<String,Clip>());
      }
   }
   public static void dropSound(final SoundManager SM,final String path)
   {
      groupMap.get(SM).remove(path);
   }
   public static Clip getSound(final SoundManager SM,final String path)
   {
      Clip mapped = groupMap.get(SM).get(path);
      if(mapped == null)
      {
         final File file = new File(path);
         try
         {
            mapped = AudioSystem.getClip();
            final AudioInputStream soundStream = AudioSystem.getAudioInputStream(file);
            mapped.open(soundStream);
         }
         catch(LineUnavailableException | UnsupportedAudioFileException | IOException e)
         {
            System.out.println("Error Loading Audio: "+path);
         }
         groupMap.get(SM).put(path,mapped);
      }
      return mapped;
   }
   public static void setMute(final SoundManager SM,final boolean mute)
   {
      groupMap.get(SM).values().parallelStream().forEach(
            clip -> 
            {
               final BooleanControl muteControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
               muteControl.setValue(mute);
               clip.setFramePosition(clip.getFramePosition());
               clip.start();
            });
      
   }
   public static void setGain(final SoundManager SM,final float gain)
   {
      groupMap.get(SM).values().parallelStream().forEach(
            clip -> 
            {
               final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
               gainControl.setValue(gain);
               clip.setFramePosition(clip.getFramePosition());
               clip.start();
            });
   }
   public static void addGain(final SoundManager SM,final float gain)
   {
      groupMap.get(SM).values().parallelStream().forEach(
            clip -> 
            {
               final FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
               gainControl.setValue(gain+gainControl.getValue());
               clip.setFramePosition(clip.getFramePosition());
               clip.start();
            });
   }
}
final class ImageManager
{
   private final static String[] extensions = new String[]{".jpg",".png"};
   private final static GraphicsConfiguration GC = Erebus.GC;
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
/*
* Enum Containing The Cardinal Directions
* Representing data on Direction or Rotation
*/
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

/* NOTE: All Listeners are run in the same thread, different from the Main Thread */
final class EventController implements KeyListener,MouseListener,MouseWheelListener,MouseMotionListener,FocusListener
{
   private final PipedOutputStream PIPE_OUT = new PipedOutputStream();
   public EventController(final PipedInputStream in)
   {
      try
      {
         PIPE_OUT.connect(in);
      }
      catch(IOException e)
      {
         System.out.println("Error Pipe Already Has Connection");
         System.exit(0);
      }   
   }
   public void keyPressed(final KeyEvent EVENT) 
   {
      final int KEY_ID = EVENT.getKeyCode();
      if(Erebus.pipeNextInput.get())
      {
         try
         {/* .write() - Adds data to Pipe */
            PIPE_OUT.write(KEY_ID);
            /* .flush() - Forces all pending writes */ 
            PIPE_OUT.flush();
            Erebus.pipeNextInput.set(false);
         }
         catch(IOException e)
         {
            System.out.println("Pipe Write Error");
            System.exit(0);
         }
      }
      else
      {/* .replace() - Sets The newValue(True) only if currently mapped and oldValue(False)*/
         Erebus.INPUT_STAT.replace(KEY_ID,false,true);
      }
   }
   public void keyReleased(final KeyEvent EVENT) 
   { 
      final int KEY_ID = EVENT.getKeyCode();
      /* .replace() - Sets the newValue(False) only if currently mapped */
      Erebus.INPUT_STAT.replace(KEY_ID,false);
   }
   public void mousePressed(final MouseEvent EVENT) 
   {
      final int BUTTON_ID = -EVENT.getButton();
      if(Erebus.pipeNextInput.get())
      {
         try
         {/* .write() - Adds data to Pipe */
            PIPE_OUT.write(BUTTON_ID);
            /* .flush() - Forces all pending writes */ 
            PIPE_OUT.flush();
            Erebus.pipeNextInput.set(false);
         }
         catch(IOException e)
         {
            System.out.println("Pipe Write Error");
            System.exit(0);
         }
      }
      else
      {
         if(-BUTTON_ID == MouseEvent.BUTTON1)
         {/* .compareAndSet() - Sets the value(True) only if currentValue(False)*/
            Erebus.CLICKED.compareAndSet(false,true);
         }
         /* .replace() - Sets The newValue(True) only if currently mapped and oldValue(False)*/
         Erebus.INPUT_STAT.replace(BUTTON_ID,false,true);
      }
   }
   public void mouseReleased(final MouseEvent EVENT) 
   { 
      final int BUTTON_ID = -EVENT.getButton();
      if(-BUTTON_ID == MouseEvent.BUTTON1)
      {
         Erebus.CLICKED.set(false);
      }
      /* .replace() - Sets the newValue(False) only if currently mapped */
      Erebus.INPUT_STAT.replace(BUTTON_ID,false);
   }
   public void mouseWheelMoved(final MouseWheelEvent EVENT)
   {
      Erebus.SCROLL.getAndAdd(EVENT.getWheelRotation());
   }
   public void focusLost(final FocusEvent EVENT)
   {
      Erebus.pause(true);
   }
   public void mouseMoved(final MouseEvent EVENT)
   {
      Erebus.MOUSE_X.set(EVENT.getX());
      Erebus.MOUSE_Y.set(EVENT.getY());
   }
   public void mouseDragged(final MouseEvent EVENT)
   {
      Erebus.MOUSE_X.set(EVENT.getX());
      Erebus.MOUSE_Y.set(EVENT.getY());
   }
   public void focusGained(final FocusEvent e){}
   public void keyTyped(final KeyEvent e){}
   public void mouseClicked(final MouseEvent e){}
   public void mouseEntered(final MouseEvent e){}
   public void mouseExited(final MouseEvent e){}
}