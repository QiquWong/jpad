package it.unina.daf.jpadcad.enums;

import standaloneutils.MyArrayUtils;

public enum XSpacingType {

	UNIFORM {
		@Override
		public Double[] calculateSpacing(double x1, double x2, int n) {
			Double[] xSpacing = MyArrayUtils.linspaceDouble(x1, x2, n);
			return xSpacing;
		}
	},
	COSINUS {
		@Override
		public Double[] calculateSpacing(double x1, double x2, int n) {
			Double[] xSpacing = MyArrayUtils.cosineSpaceDouble(x1, x2, n);
			return xSpacing;
		}
	},
	HALFCOSINUS1 { // finer spacing close to x1
		@Override
		public Double[] calculateSpacing(double x1, double x2, int n) {
			Double[] xSpacing = MyArrayUtils.halfCosine1SpaceDouble(x1, x2, n);
			return xSpacing;
		}
	}, 
	HALFCOSINUS2 { // finer spacing close to x2
		@Override
		public Double[] calculateSpacing(double x1, double x2, int n) {
			Double[] xSpacing = MyArrayUtils.halfCosine2SpaceDouble(x1, x2, n);
			return xSpacing;
		}
	}; 

	public abstract Double[] calculateSpacing(double x1, double x2, int n);
}
