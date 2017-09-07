import java.awt.image.BufferStrategy;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
public class rocketShip
{
   private boolean dead;
   
   private double xPos, yPos;
   private double flySpeed,turnSpeed;
   private double direction;
   
   private int cooldown;
   
   private final int RELOAD_TIME;
   private final int FORWARD_KEY, L_TURN_KEY, SHOOT_KEY, R_TURN_KEY;
   
   private final Color shade;
   
   private final String name;
   
   private final ArrayList<rocket> SHOTS = new ArrayList<rocket>();
      
   public rocketShip(Color hue, String title, int n, int w, int s, int e)
   {
      dead = false;
      
      xPos = enviornment.XRES/2;
      yPos = enviornment.YRES/2;
      
      flySpeed = 3;
      turnSpeed = .8;
      
      direction=0;
      
      cooldown = 0;
      
      RELOAD_TIME = 200;
      FORWARD_KEY = n;
      L_TURN_KEY = w;
      SHOOT_KEY = s;
      R_TURN_KEY = e;
      
      shade = hue;
      
      name = title;
   }
   public boolean isDead()
   {
      return dead;
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
   }
   public ArrayList<rocket> getShots()
   {
      return SHOTS;
   }
   public String toString()
   {
      return name;
   }
   public void kill(Object killer)
   {
      if(killer instanceof asteroid)
      {
         System.out.println("Killed by Asteriod");
      }
      else
      {
         System.out.println("Killed by Player " + killer.toString());
      }
      dead = true;
   }

   public Polygon getPoly()
   {
      return new Polygon(
      new int[]{
         (int)(xPos-25*Math.cos(Math.toRadians(direction))-25*Math.sin(Math.toRadians(direction))),
         (int)(xPos+25*Math.cos(Math.toRadians(direction))),
         (int)(xPos-25*Math.cos(Math.toRadians(direction))+25*Math.sin(Math.toRadians(direction))),
         (int)(xPos)},
      new int[]{
         (int)(yPos-25*Math.cos(Math.toRadians(direction))+25*Math.sin(Math.toRadians(direction))),
         (int)(yPos-25*Math.sin(Math.toRadians(direction))),
         (int)(yPos+25*Math.cos(Math.toRadians(direction))+25*Math.sin(Math.toRadians(direction))),
         (int)(yPos)},
      4);
   }
   public void move()
   {
      if(dead)
      {
         return;
      }
      cooldown--;
      if(enviornment.keyStat[FORWARD_KEY])
      {
         xPos += flySpeed*Math.cos(Math.toRadians(direction));
         yPos -= flySpeed*Math.sin(Math.toRadians(direction));
      }
      if(enviornment.keyStat[L_TURN_KEY])
      {
         direction += turnSpeed;
      }
      if(enviornment.keyStat[SHOOT_KEY])
      {
         if(cooldown<=0)
         {
            SHOTS.add(new rocket(xPos,yPos,direction));
            cooldown = RELOAD_TIME;
         }
      }
      if(enviornment.keyStat[R_TURN_KEY])
      {
         direction -= turnSpeed;
      }
      if(xPos < 0)
      {
         xPos = 0;
      }
      else if(xPos >enviornment.XRES)
      {
         xPos = enviornment.XRES;
      }
      if(yPos < 0)
      {
         yPos = 0;
      }
      else if(yPos > enviornment.YRES)
      {
         yPos = enviornment.YRES;
      }
   }
   public void drawSelf(BufferStrategy b)
   {
      do
      {
         Graphics2D g2D = (Graphics2D)b.getDrawGraphics();
         g2D.setColor(shade);
         if(dead)
         {
            g2D.setColor(Color.YELLOW);
         }
         else
         {
            g2D.setColor(shade);
         }
         g2D.fill(getPoly());
         g2D.dispose();
      }while(b.contentsRestored());
   }
}








class rocket
{
   private double xPos,yPos,direction,speed,xSpeed,ySpeed,xAccel,yAccel;
   boolean outOfBounds;
   public rocket(double x, double y, double d)
   {
      xPos = x;
      yPos = y;
      direction = d;
      speed = 5;
      xSpeed = speed*Math.cos(Math.toRadians(direction));
      ySpeed = speed*Math.sin(Math.toRadians(direction));
      xAccel = 0;
      yAccel = 0;
      outOfBounds = false;
   }
   public double getX()
   {
      return xPos;
   }
   public double getY()
   {
      return yPos;
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
      
      double accel = 2*p.getDiameter()/(dist2);
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
   public void drawSelf(BufferStrategy b)
   {
      do
      {
         Graphics2D g2D = (Graphics2D)b.getDrawGraphics();
         g2D.setColor(Color.YELLOW);
         //g2D.fillRect((int)xPos-5,(int)yPos-5,10,10);
         g2D.fill(new Polygon(
            new int[]{
               (int)(xPos-10*Math.cos(Math.toRadians(direction))-5*Math.sin(Math.toRadians(direction))),
               (int)(xPos+10*Math.cos(Math.toRadians(direction))-5*Math.sin(Math.toRadians(direction))),
               (int)(xPos+10*Math.cos(Math.toRadians(direction))+5*Math.sin(Math.toRadians(direction))),
               (int)(xPos-10*Math.cos(Math.toRadians(direction))+5*Math.sin(Math.toRadians(direction)))},
            new int[]{
               (int)(yPos-5*Math.cos(Math.toRadians(direction))+10*Math.sin(Math.toRadians(direction))),
               (int)(yPos-5*Math.cos(Math.toRadians(direction))-10*Math.sin(Math.toRadians(direction))),
               (int)(yPos+5*Math.cos(Math.toRadians(direction))-10*Math.sin(Math.toRadians(direction))),
               (int)(yPos+5*Math.cos(Math.toRadians(direction))+10*Math.sin(Math.toRadians(direction)))},
         4));
         g2D.dispose();
      }while(b.contentsRestored());
   }
}