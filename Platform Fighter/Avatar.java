import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import java.awt.geom.Area;
public final class Avatar extends PhysicsObject
{
   /* Origins */
   private final int beginX, beginY;
   private Area shape;
   private  Area shapeP;
   
   /* Stats */
   private final String name;
   private boolean godMode;
   private int angle, timeGod;
   private double life, mana, coins;
   private double maxLife,maxMana,maxCoin;
   private final double maxXSpeed, jumpSpeed, accel;
   private int timeTillShot, noShotTime;
   
   /* Proj stats */
   private final int widthP, heightP;
   private final double costP, speedP, damageP;
   private final String motionP;
   
   /* Control keys */
   private final int charLeft,charRight,charUp,charDown;
   
   /* Render Specs */
   private final Clip sJump, sShoot, sEmpty, god;
   private final ImageIcon faceR, faceL, projR, projL;
   
   public Avatar(String title,String person, int startX, int startY, int [] keys, String shoot)
   {            
      super(startX,startY,0,0,World.FLOOR_FRIC,World.FLOOR_FRIC,0,0);
      beginX = startX;
      beginY = startY;
      
      /* Character folder */
      String pathG  = World.absPath +"\\Characters\\";
      String path  = World.absPath +"\\Characters\\"+person+"\\";
      
      /* Projectile folder */
      String pathGP  = World.absPath +"\\Projectiles\\";
      String pathP = World.absPath +"\\Projectiles\\"+shoot+"\\";
      
      /* Loads Character and Projectile Attributes */
      String [] attributes  = World.readFile(path + "Attributes.txt");
      String [] attributesP = World.readFile(pathP + "Attributes.txt");
   
      /* Char attribs */
      if(attributes.length <9)
      {
         System.out.println("Not enough attributes for " + title);
         System.exit(0);
      }
      super.width  = (int)(World.XRES * Double.valueOf(attributes[0].substring(0,attributes[0].indexOf("|"))));
      super.height = (int)(World.YRES * Double.valueOf(attributes[1].substring(0,attributes[1].indexOf("|"))));
      maxXSpeed    =       World.XRES * Double.valueOf(attributes[2].substring(0,attributes[2].indexOf("|")));
      jumpSpeed    =       World.YRES * Double.valueOf(attributes[3].substring(0,attributes[3].indexOf("|")));
      accel        =       World.XRES * Double.valueOf(attributes[4].substring(0,attributes[4].indexOf("|")));
      
      maxLife                         = Double.valueOf(attributes[6].substring(0,attributes[6].indexOf("|")));
      maxMana                         = Double.valueOf(attributes[7].substring(0,attributes[7].indexOf("|")));
      maxCoin                         = Double.valueOf(attributes[8].substring(0,attributes[8].indexOf("|")));
      /* Proj attribs */
      if(attributesP.length <7)
      {
         System.out.println("Not enough attributes for " + title+"'s Projectile");
         System.exit(0);
      }
      widthP    = (int)(World.XRES * Double.valueOf(attributesP[0].substring(0,attributesP[0].indexOf("|"))));
      heightP   = (int)(World.YRES * Double.valueOf(attributesP[1].substring(0,attributesP[1].indexOf("|"))));
      speedP    =       World.XRES * Double.valueOf(attributesP[2].substring(0,attributesP[2].indexOf("|")));
      damageP   =       World.XRES * Double.valueOf(attributesP[3].substring(0,attributesP[3].indexOf("|")));
      costP     =       World.XRES * Double.valueOf(attributesP[4].substring(0,attributesP[4].indexOf("|")));
      motionP   =                                   attributesP[5].substring(0,attributesP[5].indexOf("|"));
      
      /* Sound Specs */
      sJump  = World.getSound(World.absPath + "\\SystemFiles\\jump.wav");
      sShoot = World.getSound(World.absPath + "\\SystemFiles\\shoot.wav");
      sEmpty = World.getSound(World.absPath + "\\SystemFiles\\empty.wav");
      god = World.getSound(World.absPath + "\\SystemFiles\\godMode.wav");
      
      /* Image Specs */
      String charPath = path+person;
      String projPath = pathP+shoot;
      ImageIcon[][] pics = World.getIcons(pathG,new String[]{person},new String[]{person,person+"2"});
      faceR = pics[0][0];
      faceL = pics[0][1];
      pics = World.getIcons(pathGP,new String[]{shoot},new String[]{shoot,shoot+"2"});
      projR = pics[0][0];
      projL = pics[0][1];
      
      /* Stats */
      name = title;
      angle = 0;
      life = maxLife;
      mana = maxMana;
      coins = 0;
      godMode = false;
      timeGod =0;
      
      /* Control Keys */
      charLeft = keys[0];
      charRight = keys[1];
      charUp = keys[2];
      charDown = keys[3];
      
      /* Counting Variables */
      timeTillShot = 0;
      noShotTime = 0;
      
      shape = World.convertToArea(charPath+".gif",super.width,super.height);
      if(shape.isEmpty())
      shape = World.convertToArea(charPath+".png",super.width,super.height);
      
      shapeP = World.convertToArea(projPath+".gif",widthP,heightP);
      if(shapeP.isEmpty())
      shapeP = World.convertToArea(projPath+".png",widthP,heightP);
   }
   public ImageIcon getFaceR()
   {
      return faceR;
   }
   public ImageIcon getFaceL()
   {
      return faceL;
   }
   public ImageIcon getProjR()
   {
      return projR;
   }
   public ImageIcon getProjL()
   {
      return projL;
   }
   public String getName()
   {
      return name;
   }
   public int getAngle()
   {
      return angle;
   }
   public boolean ifGod()
   {
      return godMode;
   }
   /* turns off god mode */
   public void deGod()
   {
      godMode = false;
      timeGod = -1;
   }
   public int getTimeGod()
   {
      return timeGod;
   }
   public void addTimeGod()
   {
      timeGod++;
   }
   public void kill()
   {
      life = 0;
   }
   public void addLife(double amt)
   {
      life += amt;
      // life bounds
      if(life> maxLife)
      {
         life = maxLife;
      }
   }
   public void addMana(double amt)
   {
      mana += amt;
      //mana bounds
      if(mana > maxMana)
      {
         mana = maxMana;
      }
      else if(mana < 0)
      {
         mana = 0;
      }
   }
   public void addCoin(double amt)
   {
      coins += amt;
      //coin bounds
      if(coins > maxCoin)
      {
         coins = 0;
         godMode = true;
         god.setFramePosition(0);
         god.start();
      }
      else if(coins < 0)
      {
         coins = 0;
      }
   }
   public boolean isDead()
   {
      return life<0;
   }
   // returns percentage of life
   public double getLifeRatio()
   {
      return life/maxLife;
   }
   // returns percentage of mana
   public double getManaRatio()
   {
      return mana/maxMana;
   }
   // returns percentage of coins
   public double getCoinRatio()
   {
      return coins/maxCoin;
   }
   /* returns object to original stats */
   public void reset()
   {
      super.xSpeed = 0;
      super.ySpeed = 0;
      super.xPos = beginX;
      super.yPos = beginY;
      coins = 0;
      godMode = false;
      timeGod = 0;
      life = super.width;
      mana = super.width;
   }
   public void move()
   {
      /* Left Movement */
      if(World.keyStat[charLeft])
      {
         if(!World.keyStat[charRight])
         {
            if(angle !=180)
            {
               angle = 180;
               shape = World.flipHArea(shape);
            }
            super.xSpeed -= accel * World.spf;
            if(super.xSpeed < -maxXSpeed)
            {
               super.xSpeed = -maxXSpeed;
            }
         }
      }
      /* Right Movement */
      else
      {
         if(World.keyStat[charRight])
         {
            if(angle !=0)
            {
               angle = 0;
               shape = World.flipHArea(shape);
            }
            super.xSpeed += accel * World.spf;
            if(super.xSpeed > maxXSpeed)
            {
               super.xSpeed = maxXSpeed;
            }
         }
      }
      /* can jump only if grounded and completed last jump */
      if(World.keyStat[charUp] && super.bottomBound <= super.yPos + super.height && super.ySpeed <= 0)
      {
         super.ySpeed = jumpSpeed;
         sJump.setFramePosition(0);
         sJump.start();
      }
      
      
      super.applyGravity();
      shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
      super.collisionDetectY(/*shape*/);
      super.keepYBounds();
      
      
      super.applyXMove();
      super.applyGroundMove();
      shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
      super.collisionDetectX(/*shape*/);
      super.keepXBounds();
      if(godMode)
      {
         life+= 0.1;
      }
   }
   /* Can generate projectiles */
   public void shoot()
   {
      if(World.keyStat[charDown])
      {
         if(mana >= costP || godMode)
         {
            /* Shoot */
            if(timeTillShot <= 0)
            {
               sShoot.setFramePosition(0);
               sShoot.start();
               if(motionP.equalsIgnoreCase("Bounce"))
               {
                  World.shots.add(new Projectile(name,motionP,(int)(super.xPos + super.width/2),(int)(super.yPos + super.height/2),angle==0? 45:135, widthP, heightP, speedP, damageP, shapeP));
               }
               else
               {
                  World.shots.add(new Projectile(name,motionP,(int)(super.xPos + super.width/2),(int)(super.yPos + super.height/2),angle, widthP, heightP, speedP, damageP, shapeP));
               }
               if(godMode)
               {
                  timeTillShot = 400/World.spf;
               }
               else
               {
                  timeTillShot = 500/World.spf;  
                  mana -= costP;
               }        
            }
         }
         else
         {
            /* cant Shoot */
            if(noShotTime <= 0)
            {
               sEmpty.setFramePosition(0);
               sEmpty.start();
               noShotTime = 500/World.spf;
            }
         }
      }
      noShotTime--;
      timeTillShot--;
   }
}