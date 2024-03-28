package SWbot;
import robocode.*;
import robocode.Robot;
import java.awt.*;
import java.awt.Color;
public class SWBot extends AdvancedRobot
{
	int time = 0;			// 用于重启缩敌计时，防止敌人死后无锁定敌人而挂机
	public static double PI = Math.PI;
	Enemy enemy = new Enemy();
	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);		// 炮身分离
		// 涂色，子弹黑色，剩下都红色
		setBodyColor(new Color(255, 0, 0));
		setGunColor(new Color(255, 0, 0));
		setRadarColor(new Color(255, 0, 0));
		setScanColor(new Color(255, 0, 0));
		setBulletColor(new Color(0, 0, 0));

		while(true) {
			// Replace the next 4 lines with any behavior you would like
			if(enemy.name == null) {		// 没有敌人则转动炮塔扫描
				setTurnGunRightRadians(2 * PI);		// 炮塔转动，炮塔与扫描仪一体，方便进攻
				execute();
			}else if (time == 10)		// 达到一定时间后刷新敌人，重新锁定
				enemy.name = null;
			else{
				time++;					// 找到敌人后时间也会增加
				execute();
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		enemy.update(e, this);				// 更新敌人数据
		double Offset = rectify( enemy.direction -
				getRadarHeadingRadians() );			// 纠正角度
		setTurnGunRightRadians(Offset * 1.5);	// 转动炮塔
		if(enemy.distance > 150)
			setAhead(40);
		fire(3);							// 开火
		setAhead(40);							// 前进
		setTurnRight(40);						// 左转
		execute();								// 交替互动，实现无规则运动
	}
	public double rectify ( double angle )
	{
		if ( angle < -Math.PI )
			angle += 2*Math.PI;
		if ( angle > Math.PI )
			angle -= 2*Math.PI;
		return angle;
	}

	public void onHitByBullet(HitByBulletEvent e) {

	}
	
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		setBack(10);
		setAhead(10);
	}
	public class Enemy {
		public double x, y;
		public String name = null;
		public double headingRadian = 0.0D;
		public double bearingRadian = 0.0D;
		public double distance = 1000D;
		public double direction = 0.0D;
		public double velocity = 0.0D;
		public double prevHeadingRadian = 0.0D;
		public double energy = 100.0D;


		public void update(ScannedRobotEvent e, AdvancedRobot me) {
			name = e.getName();
			headingRadian = e.getHeadingRadians();
			bearingRadian = e.getBearingRadians();
			this.energy = e.getEnergy();
			this.velocity = e.getVelocity();
			this.distance = e.getDistance();
			direction = bearingRadian +
					me.getHeadingRadians();
			x = me.getX() + Math.sin(direction) * distance;
			y = me.getY() + Math.cos(direction) * distance;
		}
	}
}
