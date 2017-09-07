import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.AffineTransform;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.Image;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

public final class World
{
   /* Private Use Only */
   private static final Random R = new Random();
   
   /* Screen Size */
   public static final int XRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()); 
   public static final int YRES = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
   
   /* Millisec per frame (all speeds should be measured as pixels per mill-sec */
   public static int spf;
   
   
   /* World Gravity */
   public static double GRAV_ACCEL, FLOOR_FRIC, AIR_FRIC;
   /* Potions */
   public static int POT_WIDTH, POT_HEIGHT; 
   public static double POT_POW;
   /* Clouds */
   public static int CLOUD_WIDTH, CLOUD_HEIGHT;
   public static double WIND_SPEED, CLOUD_AMP, CLOUD_TURB;
   /* Rain */
   public static int RAIN_WIDTH, RAIN_HEIGHT;
   public static double RAIN_SPEED;
   /* Lightning */
   public static int BOLT_WIDTH; 
   public static double BOLT_SPEED;
   /* Satilites */
   public static int SAT_WIDTH, SAT_HEIGHT;  
   public static double SAT_SPEED;
   /* Platforms */
   public static int PLAT_LENGTH, PLAT_THICK;  
   public static double PLAT_RANGE, PLAT_MAX_SPEED;
   /* Coins */
   public static int COIN_WIDTH, COIN_HEIGHT; 
   public static double COIN_XSPEED, COIN_YSPEED;
   /* Characters */
   public static int DEFAULT_CHAR_WIDTH = (int)(.04*XRES), DEFAULT_CHAR_HEIGHT = (int)(.14*YRES);
   /* Projectiles */
   public static int DEFAULT_PROJ_WIDTH = (int)(.02*XRES), DEFAULT_PROJ_HEIGHT = (int)(.02*YRES);
   /* Blocks */
   public static int MAX_BLOCK_WIDTH, MAX_BLOCK_HEIGHT, MIN_BLOCK_WIDTH, MIN_BLOCK_HEIGHT;
   
   /* Images */
   public static ImageIcon skyDay,skyNight, groundDay, groundNight, grave, rain, coin, life, mana, ledge,block, cloud, cloudStorm, sun;
   public static final ImageIcon [] moon = new ImageIcon [8];
   
   /* Sounds */
   public static Clip storming, thunder, ambiance, smack, red, blue, bling;
   /* World Objects */
   public static final ArrayList<Avatar>     characters = new ArrayList<Avatar>();
   public static final ArrayList<Potion>     drinks     = new ArrayList<Potion>();
   public static final ArrayList<Rain>       shower     = new ArrayList<Rain>();
   public static final ArrayList<Projectile> shots      = new ArrayList<Projectile>();
   public static final ArrayList<Satilite>   sky        = new ArrayList<Satilite>();
   public static final ArrayList<Cloud>      clouds     = new ArrayList<Cloud>();
   public static final ArrayList<Lightning>  bolts      = new ArrayList<Lightning>();  
   public static final ArrayList<Clip>       sounds     = new ArrayList<Clip>(); 
   public static final ArrayList<Platform>   ledges     = new ArrayList<Platform>();
   public static final ArrayList<Blockage>   blocks     = new ArrayList<Blockage>();
   public static final ArrayList<Coin>       gold       = new ArrayList<Coin>();
   
   /* Input Output Method */
   public static final boolean [] keyStat = new boolean [600];
   public static final boolean [] mouse = new boolean [4];
   public static int mouseX;
   public static int mouseY;
   
   /* World State */
   public static final String absPath = new File("").getAbsolutePath();
   private final String path;
   public final String name;
   public static boolean raining, dayTime;
   public static final int [] sPIt = new int[50];
   
   public World(String title)
   {      
      /* World Folder */
      name = title;
      path = absPath + "\\Worlds\\"+name +"\\";
      
      this.setAttrib();
      this.setMedia();
      this.spawnBlocks(3,MAX_BLOCK_WIDTH, MAX_BLOCK_HEIGHT, MIN_BLOCK_WIDTH, MIN_BLOCK_HEIGHT);
      this.spawnClouds(8,100,300);
      this.spawnLedges(10);
      /* Sun and Moon add */
      sky.add(new Satilite(0,YRES, "sun"));
      sky.add(new Satilite(XRES,YRES, "moon"));
      
      /* Collision Sounds */
      smack = getSound(absPath +"\\SystemFiles\\hit.wav");
      
      
      mouseX = 0;
      mouseY = 0;
      raining = false;
      dayTime = true;
      spf = 1;
   }
   /* mutes all sound */
   public static void muteSound()
   {
      for(int i=0;i<World.sounds.size();i++)
      {
         BooleanControl muted = (BooleanControl) World.sounds.get(i).getControl(BooleanControl.Type.MUTE);
         muted.setValue(true);
         World.sounds.get(i).setFramePosition(World.sounds.get(i).getFramePosition());
         World.sounds.get(i).start();
      }
      
   }
   /* unmutes all sound */
   public static void unMuteSound()
   {
      for(int i=0;i<World.sounds.size();i++)
      {
         BooleanControl muted = (BooleanControl) World.sounds.get(i).getControl(BooleanControl.Type.MUTE);
         muted.setValue(false);
         World.sounds.get(i).setFramePosition(World.sounds.get(i).getFramePosition());
         World.sounds.get(i).start();
      }
      
   }
   private void spawnLedges(int num)
   {
      /* Platform add */
      for(int i=0;i<10;i++)
      {
         World.ledges.add(new Platform(R.nextInt(World.XRES - World.PLAT_LENGTH),R.nextInt(World.YRES),(R.nextInt(2) == 0)? true:false,World.PLAT_RANGE, R.nextInt(3)/10.0));
      }
   }
   private void spawnClouds(int num, int maxHeight, int minHeight)
   {
      /* Cloud add */
      for(int i=0;i<num;i++)
      {
         clouds.add(new Cloud(R.nextInt(XRES - CLOUD_WIDTH),R.nextInt(minHeight-CLOUD_HEIGHT)+maxHeight, R.nextInt(360)));
      }
   }
   private void spawnBlocks(int num, int maxWidth, int maxHeight, int minWidth, int minHeight)
   {
      /* Block add */
      for(int i=0;i<num;i++)
      {
         int tempW = R.nextInt(maxWidth - minWidth) + minWidth;
         int tempH = R.nextInt(maxHeight - minHeight) + minHeight;
         Blockage maskB = new Blockage(R.nextInt(World.XRES - tempW),R.nextInt(World.YRES - tempH),tempW,tempH);
         blocks.add(maskB);
      }
   }
   private void setAttrib()
   {
      /* Loads World Variables */
      String [] attributes = readFile(path + "Attributes.txt");
      if(attributes.length<25)
      {
         System.out.println("Not enough World attributes");
         System.exit(0);
      }
      GRAV_ACCEL     =       YRES * Double.valueOf(attributes[0].substring(0,attributes[0].indexOf("|")));
      FLOOR_FRIC     =       XRES * Double.valueOf(attributes[1].substring(0,attributes[1].indexOf("|")));
      AIR_FRIC       =       XRES * Double.valueOf(attributes[2].substring(0,attributes[2].indexOf("|")));
      POT_WIDTH      = (int)(XRES * Double.valueOf(attributes[3].substring(0,attributes[3].indexOf("|"))));
      POT_HEIGHT     = (int)(YRES * Double.valueOf(attributes[4].substring(0,attributes[4].indexOf("|"))));
      POT_POW        =       XRES * Double.valueOf(attributes[5].substring(0,attributes[5].indexOf("|")));
      CLOUD_WIDTH    = (int)(XRES * Double.valueOf(attributes[6].substring(0,attributes[6].indexOf("|")))); 
      CLOUD_HEIGHT   = (int)(YRES * Double.valueOf(attributes[7].substring(0,attributes[7].indexOf("|")))); 
      RAIN_WIDTH     = (int)(XRES * Double.valueOf(attributes[8].substring(0,attributes[8].indexOf("|"))));
      RAIN_HEIGHT    = (int)(YRES * Double.valueOf(attributes[9].substring(0,attributes[9].indexOf("|"))));
      RAIN_SPEED     =       YRES * Double.valueOf(attributes[10].substring(0,attributes[10].indexOf("|")));
      BOLT_WIDTH     = (int)(XRES * Double.valueOf(attributes[11].substring(0,attributes[11].indexOf("|"))));
      BOLT_SPEED     =       YRES * Double.valueOf(attributes[12].substring(0,attributes[12].indexOf("|")));
      SAT_WIDTH      = (int)(XRES * Double.valueOf(attributes[13].substring(0,attributes[13].indexOf("|"))));
      SAT_HEIGHT     = (int)(YRES * Double.valueOf(attributes[14].substring(0,attributes[14].indexOf("|"))));
      SAT_SPEED      =              Double.valueOf(attributes[15].substring(0,attributes[15].indexOf("|")));
      PLAT_LENGTH    = (int)(XRES * Double.valueOf(attributes[16].substring(0,attributes[16].indexOf("|"))));
      PLAT_THICK     = (int)(YRES * Double.valueOf(attributes[17].substring(0,attributes[17].indexOf("|"))));
      PLAT_RANGE     =       XRES * Double.valueOf(attributes[18].substring(0,attributes[18].indexOf("|")));
      PLAT_MAX_SPEED =              Double.valueOf(attributes[19].substring(0,attributes[19].indexOf("|"))); 
      COIN_WIDTH     = (int)(XRES * Double.valueOf(attributes[20].substring(0,attributes[20].indexOf("|"))));
      COIN_HEIGHT    = (int)(YRES * Double.valueOf(attributes[21].substring(0,attributes[21].indexOf("|"))));
      COIN_XSPEED    =       XRES * Double.valueOf(attributes[22].substring(0,attributes[22].indexOf("|")));
      COIN_YSPEED    =       YRES * Double.valueOf(attributes[23].substring(0,attributes[23].indexOf("|")));
      WIND_SPEED     =       XRES * Double.valueOf(attributes[24].substring(0,attributes[24].indexOf("|")));
      CLOUD_AMP = YRES *.004;
      CLOUD_TURB = .02;
      MAX_BLOCK_WIDTH = 500;
      MAX_BLOCK_HEIGHT = 500;
      MIN_BLOCK_WIDTH = 100;
      MIN_BLOCK_HEIGHT = 100;
   }
   private void setMedia()
   {
      /* Loads World -- Default invisible and silent */
      String [] folder = new File(path).list();
      /* World Sounds */
      ambiance = null;
      storming = null;
      thunder  = null;
      red = null;
      blue = null;
      bling = null;
      /* World Images */
      ImageIcon blank = new ImageIcon("");
      skyDay = blank;
      skyNight = blank;
      groundDay = blank;
      groundNight = blank;
      grave = blank;
      rain = blank;
      coin = blank;
      life = blank;
      mana = blank;
      ledge = blank;
      block = blank;
      cloud = blank;
      cloudStorm = blank;
      sun = blank;
      for(int i=0;i<moon.length;i++)
      {
         moon[i] = blank;
      }
      for(String f : folder)
      {
         int end = f.lastIndexOf(".");
         if(end<1)
         {
            System.out.println("Skipped Irregular File -- " + f);
         }
         else
         {
            String fTitle = f.substring(0,end);
            String fChar = f.substring(0,1);
            if(fChar.equalsIgnoreCase("m"))
            {
               if(fTitle.equalsIgnoreCase("mana_pot"))
               {
                  mana = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("mana"))
               {
                  blue = getSound(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon"))
               {
                  moon[0] = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_rw"))
               {
                  moon[1] = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_rh"))
               {
                  moon[2] = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_rc"))
               {
                  moon[3] = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_lc"))
               {
                  moon[4]= new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_lh"))
               {
                  moon[5] = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("moon_lw"))
               {
                  moon[6] = new ImageIcon(path+f);
               }
            }
            else if(fChar.equalsIgnoreCase("c"))
            {
               if(fTitle.equalsIgnoreCase("charDead"))
               {
                  grave = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("coin"))
               {
                  coin = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("cloud"))
               {
                  cloud = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("cloudRain"))
               {
                  cloudStorm = new ImageIcon(path+f);
               }
            }
            else if(fChar.equalsIgnoreCase("b"))
            {
               if(fTitle.equalsIgnoreCase("backgroundDay"))
               {
                  skyDay = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("backgroundNight"))
               {
                  skyNight = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("block"))
               {
                  block = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("bling"))
               {
                  bling = getSound(path+f);
               }
            }
            else if(fChar.equalsIgnoreCase("f"))
            {
               if(fTitle.equalsIgnoreCase("foregroundDay"))
               {
                  groundDay = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("foregroundNight"))
               {
                  groundNight = new ImageIcon(path+f);
               }
            }
            else if(fChar.equalsIgnoreCase("l"))
            {
               if(fTitle.equalsIgnoreCase("life_pot"))
               {
                  life = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("ledge"))
               {
                  ledge = new ImageIcon(path+f);
               }
               else if(fTitle.equalsIgnoreCase("life"))
               {
                  red = getSound(path+f);
               }
            }
            else if(fChar.equalsIgnoreCase("r"))
            {
               if(fTitle.equalsIgnoreCase("rain"))
               {
                  rain = new ImageIcon(path+f);
               }
               else if(f.equals("raining.wav"))
               {
                  storming = getSound(path+f);
               }
            }
            else
            {
               if(f.equals("ambiance.wav"))
               {
                  ambiance = getSound(path+f);
               }
               else if(fTitle.equalsIgnoreCase("sun"))
               {
                  sun = new ImageIcon(path+f);
               }
               else if(f.equals("thunder.wav"))
               {
                  thunder = getSound(path+f);
               }
            }    
         }
      }
      if(ambiance == null)   {getSound("Ambiance");}
      if(storming == null)   {getSound("Raining");}
      if(thunder  == null)   {getSound("Thunder");}
      if(red == null)        {getSound("Life");}
      if(blue == null)       {getSound("Mana");}
      if(bling == null)      {getSound("Bling");}
      if(skyDay.equals(blank))     {System.out.println("Image Not Found --> SkyDay");}
      if(skyNight.equals(blank))   {System.out.println("Image Not Found --> SkyNight");}
      if(groundDay.equals(blank))  {System.out.println("Image Not Found --> GroundDay");}
      if(groundNight.equals(blank)){System.out.println("Image Not Found --> GroundNight");}
      if(grave.equals(blank))      {System.out.println("Image Not Found --> Grave");}
      if(rain.equals(blank))       {System.out.println("Image Not Found --> Rain");}
      if(coin.equals(blank))       {System.out.println("Image Not Found --> Coin");}
      if(life.equals(blank))       {System.out.println("Image Not Found --> Life_Pot");}
      if(mana.equals(blank))       {System.out.println("Image Not Found --> Mana_Pot");}
      if(ledge.equals(blank))      {System.out.println("Image Not Found --> Ledge");}
      if(block.equals(blank))      {System.out.println("Image Not Found --> Block");}
      if(cloud.equals(blank))      {System.out.println("Image Not Found --> Cloud");}
      if(cloudStorm.equals(blank)) {System.out.println("Image Not Found --> RainCloud");}
      if(sun.equals(blank))        {System.out.println("Image Not Found --> Sun");}
      for(int i=0;i<moon.length;i++)
      {
         if(moon[i].equals(blank)) {System.out.println("Image Not Found --> Moon Phase "+i);}
      } 
   }
   public static void reset()
   {
      /* World Objects */
      for(Avatar a : characters)
      {
         a.reset();
      }
      for(Satilite s : sky)
      {
         s.reset();
      }
      for(Blockage b: blocks)
      {
         b.reset();
      }
      drinks.clear();
      shower.clear();
      shots.clear();
      bolts.clear();  
      gold.clear();
      raining = false;
   }
   /* returns avg val of an int array */
   public static int getAvg(int [] nums)
   {
      if(nums.length == 0)
      {
         System.out.println("Can't find Average of Nothing");
         return 1;
      }
      int sum = 0;
      for(int i : nums)
      {
         sum += i;
      }
      sum = Math.round(sum/nums.length);
      if(sum ==0)
      {
         return 1;
      }
      return sum;
   }
   /* gets sound clip from path */
   public static Clip getSound(String path)
   {
      try
      {         
         AudioInputStream sound = AudioSystem.getAudioInputStream(new File(path));
         DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
         Clip clip = (Clip) AudioSystem.getLine(info);
         clip.open(sound);
         clip.setFramePosition(clip.getFrameLength());
         sounds.add(clip);
         return clip;
      }
      catch(LineUnavailableException | UnsupportedAudioFileException | IOException e)
      {
         System.out.println("Audio Not Found -------> " + path);
         System.exit(0);
         return null;
      }
   }
   /* stors a text file as a String array */
   public static String[] readFile(String fileName)
   {
      ArrayList<String> contents  = new ArrayList<String>();
      try
      {
         Scanner s = new Scanner(new File(fileName));
         while(s.hasNextLine())
         {
            contents.add(s.nextLine());
         }
      }
      catch (FileNotFoundException e)
      {
         System.out.println("File not Found -- " + fileName);
         System.exit(0);
      }
      return contents.toArray(new String[contents.size()]);
   }
   /* Stores file names within this directory in a string array */
   public static String[] getDirs(String location)
   {
      ArrayList<String> contents = new ArrayList<String>();
      for(File f : new File(location).listFiles())
      {
         if(f.isDirectory())
         {
            contents.add(f.getName());
         }
      }
      return contents.toArray(new String[contents.size()]);
   }
   /* rain handling */
   public static void rain()
   {     
      if(raining)
      {
         /* Rain per Second */
         for(Cloud c : clouds)
         {
            if(R.nextInt(100/spf) == 0)
            {
               shower.add(new Rain((int)c.getX() + R.nextInt(CLOUD_WIDTH - RAIN_WIDTH),(int)c.getY() + CLOUD_HEIGHT/2));
            }
         }
         /* Random Lightning Strike */
         if(R.nextInt(5000/spf) == 0)
         {
            int aCloud = R.nextInt(clouds.size());
            thunder.setFramePosition(0);
            thunder.start();
            bolts.add(new Lightning((int)clouds.get(aCloud).getX() + R.nextInt(CLOUD_WIDTH - BOLT_WIDTH) ,(int) clouds.get(aCloud).getY() + CLOUD_HEIGHT/2 ));
         }
         /* Random End Rain */
         if(R.nextInt(10000/spf) == 0)
         {
            raining = false;
            storming.stop();
            storming.setFramePosition(0);
         }
      }
      else
      {
         storming.stop();
         storming.setFramePosition(0);
         /* Random Start Rain */
         if(R.nextInt(50000/spf) == 0)
         {
            raining = true;
            storming.loop(Clip.LOOP_CONTINUOUSLY);
         }
      }
   }
   /* Loot from sky handling */
   public static void randDrop()
   {
      if(R.nextInt(5000/spf) == 0)
      {
         /* Life Potion */
         drinks.add(new Potion("life",R.nextInt(XRES - POT_WIDTH),-POT_HEIGHT));
      }
      if(R.nextInt(2000/spf) == 0)
      {
         /* Mana Potion */
         drinks.add(new Potion("mana",R.nextInt(XRES - POT_WIDTH),-POT_HEIGHT));
      }
      if(R.nextInt(2000/spf) == 0)
      {
         lootDrop(R.nextInt(XRES - COIN_WIDTH),-COIN_HEIGHT,1);
      }
   }
   /* reads image file from path and converts this to an area devoid of transparent sections */
   public static Area convertToArea(String path, int w, int h)
   {
      Area shape = new Area();
      try
      {
         Image scaled = ImageIO.read(new File(path)).getScaledInstance(w,h,Image.SCALE_FAST);
         BufferedImage image = new BufferedImage(scaled.getWidth(null),scaled.getHeight(null),BufferedImage.TYPE_INT_ARGB);
         image.getGraphics().drawImage(scaled,0,0,null);
         for(int r = 0; r < image.getHeight(); r ++) 
         {
            for(int c = 0; c < image.getWidth(); c ++)
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
                  shape.add(new Area(new Rectangle(start,r,end-start,1)));
               }
            }
         }
      }
      catch(IOException e)
      {
         System.out.println("No Image -----> "+ path);
      }
      return shape;

   }
   /* moves area */
   public static Area translateArea(Area a, int newX, int newY)
   {
      Area old = a;
      Rectangle bound = old.getBounds();
      int xTran = newX - (int)bound.getX();
      int yTran = newY - (int)bound.getY();
      AffineTransform t = new AffineTransform();
      t.translate(xTran,yTran);
      old.transform(t);
      return old;
   }
   /* returns a horizontaly mirrored copy of an area */
   public static Area flipHArea(Area a)
   {
      Area old = a;
      AffineTransform t = new AffineTransform();
      t.scale(-1,1);
      old.transform(t);
      return old;
   }
   /* Droped coin handling */
   public static void lootDrop(int x, int y, int max)
   {
      int drops = R.nextInt(max)+1;
      for(int i=0;i<drops;i++)
      {
         gold.add(new Coin(x,y,R.nextInt(360)));
      }
   }
   /* gets an array of icons from a specified drectory with the specified names */
   public static ImageIcon[] getIcons(String dir,String[] locations)
   {
      int locs = locations.length;
      ImageIcon[] cache = new ImageIcon[locs];
      for(int i=0;i<locs;i++)
      {
         String thisPath = dir + locations[i];
         String [] folder = new File(thisPath).list();
         for(String fil : folder)
         {
            int end = fil.lastIndexOf(".");
            if(end<1)
            {
               System.out.println("Skipped Irregular File -- " + thisPath + fil);
            }
            else
            {
               String fTitle = fil.substring(0,end);
               if(fTitle.equalsIgnoreCase(locations[i]))
               {
                  cache[i] = new ImageIcon(thisPath+"\\"+fil);
               }
            }
         }
         if(cache[i]==null)
         {
            cache[i] = new ImageIcon("");
            System.out.println("File Not Loaded ------> "+thisPath+"\\"+locations[i]);
         }
      }
      return cache;
   }
   /* gets a double array of icons from a specified drectory with the specified names per location */
   public static ImageIcon[][] getIcons(String dir,String[] locations,String[] needToLoad)
   {
      int locs = locations.length;
      int ntls = needToLoad.length;
      ImageIcon[][] cache = new ImageIcon[locs][ntls];
      for(int i=0;i<locs;i++)
      {
         String thisPath = dir + locations[i];
         String [] folder = new File(thisPath).list();
         for(String fil : folder)
         {
            int end = fil.lastIndexOf(".");
            if(end<1)
            {
               System.out.println("Skipped Irregular File -- " + thisPath + fil);
            }
            else
            {
               String fTitle = fil.substring(0,end);
               for(int j=0;j<ntls;j++)
               {
                  if(fTitle.equalsIgnoreCase(needToLoad[j]))
                  {
                     cache[i][j] = new ImageIcon(thisPath+"\\"+fil);
                  }
               }
            }
         }
         for(int j=0;j<ntls;j++)
         {
            if(cache[i][j]==null)
            {
               cache[i][j] = new ImageIcon("");
               System.out.println("File Not Loaded ------> "+thisPath+"\\"+needToLoad[j]);
            }
         }
      }
      return cache;
   }
   /* delays thread */
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





final class Potion extends PhysicsObject
{
   /* Time Since Creation */
   private int existed;
   
   /* Type of Potion */
   public final String type;
   
   
   public Potion(String kind, int startX, int startY)
   {
      super(startX,startY,0,0,World.FLOOR_FRIC,0,World.POT_WIDTH,World.POT_HEIGHT);
            
      /* Type of Potion */
      type = kind;
      
      existed = 0;
   }
   public double getAge()
   {
      return existed;
   }
   /* iteration handling */
   public void move()
   {
      existed++;
      super.applyGravity();
      super.collisionDetectY();
      super.keepYBounds();
      
      super.applyGroundMove();
      super.collisionDetectX();
      super.keepXBounds();
   }
}





final class Coin extends PhysicsObject
{   
   /* time since drop*/
   private int existed;

   public Coin(int x, int y, int angle)
   {
      super(x,y,World.COIN_XSPEED * Math.cos(Math.toRadians(angle)),World.COIN_YSPEED * Math.sin(Math.toRadians(angle)),World.FLOOR_FRIC,0,World.COIN_WIDTH,World.COIN_HEIGHT);
      /* Time Based */
      existed = 0;
   }
   public double getAge()
   {
      return existed;
   }
   /* iteration handling */
   public void move()
   {
      existed++;
      super.applyGravity();
      super.collisionDetectY();
      super.keepYBounds();
      
      super.applyXMove();
      super.applyGroundMove();
      super.collisionDetectX();
      super.keepXBounds();
   }
}





final class Lightning extends PhysicsObject
{
   /* original placement */
   private final int startY;
   
   public Lightning(int x, int y)
   {
      super(x,y,0,World.BOLT_SPEED,0,0,World.BOLT_WIDTH,0);
      startY = y;
   }
   /* iteration handling */
   public void move()
   {
      super.applyTermVel();
      super.collisionDetectY();
      super.height += super.yPos-startY;
      super.yPos = startY;
      if(super.yPos + super.height >= super.bottomBound)
      {
         World.lootDrop((int)(super.xPos+(super.width/2)),(int)(super.yPos+super.height-(2*World.COIN_HEIGHT)),10);
         for(Blockage b: World.blocks)
         {
            b.hit((int)(super.xPos+(super.width/2)),super.bottomBound, 4*super.width);
         }
         World.bolts.remove(this);
      }
   }
}





final class Platform
{
   /* Location */
   private double xPos,yPos;
   private final int xOrig, yOrig;
   
   /* Movement */
   private boolean direction;
   private double speed; 
   private double range;
   
   
   public Platform(int x, int y, boolean right, double domain, double vel)
   {
      /* Location */
      xPos = x;
      yPos = y;
      xOrig = x;
      yOrig = y;
      
      
      /* Moving */
      direction = right;
      speed = vel;
      range = domain;
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   public double getS()
   {
      return speed;
   }
   public boolean getD()
   {
      return direction;
   }
   /* iteration handling */
   public void move()
   {
      if(direction)
      {
         xPos += speed*World.spf;
         if(xPos > xOrig + range)
         {
            xPos = xOrig + range;
            direction = false;
         }
      }
      else
      {
         xPos -= speed*World.spf;
         if(xPos < xOrig - range)
         {
            xPos = xOrig - range;
            direction = true;
         }
      }
   }
}





final class Blockage
{
   /* origins */
   private final int origX,origY,origW,origH;
   /* position and size*/
   private int xPos, yPos, width, height;
   /* shape of object */
   private Area core; 
   public Blockage(int x, int y, int wide, int tall)
   {
      xPos = x;
      origX = x;
      yPos = y;
      origY = y;
      width = wide;
      origW = wide;
      height = tall;
      origH = tall;
      core = new Area(new Rectangle(x,y,wide,tall));
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return xPos;
   }
   public double getW()
   {
      return width;
   }
   public double getH()
   {
      return height;
   }
   public Area getCore()
   {
      return core;
   }
   /* removes chunk when hit */
   public void hit(int x,int y, int radius)
   {
      core.subtract(new Area(new Ellipse2D.Double(x-radius,y-radius,radius*2,radius*2)));
      Rectangle frame = core.getBounds();//core.subtract(new Area(new Rectangle2D.Double(x-radius,y-radius,radius*2,radius*2)));
      xPos   = (int)frame.getX();
      yPos   = (int)frame.getY();
      width  = (int)frame.getWidth();
      height = (int)frame.getHeight();
   }
   /* returns to original state */
   public void reset()
   {
      xPos = origX;
      yPos = origY;
      width = origW;
      height = origH;
      core = new Area(new Rectangle(origX,origY,origW,origH));
   }
}





final class Rain extends PhysicsObject
{   
   public Rain(int x, int y)
   {
      super(x,y,World.WIND_SPEED,World.RAIN_SPEED,0,0,World.RAIN_WIDTH,World.RAIN_HEIGHT);
   }
   /* iteration handling */
   public void move()
   {
      super.applyTermVel();
      super.collisionDetectY();
      
      super.applyXMove();
      super.collisionDetectX();
      if(super.yPos + super.height >= super.bottomBound)
      {
         World.shower.remove(this);
      }
      else if(super.xPos + super.width < 0 || super.xPos > World.XRES)
      {
         World.shower.remove(this);
      }
   }
}





final class Cloud
{
   /* Location */
   private double xPos,yPos;
   private double theta, origY;
   
   
   public Cloud(int x, int y,int angle)
   {
      /* Location */
      xPos = x;
      yPos = y;
      theta = Math.toRadians(angle);
      origY = y;
      
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   /* iteration handling */
   public void move()
   {
      xPos = xPos + World.WIND_SPEED*World.spf;
      yPos = origY + World.CLOUD_AMP*Math.cos(theta)*World.spf;
      theta = theta + Math.toRadians(World.CLOUD_TURB)*World.spf;
      /* Bounds 0-360 */
      if(Math.toDegrees(theta) > 360)
      {
         theta = 0;
      }
      if(xPos > World.XRES)
      {
         xPos  = 0;
      }
   }
}





final class Satilite
{
   private final int beginX, beginY, beginA;
   
   /* Location */
   private double xPos,yPos,angle;
   private int month;
   private double polX,polY;
   private final double radius;
   /* Sun vs. Moon */
   public final String kind;
   
   public Satilite(int x, int y, String type)
   {
      beginX = x;
      beginY = y;
      
      /* Location */
      xPos = x - World.SAT_WIDTH/2;
      yPos = y - World.SAT_HEIGHT/2;
      
      /* Loc from center */
      polX = x - ((World.XRES)/2);
      polY = World.YRES - y;
      radius = Math.sqrt(polX * polX + polY * polY);
      if(polX == 0)
      {
         /* Straight down */
         if(polY<0)
         {
            angle = 270;
         }
         /* Striaght up */
         else
         {
            angle = 90;
         }
      }
      else
      {
         angle = Math.toDegrees(Math.atan(polY/polX));
         /* 2nd and 3rd Quads */
         if(polX < 0)
         {
            angle += 180;
         }
         /* 4th Quad */
         else if(polY < 0)
         {
            angle += 360;
         }
      }
      /* Sun vs. Moon */
      kind = type;
      /* Pos in Moon Cycle */
      month = 0;
      beginA = (int)angle;
   }
   public double getAngle()
   {
      return angle;
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   public int getMonth()
   {
      return month;
   }
   /* sets original stats */
   public void reset()
   {
      month = 0;
      angle = beginA;
      xPos = beginX;
      yPos = beginY;
   }
   /* iteration handling */
   public void move()
   {
      angle -= World.SAT_SPEED*World.spf;
      
      /* Bounds 0-360 */
      if(angle < 0)
      {
         /* Next Moon */
         if(!kind.equals("sun"))
         {
            month++;
            if(month > 7)
            {
               month = 0;
            }
         }
         angle += 360;
      }
      /* Day if Sun is up */
      if(kind.equals("sun"))
      {
         if(angle > 0 && angle < 180)
         {
            World.dayTime = true;
         }
      }
      double radAngle = Math.toRadians(angle);
      polX = (radius*Math.cos(radAngle));
      polY = (radius*Math.sin(radAngle));
      xPos = World.XRES/2 + polX - World.SAT_WIDTH/2;
      yPos = World.YRES - polY - World.SAT_HEIGHT/2;
   }
}




// Handles keys
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




// handles mouse
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
