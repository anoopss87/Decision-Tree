
public class TNode 
{	
	int index;
	TNode left;
	TNode right;
	String name;
	int posCnt;
	int negCnt;
	int dir;
	
	TNode()
	{
		index = -1;
		name = "";
		left = null;
		right = null;
		posCnt = 0;
		negCnt = 0;
		dir = -1;
	}
	
	TNode(int v)
	{
		index = v;
		name = "";
		left = null;
		right = null;
		posCnt = 0;
		negCnt = 0;
		dir = -1;
	}
	
	TNode(int v, String s)
	{
		index = v;
		name = s;
		left = null;
		right = null;
		posCnt = 0;
		negCnt = 0;
		dir = -1;
	}
	
	int getIndex()
	{
		return index;
	}
	
	String getName()
	{
		return name;
	}
	
	TNode getRight()
	{
		return right;
	}
	
	TNode getLeft()
	{
		return left;
	}
	
	void setIndex(int val)
	{
		index = val;
	}
	
	void setName(String val)
	{
		name = val;
	}
	
	void setLeft(TNode val)
	{
		left = val;
	}
	
	void setRight(TNode val)
	{
		right = val;
	}
}
