package Frist;
import robocode.*;
import java.awt.Color;


public class Atopos extends AdvancedRobot
{

	Enemy target;                   //our current enemy  代表对手，包括了对手的所有有用参数
	final double PI = Math.PI;           //just a constant
	int direction = 1;               //direction we are heading...1 = forward, -1 = backwards
	//我们坦克车头的方向
	double firePower;                    //the power of the shot we will be using - set by do firePower() 设置我们的火力

	public void run()
	{
		target = new Enemy();               //实例化Enemy()类
		target.distance = 100000;           //initialise the distance so that we can select a target
		setColors(Color.red,Color.blue,Color.green);    //sets the colours of the robot
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2*PI);            //turns the radar right around to get a view of the field 以弧度计算旋转一周

		while(true)
		{
			doMovement();               //Move the bot 移动机器人
			doFirePower();              //select the fire power to use 选择火力
			doScanner();                //Oscillate the scanner over the bot 扫描
			doGun();                    //move the gun to predict where the enemy will be 预测敌人，调整炮管
			out.println(target.distance);
			fire(firePower);            //所有动作完成后，开火
			execute();              //execute all commands  上面使用的都为AdvancedRobot类中的非阻塞调用
			//控制权在我们，所有这里用阻塞方法返回控制给机器人
		}
	}

	void doFirePower()
	{
		firePower = 400/target.distance; //selects a bullet power based on our distance away from the target
		//根据敌人距离来选择火力,因为本身前进，后退为300，所以火力不会过大
	}

	void doMovement()
	{
		if (getTime()%20 == 0)  //过20的倍数时间就反转方向
		{
			//every twenty 'ticks'
			direction *= -1;        //reverse direction
			setAhead(direction*300);    //move in that direction
		}
		setTurnRightRadians(target.bearing + (PI/2)); //every turn move to circle strafe the enemy
		//每一时间周期以敌人为中心绕圆运动
	}
	/*
	 * this scanner method allows us to make our scanner track our target.
	 * it will track to where our target is at the moment, and some further
	 * in case the target has moved.  This way we always get up to the minute
	 * information on our target   雷达锁定目标
	 */
	void doScanner()
	{
		double radarOffset;  //雷达偏移量
		if (getTime() - target.ctime > 4)
		{
			radarOffset = 360;      //rotate the radar to find a target
		}
		else
		{
			radarOffset = getRadarHeadingRadians() - absbearing(getX(),getY(),target.x,target.y);
			if (radarOffset < 0)
				radarOffset -= PI/8;  //(0.375)
			else
				radarOffset += PI/8;
		}
		//turn the radar
		setTurnRadarLeftRadians(NormaliseBearing(radarOffset)); //左转调整转动角度到PI内
	}

	void doGun()
	{
		//works out how long it would take a bullet to travel to where the enemy is *now*
		//this is the best estimation we have
		//计算子弹到达目标的时间长speed = 20 - 3 * power;有计算公式,距离除速度=时间
		long time = getTime() + (int)(target.distance/(20-(3*firePower)));
		//offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
		//以直线为目标，偏移子弹下一次发射的角度。（这样让子弹射空的几率减少。但对付不动的和做圆运动的机器人有问题）
		//target.guesssX(),target.guessY()为目标移动后的坐标
		double gunOffset = getGunHeadingRadians() - absbearing(getX(),getY(),target.guessX(time),target.guessY(time));
		setTurnGunLeftRadians(NormaliseBearing(gunOffset));  //调整相对角度到2PI内
	}

	double NormaliseBearing(double ang)
	{
		if (ang > PI)
			ang -= 2*PI;
		if (ang < -PI)
			ang += 2*PI;
		return ang;
	}
	//if a heading is not within the 0 to 2pi range, alters it to provide the shortest angle
	double NormaliseHeading(double ang)
	{
		if (ang > 2*PI)
			ang -= 2*PI;
		if (ang < 0)
			ang += 2*PI;
		return ang;
	}
	//returns the distance between two x,y coordinates '**'
	//以两边长求得与对手之间的距离
	public double getrange( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;
	}
	//gets the absolute bearing between to x,y coordinates
	//根据x,y的坐标求出绝对角度，见"坐标锁定"利用直角坐标系来反求出角度。
	public double absbearing( double x1,double y1, double x2,double y2 )
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = getrange( x1,y1, x2,y2 );
		if( xo > 0 && yo > 0 )
		{
			return Math.asin( xo / h );
		}
		if( xo > 0 && yo < 0 )
		{
			return Math.PI - Math.asin( xo / h ); //x为正,y为负第二象限角
		}
		if( xo < 0 && yo < 0 )
		{
			return Math.PI + Math.asin( -xo / h ); //第三象限内180+角度
		}
		if( xo < 0 && yo > 0 )
		{
			return 2.0*Math.PI - Math.asin( -xo / h ); //四象限360-角度
		}
		return 0;
	}
	/**
	 * onScannedRobot: What to do when you see another robot
	 * 扫描事件,也是初始化目标数据的过程
	 */
	public void onScannedRobot(ScannedRobotEvent e)
	{
		//if we have found a closer robot....
		if ((e.getDistance() < target.distance)||(target.name == e.getName()))
		{
			//the next line gets the absolute bearing to the point where the bot is
			//求得对手的绝对弧度
			double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);
			//this section sets all the information about our target
			target.name = e.getName();
			//求得对手的x,y坐标，见"robocode基本原理之坐标锁定"文章
			target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is
			target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
			target.bearing = e.getBearingRadians();
			target.head = e.getHeadingRadians();
			target.ctime = getTime();               //game time at which this scan was produced 扫描到机器人的游戏时间
			target.speed = e.getVelocity();         //得到敌人速度
			target.distance = e.getDistance();
		}
	}
	public void onRobotDeath(RobotDeathEvent e)
	{
		if (e.getName() == target.name)
			target.distance = 10000; //this will effectively make it search for a new target
	}

}

class Enemy
{
	String name;
	public double bearing;
	public double head;
	public long ctime; //game time that the scan was produced
	public double speed;
	public double x,y;
	public double distance;
	public double guessX(long when)
	{
		//以扫描时和子弹到达的时间差 ＊ 最大速度=距离, 再用对手的坐标加上移动坐标得到敌人移动后的坐标
		long diff = when - ctime;
		return x+Math.sin(head)*speed*diff; //目标移动后的坐标
	}
	public double guessY(long when)
	{
		long diff = when - ctime;
		return y+Math.cos(head)*speed*diff;
	}
}