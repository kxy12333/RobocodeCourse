package cl有;
import robocode.*;
import java.awt.*;
/**
 * Cly - a robot by (cly)
 */
public class Cly extends AdvancedRobot
{
    Enemy enemy=new Enemy();
	double pi =Math.PI;
	public void run()
	{
	    setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		this.setColors(Color.red, Color.blue, Color.yellow, Color.black, Color.green);
		while(true)
		{
		    if(enemy.name==null){
               setTurnRadarRightRadians(2*pi);
			   execute();
           }else{
               execute();
           }
		   setAhead(100);
		   setTurnRight(90);
		   execute();
		}
	}

    public void onScannedRobot(ScannedRobotEvent e)
    {
       enemy.update(e,this);
       double Offset = rectify( enemy.direction -getRadarHeadingRadians() );
       setTurnRadarRightRadians( Offset *1.5);
	   attach();
    }

    public void attach(){
        setFire(1);
}

    public  double rectify(double angle)
    {
         if ( angle < -Math.PI )
                angle += 2*Math.PI;
         if ( angle > Math.PI )
                angle -= 2*Math.PI;
         return angle;
    }

    public class Enemy {
      public double x,y;
      public String name = null;
      public double headingRadian = 0.0D;
        public double bearingRadian = 0.0D;
        public double distance = 1000D;
        public double direction = 0.0D;
        public double velocity = 0.0D;
        public double prevHeadingRadian = 0.0D;
        public double energy = 100.0D;
        public void update(ScannedRobotEvent e,AdvancedRobot me){
          name = e.getName();
          headingRadian =e.getHeadingRadians();
          bearingRadian = e.getBearingRadians();
          this.energy = e.getEnergy();
          this.velocity = e.getVelocity();
          this.distance = e.getDistance();
          direction = bearingRadian +me.getHeadingRadians();
          x = me.getX() + Math.sin( direction ) * distance;
          y=  me.getY() + Math.cos( direction ) * distance;     
        }
   }
}

