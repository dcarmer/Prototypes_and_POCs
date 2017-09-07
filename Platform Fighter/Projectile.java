import java.awt.geom.Area;

public final class Projectile extends PhysicsObject
{
   /* Damage */
   private final double damage;
   /* Shape */
   public Area shape;
   /* Creator */
   private final String caster;
   
   /* Movement Type */
   private final String motion;
   
   public Projectile(String sender,String type,int startX, int startY, int direction,int wide,int tall,double vel,double power, Area a)
   {
      super(startX,startY,vel*Math.cos(Math.toRadians(direction)),vel*Math.sin(Math.toRadians(direction)),World.FLOOR_FRIC/2,0,wide,tall);
      motion = type;
      caster = sender;
      damage = power;
      shape = World.translateArea(a,(int)super.xPos,(int)super.yPos);
   }
   public double getD()
   {
      return damage;
   }
   public String getCast()
   {
      return caster;
   }
   /* Moves object based on motion type */
   public void move()
   {
      //Bouncing
      if(motion.equalsIgnoreCase("Bounce"))
      {       
         super.applyGravity();
         shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
         super.collisionDetectY();
         if(super.yPos + super.height >= super.bottomBound)
         {
            super.yPos = super.bottomBound - super.height;
            super.ySpeed *= -.75;
            for(Blockage b: World.blocks)
            {
               b.hit((int)(super.xPos+super.width/2),super.bottomBound, super.width*2);
            }
         }
         else if(super.yPos <= super.topBound)
         {
            super.yPos = super.topBound;
            super.ySpeed *= -1;
            for(Blockage b: World.blocks)
            {
               b.hit((int)(super.xPos+super.width/2),super.topBound, super.width*2);
            }
         }
         shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
         
         super.applyXMove();
         shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
         super.collisionDetectX();
         if(super.xPos + super.width >= super.rightBound)
         {
            super.xPos = super.rightBound-super.width;
            super.xSpeed *= -1;
            for(Blockage b: World.blocks)
            {
               b.hit(super.rightBound,(int)(super.yPos+super.height/2), super.height*2);
            }
         }
         else if(super.xPos <= super.leftBound)
         {
            super.xPos = super.leftBound;
            super.xSpeed *= -1;
            for(Blockage b: World.blocks)
            {
               b.hit(super.leftBound,(int)(super.yPos+super.height/2), super.height*2);
            }
         }
         shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
         if(super.xSpeed == 0)
         {
            World.shots.remove(this);
         }
      }
      //straight
      else
      {
         super.applyXMove();
         shape = World.translateArea(shape,(int)super.xPos,(int)super.yPos);
         super.collisionDetectX();
         if(super.xPos+super.width >= super.rightBound || super.xPos <= super.leftBound)
         {
            if(super.xPos+super.width >= super.rightBound)
            {
               for(Blockage b: World.blocks)
               {
                  b.hit(super.rightBound,(int)(super.yPos+super.height/2), super.height*2);
               }
            }
            else
            {
               for(Blockage b: World.blocks)
               {
                  b.hit(super.leftBound,(int)(super.yPos+super.height/2), super.height*2);
               }
            }
            World.shots.remove(this);
         }
      }
   }
}