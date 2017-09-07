import java.awt.geom.Area;
import java.awt.Rectangle;
public abstract class PhysicsObject
{
   /* Movement */
   protected double xPos, yPos, xSpeed, ySpeed;
   private double xPosp, yPosp;
   
   /* Sizes */
   protected int height, width;
   protected int bottomBound, rightBound, leftBound, topBound;
   
   /* Applied Forces */
   private final double grav, fricG, fricA;
   
   public PhysicsObject(int startX, int startY,double velX,double velY,double frictionG,double frictionA, int wide, int tall)
   {
      xPos = startX;
      yPos = startY;
      xPosp = startX;
      yPosp = startY;
      xSpeed = velX;
      ySpeed = velY;
      height = tall;
      width = wide;
      fricA = frictionA;
      fricG = frictionG;
      
      grav = World.GRAV_ACCEL;
      bottomBound = World.YRES;
      topBound = -World.YRES;
      rightBound = World.XRES;
      leftBound = 0;
   }
   /* Detects collisions vetically */
   public final void collisionDetectY()
   {
      bottomBound = (int)World.YRES;
      topBound = -World.YRES;
      if(yPos+height>World.YRES || yPos<-World.YRES)
      {
         return;
      }
      Rectangle me = new Rectangle((int)xPos,(int)yPos,width,height);
      //checks blocks
      for(Blockage b: World.blocks)
      {
         if(b.getCore().intersects(me))
         {
            if(yPos>yPosp)
            {
               Area column = new Area(new Rectangle((int)xPos,(int)yPosp,width,(int)(b.getY()+ b.getH() -yPosp)));
               column.intersect(b.getCore());
               bottomBound = Math.min((int)(column.getBounds().getY()),bottomBound);
            }
            else
            {
               Area column = new Area(new Rectangle((int)xPos,(int)b.getY(),width,(int)(yPosp-b.getY())));
               column.intersect(b.getCore());
               topBound = Math.max((int)(column.getBounds().getY()+column.getBounds().getHeight()),topBound);
            }
         }
      }
      //checks platforms
      for(Platform l: World.ledges)
      {
         if(l.getX()<xPos+width && l.getX()+World.PLAT_LENGTH>xPos)
         {
            if(l.getY()>=yPosp+height)
            {
               if(l.getY()<bottomBound)
               {
                  bottomBound = Math.min((int)l.getY(),bottomBound);
               }
            }
         }
      }
   }
   /* Checks for vertical collision with a custom hitbox */
   public final void collisionDetectY(Area subClass)
   {
      bottomBound = (int)World.YRES;
      topBound = -World.YRES;
      if(yPos+height>World.YRES || yPos<-World.YRES)
      {
         return;
      }
      Area me = subClass;
      //checks blocks
      for(Blockage b: World.blocks)
      {
         Area inter = (Area)b.getCore().clone();
         inter.intersect(me);
         if(!inter.isEmpty())
         {
            if(inter.getBounds().getHeight() > Math.abs(yPosp-yPos))
               {
               System.out.println("Double placement hit");
               }
            if(yPos>yPosp)
            {
               int potBound = (int)(yPos+height-inter.getBounds().getHeight());
               
               bottomBound = Math.min(potBound,bottomBound);
            }
            else
            {
               int potBound = (int)(yPos+inter.getBounds().getHeight());
               topBound = Math.max(potBound,topBound);
            }
         }
         
      }
      //checks platforms
      for(Platform l: World.ledges)
      {
         if(l.getX()<xPos+width && l.getX()+World.PLAT_LENGTH>xPos)
         {
            if(l.getY()>=yPosp+height)
            {
               if(l.getY()<bottomBound)
               {
                  bottomBound = Math.min((int)l.getY(),bottomBound);
               }
            }
         }
      }
   }
   /* checks for collisions horizontally */
   public final void collisionDetectX()
   {
      rightBound = World.XRES;
      leftBound = 0;
      if(xPos>World.XRES-width || xPos<0)
      {
         return;
      }
      Rectangle me = new Rectangle((int)xPos,(int)yPos,width,height);
      for(Blockage b: World.blocks)
      {
         
         
         if(b.getCore().intersects(me))
         {
            if(xPos>xPosp)
            {
               Area row = new Area(new Rectangle((int)xPosp,(int)yPos,(int)(b.getX()+b.getW()-xPosp),height));
               row.intersect(b.getCore());
               rightBound = Math.min((int)(row.getBounds().getX()),rightBound);
            }
            else
            {
               Area row = new Area(new Rectangle((int)b.getX(),(int)yPos,(int)(xPosp-b.getX()),height));
               row.intersect(b.getCore());
               leftBound = Math.max((int)(row.getBounds().getX()+row.getBounds().getWidth()),leftBound);
            }
         }
      }
   }
   /* checks for collisions horizontally with a custom hitbox */
   public final void collisionDetectX(Area subClass)
   {
      rightBound = World.XRES;
      leftBound = 0;
      if(xPos>World.XRES-width || xPos<0)
      {
         return;
      }
      Area me = subClass;
      for(Blockage b: World.blocks)
      {
         Area inter = (Area)b.getCore().clone();
         inter.intersect(me);
         if(!inter.isEmpty())
         {
            if(inter.getBounds().getWidth() > Math.abs(xPosp-xPos))
               {
               System.out.println("Double placement hit");
               }
            if(xPos>xPosp)
            {
               int potBound = (int)(xPos+width-inter.getBounds().getWidth());
               rightBound = Math.min(potBound,rightBound);
               rightBound = (int)xPosp+width;
            }
            else
            {
               int potBound = (int)(xPos+inter.getBounds().getWidth());
               leftBound = Math.max(potBound,leftBound);
               leftBound = (int)xPosp;
            }
         }
         
      }
   }
   /* applys downward motion with no acceleration */
   public final void applyTermVel()
   { 
      yPosp = yPos;
      yPos += ySpeed*World.spf;
   }
   /* applies vertical motion with acceleration down */
   public final void applyGravity()
   {
      /* Constant Pull of Gravity */
      yPosp = yPos;
      yPos += (-ySpeed + grav)*World.spf;
      ySpeed -= grav*World.spf;
   }
   /* applies horizontal motion */
   public final void applyXMove()
   {
      xPosp = xPos;
      xPos += xSpeed*World.spf;
      /* Ground Friction */
      if(yPos + height >= bottomBound)
      {
         if(xSpeed < fricG*World.spf && xSpeed > -fricG*World.spf)
         {
            xSpeed = 0;
         }
         else if(xSpeed > 0)
         {
            xSpeed -= fricG*World.spf;
         }
         else if(xSpeed < 0)
         {
            xSpeed += fricG*World.spf;
         }
      }
      /* Air Friction */
      else
      {
         if(xSpeed < fricA*World.spf && xSpeed > -fricA*World.spf)
         {
            xSpeed = 0;
         }
         else if(xSpeed>0)
         {
            xSpeed -= fricA*World.spf;
         }
         else if(xSpeed<0)
         {
            xSpeed += fricA*World.spf;
         }
      }
   }
   /* Adjusts movement if on platform */
   public final void applyGroundMove()
   {
      
      if(bottomBound != World.YRES && yPos == bottomBound - height)
      {
         for(int i=0;i<World.ledges.size();i++)
         {
            /* If in same xPos */ 
            if(World.ledges.get(i).getX() < xPos + width && World.ledges.get(i).getX() + World.PLAT_LENGTH > xPos)
            {
               /* If under foot */
               if(World.ledges.get(i).getY() >= yPos + height)
               {
                  if(bottomBound == (int)World.ledges.get(i).getY())
                  {
                     xPos = xPos + World.ledges.get(i).getS()*(World.ledges.get(i).getD()? 1:-1)*World.spf;
                     return;
                  }
               }
            }
         }
      }
   }
   /* keeps object on screen vertically */
   public final void keepYBounds()
   {
      /* If At Bounds */
      if(yPos + height > bottomBound)
      {
         yPos = bottomBound - height;
         ySpeed = 0;
      }
      if(yPos < topBound)
      {
         yPos = topBound;
         ySpeed = 0;
      }
   }
   /* keeps object on screen horizontally */
   public final void keepXBounds()
   {
      /* Left Bound */
      if(xPos < leftBound)
      {
         xPos = leftBound;
         xSpeed = 0;
      }
      /* Right Bound */
      else if(xPos > rightBound - width)
      {
         xPos = rightBound - width;
         xSpeed = 0;
      }
   }
}