package it.unina.daf.jpadcadfx;

import java.util.Collections;
import java.util.List;

import it.unina.daf.jpadcad.occ.OCCDataProvider;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;

public class OCCFXLeafNode extends OCCFXForm {

//		protected final OCCFXForm parent;
//
//		protected boolean visible = true;
//		protected boolean selected;
//		protected boolean pickable;
//		
//		// Useful for debugging
//		private String debugName;
//
//		private int [] selection = new int[0];
//		private Color color;
//		private OCCDataProvider dataProvider;
//
//		public OCCFXLeafNode(Group parentGroup, OCCDataProvider dataProvider, Color color) {
//			if (parentGroup == null) {
//				this.parent = new OCCFXForm();
//			} else {			
//				this.parent = (OCCFXForm) parentGroup;
//			}
//			this.parent.getChildren().add(this);
//			this.dataProvider = dataProvider;
//			this.color = color;
//		}
//
//		public List<OCCFXLeafNode> getLeaves()
//		{
//			return Collections.singletonList(this);
//		}
//
//		public Color getColor() {
//			return color;
//		}
//
//		public void setColor(Color color) {
//			if(this.color.equals(color))
//				return;
//			this.color = color;
//		}
//
//		public void setDataProvider(OCCDataProvider data) {
//			this.dataProvider = data;
//		}
//
//		public OCCDataProvider getDataProvider() {
//			return dataProvider;
//		}
//
//		public void setTransform(Transform transform) {
//			this.setTransform(transform);
//		}
//
//		public void refresh() {
//			
//			// TODO: add other necessary actions here? 
//			
//			refreshData();
//		}
//		
//		private void refreshData() {
//			
//			System.out.println("LeafNode :: refreshData()");
//			
//			dataProvider.load();
//			// createData(dataProvider); // TODO: ??
//			dataProvider.unLoad();
//		}
//
//		// see creates a data structure similar to vtkPolyData class
//		// void createData(DataProvider dataProvider) // ??
//		// TODO: find a possible alternative
//
//		public void select() {
//			selection = new int[0];
//
//			if(selected)
//				return;
//
//			selected = true;
//		}
//
//		public void unselect() {
//			if(!selected)
//				return;
//
//			selected = false;
//		}
//
//		public boolean isSelected() {
//			return selected;
//		}
//
//		public void setDebugName(String name) {
//			debugName = name;
//		}
//
//		public String toString()
//		{
//			StringBuilder sb = new StringBuilder(getClass().getName()+"@"+Integer.toHexString(hashCode()));
//			if (debugName != null)
//				sb.append(" "+debugName);
//			if (selected)
//				sb.append(" selected");
//
//			return sb.toString();
//		}
//		
//		// TODO:
//		// public void setEdgeVisible(boolean b)
//		// public void setCulling(boolean front, boolean back)
		
	}
