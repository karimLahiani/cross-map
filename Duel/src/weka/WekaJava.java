package weka;

import java.awt.BorderLayout;
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

	public static J48 training(String[] args) throws Exception {
		String[] filtre = { "victory", "defeat" };
		String fileName = "end";
		String path = Parser.saveARFF(fileName, filtre);

		// load data
		DataSource source = new DataSource(path);
		Instances data = source.getDataSet();

		// int trainSize = (int) (1 * data.numInstances());
		// Instances train = new Instances(data, 0, trainSize);
		// Instances test = new Instances(data, trainSize, data.numInstances() -
		// trainSize);

		System.out.println("WELL LOAD OFF DATA FOR\n" + path);
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
		
		int[] indicesOfColumnsToUse = {7, 8, 9, 11, 12};

		Remove remove = new Remove();
		remove.setAttributeIndicesArray(indicesOfColumnsToUse);
		remove.setInvertSelection(true);
		remove.setInputFormat(data);
		Instances dataSubset = Filter.useFilter(data, remove);
		
		
		J48 cls = new J48();
		cls.setUnpruned(true);
		cls.buildClassifier(dataSubset);
		
		//System.out.println(cls.graph());
		//visualize((J48) cls);
		return cls;

	}

	public static void visualize(J48 j48) throws Exception {
		final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
		jf.setSize(500, 400);
		jf.getContentPane().setLayout(new BorderLayout());
		TreeVisualizer tv = new TreeVisualizer(null, j48.graph(), new PlaceNode2());
		jf.getContentPane().add(tv, BorderLayout.CENTER);
		jf.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				jf.dispose();
			}
		});

		jf.setVisible(true);
		tv.fitToScreen();

	}
}
