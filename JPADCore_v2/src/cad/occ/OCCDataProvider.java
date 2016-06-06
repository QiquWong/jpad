package cad.occ;

import javax.vecmath.Point3f;

import javafx.geometry.Point3D;
import javafx.scene.transform.Transform;

public class OCCDataProvider {
	private long modifiedTime = System.nanoTime();
	private float[] nodesTransformed = new float[0];
	protected float[] nodes = new float[0];
	protected float[] normals;
	protected int[] vertices = new int[0];
	protected int[] lines = new int[0];
	protected int[] polys = new int[0];
	protected int nbrOfPolys;
	protected int nbrOfLines;
	protected int nbrOfVertices;
	private Transform transform;
	public static final OCCDataProvider EMPTY = new OCCDataProvider()
	{
		@Override
		public void setVertices(int[] vertices)
		{
			throw new RuntimeException("DataProvider.EMPTY is immutable");
		}
		@Override
		public void setLines(int[] lines)
		{
			throw new RuntimeException("DataProvider.EMPTY is immutable");
		}
		@Override
		public void setNodes(float[] nodes)
		{
			throw new RuntimeException("DataProvider.EMPTY is immutable");
		}
		@Override
		public void setPolys(int nbrOfPolys, int[] polys)
		{
			throw new RuntimeException("DataProvider.EMPTY is immutable");
		}
	};
	public void setVertices(int[] vertices)
	{
		this.vertices = vertices;
		nbrOfVertices = vertices.length / 2;
		modified();
	}
	public void setLines(int[] lines)
	{
		this.lines = lines;
		nbrOfLines = lines.length / 3;
		modified();
	}
	public void setNodes(float[] nodes)
	{
		this.nodes = nodes;
		makeTransform();
		modified();
	}
	private void makeTransform()
	{
		if(transform != null)
		{
			this.nodesTransformed = new float[this.nodes.length];
			Point3f point = new Point3f();
			// Point3D point = new Point3D();
			int j = 0;
			for(int i = 0 ; i < this.nodes.length ; )
			{
				point.x = this.nodes[i++];
				point.y = this.nodes[i++];
				point.z = this.nodes[i++];
				// transform.transform(point);
				transform.transform(new Point3D(point.x, point.y, point.z));
				nodesTransformed[j++] = point.x;
				nodesTransformed[j++] = point.y;
				nodesTransformed[j++] = point.z;
			}
		}
		else
			this.nodesTransformed = this.nodes;
	}
	public void setTransform(Transform transform)
	{
		this.transform = transform;
		makeTransform();
	}
	public void setPolys(int nbrOfPolys, int[] polys)
	{
		this.nbrOfPolys = nbrOfPolys;
		this.polys = polys;
		modified();
	}
	public int getNbrOfPolys()
	{
		return nbrOfPolys;
	}
	public int getNbrOfLines()
	{
		return nbrOfLines;
	}
	public int getNbrOfVertices()
	{
		return nbrOfVertices;
	}
	public float[] getNodes()
	{
		return nodesTransformed;
	}
	public int[] getPolys()
	{
		return polys;
	}
	public int[] getLines()
	{
		return lines;
	}
	public int[] getVertices()
	{
		return vertices;
	}
	public float[] getNormals()
	{
		return normals;
	}
	public void load()
	{
		// Do nothing
	}
	public void unLoad()
	{
		// Do nothing
	}
	protected void clean()
	{
		nodes = new float[0];
		nodesTransformed = new float[0];
		normals = null;
		vertices = new int[0];
		lines = new int[0];
		polys = new int[0];
		nbrOfVertices = 0;
		nbrOfLines = 0;
		nbrOfPolys = 0;
		modified();
	}
	protected void modified()
	{
		modifiedTime = System.nanoTime();
	}
	protected long getModifiedTime()
	{
		return modifiedTime;
	}
}