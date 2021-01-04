package weka;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class WekaJava {

	public static J48 classification(String[] filtre)  {
		String fileName = "end";
		String path;
		J48 cls =null;
		try {
			String ressourcesDir = "/ressources/learningBase/";
			path = System.getProperty("user.dir") + ressourcesDir+ fileName+".arff";
			if (! (new File(path)).exists())
				Parser.saveARFF(fileName, filtre);
			// load data
			DataSource source = new DataSource(path);
			Instances data = source.getDataSet();
			// Instances train = new Instances(data, 0, trainSize);
			// Instances test = new Instances(data, trainSize, data.numInstances() -
			// trainSize);

			System.out.println("WELL LOAD OFF DATA FOR :" + path);
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);

			// if (train.classIndex() == -1)
			// train.setClassIndex(train.numAttributes() - 1);
			// if (test.classIndex() == -1)
			// test.setClassIndex(test.numAttributes() - 1);

			// remove attributs
			//
			// OffDataSize 1
			// DefDataSize 2
			// OffDataValue 3
			// DefDataValue 4
			// AvgAltitude 5
			// MinAltitude 6
			// MaxAltitude 7
			// CurrentAltitude 8
			// FovValue 9
			// LastAction 10
			// Life 11
			// ImpactProba 12
			// res 13
			// filtre attributs
			
			int[] indicesOfColumnsToUse = {4,5,6,7, 8, 9, 11, 12};

			//Remove remove = new Remove();
			//remove.setAttributeIndicesArray(indicesOfColumnsToUse);
			//remove.setInvertSelection(true);
			//remove.setInputFormat(data);
			//data = Filter.useFilter(data, remove);
			
			
			cls = new J48();
			cls.setUnpruned(true);
			cls.buildClassifier(data);
			
			//System.out.println(cls.graph());
			//visualize((J48) cls);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		// int trainSize = (int) (1 * data.numInstances());
		
		return cls;

	}

	public static void visualize(J48 j48) {
		
		try {
			final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
			jf.setSize(500, 400);
			jf.getContentPane().setLayout(new BorderLayout());
			TreeVisualizer tv;
			tv = new TreeVisualizer(null, j48.graph(), new PlaceNode2());
			jf.getContentPane().add(tv, BorderLayout.CENTER);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
				}
			});
			jf.setVisible(true);
			tv.fitToScreen();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
