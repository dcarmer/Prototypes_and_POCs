import java.awt.image.BufferStrategy;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
//import java.awt.geom.Ellipse2D.Double;
public class MazeRunner
{
   public static void main(String[] args)
   {
      /* Utility Setup */
      //Random r = new Random();
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
      World level = new World();
      Game:
      while(true)
      {
         World.paws(1);
         if(World.keyStat[KeyEvent.VK_ESCAPE])
         {
            System.exit(0);            
         }
         do
         {
            do
            {
               Graphics2D g2D = (Graphics2D)bs.getDrawGraphics();
               g2D.setColor(Color.WHITE);
               g2D.fillRect(0,0,World.XRES,World.YRES);
               g2D.setColor(Color.BLACK);
               g2D.fill(World.Maze);
               for(Wall w: World.walls)
               {
                  g2D.fillRect(w.xPos,w.yPos,w.width,w.height);
               }
               for(Tank t: World.tanks)
               {
                  if(t.life==0)
                  {
                     break Game;
                  }
                  int barralW = 6;
                  g2D.setColor(Color.GREEN);
                  g2D.fillRect(t.xPos,t.yPos-10,(int)(World.tWidth*t.getLifeRatio()),5);
                  g2D.setColor(t.hue);
                  g2D.fillRect(t.xPos+barralW,t.yPos+barralW,World.tWidth-(2*barralW),World.tHeight-(2*barralW));
                  g2D.setColor(Color.BLACK);
                  g2D.drawRect(t.xPos,t.yPos-10,World.tWidth,5);
                  g2D.fill(new Ellipse2D.Double(t.xPos+World.tWidth/4,t.yPos+World.tHeight/4,World.tWidth/2,World.tHeight/2));
                  int centX = t.xPos+World.tWidth/2;
                  int centY = t.yPos+World.tHeight/2;
                  if(t.angle==0)
                  {
                     g2D.fillRect(t.xPos,t.yPos,World.tWidth,2*barralW);
                     g2D.fillRect(t.xPos,t.yPos+World.tHeight,World.tWidth,-2*barralW);
                     g2D.fillRect(centX,centY-barralW/2,World.tWidth/2,barralW);
                  }
                  else if(t.angle==90)
                  {
                     g2D.fillRect(t.xPos,t.yPos,2*barralW,World.tHeight);
                     g2D.fillRect(t.xPos+World.tWidth,t.yPos,-2*barralW,World.tHeight);
                     g2D.fillRect(centX-barralW/2,centY,barralW,-World.tHeight/2);
                  }
                  else if(t.angle==180)
                  {
                     g2D.fillRect(t.xPos,t.yPos,World.tWidth,2*barralW);
                     g2D.fillRect(t.xPos,t.yPos+World.tHeight,World.tWidth,-2*barralW);
                     g2D.fillRect(centX,centY-barralW/2,-World.tWidth/2,barralW);
                  }
                  else
                  {
                     g2D.fillRect(t.xPos,t.yPos,2*barralW,World.tHeight);
                     g2D.fillRect(t.xPos+World.tWidth,t.yPos,-2*barralW,World.tHeight);
                     g2D.fillRect(centX-barralW/2,centY,barralW,World.tHeight/2);
                  }
                  t.move();
                  t.shoot();
               }
               for(int i=World.shells.size()-1;i>-1;i--)
               {
                  Shell s = World.shells.get(i);
                  g2D.setColor(s.hue);
                  g2D.fillRect(s.xPos-World.sWidth/2,s.yPos-World.sHeight/2,World.sWidth,World.sHeight);
                  s.move();
               }
               for(int i =0;i<World.nodes.length;i++)
               {
                  for(int j=0;j<World.nodes[i].length;j++)
                  {
                     if(World.nodes[i][j] != null)
                     g2D.fillRect((int)World.nodes[i][j].getX()-1,(int)World.nodes[i][j].getY()-1,2,2);
                  }
               }
               for(int i=World.shells.size()-1;i>-1;i--)
               {
                  Shell s = World.shells.get(i);
                  for(Tank t: World.tanks)
                  {
                     if(!t.name.equals(s.shooter))
                     {
                        if(t.xPos<s.xPos && t.xPos+World.tWidth>s.xPos)
                        {
                           if(t.yPos<s.yPos && t.yPos+World.tHeight>s.yPos)
                           {
                              t.life-=World.DAMAGE;
                              World.shells.remove(s);
                           }
                        }
                     }
                  }
               }
               
               g2D.dispose();
            }while(bs.contentsRestored());
            bs.show();
         }while(bs.contentsLost());
      }
      System.exit(0);
   }      
}