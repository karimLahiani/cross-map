package sma.actionsBehaviours;

import org.jpl7.Query;
import org.lwjgl.Sys;

import com.jme3.math.Vector3f;

import dataStructures.tuple.Tuple2;
import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.agents.FinalAgent;
import weka.Parser;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.bayes.BayesNet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrologJ48Behavior extends TickerBehaviour {

	private static final long serialVersionUID = 5739600674796316846L;

	public static FinalAgent agent;
	public static Class nextBehavior;
	//public static J48 cls;
	public static BayesNet cls;
	
	public static Situation sit;

	public PrologJ48Behavior(Agent a, long period, Object cls) {
		super(a, period);
		agent = (FinalAgent) ((AbstractAgent) a);
		//this.cls = (J48) (cls);
		this.cls = (BayesNet) (cls);
	}

	@Override
	protected void onTick() {
		try {
			String prolog = "consult('./ressources/prolog/duel/requeteWeka.pl')";

			if (!Query.hasSolution(prolog)) {
				System.out.println("Cannot open file " + prolog);
			} else {
				sit = Situation.getCurrentSituation(agent);
				
				List<String> behavior = Arrays.asList("explore", "hunt", "attack");
				ArrayList<Object> terms = new ArrayList<Object>();
				

				for (String b : behavior) {
					terms.clear();
					// Get parameters
					if (b.equals("explore")) {
						terms.add(sit.timeSinceLastShot);
						terms.add(((HighestPlaceBehavior.prlNextOffend) ? sit.offSize : sit.defSize));
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
					} else if (b.equals("hunt")) {
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
						terms.add(sit.offSize);
						terms.add(sit.defSize);
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
						terms.add(sit.enemyInSight);
					} else if (b.equals("attack")) {
						// terms.add(sit.life);
						terms.add(sit.enemyInSight);
						// terms.add(sit.impactProba);
					} else { // RETREAT
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
					}

					String query = prologQuery(b, terms);
					if (Query.hasSolution(query)) {
						// System.out.println("has solution");
						setNextBehavior();

					}
				}
			}
		} catch (Exception e) {
			System.err.println("Behaviour file for Prolog agent not found");
			System.exit(0);
		}
	}

	public static Vector3f seePremisse() {
		ArrayList<Vector3f> neighbors = agent.sphereCast(agent.getSpatial(), 
				AbstractAgent.NEIGHBORHOOD_DISTANCE,AbstractAgent.CLOSE_PRECISION,
				AbstractAgent.VISION_ANGLE);
		String[] filtre = { "victory", "defeat" };
		
		String str = "";

		try {
			// load data an existing .arff to not recreate one (we only need the attributs)
			String path = System.getProperty("user.dir")+ "/ressources/learningBase/";
			
			DataSource sourceTmp = new DataSource(path+ "end.arff");
			Instances dataTmp = sourceTmp.getDataSet();
			// delete all instances
			dataTmp.delete();
			str += dataTmp.toString();
			Vector3f currentPosition = agent.getCurrentPosition();
			for (Vector3f neighbor : neighbors) {
				agent.teleport(neighbor);
				sit = sit.getCurrentSituation(agent);
				str += sit.offSize + "," + sit.defSize + "," + sit.offValue + "," + sit.defValue + "," +sit.averageAltitude+ ","+ sit.minAltitude
						+ "," + sit.maxAltitude + "," + sit.currentAltitude + "," + sit.fovValue + "," + sit.lastAction
						+ "," + sit.life + "," + sit.impactProba + ",?\n";
			}
			agent.teleport(currentPosition);
			sit = sit.getCurrentSituation(agent);
			FileWriter myWriter = new FileWriter(path + "noClassed.arff");
			myWriter.write(str);

			myWriter.close();
			
			DataSource source = new DataSource(path + "noClassed.arff");
			Instances instances = source.getDataSet();

			instances.setClassIndex(instances.numAttributes() - 1);
			Instances toLabel = new Instances(instances);
			double res;
			for (int i= 0; i<instances.numInstances(); i++) {
				res = cls.classifyInstance(instances.instance(i));
				
				toLabel.instance(i).setClassValue(res);
				
				if (instances.classAttribute().value((int)(res)).equals("VICTORY"))
					return neighbors.get(i);		
			}
		} catch (Exception e) {
			System.out.println("PRologJ48BV data file not found");
			e.printStackTrace();
		}
		return null;
	}

	public void setNextBehavior() {

		if (agent.currentBehavior != null && nextBehavior == agent.currentBehavior.getClass()) {
			return;
		}
		if (agent.currentBehavior != null) {
			agent.removeBehaviour(agent.currentBehavior);
		}

		if (nextBehavior == HighestPlaceBehavior.class) {
			HighestPlaceBehavior ex = new HighestPlaceBehavior(agent, FinalAgent.PERIOD);
			agent.addBehaviour(ex);
			agent.currentBehavior = ex;

		} else if (nextBehavior == HuntBehavior.class) {
			HuntBehavior h = new HuntBehavior(agent, FinalAgent.PERIOD);
			agent.currentBehavior = h;
			agent.addBehaviour(h);

		} else if (nextBehavior == AttackJ48.class) {

			AttackJ48 a = new AttackJ48(agent, FinalAgent.PERIOD, sit.enemy, this);
			agent.currentBehavior = a;
			agent.addBehaviour(a);

		}

	}

	public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t : terms) {
			query += t + ",";
		}
		return query.substring(0, query.length() - 1) + ")";
	}

	public static void executeExplore() {
		// System.out.println("explore");
		nextBehavior = HighestPlaceBehavior.class;
	}

	public static void executeHunt() {
		// System.out.println("hunt");
		nextBehavior = HuntBehavior.class;
	}

	public static void executeAttack() {
		// System.out.println("attack");
		nextBehavior = AttackJ48.class;
	}

	public static void executeRetreat() {
		// System.out.println("retreat");
		// nextBehavior = RetreatBehavior.class;
	}

}