package sma.actionsBehaviours;

import org.jpl7.Query;

import com.jme3.math.Vector3f;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

public class AttackJ48 extends TickerBehaviour{
	
	private static final long serialVersionUID = 4340498260100499548L;
	
	public static long FORGET_TIME = 35;;
	
	FinalAgent agent;
	
	String enemy;
	long lastTimeSeen;
	Vector3f lastPosition;
	public static PrologJ48Behavior BV;
	
	public static boolean openFire = false;

	public AttackJ48(Agent a, long period, String enemy,PrologJ48Behavior BV) {
		super(a, period);
		this.enemy = enemy;
		agent = (FinalAgent)((AbstractAgent)a);
		lastPosition = agent.getEnemyLocation(enemy);
		lastTimeSeen = System.currentTimeMillis();
		openFire = false;
		this.BV = BV;
		System.out.println("Player AttackJ48");
	}

	

	@Override
	protected void onTick() {
		
		Vector3f bestPosition = BV.seePremisse();
		if (bestPosition != null)
			agent.moveTo(bestPosition);
		askForSeeFirePermission();
	
		agent.goTo(lastPosition);
		
		if(agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)){
			lastTimeSeen = System.currentTimeMillis();
			lastPosition = agent.getEnemyLocation(enemy);
			agent.lookAt(lastPosition);
			if (openFire){
				System.out.println("Enemy visible, FIRE !");
				agent.lastAction = Situation.SHOOT;
				agent.shoot(enemy);
				
			}
			
		}else{
			if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME * getPeriod()){
				System.out.println("The enemy ran away");
				agent.removeBehaviour(this);
				agent.currentBehavior = null;
			}
			agent.lastAction = Situation.FOLLOW;
			
		}
	}
	
	public static void askForFirePermission(){
		String query = "toOpenFire("
					+PrologBehavior.sit.enemyInSight +","
					+PrologBehavior.sit.impactProba+")";
		
		openFire = Query.hasSolution(query);
	}
	
	public static void askForSeeFirePermission(){
		String query = "see("
					+BV.sit.enemyInSight +","
					+BV.sit.impactProba+")";
		
		openFire = Query.hasSolution(query);
		System.out.println("****"+query+"="+openFire);
	}
}














