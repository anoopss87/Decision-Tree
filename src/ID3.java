import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class ID3
{
	private static ArrayList<Integer>[] data;
	private static ArrayList<Integer>[] indexer;
	private static HashMap<Integer, String> header = new HashMap<>();
	private static HashMap<String, Integer> header2 = new HashMap<>();
	private static int attrSize;	
	private static TNode root = null;
	private static int instanceCount = 0;
	@SuppressWarnings("unused")
	private static int posCount = 0;
	@SuppressWarnings("unused")
	private static int negCount = 0;
	private static int corCount = 0;
	private static int incorCount = 0;
	private static int leafCount = 0;
	private static int nodeCount = 0;
	private static int treeDepth = 0;
	private static int pruneCount = 0;
	private static Random rand = new Random();
	
	public static void resetTreeAttrs()
	{
		root = null;
		leafCount = 0;
		nodeCount = 0;
		treeDepth = 0;
	}
	
	public static void resetAttrs()
	{
		for(int i=0;i<data.length;++i)
		{
			data[i].clear();
		}
		
		for(int i=0;i<indexer.length;++i)
		{
			indexer[i].clear();
		}
		header.clear();
		header2.clear();		
		
		attrSize = 0;		
		instanceCount = 0;
		posCount = 0;
		negCount = 0;
		corCount = 0;
		incorCount = 0;		
		pruneCount = 0;
	}
	
	public static ArrayList<Integer> intersect(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		int len1 = l1.size();
		int len2 = l2.size();
		
		if(len1 > len2)
		{
			for(Integer i : l2)
			{
				if(l1.contains(i))
					res.add(i);			
			}
		}
		else
		{
			for(Integer i : l1)
			{
				if(l2.contains(i))
					res.add(i);			
			}
		}
		return res;
	}
	
	public static ArrayList<Integer> intersectAll(ArrayList<Integer> index)
	{
		ArrayList<Integer> res = new ArrayList<Integer>();
		res = indexer[index.get(0)];
		for(int i = 1;i<index.size();i++)
		{
			res = intersect(res, indexer[index.get(i)]);			
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private static void parseData(BufferedReader reader) throws IOException {
		
		//first line is header
		String line = reader.readLine();	
		
		String[] words = line.split("\\s+");
		int index = 0;
		for(String word : words)
		{
			header.put(index, word);
			header2.put(word, index++);
		}
		
		//number of attributes including label
		attrSize = header.size();		
		
		data = (ArrayList<Integer>[])new ArrayList[attrSize];
		
		//stores the indexes of 0's and 1's for each attribute
		indexer = (ArrayList<Integer>[])new ArrayList[attrSize * 2];
		for(int i=0;i<attrSize;++i)
		{
			data[i] = new ArrayList<Integer>();
			indexer[2*i] = new ArrayList<Integer>();
			indexer[2*i+1] = new ArrayList<Integer>();
		}		
		
		line = reader.readLine();		
		while(line != null)
		{			
			String[] values = line.split("\\s+");
						
			for(int i=0;i<values.length;i++)
			{
				int temp = Integer.parseInt(values[i]);
				data[i].add(temp);
				if(temp == 0)
				{
					indexer[2*i].add(instanceCount);
				}
				else
				{
					indexer[2*i+1].add(instanceCount);
				}				
			}			
			line = reader.readLine();
			
			if(values.length > 0)
				instanceCount++;
		}
	}	
	
	private static double log2(double value)
	{
		if(value == 0){
			return 0.;
		}

		return Math.log10(value)/Math.log10(2.);
	}
	
	public static double calcEntropy(double pos, double neg)
	{
		if(pos == 0 || neg == 0)
			return 0.0;
		
		double sum = pos + neg;
		double gain = -pos * log2(pos/sum) / sum - neg * log2(neg/sum) / sum;
		return gain;
	}
	
	public static int getNumberofZerosLabel(ArrayList<Integer> list)
	{
		int count = 0;
		for(Integer i : list)
		{
			if(data[attrSize-1].get(i) == 0)
				count++;
		}
		return count;
	}
	
	public static void preOrder(TNode node)
	{
		if(node != null)
		{
			if(node.getName().isEmpty())
			{
				System.out.println(node.getIndex());				
			}
			else
			{
				System.out.print(node.getName() + " = " + node.dir + " :");
				if(node.getLeft() == null && node.getRight() == null)
					System.out.println(node.index);
				else
					System.out.println();				
			}
			preOrder(node.getLeft());
			preOrder(node.getRight());
		}
	}
	
	public static void printTree(TNode tmpRoot, boolean prune)
	{
        Queue<TNode> currentLevel = new LinkedList<TNode>();
        Queue<TNode> nextLevel = new LinkedList<TNode>();

        currentLevel.add(tmpRoot);
        System.out.println("=======================================================");
        if(prune)
        	System.out.println("Decision tree level by level post pruning: ");
        else
        	System.out.println("Decision tree level by level pre pruning: ");

        while (!currentLevel.isEmpty())
        {
            Iterator<TNode> iter = currentLevel.iterator();
            while (iter.hasNext())
            {
            	TNode currentNode = iter.next();
                if (currentNode.left != null)
                {
                    nextLevel.add(currentNode.left);
                }
                if (currentNode.right != null)
                {
                    nextLevel.add(currentNode.right);
                }
                if(currentNode.getName().isEmpty())
    				System.out.print(currentNode.getIndex() + "->" + currentNode.negCnt + "," + currentNode.posCnt + " ");
    			else
    				System.out.print(currentNode.getName() + "->" + currentNode.negCnt + "," + currentNode.posCnt + " ");
                
                if(!prune)
                	nodeCount++;
            }
            System.out.println();
            
            if(!prune)
            	treeDepth++;
            currentLevel = nextLevel;
            nextLevel = new LinkedList<TNode>();
        }
        System.out.println("=======================================================");
    }
	
	public static int randomAttr(HashSet<Integer> attrList)
	{
		int size = attrList.size();
		int ind = -1;
		
		ArrayList<Integer> a1 = new ArrayList<Integer>();
		for(Integer i : attrList)
		{
			a1.add(i);
		}
		
		if(size >= 1)
		{
			int r = rand.nextInt(size);
			ind = a1.get(r);
		}
		//System.out.println("------ " + ind);
		return ind;
	}
	
	public static int bestG(HashSet<Integer> attrList, ArrayList<Integer> labelSampleSpace)
	{
		double maxGain = Integer.MIN_VALUE;
		double curGain = 0;
		int index = -1;
		
		int noOfZeroesParentEnt = getNumberofZerosLabel(labelSampleSpace);
		int noOfOnesParentEnt = labelSampleSpace.size() - noOfZeroesParentEnt;
		double parEntropy = calcEntropy(noOfZeroesParentEnt, noOfOnesParentEnt);
		//System.out.println("....... " + noOfZeroesParentEnt + " " + noOfOnesParentEnt + " " + parEntropy);
		
		if(parEntropy > 0)
		{
			for(Integer ind : attrList)
			{
				ArrayList<Integer> attrZerosList = intersect(indexer[2 * ind], labelSampleSpace);
				ArrayList<Integer> attrOnesList = intersect(indexer[(2 * ind) + 1], labelSampleSpace);
				double numOfZerosAttr = attrZerosList.size();
				double numOfOnesAttr = attrOnesList.size();
				double w1 = numOfZerosAttr / (numOfZerosAttr + numOfOnesAttr);
				double w2 = numOfOnesAttr / (numOfZerosAttr + numOfOnesAttr);
				
				double v1 = getNumberofZerosLabel(attrZerosList);
				double v2 = numOfZerosAttr - v1;
				double v3 = getNumberofZerosLabel(attrOnesList);
				double v4 = numOfOnesAttr - v3;
				double e1 = 0.0, e2 = 0.0;
				if(w1 != 0)
					e1 = calcEntropy(v1, v2);
				
				if(w2 != 0)
					e2 = calcEntropy(v3, v4);
				curGain = parEntropy - (w1 * e1) - (w2 * e2);
				//System.out.println(w1 + " " + w2 + " " + v1 + " " + v2 + " " + v3 + " " + v4 + "  " + curGain);
				if(curGain > maxGain)
				{
					maxGain = curGain;
					index = ind;
				}
			}
		}
		return index;
	}	
	
	public static void ID3Alg(HashSet<Integer> aList, TNode node, ArrayList<Integer> sampleSpace, boolean random)
	{
		if(aList.size() == 0)
		{
			//System.out.println(sampleSpace);
			if(!sampleSpace.isEmpty())
			{
				int val = data[attrSize-1].get(sampleSpace.get(0));
				node.setIndex(val);
			}
			
			int neg = getNumberofZerosLabel(sampleSpace);
			int pos = sampleSpace.size() - neg;
			
			if(neg > 0)
			{
				negCount += neg;
				node.negCnt = neg;
			}
			
			if(pos > 0)
			{
				posCount += pos;
				node.posCnt = pos;
			}
			leafCount++;
			return;
		}
		
		if(aList.size() == attrSize-1)
		{
			int neg = getNumberofZerosLabel(sampleSpace);
			int pos = sampleSpace.size() - neg;			
			int bestInd = -1;
			
			if(!random)
				bestInd = bestG(aList, sampleSpace);
			else
				bestInd = randomAttr(aList);
			TNode temp = new TNode(bestInd, header.get(bestInd));
			temp.posCnt = pos;
			temp.negCnt = neg;
			root = temp;
			
			if(aList.contains(bestInd))
				aList.remove(bestInd);
			
			TNode left = new TNode(0);
			left.dir = 0;
			TNode right = new TNode(1);
			right.dir = 1;
			root.setLeft(left);
			root.setRight(right);
			ArrayList<Integer> zeroSS = intersect(indexer[2 * bestInd], sampleSpace);
			ArrayList<Integer> oneSS = intersect(indexer[2 * bestInd + 1], sampleSpace);
			//int labZeros = getNumberofZerosLabel(zeroSS);
			//int labOnes = getNumberofZerosLabel(oneSS);
			
			//if(labZeros != 0 && labOnes != 0)
			{
				HashSet<Integer> a1 = new HashSet<Integer>(aList);
				HashSet<Integer> a2 = new HashSet<Integer>(aList);
				ID3Alg(a1, left, zeroSS, random);				
				ID3Alg(a2, right, oneSS, random);				
			}			
		}		
		else
		{
			int neg = getNumberofZerosLabel(sampleSpace);
			int pos = sampleSpace.size() - neg;
			
			int bestInd = -1;
			
			if(!random)
				bestInd = bestG(aList, sampleSpace);
			else
				bestInd = randomAttr(aList);			
							
			if(bestInd != -1)
			{
				node.setIndex(bestInd);
				node.setName(header.get(bestInd));	
				node.posCnt = pos;
				node.negCnt = neg;
				//System.out.print(aList + " " + bestInd);
				if(aList.contains(bestInd))
					aList.remove(bestInd);
				
				ArrayList<Integer> zeroSS = intersect(indexer[2 * bestInd], sampleSpace);
				ArrayList<Integer> oneSS = intersect(indexer[2 * bestInd + 1], sampleSpace);
				//int labZeros = getNumberofZerosLabel(zeroSS);
				//int labOnes = getNumberofZerosLabel(oneSS);
				
				//System.out.println(" => " + labZeros + " " + labOnes);
				//System.out.println(zeroSS + " " + oneSS);
				//if(labZeros != 0 && labOnes != 0)
				{
					TNode left = new TNode(0);
					left.dir = 0;
					TNode right = new TNode(1);
					right.dir = 1;
					node.setLeft(left);
					node.setRight(right);
				
					HashSet<Integer> a1 = new HashSet<Integer>(aList);
					HashSet<Integer> a2 = new HashSet<Integer>(aList);
					ID3Alg(a1, left, zeroSS,random);
					ID3Alg(a2, right, oneSS,random);
				}				
			}
			else
			{
				int labZeros = getNumberofZerosLabel(sampleSpace);
				int labOnes = sampleSpace.size() - labZeros;
								
				if(sampleSpace.size() == labZeros)
				{
					node.setIndex(0);
					node.negCnt = labZeros;
					//System.out.println("!!!!!!!!!!  " + labZeros);
					negCount += labZeros;
					leafCount++;
				}
				else if(labOnes == sampleSpace.size())
				{
					node.setIndex(1);
					node.posCnt = labOnes;
					//System.out.println("!!!!!!!!!!  " + labOnes);
					posCount += labOnes;
					leafCount++;
				}
				else
				{
					System.out.println("Neither........ " + labZeros + " " + labOnes);
				}
			}
		}
	}
	
	public static void pruning(TNode tmpRoot)
	{
		Queue<TNode> currentLevel = new LinkedList<TNode>();
        Queue<TNode> nextLevel = new LinkedList<TNode>();

        currentLevel.add(tmpRoot);
        int depth = 0;
        int count = 0;

        while (!currentLevel.isEmpty())
        {
            Iterator<TNode> iter = currentLevel.iterator();
            while (iter.hasNext())
            {
            	TNode currentNode = iter.next();
                if (currentNode.left != null)
                {
                    nextLevel.add(currentNode.left);
                }
                if (currentNode.right != null)
                {
                    nextLevel.add(currentNode.right);
                }
                
                if(depth == treeDepth - 6)
                {
                	if(!currentNode.getName().isEmpty())
                	{
                		int pos = currentNode.posCnt;
                		int neg = currentNode.negCnt;
                		
                		if(pos > neg)
                		{
                			currentNode.setName("");
                			currentNode.setIndex(1);
                			
                			TNode temp = currentNode;
                			
                			if(temp.getLeft() != null)
                			{
                				count++;
                				nodeCount--;
                				if(temp.getLeft().getLeft() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                				if(temp.getLeft().getRight() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                			}
                			
                			if(temp.getRight() != null)
                			{
                				count++;
                				nodeCount--;
                				if(temp.getRight().getLeft() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                				if(temp.getRight().getRight() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                			}
                			leafCount--;
                			currentNode.setLeft(null);
                			currentNode.setRight(null);                			
                		}
                		else
                		{
                			currentNode.setName("");
                			currentNode.setIndex(0);
                			
                			TNode temp = currentNode;
                			
                			if(temp.getLeft() != null)
                			{
                				count++;
                				nodeCount--;
                				if(temp.getLeft().getLeft() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                				if(temp.getLeft().getRight() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                			}
                			
                			if(temp.getRight() != null)
                			{
                				count++;
                				nodeCount--;
                				if(temp.getRight().getLeft() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                				if(temp.getRight().getRight() != null)
                				{
                					count++;
                					leafCount--;
                					nodeCount--;
                				}
                			}
                			leafCount--;
                			currentNode.setLeft(null);
                			currentNode.setRight(null);                			
                		}            			                		
                	}
                }
                
                if(count >= pruneCount)
                {
                	System.out.println("********************** " + count);
                	break;
                }
            }
            
            if(count >= pruneCount)
            {
            	break;
            }
            depth++;
            currentLevel = nextLevel;
            nextLevel = new LinkedList<TNode>();
        }
	}
	
	public static void calcAccuracy()
	{
		TNode node = root;
		for(int i=0;i<instanceCount;++i)
		{
			int val;
			while(node != null)
			{
				int ind = header2.get(node.getName());
				if(data[ind].get(i) == 0)
					node = node.getLeft();
				else
					node = node.getRight();
				
				val = node.getIndex();
				
				if(node.getName().isEmpty())
				{
					int label = data[attrSize-1].get(i);
					if(Math.abs(label - val) == 0)
					{
						corCount++;
					}
					else
					{
						incorCount++;
					}					
					break;
				}
			}
			node = root;
		}
	}
	
	public static void printResult(int val, boolean prune)
	{
		String str = "";
		String percent = "%";
		if(val == 0 || val == 2)
			str = "train";
		else
			str = "test";
		if(prune)
			System.out.println("Post Pruned Accuracy");
		else
		{
			if(val == 2 || val == 3)
				System.out.println("Pre Pruned Accuracy using random pick:");
			else
				System.out.println("Pre Pruned Accuracy");
		}
		
		System.out.println("===================================================================");
		
		System.out.format("Number of %sing instances = %d\n", str, instanceCount);
		System.out.format("Number of %sing attributes = %d\n", str, attrSize);
		System.out.format("Total number of nodes in the tree = %d\n", nodeCount);
		System.out.format("Total number of leaf in the tree = %d\n", leafCount);
		System.out.format("Depth of the tree = %d\n", treeDepth);
		System.out.format("Accuracy of the model on the %sing dataset = %f%s\n",str, (corCount / (double)(incorCount + corCount))*100, percent);
		
		System.out.println("====================================================================");
	}
	
	public static void updateData(String fName, HashSet<Integer> attrList, ArrayList<Integer> sampleSpace) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(fName));
		
		parseData(reader);			
		
		for(int i=0;i<attrSize-1;++i)
		{
			attrList.add(i);
		}
		
		for(int i=0;i<instanceCount;++i)
		{
			sampleSpace.add(i);
		}
		reader.close();	
	}	
	
	public static void main(String[] args) throws IOException
	{				
		HashSet<Integer> attrList = new HashSet<Integer>();
		ArrayList<Integer> sampleSpace = new ArrayList<Integer>();		
		
		//training data			
		updateData(args[0], attrList, sampleSpace);			
		ID3Alg(attrList, null, sampleSpace, false);		
		
		//print tree
		TNode nod = root;
		printTree(nod, false);			
		
		//calculate accuracy
		calcAccuracy();
		printResult(0, false);	
		
		//reset attrs for test data
		resetAttrs();
		attrList.clear();
		sampleSpace.clear();
		
		//test data
		updateData(args[1], attrList, sampleSpace);
		
		calcAccuracy();
		printResult(1, false);
		
		//read prune factor
		Double temp = nodeCount * Double.parseDouble(args[2]);
		pruneCount = temp.intValue();
		System.out.println("********* " + pruneCount);
		TNode node = root;
		
		//pruning
		pruning(node);
		
		//reset data
		resetAttrs();
		attrList.clear();
		sampleSpace.clear();
		
		//load train data
		updateData(args[0], attrList, sampleSpace);
		
		nod = root;
		//printTree(nod, true);
		
		calcAccuracy();
		printResult(0, true);
		
		resetAttrs();
		attrList.clear();
		sampleSpace.clear();
		
		//load test data
		updateData(args[1], attrList, sampleSpace);
		
		nod = root;
		//printTree(nod, true);
		
		calcAccuracy();
		printResult(1, true);			
		
		System.out.println("=======================================================================");
		System.out.println("=======================================================================");
		System.out.println("Random pick results\n");
		
		//training data		
		//for(int i = 0;i<5;++i)
		{
		resetAttrs();
		resetTreeAttrs();
		attrList.clear();
		sampleSpace.clear();
		updateData(args[0], attrList, sampleSpace);			
		ID3Alg(attrList, null, sampleSpace, true);		
		
		//print tree
		nod = root;
		printTree(nod, false);			
		
		//calculate accuracy
		calcAccuracy();
		printResult(2, false);	
		
		//reset attrs for test data
		resetAttrs();
		attrList.clear();
		sampleSpace.clear();
		
		//test data
		updateData(args[1], attrList, sampleSpace);
		
		calcAccuracy();
		printResult(3, false);	
		}
	}		
}
