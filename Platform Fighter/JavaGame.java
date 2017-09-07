import java.util.Random;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.MouseInfo;
import java.awt.Color;
import java.awt.image.BufferStrategy;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Rectangle;
import java.awt.RadialGradientPaint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Image;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.sound.sampled.Clip;


public class JavaGame
{
   public static void main(String[] args)
   {
      /* Utility Setup */
      Random r = new Random();
      /* Frame Setup */
      JFrame f = new JFrame("Java Game");
      f.setSize(World.XRES,World.YRES);
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.addKeyListener(new KeyController());
      f.addMouseListener(new MouseController());
      f.setResizable(false);
      f.setUndecorated(true);
      f.setVisible(true);
      /* Graphics Setup */
      f.createBufferStrategy(2);
      BufferStrategy bs = f.getBufferStrategy();
      
      /* World Insdtantiation */
      World world = null;
      Image sOn    = new ImageIcon(World.absPath + "\\Systemfiles\\Sound_On.png").getImage();
      Image sOff   = new ImageIcon(World.absPath + "\\Systemfiles\\Sound_Off.png").getImage();
      Image replay = new ImageIcon(World.absPath + "\\Systemfiles\\Replay.png").getImage();
      Image quit   = new ImageIcon(World.absPath + "\\Systemfiles\\Quit.png").getImage();
      int audioSize = 60;
      int audioX = World.XRES-audioSize;
      int audioY = World.YRES-audioSize;
      boolean audioOn = true;
      /* Menu Loop --- To dispose variables */
      for(int z = 0;z<1;z++)
      {
         Font style = new Font("",Font.BOLD,18);
         int worldIcoSize = 300;
         int charIcoW = World.DEFAULT_CHAR_WIDTH*2;
         int charIcoH = World.DEFAULT_CHAR_HEIGHT*2;
         int projIcoW = World.DEFAULT_PROJ_WIDTH*2;
         int projIcoH = World.DEFAULT_PROJ_HEIGHT*2;
         int textOffset = 100;
         int rollover = 10;
         int halfRoll = 5;
         
         /* Loads List of Potential Worlds and their Icons */
         String worldsPath = World.absPath + "\\Worlds\\";
         String [] potentialWorlds = World.getDirs(worldsPath);
         int numPWs = potentialWorlds.length;
         int minusOne = numPWs-1;
         int halfIco = worldIcoSize/2;
         int xIco = World.XRES/2 - halfIco - halfIco*minusOne;
         int yIco = World.YRES/2 - halfIco;
         int hIco = yIco + worldIcoSize;
         ImageIcon [][] worldIcons = World.getIcons(worldsPath, potentialWorlds,new String[]{"backgroundDay","foregroundDay"});
         /* The Selection Menu for the World */
         WorldMenu:
         while(true)
         {
            if(World.keyStat[KeyEvent.VK_ESCAPE])
            {
               System.exit(0);            
            }
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            Point screen = f.getLocationOnScreen();
            World.mouseX = (int)(mouse.getX() - screen.getX());
            World.mouseY = (int)(mouse.getY() - screen.getY());
            if(World.mouse[MouseEvent.BUTTON1])
            {
               World.mouse[MouseEvent.BUTTON1] = false;
               if(World.mouseX> audioX && World.mouseX<World.XRES && World.mouseY>audioY && World.mouseY < World.YRES)
               {
                  audioOn = !audioOn;
               }
               else
               {
                  for(int i=0;i<numPWs;i++)
                  {
                     int currentIcoX = xIco + worldIcoSize*i;
                     if(World.mouseX > currentIcoX && World.mouseX < currentIcoX + worldIcoSize)
                     {
                        if(World.mouseY > yIco && World.mouseY < hIco)
                        {
                           world = new World(potentialWorlds[i]);
                           break WorldMenu;
                        }
                     }
                  }
               }
            }
            do
            {
               do
               {
                  Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
                  //g2D.fillRect(0,0,World.XRES,World.YRES);
                  g2D.setColor(Color.WHITE);
                  g2D.setFont(style);
                  String text = "Select Your World";
                  int widthTxT = g2D.getFontMetrics().stringWidth(text);
                  g2D.drawString(text,World.XRES/2-widthTxT/2,yIco-textOffset);
                  g2D.drawImage(audioOn? sOn:sOff,audioX,audioY,audioSize,audioSize,null);
                  for(int i=0;i<numPWs;i++)
                  {
                     int currentIcoX = xIco + worldIcoSize*i;
                     if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+worldIcoSize && World.mouseY> yIco && World.mouseY< hIco)
                     {
                        int rollSize = worldIcoSize+rollover;
                        int rollX = currentIcoX-halfRoll;
                        int rollY = yIco-halfRoll;
                        g2D.drawImage(worldIcons[i][0].getImage(),rollX,rollY,rollSize,rollSize,null);
                        g2D.drawImage(worldIcons[i][1].getImage(),rollX,rollY,rollSize,rollSize,null);
                     }
                     else
                     {
                        g2D.drawImage(worldIcons[i][0].getImage(),currentIcoX,yIco,worldIcoSize,worldIcoSize,null);
                        g2D.drawImage(worldIcons[i][1].getImage(),currentIcoX,yIco,worldIcoSize,worldIcoSize,null);
                     }
                  }
                  g2D.dispose();
               }while(bs.contentsRestored());
               bs.show();
            }while(bs.contentsLost());
         }
         /* Loads List of Potential Characters for Selected World  */
         String charsPath = World.absPath+"\\Characters\\";
         String [] potentialCharacters = World.readFile(worldsPath+world.name+"\\PotentialCharacters.txt");
         ImageIcon [] charIcons = World.getIcons(charsPath,potentialCharacters);
         int numPCs = potentialCharacters.length;
         minusOne = numPCs -1;
         
         int halfIcoW = charIcoW/2;
         int halfIcoH = charIcoH/2;
         xIco = World.XRES/2 - halfIcoW - halfIcoW*minusOne;
         yIco = World.YRES/2 - halfIcoH;
         hIco = yIco + charIcoH;
         /* Selection Menu for Characters */
         int charAdd = 0;
         int [] selectedChar = new int [2];
         CharacterMenu:
         while(true)
         {
            if(World.keyStat[KeyEvent.VK_ESCAPE])
            {
               System.exit(0);            
            }
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            Point screen = f.getLocationOnScreen();
            World.mouseX = (int)(mouse.getX() - screen.getX());
            World.mouseY = (int)(mouse.getY() - screen.getY());
            if(World.mouse[MouseEvent.BUTTON1])
            {
               World.mouse[MouseEvent.BUTTON1] = false;
               if(World.mouseX> audioX && World.mouseX<World.XRES && World.mouseY>audioY && World.mouseY < World.YRES)
               {
                  audioOn = !audioOn;
               }
               for(int i=0;i<numPCs;i++)
               {
                  int currentIcoX = xIco + charIcoW*i;
                  if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+charIcoW && World.mouseY> yIco && World.mouseY< hIco)
                  {
                     selectedChar [charAdd] = i;
                     charAdd++;
                     if(charAdd == 2)
                     {
                        charAdd = 0;
                        break CharacterMenu;
                     }
                     break;
                  }
               }
            }
            do
            {
               do
               {
                  Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
                  g2D.fillRect(0,0,World.XRES,World.YRES);
                  g2D.setColor(Color.WHITE);
                  g2D.setFont(style);
                  String text = "Player "+ (charAdd+1) + " Select Your Avatar";
                  int widthTxT = g2D.getFontMetrics().stringWidth(text);
                  g2D.drawString(text,World.XRES/2-widthTxT/2,yIco-textOffset);
                  g2D.drawImage(audioOn? sOn:sOff,audioX,audioY,audioSize,audioSize,null);
                  for(int i=0;i<numPCs;i++)
                  {
                     int currentIcoX = xIco + charIcoW*i;
                     if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+charIcoW && World.mouseY> yIco && World.mouseY< hIco)
                     {
                        int rollW = charIcoW+rollover;
                        int rollH = charIcoH+rollover;
                        int rollX = currentIcoX-halfRoll;
                        int rollY = yIco-halfRoll;
                        g2D.drawImage(charIcons[i].getImage(),rollX,rollY,rollW,rollH,null);
                     }
                     else
                     {
                        g2D.drawImage(charIcons[i].getImage(),currentIcoX,yIco,charIcoW,charIcoH,null);
                     }
                  }
                  g2D.dispose();
               }while(bs.contentsRestored());
               bs.show();
            }while(bs.contentsLost());
         }
         /* Loads Lists of Potential Projectiles for Selected Characters */
         String projsPath = World.absPath+"\\Projectiles\\";
         String [] potentialProjectiles1 = World.readFile(charsPath+ potentialCharacters[selectedChar[0]]+"\\PotentialProjectiles.txt");
         String [] potentialProjectiles2 = World.readFile(charsPath+ potentialCharacters[selectedChar[1]]+"\\PotentialProjectiles.txt");
         
         ImageIcon [] projIcons1 = World.getIcons(projsPath,potentialProjectiles1);
         ImageIcon [] projIcons2 = World.getIcons(projsPath,potentialProjectiles2);
         int numPP1s = projIcons1.length;
         int numPP2s = projIcons2.length;
         int minusOne1 = numPP1s-1;
         int minusOne2 = numPP2s-1;
         halfIcoW = projIcoW/2;
         halfIcoH = projIcoH/2;
         int xIco1 = World.XRES/2 - halfIcoW - halfIcoW*minusOne1;
         int xIco2 = World.XRES/2 - halfIcoW - halfIcoW*minusOne2;
         yIco = World.YRES/2 - halfIcoH;
         hIco = yIco + projIcoH;
         /* Selection Menu for Projectiles */
         ProjectileMenu:
         while(true)
         {
            if(World.keyStat[KeyEvent.VK_ESCAPE])
            {
               System.exit(0);            
            }
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            Point screen = f.getLocationOnScreen();
            World.mouseX = (int)(mouse.getX() - screen.getX());
            World.mouseY = (int)(mouse.getY() - screen.getY());
            if(World.mouse[MouseEvent.BUTTON1])
            {
               World.mouse[MouseEvent.BUTTON1] = false;
               if(World.mouseX> audioX && World.mouseX<World.XRES && World.mouseY>audioY && World.mouseY < World.YRES)
               {
                  audioOn = !audioOn;
               }
               if(charAdd == 0)
               {
                  for(int i=0;i<numPP1s;i++)
                  {
                     int currentIcoX = xIco1+projIcoW*i;
                     if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+projIcoW && World.mouseY > yIco && World.mouseY < hIco)
                     {
                        charAdd++;
                        World.characters.add(new Avatar("Player 1",potentialCharacters[selectedChar[0]],0,-World.DEFAULT_CHAR_HEIGHT,new int []{KeyEvent.VK_A,KeyEvent.VK_D,KeyEvent.VK_W,KeyEvent.VK_S},potentialProjectiles1[i]));
                        break;
                     }
                  }
               }
               else
               {
                  for(int i=0;i<numPP2s;i++)
                  {
                     int currentIcoX = xIco2+projIcoW*i;
                     if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+projIcoW && World.mouseY > yIco && World.mouseY <hIco)
                     {
                        World.characters.add(new Avatar("Player 2",potentialCharacters[selectedChar[1]],World.XRES-World.DEFAULT_CHAR_WIDTH,-World.DEFAULT_CHAR_HEIGHT,new int []{KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT,KeyEvent.VK_UP,KeyEvent.VK_DOWN},potentialProjectiles2[i]));
                        break ProjectileMenu;
                     }
                  }
               }
               
            }
            do
            {
               do
               {
                  Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
                  g2D.fillRect(0,0,World.XRES,World.YRES);
                  g2D.setColor(Color.WHITE);
                  g2D.setFont(style);
                  String text = "Player "+(charAdd+1)+" Select Your Weapon";
                  int widthTxT = g2D.getFontMetrics().stringWidth(text);
                  g2D.drawString(text,World.XRES/2-widthTxT/2,yIco-charIcoH-textOffset*2);
                  g2D.drawImage(audioOn? sOn:sOff,audioX,audioY,audioSize,audioSize,null);
                  if(charAdd == 0)
                  {
                     g2D.drawImage(charIcons[selectedChar[0]].getImage(),(World.XRES/2-charIcoW/2),yIco-textOffset - charIcoH,charIcoW,charIcoH,null);
                     for(int i=0;i<numPP1s;i++)
                     {
                        int currentIcoX = xIco1+projIcoW*i;
                        if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+projIcoW && World.mouseY> yIco && World.mouseY< hIco)
                        {
                           int rollW = projIcoW+rollover;
                           int rollH = projIcoH+rollover;
                           int rollX = currentIcoX-halfRoll;
                           int rollY = yIco-halfRoll;
                           g2D.drawImage(projIcons1[i].getImage(),rollX,rollY,rollW,rollH,null);
                        }
                        else
                        {
                           g2D.drawImage(projIcons1[i].getImage(),currentIcoX,yIco,projIcoW,projIcoH,null);
                        }
                     }
                  }
                  else
                  {
                     g2D.drawImage(charIcons[selectedChar[1]].getImage(),(World.XRES/2-charIcoW/2),yIco-textOffset - charIcoH,charIcoW,charIcoH,null);
                     for(int i=0;i<numPP2s;i++)
                     {
                        int currentIcoX = xIco2+projIcoW*i;
                        if(World.mouseX> currentIcoX && World.mouseX < currentIcoX+projIcoW && World.mouseY> yIco && World.mouseY< hIco)
                        {
                           int rollW = projIcoW+rollover;
                           int rollH = projIcoH+rollover;
                           int rollX = currentIcoX-halfRoll;
                           int rollY = yIco-halfRoll;
                           g2D.drawImage(projIcons2[i].getImage(),rollX,rollY,rollW,rollH,null);
                        }
                        else
                        {
                           g2D.drawImage(projIcons2[i].getImage(),currentIcoX,yIco,projIcoW,projIcoH,null);
                        }
                     }
                  }
                  g2D.dispose();
               }while(bs.contentsRestored());
               bs.show();
            }while(bs.contentsLost());
         }
      }
      
      /* Cached Character and Projectile Images */
      Image [][] charImg = new Image [World.characters.size()][4];
      for(int i=0; i<World.characters.size();i++)
      {
         Avatar a = World.characters.get(i);
         charImg[i][0] = a.getFaceR().getImage();
         charImg[i][1] = a.getFaceL().getImage();
         charImg[i][2] = a.getProjR().getImage();
         charImg[i][3] = a.getProjL().getImage();
      }
      /* Cached Sound */
      Clip hit = World.smack;
      Clip heal = World.red;
      Clip magi = World.blue;
      Clip cash = World.bling;
      /* Cached World Images */
      Image back       = world.skyDay.getImage();      
      Image back2      = world.skyNight.getImage();
      Image front      = world.groundDay.getImage();
      Image front2     = world.groundNight.getImage();
      Image grave      = world.grave.getImage();
      Image rain       = world.rain.getImage();
      Image coin       = world.coin.getImage();
      Image life       = world.life.getImage();
      Image mana       = world.mana.getImage();
      Image ledge      = world.ledge.getImage();
      Image block      = world.block.getImage();
      Image cloud      = world.cloud.getImage();
      Image cloudStorm = world.cloudStorm.getImage();
      Image sun        = world.sun.getImage();
      ImageIcon[] moon     = world.moon;
      
      /* Time Variables */
      int [] heats = new int [15];
      int it = 0;
      boolean ft = true;
      while(true)
      {
         /* While Not Dead ----- Play Game */
         World.ambiance.setFramePosition(0);
         World.ambiance.loop(Clip.LOOP_CONTINUOUSLY);
         JavaGame:
         while(true)
         {
            long start = System.currentTimeMillis();
            World.mouseX = (int)(MouseInfo.getPointerInfo().getLocation().getX() - f.getLocationOnScreen().getX());
            World.mouseY = (int)(MouseInfo.getPointerInfo().getLocation().getY() - f.getLocationOnScreen().getY());
            if(World.keyStat[KeyEvent.VK_ESCAPE])
            {
               System.exit(0);            
            }
            if(ft)
            {
               if(audioOn)
               {
                  World.unMuteSound();
               }
               else
               {
                  World.muteSound();
               }
               ft=false;
            }
            /* Manual Input Via Mouse */
            if(World.mouse[MouseEvent.BUTTON1])
            {
               World.gold.add(new Coin(World.mouseX,World.mouseY, r.nextInt(180)));
               if(World.mouseX> World.XRES - audioSize && World.mouseX<World.XRES && World.mouseY>World.YRES-audioSize && World.mouseY < World.YRES)
               {
                  audioOn = !audioOn;
                  ft=true;
               }
               World.mouse[MouseEvent.BUTTON1] = false;
            }
            if(World.mouse[MouseEvent.BUTTON2])
            {
               //World.shower.add(new Rain(World.mouseX,World.mouseY));
               //World.bolts.add(new Lightning(World.mouseX,World.mouseY));
               System.out.println();
               World.paws(100);
            }
            if(World.mouse[MouseEvent.BUTTON3])
            {
               //World.drinks.add(new Potion("mana",World.mouseX,World.mouseY)); 
              // World.shots.add(new Projectile("Player 1","Bounce",World.mouseX,World.mouseY,0, World.DEFAULT_PROJ_WIDTH, World.DEFAULT_PROJ_HEIGHT, 1, 0));
               World.mouse[MouseEvent.BUTTON3] = false;
            }
            
            /* World Actions */
            World.randDrop();
            World.rain();
            
            /* For Each Character */
            for(Avatar a : World.characters)
            {
               /* Character Actions */
               a.move();
               a.shoot();
               
               /* Projectile Hits */
               for(int j=World.shots.size()-1;j>-1;j--)
               {
                  if(World.shots.size() > 100)
                  {
                     World.shots.remove(0);
                  }
                  else
                  {
                     Projectile p = World.shots.get(j);
                     if(!p.getCast().equals(a.getName()))
                     {
                        if(p.xPos + p.width > a.xPos && p.xPos < a.xPos + a.width && p.yPos  + p.height  > a.yPos && p.yPos < a.yPos + a.height)
                        {
                           if(!hit.isRunning() || hit.getFramePosition() > hit.getFrameLength()/2)
                           {
                              hit.setFramePosition(0);
                              hit.start();
                           }
                           a.addLife(-p.getD());
                           int drops = r.nextInt(10)+1;
                           World.lootDrop((int)a.xPos+(a.width/2),(int)a.yPos+(a.height/2),drops);
                           a.addCoin(-drops);
                           World.shots.remove(j);
                        }
                     }
                  }
               }         
               /* Lightning Collisions */
               for(int j=World.bolts.size()-1;j>-1;j--)
               {
                  if(World.bolts.size() > 100)
                  {
                     World.bolts.remove(0);
                  }
                  else
                  {
                     Lightning l = World.bolts.get(j);
                     if(l.xPos + World.BOLT_WIDTH > a.xPos && l.xPos < a.xPos + a.width && l.yPos + l.height > a.yPos && l.yPos <  a.yPos + a.height)
                     {
                        a.kill();
                        System.out.println("Ligtning Kill");
                        World.bolts.remove(j);
                     }
                  }
               }
               /* Potion Consumption */
               for(int j=World.drinks.size()-1; j>-1;j--)
               {
                  if(World.drinks.size() > 100)
                  {
                     World.drinks.remove(0);
                  }
                  else
                  {
                     Potion d = World.drinks.get(j);
                     if(d.getAge() > 8000/World.spf)
                     {
                        World.drinks.remove(j);
                     }
                     else if(d.xPos + World.POT_WIDTH > a.xPos && d.xPos < a.xPos + a.width && d.yPos + World.POT_HEIGHT > a.yPos && d.yPos < a.yPos + a.height)
                     {
                        /* Life Potions */
                        if(d.type.equals("life"))
                        {
                           if(!heal.isRunning() || heal.getFramePosition() > heal.getFrameLength()/2)
                           {
                              heal.setFramePosition(0);
                              heal.start();
                           }
                           a.addLife(World.POT_POW);
                        }
                        /* Mana Potions */
                        else
                        {
                           if(!magi.isRunning() || magi.getFramePosition() > magi.getFrameLength()/2)
                           {
                              magi.setFramePosition(0);
                              magi.start();
                           }
                           a.addMana(World.POT_POW);
                        }
                        World.drinks.remove(j);
                     }
                  }
               }
               /* Coin Collection */
               for(int j=World.gold.size()-1; j>-1;j--)
               {
                  if(World.gold.size() > 100)
                  {
                     World.gold.remove(0);
                  }
                  else
                  {
                     Coin c = World.gold.get(j);
                     if(c.getAge() > 8000/World.spf)
                     {
                        World.gold.remove(j);
                     }
                     else if(c.xPos + World.COIN_WIDTH > a.xPos && c.xPos < a.xPos + a.width && c.yPos + World.COIN_HEIGHT > a.yPos && c.yPos < a.yPos + a.height)
                     {
                        if(c.getAge() > 300/World.spf)
                        {
                           if(!cash.isRunning() || cash.getFramePosition() > cash.getFrameLength()/2)
                           {
                              cash.setFramePosition(0);
                              cash.start();
                           }
                           if(!a.ifGod())
                           {
                              a.addCoin(10);
                           }
                           World.gold.remove(j);
                        }
                     }
                  }
               }
               /* Life Bounds */
               if(a.isDead())
               {
                  System.out.println(a.getName() +" has Died");
                  break JavaGame;
               }
            }
            /* Rain Collisions */
            for(int k=World.shower.size()-1;k>-1;k--)
            {
               Rain s = World.shower.get(k);
               for(int j=World.shots.size()-1;j>-1;j--)
               {
                  Projectile p = World.shots.get(j);
                  if(p.xPos + p.width > s.xPos && p.xPos < s.xPos + World.RAIN_WIDTH && p.yPos  + p.height  > s.yPos && p.yPos < s.yPos + World.RAIN_HEIGHT)
                  {
                     World.shots.remove(j);
                  }
               }
               s.move();
            }
            /* Projectile Movement */
            for(int i=World.shots.size()-1;i>-1;i--)
            {
               World.shots.get(i).move();
            }
            /* Potion Movement */
            for(int i=World.drinks.size()-1;i>-1;i--)
            {
               World.drinks.get(i).move();
            }
            /* Coin Movement */
            for(int i=World.gold.size()-1;i>-1;i--)
            {
               World.gold.get(i).move();
            }
            /* Satilite Movement */
            for(Satilite s : World.sky)
            {
               s.move();
            }
            /* Lightning Movement */
            for(int i=World.bolts.size()-1;i>-1;i--)
            {
               World.bolts.get(i).move();
            }
            /* Cloud Movement */
            for(Cloud c : World.clouds)
            {
               c.move();                   
            }
            /* Platform Movement */
            for(Platform p : World.ledges)
            {
               p.move();
            }
            /* Displays Frame */
            /* Redraws Frame if Lost */
            do
            {
               /* Redraws Frame If Restored */
               do
               {
                  Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
                  /* Background */
                  if(World.dayTime)
                  {
                     g2D.drawImage(back,0,0,World.XRES,World.YRES,null);
                  }
                  else
                  {
                     g2D.drawImage(back2,0,0,World.XRES,World.YRES,null);
                  }
                  /* Sun and Moon */
                  World.dayTime = false;
                  for(Satilite s : World.sky)
                  {
                     if(s.getAngle() > 0 && s.getAngle() < 180)
                     {
                        if(s.kind.equals("sun"))
                        {
                           g2D.setPaint(new RadialGradientPaint(new Point((int)(s.getX()+World.SAT_WIDTH/2),(int)(s.getY()+World.SAT_HEIGHT/2)),2*World.SAT_WIDTH,new float[]{0.0f,1.0f},new Color[]{new Color(255,255,0,128),new Color(0,0,0,0)},RadialGradientPaint.CycleMethod.NO_CYCLE));
                           g2D.drawImage(sun,(int)s.getX(),(int)s.getY(),World.SAT_WIDTH,World.SAT_HEIGHT,null);
                        }
                        else
                        {
                           g2D.setPaint(new RadialGradientPaint(new Point((int)(s.getX()+World.SAT_WIDTH/2),(int)(s.getY()+World.SAT_HEIGHT/2)),World.SAT_WIDTH,new float[]{0.0f,1.0f},new Color[]{new Color(255,255,255,128),new Color(0,0,0,0)},RadialGradientPaint.CycleMethod.NO_CYCLE));
                           g2D.drawImage(moon[s.getMonth()].getImage(),(int)s.getX(),(int)s.getY(),World.SAT_WIDTH,World.SAT_HEIGHT,null);
                        }
                        g2D.fill(new Rectangle(0,0, World.XRES,World.YRES));
                     }
                  }
                  /* Foreground */
                  if(World.dayTime)
                  {
                     g2D.drawImage(front,0,0,World.XRES,World.YRES,null);
                  }
                  else
                  {
                     g2D.drawImage(front2,0,0,World.XRES,World.YRES,null);
                  }
                  /* Rain */
                  for(Rain d : World.shower)
                  {
                     g2D.drawImage(rain,(int)d.xPos,(int)d.yPos,World.RAIN_WIDTH,World.RAIN_HEIGHT,null);
                  }
                  /* Lightning */
                  g2D.setColor(Color.YELLOW);
                  for(Lightning b : World.bolts)
                  {
                     g2D.fillRect((int)b.xPos,(int)b.yPos,World.BOLT_WIDTH,(int)b.height);
                  }
                  /* Clouds */
                  if(World.raining)
                  {
                     for(Cloud c : World.clouds)
                     {
                        g2D.drawImage(cloudStorm,(int)c.getX(),(int)c.getY(),World.CLOUD_WIDTH,World.CLOUD_HEIGHT,null);                  
                     }
                  }
                  else
                  {
                     for(Cloud c : World.clouds)
                     {
                        g2D.drawImage(cloud,(int)c.getX(),(int)c.getY(),World.CLOUD_WIDTH,World.CLOUD_HEIGHT,null);
                     }
                  }
               
                  /* Projectiles */
                  for(Projectile p : World.shots)
                  {
                     for(int j =0;j<World.characters.size();j++)
                     {
                        if(p.getCast().equals(World.characters.get(j).getName()))
                        {
                           g2D.setPaint(new RadialGradientPaint(new Point((int)(p.xPos+p.width/2),(int)(p.yPos+p.height/2)),p.width,new float[]{0.0f,1.0f},new Color[]{new Color(255,255,255,128),new Color(0,0,0,0)},RadialGradientPaint.CycleMethod.NO_CYCLE));
                           g2D.fill(new Rectangle(0,0, World.XRES,World.YRES));
                           if(p.xSpeed >= 0)
                           {
                              g2D.drawImage(charImg[j][2],(int)p.xPos,(int)p.yPos,p.width,p.height,null);
                           }
                           else
                           {
                              g2D.drawImage(charImg[j][3],(int)p.xPos,(int)p.yPos,p.width,p.height,null);
                           }
                           break;
                        }
                     }    
                  }               
                  /* Potions */
                  for(Potion p : World.drinks)
                  {
                     double age = p.getAge();
                     if(age < 6000/World.spf || age%5 != 0)
                     {
                        if(p.type.equals("life"))
                        {                  
                           g2D.drawImage(life,(int)p.xPos,(int)p.yPos,World.POT_WIDTH,World.POT_HEIGHT,null);
                        }
                        else
                        {
                           g2D.drawImage(mana,(int)p.xPos,(int)p.yPos,World.POT_WIDTH,World.POT_HEIGHT,null);
                        }
                     }
                  }
                  /* Coins */
                  for(Coin c : World.gold)
                  {
                     double age = c.getAge();
                     if(age < 6000/World.spf || age%5 != 0)
                     {
                        g2D.drawImage(coin,(int)c.xPos,(int)c.yPos,World.COIN_WIDTH,World.COIN_HEIGHT,null);
                     }
                  }
                  /* Platforms */
                  for(Platform p : World.ledges)
                  {
                     g2D.drawImage(ledge,(int)p.getX(),(int)p.getY(),World.PLAT_LENGTH,World.PLAT_THICK, null);
                  }
                  /* Blocks */
                  g2D.setColor(Color.BLACK);
                  for(Blockage b : World.blocks)
                  {
                     g2D.fill(b.getCore());
                  }
                  
                         
                  /* Characters */
                  for(int i=0;i<World.characters.size();i++)
                  {
                     Avatar a = World.characters.get(i);
                     int charX  = (int) a.xPos;
                     int charY  = (int) a.yPos;
                     int charW  =       a.width;
                     int charH  =       a.height;
                     int health = (int)(a.getLifeRatio()*charW);
                     
                     if(charY+charH>0)
                     {
                        if(a.getAngle() == 0)
                        {
                           g2D.drawImage(charImg[i][0],charX,charY,charW,charH,null);
                        }
                        else
                        {
                           g2D.drawImage(charImg[i][1],charX,charY,charW,charH,null);
                        }
                        /* Life-bar */
                        if(health < charW/3)
                        {
                           g2D.setColor(Color.RED);
                        }
                        else if(health < 2*charW/3)
                        {
                           g2D.setColor(Color.ORANGE);
                        }
                        else
                        {
                           g2D.setColor(Color.GREEN);
                        }
                        g2D.fillRect(charX,charY-25,health,5);
                        
                        /* Mana-bar */
                        g2D.setColor(Color.BLUE);
                        g2D.fillRect(charX,charY-20,(int)(a.getManaRatio()*charW),5);
                        
                        /* Coin-bar */
                        g2D.setColor(Color.YELLOW);
                        g2D.fillRect(charX,charY-15,(int)(a.getCoinRatio()*charW),5);
                        
                        /* Bar-Outline */
                        g2D.setColor(Color.BLACK);
                        g2D.drawRect(charX,(int)charY-25,charW,5);
                        g2D.drawRect(charX,(int)charY-20,charW,5);
                        g2D.drawRect(charX,(int)charY-15,charW,5);
                        if(a.ifGod())
                        {
                           if(a.getTimeGod() > 6000/World.spf)
                           {
                              World.characters.get(i).deGod();
                           }
                           else if(a.getTimeGod() < 4000/World.spf || a.getTimeGod()%5 != 0)
                           {
                              g2D.setPaint(new RadialGradientPaint(new Point(charX+charW/2,charY+charH/2),charH,new Point(charX,charY), new float[]{0.0f,1.0f},new Color[]{new Color(0,0,255,128),new Color(0,0,0,0)},RadialGradientPaint.CycleMethod.NO_CYCLE));
                              g2D.fill(new Ellipse2D.Double(charX-charW,charY-charH/2, 3*charW,2*charH ));
                           }
                           a.addTimeGod();
                        }
                     }
                     else
                     {
                        g2D.setColor(Color.RED);
                        g2D.fill(new Polygon(new int[]{charX,charX+charW/2,charX+charW},new int[]{charH/2,0,charH/2},3));
                     }
                     //g2D.fill(a.shape);
                  }
                  g2D.drawImage(audioOn? sOn:sOff,audioX,audioY,audioSize,audioSize,null);
                  
                  g2D.dispose();
                  
               }while(bs.contentsRestored());
               /* Compiled Image */
               bs.show();
            }while(bs.contentsLost());
            World.paws((int)(15-(System.currentTimeMillis()-start)));
            if(it<heats.length+5)
            {
               if(it>4)
               {
                  heats[it-5]=(int)(System.currentTimeMillis()-start);
               }
               it++;
               if(it>=heats.length+5)
               {
                  World.spf = World.getAvg(heats);
               }
            }
            else
            {
               //World.paws(50);
            }
         }
         
         /* If Dead ----- End Game */
         for(int i=1;i<World.sounds.size();i++)
         {
            Clip c = World.sounds.get(i);
            c.stop();
            c.setFramePosition(c.getFrameLength());
         }
         while(true)
         {
            World.mouseX = (int)(MouseInfo.getPointerInfo().getLocation().getX() - f.getLocationOnScreen().getX());
            World.mouseY = (int)(MouseInfo.getPointerInfo().getLocation().getY() - f.getLocationOnScreen().getY());
            if(World.keyStat[KeyEvent.VK_ESCAPE])
            {
               System.exit(0);            
            }
            if(ft)
            {
               if(audioOn)
               {
                  World.unMuteSound();
               }
               else
               {
                  World.muteSound();
               }
               ft = false;
            }
            do
            {
               do
               {
                  Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
                  
                  g2D.drawImage(grave,0,0,World.XRES,World.YRES,null);
                  /* Display Living Character */
                  for(int i=0;i<World.characters.size();i++)
                  {
                     Avatar a = World.characters.get(i);
                     int charW = (int)a.width;
                     int charH = (int)a.height;
                     int health = (int)(a.getLifeRatio()*charW);
                     if(health >0)
                     {
                        g2D.drawImage(charImg[i][0],World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH,2*charW,2*charH,null);
                        /* Life-bar */
                        if(health < charW/3)
                        {
                           g2D.setColor(Color.RED);
                        }
                        else if(health < 2*charW/3)
                        {
                           g2D.setColor(Color.ORANGE);
                        }
                        else
                        {
                           g2D.setColor(Color.GREEN);
                        }
                        g2D.fillRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-50,(int)(2*health),2*5);
                        /* Mana-bar */
                        g2D.setColor(Color.BLUE);
                        g2D.fillRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-40,(int)(2*a.getManaRatio()*charW),2*5);
                        /* Coin-bar */ 
                        g2D.setColor(Color.YELLOW);
                        g2D.fillRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-30,(int)(2*a.getCoinRatio()*charW),2*5);
                                 
                        /* Bar-Outline */
                        g2D.setColor(Color.BLACK);
                        g2D.drawRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-50,2*charW,2*5);
                        g2D.drawRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-40,2*charW,2*5);
                        g2D.drawRect(World.XRES/2 - 2*charW,World.YRES/2 + 400 - 2*charH-30,2*charW,2*5);
                        break;
                     }
                  }
                  if(World.mouseX>World.XRES/4 - 55 && World.mouseX<World.XRES/4 + 55 && World.mouseY>World.YRES/3-55 && World.mouseY<World.YRES/3+55)
                  {
                     g2D.drawImage(quit,World.XRES/4 - 55,World.YRES/3-55,110,110,null);
                  }
                  else
                  {
                     g2D.drawImage(quit,World.XRES/4 - 50,World.YRES/3-50,100,100,null);
                  }
                  if(World.mouseX>3*World.XRES/4 - 55 && World.mouseX<3*World.XRES/4 + 55 && World.mouseY>World.YRES/3-55 && World.mouseY<World.YRES/3+55)
                  {
                     g2D.drawImage(replay,3*World.XRES/4 - 55,World.YRES/3-55,110,110,null);
                  }
                  else
                  {
                     g2D.drawImage(replay,3*World.XRES/4 - 50,World.YRES/3-50,100,100,null);
                  }
                  g2D.drawImage(audioOn? sOn:sOff,audioX,audioY,audioSize,audioSize,null);
                  
                  g2D.dispose();
               }while(bs.contentsRestored());
               bs.show();
            }while(bs.contentsLost());
            
            if(World.mouse[MouseEvent.BUTTON1])
            {
               if(World.mouseX>World.XRES/4 - 55 && World.mouseX<World.XRES/4 + 55 && World.mouseY>World.YRES/3-55 && World.mouseY<World.YRES/3+55)
               {
                  System.out.println("Quit");
                  System.exit(0);
               }
               else if(World.mouseX>3*World.XRES/4 - 55 && World.mouseX<3*World.XRES/4 + 55 && World.mouseY>World.YRES/3-55 && World.mouseY<World.YRES/3+55)
               {
                  System.out.println("Replay");
                  World.reset();
                  World.mouse[MouseEvent.BUTTON1] = false;
                  break;
               }
               else if(World.mouseX> World.XRES - audioSize && World.mouseX<World.XRES && World.mouseY>World.YRES-audioSize && World.mouseY < World.YRES)
               {
                  if(audioOn)
                  {
                     audioOn = false;
                  }
                  else
                  {
                     audioOn = true;
                  }
                  ft = true;
               }
               World.mouse[MouseEvent.BUTTON1] = false;
            }
         }
      }
   }
}